const axios = require('axios');

// 121个学生的ID（从数据库实际情况来看）
const studentIds = Array.from({ length: 121 }, (_, i) => i + 1);

// 正态分布随机数生成器
function normalRandom(mean, stdDev) {
  let u1 = Math.random();
  let u2 = Math.random();
  let z0 = Math.sqrt(-2.0 * Math.log(u1)) * Math.cos(2.0 * Math.PI * u2);
  return Math.round(z0 * stdDev + mean);
}

// 生成成绩，确保在合理范围内
function generateScore(mean, stdDev, min = 0, max = 150) {
  let score = normalRandom(mean, stdDev);
  return Math.max(min, Math.min(max, score));
}

// 为每个学生生成第三周周考成绩
async function generateWeek3Scores() {
  console.log('开始生成第三周周考数据...');
  
  // 先获取第二周的成绩，用于制造成绩下降的情况
  let week2Scores = {};
  try {
    const response = await axios.get('http://localhost:8080/api/exam/list', {
      params: { examName: '第二周周考', pageNum: 1, pageSize: 200 }
    });
    if (response.data.data && response.data.data.records) {
      response.data.data.records.forEach(record => {
        week2Scores[record.studentId] = {
          chinese: record.chinese,
          math: record.math,
          english: record.english,
          comprehensive: record.comprehensive
        };
      });
    }
  } catch (error) {
    console.log('无法获取第二周成绩，将生成全新数据');
  }

  const scores = [];
  
  for (let i = 0; i < studentIds.length; i++) {
    const studentId = studentIds[i];
    let chinese, math, english, comprehensive;
    
    // 20%的学生成绩明显下降（触发预警）
    if (i < 24 && week2Scores[studentId]) {
      // 成绩下降超过20%
      chinese = Math.max(60, Math.round(week2Scores[studentId].chinese * 0.7));
      math = Math.max(50, Math.round(week2Scores[studentId].math * 0.75));
      english = Math.max(55, Math.round(week2Scores[studentId].english * 0.72));
      comprehensive = Math.max(100, Math.round(week2Scores[studentId].comprehensive * 0.73));
    }
    // 15%的学生成绩偏低（总分<400，触发风险预警）
    else if (i >= 24 && i < 42) {
      chinese = generateScore(75, 12, 60, 95);
      math = generateScore(70, 15, 50, 90);
      english = generateScore(72, 13, 55, 92);
      comprehensive = generateScore(140, 20, 110, 170);
    }
    // 10%的学生单科明显下降
    else if (i >= 42 && i < 54 && week2Scores[studentId]) {
      chinese = Math.max(70, week2Scores[studentId].chinese - 25); // 语文下降
      math = generateScore(105, 18);
      english = generateScore(108, 16);
      comprehensive = generateScore(200, 25);
    }
    // 其余学生正常分布
    else {
      // 优秀学生 (30%)
      if (i >= 54 && i < 90) {
        chinese = generateScore(115, 10, 100, 135);
        math = generateScore(120, 12, 105, 140);
        english = generateScore(118, 11, 100, 138);
        comprehensive = generateScore(220, 15, 190, 250);
      }
      // 中等学生 (25%)
      else if (i >= 90 && i < 110) {
        chinese = generateScore(100, 12, 85, 115);
        math = generateScore(95, 15, 75, 115);
        english = generateScore(98, 13, 80, 118);
        comprehensive = generateScore(190, 20, 160, 220);
      }
      // 普通学生 (10%)
      else {
        chinese = generateScore(90, 15, 70, 110);
        math = generateScore(88, 18, 65, 110);
        english = generateScore(92, 14, 70, 112);
        comprehensive = generateScore(180, 22, 150, 210);
      }
    }
    
    scores.push({
      studentId: studentId,
      examName: '第三周周考',
      chinese: chinese,
      math: math,
      english: english,
      comprehensive: comprehensive
    });
  }
  
  // 批量插入数据
  console.log(`准备插入 ${scores.length} 条第三周周考成绩记录...`);
  
  for (let i = 0; i < scores.length; i++) {
    try {
      await axios.post('http://localhost:8080/api/exam/add', scores[i]);
      if ((i + 1) % 20 === 0) {
        console.log(`已插入 ${i + 1}/${scores.length} 条记录`);
      }
    } catch (error) {
      console.error(`插入第 ${i + 1} 条记录失败:`, error.message);
    }
  }
  
  console.log('第三周周考数据生成完成！');
  
  // 统计信息
  const totalScores = scores.map(s => s.chinese + s.math + s.english + s.comprehensive);
  const avgTotal = totalScores.reduce((a, b) => a + b, 0) / totalScores.length;
  const below400 = totalScores.filter(t => t < 400).length;
  const above550 = totalScores.filter(t => t >= 550).length;
  
  console.log('\n=== 第三周周考统计 ===');
  console.log(`平均总分: ${avgTotal.toFixed(2)}`);
  console.log(`总分<400分: ${below400}人 (${(below400/121*100).toFixed(1)}%)`);
  console.log(`总分≥550分: ${above550}人 (${(above550/121*100).toFixed(1)}%)`);
  console.log(`预计触发预警: 约${24 + 18 + 12}人`);
}

generateWeek3Scores().catch(console.error);


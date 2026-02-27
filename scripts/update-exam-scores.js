const mysql = require('mysql2/promise');

async function updateExamScores() {
  const connection = await mysql.createConnection({
    host: '111.228.5.192',
    port: 3306,
    user: 'root',
    password: '123456',
    database: 'resume_demo'
  });

  console.log('数据库连接成功！');

  try {
    // 获取所有成绩记录
    const [scores] = await connection.execute('SELECT * FROM exam_score ORDER BY exam_name, total_score DESC');
    console.log(`获取到 ${scores.length} 条成绩记录`);

    // 按考试名称分组
    const scoresByExam = {};
    scores.forEach(score => {
      if (!scoresByExam[score.exam_name]) {
        scoresByExam[score.exam_name] = [];
      }
      scoresByExam[score.exam_name].push(score);
    });

    let updateCount = 0;

    // 为每次考试计算排名
    for (const [examName, examScores] of Object.entries(scoresByExam)) {
      console.log(`\n处理考试: ${examName} (${examScores.length}条记录)`);

      // 按班级分组
      const scoresByClass = {};
      examScores.forEach(score => {
        if (!scoresByClass[score.class_name]) {
          scoresByClass[score.class_name] = [];
        }
        scoresByClass[score.class_name].push(score);
      });

      // 计算年级排名（按总分降序）
      examScores.sort((a, b) => parseFloat(b.total_score) - parseFloat(a.total_score));
      
      for (let i = 0; i < examScores.length; i++) {
        const score = examScores[i];
        const gradeRank = i + 1;

        // 计算班级排名
        const classScores = scoresByClass[score.class_name];
        classScores.sort((a, b) => parseFloat(b.total_score) - parseFloat(a.total_score));
        const classRank = classScores.findIndex(s => s.id === score.id) + 1;

        // 预测大学
        const totalScore = parseFloat(score.total_score);
        let predictedUniversity;

        if (score.class_type === '理科') {
          if (totalScore >= 650) {
            predictedUniversity = '985高校(如中山大学、华南理工大学等)';
          } else if (totalScore >= 600) {
            predictedUniversity = '211高校(如暨南大学、华南师范大学等)';
          } else if (totalScore >= 574) {
            predictedUniversity = '一本院校';
          } else if (totalScore >= 516) {
            predictedUniversity = '二本院校';
          } else if (totalScore >= 400) {
            predictedUniversity = '专科院校';
          } else {
            predictedUniversity = '需要加强学习';
          }
        } else {
          if (totalScore >= 640) {
            predictedUniversity = '985高校(如中山大学、华南理工大学等)';
          } else if (totalScore >= 590) {
            predictedUniversity = '211高校(如暨南大学、华南师范大学等)';
          } else if (totalScore >= 594) {
            predictedUniversity = '一本院校';
          } else if (totalScore >= 546) {
            predictedUniversity = '二本院校';
          } else if (totalScore >= 430) {
            predictedUniversity = '专科院校';
          } else {
            predictedUniversity = '需要加强学习';
          }
        }

        // 更新数据库
        await connection.execute(
          'UPDATE exam_score SET class_rank = ?, grade_rank = ?, predicted_university = ? WHERE id = ?',
          [classRank, gradeRank, predictedUniversity, score.id]
        );

        updateCount++;
      }

      console.log(`  已更新 ${examScores.length} 条记录`);
    }

    console.log(`\n✅ 成绩更新完成！共更新 ${updateCount} 条记录`);

    // 统计信息
    const [stats] = await connection.execute(`
      SELECT 
        exam_name,
        COUNT(*) as total,
        AVG(total_score) as avg_score,
        MAX(total_score) as max_score,
        MIN(total_score) as min_score
      FROM exam_score
      GROUP BY exam_name
      ORDER BY exam_name
    `);

    console.log('\n=== 成绩统计 ===');
    stats.forEach(stat => {
      console.log(`${stat.exam_name}:`);
      console.log(`  总人数: ${stat.total}`);
      console.log(`  平均分: ${parseFloat(stat.avg_score).toFixed(2)}`);
      console.log(`  最高分: ${stat.max_score}`);
      console.log(`  最低分: ${stat.min_score}`);
    });

    // 检查预测大学分布
    const [universityStats] = await connection.execute(`
      SELECT 
        predicted_university,
        COUNT(*) as count
      FROM exam_score
      WHERE exam_name = '第三周周考'
      GROUP BY predicted_university
      ORDER BY count DESC
    `);

    console.log('\n=== 第三周周考预测大学分布 ===');
    universityStats.forEach(stat => {
      console.log(`${stat.predicted_university}: ${stat.count}人`);
    });

  } catch (error) {
    console.error('执行失败:', error);
  } finally {
    await connection.end();
    console.log('\n数据库连接已关闭');
  }
}

updateExamScores().catch(console.error);


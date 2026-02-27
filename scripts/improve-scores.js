const mysql = require('mysql2/promise');

async function improveScores() {
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
    const [scores] = await connection.execute('SELECT * FROM exam_score ORDER BY id');
    console.log(`获取到 ${scores.length} 条成绩记录`);

    let updateCount = 0;

    for (let i = 0; i < scores.length; i++) {
      const score = scores[i];
      let chinese, math, english, comprehensive;

      // 30%的学生成绩优秀（600+）
      if (i % 10 < 3) {
        chinese = 115 + Math.floor(Math.random() * 20); // 115-135
        math = 120 + Math.floor(Math.random() * 25); // 120-145
        english = 115 + Math.floor(Math.random() * 25); // 115-140
        comprehensive = 220 + Math.floor(Math.random() * 60); // 220-280
      }
      // 40%的学生成绩良好（550-600）
      else if (i % 10 < 7) {
        chinese = 105 + Math.floor(Math.random() * 15); // 105-120
        math = 110 + Math.floor(Math.random() * 20); // 110-130
        english = 105 + Math.floor(Math.random() * 20); // 105-125
        comprehensive = 200 + Math.floor(Math.random() * 40); // 200-240
      }
      // 20%的学生成绩中等（500-550）
      else if (i % 10 < 9) {
        chinese = 95 + Math.floor(Math.random() * 15); // 95-110
        math = 95 + Math.floor(Math.random() * 20); // 95-115
        english = 95 + Math.floor(Math.random() * 20); // 95-115
        comprehensive = 180 + Math.floor(Math.random() * 30); // 180-210
      }
      // 10%的学生需要加强（<500）
      else {
        chinese = 80 + Math.floor(Math.random() * 20); // 80-100
        math = 75 + Math.floor(Math.random() * 25); // 75-100
        english = 80 + Math.floor(Math.random() * 25); // 80-105
        comprehensive = 150 + Math.floor(Math.random() * 40); // 150-190
      }

      const totalScore = chinese + math + english + comprehensive;

      // 计算理综/文综各科
      let physics = null, chemistry = null, biology = null;
      let politics = null, history = null, geography = null;

      if (score.class_type === '理科') {
        physics = Math.round(comprehensive * 0.37 * 10) / 10;
        chemistry = Math.round(comprehensive * 0.33 * 10) / 10;
        biology = comprehensive - physics - chemistry;
      } else {
        politics = Math.round(comprehensive * 0.34 * 10) / 10;
        history = Math.round(comprehensive * 0.33 * 10) / 10;
        geography = comprehensive - politics - history;
      }

      // 预测大学
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
      await connection.execute(`
        UPDATE exam_score 
        SET chinese_score = ?, math_score = ?, english_score = ?, comprehensive_score = ?,
            physics_score = ?, chemistry_score = ?, biology_score = ?,
            politics_score = ?, history_score = ?, geography_score = ?,
            total_score = ?, predicted_university = ?
        WHERE id = ?
      `, [
        chinese, math, english, comprehensive,
        physics, chemistry, biology,
        politics, history, geography,
        totalScore, predictedUniversity, score.id
      ]);

      updateCount++;
      if (updateCount % 50 === 0) {
        console.log(`已更新 ${updateCount}/${scores.length} 条记录`);
      }
    }

    console.log(`\n✅ 成绩更新完成！共更新 ${updateCount} 条记录`);

    // 重新计算排名
    console.log('\n重新计算排名...');
    const [exams] = await connection.execute('SELECT DISTINCT exam_name FROM exam_score ORDER BY exam_name');
    
    for (const exam of exams) {
      const examName = exam.exam_name;
      const [examScores] = await connection.execute(
        'SELECT * FROM exam_score WHERE exam_name = ? ORDER BY total_score DESC',
        [examName]
      );

      // 按班级分组
      const scoresByClass = {};
      examScores.forEach(s => {
        if (!scoresByClass[s.class_name]) {
          scoresByClass[s.class_name] = [];
        }
        scoresByClass[s.class_name].push(s);
      });

      // 更新排名
      for (let i = 0; i < examScores.length; i++) {
        const s = examScores[i];
        const gradeRank = i + 1;
        
        const classScores = scoresByClass[s.class_name];
        const classRank = classScores.findIndex(cs => cs.id === s.id) + 1;

        await connection.execute(
          'UPDATE exam_score SET class_rank = ?, grade_rank = ? WHERE id = ?',
          [classRank, gradeRank, s.id]
        );
      }
    }

    console.log('✅ 排名计算完成');

    // 统计信息
    const [stats] = await connection.execute(`
      SELECT 
        exam_name,
        COUNT(*) as total,
        AVG(total_score) as avg_score,
        MAX(total_score) as max_score,
        MIN(total_score) as min_score,
        SUM(CASE WHEN total_score >= 600 THEN 1 ELSE 0 END) as above_600,
        SUM(CASE WHEN total_score >= 650 THEN 1 ELSE 0 END) as above_650
      FROM exam_score
      GROUP BY exam_name
      ORDER BY exam_name
    `);

    console.log('\n=== 成绩统计 ===');
    stats.forEach(stat => {
      console.log(`\n${stat.exam_name}:`);
      console.log(`  总人数: ${stat.total}`);
      console.log(`  平均分: ${parseFloat(stat.avg_score).toFixed(2)}`);
      console.log(`  最高分: ${stat.max_score}`);
      console.log(`  最低分: ${stat.min_score}`);
      console.log(`  600分以上: ${stat.above_600}人 (${(stat.above_600/stat.total*100).toFixed(1)}%)`);
      console.log(`  650分以上: ${stat.above_650}人 (${(stat.above_650/stat.total*100).toFixed(1)}%)`);
    });

  } catch (error) {
    console.error('执行失败:', error);
  } finally {
    await connection.end();
    console.log('\n数据库连接已关闭');
  }
}

improveScores().catch(console.error);


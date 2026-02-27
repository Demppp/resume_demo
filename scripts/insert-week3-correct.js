const mysql = require('mysql2/promise');

async function insertWeek3Data() {
  const connection = await mysql.createConnection({
    host: '111.228.5.192',
    port: 3306,
    user: 'root',
    password: '123456',
    database: 'resume_demo'
  });

  console.log('数据库连接成功！');

  try {
    // 先获取所有学生信息
    const [students] = await connection.execute('SELECT * FROM student ORDER BY id');
    console.log(`获取到 ${students.length} 个学生信息`);

    // 获取第二周成绩用于对比
    const [week2Scores] = await connection.execute(
      "SELECT * FROM exam_score WHERE exam_name = '第二周周考'"
    );
    
    const week2Map = {};
    week2Scores.forEach(score => {
      week2Map[score.student_id] = {
        chinese: parseFloat(score.chinese_score),
        math: parseFloat(score.math_score),
        english: parseFloat(score.english_score),
        comprehensive: parseFloat(score.comprehensive_score)
      };
    });

    console.log(`获取到 ${week2Scores.length} 条第二周成绩记录`);

    const examDate = '2013-05-22'; // 第三周周考日期
    let insertCount = 0;

    for (let i = 0; i < students.length; i++) {
      const student = students[i];
      let chinese, math, english, comprehensive;

      // 20%的学生成绩明显下降（触发预警）
      if (i < 24 && week2Map[student.id]) {
        chinese = Math.max(60, Math.round(week2Map[student.id].chinese * 0.7));
        math = Math.max(50, Math.round(week2Map[student.id].math * 0.75));
        english = Math.max(55, Math.round(week2Map[student.id].english * 0.72));
        comprehensive = Math.max(100, Math.round(week2Map[student.id].comprehensive * 0.73));
      }
      // 15%的学生成绩偏低（总分<400）
      else if (i >= 24 && i < 42) {
        chinese = 72 + Math.floor(Math.random() * 8);
        math = 65 + Math.floor(Math.random() * 8);
        english = 68 + Math.floor(Math.random() * 8);
        comprehensive = 135 + Math.floor(Math.random() * 20);
      }
      // 10%的学生单科明显下降
      else if (i >= 42 && i < 54 && week2Map[student.id]) {
        chinese = Math.max(70, week2Map[student.id].chinese - 25);
        math = 105 + Math.floor(Math.random() * 10);
        english = 108 + Math.floor(Math.random() * 8);
        comprehensive = 200 + Math.floor(Math.random() * 10);
      }
      // 优秀学生 (30%)
      else if (i >= 54 && i < 90) {
        chinese = 115 + Math.floor(Math.random() * 10);
        math = 120 + Math.floor(Math.random() * 12);
        english = 118 + Math.floor(Math.random() * 10);
        comprehensive = 220 + Math.floor(Math.random() * 15);
      }
      // 中等学生 (25%)
      else if (i >= 90 && i < 110) {
        chinese = 100 + Math.floor(Math.random() * 8);
        math = 95 + Math.floor(Math.random() * 10);
        english = 98 + Math.floor(Math.random() * 8);
        comprehensive = 190 + Math.floor(Math.random() * 15);
      }
      // 普通学生
      else {
        chinese = 90 + Math.floor(Math.random() * 15);
        math = 88 + Math.floor(Math.random() * 15);
        english = 92 + Math.floor(Math.random() * 12);
        comprehensive = 180 + Math.floor(Math.random() * 20);
      }

      const totalScore = chinese + math + english + comprehensive;

      const sql = `
        INSERT INTO exam_score 
        (student_id, student_name, class_name, class_type, exam_name, exam_date, 
         chinese_score, math_score, english_score, comprehensive_score, total_score)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
      `;

      await connection.execute(sql, [
        student.id,
        student.student_name,
        student.class_name,
        student.class_type || '理科', // 默认理科
        '第三周周考',
        examDate,
        chinese,
        math,
        english,
        comprehensive,
        totalScore
      ]);

      insertCount++;
      if (insertCount % 20 === 0) {
        console.log(`已插入 ${insertCount}/${students.length} 条记录`);
      }
    }

    console.log(`\n✅ 第三周周考数据插入完成！共插入 ${insertCount} 条记录`);

    // 统计信息
    const [stats] = await connection.execute(`
      SELECT 
        COUNT(*) as total,
        AVG(total_score) as avg_score,
        MIN(total_score) as min_score,
        MAX(total_score) as max_score,
        SUM(CASE WHEN total_score < 400 THEN 1 ELSE 0 END) as below_400,
        SUM(CASE WHEN total_score >= 550 THEN 1 ELSE 0 END) as above_550
      FROM exam_score 
      WHERE exam_name = '第三周周考'
    `);

    console.log('\n=== 第三周周考统计 ===');
    console.log(`总记录数: ${stats[0].total}`);
    console.log(`平均分: ${parseFloat(stats[0].avg_score).toFixed(2)}`);
    console.log(`最低分: ${stats[0].min_score}`);
    console.log(`最高分: ${stats[0].max_score}`);
    console.log(`总分<400分: ${stats[0].below_400}人 (${(stats[0].below_400/stats[0].total*100).toFixed(1)}%)`);
    console.log(`总分≥550分: ${stats[0].above_550}人 (${(stats[0].above_550/stats[0].total*100).toFixed(1)}%)`);

  } catch (error) {
    console.error('执行失败:', error);
  } finally {
    await connection.end();
    console.log('\n数据库连接已关闭');
  }
}

insertWeek3Data().catch(console.error);


const mysql = require('mysql2/promise');

async function insertWeek2Data() {
  const connection = await mysql.createConnection({
    host: '111.228.5.192',
    port: 3306,
    user: 'root',
    password: '123456',
    database: 'resume_demo'
  });

  console.log('数据库连接成功！');

  try {
    // 获取所有学生信息
    const [students] = await connection.execute('SELECT * FROM student ORDER BY id');
    console.log(`获取到 ${students.length} 个学生信息`);

    // 获取第一周成绩作为基准
    const [week1Scores] = await connection.execute(
      "SELECT * FROM exam_score WHERE exam_name = '第一周周考'"
    );
    
    const week1Map = {};
    week1Scores.forEach(score => {
      week1Map[score.student_id] = {
        chinese: parseFloat(score.chinese_score),
        math: parseFloat(score.math_score),
        english: parseFloat(score.english_score),
        comprehensive: parseFloat(score.comprehensive_score)
      };
    });

    console.log(`获取到 ${week1Scores.length} 条第一周成绩记录`);

    const examDate = '2013-05-15'; // 第二周周考日期
    let insertCount = 0;

    for (let i = 0; i < students.length; i++) {
      const student = students[i];
      let chinese, math, english, comprehensive;

      // 基于第一周成绩生成第二周成绩
      if (week1Map[student.id]) {
        // 大部分学生成绩略有波动（±5分）
        const variation = () => Math.floor(Math.random() * 11) - 5; // -5到+5
        
        chinese = Math.max(50, Math.min(150, Math.round(week1Map[student.id].chinese + variation())));
        math = Math.max(50, Math.min(150, Math.round(week1Map[student.id].math + variation())));
        english = Math.max(50, Math.min(150, Math.round(week1Map[student.id].english + variation())));
        comprehensive = Math.max(100, Math.min(300, Math.round(week1Map[student.id].comprehensive + variation() * 2)));
      } else {
        // 新学生，生成正常分布的成绩
        chinese = 100 + Math.floor(Math.random() * 30);
        math = 95 + Math.floor(Math.random() * 35);
        english = 98 + Math.floor(Math.random() * 32);
        comprehensive = 190 + Math.floor(Math.random() * 50);
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
        student.class_type || '理科',
        '第二周周考',
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

    console.log(`\n✅ 第二周周考数据插入完成！共插入 ${insertCount} 条记录`);

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
      WHERE exam_name = '第二周周考'
    `);

    console.log('\n=== 第二周周考统计 ===');
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

insertWeek2Data().catch(console.error);


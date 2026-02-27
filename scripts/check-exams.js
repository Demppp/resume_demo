const mysql = require('mysql2/promise');

async function checkExamData() {
  const connection = await mysql.createConnection({
    host: '111.228.5.192',
    port: 3306,
    user: 'root',
    password: '123456',
    database: 'resume_demo'
  });

  console.log('数据库连接成功！');

  try {
    const [exams] = await connection.execute(`
      SELECT exam_name, COUNT(*) as count 
      FROM exam_score 
      GROUP BY exam_name 
      ORDER BY exam_name
    `);
    
    console.log('\n现有考试数据统计:');
    exams.forEach(exam => {
      console.log(`${exam.exam_name}: ${exam.count}条记录`);
    });

  } catch (error) {
    console.error('查询失败:', error);
  } finally {
    await connection.end();
  }
}

checkExamData().catch(console.error);


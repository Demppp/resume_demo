const mysql = require('mysql2/promise');

async function testPagination() {
  const connection = await mysql.createConnection({
    host: '111.228.5.192',
    user: 'root',
    password: '123456',
    database: 'resume_demo'
  });

  try {
    // 测试学生总数
    const [countResult] = await connection.execute('SELECT COUNT(*) as total FROM student');
    console.log('学生总数:', countResult[0].total);

    // 测试成绩总数
    const [scoreCount] = await connection.execute('SELECT COUNT(*) as total FROM exam_score');
    console.log('成绩记录总数:', scoreCount[0].total);

    // 测试分页查询
    const [students] = await connection.execute('SELECT * FROM student LIMIT 10 OFFSET 0');
    console.log('第一页学生数:', students.length);

    // 测试考勤总数
    const [attendanceCount] = await connection.execute('SELECT COUNT(*) as total FROM attendance');
    console.log('考勤记录总数:', attendanceCount[0].total);

    // 测试日志总数
    const [diaryCount] = await connection.execute('SELECT COUNT(*) as total FROM class_diary');
    console.log('日志记录总数:', diaryCount[0].total);

  } catch (error) {
    console.error('错误:', error.message);
  } finally {
    await connection.end();
  }
}

testPagination();


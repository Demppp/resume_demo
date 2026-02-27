const mysql = require('mysql2/promise');

async function getStudentNames() {
  const connection = await mysql.createConnection({
    host: '111.228.5.192',
    port: 3306,
    user: 'root',
    password: '123456',
    database: 'resume_demo'
  });

  console.log('数据库连接成功！');

  try {
    const [students] = await connection.execute('SELECT student_name, class_name FROM student LIMIT 10');
    console.log('\n前10个学生信息:');
    students.forEach((s, i) => {
      console.log(`${i + 1}. ${s.student_name} - ${s.class_name}`);
    });

  } catch (error) {
    console.error('查询失败:', error);
  } finally {
    await connection.end();
  }
}

getStudentNames().catch(console.error);


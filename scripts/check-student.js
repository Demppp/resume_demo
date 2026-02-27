const mysql = require('mysql2/promise');

async function checkStudentData() {
  const connection = await mysql.createConnection({
    host: '111.228.5.192',
    port: 3306,
    user: 'root',
    password: '123456',
    database: 'resume_demo'
  });

  console.log('数据库连接成功！');

  try {
    const [students] = await connection.execute('SELECT * FROM student LIMIT 3');
    console.log('学生数据示例:');
    students.forEach(s => {
      console.log('\n学生ID:', s.id);
      console.log('姓名:', s.name);
      console.log('班级:', s.class_name);
      console.log('班级类型:', s.class_type);
      console.log('所有字段:', Object.keys(s));
    });

  } catch (error) {
    console.error('查询失败:', error);
  } finally {
    await connection.end();
  }
}

checkStudentData().catch(console.error);


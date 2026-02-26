const mysql = require('mysql2/promise');

async function addStudentToClass() {
  const connection = await mysql.createConnection({
    host: '111.228.5.192',
    user: 'root',
    password: '123456',
    database: 'resume_demo'
  });

  try {
    // 插入新学生到六班
    const [result] = await connection.execute(
      `INSERT INTO student (student_name, gender, address, parent_phone, class_id, class_name, class_type, student_number) 
       VALUES (?, ?, ?, ?, ?, ?, ?, ?)`,
      ['AI测试学生', '男', '广州市天河区AI测试路12200号', '13800138122', 6, '六班', '文科', '2013122']
    );

    console.log('✅ 成功添加学生到六班！学生ID:', result.insertId);

    // 更新班级人数
    await connection.execute(
      `UPDATE class_info SET student_count = student_count + 1 WHERE class_name = ?`,
      ['六班']
    );

    console.log('✅ 已更新六班人数');

    // 查询验证
    const [rows] = await connection.execute(
      `SELECT * FROM student WHERE class_name = ? ORDER BY id DESC LIMIT 1`,
      ['六班']
    );

    console.log('✅ 新添加的学生信息:', rows[0]);

  } catch (error) {
    console.error('❌ 错误:', error.message);
  } finally {
    await connection.end();
  }
}

addStudentToClass();


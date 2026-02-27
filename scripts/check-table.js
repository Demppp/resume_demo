const mysql = require('mysql2/promise');

async function checkTableStructure() {
  const connection = await mysql.createConnection({
    host: '111.228.5.192',
    port: 3306,
    user: 'root',
    password: '123456',
    database: 'resume_demo'
  });

  console.log('数据库连接成功！');

  try {
    // 查看表结构
    const [columns] = await connection.execute('DESCRIBE exam_score');
    console.log('\nexam_score 表结构:');
    console.table(columns);

    // 查看现有数据示例
    const [rows] = await connection.execute('SELECT * FROM exam_score LIMIT 1');
    console.log('\n现有数据示例:');
    console.log(rows[0]);

  } catch (error) {
    console.error('查询失败:', error);
  } finally {
    await connection.end();
  }
}

checkTableStructure().catch(console.error);


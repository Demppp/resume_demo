const mysql = require('mysql2/promise');
const fs = require('fs');
const path = require('path');

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
    // 读取SQL文件
    const sqlFile = path.join(__dirname, 'week3-exam-data.sql');
    const sqlContent = fs.readFileSync(sqlFile, 'utf8');
    
    // 分割SQL语句（按分号分割，忽略注释）
    const statements = sqlContent
      .split('\n')
      .filter(line => !line.trim().startsWith('--') && line.trim().length > 0)
      .join('\n')
      .split(';')
      .filter(stmt => stmt.trim().length > 0);

    console.log(`准备执行 ${statements.length} 条SQL语句...`);

    let successCount = 0;
    for (const statement of statements) {
      try {
        await connection.execute(statement.trim());
        successCount++;
        if (successCount % 10 === 0) {
          console.log(`已成功执行 ${successCount} 条语句`);
        }
      } catch (error) {
        console.error(`执行失败: ${error.message}`);
      }
    }

    console.log(`\n✅ 第三周周考数据插入完成！`);
    console.log(`成功: ${successCount} 条`);

    // 验证数据
    const [rows] = await connection.execute(
      "SELECT COUNT(*) as count FROM exam_score WHERE exam_name = '第三周周考'"
    );
    console.log(`\n数据库中第三周周考记录数: ${rows[0].count}`);

  } catch (error) {
    console.error('执行失败:', error);
  } finally {
    await connection.end();
    console.log('数据库连接已关闭');
  }
}

insertWeek3Data().catch(console.error);


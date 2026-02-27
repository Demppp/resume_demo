const mysql = require('mysql2/promise');

async function generateAbnormalAttendance() {
  const connection = await mysql.createConnection({
    host: '111.228.5.192',
    port: 3306,
    user: 'root',
    password: '123456',
    database: 'resume_demo'
  });

  console.log('数据库连接成功！');

  try {
    // 先清空考勤表
    await connection.execute('DELETE FROM attendance');
    console.log('已清空考勤表');

    // 获取所有学生
    const [students] = await connection.execute('SELECT * FROM student ORDER BY id');
    console.log(`获取到 ${students.length} 个学生信息`);

    // 生成最近7天的考勤数据（只记录异常）
    const today = new Date();
    
    let insertCount = 0;

    for (let i = 0; i < students.length; i++) {
      const student = students[i];
      
      // 为每个学生生成最近7天的异常考勤
      for (let day = 0; day < 7; day++) {
        const date = new Date(today);
        date.setDate(date.getDate() - day);
        const dateStr = date.toISOString().split('T')[0];
        
        let status = null;
        let reason = null;
        
        // 10%的学生有连续旷课（触发预警）
        if (i < 12) {
          if (day < 3) {
            status = '旷课';
            reason = '未请假缺席';
          }
        }
        // 15%的学生偶尔迟到/早退
        else if (i >= 12 && i < 30) {
          const rand = Math.random();
          if (rand < 0.3) {
            status = day % 2 === 0 ? '迟到' : '早退';
            reason = '交通原因';
          }
        }
        // 5%的学生请假
        else if (i >= 30 && i < 36) {
          if (day < 2) {
            status = '请假';
            reason = '身体不适';
          }
        }
        // 其余学生偶尔迟到
        else {
          const rand = Math.random();
          if (rand < 0.05) {
            status = '迟到';
            reason = '交通拥堵';
          }
        }
        
        // 只插入异常记录
        if (status && status !== '正常') {
          const sql = `
            INSERT INTO attendance 
            (student_id, student_name, class_name, attendance_date, attendance_status, reason)
            VALUES (?, ?, ?, ?, ?, ?)
          `;

          await connection.execute(sql, [
            student.id,
            student.student_name,
            student.class_name,
            dateStr,
            status,
            reason
          ]);

          insertCount++;
        }
      }
      
      if ((i + 1) % 20 === 0) {
        console.log(`已处理 ${i + 1}/${students.length} 个学生`);
      }
    }

    console.log(`\n✅ 异常考勤数据生成完成！共插入 ${insertCount} 条记录`);

    // 统计信息
    const [stats] = await connection.execute(`
      SELECT 
        attendance_status,
        COUNT(*) as count
      FROM attendance 
      GROUP BY attendance_status
    `);

    console.log('\n=== 异常考勤统计 ===');
    stats.forEach(stat => {
      console.log(`${stat.attendance_status}: ${stat.count}条`);
    });

    // 统计连续旷课的学生
    const [absenceStudents] = await connection.execute(`
      SELECT student_name, COUNT(*) as absence_count
      FROM attendance
      WHERE attendance_status = '旷课'
      GROUP BY student_id, student_name
      HAVING absence_count >= 2
      ORDER BY absence_count DESC
    `);

    console.log(`\n连续旷课学生: ${absenceStudents.length}人`);
    absenceStudents.forEach(s => {
      console.log(`  ${s.student_name}: ${s.absence_count}天`);
    });

  } catch (error) {
    console.error('执行失败:', error);
  } finally {
    await connection.end();
    console.log('\n数据库连接已关闭');
  }
}

generateAbnormalAttendance().catch(console.error);


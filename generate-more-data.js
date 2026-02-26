const mysql = require('mysql2/promise');

async function generateMoreData() {
  const connection = await mysql.createConnection({
    host: '111.228.5.192',
    user: 'root',
    password: '123456',
    database: 'resume_demo'
  });

  try {
    // 获取所有学生
    const [students] = await connection.execute('SELECT id, student_name, class_name FROM student');
    
    console.log('📅 开始生成考勤数据...');
    
    // 生成最近5天的考勤记录
    const dates = ['2013-05-20', '2013-05-21', '2013-05-22', '2013-05-23', '2013-05-24'];
    const statuses = ['正常', '迟到', '早退', '请假', '旷课'];
    const reasons = {
      '迟到': ['公交车晚点', '闹钟没响', '路上堵车'],
      '早退': ['身体不适', '家中有事', '看病'],
      '请假': ['生病', '家中有事', '参加比赛'],
      '旷课': ['无故缺席', '']
    };
    
    let attendanceCount = 0;
    for (const date of dates) {
      // 每天随机选择10-15个学生记录考勤异常
      const abnormalCount = 10 + Math.floor(Math.random() * 6);
      const selectedStudents = students.sort(() => 0.5 - Math.random()).slice(0, abnormalCount);
      
      for (const student of selectedStudents) {
        const status = statuses[Math.floor(Math.random() * statuses.length)];
        let reason = null;
        
        if (status !== '正常' && reasons[status]) {
          const reasonList = reasons[status];
          reason = reasonList[Math.floor(Math.random() * reasonList.length)];
        }
        
        await connection.execute(
          `INSERT INTO attendance (student_id, student_name, class_name, attendance_date, attendance_status, reason) 
           VALUES (?, ?, ?, ?, ?, ?)`,
          [student.id, student.student_name, student.class_name, date, status, reason]
        );
        attendanceCount++;
      }
    }
    
    console.log(`✅ 生成了 ${attendanceCount} 条考勤记录`);
    
    // 生成班干部日志
    console.log('📝 开始生成班干部日志...');
    
    const classes = ['一班', '二班', '三班', '四班', '五班', '六班'];
    const recorders = {
      '一班': '张伟',
      '二班': '马超',
      '三班': '姜涛',
      '四班': '方明',
      '五班': '左明',
      '六班': '花明'
    };
    
    const diaryTemplates = [
      '今天班级整体表现良好，早读认真，课堂纪律良好。体育课全班积极参与活动。',
      '上午数学课进行了小测验，大部分同学成绩理想。下午英语课气氛活跃。',
      '今天进行了班级卫生大扫除，同学们积极参与。晚自习秩序良好。',
      '早读时间学习效率高，午休纪律需要加强。下午化学实验课表现优秀。',
      '今天班会讨论了学习方法，同学们踊跃发言。课间秩序良好。'
    ];
    
    let diaryCount = 0;
    for (const date of dates) {
      for (const className of classes) {
        const content = diaryTemplates[Math.floor(Math.random() * diaryTemplates.length)];
        const summary = '班级整体表现良好，学习氛围浓厚。';
        
        await connection.execute(
          `INSERT INTO class_diary (class_name, diary_date, recorder_name, diary_content, ai_summary) 
           VALUES (?, ?, ?, ?, ?)`,
          [className, date, recorders[className], content, summary]
        );
        diaryCount++;
      }
    }
    
    console.log(`✅ 生成了 ${diaryCount} 条班干部日志`);
    
    console.log('🎉 所有数据生成完成！');

  } catch (error) {
    console.error('❌ 错误:', error.message);
  } finally {
    await connection.end();
  }
}

generateMoreData();


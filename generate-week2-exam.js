const mysql = require('mysql2/promise');

async function generateWeek2ExamScores() {
  const connection = await mysql.createConnection({
    host: '111.228.5.192',
    user: 'root',
    password: '123456',
    database: 'resume_demo'
  });

  try {
    // 获取所有学生
    const [students] = await connection.execute(
      `SELECT id, student_name, class_name, class_type FROM student ORDER BY id`
    );

    console.log(`📊 开始为 ${students.length} 名学生生成第二周周考成绩...`);

    let insertCount = 0;
    
    for (const student of students) {
      // 生成第二周成绩（与第一周有波动）
      const baseScore = 400 + (student.id % 30) * 10;
      const variance = Math.floor(Math.random() * 80) - 40; // 波动范围更大
      
      const chineseScore = Math.min(150, Math.max(80, 100 + (student.id % 15) * 3 + Math.random() * 25 - 5));
      const mathScore = Math.min(150, Math.max(70, 90 + (student.id % 20) * 3 + Math.random() * 30 - 10));
      const englishScore = Math.min(150, Math.max(75, 95 + (student.id % 18) * 3 + Math.random() * 25 - 5));
      const comprehensiveScore = Math.min(300, Math.max(180, 200 + (student.id % 25) * 4 + Math.random() * 50 - 15));
      
      const totalScore = chineseScore + mathScore + englishScore + comprehensiveScore;
      
      // 预测大学
      let predictedUniversity;
      if (student.class_type === '理科') {
        if (totalScore >= 650) {
          predictedUniversity = '985高校(如中山大学、华南理工大学等)';
        } else if (totalScore >= 600) {
          predictedUniversity = '211高校(如暨南大学、华南师范大学等)';
        } else if (totalScore >= 574) {
          predictedUniversity = '一本院校';
        } else if (totalScore >= 516) {
          predictedUniversity = '二本院校';
        } else if (totalScore >= 400) {
          predictedUniversity = '专科院校';
        } else {
          predictedUniversity = '需要加强学习';
        }
      } else {
        if (totalScore >= 640) {
          predictedUniversity = '985高校(如中山大学、华南理工大学等)';
        } else if (totalScore >= 590) {
          predictedUniversity = '211高校(如暨南大学、华南师范大学等)';
        } else if (totalScore >= 594) {
          predictedUniversity = '一本院校';
        } else if (totalScore >= 546) {
          predictedUniversity = '二本院校';
        } else if (totalScore >= 430) {
          predictedUniversity = '专科院校';
        } else {
          predictedUniversity = '需要加强学习';
        }
      }

      // 插入成绩
      await connection.execute(
        `INSERT INTO exam_score (student_id, student_name, class_name, class_type, exam_name, exam_date, 
         chinese_score, math_score, english_score, comprehensive_score, total_score, predicted_university) 
         VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)`,
        [
          student.id,
          student.student_name,
          student.class_name,
          student.class_type,
          '第二周周考',
          '2013-05-22',
          chineseScore.toFixed(1),
          mathScore.toFixed(1),
          englishScore.toFixed(1),
          comprehensiveScore.toFixed(1),
          totalScore.toFixed(1),
          predictedUniversity
        ]
      );

      insertCount++;
    }

    // 计算排名
    console.log('📈 正在计算班级排名和年级排名...');
    
    // 更新班级排名
    const [exams] = await connection.execute(
      `SELECT id, class_name, total_score FROM exam_score WHERE exam_name = '第二周周考' ORDER BY class_name, total_score DESC`
    );
    
    let currentClass = '';
    let classRank = 0;
    
    for (const exam of exams) {
      if (exam.class_name !== currentClass) {
        currentClass = exam.class_name;
        classRank = 1;
      } else {
        classRank++;
      }
      
      await connection.execute(
        `UPDATE exam_score SET class_rank = ? WHERE id = ?`,
        [classRank, exam.id]
      );
    }
    
    // 更新年级排名（按科类）
    const [gradeExams] = await connection.execute(
      `SELECT id, class_type, total_score FROM exam_score WHERE exam_name = '第二周周考' ORDER BY class_type, total_score DESC`
    );
    
    let currentType = '';
    let gradeRank = 0;
    
    for (const exam of gradeExams) {
      if (exam.class_type !== currentType) {
        currentType = exam.class_type;
        gradeRank = 1;
      } else {
        gradeRank++;
      }
      
      await connection.execute(
        `UPDATE exam_score SET grade_rank = ? WHERE id = ?`,
        [gradeRank, exam.id]
      );
    }

    console.log(`✅ 成功为 ${insertCount} 名学生生成第二周周考成绩！`);
    console.log('✅ 排名计算完成！');

  } catch (error) {
    console.error('❌ 错误:', error.message);
  } finally {
    await connection.end();
  }
}

generateWeek2ExamScores();


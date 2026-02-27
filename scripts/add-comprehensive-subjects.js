const mysql = require('mysql2/promise');

async function addComprehensiveSubjects() {
  const connection = await mysql.createConnection({
    host: '111.228.5.192',
    port: 3306,
    user: 'root',
    password: '123456',
    database: 'resume_demo'
  });

  console.log('数据库连接成功！');

  try {
    // 1. 添加新字段
    console.log('\n添加新字段...');
    
    // 理科：物理、化学、生物
    await connection.execute(`
      ALTER TABLE exam_score 
      ADD COLUMN physics_score DECIMAL(5,1) DEFAULT NULL COMMENT '物理成绩',
      ADD COLUMN chemistry_score DECIMAL(5,1) DEFAULT NULL COMMENT '化学成绩',
      ADD COLUMN biology_score DECIMAL(5,1) DEFAULT NULL COMMENT '生物成绩'
    `).catch(e => console.log('理科字段可能已存在'));
    
    // 文科：政治、历史、地理
    await connection.execute(`
      ALTER TABLE exam_score 
      ADD COLUMN politics_score DECIMAL(5,1) DEFAULT NULL COMMENT '政治成绩',
      ADD COLUMN history_score DECIMAL(5,1) DEFAULT NULL COMMENT '历史成绩',
      ADD COLUMN geography_score DECIMAL(5,1) DEFAULT NULL COMMENT '地理成绩'
    `).catch(e => console.log('文科字段可能已存在'));
    
    console.log('✅ 字段添加完成');

    // 2. 获取所有成绩记录
    const [scores] = await connection.execute('SELECT * FROM exam_score');
    console.log(`\n获取到 ${scores.length} 条成绩记录`);

    let updateCount = 0;

    // 3. 为每条记录生成各科分数
    for (const score of scores) {
      const comprehensiveScore = parseFloat(score.comprehensive_score);
      
      if (score.class_type === '理科') {
        // 理综 = 物理 + 化学 + 生物
        // 物理：110分满分，化学：100分满分，生物：90分满分
        const physics = Math.round((comprehensiveScore * 0.37) * 10) / 10; // 约37%
        const chemistry = Math.round((comprehensiveScore * 0.33) * 10) / 10; // 约33%
        const biology = comprehensiveScore - physics - chemistry; // 剩余30%
        
        await connection.execute(
          'UPDATE exam_score SET physics_score = ?, chemistry_score = ?, biology_score = ? WHERE id = ?',
          [physics, chemistry, biology, score.id]
        );
      } else {
        // 文综 = 政治 + 历史 + 地理
        // 每科100分满分
        const politics = Math.round((comprehensiveScore * 0.34) * 10) / 10;
        const history = Math.round((comprehensiveScore * 0.33) * 10) / 10;
        const geography = comprehensiveScore - politics - history;
        
        await connection.execute(
          'UPDATE exam_score SET politics_score = ?, history_score = ?, geography_score = ? WHERE id = ?',
          [politics, history, geography, score.id]
        );
      }
      
      updateCount++;
      if (updateCount % 50 === 0) {
        console.log(`已更新 ${updateCount}/${scores.length} 条记录`);
      }
    }

    console.log(`\n✅ 成绩更新完成！共更新 ${updateCount} 条记录`);

    // 4. 验证数据
    const [sample] = await connection.execute(`
      SELECT 
        student_name, class_type, comprehensive_score,
        physics_score, chemistry_score, biology_score,
        politics_score, history_score, geography_score
      FROM exam_score 
      LIMIT 5
    `);

    console.log('\n=== 数据示例 ===');
    sample.forEach(s => {
      console.log(`\n${s.student_name} (${s.class_type}):`);
      console.log(`  综合分: ${s.comprehensive_score}`);
      if (s.class_type === '理科') {
        const sum = parseFloat(s.physics_score) + parseFloat(s.chemistry_score) + parseFloat(s.biology_score);
        console.log(`  物理: ${s.physics_score}, 化学: ${s.chemistry_score}, 生物: ${s.biology_score}`);
        console.log(`  总和: ${sum.toFixed(1)} (差值: ${Math.abs(sum - parseFloat(s.comprehensive_score)).toFixed(1)})`);
      } else {
        const sum = parseFloat(s.politics_score) + parseFloat(s.history_score) + parseFloat(s.geography_score);
        console.log(`  政治: ${s.politics_score}, 历史: ${s.history_score}, 地理: ${s.geography_score}`);
        console.log(`  总和: ${sum.toFixed(1)} (差值: ${Math.abs(sum - parseFloat(s.comprehensive_score)).toFixed(1)})`);
      }
    });

  } catch (error) {
    console.error('执行失败:', error);
  } finally {
    await connection.end();
    console.log('\n数据库连接已关闭');
  }
}

addComprehensiveSubjects().catch(console.error);


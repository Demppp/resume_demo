const axios = require('axios');

async function testPagination() {
  const baseURL = 'http://localhost:8080/api';
  
  console.log('🧪 开始测试分页功能...\n');
  
  try {
    // 测试学生列表分页
    console.log('1️⃣ 测试学生列表分页');
    const studentPage1 = await axios.get(`${baseURL}/student/list`, {
      params: { pageNum: 1, pageSize: 10 }
    });
    console.log(`   第1页: ${studentPage1.data.data.records.length} 条记录`);
    console.log(`   总记录数: ${studentPage1.data.data.total}`);
    console.log(`   总页数: ${studentPage1.data.data.pages}`);
    
    const studentPage2 = await axios.get(`${baseURL}/student/list`, {
      params: { pageNum: 2, pageSize: 10 }
    });
    console.log(`   第2页: ${studentPage2.data.data.records.length} 条记录\n`);
    
    // 测试成绩列表分页
    console.log('2️⃣ 测试成绩列表分页');
    const examPage1 = await axios.get(`${baseURL}/exam/list`, {
      params: { pageNum: 1, pageSize: 10 }
    });
    console.log(`   第1页: ${examPage1.data.data.records.length} 条记录`);
    console.log(`   总记录数: ${examPage1.data.data.total}`);
    console.log(`   总页数: ${examPage1.data.data.pages}`);
    
    const examPage2 = await axios.get(`${baseURL}/exam/list`, {
      params: { pageNum: 2, pageSize: 10 }
    });
    console.log(`   第2页: ${examPage2.data.data.records.length} 条记录\n`);
    
    // 测试考勤列表分页
    console.log('3️⃣ 测试考勤列表分页');
    const attendancePage1 = await axios.get(`${baseURL}/attendance/list`, {
      params: { pageNum: 1, pageSize: 10 }
    });
    console.log(`   第1页: ${attendancePage1.data.data.records.length} 条记录`);
    console.log(`   总记录数: ${attendancePage1.data.data.total}`);
    console.log(`   总页数: ${attendancePage1.data.data.pages}\n`);
    
    // 测试日志列表分页
    console.log('4️⃣ 测试日志列表分页');
    const diaryPage1 = await axios.get(`${baseURL}/diary/list`, {
      params: { pageNum: 1, pageSize: 10 }
    });
    console.log(`   第1页: ${diaryPage1.data.data.records.length} 条记录`);
    console.log(`   总记录数: ${diaryPage1.data.data.total}`);
    console.log(`   总页数: ${diaryPage1.data.data.pages}\n`);
    
    console.log('✅ 分页测试完成！');
    
  } catch (error) {
    console.error('❌ 测试失败:', error.message);
    if (error.response) {
      console.error('   响应状态:', error.response.status);
      console.error('   响应数据:', error.response.data);
    }
  }
}

testPagination();


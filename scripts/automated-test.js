const puppeteer = require('puppeteer');
const fs = require('fs');
const path = require('path');

// 创建测试数据目录
const testDataDir = path.join(__dirname, '../testData');
if (!fs.existsSync(testDataDir)) {
  fs.mkdirSync(testDataDir, { recursive: true });
}

// 测试结果
const testResults = [];

async function sleep(ms) {
  return new Promise(resolve => setTimeout(resolve, ms));
}

async function takeScreenshot(page, name) {
  const screenshotPath = path.join(testDataDir, `${name}.png`);
  await page.screenshot({ path: screenshotPath, fullPage: true });
  console.log(`✅ 截图已保存: ${name}.png`);
  return screenshotPath;
}

async function runTest(testName, testFn) {
  console.log(`\n🧪 开始测试: ${testName}`);
  const startTime = Date.now();
  try {
    await testFn();
    const duration = Date.now() - startTime;
    testResults.push({
      name: testName,
      status: '✅ 通过',
      duration: `${duration}ms`,
      error: null
    });
    console.log(`✅ 测试通过: ${testName} (${duration}ms)`);
  } catch (error) {
    const duration = Date.now() - startTime;
    testResults.push({
      name: testName,
      status: '❌ 失败',
      duration: `${duration}ms`,
      error: error.message
    });
    console.error(`❌ 测试失败: ${testName}`, error.message);
  }
}

async function main() {
  console.log('🚀 开始自动化测试...\n');
  
  const browser = await puppeteer.launch({
    headless: false,
    defaultViewport: { width: 1920, height: 1080 },
    args: ['--start-maximized']
  });

  const page = await browser.newPage();
  
  try {
    // 测试1: 首页加载
    await runTest('1. 首页加载测试', async () => {
      await page.goto('http://localhost:3000', { waitUntil: 'networkidle0' });
      await sleep(2000);
      await takeScreenshot(page, '01-首页加载');
      
      const title = await page.title();
      if (!title) throw new Error('页面标题为空');
    });

    // 测试2: 数据看板
    await runTest('2. 数据看板测试', async () => {
      await page.click('a[href="/dashboard"]');
      await sleep(3000);
      await takeScreenshot(page, '02-数据看板');
      
      // 检查统计卡片是否存在
      const cards = await page.$$('.stat-card');
      if (cards.length < 4) throw new Error('统计卡片数量不足');
    });

    // 测试3: 学生管理
    await runTest('3. 学生管理测试', async () => {
      await page.click('a[href="/student"]');
      await sleep(2000);
      await takeScreenshot(page, '03-学生管理');
      
      // 检查表格是否存在
      const table = await page.$('.el-table');
      if (!table) throw new Error('学生列表表格未找到');
    });

    // 测试4: 考勤管理
    await runTest('4. 考勤管理测试', async () => {
      await page.click('a[href="/attendance"]');
      await sleep(2000);
      await takeScreenshot(page, '04-考勤管理');
      
      const table = await page.$('.el-table');
      if (!table) throw new Error('考勤列表表格未找到');
    });

    // 测试5: 成绩管理
    await runTest('5. 成绩管理测试', async () => {
      await page.click('a[href="/exam"]');
      await sleep(2000);
      await takeScreenshot(page, '05-成绩管理');
      
      // 检查统计卡片
      const statCards = await page.$$('.stat-card');
      if (statCards.length < 4) throw new Error('成绩统计卡片数量不足');
      
      // 检查表格
      const table = await page.$('.el-table');
      if (!table) throw new Error('成绩列表表格未找到');
    });

    // 测试6: 班干部日志
    await runTest('6. 班干部日志测试', async () => {
      await page.click('a[href="/diary"]');
      await sleep(2000);
      await takeScreenshot(page, '06-班干部日志');
      
      const table = await page.$('.el-table');
      if (!table) throw new Error('日志列表表格未找到');
    });

    // 测试7: 预警系统
    await runTest('7. 预警系统测试', async () => {
      await page.click('a[href="/warning"]');
      await sleep(3000);
      await takeScreenshot(page, '07-预警系统');
      
      // 检查预警统计卡片
      const statCards = await page.$$('.stat-card');
      if (statCards.length < 4) throw new Error('预警统计卡片数量不足');
      
      // 检查预警列表
      const table = await page.$('.el-table');
      if (!table) throw new Error('预警列表表格未找到');
    });

    // 测试8: 数据分析
    await runTest('8. 数据分析测试', async () => {
      await page.click('a[href="/analytics"]');
      await sleep(5000); // 等待图表渲染
      await takeScreenshot(page, '08-数据分析');
      
      // 检查图表容器
      const charts = await page.$$('.chart-card');
      if (charts.length < 3) throw new Error('图表数量不足');
    });

    // 测试9: AI搜索功能
    await runTest('9. AI搜索功能测试', async () => {
      // 点击搜索按钮
      await page.click('button[type="primary"]');
      await sleep(1000);
      await takeScreenshot(page, '09-AI搜索对话框');
      
      // 输入搜索内容
      await page.type('textarea', '查看张伟的第一周周考成绩');
      await sleep(500);
      await takeScreenshot(page, '09-AI搜索输入');
      
      // 关闭对话框
      await page.keyboard.press('Escape');
      await sleep(500);
    });

    // 测试10: 成绩管理搜索功能
    await runTest('10. 成绩管理搜索测试', async () => {
      await page.click('a[href="/exam"]');
      await sleep(2000);
      
      // 输入学生姓名
      await page.type('input[placeholder="请输入学生姓名"]', '张伟');
      await sleep(500);
      
      // 点击查询按钮
      await page.click('button.el-button--primary');
      await sleep(2000);
      await takeScreenshot(page, '10-成绩搜索结果');
      
      // 检查搜索结果
      const rows = await page.$$('.el-table__row');
      if (rows.length === 0) throw new Error('搜索结果为空');
    });

  } catch (error) {
    console.error('❌ 测试过程中发生错误:', error);
    await takeScreenshot(page, 'error-screenshot');
  } finally {
    await browser.close();
  }

  // 生成测试报告
  generateReport();
}

function generateReport() {
  console.log('\n' + '='.repeat(80));
  console.log('📊 测试报告');
  console.log('='.repeat(80));
  
  const totalTests = testResults.length;
  const passedTests = testResults.filter(r => r.status.includes('通过')).length;
  const failedTests = totalTests - passedTests;
  const passRate = ((passedTests / totalTests) * 100).toFixed(2);
  
  console.log(`\n总测试数: ${totalTests}`);
  console.log(`通过: ${passedTests}`);
  console.log(`失败: ${failedTests}`);
  console.log(`通过率: ${passRate}%\n`);
  
  console.log('详细结果:');
  console.log('-'.repeat(80));
  testResults.forEach((result, index) => {
    console.log(`${index + 1}. ${result.name}`);
    console.log(`   状态: ${result.status}`);
    console.log(`   耗时: ${result.duration}`);
    if (result.error) {
      console.log(`   错误: ${result.error}`);
    }
    console.log('-'.repeat(80));
  });
  
  // 生成HTML报告
  const htmlReport = generateHTMLReport(totalTests, passedTests, failedTests, passRate);
  const reportPath = path.join(testDataDir, 'test-report.html');
  fs.writeFileSync(reportPath, htmlReport);
  console.log(`\n📄 HTML测试报告已生成: ${reportPath}`);
  
  // 生成JSON报告
  const jsonReport = {
    timestamp: new Date().toISOString(),
    summary: {
      total: totalTests,
      passed: passedTests,
      failed: failedTests,
      passRate: passRate + '%'
    },
    results: testResults
  };
  const jsonPath = path.join(testDataDir, 'test-report.json');
  fs.writeFileSync(jsonPath, JSON.stringify(jsonReport, null, 2));
  console.log(`📄 JSON测试报告已生成: ${jsonPath}`);
  
  console.log('\n✨ 测试完成！所有截图已保存到 testData 目录\n');
}

function generateHTMLReport(total, passed, failed, passRate) {
  return `
<!DOCTYPE html>
<html lang="zh-CN">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>自动化测试报告</title>
  <style>
    * { margin: 0; padding: 0; box-sizing: border-box; }
    body {
      font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', sans-serif;
      background: #f5f7fa;
      padding: 20px;
    }
    .container {
      max-width: 1200px;
      margin: 0 auto;
      background: white;
      border-radius: 8px;
      box-shadow: 0 2px 12px rgba(0,0,0,0.1);
      padding: 40px;
    }
    h1 {
      color: #2c3e50;
      margin-bottom: 10px;
      font-size: 32px;
    }
    .timestamp {
      color: #8c8c8c;
      margin-bottom: 30px;
    }
    .summary {
      display: grid;
      grid-template-columns: repeat(4, 1fr);
      gap: 20px;
      margin-bottom: 40px;
    }
    .summary-card {
      padding: 20px;
      border-radius: 8px;
      text-align: center;
    }
    .summary-card.total { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; }
    .summary-card.passed { background: linear-gradient(135deg, #52c41a 0%, #389e0d 100%); color: white; }
    .summary-card.failed { background: linear-gradient(135deg, #f5222d 0%, #cf1322 100%); color: white; }
    .summary-card.rate { background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%); color: white; }
    .summary-value {
      font-size: 36px;
      font-weight: 700;
      margin-bottom: 8px;
    }
    .summary-label {
      font-size: 14px;
      opacity: 0.9;
    }
    .test-results {
      margin-top: 30px;
    }
    .test-item {
      border: 1px solid #e8e8e8;
      border-radius: 8px;
      padding: 20px;
      margin-bottom: 15px;
      transition: all 0.3s;
    }
    .test-item:hover {
      box-shadow: 0 4px 12px rgba(0,0,0,0.1);
    }
    .test-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 10px;
    }
    .test-name {
      font-size: 16px;
      font-weight: 600;
      color: #2c3e50;
    }
    .test-status {
      padding: 4px 12px;
      border-radius: 4px;
      font-size: 14px;
      font-weight: 500;
    }
    .test-status.passed {
      background: #f6ffed;
      color: #52c41a;
      border: 1px solid #b7eb8f;
    }
    .test-status.failed {
      background: #fff1f0;
      color: #f5222d;
      border: 1px solid #ffa39e;
    }
    .test-duration {
      color: #8c8c8c;
      font-size: 14px;
    }
    .test-error {
      margin-top: 10px;
      padding: 10px;
      background: #fff1f0;
      border-left: 3px solid #f5222d;
      color: #f5222d;
      font-size: 14px;
    }
    .screenshots {
      margin-top: 40px;
    }
    .screenshots h2 {
      color: #2c3e50;
      margin-bottom: 20px;
    }
    .screenshot-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
      gap: 20px;
    }
    .screenshot-item {
      border: 1px solid #e8e8e8;
      border-radius: 8px;
      overflow: hidden;
      transition: all 0.3s;
    }
    .screenshot-item:hover {
      box-shadow: 0 4px 12px rgba(0,0,0,0.1);
      transform: translateY(-2px);
    }
    .screenshot-item img {
      width: 100%;
      height: 200px;
      object-fit: cover;
    }
    .screenshot-name {
      padding: 10px;
      text-align: center;
      font-size: 14px;
      color: #595959;
      background: #fafafa;
    }
  </style>
</head>
<body>
  <div class="container">
    <h1>🧪 自动化测试报告</h1>
    <div class="timestamp">生成时间: ${new Date().toLocaleString('zh-CN')}</div>
    
    <div class="summary">
      <div class="summary-card total">
        <div class="summary-value">${total}</div>
        <div class="summary-label">总测试数</div>
      </div>
      <div class="summary-card passed">
        <div class="summary-value">${passed}</div>
        <div class="summary-label">通过</div>
      </div>
      <div class="summary-card failed">
        <div class="summary-value">${failed}</div>
        <div class="summary-label">失败</div>
      </div>
      <div class="summary-card rate">
        <div class="summary-value">${passRate}%</div>
        <div class="summary-label">通过率</div>
      </div>
    </div>
    
    <div class="test-results">
      <h2>测试详情</h2>
      ${testResults.map((result, index) => `
        <div class="test-item">
          <div class="test-header">
            <div class="test-name">${result.name}</div>
            <div class="test-status ${result.status.includes('通过') ? 'passed' : 'failed'}">
              ${result.status}
            </div>
          </div>
          <div class="test-duration">耗时: ${result.duration}</div>
          ${result.error ? `<div class="test-error">错误: ${result.error}</div>` : ''}
        </div>
      `).join('')}
    </div>
    
    <div class="screenshots">
      <h2>📸 测试截图</h2>
      <div class="screenshot-grid">
        ${testResults.map((result, index) => {
          const screenshotName = result.name.split('.')[1].trim().replace(/测试$/, '');
          const screenshotFile = `${String(index + 1).padStart(2, '0')}-${screenshotName}.png`;
          return `
            <div class="screenshot-item">
              <img src="${screenshotFile}" alt="${screenshotName}">
              <div class="screenshot-name">${screenshotName}</div>
            </div>
          `;
        }).join('')}
      </div>
    </div>
  </div>
</body>
</html>
  `;
}

main().catch(console.error);


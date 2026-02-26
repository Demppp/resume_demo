# 班级管理系统

## 项目简介
这是一个为高三年级主任设计的班级管理系统，基于2013年广东高考制度开发。

## 技术栈

### 后端
- Java 17
- Spring Boot 3.1.5
- MySQL 8.0
- MyBatis Plus 3.5.4
- LangChain4j (集成通义千问)

### 前端
- Vue 3
- Element Plus
- Vite
- Axios

## 功能模块

### 1. 学生管理
- 学生信息的增删改查
- 支持AI一句话添加学生（通过LangChain解析）
- 按班级、科类筛选

### 2. 考勤管理
- 记录学生考勤情况（正常/迟到/早退/请假/旷课）
- 记录考勤原因
- 按班级、日期范围查询

### 3. 成绩管理
- 录入周考成绩（语文、数学、英语、文综/理综）
- 自动计算总分
- 自动计算班级排名和年级排名
- 基于2013年广东高考分数线预测大学

### 4. 班干部日志
- 班干部每日记录班级情况
- AI自动生成一句话总结（通过LangChain）

## 项目结构

```
resumeDemo/
├── backend/                    # 后端项目
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/classmanagement/
│   │   │   │   ├── controller/    # 控制器层
│   │   │   │   ├── service/       # 业务逻辑层
│   │   │   │   ├── mapper/        # 数据访问层
│   │   │   │   ├── entity/        # 实体类
│   │   │   │   ├── dto/           # 数据传输对象
│   │   │   │   └── config/        # 配置类
│   │   │   └── resources/
│   │   │       ├── application.yml
│   │   │       └── schema.sql
│   │   └── test/
│   └── pom.xml
└── frontend/                   # 前端项目
    ├── src/
    │   ├── views/             # 页面组件
    │   ├── api/               # API接口
    │   ├── router/            # 路由配置
    │   ├── App.vue
    │   └── main.js
    ├── index.html
    ├── vite.config.js
    └── package.json
```

## 安装和运行

### 后端

1. 确保已安装 Java 17 和 Maven
2. 创建MySQL数据库并执行 `backend/src/main/resources/schema.sql`
3. 修改 `application.yml` 中的数据库配置
4. 在 backend 目录下运行：
```bash
mvn clean install
mvn spring-boot:run
```

后端将在 http://localhost:8080 启动

### 前端

1. 确保已安装 Node.js (建议 16+)
2. 在 frontend 目录下运行：
```bash
npm install
npm run dev
```

前端将在 http://localhost:3000 启动

## 配置说明

### 数据库配置
在 `backend/src/main/resources/application.yml` 中修改：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/class_management
    username: root
    password: root
```

### LangChain配置
已配置通义千问API：
```yaml
langchain:
  qwen:
    api-key: sk-cbc7273aedbe42d39d9410f0e3db6c9c
    model-name: qwen-turbo
```

## 2013年广东高考分数线参考

### 理科
- 一本线：574分
- 二本线：516分

### 文科
- 一本线：594分
- 二本线：546分

系统会根据学生总分自动预测可能录取的大学层次。

## 班级设置
- 一班、二班、三班：理科班
- 四班、五班、六班：文科班
- 每班约20人

## 注意事项
1. 首次运行需要先执行数据库初始化脚本
2. 确保通义千问API Key有效
3. 前端代理配置已设置，开发时前后端分离运行


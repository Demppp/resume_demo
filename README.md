# 高三班级管理系统 — AI RAG 增强版

> 使用 Cursor 辅助开发 · pgvector 语义检索 · LangChain4j · 千问大模型 · Spring AI

## 项目简介

这是一个为高三年级主任设计的班级管理系统，基于 2013 年广东高考制度开发。

本项目的核心亮点不是 CRUD，而是构建了一套完整的 **AI RAG（检索增强生成）业务闭环**：将学生成绩趋势、出勤行为、年级排名等多维度数据生成**学情画像**并向量化存储到 pgvector，支持用自然语言查询最相关学生，再由千问大模型给出专业分析与干预建议。

---

## 核心 AI 技术亮点

### 1. 真实 RAG 业务闭环

```
用户自然语言输入
       ↓
千问 text-embedding-v3 向量化（1024维）
       ↓
PostgreSQL + pgvector IVFFlat 索引语义检索
       ↓
召回最相关 Top-K 学生画像
       ↓
构建增强 Prompt → 千问 qwen-turbo 生成分析报告
       ↓
返回：学生列表 + 相似度 + AI 分析建议
```

这不是简单的 Prompt 堆砌，而是将**业务数据向量化**后做语义检索，再增强大模型输出。

### 2. 学情画像向量化

向量化的不是原始数字，而是结构化的**语义摘要文本**，例如：

```
张伟，高三1班，理科。近3次考试总分：580→542→510。
当前年级排名第45名。较上次考试下滑32分，退步明显。
成绩呈持续下降趋势。近30天缺勤6次，出勤问题严重，需重点关注。
综合风险等级：高。
```

这种设计使语义相似度更准确，能理解"退步明显"、"风险高"等语义。

### 3. pgvector 余弦距离检索

```sql
SELECT student_name, risk_level,
       1 - (embedding <=> '[...query vector...]'::vector) AS similarity
FROM student_profile_embedding
WHERE embedding IS NOT NULL
ORDER BY embedding <=> '[...query vector...]'::vector
LIMIT 5;
```

使用 `<=>` 余弦距离运算符，配合 IVFFlat 索引实现高效语义检索。

### 4. 技术选型说明

| 组件 | 选择 | 原因 |
|------|------|------|
| 数据库 | PostgreSQL + pgvector | 原生向量存储，避免引入独立向量库 |
| ORM | Spring Data JPA | 与 pgvector 兼容性更好，生态完整 |
| Embedding | 千问 text-embedding-v3 | 1024维，中文语义理解更准确 |
| LLM | 千问 qwen-turbo | 国内稳定，成本低，中文效果好 |
| RAG框架 | LangChain4j | Java 生态最成熟的 LLM 集成框架 |
| 开发工具 | Cursor + MCP | AI辅助编程，提升开发效率 |

---

## 技术栈

### 后端
- Java 17
- Spring Boot 3.2.5
- **PostgreSQL 15 + pgvector**（向量存储与语义检索）
- Spring Data JPA + Hibernate 6
- **LangChain4j 0.36.2**（Embedding + Chat 模型集成）
- **千问大模型**（text-embedding-v3 + qwen-turbo）

### 前端
- Vue 3 + Vite
- Element Plus
- Axios
- Marked（Markdown 渲染）

---

## 功能模块

### AI 学情分析（核心展示功能）
- 自然语言查询学生学情（如："找出近三次考试退步明显且缺勤较多的学生"）
- pgvector 语义相似度检索，返回最相关学生画像
- 千问大模型生成专业分析报告和干预建议
- 支持手动触发全量向量索引重建

### 学生管理
- 学生信息增删改查
- 按班级、科类筛选

### 考勤管理
- 记录学生出勤情况（正常/迟到/早退/请假/旷课）
- 按班级、日期范围查询

### 成绩管理
- 录入周考成绩（语数英 + 文综/理综）
- 自动计算总分、班级排名、年级排名
- 基于 2013 年广东高考分数线预测大学层次

---

## 快速启动

### 前置条件
- Java 17
- Maven 3.8+
- Node.js 16+
- PostgreSQL 15+（需启用 pgvector 扩展）

### 数据库初始化

```sql
-- 启用 pgvector
CREATE EXTENSION IF NOT EXISTS vector;

-- 执行建表脚本
-- backend/src/main/resources/schema.sql
```

### 后端启动

```bash
cd backend
mvn spring-boot:run
```

后端在 `http://localhost:8080` 启动，context path 为 `/api`

### 前端启动

```bash
cd frontend
npm install
npm run dev
```

前端在 `http://localhost:3000/resumeDemo/` 启动

---

## 配置说明

### application.yml 关键配置

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/resumeDemo
    username: postgres
    password: your_password

langchain:
  qwen:
    api-key: ${QWEN_API_KEY:your-api-key}
    model-name: qwen-turbo
    embedding-model-name: text-embedding-v3
    base-url: https://dashscope.aliyuncs.com/compatible-mode/v1
  pgvector:
    dimension: 1024
```

---

## 项目结构

```
resumeDemo/
├── backend/
│   └── src/main/java/com/classmanagement/
│       ├── config/
│       │   ├── LangChainConfig.java        # Embedding + Chat + pgvector Bean
│       │   └── PromptTemplates.java        # Prompt 模板
│       ├── service/
│       │   ├── StudentProfileEmbeddingService.java  # 学情画像向量化
│       │   ├── StudentRagService.java               # RAG 查询核心逻辑
│       │   └── AiChatService.java                   # AI 对话服务
│       ├── controller/
│       │   ├── RagController.java          # RAG 查询接口
│       │   └── AiChatController.java       # AI 对话接口
│       └── repository/
│           └── StudentProfileEmbeddingRepository.java
├── frontend/
│   └── src/components/
│       └── AIRobot.vue                     # AI 助手（含 RAG 学情分析 Tab）
└── scripts/
    ├── seed-demo-pg.js                     # 演示数据初始化
    └── test-rag-final.js                   # RAG 链路验证脚本
```

---

## AI 接口速查（代码导航）

### 学情分析 RAG 核心链路

| 层级 | 文件路径 | 说明 |
|------|----------|------|
| 入口接口 | `backend/src/main/java/com/classmanagement/controller/RagController.java` | `POST /api/ai/rag/query` — 接收自然语言查询，触发完整 RAG 流程 |
| RAG 核心逻辑 | `backend/src/main/java/com/classmanagement/service/StudentRagService.java` | 向量化 → pgvector 检索 → 构建增强 Prompt → 调用千问生成报告 |
| 画像向量化 | `backend/src/main/java/com/classmanagement/service/StudentProfileEmbeddingService.java` | 将学生多维数据生成语义摘要文本并调用 text-embedding-v3 向量化存储 |
| Prompt 模板 | `backend/src/main/java/com/classmanagement/config/PromptTemplates.java` | 所有大模型调用的 Prompt 模板集中管理 |
| LangChain 配置 | `backend/src/main/java/com/classmanagement/config/LangChainConfig.java` | EmbeddingModel、ChatLanguageModel、pgvector Bean 注册 |
| 前端调用入口 | `frontend/src/components/AIRobot.vue` — `sendRagQuery()` 方法 | 触发 `POST /api/ai/rag/query`，分步骤展示进度（向量化→检索→生成） |

### AI 搜索导航链路

| 层级 | 文件路径 | 说明 |
|------|----------|------|
| 入口接口 | `backend/src/main/java/com/classmanagement/controller/AiStreamController.java` | `POST /api/ai/search` — 意图识别，返回路由跳转指令 |
| 流式对话接口 | `backend/src/main/java/com/classmanagement/controller/AiStreamController.java` | `GET /api/ai/chat/stream` — SSE 流式输出 AI 对话 |
| 对话服务 | `backend/src/main/java/com/classmanagement/service/AiChatService.java` | 多轮对话管理、学生学情报告生成 |
| 前端调用入口 | `frontend/src/components/AIRobot.vue` — `sendMessage()` → `streamChat()` | 先走意图识别，识别到路由则跳转；否则降级为 SSE 流式对话 |

### 重建向量索引接口

```
POST /api/ai/rag/rebuild-index       # 全量重建所有学生画像向量
POST /api/ai/rag/rebuild/{studentId} # 重建单个学生画像向量
```

### 其他 AI 接口

```
GET  /api/ai/weekly-broadcast        # 数据大屏 AI 周报播报（AIController.java）
POST /api/ai-chat/student-report     # 单个学生学情分析报告（AiChatController.java）
```

---

## 面试话术参考

### 项目亮点一句话

> 基于 PostgreSQL + pgvector + LangChain4j + 千问大模型，构建学生学情语义检索与智能分析系统。将成绩趋势、出勤行为、排名变化等多维数据生成学情画像并向量化存储，实现自然语言学情查询、风险识别与干预建议生成，形成完整 RAG 业务闭环。

### 技术细节说明

**Q: 为什么用 pgvector 而不是专门的向量数据库？**

A: 业务数据本身就在 PostgreSQL，避免引入额外的 Milvus/Pinecone 等组件，降低运维成本。pgvector 的 IVFFlat 索引在千级数据量下性能完全够用，而且可以和业务表做 JOIN 查询。

**Q: 向量化的是什么内容？**

A: 不是原始数字，而是结构化的语义摘要文本。把成绩趋势、缺勤次数、风险等级等信息转换成自然语言描述，这样语义相似度更准确，能理解"退步明显"、"高风险"等语义。

**Q: RAG 和普通 Prompt 有什么区别？**

A: 普通 Prompt 是把问题直接发给大模型，大模型不知道具体学生数据。RAG 是先从数据库检索出最相关的学生画像，再把这些真实数据注入 Prompt，大模型基于真实数据回答，更准确、更有依据。

**Q: 用了 Cursor 和 MCP，有什么体会？**

A: Cursor 的 Agent 模式可以直接读写代码、执行命令、访问数据库。MCP 让 AI 能直接连接 PostgreSQL 执行 SQL 验证数据，整个从 MySQL 迁移到 pgvector 的改造过程效率提升非常明显。

---

## 项目面试完整描述

### 一句话介绍

> 这是一个高三班级管理系统，核心亮点是用 PostgreSQL + pgvector + LangChain4j + 阿里云千问大模型，构建了一套完整的 AI RAG 学情分析功能。将学生成绩趋势、出勤行为、年级排名等多维度数据生成学情画像向量，支持自然语言查询、语义检索和 AI 分析报告生成。

### 技术栈完整描述（面试时逐层展开）

**后端：**
- Spring Boot 3.2.5 + Java 17
- PostgreSQL 15 + pgvector 扩展（向量存储与余弦距离语义检索）
- Spring Data JPA + Hibernate 6（业务数据 ORM）
- LangChain4j 0.36.2（Embedding 模型 + Chat 模型 Java 集成框架）
- 阿里云千问 text-embedding-v3（1024维向量，中文语义）
- 阿里云千问 qwen-turbo（分析报告生成）
- Spring MVC SSE（Server-Sent Events 流式输出，实现 ChatGPT 打字机效果）

**前端：**
- Vue 3 Composition API + Vite
- Element Plus 组件库
- Axios + fetch SSE 双模式请求
- Marked + DOMPurify（Markdown 安全渲染）
- Vue Router（AI 意图识别后自动跳转路由）

**工程化：**
- Cursor AI IDE + MCP（Model Context Protocol）连接 PostgreSQL 辅助开发
- Maven 构建，打包为单 jar 部署

### 核心功能描述（按亮点排序）

**1. RAG 学情分析（最核心亮点）**

用自然语言输入查询条件（如"找出近三次考试退步明显且缺勤较多的学生"），系统：
1. 调用千问 text-embedding-v3 将查询文本向量化为 1024 维浮点数组
2. 通过 pgvector `<=>` 余弦距离运算符在数据库中做语义相似度检索，召回 Top-5 最相关学生画像
3. 将召回的真实学生数据注入 Prompt（RAG 增强），调用 qwen-turbo 生成专业分析报告和干预建议
4. 前端分步骤展示进度：向量化中 → 检索中 → 生成报告中

**学情画像设计亮点**：向量化的不是原始数字，而是结构化语义文本，例如：
```
张伟，高三1班，理科。近3次考试总分：580→542→510，持续下降。
年级排名第45名，近30天缺勤6次，综合风险等级：高。
```
这使得"退步明显"、"高风险"等语义能被正确理解和匹配。

**2. AI 搜索导航 + SSE 流式对话**

悬浮 AI 机器人支持两种模式：
- **意图识别模式**：输入"帮我看高三1班的学生列表"，后端解析意图返回路由指令，前端自动跳转并填充筛选条件
- **流式对话模式**：意图未识别时降级为 SSE 流式对话，实现 ChatGPT 打字机效果

**3. 成绩管理**
- 录入周考成绩（语数英 + 文综/理综），自动计算总分、班级排名、年级排名
- 基于 2013 年广东高考分数线预测大学层次
- 统计卡片支持点击快速筛选优秀/需关注学生
- 分数范围筛选时自动补全最新考试名避免跨周排名混乱

**4. 出勤管理 / 学生画像 / 数据大屏**
- 出勤异常预警，AI 生成周报播报词
- 单学生多维度学情报告（成绩趋势图 + AI 分析）
- 可视化数据大屏，实时展示年级整体学情

### 可能被追问的技术细节

**Q: pgvector 怎么存向量？**

A: 表结构中有一列类型为 `vector(1024)`，存储 1024 维浮点向量。建了 IVFFlat 索引加速检索。查询时用 `ORDER BY embedding <=> '[...]'::vector LIMIT 5` 按余弦距离排序取前5条。因为 JPA 对 pgvector 参数绑定有兼容性问题，直接用 `JdbcTemplate` 拼接向量字符串执行 native SQL。

**Q: LangChain4j 在这里具体做了什么？**

A: 主要用它封装了阿里云千问的 Embedding 接口和 Chat 接口，通过 Spring Bean 注入 `EmbeddingModel` 和 `ChatLanguageModel`，调用时只需 `embeddingModel.embed(text)` 和 `chatLanguageModel.generate(prompt)`，屏蔽了 HTTP 调用细节。

**Q: SSE 流式输出怎么实现的？**

A: 后端用 Spring MVC 的 `SseEmitter`，调用千问 `StreamingChatLanguageModel` 逐 token 回调，每个 token 通过 `emitter.send()` 推送。前端用 `fetch` + `ReadableStream` 读取，解析 `data:` 前缀的 token 追加到消息气泡，实现打字机效果。

**Q: 为什么不用 MySQL？**

A: MySQL 不原生支持向量类型，RAG 功能需要向量数据库。pgvector 是 PostgreSQL 的扩展，业务数据和向量数据在同一个数据库，避免引入 Milvus、Pinecone 等额外组件，部署更简单，还可以跨表 JOIN 查询。

---

## 完整技术链路详解（面试讲解版）

### 一、RAG 学情分析完整链路

```
用户输入自然语言
  "找出近三次考试退步明显且缺勤较多的学生"
           │
           ▼
┌─────────────────────────────────┐
│  前端 AIRobot.vue               │
│  sendRagQuery()                 │
│  POST /api/ai/rag/query         │
│  { query: "...", topK: 5 }      │
└────────────┬────────────────────┘
             │
             ▼
┌─────────────────────────────────┐
│  RagController.java             │
│  解析 topK（默认5，限制1~20）    │
│  → studentRagService.ragQuery() │
└────────────┬────────────────────┘
             │
             ▼
┌─────────────────────────────────┐  耗时约 300ms
│  StudentRagService              │
│  Step 1: Embedding              │
│  embeddingModel.embed(query)    │
│  千问 text-embedding-v3         │
│  输出: float[1024]              │
└────────────┬────────────────────┘
             │
             ▼
┌─────────────────────────────────┐  耗时约 50ms
│  Step 2: pgvector 语义检索      │
│  JdbcTemplate 执行 native SQL   │
│                                 │
│  SELECT student_name,           │
│    1-(embedding<=>query::vector)│
│    AS similarity                │
│  FROM student_profile_embedding │
│  ORDER BY embedding <=> query   │
│  LIMIT 5                        │
│                                 │
│  返回 Top-K 学生画像 + 相似度   │
└────────────┬────────────────────┘
             │
             ▼
┌─────────────────────────────────┐  耗时约 2~4s
│  Step 3: 构建增强 Prompt        │
│  buildRagPrompt():              │
│  - 高考分数线背景知识           │
│  - 每个学生画像数据（姓名/总分  │
│    /排名/趋势/缺勤/相似度）     │
│  - 用户原始问题                 │
│  - 输出格式约束                 │
│                                 │
│  chatLanguageModel.generate()   │
│  → 千问 qwen-turbo              │
│  → 生成专业分析报告+干预建议    │
└────────────┬────────────────────┘
             │
             ▼
┌─────────────────────────────────┐
│  返回前端                       │
│  { success, retrievedProfiles,  │
│    aiAnalysis, totalTimeMs,     │
│    embedTimeMs, retrievedCount} │
└─────────────────────────────────┘
             │
             ▼
前端展示:
  - 召回学生卡片（含相似度百分比、风险等级）
  - AI 分析报告（Markdown 渲染）
  - 耗时统计（向量化Xms · 总耗时Xms）
```

---

### 二、学情画像向量化链路

画像数据在每次成绩录入/更新/出勤变更后自动触发重建：

```
触发时机
  ExamScoreService.addExamScore()   ─┐
  ExamScoreService.updateExamScore() ├→ studentProfileEmbeddingService
  ExamScoreService.deleteExamScore() │     .buildAndSaveEmbedding(studentId)
  AttendanceService.addAttendance()  ┘
           │
           ▼
┌─────────────────────────────────────────────┐
│  StudentProfileEmbeddingService             │
│                                             │
│  1. 查询学生基本信息                        │
│  2. 查询近3次考试成绩（计算趋势）           │
│  3. 查询近30天考勤异常次数                  │
│  4. 查询当前年级排名                        │
│  5. 计算风险等级（high/medium/low）         │
│                                             │
│  6. 生成语义摘要文本:                       │
│  "张伟，高三1班，理科。近3次考试总分：      │
│   580→542→510。当前年级排名第45名。         │
│   较上次考试下滑32分，退步明显。近30天       │
│   缺勤6次，出勤问题严重。风险等级：高。"    │
│                                             │
│  7. embeddingModel.embed(profileText)       │
│     千问 text-embedding-v3 → float[1024]    │
│                                             │
│  8. 存入 student_profile_embedding 表       │
│     embedding 列类型: vector(1024)          │
└─────────────────────────────────────────────┘
```

**关键设计决策**：向量化的是自然语言语义摘要，而非原始数字。
原因：`[580, 542, 510]` 这样的数字向量无法让模型理解"持续下降"的语义，
而"退步明显"、"高风险"等文本描述能被 Embedding 模型正确编码，
使得用户查询"退步的学生"时余弦相似度能正确命中。

---

### 三、AI 搜索导航 + 流式对话链路

```
用户在 AI 机器人输入框输入文字
           │
           ▼
┌─────────────────────────────────┐
│  AIRobot.vue sendMessage()      │
│                                 │
│  Step 1: 意图识别               │
│  POST /api/ai/search            │
│  { query: "帮我看高三1班学生" } │
└────────────┬────────────────────┘
             │
             ▼
┌─────────────────────────────────┐
│  AiStreamController             │
│  POST /api/ai/search            │
│                                 │
│  用 INTENT_RECOGNITION Prompt   │
│  + Few-shot 示例送给千问        │
│  → 返回 JSON:                   │
│  { action: "view_students",     │
│    className: "高三1班",        │
│    description: "高三1班学生"   │
│    route: "/students?class=..."│
│  }                              │
└────────────┬────────────────────┘
             │
      ┌──────┴──────┐
      │识别成功      │识别失败(unknown)
      ▼              ▼
┌──────────┐  ┌──────────────────────────────┐
│ 前端跳转  │  │ 降级为 SSE 流式对话           │
│ router   │  │ GET /api/ai/chat/stream       │
│ .push()  │  │   ?message=xxx&history=json  │
│ + 参数   │  │                              │
│ 回填筛选 │  │ AiChatService                │
│ 条件     │  │ isRagQuery() 判断:           │
└──────────┘  │  - 命中学情关键词 → RAG链路  │
              │  - 普通问答 → ChatLM直接回答 │
              │                              │
              │ StreamingChatLanguageModel   │
              │ 逐 token 回调                │
              │ emitter.send(token)          │
              │ 前端 fetch ReadableStream    │
              │ 追加到消息气泡（打字机效果） │
              └──────────────────────────────┘
```

---

### 四、关键踩坑点（面试时体现真实经验）

**踩坑1：JPA 无法绑定 pgvector 参数**

问题：用 Spring Data JPA `@Query` 传向量参数时，Hibernate 不识别 `vector` 类型，抛类型转换异常。

```java
// ❌ 这样写会报错
@Query("SELECT e FROM Embedding e ORDER BY e.embedding <=> :vec")
List<Embedding> search(@Param("vec") float[] vec);

// ✅ 改用 JdbcTemplate 直接拼字符串绕过类型绑定
String sql = "SELECT ... FROM student_profile_embedding " +
             "ORDER BY embedding <=> '" + vectorStr + "'::vector LIMIT " + topK;
jdbcTemplate.query(sql, ...);
```

注意：向量字符串做了安全校验，只允许 `[0-9.,E+\-]` 字符，防注入。

**踩坑2：`generateStudentReport` 构建了 Prompt 却没用**

问题：原代码花大量篇幅拼接了含成绩/出勤/排名的 `STUDENT_REPORT` Prompt，
但最后调用的是 `ragQuery(student.getStudentName())`，
完整 Prompt 被完全丢弃，用学生姓名做语义检索，结果质量极差。

修复：改为直接 `chatLanguageModel.generate(prompt)` 使用构建好的 Prompt。

**踩坑3：`chat()` 所有消息都走 RAG 导致资源浪费**

问题：原代码任何消息（包括"你好"）都调用 Embedding API 向量化，
每次消耗一次 API 调用，且返回无意义的学生画像。

修复：增加 `isRagQuery()` 关键词判断，学情相关才走 RAG，
普通问答直接用 `ChatLanguageModel`，RAG 失败自动降级。

---

### 五、数据库表结构关键字段

```sql
-- 学生画像向量表
CREATE TABLE student_profile_embedding (
    id                      BIGSERIAL PRIMARY KEY,
    student_id              BIGINT NOT NULL,
    student_name            VARCHAR(50),
    class_name              VARCHAR(50),
    class_type              VARCHAR(20),
    profile_text            TEXT,           -- 语义摘要文本
    latest_total_score      DECIMAL(6,1),
    score_trend             VARCHAR(20),    -- rising/declining/stable
    abnormal_attendance_count INT,
    grade_rank              INT,
    risk_level              VARCHAR(10),    -- high/medium/low
    embedding               vector(1024),  -- pgvector 向量列
    updated_at              TIMESTAMP
);

-- IVFFlat 索引（余弦距离）
CREATE INDEX ON student_profile_embedding
    USING ivfflat (embedding vector_cosine_ops) WITH (lists = 10);
```
的扩展，业务数据和向量数据在同一个数据库，避免引入 Milvus、Pinecone 等额外组件，部署更简单，还可以跨表 JOIN 查询。

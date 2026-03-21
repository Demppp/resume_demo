-- =============================================
-- PostgreSQL Schema for Class Management System
-- with pgvector RAG support
-- =============================================

-- 启用 pgvector 扩展
CREATE EXTENSION IF NOT EXISTS vector;

-- 学生表
CREATE TABLE IF NOT EXISTS student (
    id BIGSERIAL PRIMARY KEY,
    student_name VARCHAR(50) NOT NULL,
    gender VARCHAR(10),
    address VARCHAR(200),
    parent_phone VARCHAR(20),
    class_id BIGINT,
    class_name VARCHAR(50),
    class_type VARCHAR(20),
    student_number VARCHAR(30),
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER DEFAULT 0
);

-- 班级信息表
CREATE TABLE IF NOT EXISTS class_info (
    id BIGSERIAL PRIMARY KEY,
    class_name VARCHAR(50) NOT NULL,
    class_type VARCHAR(20),
    teacher_name VARCHAR(50),
    student_count INTEGER DEFAULT 0,
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER DEFAULT 0
);

-- 学期表
CREATE TABLE IF NOT EXISTS semester (
    id BIGSERIAL PRIMARY KEY,
    semester_name VARCHAR(50) NOT NULL,
    start_date DATE,
    end_date DATE,
    is_current INTEGER DEFAULT 0,
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER DEFAULT 0
);

-- 考勤表
CREATE TABLE IF NOT EXISTS attendance (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL,
    student_name VARCHAR(50),
    class_name VARCHAR(50),
    attendance_date DATE NOT NULL,
    attendance_status VARCHAR(20) NOT NULL,
    reason VARCHAR(200),
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER DEFAULT 0
);

-- 考试成绩表
CREATE TABLE IF NOT EXISTS exam_score (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL,
    student_name VARCHAR(50),
    class_name VARCHAR(50),
    class_type VARCHAR(20),
    exam_name VARCHAR(100) NOT NULL,
    exam_date DATE,
    chinese_score NUMERIC(5,1),
    math_score NUMERIC(5,1),
    english_score NUMERIC(5,1),
    comprehensive_score NUMERIC(5,1),
    physics_score NUMERIC(5,1),
    chemistry_score NUMERIC(5,1),
    biology_score NUMERIC(5,1),
    politics_score NUMERIC(5,1),
    history_score NUMERIC(5,1),
    geography_score NUMERIC(5,1),
    total_score NUMERIC(6,1),
    class_rank INTEGER,
    grade_rank INTEGER,
    predicted_university VARCHAR(100),
    score_change INTEGER,
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER DEFAULT 0
);

-- 班级日记表
CREATE TABLE IF NOT EXISTS class_diary (
    id BIGSERIAL PRIMARY KEY,
    class_name VARCHAR(50),
    diary_date DATE,
    content TEXT,
    author VARCHAR(50),
    weather VARCHAR(20),
    mood VARCHAR(20),
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER DEFAULT 0
);

-- 通知表
CREATE TABLE IF NOT EXISTS notification (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200),
    content TEXT,
    type VARCHAR(50),
    is_read INTEGER DEFAULT 0,
    student_id BIGINT,
    student_name VARCHAR(50),
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER DEFAULT 0
);

-- 操作日志表
CREATE TABLE IF NOT EXISTS operation_log (
    id BIGSERIAL PRIMARY KEY,
    operation_type VARCHAR(50),
    operation_desc VARCHAR(500),
    operator VARCHAR(50),
    ip_address VARCHAR(50),
    request_url VARCHAR(200),
    request_method VARCHAR(10),
    request_params TEXT,
    response_result TEXT,
    execution_time BIGINT,
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER DEFAULT 0
);

-- =============================================
-- pgvector 核心表：学生画像向量表
-- 用于 RAG 语义检索
-- =============================================
CREATE TABLE IF NOT EXISTS student_profile_embedding (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL UNIQUE,
    student_name VARCHAR(50) NOT NULL,
    class_name VARCHAR(50),
    class_type VARCHAR(20),
    -- 学情文本摘要（用于生成向量，同时供面试展示）
    profile_text TEXT NOT NULL,
    -- 千问 text-embedding-v3 输出维度 1024
    embedding vector(1024),
    -- 冗余字段，方便快速过滤
    latest_total_score NUMERIC(6,1),
    score_trend VARCHAR(20),        -- 'rising'|'declining'|'stable'
    abnormal_attendance_count INTEGER DEFAULT 0,
    grade_rank INTEGER,
    risk_level VARCHAR(10),         -- 'high'|'medium'|'low'
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- IVFFlat 索引：cosine 距离，适合语义相似度场景
-- lists 参数建议 = sqrt(行数)，学生数百人时设 10 即可
CREATE INDEX IF NOT EXISTS idx_student_profile_embedding_vec
    ON student_profile_embedding USING ivfflat (embedding vector_cosine_ops)
    WITH (lists = 10);

CREATE INDEX IF NOT EXISTS idx_student_profile_student_id
    ON student_profile_embedding (student_id);

CREATE INDEX IF NOT EXISTS idx_student_profile_risk_level
    ON student_profile_embedding (risk_level);

-- 常用业务索引
CREATE INDEX IF NOT EXISTS idx_student_deleted ON student (deleted);
CREATE INDEX IF NOT EXISTS idx_exam_score_student_id ON exam_score (student_id);
CREATE INDEX IF NOT EXISTS idx_exam_score_exam_name ON exam_score (exam_name);
CREATE INDEX IF NOT EXISTS idx_attendance_student_id ON attendance (student_id);
CREATE INDEX IF NOT EXISTS idx_attendance_date ON attendance (attendance_date);


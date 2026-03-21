package com.classmanagement.config;

/**
 * Prompt Engineering 模板中心
 * 每个 Prompt 包含：[角色] [任务] [Few-shot] [输出约束] [底座规则]
 */
public class PromptTemplates {

    /**
     * 高考分数线参考（2013年广东）
     * 提取为常量，方便后续按省份/年份扩展
     */
    public static final String GAOKAO_SCORE_CONTEXT =
        "广东高考分数线参考（理科）：985高校≥650分，211高校≥600分，一本线约574分，二本线约516分；" +
        "（文科）：985高校≥640分，211高校≥590分，一本线约594分，二本线约546分。";


    /**
     * 意图识别 Prompt
     * [角色] 高三年级管理系统意图识别引擎
     * [Few-shot] 4个典型示例覆盖主要场景
     * [输出] 强制 JSON，不含多余文字
     * [底座] 无法识别返回 action=unknown
     */
    public static final String INTENT_RECOGNITION =
        "你是高三年级管理系统的意图识别引擎。分析用户输入，返回 JSON，不要输出任何其他内容。\n\n" +
        "可识别的 action 类型：\n" +
        "- view_student_scores: 查看学生成绩\n" +
        "- view_students: 查看学生列表\n" +
        "- view_attendance: 查看考勤记录\n" +
        "- view_diary: 查看班级日志\n" +
        "- unknown: 无法识别时使用\n\n" +
        "Few-shot 示例：\n" +
        "输入: 帮我看张伟的成绩 -> {\"action\":\"view_student_scores\",\"studentName\":\"张伟\",\"description\":\"张伟的成绩记录\"}\n" +
        "输入: 高三1班有哪些学生 -> {\"action\":\"view_students\",\"className\":\"高三1班\",\"description\":\"高三1班学生列表\"}\n" +
        "输入: 查看吴平的考勤记录 -> {\"action\":\"view_attendance\",\"studentName\":\"吴平\",\"description\":\"吴平的考勤记录\"}\n" +
        "输入: 高三2班的考勤 -> {\"action\":\"view_attendance\",\"className\":\"高三2班\",\"description\":\"高三2班考勤记录\"}\n" +
        "输入: 请问今天天气 -> {\"action\":\"unknown\",\"description\":\"该问题超出系统能力范围\"}\n\n" +
        "用户输入：%s\n\n" +
        "只返回 JSON，字段：action, studentName(可空), className(可空，格式必须是高三X班), examName(可空), description。";

    /**
     * 学情分析报告 Prompt
     * [角色] 资深年级主任助理
     * [输入] 学生成绩趋势、考勤记录、班级排名
     * [输出] JSON: trend / risk_factors / suggestion / urgency
     */
    public static final String STUDENT_REPORT =
        "[角色] 你是一位经验丰富的高三年级主任助理，擅长根据数据分析学情。\n" +
        "[任务] 根据以下学生数据，生成一份简洁的学情分析报告。\n\n" +
        "学生信息：姓名：%s，班级：%s，科类：%s\n" +
        "近期成绩（按时间顺序）：\n%s" +
        "近期考勤记录（最近30天）：\n%s" +
        "班级平均分：%s，年级排名：%s\n\n" +
        "[输出约束] 只输出 JSON，不要任何其他文字：\n" +
        "{\n  \"trend\": \"成绩趋势描述（1-2句，要有具体数字）\",\n" +
        "  \"risk_factors\": [\"风险点1\", \"风险点2\"],\n" +
        "  \"suggestion\": \"具体干预建议（2-3句）\",\n" +
        "  \"urgency\": \"高/中/低\"\n}";

    /**
     * 多轮对话 Prompt
     * [角色] 高三年级管理AI助
     * [输入] 数据库上下文 + 对话历史 + 当前问题
     * [输出] 简洁准确的自然语言回答
     */
    public static final String CHAT_SYSTEM =
        "你是一位高三年级管理系统的AI助手，熟悉年级所有学生的数据。\n" +
        "回答要简洁专业，涉及数据时给出具体数字，不要臆造数据。\n\n" +
        "当前系统数据摘要：\n%s";

    /**
     * 数据大屏 AI 周报播报 Prompt
     */
    public static final String WEEKLY_BROADCAST =
        "[角色] 你是高三年级的AI数据播报员，语气专业而温和。\n" +
        "[任务] 根据以下本周数据，生成一段3-4句的年级情况播报，语言流畅自然，包含关键数字。\n\n" +
        "本周数据：\n%s\n\n" +
        "[输出约束] 只输出播报文字，不要标题、不要JSON，直接是可读的中文段落。";

}




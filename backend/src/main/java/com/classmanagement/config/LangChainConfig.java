package com.classmanagement.config;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.model.openai.OpenAiTokenizer;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LangChainConfig {

    @Value("${langchain.qwen.api-key}")
    private String apiKey;

    @Value("${langchain.qwen.model-name}")
    private String modelName;

    @Value("${langchain.qwen.embedding-model-name}")
    private String embeddingModelName;

    @Value("${langchain.qwen.base-url}")
    private String baseUrl;

    @Value("${langchain.pgvector.host}")
    private String pgHost;

    @Value("${langchain.pgvector.port}")
    private int pgPort;

    @Value("${langchain.pgvector.database}")
    private String pgDatabase;

    @Value("${langchain.pgvector.user}")
    private String pgUser;

    @Value("${langchain.pgvector.password}")
    private String pgPassword;

    @Value("${langchain.pgvector.table}")
    private String pgTable;

    @Value("${langchain.pgvector.dimension}")
    private int pgDimension;

    // ── 对话模型（千问 qwen-turbo）──────────────────────────────
    @Bean
    public ChatLanguageModel chatLanguageModel() {
        return OpenAiChatModel.builder()
                .apiKey(apiKey)
                .modelName(modelName)
                .baseUrl(baseUrl)
                .temperature(0.7)
                .maxTokens(2000)
                .build();
    }

    // ── 流式对话模型 ─────────────────────────────────────────────
    @Bean
    public StreamingChatLanguageModel streamingChatLanguageModel() {
        return OpenAiStreamingChatModel.builder()
                .apiKey(apiKey)
                .modelName(modelName)
                .baseUrl(baseUrl)
                .temperature(0.7)
                .maxTokens(2000)
                .tokenizer(new OpenAiTokenizer("gpt-3.5-turbo"))
                .build();
    }

    // ── Embedding 模型（千问 text-embedding-v3，1024 维）─────────
    @Bean
    public EmbeddingModel embeddingModel() {
        return OpenAiEmbeddingModel.builder()
                .apiKey(apiKey)
                .modelName(embeddingModelName)
                .baseUrl(baseUrl)
                .build();
    }

    // ── pgvector 向量存储（LangChain4j 内置实现）─────────────────
    // 此 Bean 供 LangChain4j 标准 RAG pipeline 使用
    // 业务层直接操作 student_profile_embedding 表则走 StudentProfileEmbeddingRepository
    @Bean
    public EmbeddingStore<TextSegment> embeddingStore() {
        return PgVectorEmbeddingStore.builder()
                .host(pgHost)
                .port(pgPort)
                .database(pgDatabase)
                .user(pgUser)
                .password(pgPassword)
                .table(pgTable)
                .dimension(pgDimension)
                .createTable(true)
                .build();
    }
}

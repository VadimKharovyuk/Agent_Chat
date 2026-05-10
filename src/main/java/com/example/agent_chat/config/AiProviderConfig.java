package com.example.agent_chat.config;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Configuration
public class AiProviderConfig {

    // ── LOCAL: Ollama ─────────────────────────────────────────────
    @Configuration
    @Profile("local")
    static class OllamaConfig {

        @Bean
        @Primary
        public ChatModel primaryChatModel(OllamaChatModel ollamaChatModel) {
            return ollamaChatModel;
        }

        @Bean("agentChatModel")
        public ChatModel agentChatModel(OllamaChatModel ollamaChatModel) {
            return ollamaChatModel;
        }
    }

    // ── PROD: OpenRouter ──────────────────────────────────────────
    @Configuration
    @Profile("openai")
    static class OpenAiConfig {

        @Bean
        @Primary
        public ChatModel primaryChatModel(OpenAiChatModel openAiChatModel) {
            return openAiChatModel;
        }

        @Bean("agentChatModel")
        public ChatModel agentChatModel(OpenAiChatModel openAiChatModel) {
            return openAiChatModel;
        }
    }
}
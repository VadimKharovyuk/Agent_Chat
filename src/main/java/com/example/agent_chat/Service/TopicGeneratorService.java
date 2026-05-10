package com.example.agent_chat.Service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@ConditionalOnProperty(
        name = "app.agent.experiment.enabled",
        havingValue = "true",
        matchIfMissing = false
)
public class TopicGeneratorService {

    private static final List<String> NEWS_QUERIES = List.of(
            "technology AI society",
            "economy inflation future",
            "climate environment crisis",
            "politics democracy freedom",
            "science space exploration",
            "healthcare medicine future",
            "education technology students",
            "cryptocurrency bitcoin finance"
    );

    private final ChatModel primaryChatModel;
    private final NewsApiSearchTool newsApiSearchTool;

    public TopicGeneratorService(ChatModel primaryChatModel,
                                 NewsApiSearchTool newsApiSearchTool) {
        this.primaryChatModel = primaryChatModel;
        this.newsApiSearchTool = newsApiSearchTool;
    }

    // ── Генерація теми ────────────────────────────────────────────────────

    public String generateTopic() {
        log.info("Generating topic from news...");
        try {
            String randomQuery = NEWS_QUERIES.get(
                    (int) (Math.random() * NEWS_QUERIES.size())
            );
            log.info("Using news category: '{}'", randomQuery);

            String news = newsApiSearchTool.searchNews(randomQuery);

            String prompt = """
                    На основі цих новин придумай одну провокаційну тему для філософської дискусії.
                    Тема має бути спірною — щоб два агенти з протилежними поглядами могли сперечатись.
                    Відповідай ТІЛЬКИ темою — одне речення, без пояснень, без лапок.
                    Новини: %s
                    """.formatted(news);

            String topic = primaryChatModel.call(prompt);
            log.info("Generated topic: '{}'", topic);
            return topic.trim();

        } catch (Exception e) {
            log.warn("Topic generation failed: {}", e.getMessage());
            return "Чи змінить штучний інтелект майбутнє людства?";
        }
    }

    // ── Генерація промптів ────────────────────────────────────────────────

    public GeneratedPrompts generatePrompts(String topic) {
        log.info("Generating prompts for topic: '{}'", topic);
        try {
            String prompt = """
        Тема дискусії: %s
        
        Придумай двох агентів з протилежними поглядами.
        
        Відповідай СТРОГО у форматі:
        AGENT_A: [промпт]
        AGENT_B: [промпт]
        
        Кожен промпт має містити:
        - Роль і характер
        - Чітку позицію
        - НІКОЛИ не погоджуйся з опонентом
        - Відповідай ТІЛЬКИ 2 речення — не більше!
        - БЕЗ списків, БЕЗ заголовків, БЕЗ висновків
        - Закінчуй гострим питанням
        - Відповідай ТІЛЬКИ українською мовою
        """.formatted(topic);

            String response = primaryChatModel.call(prompt);
            log.info("Generated prompts response: '{}'", response);

            String promptA = extractPrompt(response, "AGENT_A:");
            String promptB = extractPrompt(response, "AGENT_B:");

            return new GeneratedPrompts(promptA, promptB);

        } catch (Exception e) {
            log.warn("Prompt generation failed: {}", e.getMessage());
            return new GeneratedPrompts("", "");
        }
    }

    // ── Парсинг ───────────────────────────────────────────────────────────

    private String extractPrompt(String response, String marker) {
        int start = response.indexOf(marker);
        if (start == -1) return "";
        start += marker.length();

        int endA = response.indexOf("AGENT_A:", start);
        int endB = response.indexOf("AGENT_B:", start);
        int end = -1;

        if (endA > start) end = endA;
        if (endB > start && (end == -1 || endB < end)) end = endB;

        if (end == -1) return response.substring(start).trim();
        return response.substring(start, end).trim();
    }

    // ── DTO ───────────────────────────────────────────────────────────────

    public record GeneratedPrompts(String promptA, String promptB) {}
}
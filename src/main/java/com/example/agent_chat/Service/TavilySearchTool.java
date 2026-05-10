package com.example.agent_chat.Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Slf4j
@Component
public class TavilySearchTool {

    private final RestClient restClient;
    private final String apiKey;

    public TavilySearchTool(@Value("${app.tavily.api-key:}") String apiKey) {
        this.apiKey = apiKey;
        this.restClient = RestClient.builder()
                .baseUrl("https://api.tavily.com")
                .build();
    }

    @Tool(description = """
            Ищет актуальную информацию в интернете.
            Используй для поиска свежих фактов, новостей, статистики.
            Используй короткие запросы — 2-4 слова.
            Предпочитай этот инструмент для поиска актуальных данных.
            """)
    public String searchWeb(String query) {
        log.info("Tavily search: '{}'", query);

        if (apiKey == null || apiKey.isBlank()) {
            log.warn("Tavily API key not set");
            return "Поиск недоступен";
        }

        try {
            TavilyRequest request = new TavilyRequest(
                    apiKey, query, 3, false
            );
            TavilyResponse response = restClient.post()
                    .uri("/search")
                    .header("Content-Type", "application/json")
                    .body(request)
                    .retrieve()
                    .body(TavilyResponse.class);

            if (response == null || response.results().isEmpty()) {
                return "Результаты не найдены";
            }

            StringBuilder sb = new StringBuilder();
            response.results().forEach(r -> {
                sb.append("• ").append(r.title()).append("\n");
                sb.append("  ").append(r.content()).append("\n\n");
            });

            log.info("Tavily found {} results for '{}'",
                    response.results().size(), query);

            return sb.toString().trim();

        } catch (Exception e) {
            log.warn("Tavily search failed for '{}': {}", query, e.getMessage());
            return "Поиск не дал результатов";
        }
    }

    record TavilyRequest(
            String api_key,
            String query,
            int max_results,
            boolean include_answer
    ) {}

    record TavilyResponse(List<SearchResult> results) {}

    record SearchResult(String title, String content, String url) {}
}
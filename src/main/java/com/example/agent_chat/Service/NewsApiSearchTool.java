package com.example.agent_chat.Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Slf4j
@Component
public class NewsApiSearchTool {

    private final RestClient restClient;
    private final String apiKey;

    public NewsApiSearchTool(@Value("${app.newsapi.api-key:}") String apiKey) {
        this.apiKey = apiKey;
        this.restClient = RestClient.builder()
                .baseUrl("https://newsapi.org/v2")
                .build();
    }

    @Tool(description = """
            Шукає свіжі новини по темі.
            Використовуй коли потрібні актуальні події, останні новини як аргумент.
            Запит — 1-3 слова.
            """)
    public String searchNews(String query) {
        log.info("NewsAPI search: '{}'", query);

        if (apiKey == null || apiKey.isBlank()) {
            return "NewsAPI ключ не налаштований";
        }

        try {
            NewsResponse response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/everything")
                            .queryParam("q", query)
                            .queryParam("pageSize", 3)
                            .queryParam("sortBy", "publishedAt")
                            .queryParam("language", "ru")
                            .queryParam("apiKey", apiKey)
                            .build())
                    .retrieve()
                    .body(NewsResponse.class);

            if (response == null || response.articles().isEmpty()) {
                return "Новини не знайдені";
            }

            StringBuilder sb = new StringBuilder();
            response.articles().forEach(a -> {
                sb.append("• ").append(a.title()).append("\n");
                if (a.description() != null) {
                    sb.append("  ").append(a.description()).append("\n");
                }
                sb.append("\n");
            });

            log.info("NewsAPI found {} articles for '{}'",
                    response.articles().size(), query);

            return sb.toString().trim();

        } catch (Exception e) {
            log.warn("NewsAPI search failed for '{}': {}", query, e.getMessage());
            return "Пошук не дав результатів";
        }
    }

    record NewsResponse(List<Article> articles) {}
    record Article(String title, String description, String url) {}
}

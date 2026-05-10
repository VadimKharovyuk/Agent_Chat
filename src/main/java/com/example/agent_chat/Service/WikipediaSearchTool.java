package com.example.agent_chat.Service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
public class WikipediaSearchTool {

    private final RestClient restClient;

    public WikipediaSearchTool() {
        this.restClient = RestClient.builder().build();
    }

    @Tool(description = """
            Ищет информацию в Wikipedia на русском языке.
            Используй ТОЛЬКО одно-два слова для поиска.
            Например: 'капитализм', 'ВВП', 'Маркс', 'СССР', 'неравенство'.
            НИКОГДА не используй длинные фразы.
            """)
    public String searchWikipedia(String query) {
        String shortQuery = query.trim().split("\\s+")[0];
        log.info("Wikipedia search: '{}' → '{}'", query, shortQuery);

        try {
            WikiSearchResponse response = restClient.get()
                    .uri("https://ru.wikipedia.org/w/api.php", uriBuilder -> uriBuilder
                            .queryParam("action", "query")
                            .queryParam("list", "search")
                            .queryParam("srsearch", shortQuery)
                            .queryParam("utf8", "1")
                            .queryParam("format", "json")
                            .queryParam("srlimit", "1")
                            .build())
                    .retrieve()
                    .body(WikiSearchResponse.class);

            if (response == null
                    || response.query() == null
                    || response.query().search().isEmpty()) {
                return "Информация не найдена";
            }

            String title = response.query().search().get(0).title();
            String snippet = response.query().search().get(0).snippet();
            snippet = snippet.replaceAll("<[^>]+>", "").trim();

            log.info("Wikipedia found: '{}'", title);
            return "Статья: " + title + "\n" + snippet;

        } catch (Exception e) {
            log.warn("Wikipedia search failed for '{}': {}", shortQuery, e.getMessage());
            return "Поиск не дал результатов";
        }
    }

    record WikiSearchResponse(Query query) {
        record Query(java.util.List<SearchResult> search) {}
        record SearchResult(String title, String snippet) {}
    }
}
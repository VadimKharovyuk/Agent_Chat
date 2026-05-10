package com.example.agent_chat.Service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
///ArXiv — для наукових суперечок:
@Slf4j
@Component
public class ArxivSearchTool {

    private final RestClient restClient;

    public ArxivSearchTool() {
        this.restClient = RestClient.builder()
                .baseUrl("http://export.arxiv.org")
                .build();
    }

    @Tool(description = """
            Шукає наукові статті та дослідження на ArXiv.
            Використовуй для наукових суперечок — знаходить реальні дослідження.
            Запит — англійською, 2-4 слова.
            """)
    public String searchPapers(String query) {
        log.info("ArXiv search: '{}'", query);

        try {
            String response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/query")
                            .queryParam("search_query", "all:" + query)
                            .queryParam("start", 0)
                            .queryParam("max_results", 3)
                            .queryParam("sortBy", "relevance")
                            .build())
                    .retrieve()
                    .body(String.class);

            if (response == null || response.isBlank()) {
                return "Статті не знайдені";
            }

            // парсимо XML вручну — просто витягуємо title і summary
            StringBuilder sb = new StringBuilder();
            String[] entries = response.split("<entry>");

            int count = 0;
            for (int i = 1; i < entries.length && count < 3; i++) {
                String entry = entries[i];

                String title = extractTag(entry, "title");
                String summary = extractTag(entry, "summary");

                if (title != null) {
                    sb.append("• ").append(title.trim()).append("\n");
                    if (summary != null) {
                        String shortSummary = summary.trim();
                        if (shortSummary.length() > 200) {
                            shortSummary = shortSummary.substring(0, 200) + "...";
                        }
                        sb.append("  ").append(shortSummary).append("\n\n");
                    }
                    count++;
                }
            }

            if (sb.isEmpty()) {
                return "Статті не знайдені";
            }

            log.info("ArXiv found {} papers for '{}'", count, query);
            return sb.toString().trim();

        } catch (Exception e) {
            log.warn("ArXiv search failed for '{}': {}", query, e.getMessage());
            return "Пошук не дав результатів";
        }
    }

    private String extractTag(String xml, String tag) {
        String open = "<" + tag + ">";
        String close = "</" + tag + ">";
        int start = xml.indexOf(open);
        int end = xml.indexOf(close);
        if (start == -1 || end == -1) return null;
        return xml.substring(start + open.length(), end);
    }
}
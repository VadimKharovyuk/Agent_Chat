package com.example.agent_chat.Service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;
///Alpha Vantage — для економічних дискусій:
@Slf4j
@Component
public class AlphaVantageTool {

    private final RestClient restClient;
    private final String apiKey;

    public AlphaVantageTool(@Value("${app.alphavantage.api-key:}") String apiKey) {
        this.apiKey = apiKey;
        this.restClient = RestClient.builder()
                .baseUrl("https://www.alphavantage.co")
                .build();
    }

    @Tool(description = """
            Отримує поточну ціну акцій або фінансові дані компанії.
            Використовуй для економічних дискусій — ціни акцій, капіталізація.
            Запит — тікер акції: AAPL, GOOGL, TSLA, AMZN.
            """)
    public String getStockPrice(String symbol) {
        log.info("AlphaVantage stock: '{}'", symbol);

        if (apiKey == null || apiKey.isBlank()) {
            return "AlphaVantage ключ не налаштований";
        }

        try {
            Map response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/query")
                            .queryParam("function", "GLOBAL_QUOTE")
                            .queryParam("symbol", symbol.toUpperCase())
                            .queryParam("apikey", apiKey)
                            .build())
                    .retrieve()
                    .body(Map.class);

            if (response == null || !response.containsKey("Global Quote")) {
                return "Дані не знайдені для " + symbol;
            }

            Map<String, String> quote = (Map<String, String>) response.get("Global Quote");
            String price = quote.getOrDefault("05. price", "N/A");
            String change = quote.getOrDefault("10. change percent", "N/A");
            String high = quote.getOrDefault("03. high", "N/A");
            String low = quote.getOrDefault("04. low", "N/A");

            log.info("AlphaVantage: {} = ${}", symbol, price);

            return String.format("Акція %s: $%s | Зміна: %s | Макс: $%s | Мін: $%s",
                    symbol.toUpperCase(), price, change, high, low);

        } catch (Exception e) {
            log.warn("AlphaVantage failed for '{}': {}", symbol, e.getMessage());
            return "Не вдалося отримати дані";
        }
    }
}
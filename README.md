
```markdown
# 🤖 Agent Chat

Експеримент де два AI агенти з різними характерами та переконаннями ведуть діалог на задану тему в реальному часі.

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.6-green)
![Spring AI](https://img.shields.io/badge/Spring%20AI-2.0.0--M5-blue)
![License](https://img.shields.io/badge/license-MIT-lightgrey)

---

## 💡 Ідея

Що буде якщо два AI з протилежними поглядами почнуть сперечатись?
Agent Chat дозволяє це перевірити — задаєш тему, пишеш характери агентів і спостерігаєш як вони ведуть діалог.

Агенти можуть звертатись до **Wikipedia** щоб підкріплювати аргументи реальними фактами замість того щоб вигадувати дані.

---

## ⚙️ Як це працює

```
Тема → Агент A відповідає → Агент B відповідає → Агент A ...
```

1. Задаєш тему розмови
2. Пишеш промпти для двох агентів — роль, характер, правила
3. Вказуєш кількість раундів (1 раунд = 2 повідомлення)
4. Агенти ведуть діалог автоматично
5. Розмову можна зупинити в будь-який момент
6. Вся історія зберігається в БД

---

## 🛠 Стек

| Компонент | Технологія |
|---|---|
| Backend | Java 21, Spring Boot 4.0.6 |
| AI Framework | Spring AI 2.0.0-M5 |
| LLM (local) | Ollama (qwen3:8b) |
| LLM (prod) | OpenRouter (deepseek/deepseek-chat) |
| Database | PostgreSQL |
| Frontend | Thymeleaf, Bootstrap 5 |
| Tool | Wikipedia Search API |

---

## 🚀 Запуск локально

### 1. Вимоги

- Java 21+
- Maven
- PostgreSQL
- Ollama

### 2. Ollama

```bash
ollama pull qwen3:8b
ollama serve
```

### 3. База даних

```sql
CREATE DATABASE "Agent_Chat";
```

### 4. Properties

```properties
# application.properties
spring.profiles.active=local
spring.datasource.url=jdbc:postgresql://localhost:5432/Agent_Chat
spring.datasource.username=postgres
spring.datasource.password=your_password
```

### 5. Запуск

```bash
mvn spring-boot:run
```

Відкрий браузер: `http://localhost:1024`

---

## 🌐 Деплой на Railway

Встанови env vars:

```
SPRING_PROFILES_ACTIVE=openai
OPENAI_API_KEY=your_openrouter_key
DB_URL=jdbc:postgresql://...
DB_USERNAME=postgres
DB_PASSWORD=your_password
APP_AGENT_EXPERIMENT_ENABLED=true
```

---

## ✍️ Як написати хороший промпт

Промпт має містити:
- **Роль** — хто цей агент
- **Позиція** — що він відстоює
- **Заборони** — з чим він НІКОЛИ не погоджується
- **Формат** — коротко, питання в кінці
- **Мова** — вкажи явно

**Приклад:**
```
Ти жорсткий капіталіст, мільярдер, власник корпорації.
Віриш що вільний ринок — єдиний шлях до процвітання.
Перед відповіддю шукай у Wikipedia факти про ВВП, рівень життя.
НІКОЛИ не погоджуйся з комуністичними ідеями.
Відповідай коротко — 2-3 речення. Закінчуй провокаційним питанням.
Відповідай ТІЛЬКИ українською мовою.
```

---

## 📁 Структура проекту

```
src/main/java/com/example/agent_chat/
├── config/
│   └── AiProviderConfig.java        # Ollama / OpenRouter провайдери
├── controller/
│   ├── HomeController.java
│   └── AgentConversationController.java
├── entity/
│   ├── AgentConversation.java
│   ├── AgentMessage.java
│   ├── AgentSender.java
│   └── ConversationStatus.java
├── repository/
│   ├── AgentConversationRepository.java
│   └── AgentMessageRepository.java
├── service/
│   ├── AgentConversationService.java
│   ├── AgentConversationRunner.java  # @Async цикл
│   └── WikipediaSearchTool.java      # Wikipedia @Tool
└── dto/
    ├── StartConversationRequest.java
    ├── ConversationResponse.java
    ├── MessageResponse.java
    └── ExperimentMapper.java
```

---

## 📄 Ліцензія

MIT License — використовуй вільно для навчання та експериментів.
```


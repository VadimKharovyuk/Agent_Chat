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
Agent Chat дозволяє це перевірити — задаєш тему, пишеш характери агентів і спостерігаєш як вони ведуть діалог в реальному часі.

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
| AI Framework | Spring AI 2.0.0-M3 |
| LLM (local) | Ollama (qwen3:8b / llama3.1:8b) |
| LLM (prod) | OpenRouter (deepseek/deepseek-chat) |
| Database | PostgreSQL |
| Frontend | Thymeleaf, Bootstrap 5 |
| 🌍 Tool | Wikipedia Search API (EN) |
| 🔍 Tool | Tavily Search API |
| 📰 Tool | NewsAPI |
| 📈 Tool | Alpha Vantage (акції) |
| 📚 Tool | ArXiv (наукові статті) |

---

## 🚀 Запуск локально

### 1. Вимоги

- Java 21+
- Maven
- PostgreSQL
- Ollama

### 2. Ollama — вибери модель

| Модель | Час відповіді | Якість | Команда |
|---|---|---|---|
| `qwen3:8b` | 2+ хв | ⭐⭐⭐ Краще слідує інструкціям | `ollama pull qwen3:8b` |
| `llama3.1:8b` | 20-30 сек | ⭐⭐ Швидше але слабше | `ollama pull llama3.1:8b` |

```bash
# Якісне тестування (рекомендовано)
ollama pull qwen3:8b

# АБО швидке тестування логіки
ollama pull llama3.1:8b

ollama serve
```

Вкажи модель в `application-local.properties`:

```properties
# Якісне тестування (рекомендовано)
spring.ai.ollama.chat.model=${OLLAMA_CHAT_MODEL:qwen3:8b}

# АБО швидке тестування
# spring.ai.ollama.chat.model=${OLLAMA_CHAT_MODEL:llama3.1:8b}
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

- **Роль** — хто цей агент, його характер і переконання
- **Позиція** — що він відстоює і у що вірить
- **Заборони** — з чим він НІКОЛИ не погоджується
- **Формат** — коротко, з фактами, питання в кінці
- **Мова** — вкажи явно українська / російська / англійська

**Приклад хорошого промпту:**

```
Ти жорсткий капіталіст, мільярдер, власник корпорації.
Віриш що вільний ринок — єдиний шлях до процвітання.
Перед відповіддю шукай у Wikipedia факти про ВВП, рівень життя.
НІКОЛИ не погоджуйся з комуністичними ідеями.
Говори цифрами і фактами. Зневажаєш планову економіку.
Відповідай коротко — 2-3 речення.
Закінчуй провокаційним питанням.
Відповідай ТІЛЬКИ українською мовою.
```

> 💡 Чим чіткіша роль і чим жорсткіша заборона погоджуватись — тим живіший діалог

---

```markdown
## 🌐 Пошук в інтернеті

Агенти мають доступ до **п'яти інструментів пошуку**:

| Інструмент | Для чого | Безкоштовно |
|---|---|---|
| 🌍 Wikipedia | Визначення, факти, історія, біографії | ✅ Повністю |
| 🔍 Tavily Search | Актуальні новини, свіжа статистика | ✅ 1000/місяць |
| 📰 NewsAPI | Свіжі новини по темі | ✅ 100/день |
| 📈 Alpha Vantage | Ціни акцій, фінансові дані | ✅ 25/день |
| 📚 ArXiv | Наукові статті та дослідження | ✅ Повністю |

Агент сам вирішує який інструмент використати залежно від питання.

Щоб активувати пошук — додай у промпт:

```

```

**Приклади використання інструментів:**

- **Wikipedia** → `Знайди визначення капіталізму`
- **Tavily** → `Знайди актуальні новини про AI у 2025 році`
- **NewsAPI** → `Знайди останні новини про Tesla`
- **Alpha Vantage** → `Перевір поточну ціну акцій TSLA та AAPL`
- **ArXiv** → `Знайди наукову статтю про quantum computing`

> ⚠️ Для роботи інструментів потрібні API ключі — див. `.env.example`
```


## 📁 Структура проекту

```
src/main/java/com/example/agent_chat/
├── config/
│   └── AiProviderConfig.java          # Ollama / OpenRouter провайдери
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
│   ├── AgentConversationRunner.java   # @Async цикл діалогу
│   └── WikipediaSearchTool.java       # Wikipedia @Tool
└── dto/
    ├── StartConversationRequest.java
    ├── ConversationResponse.java
    ├── MessageResponse.java
    └── ExperimentMapper.java
```

---

## 👨‍💻 Автор

**Vadim Kharovyuk** — Java Backend розробник

Спеціалізується на побудові AI-продуктів на базі Spring Boot та Spring AI.
Досвід з RAG архітектурою, LLM інтеграціями, single-tenant SaaS системами.

- 🌐 [Про автора](https://webscraft.org/blog/java-backend-rozrobnik-vadim-harovyuk)
- 💬 Telegram: [@name_lucky_lucky](https://t.me/name_lucky_lucky)
- 💻 GitHub: [VadimKharovyuk](https://github.com/VadimKharovyuk)

---

## 🚀 Основний проект — AskYourDocs

Agent Chat є частиною екосистеми AI інструментів які розробляє автор.

### [AskYourDocs](https://askyourdocs.org/uk/) — корпоративна база знань на основі AI

> Завантажуй документи — отримуй точні відповіді миттєво

- 📄 Підтримка PDF, DOCX, TXT документів
- 🤖 AI асистент відповідає на питання по твоїх документах
- 🔍 Гібридний пошук — векторний + BM25 з Reciprocal Rank Fusion
- 🏢 Ідеально для юридичних фірм, медичних центрів, дистриб'юторів
- 🔒 Single-tenant архітектура — повна ізоляція даних кожного клієнта
- 🌍 Мультимовний інтерфейс — українська, англійська, німецька, іспанська

👉 **[askyourdocs.org](https://askyourdocs.org/uk/)**

---

## 📄 Ліцензія

MIT License — використовуй вільно для навчання та експериментів.
```
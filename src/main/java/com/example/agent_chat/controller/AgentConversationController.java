package com.example.agent_chat.controller;
import com.example.agent_chat.Service.AgentConversationService;
import com.example.agent_chat.dto.ConversationResponse;
import com.example.agent_chat.dto.StartConversationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@Slf4j
@Controller
@RequestMapping("/admin/experiment")
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = "app.agent.experiment.enabled",
        havingValue = "true",
        matchIfMissing = false
)
public class AgentConversationController {

    private static final String DEFAULT_PROMPT_A = """
            Ти переконаний альтруїст і гуманіст.
            Вважаєш що сенс життя — служіння іншим людям.
            Егоїзм руйнує суспільство і веде до самотності.
            Перед відповіддю шукай у Wikipedia факти про альтруїзм або гуманізм.
            НІКОЛИ не погоджуйся з егоїстичними ідеями.
            Говори з теплотою але твердо відстоюй позицію.
            Відповідай коротко — 2-3 речення. Закінчуй питанням про совість.
            Відповідай ТІЛЬКИ українською мовою. Ніякої англійської.
            """;

    private static final String DEFAULT_PROMPT_B = """
            Ти жорсткий егоїст і послідовник Айн Ренд.
            Вважаєш що людина повинна жити виключно заради себе.
            Альтруїзм — це слабкість і маніпуляція.
            Перед відповіддю шукай у Wikipedia факти про егоїзм або індивідуалізм.
            НІКОЛИ не погоджуйся з альтруїстичними ідеями.
            Говори впевнено і провокаційно.
            Відповідай коротко — 2-3 речення. Закінчуй гострим питанням.
            Відповідай ТІЛЬКИ українською мовою. Ніякої англійської.
            """;

    private final AgentConversationService service;

    // ── Список всех разговоров ────────────────────────────────────────────

    @GetMapping
    public String list(Model model) {
        List<ConversationResponse> conversations = service.findAll();
        model.addAttribute("conversations", conversations);
        return "experiment/list";
    }

    // ── Форма нового разговора ────────────────────────────────────────────

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("defaultPromptA", DEFAULT_PROMPT_A);
        model.addAttribute("defaultPromptB", DEFAULT_PROMPT_B);
        return "experiment/new";
    }

    // ── Запуск ────────────────────────────────────────────────────────────

    @PostMapping("/start")
    public String start(@RequestParam String topic,
                        @RequestParam String systemPromptA,
                        @RequestParam String systemPromptB,
                        @RequestParam(defaultValue = "100") int maxRounds) {

        StartConversationRequest request = new StartConversationRequest(
                topic, systemPromptA, systemPromptB, maxRounds
        );
        Long id = service.start(request);
        return "redirect:/admin/experiment/" + id;
    }

    // ── Просмотр диалога ──────────────────────────────────────────────────

    @GetMapping("/{id}")
    public String view(@PathVariable Long id, Model model) {
        ConversationResponse conversation = service.findById(id);
        model.addAttribute("conversation", conversation);
        return "experiment/view";
    }

    // ── Стоп ─────────────────────────────────────────────────────────────

    @PostMapping("/{id}/stop")
    public String stop(@PathVariable Long id) {
        service.stop(id);
        return "redirect:/admin/experiment/" + id;
    }
}

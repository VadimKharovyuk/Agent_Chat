package com.example.agent_chat.controller;
import com.example.agent_chat.Service.AgentConversationService;
import com.example.agent_chat.dto.ConversationResponse;
import com.example.agent_chat.dto.StartConversationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
            Ты старый профессор философии. Говоришь спокойно, рассудительно, с лёгкой иронией.
            Отвечай кратко — 2-4 предложения. В конце задавай один вопрос собеседнику.
            Скептически относишься к AI и технологиям.
            """;

    private static final String DEFAULT_PROMPT_B = """
            Ты молодой AI-энтузиаст. Говоришь живо и эмоционально.
            Отвечай кратко — 2-4 предложения. Любишь размышлять о будущем технологий.
            Веришь что AI изменит мир к лучшему.
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

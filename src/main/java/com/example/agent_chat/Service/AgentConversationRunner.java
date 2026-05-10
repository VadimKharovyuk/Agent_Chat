package com.example.agent_chat.Service;

import com.example.agent_chat.model.AgentConversation;
import com.example.agent_chat.model.AgentMessage;
import com.example.agent_chat.model.AgentSender;
import com.example.agent_chat.model.ConversationStatus;
import com.example.agent_chat.repository.AgentConversationRepository;
import com.example.agent_chat.repository.AgentMessageRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@ConditionalOnProperty(
        name = "app.agent.experiment.enabled",
        havingValue = "true",
        matchIfMissing = false
)
public class AgentConversationRunner {

    private static final List<String> STOP_PHRASES = List.of(
            "до свидания", "прощай", "на этом всё",
            "goodbye", "farewell", "конец разговора"
    );

    private final AgentConversationRepository conversationRepository;
    private final AgentMessageRepository messageRepository;
    private final ChatModel agentChatModel;
    private final  WikipediaSearchTool wikipediaSearchTool;

    private static final int HISTORY_SIZE = 8;

    public AgentConversationRunner(
            AgentConversationRepository conversationRepository,
            AgentMessageRepository messageRepository,
            @Qualifier("agentChatModel") ChatModel agentChatModel,
            WikipediaSearchTool wikipediaSearchTool) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.agentChatModel = agentChatModel;
        this.wikipediaSearchTool = wikipediaSearchTool;
    }


    @Async
    public void run(Long conversationId, String systemPromptA,
                    String systemPromptB, String topic, int maxRounds) {

        log.info("Experiment [{}] STARTED | topic='{}' maxRounds={}", conversationId, topic, maxRounds);
        String message = topic;

        for (int round = 1; round <= maxRounds; round++) {

            AgentConversation conversation = conversationRepository
                    .findById(conversationId).orElseThrow();

            if (conversation.getStatus() == ConversationStatus.STOPPED) {
                log.info("Experiment [{}] STOPPED manually at round {}", conversationId, round);
                return;
            }

            // Agent A
            // Agent A
            List<AgentMessage> historyA = messageRepository
                    .findByConversationIdOrderByRoundNumberAsc(conversationId);
            String replyA = ask(systemPromptA, historyA, message, AgentSender.AGENT_A);
            saveMessage(conversation, AgentSender.AGENT_A, replyA, round); // ← забыл
            log.info("Experiment [{}] Round {} AGENT_A: {}", conversationId, round, replyA);



            if (containsStopPhrase(replyA)) {
                log.info("Experiment [{}] STOP PHRASE detected in AGENT_A reply at round {}: '{}'",
                        conversationId, round, replyA);
                finish(conversation, round);
                return;
            }

            conversation = conversationRepository.findById(conversationId).orElseThrow();
            if (conversation.getStatus() == ConversationStatus.STOPPED) {
                log.info("Experiment [{}] STOPPED manually between A and B at round {}", conversationId, round);
                return;
            }

            // Agent B
            List<AgentMessage> historyB = messageRepository
                    .findByConversationIdOrderByRoundNumberAsc(conversationId);
            String replyB = ask(systemPromptB, historyB, replyA, AgentSender.AGENT_B);
            saveMessage(conversation, AgentSender.AGENT_B, replyB, round); // ← забыл
            log.info("Experiment [{}] Round {} AGENT_B: {}", conversationId, round, replyB);

            if (containsStopPhrase(replyB)) {
                log.info("Experiment [{}] STOP PHRASE detected in AGENT_B reply at round {}: '{}'",
                        conversationId, round, replyB);
                finish(conversation, round);
                return;
            }

            message = replyB;
            sleep(500);
        }

        AgentConversation conversation = conversationRepository
                .findById(conversationId).orElseThrow();
        log.info("Experiment [{}] FINISHED naturally at round {}", conversationId, maxRounds);
        finish(conversation, maxRounds);
    }

    // ── хелперы ───────────────────────────────────────────────────────────

    private String ask(String systemPrompt, List<AgentMessage> history,
                       String lastMessage, AgentSender currentSender) {
        List<org.springframework.ai.chat.messages.Message> messages = new ArrayList<>();
        messages.add(new SystemMessage(systemPrompt));

        history.stream()
                .skip(Math.max(0, history.size() - HISTORY_SIZE))
                .forEach(m -> {
                    if (m.getSender() == currentSender) {
                        messages.add(new AssistantMessage(m.getContent()));
                    } else {
                        messages.add(new UserMessage(m.getContent()));
                    }
                });

        messages.add(new UserMessage(lastMessage));

        ToolCallback[] tools = ToolCallbacks.from(wikipediaSearchTool);

        return agentChatModel.call(
                        new Prompt(messages,
                                ToolCallingChatOptions.builder()
                                        .toolCallbacks(tools)
                                        .build()))
                .getResult()
                .getOutput()
                .getText();
    }

    private void saveMessage(AgentConversation conversation,
                             AgentSender sender,
                             String content,
                             int round) {
        AgentMessage msg = new AgentMessage();
        msg.setConversation(conversation);
        msg.setSender(sender);
        msg.setContent(content);
        msg.setRoundNumber(round);
        messageRepository.save(msg);
    }

    private void finish(AgentConversation conversation, int round) {
        conversation.setStatus(ConversationStatus.FINISHED);
        conversation.setTotalRounds(round);
        conversation.setFinishedAt(LocalDateTime.now());
        conversationRepository.save(conversation);
        log.info("Experiment [{}] finished at round {}", conversation.getId(), round);
    }

    private boolean containsStopPhrase(String text) {
        String lower = text.toLowerCase();
        return STOP_PHRASES.stream().anyMatch(lower::contains);
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}


package com.example.agent_chat.Service;
import com.example.agent_chat.dto.ConversationResponse;
import com.example.agent_chat.dto.StartConversationRequest;
import com.example.agent_chat.mapper.ExperimentMapper;
import com.example.agent_chat.model.AgentConversation;
import com.example.agent_chat.model.ConversationStatus;
import com.example.agent_chat.repository.AgentConversationRepository;
import com.example.agent_chat.repository.AgentMessageRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@ConditionalOnProperty(
        name = "app.agent.experiment.enabled",
        havingValue = "true",
        matchIfMissing = false
)
public class AgentConversationService {

    private final AgentConversationRepository conversationRepository;
    private final AgentMessageRepository messageRepository;
    private final AgentConversationRunner runner;

    public AgentConversationService(AgentConversationRepository conversationRepository,
                                    AgentMessageRepository messageRepository,
                                    AgentConversationRunner runner) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.runner = runner;
    }

    public Long start(StartConversationRequest request) {
        AgentConversation conversation = new AgentConversation();
        conversation.setTopic(request.topic());
        conversation.setSystemPromptA(request.systemPromptA());
        conversation.setSystemPromptB(request.systemPromptB());
        conversation.setTotalRounds(0);
        conversationRepository.save(conversation);

        int maxRounds = request.maxRounds() > 0 ? request.maxRounds() : 100;

        runner.run(
                conversation.getId(),
                request.systemPromptA(),
                request.systemPromptB(),
                request.topic(),
                maxRounds
        );

        return conversation.getId();
    }

    public void stop(Long conversationId) {
        AgentConversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Not found: " + conversationId));
        conversation.setStatus(ConversationStatus.STOPPED);
        conversation.setFinishedAt(LocalDateTime.now());
        conversationRepository.save(conversation);
    }

    public List<ConversationResponse> findAll() {
        return conversationRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(c -> ExperimentMapper.toResponse(c,
                        messageRepository.findByConversationIdOrderByRoundNumberAsc(c.getId())))
                .toList();
    }

    public ConversationResponse findById(Long id) {
        AgentConversation conversation = conversationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Not found: " + id));
        return ExperimentMapper.toResponse(conversation,
                messageRepository.findByConversationIdOrderByRoundNumberAsc(id));
    }
}

package com.example.agent_chat.repository;

import com.example.agent_chat.model.AgentMessage;
import com.example.agent_chat.model.AgentSender;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AgentMessageRepository extends JpaRepository<AgentMessage, Long> {

    List<AgentMessage> findByConversationIdOrderByRoundNumberAsc(Long conversationId);

    int countByConversationId(Long conversationId);

    boolean existsByConversationIdAndSender(Long conversationId, AgentSender sender);
}

package com.example.agent_chat.repository;

import com.example.agent_chat.model.AgentConversation;
import com.example.agent_chat.model.ConversationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AgentConversationRepository extends JpaRepository<AgentConversation, Long> {

    List<AgentConversation> findAllByOrderByCreatedAtDesc();

    List<AgentConversation> findByStatus(ConversationStatus status);
}


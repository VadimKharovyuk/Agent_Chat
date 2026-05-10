package com.example.agent_chat.dto;

import com.example.agent_chat.model.ConversationStatus;

import java.time.LocalDateTime;
import java.util.List;

public record ConversationResponse(
        Long id,
        String topic,
        String systemPromptA,
        String systemPromptB,
        ConversationStatus status,
        int totalRounds,
        LocalDateTime createdAt,
        LocalDateTime finishedAt,
        List<MessageResponse> messages
) {}

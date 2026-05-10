package com.example.agent_chat.dto;

import com.example.agent_chat.model.AgentSender;

import java.time.LocalDateTime;

public record MessageResponse(
        Long id,
        AgentSender sender,
        String content,
        int roundNumber,
        LocalDateTime createdAt
) {}
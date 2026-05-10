package com.example.agent_chat.dto;
public record StartConversationRequest(
        String topic,
        String systemPromptA,
        String systemPromptB,
        int maxRounds
) {}

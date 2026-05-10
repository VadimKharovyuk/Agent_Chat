package com.example.agent_chat.mapper;

import com.example.agent_chat.dto.ConversationResponse;
import com.example.agent_chat.dto.MessageResponse;
import com.example.agent_chat.model.AgentConversation;
import com.example.agent_chat.model.AgentMessage;

import java.util.List;

public final class ExperimentMapper {

    private ExperimentMapper() {}

    public static ConversationResponse toResponse(AgentConversation conversation,
                                                  List<AgentMessage> messages) {
        return new ConversationResponse(
                conversation.getId(),
                conversation.getTopic(),
                conversation.getSystemPromptA(),
                conversation.getSystemPromptB(),
                conversation.getStatus(),
                conversation.getTotalRounds(),
                conversation.getCreatedAt(),
                conversation.getFinishedAt(),
                messages.stream().map(ExperimentMapper::toMessageResponse).toList()
        );
    }

    public static MessageResponse toMessageResponse(AgentMessage message) {
        return new MessageResponse(
                message.getId(),
                message.getSender(),
                message.getContent(),
                message.getRoundNumber(),
                message.getCreatedAt()
        );
    }
}

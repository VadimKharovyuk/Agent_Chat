package com.example.agent_chat.model;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "agent_conversations")
@Getter
@Setter
public class AgentConversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String topic;

    @Column(columnDefinition = "TEXT")
    private String systemPromptA;

    @Column(columnDefinition = "TEXT")
    private String systemPromptB;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConversationStatus status;

    private int totalRounds;

    private LocalDateTime createdAt;
    private LocalDateTime finishedAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        status = ConversationStatus.RUNNING;
    }
}

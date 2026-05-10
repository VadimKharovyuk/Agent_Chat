package com.example.agent_chat.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "agent_messages")
@Getter
@Setter
public class AgentMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    private AgentConversation conversation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AgentSender sender;   // AGENT_A / AGENT_B

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    private int roundNumber;

    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }
}

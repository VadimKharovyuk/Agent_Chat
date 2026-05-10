package com.example.agent_chat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class AgentChatApplication {

    public static void main(String[] args) {
        SpringApplication.run(AgentChatApplication.class, args);
    }

}

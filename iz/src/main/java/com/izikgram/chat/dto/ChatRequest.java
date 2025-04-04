package com.izikgram.chat.dto;

import com.izikgram.chat.config.ChatConfig;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {
    private String modelId;
    private ChatConfig config;
    private List<Message> messages;
}


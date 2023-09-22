package com.ClarifAI.main.sample.src.LLMInteractionModule;

import com.cjcrafter.openai.chat.ChatMessage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class ExtendedChatMessage {

    private ChatMessage baseMessage;
    private LocalDateTime timestamp;


    // Constructor
    public ExtendedChatMessage(ChatMessage baseMessage) {
        this(baseMessage, LocalDateTime.now());  // Default timestamp to current date-time and comment to empty string
    }

    public ExtendedChatMessage(ChatMessage baseMessage, LocalDateTime timestamp) {
        this.baseMessage = baseMessage;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public ChatMessage getBaseMessage() {
        return baseMessage;
    }

    public void setBaseMessage(ChatMessage baseMessage) {
        this.baseMessage = baseMessage;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    // Override equals, hashCode and toString (optional, but recommended for data-like classes in Java)

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExtendedChatMessage that = (ExtendedChatMessage) o;
        return Objects.equals(baseMessage, that.baseMessage) &&
                Objects.equals(timestamp, that.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(baseMessage, timestamp);
    }


    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return String.format("[%s] %s: %s", timestamp.format(formatter), baseMessage.getRole(), baseMessage.getContent());
    }
}

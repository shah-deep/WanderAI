package com.planner.travel.agentsystem.state;

import org.bsc.langgraph4j.prebuilt.MessagesState;
import dev.langchain4j.data.message.ChatMessage;
import java.util.Map;
import java.util.Optional;

public class ChatState extends MessagesState<ChatMessage> {
    public Optional<String> next() {
        return this.value("next");
    }

    public ChatState(Map<String, Object> initData) {
        super(initData);
    }
}
package com.planner.travel.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDto {
    private String content;
    private String sender; // Optional: to differentiate user/ai or for display

    public ChatMessageDto(String content) {
        this.content = content;
    }

//    public ChatMessageDto(String content, String sender) {
//        this.content = content;
//        this.sender = sender;
//    }

}
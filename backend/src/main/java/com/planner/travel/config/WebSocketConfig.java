package com.planner.travel.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue", "/user"); // "/user" for user-specific messages
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        /* Endpoint for WebSocket connections
//        registry.addEndpoint("/ws-chat").setAllowedOrigins("*");
     // registry.addEndpoint("/ws-chat").withSockJS();
         // You can add more options here, like allowed origins for CORS
       // registry.addEndpoint("/ws-chat").setAllowedOrigins("*").withSockJS(); */

        registry.addEndpoint("/ws-chat")
                .setAllowedOriginPatterns("*") // Changed from setAllowedOrigins
                .withSockJS();

    }
}
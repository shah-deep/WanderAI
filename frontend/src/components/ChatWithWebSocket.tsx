"use client";

import React from 'react';
import { useWebSocket } from '@/hooks/useWebSocket';
import ChatWindow from '@/components/ChatWindow';
import ChatInput from '@/components/ChatInput';
import ConnectionStatus from '@/components/ConnectionStatus';
import SuggestionPrompts from './SuggestionPrompts';

interface ChatWithWebSocketProps {
  onMessagesChange?: (messages: any[]) => void;
}

export default function ChatWithWebSocket({ onMessagesChange }: ChatWithWebSocketProps) {
  const {
    connected,
    connecting,
    messages,
    sendMessage,
    awaitingResponse
  } = useWebSocket(process.env.NEXT_PUBLIC_WEBSOCKET_URL || 'http://localhost:8080/ws-chat');

  // Track if the first user message has been sent
  const [showSuggestions, setShowSuggestions] = React.useState(true);

  // Hide suggestions after the first user message
  React.useEffect(() => {
    if (messages.some(m => m.sender.toLowerCase() === 'user')) {
      setShowSuggestions(false);
    }
  }, [messages]);

  // Notify parent of messages changes
  React.useEffect(() => {
    onMessagesChange?.(messages);
  }, [messages, onMessagesChange]);

  // Input should be disabled when connecting, disconnected, or awaiting response
  const inputDisabled = !connected || connecting || awaitingResponse;

  // Handler for suggestion click
  const handleSuggestion = (prompt: string) => {
    if (!inputDisabled) {
      sendMessage(prompt);
      setShowSuggestions(false);
    }
  };

  return (
    <div className="flex flex-col h-full">
      <ConnectionStatus 
        connected={connected} 
        connecting={connecting} 
      />
      
        <ChatWindow 
          messages={messages} 
          isConnecting={connecting} 
          awaitingResponse={awaitingResponse}
        />
        {showSuggestions && !inputDisabled && (
          <SuggestionPrompts onSelect={handleSuggestion} />
        )}
    
      <ChatInput 
        onSendMessage={sendMessage} 
        disabled={inputDisabled} 
      />
    </div>
  );
}
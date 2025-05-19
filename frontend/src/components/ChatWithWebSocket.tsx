"use client";

import React from 'react';
import { useWebSocket } from '@/hooks/useWebSocket';
import ChatWindow from '@/components/ChatWindow';
import ChatInput from '@/components/ChatInput';
import ConnectionStatus from '@/components/ConnectionStatus';

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

  // Notify parent of messages changes
  React.useEffect(() => {
    onMessagesChange?.(messages);
  }, [messages, onMessagesChange]);

  // Input should be disabled when connecting, disconnected, or awaiting response
  const inputDisabled = !connected || connecting || awaitingResponse;

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
      
      <ChatInput 
        onSendMessage={sendMessage} 
        disabled={inputDisabled} 
      />
    </div>
  );
}
"use client";

import React from 'react';
import { useWebSocket } from '@/hooks/useWebSocket';
import ChatWindow from '@/components/ChatWindow';
import ChatInput from '@/components/ChatInput';
import ConnectionStatus from '@/components/ConnectionStatus';

export default function ChatWithWebSocket() {
  const {
    connected,
    connecting,
    messages,
    sendMessage,
    awaitingResponse
  } = useWebSocket(process.env.NEXT_PUBLIC_WEBSOCKET_URL || 'http://localhost:8080/ws-chat');

  // Input should be disabled when connecting, disconnected, or awaiting response
  const inputDisabled = !connected || connecting || awaitingResponse;

  return (
    <>
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
      
    </>
  );
}
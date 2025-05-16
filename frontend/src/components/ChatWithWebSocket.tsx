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
      
      {awaitingResponse && (
        <div className="mt-2 text-sm text-gray-500 flex items-center">
          <svg className="animate-spin h-4 w-4 mr-2 text-gray-500" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
            <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
            <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
          </svg>
          Waiting for response...
        </div>
      )}
    </>
  );
}
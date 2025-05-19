import React, { useEffect, useRef } from 'react';
import { ChatMessage } from '@/hooks/useWebSocket';
import ChatMessageComponent from './ChatMessage';

interface ChatWindowProps {
  messages: ChatMessage[];
  isConnecting: boolean;
  awaitingResponse: boolean;
}

const ChatWindow: React.FC<ChatWindowProps> = ({ messages, isConnecting, awaitingResponse }) => {
  const messagesEndRef = useRef<HTMLDivElement>(null);

  // Auto-scroll to the latest message
  useEffect(() => {
    if (messagesEndRef.current) {
      messagesEndRef.current.scrollIntoView({ behavior: 'smooth' });
    }
  }, [messages]);

  return (
    <div className="bg-white border rounded-lg flex-1 overflow-y-auto p-4 min-h-0">
      {isConnecting && (
        <div className="text-center py-4 text-gray-500 italic">
          <p>Connecting to server...</p>
          <p>In case of prolonged connecting, please reload the page.</p>
        </div>
      )}
      
      {!isConnecting && messages.length === 0 && (
        <div className="text-center py-16 text-gray-500 italic flex-grow flex flex-col justify-center">
          <p className="mb-2">WanderAI: Plan your next adventure with ease</p>
          <p>Send a message to start the conversation!</p>
        </div>
      )}
      
      {messages.map((message, index) => (
        <ChatMessageComponent key={index} message={message} />
      ))}
      
      <div ref={messagesEndRef} />
    </div>
  );
};

export default ChatWindow;
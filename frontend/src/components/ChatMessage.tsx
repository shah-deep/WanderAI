import React from 'react';
import { ChatMessage } from '@/hooks/useWebSocket';

interface ChatMessageProps {
  message: ChatMessage;
}

const ChatMessageComponent: React.FC<ChatMessageProps> = ({ message }) => {
  const isUser = message.sender.toLowerCase() === 'user';
  
  return (
    <div 
      className={`mb-4 p-3 rounded-lg max-w-[80%] ${
        isUser 
          ? 'ml-auto bg-blue-500 text-white rounded-br-none' 
          : 'mr-auto bg-gray-200 text-gray-800 rounded-bl-none'
      }`}
    >
      <p className="text-sm whitespace-pre-wrap">{message.content}</p>
    </div>
  );
};

export default ChatMessageComponent;
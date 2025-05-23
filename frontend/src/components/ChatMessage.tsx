import React from 'react';
import { ChatMessage } from '@/hooks/useWebSocket';
import ReactMarkdown from 'react-markdown';

interface ChatMessageProps {
  message: ChatMessage;
}

const ChatMessageComponent: React.FC<ChatMessageProps> = ({ message }) => {
  const isUser = message.sender.toLowerCase() === 'user';
  
  return (
    <div 
      className={`mb-4 p-3 rounded-lg max-w-[80%] overflow-wrap-anywhere ${
        isUser 
          ? 'ml-auto bg-blue-500 text-white rounded-br-none' 
          : 'mr-auto bg-gray-200 text-gray-800 rounded-bl-none'
      }`}
    >
      <div className={`prose prose-sm dark:prose-invert max-w-none ${
        isUser
          ? 'prose-headings:text-white prose-p:text-white prose-strong:text-white prose-code:text-white prose-li:text-white prose-a:text-blue-100 hover:prose-a:text-blue-100'
          : 'prose-headings:text-gray-800 prose-p:text-gray-800 prose-strong:text-gray-800 prose-code:text-gray-800 prose-li:text-gray-800 prose-a:text-blue-600 hover:prose-a:text-blue-600'
      }`}>
        <ReactMarkdown>{message.content}</ReactMarkdown>
      </div>
    </div>
  );
};

export default ChatMessageComponent;
import React, { useState } from 'react';
import LoadingIndicator from './LoadingIndicator';

interface ChatInputProps {
  onSendMessage: (message: string) => void;
  disabled: boolean;
}

const ChatInput: React.FC<ChatInputProps> = ({ onSendMessage, disabled }) => {
  const [message, setMessage] = useState('');

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (message.trim() && !disabled) {
      onSendMessage(message);
      setMessage('');
    }
  };

  return (
    <form onSubmit={handleSubmit} className="mt-4 flex">
      <div className="flex-grow relative">
        <input
          type="text"
          className={`w-full px-4 py-2 border rounded-l-lg focus:outline-none focus:ring-2 focus:ring-blue-500 ${
            disabled ? 'bg-gray-100 text-gray-500' : 'bg-white'
          }`}
          placeholder={disabled ? "Waiting for response..." : "Type your message..."}
          value={message}
          onChange={(e) => setMessage(e.target.value)}
          disabled={disabled}
        />
        {disabled && (
          <div className="absolute right-2 top-1/2 transform -translate-y-1/2">
            <LoadingIndicator />
          </div>
        )}
      </div>
      <button
        type="submit"
        className={`px-4 py-2 rounded-r-lg font-medium ${
          disabled
            ? 'bg-gray-300 text-gray-500 cursor-not-allowed'
            : 'bg-blue-500 text-white hover:bg-blue-600'
        }`}
        disabled={disabled}
      >
        Send
      </button>
    </form>
  );
};

export default ChatInput;
import React, { useState } from 'react';
import { ArrowDownTrayIcon } from '@heroicons/react/24/outline';
import { ChatMessage } from '@/hooks/useWebSocket';

interface ExportChatProps {
  messages: ChatMessage[];
}

const ExportChat: React.FC<ExportChatProps> = ({ messages }) => {
  const [showTooltip, setShowTooltip] = useState(false);
  const [showDropdown, setShowDropdown] = useState(false);

  const handleExport = (format: 'text' | 'json') => {
    let content: string;
    let filename: string;
    let type: string;

    if (format === 'json') {
      content = JSON.stringify(messages, null, 2);
      filename = 'chat-export.json';
      type = 'application/json';
    } else {
      content = messages
        .map(msg => `${msg.sender}: ${msg.content}`)
        .join('\n\n');
      filename = 'chat-export.txt';
      type = 'text/plain';
    }
    
    const blob = new Blob([content], { type });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = filename;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
    setShowDropdown(false);
  };

  return (
    <div className="relative">
      <button
        onClick={() => setShowDropdown(!showDropdown)}
        onMouseEnter={() => setShowTooltip(true)}
        onMouseLeave={() => setShowTooltip(false)}
        className="p-2 hover:bg-gray-100 rounded-full transition-colors"
      >
        <ArrowDownTrayIcon className="h-5 w-5 text-gray-600" />
      </button>
      
      {showTooltip && !showDropdown && (
        <div className="absolute bottom-full left-1/2 transform -translate-x-1/2 mb-2 px-2 py-1 text-xs text-white bg-gray-800 rounded whitespace-nowrap">
          Export Chat
        </div>
      )}

      {showDropdown && (
        <div className="absolute right-0 mt-2 w-48 bg-white rounded-md shadow-lg z-10 border border-gray-200">
          <button
            onClick={() => handleExport('text')}
            className="block w-full text-left px-4 py-2 hover:bg-gray-100"
          >
            Export as Text
          </button>
          <button
            onClick={() => handleExport('json')}
            className="block w-full text-left px-4 py-2 hover:bg-gray-100"
          >
            Export as JSON
          </button>
        </div>
      )}
    </div>
  );
};

export default ExportChat;

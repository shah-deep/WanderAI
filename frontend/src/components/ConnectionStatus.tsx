import React from 'react';

interface ConnectionStatusProps {
  connected: boolean;
  connecting: boolean;
}

const ConnectionStatus: React.FC<ConnectionStatusProps> = ({ connected, connecting }) => {
  // Don't render anything if connected
  if (connected) return null;

  return (
    <div className="flex items-center mb-4">
      <div 
        className={`h-3 w-3 rounded-full mr-2 ${
          connecting 
            ? 'bg-yellow-400 animate-pulse' 
            : 'bg-red-500'
        }`}
      />
      <span className="text-sm text-gray-600">
        {connecting 
          ? 'Connecting...' 
          : 'Disconnected'}
      </span>
    </div>
  );
};

export default ConnectionStatus;
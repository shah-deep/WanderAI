"use client";

import React, { useState, useEffect } from 'react';
import dynamic from 'next/dynamic';
import ExportChat from '@/components/ExportChat';
import { checkBackendHealth } from '@/lib/api';

// Dynamically import the WebSocket hook with no SSR
const ChatWithWebSocket = dynamic(
  () => import('@/components/ChatWithWebSocket').then((mod) => mod.default),
  { ssr: false }
);

export default function Home() {
  const [messages, setMessages] = useState<any[]>([]);

  useEffect(() => {
    // Check backend health on page load
    checkBackendHealth().then(isHealthy => {
      console.log('Backend health check:', isHealthy ? 'OK' : 'Failed');
    });
  }, []);

  return (
    <main className="flex flex-col items-center h-screen w-full bg-gray-50 p-4">
      <div className="w-full max-w-[95%] md:max-w-[90%] lg:max-w-[85%] bg-white rounded-xl shadow-lg p-6 flex flex-col h-[calc(100vh-2rem)]">
        <div className="flex items-center justify-center mb-4 relative">
          <h1 className="text-2xl font-bold text-gray-800">
            WanderAI
          </h1>
          <div className="absolute right-0">
            <ExportChat messages={messages} />
          </div>
        </div>
        <div className="flex-1 min-h-0">
          <ChatWithWebSocket onMessagesChange={setMessages} />
        </div>
      </div>
    </main>
  );
}
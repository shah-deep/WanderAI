"use client";

import React, { useState, useEffect } from 'react';
import dynamic from 'next/dynamic';

// Dynamically import the WebSocket hook with no SSR
const ChatWithWebSocket = dynamic(
  () => import('@/components/ChatWithWebSocket').then((mod) => mod.default),
  { ssr: false }
);

export default function Home() {
  return (
    <main className="min-h-screen flex flex-col items-center justify-center bg-gray-50 p-4">
      <div className="w-full max-w-[95%] md:max-w-[90%] lg:max-w-[85%] bg-white rounded-xl shadow-lg p-6">
        <h1 className="text-2xl font-bold text-center mb-4 text-gray-800">
          Travel Planner
        </h1>
        
        <ChatWithWebSocket />
      </div>
    </main>
  );
}
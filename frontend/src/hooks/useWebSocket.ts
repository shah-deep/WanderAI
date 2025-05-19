import { useState, useEffect, useCallback } from 'react';
import { Client, StompSubscription } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

// Define the message interface
export interface ChatMessage {
  content: string;
  sender: string;
}

// Define the hook return interface
interface UseWebSocketReturn {
  connected: boolean;
  connecting: boolean;
  messages: ChatMessage[];
  sendMessage: (message: string) => void;
  awaitingResponse: boolean;
}

export const useWebSocket = (serverUrl: string): UseWebSocketReturn => {

  const [client, setClient] = useState<Client | null>(null);
  const [connected, setConnected] = useState<boolean>(false);
  const [connecting, setConnecting] = useState<boolean>(false);
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [subscription, setSubscription] = useState<StompSubscription | null>(null);
  const [awaitingResponse, setAwaitingResponse] = useState<boolean>(false);
  const [sessionId, setSessionId] = useState<string | null>(null);

  // Initialize the STOMP client
  useEffect(() => {
    // Only run in browser environment
    if (typeof window === 'undefined') return;

    const initializeClient = () => {
      setConnecting(true);
      
      // Determine if we should use secure WebSocket
      const useSecureWs = process.env.NEXT_PUBLIC_USE_WSS === 'true' || 
                         (typeof window !== 'undefined' && window.location.protocol === 'https:');
      
      // Modify URL to use appropriate protocol
      const wsUrl = useSecureWs 
        ? serverUrl.replace('http://', 'https://').replace('ws://', 'wss://')
        : serverUrl;
      
      // Create a new STOMP client over SockJS
      const socket = new SockJS(wsUrl);
      const newClient = new Client({
        webSocketFactory: () => socket,
        debug: (str) => {
          console.log(str);
        },
        reconnectDelay: 5000,
        heartbeatIncoming: 4000,
        heartbeatOutgoing: 4000,
        onConnect: (frame) => {
          console.log('Connected to STOMP server');
          setConnected(true);
          setConnecting(false);
          
          // console.log(socket);
          

          // Extract session ID from the socket URL (format: ws://host/ws-chat/470/yzw5r3mp/websocket)
          let extractedSessionId: string | null = null;
          // _transport is not public, so check existence before accessing
          if ((socket as any)._transport && (socket as any)._transport.url) {
            const socketUrl = (socket as any)._transport.url;
            // console.log('Socket URL:', socketUrl);

            const urlParts = socketUrl.split('/');
            extractedSessionId = urlParts[urlParts.length - 2];
            // console.log('Extracted session ID:', extractedSessionId);

            setSessionId(extractedSessionId);
          } else {
            console.warn('SockJS _transport or url not available yet.');
          }
          
          if (extractedSessionId) {
            // Subscribe to session-specific topic
            const sub = newClient.subscribe(`/topic/reply/${extractedSessionId}`, (message) => {
              console.log('Received message:', message.body);
              const parsedMessage = JSON.parse(message.body);
              setMessages(prev => [...prev, parsedMessage]);
              setAwaitingResponse(false); // Message received, no longer awaiting
            });
            
            setSubscription(sub);
          }
        },
        onDisconnect: () => {
          console.log('Disconnected from STOMP server');
          setConnected(false);
          setConnecting(false);
          setAwaitingResponse(false);
        },
        onStompError: (frame) => {
          console.error('STOMP error:', frame);
          setConnected(false);
          setConnecting(false);
          setAwaitingResponse(false);
        }
      });
      
      // Activate the client
      newClient.activate();
      setClient(newClient);
    };

    initializeClient();

    // Cleanup function to disconnect when component unmounts
    return () => {
      if (subscription) {
        subscription.unsubscribe();
      }
      
      if (client && client.connected) {
        client.deactivate();
      }
    };
  }, [serverUrl]);

  // Function to send messages
  const sendMessage = useCallback((content: string) => {
    if (client && client.connected && content.trim()) {
      const chatMessage: ChatMessage = {
        content,
        sender: 'User'
      };
      
      client.publish({
        destination: '/app/chat.sendMessage',
        body: JSON.stringify(chatMessage)
      });
      
      // Add user message to the list
      setMessages(prev => [...prev, chatMessage]);
      setAwaitingResponse(true); // Now awaiting response
    }
  }, [client]);

  return {
    connected,
    connecting,
    messages,
    sendMessage,
    awaitingResponse
  };
};
// This file provides TypeScript definitions for environment variables
// to ensure type safety when accessing them in your application

declare namespace NodeJS {
  interface ProcessEnv {
    NEXT_PUBLIC_WEBSOCKET_URL: string;
    NEXT_PUBLIC_BACKEND_URL: string;
  }
}
# WanderAI Frontend

React-based frontend for the WanderAI travel planning assistant, built with Next.js and TypeScript.

## Prerequisites

- Node.js 20.x or later
- npm/yarn package manager
- Backend service running ([see backend README](../backend/README.md))

## Configuration

1. Create a `.env.local` file in the root directory with:
```properties
NEXT_PUBLIC_WEBSOCKET_URL=http://localhost:8080/ws-chat
```

2. For production deployment, use `wss://` instead of `ws://` for secure WebSocket connections.

## Installation

Install dependencies:
```bash
npm install
# or
yarn install
```

## Running the Application

Development mode:
```bash
npm run dev
# or
yarn dev
```

Production build:
```bash
npm run build
npm start
# or
yarn build
yarn start
```

The application will be available at `http://localhost:3000`

## Project Structure

```
src/
├── app/                # Next.js app directory
├── components/         # React components
│   ├── ChatInput      # Message input component
│   ├── ChatMessage    # Message display component
│   ├── ChatWindow     # Main chat interface
│   └── ExportChat     # Chat export functionality
├── hooks/             # Custom React hooks
│   └── useWebSocket   # WebSocket connection management
├── lib/              # Utility functions and constants
└── types/            # TypeScript type definitions
```

## Features

- Real-time chat interface with WebSocket connection
- Markdown support for messages
- Chat export functionality (TEXT/JSON)
- Responsive design with Tailwind CSS
- TypeScript for type safety

## Development

### Available Scripts

- `npm run dev`: Start development server
- `npm run build`: Create production build
- `npm start`: Start production server
- `npm run lint`: Run ESLint
- `npm run export`: Export static site

### Technology Stack

- Next.js 15.x
- React 19.x
- TypeScript
- Tailwind CSS
- STOMP WebSocket client
- React Markdown for message formatting

## Building for Production

1. Create production build:
```bash
npm run build
```

2. Export static site (if needed):
```bash
npm run export
```

## Environment Variables

- `NEXT_PUBLIC_WEBSOCKET_URL`: WebSocket server URL
- `NEXT_PUBLIC_BASE_PATH`: Base path for deployment (optional)
- `NEXT_PUBLIC_USE_WSS`: Use secure WebSocket connection (optional)
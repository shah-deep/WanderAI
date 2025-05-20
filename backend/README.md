# WanderAI Backend

Backend service for the WanderAI travel planning assistant, built with Spring Boot and Langchain4j.

## Prerequisites

- Java Development Kit (JDK) 21
- Maven 3.9.9+ or use the included Maven wrapper
- API Keys:
  - TripAdvisor API key
  - Google Gemini API key

## Configuration

1. Create a `.env` file in the root directory with:
```properties
TRIPADVISOR_API_KEY=your_tripadvisor_api_key
LLM_API_KEY_GEMINI=your_gemini_api_key
```

2. The application.properties file will automatically pick up these environment variables.

## Building the Project

Using Maven wrapper (recommended):
```bash
./mvnw clean install
```

Using Maven directly:
```bash
mvn clean install
```

## Running the Application

Using Maven wrapper:
```bash
./mvnw spring-boot:run
```

Using Maven:
```bash
mvn spring-boot:run
```

Using JAR file:
```bash
java -jar out/artifacts/travel.jar
```

The server will start on `http://localhost:8080`

## API Endpoints

- WebSocket endpoint: `/ws-chat`
- Health check: `GET /health`

## WebSocket Communication

The backend uses STOMP protocol over WebSocket for real-time communication:
- Client connect endpoint: `/ws-chat`
- Message publish endpoint: `/app/chat.sendMessage`
- Client subscription topic: `/topic/reply/{sessionId}`

## Development

### Project Structure
```
src/main/java/com/planner/travel/
├── agentsystem/           # AI agent implementation
│   ├── agents/           # Agent implementations
│   ├── assistant/        # Agent assistants
│   ├── state/           # State management
│   └── tools/           # Agent tools
├── config/               # Configuration classes
├── controller/          # WebSocket controllers
└── util/                # Utility classes
```

### Key Components

- `ChatController`: Handles WebSocket connections and message routing
- `TravelPlannerWorkflow`: Manages the agent system workflow
- `SupervisorAgent`: Main decision-making agent
- `SearchAgent`: Handles location searches
- `DetailsAgent`: Retrieves detailed information

## Docker Support

Build the Docker image:
```bash
docker build -t wanderai-backend .
```

Run the container:
```bash
docker run -p 8080:8080 \
  -e TRIPADVISOR_API_KEY=your_key \
  -e LLM_API_KEY_GEMINI=your_key \
  wanderai-backend
```


## Error Handling

The application includes comprehensive error handling:
- WebSocket session management
- Graceful error responses to clients
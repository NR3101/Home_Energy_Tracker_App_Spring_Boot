# Home Energy Tracker

A microservice-based application for tracking home energy usage.

## Project Status
🚧 **In Development** - This project is currently under active development.

## Architecture
This is a microservices architecture project consisting of:
- **api-gateway**: Central entry point that routes API requests to backend services
- **user-service**: User management service
- **device-service**: Device management service
- **ingestion-service**: Energy usage data ingestion service
- **usage-service**: Energy usage monitoring and alerting service
- **alert-service**: Alert notification service
- **insight-service**: AI-powered energy insights and recommendations service

## Tech Stack
- Java 25
- Spring Boot 4.0.0
- Spring Security (OAuth2 Resource Server)
- SpringDoc OpenAPI (Swagger UI)
- Spring Data JPA
- Spring Mail
- Spring AI (Ollama integration)
- Flyway (Database Migration)
- Keycloak
- MySQL
- Apache Kafka
- InfluxDB
- Ollama (Local LLM)
- Docker & Docker Compose
- Maven
- Lombok
- AOP (Aspect-Oriented Programming)

## Project Structure
```
.
├── api-gateway/           # API gateway microservice
│   ├── src/
│   │   └── main/
│   │       ├── java/
│   │       └── resources/
│   └── pom.xml
├── docker/                # Docker configuration files
│   ├── mysql/             # MySQL initialization scripts
│   └── kafka_data/        # Kafka data storage
├── user-service/          # User management microservice
│   ├── src/
│   │   └── main/
│   │       ├── java/
│   │       └── resources/
│   └── pom.xml
├── device-service/        # Device management microservice
│   ├── src/
│   │   └── main/
│   │       ├── java/
│   │       └── resources/
│   └── pom.xml
├── ingestion-service/     # Energy data ingestion microservice
│   ├── src/
│   │   └── main/
│   │       ├── java/
│   │       └── resources/
│   └── pom.xml
├── usage-service/         # Energy usage monitoring and alerting microservice
│   ├── src/
│   │   └── main/
│   │       ├── java/
│   │       └── resources/
│   └── pom.xml
├── alert-service/         # Alert notification microservice
│   ├── src/
│   │   └── main/
│   │       ├── java/
│   │       └── resources/
│   └── pom.xml
├── insight-service/       # AI-powered insights microservice
│   ├── src/
│   │   └── main/
│   │       ├── java/
│   │       └── resources/
│   └── pom.xml
└── docker-compose.yml     # Docker Compose configuration
```

## Services

### API Gateway
Routes external API calls to backend services:
- `/api/v1/user/**` -> `http://localhost:8080`
- `/api/v1/device/**` -> `http://localhost:8081`
- `/api/v1/ingestion/**` -> `http://localhost:8082`
- `/api/v1/insight/**` -> `http://localhost:8085`

Also handles JWT authentication using Keycloak for protected routes.
Also exposes aggregated Swagger UI and proxied service docs for user and device services.

- Swagger UI: `/swagger-ui.html`
- User Service docs via gateway: `/docs/user-service/v3/api-docs`
- Device Service docs via gateway: `/docs/device-service/v3/api-docs`

**Port**: 9000

### User Service
Handles user management operations including:
- User registration
- User profile management
- User authentication data
- Email validation
- OpenAPI docs support (SpringDoc)

**Port**: 8080

### Device Service
Handles device management operations including:
- Device registration
- Device profile management (name, type, location)
- Device association with users
- OpenAPI docs support (SpringDoc)

**Port**: 8081

### Ingestion Service
Handles energy usage data ingestion operations including:
- Energy usage data ingestion via REST API
- Data publishing to Kafka

**Port**: 8082

### Usage Service
Handles energy usage monitoring and alerting including:
- Consuming energy usage events from Kafka
- Storing time-series data in InfluxDB
- Aggregating device energy usage per user
- Threshold-based alerting system
- Publishing alerts to Kafka

**Port**: 8083

### Alert Service
Handles alert notifications including:
- Consuming alert events from Kafka
- Sending email notifications via SMTP
- Tracking alert delivery status in MySQL

**Port**: 8084

### Insight Service
Provides AI-powered energy insights and recommendations including:
- Generating personalized energy-saving tips using local LLM
- Analyzing device-level energy consumption patterns
- Providing comprehensive energy usage overviews
- Integration with Ollama for natural language insights

**Port**: 8085

## Getting Started

### Prerequisites
- Java 25
- Maven
- Docker & Docker Compose

### Running with Docker Compose
```bash
docker-compose up -d
```

This starts MySQL, Kafka, Kafka UI, InfluxDB, Mailpit, Keycloak, and Keycloak MySQL.

### Running Services Locally

**API Gateway:**
```bash
cd api-gateway
./mvnw spring-boot:run
```

**User Service:**
```bash
cd user-service
./mvnw spring-boot:run
```

**Device Service:**
```bash
cd device-service
./mvnw spring-boot:run
```

**Ingestion Service:**
```bash
cd ingestion-service
./mvnw spring-boot:run
```

**Usage Service:**
```bash
cd usage-service
./mvnw spring-boot:run
```

**Alert Service:**
```bash
cd alert-service
./mvnw spring-boot:run
```

**Insight Service:**
```bash
cd insight-service
./mvnw spring-boot:run
```

### Database Migrations
Database migrations are handled automatically by Flyway on application startup.

## Development

### Building the Project

**API Gateway:**
```bash
cd api-gateway
./mvnw clean install
```

**User Service:**
```bash
cd user-service
./mvnw clean install
```

**Device Service:**
```bash
cd device-service
./mvnw clean install
```

**Ingestion Service:**
```bash
cd ingestion-service
./mvnw clean install
```

**Usage Service:**
```bash
cd usage-service
./mvnw clean install
```

**Alert Service:**
```bash
cd alert-service
./mvnw clean install
```

**Insight Service:**
```bash
cd insight-service
./mvnw clean install
```


## API Documentation
Available now for implemented services:

- API Gateway Swagger UI: `http://localhost:9000/swagger-ui.html`
- User Service docs (via gateway): `http://localhost:9000/docs/user-service/v3/api-docs`
- Device Service docs (via gateway): `http://localhost:9000/docs/device-service/v3/api-docs`

## Contributing
This is a personal project currently in development.

## License
TBD

## Author
Neeraj

## Progress Tracking
Check the [commit history](../../commits) to track development progress.


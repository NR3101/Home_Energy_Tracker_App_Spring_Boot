# Home Energy Tracker

A microservice-based application for tracking home energy usage.

## Project Status
🚧 **In Development** - This project is currently under active development.

## Architecture
This is a microservices architecture project consisting of:
- **user-service**: User management service

## Tech Stack
- Java 25
- Spring Boot 4.0.0
- Spring Data JPA
- Flyway (Database Migration)
- MySQL
- Docker & Docker Compose
- Maven
- Lombok
- AOP (Aspect-Oriented Programming)

## Project Structure
```
.
├── docker/                 # Docker configuration files
│   └── mysql/             # MySQL initialization scripts
├── user-service/          # User management microservice
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   └── resources/
│   │   └── test/
│   └── pom.xml
└── docker-compose.yml     # Docker Compose configuration
```

## Services

### User Service
Handles user management operations including:
- User registration
- User profile management
- User authentication data
- Email validation

**Port**: TBD

## Getting Started

### Prerequisites
- Java 25
- Maven
- Docker & Docker Compose

### Running with Docker Compose
```bash
docker-compose up -d
```

### Running User Service Locally
```bash
cd user-service
./mvnw spring-boot:run
```

### Database Migrations
Database migrations are handled automatically by Flyway on application startup.

## Development

### Building the Project
```bash
cd user-service
./mvnw clean install
```

### Running Tests
```bash
cd user-service
./mvnw test
```

## API Documentation
API documentation will be available once Swagger/OpenAPI is integrated.

## Contributing
This is a personal project currently in development.

## License
TBD

## Author
Neeraj

## Progress Tracking
Check the [commit history](../../commits) to track development progress.


# Home Energy Tracker

A microservice-based application for tracking home energy usage.

## Project Status
🚧 **In Development** - This project is currently under active development.

## Architecture
This is a microservices architecture project consisting of:
- **user-service**: User management service
- **device-service**: Device management service
- **ingestion-service**: Energy usage data ingestion service
- **usage-service**: Energy usage monitoring and alerting service

## Tech Stack
- Java 25
- Spring Boot 4.0.0
- Spring Data JPA
- Flyway (Database Migration)
- MySQL
- Apache Kafka
- InfluxDB
- Docker & Docker Compose
- Maven
- Lombok
- AOP (Aspect-Oriented Programming)

## Project Structure
```
.
├── docker/                 # Docker configuration files
│   ├── mysql/             # MySQL initialization scripts
│   └── kafka_data/        # Kafka data storage
├── user-service/          # User management microservice
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   └── resources/
│   │   └── test/
│   └── pom.xml
├── device-service/        # Device management microservice
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   └── resources/
│   │   └── test/
│   └── pom.xml
├── ingestion-service/     # Energy data ingestion microservice
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   └── resources/
│   │   └── test/
│   └── pom.xml
├── usage-service/         # Energy usage monitoring and alerting microservice
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

**Port**: 8080

### Device Service
Handles device management operations including:
- Device registration
- Device profile management (name, type, location)
- Device association with users

**Port**: 8081

### Ingestion Service
Handles energy usage data ingestion operations including:
- Energy usage data ingestion via REST API
- Data publishing to Kafka
- Parallel data simulation for testing

**Port**: 8082

### Usage Service
Handles energy usage monitoring and alerting including:
- Consuming energy usage events from Kafka
- Storing time-series data in InfluxDB
- Aggregating device energy usage per user
- Threshold-based alerting system
- Publishing alerts to Kafka

**Port**: 8083

## Getting Started

### Prerequisites
- Java 25
- Maven
- Docker & Docker Compose

### Running with Docker Compose
```bash
docker-compose up -d
```

### Running Services Locally

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

### Database Migrations
Database migrations are handled automatically by Flyway on application startup.

## Development

### Building the Project

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

### Running Tests

**User Service:**
```bash
cd user-service
./mvnw test
```

**Device Service:**
```bash
cd device-service
./mvnw test
```

**Ingestion Service:**
```bash
cd ingestion-service
./mvnw test
```

**Usage Service:**
```bash
cd usage-service
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


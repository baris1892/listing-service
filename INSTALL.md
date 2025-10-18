# ⚙️ Installation & Local Setup Guide

This document explains how to set up and run the **Listing Service** locally for development and testing.

---

## 1. Prerequisites

Make sure you have the following installed:

- **Java 21 (JDK)**
- **Maven 3.9+**
- **Docker & Docker Compose**

---

## 2. Clone the Repository

```
git clone https://github.com/baris-top-portfolio/listing-service.git
cd listing-service
```

## 3. Configure Environment Variables

Copy the example environment file and adjust values if necessary:  
`cp .env.dist .env`  
The .env file defines credentials and service URLs used during local development (Postgres, Redis, Kafka, Keycloak).

## 4. Start Infrastructure with Docker

Start all required dependencies via Docker Compose:  
`docker-compose up -d`

This will start:

- PostgreSQL – database
- Kafka – event broker
- Redis – caching layer
- Keycloak – authentication server

You can verify running containers using:  
`docker ps`

## 5. Run the Application

Make sure all required containers (Postgres, Redis, Kafka, Keycloak) are running before starting the service.

Then start the Spring Boot application with:

```
export $(grep -v '^#' .env | xargs) && mvn spring-boot:run
```

This command:

- Loads environment variables from .env
- Starts the app with the dev Spring profile (as defined in `.env`)

Once started, the service will be available at:  
👉 http://localhost:8080/api/v1/listings

Swagger UI (API documentation) is accessible at:  
👉 http://localhost:8080/docs

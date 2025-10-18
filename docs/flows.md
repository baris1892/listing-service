# Listing & Messaging Microservices – Event Flows

This document provides **detailed event and system flows** for the Listing and Messaging microservices.

## Listing Retrieval Flow

Shows how the Listing Service first checks Redis for cached data before querying Postgres — ensuring fast and efficient
retrieval of listing details.

```mermaid
sequenceDiagram
    participant User
    participant ListingService
    participant Redis
    participant PostgresDB
    User ->> ListingService: GET /api/v1/listings/{id}
    ListingService ->> Redis: Check cache
    alt cache miss
        ListingService ->> PostgresDB: Query listing
        PostgresDB -->> ListingService: Return listing
        ListingService ->> Redis: Cache listing
    end
    ListingService -->> User: Return listing
```

## Listing Creation & Approval Flow

Demonstrates how a new listing is created and approved by an admin.
Once approved, the service publishes a Kafka event consumed by the Messaging Service to trigger a notification email.

```mermaid
sequenceDiagram
    participant User
    participant ListingService
    participant PostgresDB
    participant Admin
    participant Kafka
    participant MessagingService
    User ->> ListingService: POST /api/v1/listings (data)
    ListingService ->> PostgresDB: Save listing (status=PENDING)
    ListingService -->> User: Return confirmation
    Admin ->> ListingService: PATCH /api/v1/admin/listings/{id}/approve
    ListingService ->> PostgresDB: Update status=APPROVED
    ListingService -->> Admin: Return approval confirmation
    ListingService ->> Kafka: Produce ListingStatusChangedEvent
    Kafka -->> MessagingService: Consume ListingStatusChangedEvent
    MessagingService -->> User: Send notification email
```

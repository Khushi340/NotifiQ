# NotifiQ - Reliable Multi-Channel Notification Delivery Platform

NotifiQ is a backend-focused notification delivery platform built using Spring Boot. It supports reliable notification delivery across multiple channels such as Email, In-App, and Webhook, along with user preferences, quiet hours, async processing, retry handling, dead-letter queue, replay support, idempotency, and delivery monitoring.

## Project Overview

Modern applications need to send notifications reliably across different channels. A simple notification system only sends a message once, but real-world systems need to handle failures, retries, duplicate requests, user preferences, and delivery tracking.

NotifiQ is designed to solve these problems by providing a reliable multi-channel notification backend.

## Key Features

* User management
* User notification preferences
* Email notification delivery
* In-app notification delivery
* Webhook notification delivery
* Idempotency using unique idempotency keys
* Quiet hours support
* Async notification dispatch
* Scheduled delivery using Spring Scheduler
* Retry mechanism with backoff
* Dead-letter queue for permanently failed notifications
* Replay support for dead-lettered notifications
* Read/unread tracking for in-app notifications
* Notification statistics API
* Swagger/OpenAPI documentation

## Supported Notification Channels

### Email

Email notifications are delivered using Spring Mail and Mailtrap for testing.

### In-App

In-app notifications are stored inside the application and support read/unread tracking using `readAt`.

### Webhook

Webhook notifications are delivered to an external system using HTTP POST. The user's webhook URL is stored in preferences.

Example webhook payload:

```json
{
  "notificationId": 10,
  "userId": 5,
  "type": "ORDER_PLACED",
  "channel": "WEBHOOK",
  "priority": "HIGH",
  "subject": "Webhook test",
  "message": "This should be delivered to webhook.site",
  "createdAt": "2026-06-13T11:52:34.607351800"
}
```

## Reliability Features

### Async Dispatch

Notification delivery is handled asynchronously using a custom `ThreadPoolTaskExecutor`. The API returns quickly after saving the notification, while delivery happens in the background.

### Retry Mechanism

If delivery fails, the notification moves to `RETRYING` status and is retried later based on `nextRetryAt`.

Current retry strategy:

```text
Failure 1 -> RETRYING
Failure 2 -> RETRYING
Failure 3 -> RETRYING
Failure 4 -> DEAD_LETTERED
```

This means the system supports the original attempt plus 3 retries.

### Dead Letter Queue

If a notification keeps failing after retries, it is marked as `DEAD_LETTERED` and a dead-letter record is created.

### Replay Dead Letter

Admin can replay a dead-lettered notification. Replay resets the notification and requeues it for delivery.

## Tech Stack

* Java 17
* Spring Boot
* Spring Web
* Spring Data JPA
* PostgreSQL
* Spring Validation
* Spring Mail
* Spring Scheduler
* Spring Async
* Lombok
* Swagger/OpenAPI
* Mailtrap
* Webhook.site

## Core Modules

```text
com.khush.notifiq
├── config
├── common
├── user
├── preference
├── notification
│   ├── dto
│   ├── delivery
│   ├── scheduler
│   └── deadletter
```

## Notification Status Flow

```text
QUEUED
  ↓
SENT
```

On delivery failure:

```text
QUEUED
  ↓
RETRYING
  ↓
RETRYING
  ↓
RETRYING
  ↓
DEAD_LETTERED
```

When skipped due to user preference:

```text
SKIPPED_BY_PREFERENCE
```

## Important Statuses

| Status                | Meaning                                        |
| --------------------- | ---------------------------------------------- |
| QUEUED                | Notification is saved and waiting for delivery |
| SENT                  | Notification delivered successfully            |
| RETRYING              | Delivery failed and will be retried            |
| DEAD_LETTERED         | Delivery failed after max retries              |
| SKIPPED_BY_PREFERENCE | User preference disabled this channel          |

## API Endpoints

### User APIs

| Method | Endpoint          | Description    |
| ------ | ----------------- | -------------- |
| POST   | `/api/users`      | Create user    |
| GET    | `/api/users`      | Get all users  |
| GET    | `/api/users/{id}` | Get user by ID |
| PUT    | `/api/users/{id}` | Update user    |
| DELETE | `/api/users/{id}` | Delete user    |

### Preference APIs

| Method | Endpoint                          | Description               |
| ------ | --------------------------------- | ------------------------- |
| GET    | `/api/users/{userId}/preferences` | Get user preferences      |
| PUT    | `/api/users/{userId}/preferences` | Create/update preferences |

Example preference payload:

```json
{
  "emailEnabled": true,
  "inAppEnabled": true,
  "webhookEnabled": true,
  "webhookUrl": "https://webhook.site/your-unique-url",
  "quietHoursEnabled": false,
  "quietHoursStart": "22:00:00",
  "quietHoursEnd": "08:00:00",
  "preferredChannel": "WEBHOOK"
}
```

### Notification APIs

| Method | Endpoint                                  | Description                      |
| ------ | ----------------------------------------- | -------------------------------- |
| POST   | `/api/notifications`                      | Create notification              |
| GET    | `/api/notifications/user/{userId}`        | Get notifications by user        |
| PATCH  | `/api/notifications/{id}/read`            | Mark in-app notification as read |
| GET    | `/api/notifications/user/{userId}/unread` | Get unread in-app notifications  |
| GET    | `/api/notifications/stats`                | Get notification statistics      |

Example notification request:

```json
{
  "userId": 5,
  "type": "ORDER_PLACED",
  "channel": "WEBHOOK",
  "priority": "HIGH",
  "subject": "Webhook test",
  "message": "This should be delivered to webhook.site",
  "idempotencyKey": "webhook-test-001"
}
```

### Dead Letter APIs

| Method | Endpoint                              | Description                         |
| ------ | ------------------------------------- | ----------------------------------- |
| GET    | `/api/admin/dead-letters`             | Get all dead-lettered notifications |
| POST   | `/api/admin/dead-letters/{id}/replay` | Replay a dead-lettered notification |

## Notification Statistics

Stats API returns counts for:

```json
{
  "totalNotifications": 5,
  "sentNotifications": 4,
  "queuedNotifications": 0,
  "retryingNotifications": 0,
  "failedNotifications": 0,
  "deadLetteredNotifications": 1,
  "skippedByPreferenceNotifications": 0,
  "unreadInAppNotifications": 1
}
```

## Idempotency

NotifiQ currently uses DB-backed idempotency.

Each notification request must include a unique `idempotencyKey`.

Before creating a notification, the system checks whether a notification with the same key already exists. If it exists, the existing notification response is returned and no duplicate notification is created.

The `idempotencyKey` is also protected using a unique constraint in the database.

Redis-based idempotency is not implemented yet and can be added as a future enhancement.

## Swagger Documentation

Swagger UI is available at:

```text
http://localhost:8080/swagger-ui/index.html
```

OpenAPI docs are available at:

```text
http://localhost:8080/v3/api-docs
```

## Local Setup

### 1. Clone the repository

```bash
git clone <repository-url>
cd notifiq
```

### 2. Configure local properties

Create:

```text
src/main/resources/application-local.properties
```

Use the provided example file:

```text
src/main/resources/application-local.properties.example
```

Add your local PostgreSQL and Mailtrap credentials.

### 3. Run PostgreSQL

Create a local database:

```sql
CREATE DATABASE notifiq_db;
```

### 4. Run the application

```bash
mvn spring-boot:run
```

The application will start at:

```text
http://localhost:8080
```

## Testing Tools

* Postman or Swagger UI for API testing
* Mailtrap for email testing
* Webhook.site for webhook testing
* PostgreSQL for database verification

## Example Webhook Testing Flow

1. Open `https://webhook.site`
2. Copy the generated unique URL
3. Update user preference with that URL
4. Create a notification with `channel = WEBHOOK`
5. Check webhook.site for received JSON payload
6. Verify notification status becomes `SENT`

## Future Enhancements

* React dashboard
* Deployment on Render
* Hosted PostgreSQL using Supabase
* JWT authentication
* Redis-based idempotency
* Kafka/RabbitMQ based event-driven processing
* Advanced delivery history/audit logs
* Docker support

## Summary

NotifiQ is a reliable multi-channel notification platform supporting Email, In-App, and Webhook delivery. It includes user preferences, quiet hours, async processing, retry with backoff, dead-letter queue, replay support, DB-backed idempotency, read/unread tracking, statistics, and Swagger documentation.

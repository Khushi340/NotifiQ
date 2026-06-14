# NotifiQ - Reliable Multi-Channel Notification Delivery Platform

NotifiQ is a backend-focused notification delivery platform built using Spring Boot. It supports reliable notification delivery across multiple channels such as Email, In-App, and Webhook, along with user preferences, quiet hours, async processing, retry handling, dead-letter queue, replay support, idempotency, read/unread tracking, delivery statistics, Swagger documentation, and a React dashboard for monitoring and operations.

---

## Project Overview

Modern applications need to send notifications reliably across different channels. A simple notification system only sends a message once, but real-world systems need to handle failures, retries, duplicate requests, user preferences, quiet hours, delivery tracking, and operational visibility.

NotifiQ is designed to solve these problems by providing a reliable multi-channel notification backend.

The project follows a modular monolith structure with separate modules for users, preferences, notifications, delivery, scheduling, and dead-letter handling.

---

## Key Features

* User management
* User notification preferences
* Email notification delivery
* In-app notification delivery
* Webhook notification delivery
* DB-backed idempotency using unique idempotency keys
* Quiet hours support
* Async notification dispatch
* Scheduled retry processing using Spring Scheduler
* Retry mechanism with backoff
* Dead-letter queue for permanently failed notifications
* Replay support for dead-lettered notifications
* Read/unread tracking for in-app notifications
* Notification statistics API
* Recent notifications API for dashboard
* Swagger/OpenAPI documentation
* React dashboard integration

---

## Supported Notification Channels

### Email

Email notifications are delivered using Spring Mail and Mailtrap for local testing.

### In-App

In-app notifications are stored inside the application database and support read/unread tracking using `readAt`.

In-app notifications can be viewed through:

```text
GET /api/notifications/user/{userId}
GET /api/notifications/user/{userId}/unread
```

They are also visible in the React dashboard by searching for a user in the Notifications page.

### Webhook

Webhook notifications are delivered to an external system using HTTP POST. The user's webhook URL is stored in preferences.

Webhook delivery can be tested using `https://webhook.site`.

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

---

## Reliability Features

### Async Dispatch

Notification delivery is handled asynchronously using a custom `ThreadPoolTaskExecutor`.

The API saves the notification and returns quickly, while actual delivery happens in the background.

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

Replay flow:

```text
DEAD_LETTERED
  ↓
Replay
  ↓
QUEUED
  ↓
Scheduler/worker picks it again
  ↓
SENT or DEAD_LETTERED
```

---

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

---

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

---

## Notification Status Flow

Successful delivery:

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

When held due to quiet hours:

```text
QUEUED with nextRetryAt set to the next allowed delivery time
```

---

## Important Statuses

| Status                   | Meaning                                                |
| ------------------------ | ------------------------------------------------------ |
| `QUEUED`                 | Notification is saved and waiting for delivery         |
| `SENT`                   | Notification delivered successfully                    |
| `RETRYING`               | Delivery failed and will be retried                    |
| `DEAD_LETTERED`          | Delivery failed after max retries                      |
| `SKIPPED_BY_PREFERENCE`  | User preference disabled this channel                  |
| `SKIPPED_BY_QUIET_HOURS` | Status available for quiet-hours based skip/hold logic |

---

## API Endpoints

### User APIs

| Method | Endpoint          | Description    |
| ------ | ----------------- | -------------- |
| POST   | `/api/users`      | Create user    |
| GET    | `/api/users`      | Get all users  |
| GET    | `/api/users/{id}` | Get user by ID |
| PUT    | `/api/users/{id}` | Update user    |
| DELETE | `/api/users/{id}` | Delete user    |

Example create user request:

```json
{
  "name": "Khush",
  "email": "khush@example.com"
}
```

---

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

---

### Notification APIs

| Method | Endpoint                                  | Description                      |
| ------ | ----------------------------------------- | -------------------------------- |
| POST   | `/api/notifications`                      | Create notification              |
| GET    | `/api/notifications/recent`               | Get recent notifications         |
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

---

### Dead Letter APIs

| Method | Endpoint                              | Description                         |
| ------ | ------------------------------------- | ----------------------------------- |
| GET    | `/api/admin/dead-letters`             | Get all dead-lettered notifications |
| POST   | `/api/admin/dead-letters/{id}/replay` | Replay a dead-lettered notification |

---

## Notification Statistics

Stats API returns counts for dashboard monitoring.

Example response:

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

---

## Idempotency

NotifiQ currently uses DB-backed idempotency.

Each notification request must include a unique `idempotencyKey`.

Before creating a notification, the system checks whether a notification with the same key already exists. If it exists, the existing notification response is returned and no duplicate notification is created.

The `idempotencyKey` is also protected using a unique constraint in the database.

This protects the system from duplicate notification creation during client retries, repeated submissions, or accidental double-clicks.

Redis-based idempotency is not implemented yet and can be added as a future enhancement.

---

## Frontend Dashboard

A React + TypeScript dashboard is available for monitoring and managing NotifiQ.

Dashboard features:

* View notification statistics
* Inspect recent notifications
* Search notifications by user ID
* Send notifications manually
* Manage user preferences
* View and replay dead-lettered notifications

Frontend repository:

```text
https://github.com/Khushi340/notifiq-dashboard
```

Local frontend URL:

```text
http://localhost:5173
```

---

## Swagger Documentation

Swagger UI is available at:

```text
http://localhost:8080/swagger-ui/index.html
```

OpenAPI docs are available at:

```text
http://localhost:8080/v3/api-docs
```

---

## Local Setup

### 1. Clone the repository

```bash
git clone https://github.com/Khushi340/NotifiQ
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

Example properties:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/notifiq_db
spring.datasource.username=your_postgres_username
spring.datasource.password=your_postgres_password

spring.mail.host=sandbox.smtp.mailtrap.io
spring.mail.port=2525
spring.mail.username=your_mailtrap_username
spring.mail.password=your_mailtrap_password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

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

---

## Frontend Integration

The React dashboard runs locally on:

```text
http://localhost:5173
```

The frontend reads the backend URL using:

```env
VITE_API_BASE_URL=http://localhost:8080
```

The backend should be running before using the dashboard locally.

---

## Testing Tools

* Postman or Swagger UI for API testing
* Mailtrap for email testing
* Webhook.site for webhook testing
* PostgreSQL for database verification
* React dashboard for operational testing

---

## Example Webhook Testing Flow

1. Open `https://webhook.site`.
2. Copy the generated unique URL.
3. Update user preference with that URL.
4. Create a notification with `channel = WEBHOOK`.
5. Check webhook.site for received JSON payload.
6. Verify notification status becomes `SENT`.

---

## Example In-App Testing Flow

1. Create or use an existing user.
2. Create a notification with `channel = IN_APP`.
3. Open the React dashboard.
4. Go to the Notifications page.
5. Search using the user ID.
6. Verify the notification appears with `channel = IN_APP`.
7. Use the unread API to check unread in-app notifications.
8. Mark the notification as read using the read API.

---

## Demo Flow

1. Create a user.
2. Configure user preferences.
3. Send an Email, In-App, or Webhook notification.
4. Monitor status from the dashboard.
5. Disable a channel in preferences and verify `SKIPPED_BY_PREFERENCE`.
6. Use an invalid webhook URL to trigger retries.
7. Verify failed notification moves to Dead Letter Queue.
8. Replay the dead-lettered notification.

---

## Future Enhancements

* Deployment on Render
* Hosted PostgreSQL using Supabase
* JWT authentication
* Redis-based idempotency
* Kafka/RabbitMQ based event-driven processing
* Advanced delivery history/audit logs
* Docker support

---

## Related Repository

Frontend repository:

```text
https://github.com/Khushi340/notifiq-dashboard
```

---

## Summary

NotifiQ is a reliable multi-channel notification platform supporting Email, In-App, and Webhook delivery. It includes user preferences, quiet hours, async processing, retry with backoff, dead-letter queue, replay support, DB-backed idempotency, read/unread tracking, statistics, Swagger documentation, and a React dashboard for monitoring and operations.

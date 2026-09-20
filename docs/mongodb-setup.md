# MongoDB Atlas Setup & Database-per-Service Architecture

## Overview

Student Central follows a strict **database-per-service** microservice architectural pattern. Each business microservice is completely isolated at the data tier and connects to its own dedicated MongoDB database. No microservice ever directly accesses another microservice's database.

---

## Dedicated Databases

All databases reside on a single MongoDB Atlas cluster while maintaining complete logical and access isolation:

| Microservice | Port | Dedicated Database Name | Collections |
|---|---|---|---|
| **Auth Service** | 8081 | `student_central_auth` | `users` |
| **Student Service** | 8082 | `student_central_student` | `students` |
| **Admission Service** | 8083 | `student_central_admission` | `applications`, `documents` |
| **Course Service** | 8084 | `student_central_course` | `courses`, `prerequisites` |
| **Registration Service** | 8085 | `student_central_registration` | `registrations` |
| **Schedule Service** | 8086 | `student_central_schedule` | `schedules` |
| **Notification Service** | 8087 | `student_central_notification` | `notifications` |

---

## Environment Variables & Secrets Management

To guarantee security and follow the Twelve-Factor App methodology:
- **No credentials or connection URIs are hardcoded into Java code or committed to Git.**
- All configuration is read via environment variables defined in `.env`.
- `.env` is permanently excluded via `.gitignore`.
- Clean templates and placeholders are provided in `.env.example`.

### Atlas URI Configuration Hierarchy

Each microservice's `application.yml` uses a resilient fallback hierarchy:

```yaml
spring:
  data:
    mongodb:
      uri: ${MONGODB_<SERVICE>_URI:${MONGODB_URI:${MONGODB_BASE_URI:mongodb://localhost:27017}/<database_name>${MONGODB_URI_OPTIONS:}}}
```

### Setup Instructions

1. Copy `.env.example` to `.env`:
   ```bash
   cp .env.example .env
   ```

2. Configure your MongoDB Atlas Cluster credentials in `.env`:
   ```properties
   # Option A: Single MongoDB Atlas Cluster Base URI (Recommended)
   MONGODB_BASE_URI=mongodb+srv://<username>:<password>@<cluster-host>.mongodb.net
   MONGODB_URI_OPTIONS=?retryWrites=true&w=majority

   # Option B: Individual Service Database URIs
   MONGODB_AUTH_URI=mongodb+srv://<username>:<password>@<cluster-host>.mongodb.net/student_central_auth?retryWrites=true&w=majority
   MONGODB_STUDENT_URI=mongodb+srv://<username>:<password>@<cluster-host>.mongodb.net/student_central_student?retryWrites=true&w=majority
   MONGODB_ADMISSION_URI=mongodb+srv://<username>:<password>@<cluster-host>.mongodb.net/student_central_admission?retryWrites=true&w=majority
   MONGODB_COURSE_URI=mongodb+srv://<username>:<password>@<cluster-host>.mongodb.net/student_central_course?retryWrites=true&w=majority
   MONGODB_REGISTRATION_URI=mongodb+srv://<username>:<password>@<cluster-host>.mongodb.net/student_central_registration?retryWrites=true&w=majority
   MONGODB_SCHEDULE_URI=mongodb+srv://<username>:<password>@<cluster-host>.mongodb.net/student_central_schedule?retryWrites=true&w=majority
   MONGODB_NOTIFICATION_URI=mongodb+srv://<username>:<password>@<cluster-host>.mongodb.net/student_central_notification?retryWrites=true&w=majority
   ```

3. **MongoDB Atlas Network Access**:
   Ensure your IP address is whitelisted in **Network Access** on MongoDB Atlas (or set to `0.0.0.0/0` during development).

---

## Database Indexes

To guarantee high performance and data integrity, each service maintains essential indexes:

- **Auth Service**:
  - `email` (Unique index for identity lookup and duplicate rejection)
- **Student Service**:
  - `userId` (Unique index for user binding)
  - `studentId` (Unique index for campus identity)
- **Admission Service**:
  - `applicationId` (Unique index)
  - `studentId` (Indexed)
  - `status` (Indexed for administrative review filtering)
- **Course Service**:
  - `courseCode` (Unique index)
  - `department`, `semester`, `status` (Indexed for catalog search/filtering)
  - `prerequisites`: `courseId`, `prerequisiteCourseId`
- **Registration Service**:
  - `registrationId` (Unique index)
  - `studentId`, `userId`, `courseId` (Indexed for fast lookup and duplicate prevention)
- **Schedule Service**:
  - `courseId`, `courseCode`, `dayOfWeek`
  - Compound Indexes: `(courseId, dayOfWeek)`, `(classroom, dayOfWeek)`, `(faculty, dayOfWeek)` for conflict detection
- **Notification Service**:
  - `notificationId` (Unique index)
  - `userId`, `isRead`
  - Compound Indexes: `(userId, isRead)`, `(userId, createdAt)` for fast feed pagination

# Student Central

## Overview
Student Central is a centralized Smart Campus platform that manages the student academic lifecycle from registration and admission application to course enrollment and timetable management.

---

## Architecture

```
                      React Frontend (Vite + TS + Tailwind)
                                     │ (Port 5173)
                                     ▼
                       Spring Cloud API Gateway (Port 8080)
                                     │
                    ┌────────────────┴────────────────┐
                    │                                 │
            Service Discovery                 Security Filter
          (Eureka Server :8761)                 (JWT Validation)
                    │
   ┌────────────────┼────────────────┬────────────────┬────────────────┐
   │                │                │                │                │
   ▼                ▼                ▼                ▼                ▼
Auth Service    Student Service  Admission Service Course Service Registration Service ...
  (:8081)           (:8082)          (:8083)          (:8084)          (:8085)
   │                │                │                │                │
[MongoDB]        [MongoDB]        [MongoDB]        [MongoDB]        [MongoDB]
```

Communication Flow:
`React Frontend` → `Spring Cloud API Gateway` → `Eureka Service Discovery` → `Microservices` → `Dedicated MongoDB Instances`

---

## Services

- **Eureka Server (`eureka-server`)**: Service registry & dynamic service discovery.
- **API Gateway (`api-gateway`)**: Single entry point, routing, and development CORS management.
- **Auth Service (`auth-service`)**: User authentication and identity lifecycle.
- **Student Service (`student-service`)**: Student profile and academic record management.
- **Admission Service (`admission-service`)**: Admission application submission & document review workflow.
- **Course Service (`course-service`)**: Academic course catalog and prerequisite definitions.
- **Registration Service (`registration-service`)**: Course enrollment and multi-step validation pipeline.
- **Schedule Service (`schedule-service`)**: Timetable generation and conflict checking.
- **Notification Service (`notification-service`)**: In-app notifications and student alerts.

---

## Technology Stack

- **Backend Language**: Java 17 / 21
- **Framework**: Spring Boot 3.2.5
- **Cloud Infrastructure**: Spring Cloud 2023.0.1 (Eureka Server, Spring Cloud Gateway, Netflix Eureka Client)
- **Database**: MongoDB with Spring Data MongoDB (database-per-service isolation)
- **Build Tool**: Maven (Multi-module Parent Reactor)
- **Observability**: Spring Boot Actuator (`/actuator/health`)
- **Frontend**: Existing React + TypeScript + Tailwind CSS

---

## Port Table

| Microservice | Port | Database Name | Health Verification Endpoint |
|---|---|---|---|
| **Eureka Server** | `8761` | N/A | `http://localhost:8761` |
| **API Gateway** | `8080` | N/A | `http://localhost:8080/actuator/health` |
| **Auth Service** | `8081` | `student_central_auth` | `http://localhost:8080/api/auth/health` |
| **Student Service** | `8082` | `student_central_student` | `http://localhost:8080/api/students/health` |
| **Admission Service** | `8083` | `student_central_admission` | `http://localhost:8080/api/admissions/health` |
| **Course Service** | `8084` | `student_central_course` | `http://localhost:8080/api/courses/health` |
| **Registration Service** | `8085` | `student_central_registration` | `http://localhost:8080/api/registrations/health` |
| **Schedule Service** | `8086` | `student_central_schedule` | `http://localhost:8080/api/schedules/health` |
| **Notification Service** | `8087` | `student_central_notification` | `http://localhost:8080/api/notifications/health` |

---

## Local Setup

### 1. Start MongoDB
Ensure MongoDB is running locally on port 27017:
```bash
# Default connection string: mongodb://localhost:27017
```

### 2. Start Eureka Server
```bash
cd eureka-server
mvn spring-boot:run
```
*Access dashboard at: `http://localhost:8761`*

### 3. Start Business Microservices
Start each service in a separate terminal:
```bash
cd auth-service && mvn spring-boot:run
cd student-service && mvn spring-boot:run
cd admission-service && mvn spring-boot:run
cd course-service && mvn spring-boot:run
cd registration-service && mvn spring-boot:run
cd schedule-service && mvn spring-boot:run
cd notification-service && mvn spring-boot:run
```

### 4. Start API Gateway
```bash
cd api-gateway
mvn spring-boot:run
```

### 5. Start Frontend
```bash
cd frontend
npm run dev
```

---

---

## Phase 2 — Authentication & JWT Security Architecture

### Auth Microservice (`auth-service`)
- **Port**: `8081`
- **Database**: `student_central_auth` (Collection: `users`)
- **Authentication**: Stateless JWT authentication with Spring Security 6 & BCrypt password hashing.
- **Roles**: `STUDENT`, `ADMIN`

### Auth Endpoints

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `POST` | `/api/auth/register` | Public | Register a new user (defaults to `STUDENT` role) |
| `POST` | `/api/auth/login` | Public | Authenticate user & return signed JWT |
| `GET` | `/api/auth/me` | Authenticated | Retrieve current user's profile |
| `GET` | `/api/auth/validate` | Authenticated | Validate JWT token & extract claims |

### Gateway Security & Identity Propagation
- **Public Routes**: `/api/auth/register`, `/api/auth/login`, `/api/auth/health`, `/actuator/**`
- **Protected Routes**: All business service endpoints (`/api/students/**`, `/api/courses/**`, etc.)
- **Header Propagation**: Verified JWT tokens have their identity extracted and forwarded to downstream services via `X-User-Id` and `X-User-Role` headers.
- **Spoofing Protection**: Untrusted incoming `X-User-Id` / `X-User-Role` headers are stripped by the API Gateway before routing.

### Postman Test Suite
A complete Postman test collection is provided in:
`postman/Student_Central_Auth_Phase2.postman_collection.json`

---

## Phase 3 — Student Profile & Academic Record Management

### Student Microservice (`student-service`)
- **Port**: `8082`
- **Database**: `student_central_student` (Collection: `students`)
- **Authentication**: Stateless JWT security with role authorization (`STUDENT`, `ADMIN`)

### Student Endpoints

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `POST` | `/api/students/profile` | STUDENT | Create student profile for authenticated user |
| `GET` | `/api/students/profile` | STUDENT | Retrieve own student profile |
| `PUT` | `/api/students/profile` | STUDENT | Update own student profile |
| `GET` | `/api/students/profile/academic-record` | STUDENT | Retrieve own academic history |
| `PUT` | `/api/students/profile/academic-record` | STUDENT | Update own academic history |
| `GET` | `/api/students/{id}` | ADMIN / Owner | Get student by MongoDB ID, studentId, or userId |
| `GET` | `/api/students` | ADMIN | List all students with optional filters (`department`, `status`) |

### Phase 3 Postman Test Suite
A complete Postman test collection is provided in:
`postman/Student_Central_Phase3_Collection.postman_collection.json`

---

## Phase 4 — Admission Workflow & Application Management

### Admission Microservice (`admission-service`)
- **Port**: `8083`
- **Database**: `student_central_admission` (Collections: `applications`, `documents`)
- **Authentication**: Stateless JWT security with role authorization (`STUDENT`, `ADMIN`)

### Admission State Flow
```
DRAFT ──(submit)──> SUBMITTED ──(start review)──> UNDER_REVIEW ──(approve)──> APPROVED
                         │                              │
                         │                              ├──(reject)───> REJECTED
                         │                              │
                         │                              └──(request changes)──> CHANGES_REQUESTED
                         │                                                             │
                         └─────────────────────────────────────────────────────────────┘ (re-submit)
```

### Admission Endpoints

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `POST` | `/api/admissions/apply` | STUDENT | Create new admission application (starts in `DRAFT`) |
| `GET` | `/api/admissions/my-application` | STUDENT | Retrieve own admission application |
| `PUT` | `/api/admissions/{id}` | STUDENT | Update own application (when `DRAFT` or `CHANGES_REQUESTED`) |
| `POST` | `/api/admissions/{id}/submit` | STUDENT | Submit application (validates required documents) |
| `POST` | `/api/admissions/{id}/documents` | STUDENT | Upload document metadata (`ID_PROOF`, `MARKS_CERTIFICATE`, etc.) |
| `GET` | `/api/admissions/{id}/documents` | STUDENT / ADMIN | List documents attached to an application |
| `DELETE` | `/api/admissions/{id}/documents/{documentId}` | STUDENT | Delete document from draft / changes-requested application |
| `GET` | `/api/admissions` | ADMIN | List all applications with optional query filters (`department`, `status`) |
| `GET` | `/api/admissions/{id}` | ADMIN | Get full application details and document summaries |
| `PUT` | `/api/admissions/{id}/review` | ADMIN | Review application (`START_REVIEW`, `APPROVE`, `REJECT`, `REQUEST_CHANGES`) |
| `PUT` | `/api/admissions/{id}/documents/{documentId}/review` | ADMIN | Review individual document (`VERIFIED`, `REJECTED`, `UNDER_REVIEW`) |

### Phase 4 Postman Test Suite
A complete Postman test collection is provided in:
`postman/Student_Central_Phase4_Collection.postman_collection.json`

---

## Phase 5 — Course Catalogue & Academic Prerequisite Management

### Course Microservice (`course-service`)
- **Port**: `8084`
- **Database**: `student_central_course` (Collections: `courses`, `prerequisites`)
- **Authentication**: Stateless JWT security with role authorization (`STUDENT`, `ADMIN`)

### Capacity & Available Seat Business Rules
- `capacity` must be greater than zero.
- On creation: `availableSeats = capacity`.
- When updating capacity: Occupied seats = `capacity - availableSeats`. If `newCapacity < occupiedSeats`, update is rejected with `400 Bad Request` (`INVALID_CAPACITY`).
- When capacity increases, `availableSeats = newCapacity - occupiedSeats`.
- Deletion performs soft deactivation (`status = INACTIVE`).

### Course Endpoints

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `POST` | `/api/courses` | ADMIN | Create new course in catalogue (initial status `ACTIVE`, `availableSeats = capacity`) |
| `GET` | `/api/courses` | Authenticated | List courses with filters (`search`, `department`, `semester`, `courseType`, `status`, `available`). Students only receive `ACTIVE` courses. |
| `GET` | `/api/courses/{id}` | Authenticated | Get course details and prerequisite list by ID or course code |
| `PUT` | `/api/courses/{id}` | ADMIN | Update course details and capacity with occupied seat protection |
| `DELETE` | `/api/courses/{id}` | ADMIN | Deactivate / soft-delete course (`status = INACTIVE`) |
| `PUT` | `/api/courses/{id}/activate` | ADMIN | Explicitly activate course (`status = ACTIVE`) |
| `PUT` | `/api/courses/{id}/deactivate` | ADMIN | Explicitly deactivate course (`status = INACTIVE`) |
| `GET` | `/api/courses/{id}/availability` | Authenticated | Check seat capacity, available seats, and availability flag |
| `GET` | `/api/courses/{id}/prerequisites` | Authenticated | Get prerequisite courses list |
| `POST` | `/api/courses/{id}/prerequisites` | ADMIN | Add prerequisite course (validates self/duplicate/circular references) |
| `DELETE` | `/api/courses/{id}/prerequisites/{prereqId}` | ADMIN | Delete prerequisite relationship |

### Phase 5 Postman Test Suite
A complete Postman test collection is provided in:
`postman/Student_Central_Phase5_Collection.postman_collection.json`

---

## Phase 6 — Course Registration & Business Validation Pipeline

### Registration Microservice (`registration-service`)
- **Port**: `8085`
- **Database**: `student_central_registration` (Collection: `registrations`)
- **Authentication**: Stateless JWT security with role authorization (`STUDENT`, `ADMIN`)
- **Sequential Registration ID**: Collision-resistant format `SC-REG-2026-00001`
- **Configurable Semester Credit Limit**: Default 24 credits per semester

### 7-Step Business Validation Pipeline
When a student registers for a course (`POST /api/registrations`), the service validates:
1. **Student Existence**: Resolves profile from `StudentServiceClient`.
2. **Admission Status**: Student's admission must be `APPROVED`.
3. **Course Existence & Status**: Course must exist and have status `ACTIVE`.
4. **Duplicate Registration**: Checks if student already has a `REGISTERED` record for the course.
5. **Prerequisites Fulfillment**: Verifies student has completed all prerequisite courses.
6. **Credit Limit**: Total registered credits in target semester + new course credits <= 24.
7. **Seat Availability**: Verifies `availableSeats > 0`.
8. **Timetable Conflict**: Checks schedule overlap via pluggable `ScheduleValidationClient`.
9. **Atomic Seat Reservation**: Atomically decrements course seats with compensating rollback on persistence failure.

### Course Drop Lifecycle
- When a student drops a course (`DELETE /api/registrations/{registrationId}`), the registration status transitions to `DROPPED` and the seat is released back to `Course Service`.

### Registration Endpoints

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `POST` | `/api/registrations` | STUDENT | Register for course through validation pipeline |
| `GET` | `/api/registrations/my` | STUDENT | Retrieve own registrations |
| `GET` | `/api/registrations/my/active` | STUDENT | Retrieve active (`REGISTERED`) enrollments |
| `GET` | `/api/registrations/my/history` | STUDENT | Retrieve complete registration history |
| `GET` | `/api/registrations/{id}` | STUDENT / ADMIN | Get registration details |
| `DELETE` | `/api/registrations/{id}` | STUDENT | Drop course and release seat |
| `GET` | `/api/registrations` | ADMIN | Audit query with filters (`courseId`, `semester`, `status`) |
| `GET` | `/api/registrations/student/{studentId}` | ADMIN | Get registrations for specific student |

### Phase 6 Postman Test Suite
A complete Postman test collection is provided in:
`postman/Student_Central_Phase6_Collection.postman_collection.json`


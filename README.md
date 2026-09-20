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

## Local Setup & Configuration

### 1. Configure MongoDB Atlas & Environment
Student Central connects to MongoDB Atlas using database-per-service isolation.
Copy `.env.example` to `.env` and supply your Atlas credentials:
```bash
cp .env.example .env
```
For detailed setup instructions, see:
- [MongoDB Atlas Setup Guide](docs/mongodb-setup.md)
- [Postman API Testing Guide](docs/postman-testing.md)

### 2. Start Eureka Server
```bash
cd eureka-server
mvn spring-boot:run
```
*Access dashboard at: `http://localhost:8761`*

### 3. Start Business Microservices
Start each service in a separate terminal (all will connect to their respective MongoDB Atlas database):
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
*All client requests route through: `http://localhost:8080`*

### 5. Start Frontend
```bash
cd frontend
npm run dev
```
*Access the React application at: `http://localhost:5173`*

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

---

## Phase 7 — Timetable Management & Schedule Conflict Service

### Schedule Microservice (`schedule-service`)
- **Port**: `8086`
- **Database**: `student_central_schedule` (Collection: `schedules`)
- **Authentication**: Stateless JWT security with role authorization (`STUDENT`, `ADMIN`)
- **Compound & Single Indexes**: `courseId`, `dayOfWeek`, `semester`, `academicYear`, `classroom`, `faculty`

### Conflict Detection Rules
1. **Time Range Validity**: `startTime < endTime` (Rejects `startTime >= endTime` with `400 BAD REQUEST` / `INVALID_TIME_RANGE`).
2. **Course Schedule Overlap**: Prevents same course and section from overlapping on the same day (`409 CONFLICT` / `SCHEDULE_CONFLICT`).
3. **Classroom Conflict**: Prevents duplicate booking of the same classroom on the same day/time (`409 CONFLICT` / `CLASSROOM_CONFLICT`).
4. **Faculty Conflict**: Prevents assigning the same faculty member to multiple concurrent classes (`409 CONFLICT` / `FACULTY_SCHEDULE_CONFLICT`).
5. **Student Timetable Conflict**: Evaluates student's active enrollments against requested course slots via `POST /api/schedules/check-conflict` (`409 CONFLICT` / `SCHEDULE_CONFLICT`).

### Schedule Endpoints

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `POST` | `/api/schedules` | ADMIN | Create new timetable schedule slot |
| `GET` | `/api/schedules/{id}` | Authenticated | Retrieve timetable slot by ID |
| `PUT` | `/api/schedules/{id}` | ADMIN | Update timetable schedule slot |
| `DELETE` | `/api/schedules/{id}` | ADMIN | Delete timetable schedule slot |
| `GET` | `/api/schedules` | Authenticated | List / filter timetable entries (`dayOfWeek`, `courseId`, `semester`, `academicYear`, `classroom`, `faculty`) |
| `GET` | `/api/schedules/course/{courseId}` | Authenticated | Get weekly timetable for a specific course |
| `GET` | `/api/schedules/my` | STUDENT | Retrieve personalized timetable from active course registrations |
| `POST` | `/api/schedules/check-conflict` | Authenticated / Service | Service-to-service student timetable conflict check |

### Phase 7 Postman Test Suite
A complete Postman test collection is provided in:
`postman/Student_Central_Phase7_Collection.postman_collection.json`

---

## Phase 8: Notification Service (Port 8087)

The **Notification Service** provides decoupled, resilient, in-app event notifications and administrative announcements across the microservice ecosystem.

### Core Features
- **In-App Notification Feed**: Students can fetch paginated feeds of their notifications, filter unread alerts, and monitor unread counts with badge support.
- **Read & Retention Lifecycle**: Individual notification mark-as-read, bulk mark-all-read, and individual deletion with strict user ownership enforcement.
- **Admin Communications**: Direct targeted notifications to single users, and broadcast announcements to multiple students simultaneously.
- **Internal Service Integrations**: Best-effort REST client integration with:
  - **Admission Service**: Event alerts when applications are `APPROVED`, `REJECTED`, or `CHANGES_REQUESTED`.
  - **Registration Service**: Event alerts when courses are `REGISTERED` or `DROPPED`.
- **Fault Tolerance**: Microservice interactions are strictly best-effort — if the Notification Service is temporarily degraded, registration and admission flows never fail.
- **Data Model**: MongoDB document store with compound indexes (`userId` + `createdAt DESC`, `userId` + `isRead`) and unique `notificationId` (`NOTIF-<8-hex>`).

### Notification Endpoints

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `GET` | `/api/notifications/health` | Public | Service health probe |
| `GET` | `/api/notifications/my` | STUDENT | Get student's notifications (paginated, sorted by `createdAt DESC`) |
| `GET` | `/api/notifications/my/unread` | STUDENT | Get student's unread notifications |
| `GET` | `/api/notifications/my/unread-count` | STUDENT | Get total unread count for badge display |
| `PUT` | `/api/notifications/{id}/read` | STUDENT | Mark specific notification as read (ownership verified) |
| `PUT` | `/api/notifications/my/read-all` | STUDENT | Mark all student's notifications as read in bulk |
| `DELETE` | `/api/notifications/{id}` | STUDENT | Delete specific notification (ownership verified) |
| `POST` | `/api/notifications` | ADMIN | Create a direct notification for a specific user |
| `POST` | `/api/notifications/broadcast` | ADMIN | Broadcast notification to multiple users |
| `POST` | `/api/notifications/internal` | Service / Internal | Internal service-to-service notification creation |

### Phase 8 Postman Test Suite
A complete Postman test collection is provided in:
`postman/Student_Central_Phase8_Collection.postman_collection.json`

---

## Phase 9: Frontend & Microservices Integration

Phase 9 integrates the pre-existing **Google Stitch React Frontend** (`frontend/`) with the Spring Boot microservices ecosystem via the **Spring Cloud API Gateway** (`http://localhost:8080`).

### Architectural Highlights
- **Single Point of Ingress**: The frontend exclusively communicates with the API Gateway (`http://localhost:8080`). Direct browser calls to individual microservice ports (`8081`-`8087`) are strictly avoided.
- **Frontend Stack**: React 19, TypeScript, Vite, Tailwind CSS v4 (`@tailwindcss/vite`), React Router v7.
- **Visual Design Preservation**: Strict preservation of Google Stitch layouts, components, typography, color palettes, and glassmorphism styling.
- **Strong Typing**: Comprehensive TypeScript interfaces (`src/types/api.ts`) for all request/response DTOs across the 7 backend microservices.
- **Centralized API Client**: Axios HTTP client (`src/services/api/apiClient.ts`) with automated `Authorization: Bearer <JWT>` injection, centralized error extraction, and 401 unauthenticated session revocation.
- **Auth Context & Route Guards**: Session restoration via `GET /api/auth/me`, protected route wrapper (`ProtectedRoute.tsx`), and role-based access control separating Student and Admin portals.
- **Zero Mock Data**: Hardcoded student profiles, mock admissions, fake course registrations, static timetable blocks, and mock statistics replaced with real backend microservice APIs.

### Integrated Routes & Endpoints

| Route | View Component | Role | Backend Service & Endpoints |
|---|---|---|---|
| `/` | `LandingPage` | Public | — |
| `/login` | `LoginPage` | Public | Auth Service: `POST /api/auth/login` |
| `/register` | `RegisterPage` | Public | Auth Service: `POST /api/auth/register`, `POST /api/auth/login` |
| `/dashboard` | `StudentDashboard` | `STUDENT` | Parallel aggregation: Student, Admission, Registration, Schedule, Notification APIs |
| `/profile` | `StudentProfile` | `STUDENT` | Student Service: `GET /api/students/profile`, `PUT /api/students/profile` |
| `/admission/apply` | `AdmissionApplication` | `STUDENT` | Admission Service: `GET /api/admissions/my-application`, `POST /api/admissions/apply`, `POST /api/admissions/{id}/submit`, `POST /api/admissions/{id}/documents` |
| `/courses` | `ExploreCourses` | `STUDENT` | Course Service: `GET /api/courses` (filters: search, dept, sem); Registration Service: `POST /api/registrations` |
| `/my-courses` | `MyRegisteredCourses` | `STUDENT` | Registration Service: `GET /api/registrations/my/active`, `DELETE /api/registrations/{id}` |
| `/timetable` | `WeeklyTimetable` | `STUDENT` | Schedule Service: `GET /api/schedules/my` (dynamic 5-day time grid) |
| `/notifications` | `StudentNotifications` | `STUDENT` | Notification Service: `GET /api/notifications/my`, `PUT /api/notifications/{id}/read`, `PUT /api/notifications/my/read-all`, `DELETE /api/notifications/{id}` |
| `/admin/dashboard` | `AdminDashboard` | `ADMIN` | Microservices metric aggregation: Admissions, Courses, Registrations |
| `/admin/admissions` | `AdminAdmissionReview` | `ADMIN` | Admission Service: `GET /api/admissions`, `GET /api/admissions/{id}/documents`, `PUT /api/admissions/{id}/review` |
| `/admin/courses` | `AdminCourseManagement` | `ADMIN` | Course Service: `GET /api/courses`, `PUT /api/courses/{id}/activate`, `PUT /api/courses/{id}/deactivate` |
| `/admin/courses/:courseId`| `AdminCourseEditor` | `ADMIN` | Course Service: `GET /api/courses/{id}`, `POST /api/courses`, `PUT /api/courses/{id}`, `POST/DELETE /api/courses/{id}/prerequisites` |
| `/admin/schedules` | `AdminScheduleManagement` | `ADMIN` | Schedule Service: `GET /api/schedules`, `POST /api/schedules`, `DELETE /api/schedules/{id}` |
| `/admin/notifications` | `AdminNotifications` | `ADMIN` | Notification Service: `GET /api/notifications/admin`, `POST /api/notifications`, `POST /api/notifications/broadcast` |

### Environment Configuration
Copy `.env.example` to `.env` in `frontend/`:
```env
VITE_API_BASE_URL=http://localhost:8080
```





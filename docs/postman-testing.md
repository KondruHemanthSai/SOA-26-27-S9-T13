# Postman API Testing & Validation Guide

## Overview

The **Student Central API** collection covers all 7 business microservices through the Spring Cloud API Gateway (Port 8080).

Collection File:
[`postman/Student_Central_API.postman_collection.json`](../postman/Student_Central_API.postman_collection.json)

---

## Environment Variables

Import the collection into Postman. The collection defines the following variables:

| Variable | Default Value | Description |
|---|---|---|
| `baseUrl` | `http://localhost:8080` | Spring Cloud API Gateway URL |
| `studentToken` | *(auto-populated)* | JWT Bearer token after student login |
| `adminToken` | *(auto-populated)* | JWT Bearer token after admin login |
| `studentUserId` | *(auto-populated)* | User ID of registered student |
| `studentId` | *(auto-populated)* | Campus Student ID |
| `applicationId` | *(auto-populated)* | Admission Application ID |
| `courseId` | *(auto-populated)* | Academic Course MongoDB ID |
| `courseCode` | `CS101` | Course code under test |
| `registrationId`| *(auto-populated)* | Course Registration ID |
| `scheduleId` | *(auto-populated)* | Course Schedule ID |
| `notificationId`| *(auto-populated)* | Notification ID |

---

## Folder Breakdown

The collection is organized into 9 folders:

### 01 Authentication
- **Register Student User**: Creates new credentials (`POST /api/auth/register`).
- **Login Student**: Authenticates student and extracts `studentToken` into collection variables.
- **Login Admin**: Authenticates default administrator (`admin@studentcentral.com`) and extracts `adminToken`.
- **Get Current User (`/me`)**: Validates token and returns current user identity.
- **Validate Token**: Validates JWT validity (`GET /api/auth/validate`).
- **Invalid Credentials**: Asserts HTTP 401 Unauthorized for incorrect password.
- **Duplicate Registration**: Asserts HTTP 400/409 rejection when re-registering existing email.

### 02 Student
- **Create Student Profile**: Initializes student profile with contact and program information.
- **Get Student Profile**: Retrieves current profile using Bearer `studentToken`.
- **Update Student Profile**: Edits phone and address details.
- **Get Academic Record**: Fetches GPA and completed credits.
- **Update Academic Record**: Admin updates academic standing.

### 03 Admission
- **Submit Admission Application**: Submits academic history and program application.
- **Get My Application**: Fetches current student's application.
- **Update Application**: Updates SOP and academic details while in DRAFT status.
- **Upload Verification Document Metadata**: Adds transcripts/certificates.
- **Submit Application for Review**: Transitions status to `SUBMITTED`.

### 04 Courses
- **Create Course (CS101)**: Admin creates course with credits and seat capacity.
- **Get All Courses**: Students browse course catalog.
- **Get Course by ID**: Retrieves detailed syllabus and prerequisites.
- **Update Course**: Admin updates course metadata.
- **Activate / Deactivate Course**: Toggles course enrollment availability.
- **Search and Filter Courses**: Query courses by keyword and department.
- **Manage Prerequisites**: Links prerequisite course requirements.
- **Check Seat Availability**: Verifies live remaining seat count.

### 05 Registration
- **Register Course (CS101)**: Validates eligibility, checks seats, decrements seat count, creates registration.
- **View Registered Courses**: Lists student's currently active enrollments.
- **View Registration History**: Complete enrollment audit trail.
- **Duplicate Registration Rejected**: Asserts business rule preventing enrolling in the same course twice.
- **Inactive Course Rejected**: Asserts business rule preventing enrolling in deactivated courses.
- **Drop Course**: Releases reserved seat back to course pool, updates registration status, triggers notification.

### 06 Schedule
- **Create Schedule Section**: Admin creates weekly lecture time slots.
- **Timetable Conflict Check (Overlap)**: Asserts HTTP 409 Conflict when section overlaps existing classroom time (e.g. 10:00-11:00 vs 10:30-11:30).
- **Timetable Boundary Check (Adjacent)**: Asserts HTTP 200/201 success for adjacent non-overlapping times (10:00-11:00 vs 11:00-12:00).
- **Get Course Schedule**: Lists all sections for a course code.
- **Get Student Timetable**: Compiles student's personalized weekly schedule.
- **Delete Schedule**: Removes timetable section.

### 07 Notifications
- **Create Notification**: In-app alert dispatch.
- **Get Student Notifications**: Paginated feed sorted by date.
- **Get Unread Notifications**: Filtered unread list.
- **Get Unread Count**: Counter for top navigation bell icon.
- **Mark Notification As Read**: Updates read timestamp.
- **Mark All Read**: Bulk mark read.
- **Admin Broadcast Announcement**: Sends broadcast announcement to students.
- **Delete Notification**: Deletes notification document.

### 08 Admin
- **Admin View All Applications**: Admin oversight across all applicants.
- **Admin Filter Applications by Status**: Filter by `SUBMITTED`, `UNDER_REVIEW`, `APPROVED`.
- **Admin Review Application (APPROVE)**: Approves application and triggers admission approval notification.
- **Admin View Student by ID**: Detailed administrative inspection.
- **Admin Notification Audit**: Administrative notification delivery logs.

### 09 End-to-End
- Full lifecycle flow executing sequentially:
  1. Authenticate Student
  2. Verify Admission Approved
  3. Student Check Admission Notification
  4. Browse Active Catalog
  5. Register For Academic Course
  6. Fetch Weekly Timetable
  7. Verify Registration Alert Notification
  8. Drop Course & Release Seat

---

## Test Execution via Newman CLI

To run the complete collection from the terminal:

```bash
npx newman run postman/Student_Central_API.postman_collection.json \
  --global-var "baseUrl=http://localhost:8080" \
  --reporters cli
```

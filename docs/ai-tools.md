# AI Agent Tools Specification

The AI Service incorporates an Agent Tool Router (`AgentToolRouter`) that determines when and how to call deterministic campus tools to serve student inquiries.

## Catalog of 8 Domain Tools

| Tool Name | Class | Target Microservice | Description |
|-----------|-------|---------------------|-------------|
| `get_student_profile` | `StudentProfileTool` | `student-service` | Retrieves student demographic, semester, major, and admission status. |
| `get_admission_status` | `AdmissionStatusTool` | `admission-service` | Retrieves current admission application stage, verification status, and decision notes. |
| `get_registered_courses` | `RegisteredCoursesTool` | `registration-service` | Retrieves list of actively enrolled courses, section IDs, and credit totals. |
| `get_available_courses` | `AvailableCoursesTool` | `course-service` | Retrieves active catalogue of courses, available seats, and departments. |
| `get_course_details` | `CourseDetailsTool` | `course-service` | Retrieves in-depth syllabus, prerequisites, credit hours, and instructor info for a course. |
| `check_course_prerequisites` | `CoursePrerequisitesTool` | `course-service` | Verifies whether the student has completed necessary prerequisites for an intended course. |
| `get_student_schedule` | `StudentScheduleTool` | `schedule-service` | Retrieves weekly class timetable, day-by-day room assignments, and conflict analysis. |
| `get_unread_notifications` | `UnreadNotificationsTool` | `notification-service` | Retrieves unread campus announcements, registration alerts, and fee notices. |

## Tool Invocation Flow
1. **Student Prompt Intake**: Sanitized against prompt injection attacks (neutralizing system jailbreaks, "ignore previous instructions", role manipulation).
2. **Intent & Keyword Matching / LLM Function Calling**: Evaluates user query to select appropriate tool(s).
3. **Internal REST Delegation**: Forwarding JWT Authorization header via Eureka `lb://<service-name>`.
4. **Context Injection & Synthesis**: Aggregates verified tool outputs into prompt context for final grounded response.

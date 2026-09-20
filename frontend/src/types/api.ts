/**
 * TypeScript API type definitions for Student Central microservices.
 */

export type Role = 'STUDENT' | 'ADMIN';

export interface User {
  id: string;
  name: string;
  email: string;
  role: Role;
  active?: boolean;
  createdAt?: string;
  firstName?: string;
  lastName?: string;
}

export interface AuthResponse {
  token: string;
  userId: string;
  name: string;
  email: string;
  role: Role;
  expiresIn: number;
}

export interface UserResponse {
  id: string;
  name: string;
  email: string;
  role: Role;
  active: boolean;
  createdAt: string;
}

export interface TokenValidationResponse {
  valid: boolean;
  userId?: string;
  email?: string;
  role?: Role;
  message?: string;
}

// Student Profile Types
export type AdmissionStatus =
  | 'APPLIED'
  | 'UNDER_REVIEW'
  | 'APPROVED'
  | 'REJECTED'
  | 'CHANGES_REQUESTED'
  | 'DOCUMENTS_PENDING';

export interface AcademicRecord {
  tenthSchool?: string;
  tenthPercentage?: number;
  tenthBoard?: string;
  twelfthCollege?: string;
  twelfthPercentage?: number;
  twelfthBoard?: string;
  twelfthStream?: string;
  cgpa?: number;
  currentSemester?: number;
}

export interface StudentProfile {
  id: string;
  userId: string;
  studentId: string;
  firstName: string;
  lastName: string;
  email: string;
  phone?: string;
  dateOfBirth?: string;
  address?: string;
  program?: string;
  department?: string;
  enrollmentYear?: number;
  semester?: number;
  admissionStatus?: AdmissionStatus;
  profileCompleted: boolean;
  academicRecord?: AcademicRecord;
  createdAt?: string;
  updatedAt?: string;
}

export interface CreateStudentRequest {
  firstName: string;
  lastName: string;
  phone?: string;
  dateOfBirth?: string;
  address?: string;
  program: string;
  department: string;
}

export interface UpdateStudentRequest {
  firstName?: string;
  lastName?: string;
  phone?: string;
  dateOfBirth?: string;
  address?: string;
  program?: string;
  department?: string;
  profileCompleted?: boolean;
}

// Admission Types
export type ApplicationStatus =
  | 'DRAFT'
  | 'SUBMITTED'
  | 'UNDER_REVIEW'
  | 'APPROVED'
  | 'REJECTED'
  | 'CHANGES_REQUESTED';

export type DocumentType =
  | 'ID_PROOF'
  | 'MARKS_CERTIFICATE'
  | 'TRANSFER_CERTIFICATE'
  | 'PASSPORT_PHOTO'
  | 'OTHER';

export type DocumentVerificationStatus = 'PENDING' | 'VERIFIED' | 'REJECTED';

export interface AdmissionDocument {
  id: string;
  applicationId: string;
  type: DocumentType;
  fileName: string;
  fileUrl?: string;
  status: DocumentVerificationStatus;
  verifiedBy?: string;
  verifiedAt?: string;
  rejectionReason?: string;
  createdAt?: string;
}

export interface AcademicDetails {
  previousInstitution?: string;
  board?: string;
  percentage?: number;
  cgpa?: number;
  graduationYear?: number;
}

export interface AdmissionApplication {
  id: string;
  applicationId: string;
  userId: string;
  studentId?: string;
  studentName?: string;
  studentEmail?: string;
  program: string;
  department: string;
  status: ApplicationStatus;
  academicDetails?: AcademicDetails;
  documents?: AdmissionDocument[];
  reviewedBy?: string;
  reviewedAt?: string;
  reviewerRemarks?: string;
  submittedAt?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface CreateApplicationRequest {
  program: string;
  department: string;
  academicDetails?: AcademicDetails;
}

export interface UpdateApplicationRequest {
  program?: string;
  department?: string;
  academicDetails?: AcademicDetails;
}

export interface UploadDocumentRequest {
  type: DocumentType;
  fileName: string;
  fileUrl?: string;
}

export type ReviewAction = 'START_REVIEW' | 'APPROVE' | 'REJECT' | 'REQUEST_CHANGES';

export interface ApplicationReviewRequest {
  action: ReviewAction;
  remarks?: string;
}

// Course Types
export type CourseType = 'THEORY' | 'LAB' | 'SEMINAR' | 'PROJECT';
export type CourseStatus = 'ACTIVE' | 'INACTIVE';

export interface Course {
  id: string;
  courseCode: string;
  courseName: string;
  description?: string;
  department: string;
  semester: number;
  credits: number;
  capacity: number;
  enrolledCount?: number;
  availableSeats?: number;
  courseType: CourseType;
  faculty?: string;
  status: CourseStatus;
  prerequisites?: string[];
  createdAt?: string;
  updatedAt?: string;
}

export interface CourseAvailability {
  courseId: string;
  courseCode: string;
  capacity: number;
  enrolledCount: number;
  availableSeats: number;
  full: boolean;
}

export interface PrerequisiteItem {
  id: string;
  courseId: string;
  prerequisiteCourseId: string;
  prerequisiteCourseCode?: string;
  prerequisiteCourseName?: string;
}

export interface PrerequisiteResponse {
  courseId: string;
  courseCode: string;
  prerequisites: PrerequisiteItem[];
}

export interface CreateCourseRequest {
  courseCode: string;
  courseName: string;
  description?: string;
  department: string;
  semester: number;
  credits: number;
  capacity: number;
  courseType: CourseType;
  faculty?: string;
}

export interface UpdateCourseRequest {
  courseName?: string;
  description?: string;
  department?: string;
  semester?: number;
  credits?: number;
  capacity?: number;
  courseType?: CourseType;
  faculty?: string;
}

// Registration Types
export type RegistrationStatus = 'REGISTERED' | 'DROPPED' | 'WAITLISTED';

export interface Registration {
  id: string;
  registrationId: string;
  studentId: string;
  courseId: string;
  courseCode: string;
  courseName: string;
  semester: number;
  academicYear: string;
  credits: number;
  status: RegistrationStatus;
  registeredAt: string;
  updatedAt?: string;
}

export interface RegistrationListResponse {
  studentId: string;
  registrations: Registration[];
}

export interface CreateRegistrationRequest {
  courseId: string;
  semester?: number;
  academicYear?: string;
}

export interface RegistrationSuccessResponse {
  registrationId: string;
  courseId: string;
  courseCode: string;
  courseName: string;
  credits: number;
  status: RegistrationStatus;
  message: string;
}

// Schedule Types
export type DayOfWeek = 'MONDAY' | 'TUESDAY' | 'WEDNESDAY' | 'THURSDAY' | 'FRIDAY' | 'SATURDAY' | 'SUNDAY';
export type ScheduleType = 'LECTURE' | 'LAB' | 'TUTORIAL';

export interface ScheduleItem {
  id: string;
  courseId: string;
  courseCode: string;
  courseName: string;
  section?: string;
  dayOfWeek: DayOfWeek;
  startTime: string; // HH:mm
  endTime: string;   // HH:mm
  classroom: string;
  building?: string;
  faculty?: string;
  semester?: number;
  academicYear?: string;
  scheduleType?: ScheduleType;
  createdAt?: string;
  updatedAt?: string;
}

export interface CreateScheduleRequest {
  courseId: string;
  section?: string;
  dayOfWeek: DayOfWeek;
  startTime: string;
  endTime: string;
  classroom: string;
  building?: string;
  faculty?: string;
  semester?: number;
  academicYear?: string;
  scheduleType?: ScheduleType;
}

// Notification Types
export type NotificationType =
  | 'ADMISSION'
  | 'REGISTRATION'
  | 'SCHEDULE'
  | 'SYSTEM'
  | 'GENERAL'
  | 'ACADEMIC';

export type NotificationPriority = 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT';

export interface NotificationItem {
  id: string;
  notificationId: string;
  userId: string;
  title: string;
  message: string;
  type: NotificationType;
  priority: NotificationPriority;
  isRead: boolean;
  actionUrl?: string;
  metadata?: Record<string, string>;
  createdAt: string;
  readAt?: string;
}

export interface NotificationPageResponse {
  content: NotificationItem[];
  pageNumber: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
  first: boolean;
  unreadCount?: number;
}

export interface UnreadCountResponse {
  unreadCount: number;
}

export interface CreateNotificationRequest {
  userId: string;
  title: string;
  message: string;
  type: NotificationType;
  priority: NotificationPriority;
  actionUrl?: string;
}

export interface BroadcastNotificationRequest {
  userIds: string[];
  title: string;
  message: string;
  type: NotificationType;
  priority: NotificationPriority;
  actionUrl?: string;
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
}

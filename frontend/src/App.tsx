import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import LandingPage from './pages/LandingPage';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';

// Student Pages
import StudentDashboard from './pages/student/StudentDashboard';
import StudentProfile from './pages/student/StudentProfile';
import AdmissionApplication from './pages/student/AdmissionApplication';
import ExploreCourses from './pages/student/ExploreCourses';
import MyRegisteredCourses from './pages/student/MyRegisteredCourses';
import WeeklyTimetable from './pages/student/WeeklyTimetable';
import RegistrationSuccess from './pages/student/RegistrationSuccess';
import StudentNotifications from './pages/student/StudentNotifications';
import StudentAIAssistant from './pages/student/StudentAIAssistant';

// Admin Pages
import AdminDashboard from './pages/admin/AdminDashboard';
import AdminAdmissionReview from './pages/admin/AdminAdmissionReview';
import AdminCourseManagement from './pages/admin/AdminCourseManagement';
import AdminCourseEditor from './pages/admin/AdminCourseEditor';
import AdminScheduleManagement from './pages/admin/AdminScheduleManagement';
import AdminNotifications from './pages/admin/AdminNotifications';

// Auth and Route Guards
import { AuthProvider } from './context/AuthContext';
import ProtectedRoute from './components/common/ProtectedRoute';

function App() {
  return (
    <AuthProvider>
      <Router>
        <Routes>
          {/* Public Routes */}
          <Route path="/" element={<LandingPage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />

          {/* Student Protected Routes */}
          <Route
            path="/dashboard"
            element={
              <ProtectedRoute allowedRoles={['STUDENT']}>
                <StudentDashboard />
              </ProtectedRoute>
            }
          />
          <Route
            path="/profile"
            element={
              <ProtectedRoute allowedRoles={['STUDENT']}>
                <StudentProfile />
              </ProtectedRoute>
            }
          />
          <Route path="/admission" element={<Navigate to="/admission/apply" replace />} />
          <Route
            path="/admission/apply"
            element={
              <ProtectedRoute allowedRoles={['STUDENT']}>
                <AdmissionApplication />
              </ProtectedRoute>
            }
          />
          <Route
            path="/courses"
            element={
              <ProtectedRoute allowedRoles={['STUDENT']}>
                <ExploreCourses />
              </ProtectedRoute>
            }
          />
          <Route path="/registration" element={<Navigate to="/my-courses" replace />} />
          <Route
            path="/my-courses"
            element={
              <ProtectedRoute allowedRoles={['STUDENT']}>
                <MyRegisteredCourses />
              </ProtectedRoute>
            }
          />
          <Route path="/schedule" element={<Navigate to="/timetable" replace />} />
          <Route
            path="/timetable"
            element={
              <ProtectedRoute allowedRoles={['STUDENT']}>
                <WeeklyTimetable />
              </ProtectedRoute>
            }
          />
          <Route
            path="/notifications"
            element={
              <ProtectedRoute allowedRoles={['STUDENT']}>
                <StudentNotifications />
              </ProtectedRoute>
            }
          />
          <Route
            path="/student/ai-assistant"
            element={
              <ProtectedRoute allowedRoles={['STUDENT']}>
                <StudentAIAssistant />
              </ProtectedRoute>
            }
          />
          <Route path="/ai-assistant" element={<Navigate to="/student/ai-assistant" replace />} />
          <Route path="/ai" element={<Navigate to="/student/ai-assistant" replace />} />
          <Route
            path="/registration-success"
            element={
              <ProtectedRoute allowedRoles={['STUDENT']}>
                <RegistrationSuccess />
              </ProtectedRoute>
            }
          />

          {/* Admin Protected Routes */}
          <Route path="/admin" element={<Navigate to="/admin/dashboard" replace />} />
          <Route
            path="/admin/dashboard"
            element={
              <ProtectedRoute allowedRoles={['ADMIN']}>
                <AdminDashboard />
              </ProtectedRoute>
            }
          />
          <Route
            path="/admin/admissions"
            element={
              <ProtectedRoute allowedRoles={['ADMIN']}>
                <AdminAdmissionReview />
              </ProtectedRoute>
            }
          />
          <Route
            path="/admin/courses"
            element={
              <ProtectedRoute allowedRoles={['ADMIN']}>
                <AdminCourseManagement />
              </ProtectedRoute>
            }
          />
          <Route
            path="/admin/courses/:courseId"
            element={
              <ProtectedRoute allowedRoles={['ADMIN']}>
                <AdminCourseEditor />
              </ProtectedRoute>
            }
          />
          <Route
            path="/admin/schedules"
            element={
              <ProtectedRoute allowedRoles={['ADMIN']}>
                <AdminScheduleManagement />
              </ProtectedRoute>
            }
          />
          <Route
            path="/admin/notifications"
            element={
              <ProtectedRoute allowedRoles={['ADMIN']}>
                <AdminNotifications />
              </ProtectedRoute>
            }
          />

          {/* Fallback */}
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </Router>
    </AuthProvider>
  );
}

export default App;

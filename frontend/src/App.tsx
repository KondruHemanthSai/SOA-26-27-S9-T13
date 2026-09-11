import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import LandingPage from './pages/LandingPage';
import StudentDashboard from './pages/student/StudentDashboard';
import AdmissionApplication from './pages/student/AdmissionApplication';
import ExploreCourses from './pages/student/ExploreCourses';
import MyRegisteredCourses from './pages/student/MyRegisteredCourses';
import WeeklyTimetable from './pages/student/WeeklyTimetable';
import RegistrationSuccess from './pages/student/RegistrationSuccess';
import AdminDashboard from './pages/admin/AdminDashboard';
import AdminAdmissionReview from './pages/admin/AdminAdmissionReview';
import AdminCourseManagement from './pages/admin/AdminCourseManagement';
import AdminCourseEditor from './pages/admin/AdminCourseEditor';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import { AuthProvider } from './context/AuthContext';

function App() {
  return (
    <AuthProvider>
      <Router>
        <Routes>
          {/* Public Routes */}
          <Route path="/" element={<LandingPage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />

          {/* Student Routes */}
          <Route path="/dashboard" element={<StudentDashboard />} />
          <Route path="/admission/apply" element={<AdmissionApplication />} />
          <Route path="/courses" element={<ExploreCourses />} />
          <Route path="/my-courses" element={<MyRegisteredCourses />} />
          <Route path="/timetable" element={<WeeklyTimetable />} />
          <Route path="/registration-success" element={<RegistrationSuccess />} />

          {/* Admin Routes */}
          <Route path="/admin/dashboard" element={<AdminDashboard />} />
          <Route path="/admin/admissions" element={<AdminAdmissionReview />} />
          <Route path="/admin/courses" element={<AdminCourseManagement />} />
          <Route path="/admin/courses/:courseId" element={<AdminCourseEditor />} />

          {/* Fallback */}
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </Router>
    </AuthProvider>
  );
}

export default App;

import { useState, useEffect } from 'react';
import StudentSidebar from '../../components/layout/StudentSidebar';
import TopNav from '../../components/layout/TopNav';
import { courseApi, registrationApi, aiApi, getErrorMessage } from '../../services/api';
import type { CourseRecommendation } from '../../services/api';
import type { Course } from '../../types/api';

export default function ExploreCourses() {
  const [courses, setCourses] = useState<Course[]>([]);
  const [recommendations, setRecommendations] = useState<CourseRecommendation[]>([]);
  const [search, setSearch] = useState('');
  const [department, setDepartment] = useState('');
  const [semester, setSemester] = useState<number | undefined>(undefined);
  const [loading, setLoading] = useState(true);
  const [registeringCourseId, setRegisteringCourseId] = useState<string | null>(null);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const fetchCourses = async () => {
    try {
      setLoading(true);
      setError('');
      const data = await courseApi.getCourses({
        search: search.trim() || undefined,
        department: department || undefined,
        semester: semester || undefined,
      });
      setCourses(data || []);
    } catch (err) {
      setError(getErrorMessage(err, 'Failed to fetch course catalogue.'));
    } finally {
      setLoading(false);
    }
  };

  const fetchRecommendations = async () => {
    try {
      const recs = await aiApi.getCourseRecommendations();
      setRecommendations(recs || []);
    } catch {
      setRecommendations([]);
    }
  };

  useEffect(() => {
    fetchCourses();
    fetchRecommendations();
  }, [department, semester]);

  const handleSearchSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    fetchCourses();
  };

  const handleRegister = async (course: Course) => {
    try {
      setRegisteringCourseId(course.id);
      setError('');
      setSuccess('');
      const res = await registrationApi.registerCourse({
        courseId: course.id,
        semester: course.semester,
        academicYear: '2024-2025',
      });
      setSuccess(res.message || `Successfully registered for ${course.courseCode}: ${course.courseName}!`);
      setTimeout(() => setSuccess(''), 4000);
      // Refresh course list to update seats
      fetchCourses();
    } catch (err) {
      setError(getErrorMessage(err, 'Course registration failed.'));
    } finally {
      setRegisteringCourseId(null);
    }
  };

  return (
    <div className="flex h-screen bg-background text-on-surface">
      <StudentSidebar />
      <main className="ml-64 flex-1 flex flex-col min-h-screen relative overflow-y-auto">
        <TopNav searchPlaceholder="Search courses, codes..." />
        <div className="p-[24px] flex-1 flex flex-col gap-[32px] max-w-[1280px] mx-auto w-full">
          {/* Header */}
          <div className="flex flex-col sm:flex-row sm:items-end justify-between gap-4">
            <div>
              <h1 className="text-display-lg font-display-lg text-on-surface mb-2">Explore Courses</h1>
              <p className="text-body-lg font-body-lg text-on-surface-variant">Fall 2024 Semester Registration</p>
            </div>

            {/* Filter controls */}
            <form onSubmit={handleSearchSubmit} className="flex flex-wrap items-center gap-2">
              <div className="relative">
                <input
                  type="text"
                  placeholder="Search code or name..."
                  value={search}
                  onChange={(e) => setSearch(e.target.value)}
                  className="bg-surface/60 border border-outline-variant/40 rounded-lg px-3 py-2 text-sm text-on-surface placeholder:text-on-surface-variant/50 focus:ring-1 focus:ring-primary outline-none"
                />
              </div>

              <select
                value={department}
                onChange={(e) => setDepartment(e.target.value)}
                className="px-3 py-2 rounded-lg bg-surface/60 border border-outline-variant/40 text-on-surface text-sm outline-none"
              >
                <option value="">All Departments</option>
                <option value="Computer Science">Computer Science</option>
                <option value="Mathematics">Mathematics</option>
                <option value="Physics">Physics</option>
                <option value="Humanities">Humanities</option>
              </select>

              <select
                value={semester || ''}
                onChange={(e) => setSemester(e.target.value ? parseInt(e.target.value) : undefined)}
                className="px-3 py-2 rounded-lg bg-surface/60 border border-outline-variant/40 text-on-surface text-sm outline-none"
              >
                <option value="">All Semesters</option>
                <option value="1">Sem 1</option>
                <option value="2">Sem 2</option>
                <option value="3">Sem 3</option>
                <option value="4">Sem 4</option>
                <option value="5">Sem 5</option>
              </select>

              <button
                type="submit"
                className="px-4 py-2 rounded-lg bg-primary text-on-primary font-label-md text-sm hover:bg-primary/90 transition-colors"
              >
                Filter
              </button>
            </form>
          </div>

          {/* Feedback alerts */}
          {error && (
            <div className="p-4 bg-error-container/30 border border-error/20 text-on-error-container rounded-xl font-body-sm text-body-sm flex items-center gap-3">
              <span className="material-symbols-outlined text-error">error</span>
              {error}
            </div>
          )}

          {success && (
            <div className="p-4 bg-emerald-50 border border-emerald-200 text-emerald-800 rounded-xl font-body-sm text-body-sm flex items-center gap-3">
              <span className="material-symbols-outlined text-emerald-600" style={{ fontVariationSettings: "'FILL' 1" }}>
                check_circle
              </span>
              {success}
            </div>
          )}

          {/* AI Personalized Course Pathfinder (Novelty #1) */}
          {recommendations.length > 0 && (
            <div className="bg-gradient-to-r from-indigo-900/10 via-indigo-600/5 to-transparent border border-indigo-200/60 rounded-2xl p-6">
              <div className="flex items-center justify-between mb-4">
                <div className="flex items-center gap-2.5">
                  <span className="w-8 h-8 rounded-lg bg-indigo-600 text-white flex items-center justify-center text-xs font-bold shadow-sm">AI</span>
                  <div>
                    <h2 className="text-base font-bold text-slate-900">Personalized Course Pathfinder</h2>
                    <p className="text-xs text-slate-500">AI recommendations based on your semester, prerequisites, and timetable compatibility</p>
                  </div>
                </div>
                <span className="text-xs font-semibold px-2.5 py-1 bg-indigo-100 text-indigo-700 rounded-full">Top Recommendations</span>
              </div>
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                {recommendations.map((rec) => {
                  const matchingCourse = courses.find((c) => c.courseCode === rec.courseCode);
                  return (
                    <div key={rec.courseCode} className="bg-white rounded-xl border border-indigo-100 p-4 shadow-sm hover:shadow-md transition flex flex-col justify-between">
                      <div>
                        <div className="flex items-center justify-between gap-2 mb-2">
                          <span className="px-2.5 py-0.5 rounded-md bg-indigo-50 text-indigo-700 font-mono font-bold text-xs">
                            {rec.courseCode}
                          </span>
                          <span className="text-[11px] font-semibold text-emerald-600 bg-emerald-50 px-2 py-0.5 rounded">
                            {Math.round(rec.confidence * 100)}% Fit
                          </span>
                        </div>
                        <h3 className="text-sm font-bold text-slate-900 line-clamp-1 mb-1">{rec.courseName}</h3>
                        <p className="text-xs text-slate-500 mb-3">{rec.department} • {rec.credits} Credits • {rec.availableSeats} Seats</p>
                        <div className="p-2.5 bg-slate-50 rounded-lg text-xs text-slate-600 mb-4 leading-relaxed border border-slate-100">
                          <span className="font-semibold text-indigo-900">Why recommended:</span> {rec.reason}
                        </div>
                      </div>
                      {matchingCourse && (
                        <button
                          onClick={() => handleRegister(matchingCourse)}
                          disabled={registeringCourseId === matchingCourse.id || matchingCourse.status !== 'ACTIVE' || (rec.availableSeats <= 0)}
                          className="w-full py-2 px-3 rounded-lg bg-indigo-600 hover:bg-indigo-700 text-white text-xs font-semibold transition disabled:opacity-50 flex items-center justify-center gap-1 shadow-sm"
                        >
                          {registeringCourseId === matchingCourse.id ? 'Registering...' : 'Register for Course'}
                        </button>
                      )}
                    </div>
                  );
                })}
              </div>
            </div>
          )}

          {/* Course Grid */}
          {loading ? (
            <div className="glass-card rounded-xl p-16 flex flex-col items-center justify-center">
              <div className="w-10 h-10 border-4 border-primary/20 border-t-primary rounded-full animate-spin"></div>
              <p className="mt-4 text-on-surface-variant font-body-md">Loading course catalogue...</p>
            </div>
          ) : courses.length === 0 ? (
            <div className="glass-card rounded-xl p-16 text-center text-on-surface-variant flex flex-col items-center justify-center">
              <span className="material-symbols-outlined text-[48px] opacity-40 mb-2">library_books</span>
              <p className="text-on-surface font-headline-md text-lg font-semibold">No courses found</p>
              <p className="text-sm mt-1">Try adjusting your search criteria or department filter.</p>
            </div>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-[24px]">
              {courses.map((course) => {
                const availableSeats =
                  course.availableSeats !== undefined
                    ? course.availableSeats
                    : course.capacity - (course.enrolledCount || 0);
                const isFull = availableSeats <= 0;
                const isRegistering = registeringCourseId === course.id;

                return (
                  <div
                    key={course.id || course.courseCode}
                    className="glass-panel bg-surface/40 rounded-xl p-[16px] flex flex-col gap-[8px] hover:shadow-lg transition-shadow"
                  >
                    <div className="flex justify-between items-start">
                      <span className="bg-primary-container/30 text-primary font-label-sm text-label-sm px-2.5 py-1 rounded-md font-semibold">
                        {course.courseCode}
                      </span>
                      <span className="text-xs px-2 py-0.5 rounded bg-surface-container font-label-sm text-on-surface-variant">
                        {course.credits} Credits
                      </span>
                    </div>

                    <h3 className="font-headline-md text-[20px] text-on-surface leading-tight mt-2">
                      {course.courseName}
                    </h3>
                    <p className="font-body-sm text-body-sm text-on-surface-variant line-clamp-2 mt-1">
                      {course.description || `${course.department} - Semester ${course.semester}`}
                    </p>

                    <div className="mt-auto pt-[12px] flex items-center justify-between border-t border-outline-variant/20">
                      <div className="flex items-center gap-1 text-on-surface-variant font-label-sm text-xs">
                        <span className="material-symbols-outlined text-[16px]">group</span>
                        <span className={isFull ? 'text-error font-medium' : 'text-on-surface-variant'}>
                          {availableSeats} / {course.capacity} Seats Available
                        </span>
                      </div>

                      <button
                        onClick={() => handleRegister(course)}
                        disabled={Boolean(isFull || isRegistering)}
                        className="px-4 py-1.5 bg-primary text-on-primary font-label-sm text-xs rounded-lg shadow-sm hover:bg-primary/90 transition-all disabled:opacity-40 disabled:cursor-not-allowed flex items-center gap-1"
                      >
                        {isRegistering ? (
                          'Registering...'
                        ) : isFull ? (
                          'Full'
                        ) : (
                          <>
                            <span className="material-symbols-outlined text-[14px]">add</span>
                            Register
                          </>
                        )}
                      </button>
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </div>
      </main>
    </div>
  );
}

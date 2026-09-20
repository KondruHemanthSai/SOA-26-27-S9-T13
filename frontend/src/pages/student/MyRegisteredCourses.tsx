import { useState, useEffect } from 'react';
import StudentSidebar from '../../components/layout/StudentSidebar';
import TopNav from '../../components/layout/TopNav';
import { registrationApi, getErrorMessage } from '../../services/api';
import type { Registration } from '../../types/api';

export default function MyRegisteredCourses() {
  const [courses, setCourses] = useState<Registration[]>([]);
  const [loading, setLoading] = useState(true);
  const [droppingId, setDroppingId] = useState<string | null>(null);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const fetchRegistrations = async () => {
    try {
      setLoading(true);
      setError('');
      const data = await registrationApi.getMyActiveRegistrations();
      setCourses(data || []);
    } catch (err) {
      setError(getErrorMessage(err, 'Failed to fetch registered courses.'));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchRegistrations();
  }, []);

  const handleDrop = async (regId: string, courseName: string) => {
    if (!window.confirm(`Are you sure you want to drop ${courseName}? This will release your seat.`)) {
      return;
    }

    try {
      setDroppingId(regId);
      setError('');
      setSuccess('');
      await registrationApi.dropCourse(regId);
      setSuccess(`Successfully dropped ${courseName}.`);
      setTimeout(() => setSuccess(''), 4000);
      // Refresh list
      fetchRegistrations();
    } catch (err) {
      setError(getErrorMessage(err, 'Failed to drop course.'));
    } finally {
      setDroppingId(null);
    }
  };

  const totalCredits = courses
    .filter((c) => c.status === 'REGISTERED')
    .reduce((sum, c) => sum + (c.credits || 0), 0);

  return (
    <div className="flex h-screen bg-background text-on-background">
      <StudentSidebar />
      <main className="ml-64 flex-1 flex flex-col min-h-screen relative overflow-y-auto">
        <TopNav searchPlaceholder="Search registered courses..." />

        <div className="flex-grow p-margin-mobile md:p-margin-desktop max-w-container-max mx-auto w-full flex flex-col gap-stack-lg relative">
          {/* Background Glow */}
          <div className="absolute top-0 right-0 w-[600px] h-[600px] bg-primary/5 rounded-full blur-[100px] -z-10 pointer-events-none"></div>

          {/* Header Section */}
          <header className="flex flex-col md:flex-row md:items-end justify-between gap-stack-md pt-stack-sm">
            <div>
              <h1 className="font-display-lg text-display-lg text-on-surface mb-unit">My Courses</h1>
              <p className="font-body-lg text-body-lg text-on-surface-variant">Fall 2024 Semester</p>
            </div>

            {/* Status Bar */}
            <div className="glass-card px-4 py-2 inline-flex items-center gap-2 bg-emerald-50/50 border-emerald-200/30 text-emerald-800">
              <span className="material-symbols-outlined text-[20px] text-emerald-600" style={{ fontVariationSettings: "'FILL' 1" }}>
                check_circle
              </span>
              <span className="font-label-md text-label-md font-medium">
                {courses.length} Active Enrollments
              </span>
            </div>
          </header>

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

          {/* Course List Glass Panel */}
          <section className="glass-panel overflow-hidden rounded-2xl">
            {loading ? (
              <div className="p-16 flex flex-col items-center justify-center">
                <div className="w-10 h-10 border-4 border-primary/20 border-t-primary rounded-full animate-spin"></div>
                <p className="mt-4 text-on-surface-variant font-body-md">Loading your courses...</p>
              </div>
            ) : courses.length === 0 ? (
              <div className="p-16 text-center text-on-surface-variant flex flex-col items-center justify-center">
                <span className="material-symbols-outlined text-[48px] opacity-40 mb-2">folder_open</span>
                <p className="text-on-surface font-headline-md text-lg font-semibold">No registered courses</p>
                <p className="text-sm mt-1 mb-4">You have not registered for any courses for this term.</p>
                <a
                  href="/courses"
                  className="px-5 py-2.5 bg-primary text-on-primary font-label-md text-sm rounded-lg shadow-sm hover:bg-primary/90 transition-all"
                >
                  Browse Course Catalogue
                </a>
              </div>
            ) : (
              <>
                {/* Desktop Table View */}
                <div className="hidden lg:block overflow-x-auto w-full">
                  <table className="w-full text-left border-collapse">
                    <thead>
                      <tr className="border-b border-outline-variant/30 text-on-surface-variant bg-surface-variant/20">
                        <th className="py-4 px-6 font-label-md text-label-md font-semibold">Course</th>
                        <th className="py-4 px-6 font-label-md text-label-md font-semibold">Credits</th>
                        <th className="py-4 px-6 font-label-md text-label-md font-semibold">Semester</th>
                        <th className="py-4 px-6 font-label-md text-label-md font-semibold">Academic Year</th>
                        <th className="py-4 px-6 font-label-md text-label-md font-semibold">Status</th>
                        <th className="py-4 px-6 font-label-md text-label-md font-semibold text-right">Action</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-outline-variant/20">
                      {courses.map((course) => {
                        const regId = course.id || course.registrationId;
                        const isDropping = droppingId === regId;
                        return (
                          <tr key={regId} className="hover:bg-primary/5 transition-colors group">
                            <td className="py-5 px-6">
                              <div className="flex flex-col">
                                <span className="font-headline-md text-headline-md text-[18px] leading-snug text-on-surface">
                                  {course.courseName}
                                </span>
                                <span className="font-label-sm text-label-sm text-primary mt-1 font-semibold">
                                  {course.courseCode}
                                </span>
                              </div>
                            </td>
                            <td className="py-5 px-6 font-body-md text-body-md text-on-surface-variant font-medium">
                              {course.credits}
                            </td>
                            <td className="py-5 px-6 font-body-md text-body-md text-on-surface-variant">
                              Semester {course.semester}
                            </td>
                            <td className="py-5 px-6 font-body-md text-body-md text-on-surface-variant">
                              {course.academicYear}
                            </td>
                            <td className="py-5 px-6">
                              <span
                                className={`px-3 py-1 rounded-full font-label-sm text-label-sm inline-flex items-center gap-1 ${
                                  course.status === 'REGISTERED'
                                    ? 'bg-primary-container text-on-primary-container shadow-sm'
                                    : 'bg-secondary-container text-on-secondary-container'
                                }`}
                              >
                                {course.status}
                              </span>
                            </td>
                            <td className="py-5 px-6 text-right">
                              <button
                                onClick={() => handleDrop(regId, course.courseName)}
                                disabled={isDropping}
                                className="font-label-sm text-label-sm px-4 py-2 border border-error/30 text-error rounded-md hover:bg-error/10 hover:border-error transition-all disabled:opacity-40"
                              >
                                {isDropping ? 'Dropping...' : 'Drop Course'}
                              </button>
                            </td>
                          </tr>
                        );
                      })}
                    </tbody>
                  </table>
                </div>

                {/* Mobile/Tablet Stacked View */}
                <div className="lg:hidden flex flex-col divide-y divide-outline-variant/20">
                  {courses.map((course) => {
                    const regId = course.id || course.registrationId;
                    const isDropping = droppingId === regId;
                    return (
                      <div key={regId} className="p-4 flex flex-col gap-3">
                        <div className="flex justify-between items-start">
                          <div>
                            <h3 className="font-headline-md text-[18px] text-on-surface">{course.courseName}</h3>
                            <span className="font-label-sm text-label-sm text-primary font-semibold">{course.courseCode}</span>
                          </div>
                          <span
                            className={`px-3 py-1 rounded-full font-label-sm text-xs ${
                              course.status === 'REGISTERED'
                                ? 'bg-primary-container text-on-primary-container'
                                : 'bg-secondary-container text-on-secondary-container'
                            }`}
                          >
                            {course.status}
                          </span>
                        </div>
                        <div className="grid grid-cols-2 gap-2 text-sm text-on-surface-variant">
                          <div><span className="text-outline">Credits:</span> {course.credits}</div>
                          <div><span className="text-outline">Semester:</span> {course.semester}</div>
                          <div className="col-span-2"><span className="text-outline">Year:</span> {course.academicYear}</div>
                        </div>
                        <button
                          onClick={() => handleDrop(regId, course.courseName)}
                          disabled={isDropping}
                          className="mt-2 w-full font-label-sm text-label-sm px-4 py-2 border border-error/30 text-error rounded-md hover:bg-error/10 transition-colors disabled:opacity-40"
                        >
                          {isDropping ? 'Dropping...' : 'Drop Course'}
                        </button>
                      </div>
                    );
                  })}
                </div>
              </>
            )}
          </section>

          {/* Summary Info */}
          <div className="flex justify-end pt-2">
            <p className="font-body-md text-body-md text-on-surface-variant">
              Total Registered Credits: <strong className="text-on-surface font-semibold">{totalCredits}</strong> / 18 Max
            </p>
          </div>
        </div>
      </main>
    </div>
  );
}

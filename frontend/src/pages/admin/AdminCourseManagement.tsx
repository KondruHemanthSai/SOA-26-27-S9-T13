import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import AdminSidebar from '../../components/layout/AdminSidebar';
import { courseApi, getErrorMessage } from '../../services/api';
import type { Course } from '../../types/api';

export default function AdminCourseManagement() {
  const [courses, setCourses] = useState<Course[]>([]);
  const [search, setSearch] = useState('');
  const [department, setDepartment] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const fetchCourses = async () => {
    try {
      setLoading(true);
      setError('');
      const data = await courseApi.getCourses({
        search: search.trim() || undefined,
        department: department || undefined,
      });
      setCourses(data || []);
    } catch (err) {
      setError(getErrorMessage(err, 'Failed to fetch courses.'));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchCourses();
  }, [department]);

  const handleSearchSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    fetchCourses();
  };

  const handleToggleStatus = async (course: Course) => {
    try {
      setError('');
      setSuccess('');
      if (course.status === 'ACTIVE') {
        await courseApi.deactivateCourse(course.id);
        setSuccess(`Deactivated course ${course.courseCode}.`);
      } else {
        await courseApi.activateCourse(course.id);
        setSuccess(`Activated course ${course.courseCode}.`);
      }
      setTimeout(() => setSuccess(''), 3000);
      fetchCourses();
    } catch (err) {
      setError(getErrorMessage(err, 'Failed to update course status.'));
    }
  };

  return (
    <div className="font-body-md text-body-md bg-background text-on-surface antialiased overflow-x-hidden min-h-screen flex flex-col md:flex-row">
      <AdminSidebar />

      <main className="flex-1 w-full md:pl-64 flex flex-col min-h-screen">
        {/* TopNavBar */}
        <header className="bg-surface/60 backdrop-blur-lg text-primary font-body-md text-body-md fixed top-0 w-full z-40 border-b border-primary/10 shadow-sm flex justify-between items-center px-gutter h-16 md:w-[calc(100%-16rem)]">
          <form onSubmit={handleSearchSubmit} className="hidden md:flex flex-1 items-center max-w-md relative mr-4">
            <span className="material-symbols-outlined absolute left-3 text-on-surface-variant text-[20px] pointer-events-none">
              search
            </span>
            <input
              className="w-full bg-surface-container-low border-none rounded-full py-2 pl-10 pr-4 text-sm focus:ring-2 focus:ring-primary/20 transition-shadow outline-none placeholder:text-on-surface-variant/60"
              placeholder="Search courses by code or title..."
              type="text"
              value={search}
              onChange={(e) => setSearch(e.target.value)}
            />
          </form>

          <div className="flex items-center gap-2">
            <Link
              to="/admin/notifications"
              className="p-2 text-on-surface-variant hover:bg-surface-container transition-colors rounded-full"
            >
              <span className="material-symbols-outlined">notifications</span>
            </Link>
            <div className="w-8 h-8 rounded-full bg-primary/20 flex items-center justify-center font-bold text-xs text-primary">
              AD
            </div>
          </div>
        </header>

        {/* Page Canvas */}
        <div className="pt-24 pb-12 px-margin-mobile md:px-margin-desktop max-w-container-max mx-auto w-full flex-1">
          {/* Page Header Section */}
          <div className="flex flex-col md:flex-row md:items-end justify-between gap-stack-md mb-stack-lg">
            <div>
              <h1 className="font-headline-lg-mobile md:font-headline-lg text-headline-lg-mobile md:text-headline-lg text-on-surface mb-2">
                Manage Academic Courses
              </h1>
              <p className="font-body-md text-body-md text-on-surface-variant max-w-2xl">
                Update course details, monitor enrollment limits, and configure prerequisites.
              </p>
            </div>
            <Link
              to="/admin/courses/new"
              className="bg-primary text-on-primary font-semibold py-2.5 px-5 rounded-lg shadow-sm hover:opacity-90 hover:shadow-md transition-all flex items-center justify-center gap-2 self-start md:self-auto shrink-0 font-label-md text-sm"
            >
              <span className="material-symbols-outlined text-[20px]">add</span>
              Create Course
            </Link>
          </div>

          {error && (
            <div className="p-4 mb-4 bg-error-container/30 border border-error/20 text-on-error-container rounded-xl font-body-sm text-body-sm flex items-center gap-3">
              <span className="material-symbols-outlined text-error">error</span>
              {error}
            </div>
          )}

          {success && (
            <div className="p-4 mb-4 bg-emerald-50 border border-emerald-200 text-emerald-800 rounded-xl font-body-sm text-body-sm flex items-center gap-3">
              <span className="material-symbols-outlined text-emerald-600" style={{ fontVariationSettings: "'FILL' 1" }}>
                check_circle
              </span>
              {success}
            </div>
          )}

          {/* Controls & Filters Bar */}
          <div className="glass-card rounded-xl p-4 mb-stack-md flex flex-col lg:flex-row gap-4 items-start lg:items-center justify-between shadow-sm">
            <div className="relative w-full lg:w-96">
              <span className="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-on-surface-variant">
                search
              </span>
              <input
                className="w-full bg-surface/50 border border-outline-variant/50 rounded-lg py-2 pl-10 pr-4 text-sm focus:border-primary focus:ring-1 focus:ring-primary transition-colors outline-none"
                placeholder="Search by course name or code..."
                type="text"
                value={search}
                onChange={(e) => setSearch(e.target.value)}
              />
            </div>
            <div className="flex flex-wrap items-center gap-2 w-full lg:w-auto">
              <select
                value={department}
                onChange={(e) => setDepartment(e.target.value)}
                className="px-3 py-1.5 bg-surface-container-low border border-outline-variant/30 rounded-lg text-sm font-label-md outline-none"
              >
                <option value="">All Departments</option>
                <option value="Computer Science">Computer Science</option>
                <option value="Mathematics">Mathematics</option>
                <option value="Physics">Physics</option>
                <option value="Humanities">Humanities</option>
              </select>
            </div>
          </div>

          {/* Data Table Card */}
          <div className="glass-card rounded-xl shadow-sm overflow-hidden flex flex-col">
            {loading ? (
              <div className="p-16 flex flex-col items-center justify-center">
                <div className="w-10 h-10 border-4 border-primary/20 border-t-primary rounded-full animate-spin"></div>
                <p className="mt-4 text-on-surface-variant text-sm">Loading course catalogue...</p>
              </div>
            ) : courses.length === 0 ? (
              <div className="p-16 text-center text-on-surface-variant">
                <span className="material-symbols-outlined text-[48px] opacity-40 mb-2">school</span>
                <p className="font-semibold text-base text-on-surface">No courses found</p>
              </div>
            ) : (
              <div className="overflow-x-auto w-full">
                <table className="w-full text-left border-collapse min-w-[800px]">
                  <thead>
                    <tr className="border-b border-outline-variant/20 bg-surface-container-lowest/50 text-on-surface-variant font-label-sm text-label-sm uppercase tracking-wider">
                      <th className="py-4 px-6 font-semibold">Course</th>
                      <th className="py-4 px-6 font-semibold">Department</th>
                      <th className="py-4 px-6 font-semibold">Credits</th>
                      <th className="py-4 px-6 font-semibold">Enrollment</th>
                      <th className="py-4 px-6 font-semibold">Status</th>
                      <th className="py-4 px-6 font-semibold text-right">Actions</th>
                    </tr>
                  </thead>
                  <tbody className="font-body-sm text-body-sm divide-y divide-outline-variant/10">
                    {courses.map((c) => {
                      const enrolled = c.enrolledCount || 0;
                      const cap = c.capacity || 40;
                      const pct = Math.min(100, Math.round((enrolled / cap) * 100));
                      const isFull = enrolled >= cap;

                      return (
                        <tr key={c.id || c.courseCode} className="table-row-hover transition-colors group">
                          <td className="py-4 px-6">
                            <div className="flex items-center gap-3">
                              <div className="w-10 h-10 rounded-lg bg-primary/10 flex items-center justify-center text-primary font-label-md text-xs font-bold">
                                {c.courseCode.slice(0, 3)}
                              </div>
                              <div>
                                <div className="font-semibold text-on-surface">{c.courseName}</div>
                                <div className="font-label-sm text-on-surface-variant mt-0.5">{c.courseCode}</div>
                              </div>
                            </div>
                          </td>
                          <td className="py-4 px-6 text-on-surface-variant">{c.department}</td>
                          <td className="py-4 px-6 text-on-surface-variant font-label-md">{c.credits}</td>
                          <td className="py-4 px-6">
                            <div className="flex items-center gap-3">
                              <div className="w-24 h-2 bg-surface-container-high rounded-full overflow-hidden">
                                <div
                                  className={`h-full ${isFull ? 'bg-error' : 'bg-tertiary-container'}`}
                                  style={{ width: `${pct}%` }}
                                ></div>
                              </div>
                              <span className="font-label-md text-xs text-on-surface-variant w-14 text-right">
                                {enrolled}/{cap}
                              </span>
                            </div>
                          </td>
                          <td className="py-4 px-6">
                            <span
                              className={`inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full font-label-sm text-xs ${
                                c.status === 'ACTIVE'
                                  ? 'bg-emerald-50 text-emerald-700'
                                  : 'bg-error/10 text-error'
                              }`}
                            >
                              <span
                                className={`w-1.5 h-1.5 rounded-full ${
                                  c.status === 'ACTIVE' ? 'bg-emerald-600' : 'bg-error'
                                }`}
                              ></span>
                              {c.status}
                            </span>
                          </td>
                          <td className="py-4 px-6 text-right">
                            <div className="flex items-center justify-end gap-1">
                              <button
                                onClick={() => handleToggleStatus(c)}
                                className="p-1.5 text-on-surface-variant hover:text-primary rounded-md transition-colors"
                                title={c.status === 'ACTIVE' ? 'Deactivate course' : 'Activate course'}
                              >
                                <span className="material-symbols-outlined text-[20px]">
                                  {c.status === 'ACTIVE' ? 'toggle_on' : 'toggle_off'}
                                </span>
                              </button>
                              <Link
                                to={`/admin/courses/${c.id || c.courseCode}`}
                                className="p-1.5 text-on-surface-variant hover:text-primary rounded-md transition-colors"
                                title="Edit course"
                              >
                                <span className="material-symbols-outlined text-[20px]">edit</span>
                              </Link>
                            </div>
                          </td>
                        </tr>
                      );
                    })}
                  </tbody>
                </table>
              </div>
            )}

            {/* Table Pagination Footer */}
            <div className="border-t border-outline-variant/20 bg-surface-container-lowest/30 px-6 py-3 flex items-center justify-between mt-auto">
              <span className="text-sm text-on-surface-variant font-medium">
                Showing {courses.length} entries
              </span>
            </div>
          </div>
        </div>
      </main>
    </div>
  );
}

import { useState, useEffect } from 'react';
import AdminSidebar from '../../components/layout/AdminSidebar';
import { scheduleApi, getErrorMessage } from '../../services/api';
import type { ScheduleItem, CreateScheduleRequest, DayOfWeek, ScheduleType } from '../../types/api';

export default function AdminScheduleManagement() {
  const [schedules, setSchedules] = useState<ScheduleItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [showModal, setShowModal] = useState(false);

  // Filter state
  const [filterDay, setFilterDay] = useState<string>('');

  // Form state
  const [formData, setFormData] = useState<CreateScheduleRequest>({
    courseId: '',
    section: 'A',
    dayOfWeek: 'MONDAY',
    startTime: '09:00',
    endTime: '11:00',
    classroom: 'Room 101',
    building: 'Science Hall',
    faculty: 'Dr. Smith',
    semester: 1,
    academicYear: '2024-2025',
    scheduleType: 'LECTURE',
  });

  const fetchSchedules = async () => {
    try {
      setLoading(true);
      setError('');
      const data = await scheduleApi.listSchedules(filterDay ? { dayOfWeek: filterDay as DayOfWeek } : undefined);
      setSchedules(data);
    } catch (err) {
      setError(getErrorMessage(err, 'Failed to load schedules.'));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchSchedules();
  }, [filterDay]);

  const handleCreateSchedule = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    setIsSubmitting(true);

    try {
      await scheduleApi.createSchedule(formData);
      setSuccess('Timetable slot created successfully.');
      setShowModal(false);
      fetchSchedules();
      setTimeout(() => setSuccess(''), 4000);
    } catch (err) {
      setError(getErrorMessage(err, 'Failed to create schedule slot.'));
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleDelete = async (scheduleId: string) => {
    if (!window.confirm('Are you sure you want to remove this timetable slot?')) return;
    try {
      await scheduleApi.deleteSchedule(scheduleId);
      setSuccess('Schedule slot removed successfully.');
      setSchedules((prev) => prev.filter((s) => s.id !== scheduleId));
      setTimeout(() => setSuccess(''), 4000);
    } catch (err) {
      setError(getErrorMessage(err, 'Failed to remove schedule slot.'));
    }
  };

  return (
    <div className="flex h-screen bg-background text-on-surface">
      <AdminSidebar />
      <main className="ml-64 flex-1 flex flex-col min-h-screen relative overflow-y-auto">
        {/* Top Header */}
        <header className="bg-surface/60 backdrop-blur-lg border-b border-primary/10 h-16 flex items-center justify-between px-gutter sticky top-0 z-40">
          <div className="flex items-center gap-3">
            <span className="font-headline-md text-headline-md font-semibold text-primary">Timetable Management</span>
          </div>
          <button
            onClick={() => setShowModal(true)}
            className="px-4 py-2 bg-primary text-on-primary font-label-md text-sm rounded-lg shadow-sm hover:bg-primary/90 transition-colors flex items-center gap-1.5"
          >
            <span className="material-symbols-outlined text-[18px]">add</span>
            Add Schedule Slot
          </button>
        </header>

        <div className="p-margin-mobile md:p-margin-desktop max-w-container-max mx-auto w-full flex flex-col gap-stack-lg">
          <div>
            <h1 className="font-display-lg text-display-lg text-on-surface">Academic Timetables</h1>
            <p className="font-body-lg text-body-lg text-on-surface-variant mt-1">
              Configure course schedules, assigned classrooms, faculty, and time allocations.
            </p>
          </div>

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

          {/* Controls Bar */}
          <div className="glass-card rounded-xl p-4 flex flex-wrap items-center justify-between gap-4">
            <div className="flex items-center gap-3">
              <span className="font-label-sm text-xs text-on-surface-variant">Filter by Day:</span>
              <select
                value={filterDay}
                onChange={(e) => setFilterDay(e.target.value)}
                className="bg-surface/50 border border-outline-variant/50 rounded-lg px-3 py-1.5 font-label-md text-xs text-on-surface outline-none"
              >
                <option value="">All Days</option>
                <option value="MONDAY">Monday</option>
                <option value="TUESDAY">Tuesday</option>
                <option value="WEDNESDAY">Wednesday</option>
                <option value="THURSDAY">Thursday</option>
                <option value="FRIDAY">Friday</option>
                <option value="SATURDAY">Saturday</option>
              </select>
            </div>
            <span className="font-label-sm text-xs text-on-surface-variant">
              Showing {schedules.length} schedule entries
            </span>
          </div>

          {/* Schedule Table */}
          <div className="glass-card rounded-xl shadow-sm overflow-hidden flex flex-col">
            {loading ? (
              <div className="p-12 flex flex-col items-center justify-center">
                <div className="w-8 h-8 border-4 border-primary/20 border-t-primary rounded-full animate-spin"></div>
                <p className="mt-3 text-on-surface-variant text-sm">Loading schedules...</p>
              </div>
            ) : schedules.length === 0 ? (
              <div className="p-12 text-center text-on-surface-variant">
                <span className="material-symbols-outlined text-[40px] opacity-40 mb-2">calendar_today</span>
                <p className="font-semibold text-sm">No schedule entries found</p>
                <p className="text-xs mt-1">Create a schedule slot to populate this day.</p>
              </div>
            ) : (
              <div className="overflow-x-auto w-full">
                <table className="w-full text-left border-collapse min-w-[700px]">
                  <thead>
                    <tr className="border-b border-outline-variant/20 bg-surface-container-lowest/50 text-on-surface-variant font-label-sm text-xs uppercase tracking-wider">
                      <th className="py-3 px-4">Course</th>
                      <th className="py-3 px-4">Day & Time</th>
                      <th className="py-3 px-4">Classroom</th>
                      <th className="py-3 px-4">Faculty</th>
                      <th className="py-3 px-4">Type</th>
                      <th className="py-3 px-4 text-right">Actions</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-outline-variant/10 text-body-sm text-sm">
                    {schedules.map((s) => (
                      <tr key={s.id} className="hover:bg-primary/5 transition-colors">
                        <td className="py-3 px-4">
                          <div className="font-semibold text-on-surface">{s.courseName || s.courseCode}</div>
                          <div className="font-label-sm text-xs text-primary">{s.courseCode} • Sec {s.section || 'A'}</div>
                        </td>
                        <td className="py-3 px-4 text-on-surface-variant">
                          <div className="font-semibold text-on-surface">{s.dayOfWeek}</div>
                          <div className="text-xs">{s.startTime} - {s.endTime}</div>
                        </td>
                        <td className="py-3 px-4 text-on-surface-variant">
                          <div>{s.classroom}</div>
                          <div className="text-xs text-on-surface-variant/70">{s.building || 'Campus'}</div>
                        </td>
                        <td className="py-3 px-4 text-on-surface-variant">{s.faculty || 'Unassigned'}</td>
                        <td className="py-3 px-4">
                          <span className="px-2 py-0.5 rounded text-xs font-label-sm bg-primary/10 text-primary">
                            {s.scheduleType || 'LECTURE'}
                          </span>
                        </td>
                        <td className="py-3 px-4 text-right">
                          <button
                            onClick={() => handleDelete(s.id)}
                            className="p-1.5 text-on-surface-variant hover:text-error hover:bg-error/10 rounded-md transition-colors"
                            title="Delete slot"
                          >
                            <span className="material-symbols-outlined text-[18px]">delete</span>
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        </div>
      </main>

      {/* Modal for adding new schedule slot */}
      {showModal && (
        <div className="fixed inset-0 z-50 bg-black/40 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-surface rounded-2xl p-6 max-w-lg w-full border border-primary/20 shadow-2xl space-y-4">
            <div className="flex justify-between items-center">
              <h3 className="font-headline-md text-lg font-bold text-on-surface">Add Timetable Slot</h3>
              <button onClick={() => setShowModal(false)} className="text-on-surface-variant hover:text-on-surface">
                <span className="material-symbols-outlined">close</span>
              </button>
            </div>

            <form onSubmit={handleCreateSchedule} className="space-y-4">
              <div className="grid grid-cols-2 gap-3">
                <div className="space-y-1">
                  <label className="font-label-sm text-xs text-on-surface-variant">Course ID or Code</label>
                  <input
                    type="text"
                    required
                    placeholder="e.g. CS301"
                    value={formData.courseId}
                    onChange={(e) => setFormData({ ...formData, courseId: e.target.value })}
                    className="w-full rounded-lg glass-input px-3 py-2 font-body-sm text-on-surface"
                  />
                </div>
                <div className="space-y-1">
                  <label className="font-label-sm text-xs text-on-surface-variant">Section</label>
                  <input
                    type="text"
                    value={formData.section}
                    onChange={(e) => setFormData({ ...formData, section: e.target.value })}
                    className="w-full rounded-lg glass-input px-3 py-2 font-body-sm text-on-surface"
                  />
                </div>
              </div>

              <div className="grid grid-cols-3 gap-3">
                <div className="space-y-1">
                  <label className="font-label-sm text-xs text-on-surface-variant">Day</label>
                  <select
                    value={formData.dayOfWeek}
                    onChange={(e) => setFormData({ ...formData, dayOfWeek: e.target.value as DayOfWeek })}
                    className="w-full rounded-lg glass-input px-3 py-2 font-body-sm text-on-surface"
                  >
                    <option value="MONDAY">Monday</option>
                    <option value="TUESDAY">Tuesday</option>
                    <option value="WEDNESDAY">Wednesday</option>
                    <option value="THURSDAY">Thursday</option>
                    <option value="FRIDAY">Friday</option>
                    <option value="SATURDAY">Saturday</option>
                  </select>
                </div>
                <div className="space-y-1">
                  <label className="font-label-sm text-xs text-on-surface-variant">Start Time</label>
                  <input
                    type="time"
                    required
                    value={formData.startTime}
                    onChange={(e) => setFormData({ ...formData, startTime: e.target.value })}
                    className="w-full rounded-lg glass-input px-3 py-2 font-body-sm text-on-surface"
                  />
                </div>
                <div className="space-y-1">
                  <label className="font-label-sm text-xs text-on-surface-variant">End Time</label>
                  <input
                    type="time"
                    required
                    value={formData.endTime}
                    onChange={(e) => setFormData({ ...formData, endTime: e.target.value })}
                    className="w-full rounded-lg glass-input px-3 py-2 font-body-sm text-on-surface"
                  />
                </div>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div className="space-y-1">
                  <label className="font-label-sm text-xs text-on-surface-variant">Classroom</label>
                  <input
                    type="text"
                    required
                    value={formData.classroom}
                    onChange={(e) => setFormData({ ...formData, classroom: e.target.value })}
                    className="w-full rounded-lg glass-input px-3 py-2 font-body-sm text-on-surface"
                  />
                </div>
                <div className="space-y-1">
                  <label className="font-label-sm text-xs text-on-surface-variant">Building</label>
                  <input
                    type="text"
                    value={formData.building}
                    onChange={(e) => setFormData({ ...formData, building: e.target.value })}
                    className="w-full rounded-lg glass-input px-3 py-2 font-body-sm text-on-surface"
                  />
                </div>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div className="space-y-1">
                  <label className="font-label-sm text-xs text-on-surface-variant">Faculty</label>
                  <input
                    type="text"
                    value={formData.faculty}
                    onChange={(e) => setFormData({ ...formData, faculty: e.target.value })}
                    className="w-full rounded-lg glass-input px-3 py-2 font-body-sm text-on-surface"
                  />
                </div>
                <div className="space-y-1">
                  <label className="font-label-sm text-xs text-on-surface-variant">Type</label>
                  <select
                    value={formData.scheduleType}
                    onChange={(e) => setFormData({ ...formData, scheduleType: e.target.value as ScheduleType })}
                    className="w-full rounded-lg glass-input px-3 py-2 font-body-sm text-on-surface"
                  >
                    <option value="LECTURE">Lecture</option>
                    <option value="LAB">Lab</option>
                    <option value="TUTORIAL">Tutorial</option>
                  </select>
                </div>
              </div>

              <div className="flex justify-end gap-3 pt-3 border-t border-outline-variant/20">
                <button
                  type="button"
                  onClick={() => setShowModal(false)}
                  className="px-4 py-2 border border-outline-variant text-on-surface font-label-md text-sm rounded-lg hover:bg-surface-container"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={isSubmitting}
                  className="px-6 py-2 bg-primary text-on-primary font-label-md text-sm rounded-lg shadow-sm hover:bg-primary/90 disabled:opacity-50"
                >
                  {isSubmitting ? 'Saving...' : 'Add Slot'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}

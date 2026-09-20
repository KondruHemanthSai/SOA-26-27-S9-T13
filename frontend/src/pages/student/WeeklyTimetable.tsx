import { useState, useEffect } from 'react';
import StudentSidebar from '../../components/layout/StudentSidebar';
import TopNav from '../../components/layout/TopNav';
import { scheduleApi, getErrorMessage } from '../../services/api';
import type { ScheduleItem, DayOfWeek } from '../../types/api';

const DAYS: DayOfWeek[] = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY'];
const DAY_LABELS: Record<DayOfWeek, string> = {
  MONDAY: 'Monday',
  TUESDAY: 'Tuesday',
  WEDNESDAY: 'Wednesday',
  THURSDAY: 'Thursday',
  FRIDAY: 'Friday',
  SATURDAY: 'Saturday',
  SUNDAY: 'Sunday',
};

const TIME_SLOTS = [
  '09:00',
  '10:00',
  '11:00',
  '12:00',
  '13:00',
  '14:00',
  '15:00',
  '16:00',
];

export default function WeeklyTimetable() {
  const [schedules, setSchedules] = useState<ScheduleItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    async function fetchTimetable() {
      try {
        setLoading(true);
        setError('');
        const data = await scheduleApi.getMySchedule();
        setSchedules(data || []);
      } catch (err) {
        setError(getErrorMessage(err, 'Failed to fetch personal timetable.'));
      } finally {
        setLoading(false);
      }
    }
    fetchTimetable();
  }, []);

  // Compute position helper: 09:00 is top 0px, each hour is 100px
  const getTopAndHeight = (startTime: string, endTime: string) => {
    const parseHour = (t: string) => {
      const [h, m] = t.split(':').map(Number);
      return (h || 9) + (m || 0) / 60;
    };
    const startHour = parseHour(startTime);
    const endHour = parseHour(endTime);
    const top = Math.max(0, (startHour - 9) * 100);
    const height = Math.max(80, (endHour - startHour) * 100);
    return { top, height };
  };

  const dayColumns: Record<DayOfWeek, number> = {
    MONDAY: 2,
    TUESDAY: 3,
    WEDNESDAY: 4,
    THURSDAY: 5,
    FRIDAY: 6,
    SATURDAY: 7,
    SUNDAY: 8,
  };

  return (
    <div className="flex h-screen bg-background text-on-background font-body-md selection:bg-primary-container selection:text-on-primary-container">
      <StudentSidebar />
      <main className="flex-1 md:ml-64 flex flex-col min-h-screen overflow-y-auto">
        <TopNav searchPlaceholder="Search courses, rooms, or peers..." />

        {/* Page Content */}
        <div className="p-gutter max-w-container-max mx-auto w-full flex flex-col gap-stack-lg">
          {/* Header & Status Bar */}
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
            <div>
              <h1 className="font-headline-md text-headline-md font-bold text-on-surface">Weekly Schedule</h1>
              <p className="font-body-sm text-body-sm text-secondary mt-1">Fall Semester 2024</p>
            </div>
            <div className="inline-flex items-center gap-2 bg-tertiary-container/10 text-tertiary border border-tertiary/20 px-4 py-2 rounded-full font-label-md text-label-md shadow-sm backdrop-blur-sm self-start sm:self-auto">
              <span className="material-symbols-outlined text-sm" style={{ fontVariationSettings: "'FILL' 1" }}>
                check_circle
              </span>
              {schedules.length > 0 ? `${schedules.length} Scheduled Sessions` : 'No conflict'}
            </div>
          </div>

          {error && (
            <div className="p-4 bg-error-container/30 border border-error/20 text-on-error-container rounded-xl font-body-sm text-body-sm flex items-center gap-3">
              <span className="material-symbols-outlined text-error">error</span>
              {error}
            </div>
          )}

          {/* Timetable Grid */}
          <div className="bg-surface-container-lowest rounded-xl border border-outline-variant/60 shadow-sm overflow-hidden flex flex-col">
            {/* Grid Header (Days) */}
            <div className="grid grid-cols-[60px_repeat(5,1fr)] md:grid-cols-[80px_repeat(5,1fr)] border-b border-outline-variant/60 bg-surface-container/30">
              <div className="p-4"></div>
              {DAYS.map((day) => (
                <div key={day} className="p-4 text-center font-label-md text-label-md text-on-surface-variant font-medium border-l border-outline-variant/30">
                  {DAY_LABELS[day]}
                </div>
              ))}
            </div>

            {loading ? (
              <div className="h-[400px] flex flex-col items-center justify-center">
                <div className="w-10 h-10 border-4 border-primary/20 border-t-primary rounded-full animate-spin"></div>
                <p className="mt-4 text-on-surface-variant text-sm">Loading your weekly timetable...</p>
              </div>
            ) : schedules.length === 0 ? (
              <div className="h-[400px] flex flex-col items-center justify-center text-on-surface-variant text-center p-6">
                <span className="material-symbols-outlined text-[48px] opacity-40 mb-2">event_busy</span>
                <p className="font-semibold text-base text-on-surface">No Timetable Entries</p>
                <p className="text-sm mt-1 max-w-sm">
                  You have not registered for any courses with scheduled classroom slots yet.
                </p>
              </div>
            ) : (
              /* Grid Body */
              <div className="relative grid grid-cols-[60px_repeat(5,1fr)] md:grid-cols-[80px_repeat(5,1fr)] grid-rows-8 h-[800px] bg-[linear-gradient(to_bottom,var(--color-surface-variant)_1px,transparent_1px)] bg-[size:100%_100px]">
                {/* Time Column */}
                <div className="row-span-8 flex flex-col border-r border-outline-variant/60 bg-surface-container-lowest z-10">
                  {TIME_SLOTS.map((slot) => (
                    <div key={slot} className="h-[100px] flex items-start justify-end pr-3 pt-2 font-label-sm text-label-sm text-secondary">
                      {slot}
                    </div>
                  ))}
                </div>

                {/* Day Vertical Dividers */}
                <div className="col-start-2 row-span-8 border-l border-outline-variant/30"></div>
                <div className="col-start-3 row-span-8 border-l border-outline-variant/30"></div>
                <div className="col-start-4 row-span-8 border-l border-outline-variant/30"></div>
                <div className="col-start-5 row-span-8 border-l border-outline-variant/30"></div>
                <div className="col-start-6 row-span-8 border-l border-outline-variant/30"></div>

                {/* Schedule Blocks */}
                {schedules.map((item, index) => {
                  const col = dayColumns[item.dayOfWeek] || 2;
                  const { top, height } = getTopAndHeight(item.startTime, item.endTime);
                  const colorScheme = index % 3 === 0
                    ? 'bg-primary/10 border-primary/20 text-primary'
                    : index % 3 === 1
                    ? 'bg-tertiary/10 border-tertiary/20 text-tertiary'
                    : 'bg-secondary-container/30 border-secondary-fixed/50 text-on-secondary-container';

                  return (
                    <div
                      key={item.id || index}
                      style={{ top: `${top}px`, height: `${height}px` }}
                      className={`absolute col-start-${col} w-full p-1 z-20`}
                    >
                      <div
                        className={`h-full w-full backdrop-blur-md border rounded-lg p-3 flex flex-col hover:shadow-md hover:scale-[1.01] transition-all cursor-pointer group ${colorScheme}`}
                      >
                        <span className="font-label-sm text-label-sm font-bold mb-1">
                          {item.courseCode}
                        </span>
                        <h3 className="font-body-sm text-body-sm font-semibold text-on-surface leading-tight">
                          {item.courseName}
                        </h3>
                        <div className="mt-auto flex items-center gap-1 font-label-sm text-label-sm text-secondary transition-colors">
                          <span className="material-symbols-outlined text-[14px]">location_on</span>
                          {item.classroom}{item.building ? `, ${item.building}` : ''}
                        </div>
                      </div>
                    </div>
                  );
                })}
              </div>
            )}
          </div>
        </div>
      </main>
    </div>
  );
}

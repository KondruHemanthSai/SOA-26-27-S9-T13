import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import StudentSidebar from '../../components/layout/StudentSidebar';
import TopNav from '../../components/layout/TopNav';
import {
  studentApi,
  admissionApi,
  registrationApi,
  scheduleApi,
  notificationApi,
  aiApi,
} from '../../services/api';
import type { StudentInsight } from '../../services/api';
import type {
  StudentProfile,
  AdmissionApplication,
  Registration,
  ScheduleItem,
  NotificationItem,
} from '../../types/api';

export default function StudentDashboard() {
  const [profile, setProfile] = useState<StudentProfile | null>(null);
  const [application, setApplication] = useState<AdmissionApplication | null>(null);
  const [registrations, setRegistrations] = useState<Registration[]>([]);
  const [schedules, setSchedules] = useState<ScheduleItem[]>([]);
  const [notifications, setNotifications] = useState<NotificationItem[]>([]);
  const [insights, setInsights] = useState<StudentInsight[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let isMounted = true;

    async function loadDashboardData() {
      try {
        setLoading(true);
        const [profRes, admRes, regRes, schedRes, notifRes, aiRes] = await Promise.allSettled([
          studentApi.getProfile(),
          admissionApi.getMyApplication(),
          registrationApi.getMyActiveRegistrations(),
          scheduleApi.getMySchedule(),
          notificationApi.getMyNotifications(0, 5),
          aiApi.getStudentInsights(),
        ]);

        if (!isMounted) return;

        if (profRes.status === 'fulfilled') setProfile(profRes.value);
        if (admRes.status === 'fulfilled') setApplication(admRes.value);
        if (regRes.status === 'fulfilled') setRegistrations(regRes.value);
        if (schedRes.status === 'fulfilled') setSchedules(schedRes.value);
        if (notifRes.status === 'fulfilled') setNotifications(notifRes.value?.content || []);
        if (aiRes.status === 'fulfilled') setInsights(aiRes.value || []);
      } finally {
        if (isMounted) setLoading(false);
      }
    }

    loadDashboardData();
    return () => {
      isMounted = false;
    };
  }, []);

  const totalCredits = registrations.reduce((sum, r) => sum + (r.credits || 0), 0);

  // Determine current day of week to filter "Today's Schedule"
  const dayNames = ['SUNDAY', 'MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY'];
  const todayName = dayNames[new Date().getDay()];
  const todaySchedules = schedules.filter((s) => s.dayOfWeek === todayName);
  const displayedSchedules = todaySchedules.length > 0 ? todaySchedules : schedules.slice(0, 3);

  const admissionStatus = application?.status || profile?.admissionStatus || 'NOT_APPLIED';

  return (
    <div className="flex h-screen">
      <StudentSidebar />
      <main className="ml-64 flex-1 flex flex-col min-h-screen relative overflow-y-auto">
        <TopNav searchPlaceholder="Search campus..." />
        <div className="p-[24px] flex-1 flex flex-col gap-[32px] max-w-[1280px] mx-auto w-full">
          {/* Header */}
          <div className="flex justify-between items-end">
            <div>
              <h1 className="text-display-lg font-display-lg text-on-surface mb-2">Dashboard</h1>
              <p className="text-body-lg font-body-lg text-on-surface-variant">
                {profile?.firstName ? `Welcome back, ${profile.firstName}.` : 'Welcome back.'} Here's your academic overview.
              </p>
            </div>
          </div>

          {loading ? (
            <div className="glass-card rounded-xl p-16 flex flex-col items-center justify-center">
              <div className="w-10 h-10 border-4 border-primary/20 border-t-primary rounded-full animate-spin"></div>
              <p className="mt-4 text-on-surface-variant font-body-md">Loading your academic dashboard...</p>
            </div>
          ) : (
            /* Bento Grid */
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-[24px]">
              {/* Admission Status Card */}
              <div className="lg:col-span-2 glass-card-no-hover rounded-xl p-[16px] flex flex-col gap-[16px]">
                <div className="flex justify-between items-start">
                  <div className="flex items-center gap-3">
                    <div className="w-10 h-10 rounded-full bg-primary/10 text-primary flex items-center justify-center">
                      <span className="material-symbols-outlined">how_to_reg</span>
                    </div>
                    <div>
                      <p className="font-label-sm text-label-sm text-on-surface-variant">Admission Status</p>
                      <p className="text-headline-md font-headline-md text-on-surface">
                        {admissionStatus.replace('_', ' ')}
                      </p>
                    </div>
                  </div>
                  <span className="bg-tertiary-container/10 text-tertiary px-3 py-1 rounded-full text-label-sm font-label-sm border border-tertiary/20">
                    Fall 2024
                  </span>
                </div>
                <div className="grid grid-cols-3 gap-[16px]">
                  <div className="bg-surface-container/30 rounded-lg p-3 text-center">
                    <p className="text-headline-md font-headline-md text-primary">{registrations.length}</p>
                    <p className="text-label-sm font-label-sm text-on-surface-variant">Courses</p>
                  </div>
                  <div className="bg-surface-container/30 rounded-lg p-3 text-center">
                    <p className="text-headline-md font-headline-md text-tertiary">{totalCredits}</p>
                    <p className="text-label-sm font-label-sm text-on-surface-variant">Credits</p>
                  </div>
                  <div className="bg-surface-container/30 rounded-lg p-3 text-center">
                    <p className="text-headline-md font-headline-md text-on-surface">18</p>
                    <p className="text-label-sm font-label-sm text-on-surface-variant">Max Credits</p>
                  </div>
                </div>
              </div>

              {/* Quick Actions */}
              <div className="glass-card-no-hover rounded-xl p-[16px] flex flex-col gap-3">
                <h3 className="font-headline-md text-[18px] text-on-surface font-semibold flex items-center gap-2">
                  <span className="material-symbols-outlined text-primary text-[20px]">bolt</span>
                  Quick Actions
                </h3>
                <div className="flex flex-col gap-2 flex-1">
                  <Link
                    to="/student/ai-assistant"
                    className="flex items-center gap-3 px-3 py-2.5 bg-gradient-to-r from-primary/15 to-tertiary/15 hover:from-primary/25 hover:to-tertiary/25 rounded-lg transition-all text-body-sm font-body-sm text-primary font-semibold border border-primary/20"
                  >
                    <span className="material-symbols-outlined text-primary text-[20px]">smart_toy</span>
                    Ask Campus AI
                  </Link>
                  <Link
                    to="/courses"
                    className="flex items-center gap-3 px-3 py-2.5 bg-primary/5 hover:bg-primary/10 rounded-lg transition-colors text-body-sm font-body-sm text-on-surface"
                  >
                    <span className="material-symbols-outlined text-primary text-[20px]">add_circle</span>
                    Register for Courses
                  </Link>
                  <Link
                    to="/timetable"
                    className="flex items-center gap-3 px-3 py-2.5 hover:bg-surface-container rounded-lg transition-colors text-body-sm font-body-sm text-on-surface-variant"
                  >
                    <span className="material-symbols-outlined text-[20px]">calendar_today</span>
                    View Timetable
                  </Link>
                  <Link
                    to="/my-courses"
                    className="flex items-center gap-3 px-3 py-2.5 hover:bg-surface-container rounded-lg transition-colors text-body-sm font-body-sm text-on-surface-variant"
                  >
                    <span className="material-symbols-outlined text-[20px]">list_alt</span>
                    My Courses
                  </Link>
                </div>
              </div>

              {/* AI Academic Advisor Insights Card */}
              {insights.length > 0 && (
                <div className="lg:col-span-3 glass-card-no-hover rounded-xl p-[18px] border border-primary/20 bg-gradient-to-br from-primary/5 via-surface to-tertiary/5">
                  <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 mb-3">
                    <div className="flex items-center gap-2.5">
                      <span className="material-symbols-outlined text-primary text-[24px]">psychology</span>
                      <div>
                        <h3 className="font-headline-md text-base font-semibold text-on-surface flex items-center gap-2">
                          AI Academic Advisor Insights
                          <span className="text-[10px] px-2 py-0.5 rounded-full bg-primary/10 text-primary font-bold uppercase tracking-wider">
                            Active
                          </span>
                        </h3>
                        <p className="text-xs text-on-surface-variant">
                          Real-time academic evaluation &amp; personalized recommendations
                        </p>
                      </div>
                    </div>
                    <Link
                      to="/student/ai-assistant"
                      className="inline-flex items-center gap-1.5 text-xs font-semibold text-primary hover:underline"
                    >
                      <span>Discuss with AI</span>
                      <span className="material-symbols-outlined text-[16px]">arrow_forward</span>
                    </Link>
                  </div>

                  <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-3">
                    {insights.slice(0, 3).map((item: StudentInsight, idx: number) => {
                      const isWarn = item.severity === 'WARNING';
                      const isCrit = item.severity === 'CRITICAL';
                      const isSuccess = item.severity === 'SUCCESS';
                      const borderColor = isCrit
                        ? 'border-error/30 bg-error/5'
                        : isWarn
                        ? 'border-amber-500/30 bg-amber-500/5'
                        : isSuccess
                        ? 'border-emerald-500/30 bg-emerald-500/5'
                        : 'border-primary/20 bg-primary/5';

                      const iconColor = isCrit
                        ? 'text-error'
                        : isWarn
                        ? 'text-amber-500'
                        : isSuccess
                        ? 'text-emerald-600'
                        : 'text-primary';

                      return (
                        <div
                          key={idx}
                          className={`p-3 rounded-lg border ${borderColor} flex flex-col justify-between`}
                        >
                          <div>
                            <div className="flex items-center gap-2 mb-1">
                              <span className={`material-symbols-outlined text-[18px] ${iconColor}`}>
                                {isCrit ? 'warning' : isWarn ? 'report_problem' : isSuccess ? 'check_circle' : 'lightbulb'}
                              </span>
                              <span className="text-xs font-bold text-on-surface line-clamp-1">{item.title}</span>
                            </div>
                            <p className="text-[11px] text-on-surface-variant leading-relaxed line-clamp-2">
                              {item.message}
                            </p>
                          </div>
                          {item.actionableStep && (
                            <div className="mt-2 pt-2 border-t border-outline-variant/10 text-[10px] text-primary font-medium flex items-center gap-1">
                              <span className="material-symbols-outlined text-[12px]">flag</span>
                              <span className="line-clamp-1">{item.actionableStep}</span>
                            </div>
                          )}
                        </div>
                      );
                    })}
                  </div>
                </div>
              )}

              {/* Today's Schedule */}
              <div className="lg:col-span-2 glass-card-no-hover rounded-xl p-[16px] flex flex-col gap-[16px]">
                <div className="flex justify-between items-center">
                  <h3 className="font-headline-md text-[18px] text-on-surface font-semibold flex items-center gap-2">
                    <span className="material-symbols-outlined text-primary text-[20px]">schedule</span>
                    {todaySchedules.length > 0 ? "Today's Schedule" : "Upcoming Schedule"}
                  </h3>
                  <Link to="/timetable" className="text-label-sm font-label-sm text-primary hover:underline">
                    View Full →
                  </Link>
                </div>

                {displayedSchedules.length === 0 ? (
                  <div className="p-6 text-center text-on-surface-variant bg-surface-container/20 rounded-lg">
                    <p className="text-sm font-medium">No classes scheduled for today.</p>
                    <Link to="/courses" className="text-primary text-xs mt-1 inline-block hover:underline">
                      Explore and register for courses →
                    </Link>
                  </div>
                ) : (
                  <div className="space-y-3">
                    {displayedSchedules.map((item, idx) => (
                      <div
                        key={item.id || idx}
                        className={`flex items-center gap-4 p-3 rounded-lg border-l-4 ${
                          idx % 2 === 0 ? 'bg-primary/5 border-primary' : 'bg-tertiary/5 border-tertiary'
                        }`}
                      >
                        <div className="text-center min-w-[60px]">
                          <p className={`text-label-md font-label-md ${idx % 2 === 0 ? 'text-primary' : 'text-tertiary'}`}>
                            {item.startTime}
                          </p>
                          <p className="text-[10px] text-on-surface-variant">{item.dayOfWeek.slice(0, 3)}</p>
                        </div>
                        <div className="flex-1">
                          <p className="text-body-md font-body-md font-semibold text-on-surface">
                            {item.courseName || item.courseCode}
                          </p>
                          <p className="text-label-sm font-label-sm text-on-surface-variant">
                            {item.classroom}{item.building ? `, ${item.building}` : ''} • {item.courseCode}
                          </p>
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </div>

              {/* Notifications Preview */}
              <div className="glass-card-no-hover rounded-xl p-[16px] flex flex-col gap-3">
                <div className="flex justify-between items-center">
                  <h3 className="font-headline-md text-[18px] text-on-surface font-semibold flex items-center gap-2">
                    <span className="material-symbols-outlined text-primary text-[20px]">notifications</span>
                    Notifications
                  </h3>
                  <Link to="/notifications" className="text-label-sm font-label-sm text-primary hover:underline">
                    View All →
                  </Link>
                </div>
                {notifications.length === 0 ? (
                  <div className="p-4 text-center text-on-surface-variant text-sm">
                    No new notifications
                  </div>
                ) : (
                  <div className="flex flex-col gap-2 flex-1">
                    {notifications.slice(0, 3).map((n) => (
                      <div
                        key={n.id || n.notificationId}
                        className={`p-2 rounded-lg ${!n.isRead ? 'bg-primary-fixed/20' : 'bg-surface-container/20'}`}
                      >
                        <p className="text-body-sm font-body-sm text-on-surface font-medium">{n.title}</p>
                        <p className="text-xs text-on-surface-variant line-clamp-1 mt-0.5">{n.message}</p>
                        <p className="text-[10px] text-on-surface-variant mt-1">
                          {n.createdAt ? new Date(n.createdAt).toLocaleDateString() : 'Recent'}
                        </p>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            </div>
          )}
        </div>
      </main>
    </div>
  );
}

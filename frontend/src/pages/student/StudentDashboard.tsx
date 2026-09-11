import StudentSidebar from '../../components/layout/StudentSidebar';
import TopNav from '../../components/layout/TopNav';

export default function StudentDashboard() {
  return (
    <div className="flex h-screen">
      <StudentSidebar />
      <main className="ml-64 flex-1 flex flex-col min-h-screen relative">
        <TopNav searchPlaceholder="Search campus..." />
        <div className="p-[24px] flex-1 flex flex-col gap-[32px] max-w-[1280px] mx-auto w-full">
          {/* Header */}
          <div className="flex justify-between items-end">
            <div>
              <h1 className="text-display-lg font-display-lg text-on-surface mb-2">Dashboard</h1>
              <p className="text-body-lg font-body-lg text-on-surface-variant">Welcome back. Here's your academic overview.</p>
            </div>
          </div>

          {/* Bento Grid */}
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
                    <p className="text-headline-md font-headline-md text-on-surface">Approved</p>
                  </div>
                </div>
                <span className="bg-tertiary-container/10 text-tertiary px-3 py-1 rounded-full text-label-sm font-label-sm border border-tertiary/20">
                  Fall 2024
                </span>
              </div>
              <div className="grid grid-cols-3 gap-[16px]">
                <div className="bg-surface-container/30 rounded-lg p-3 text-center">
                  <p className="text-headline-md font-headline-md text-primary">3</p>
                  <p className="text-label-sm font-label-sm text-on-surface-variant">Courses</p>
                </div>
                <div className="bg-surface-container/30 rounded-lg p-3 text-center">
                  <p className="text-headline-md font-headline-md text-tertiary">10</p>
                  <p className="text-label-sm font-label-sm text-on-surface-variant">Credits</p>
                </div>
                <div className="bg-surface-container/30 rounded-lg p-3 text-center">
                  <p className="text-headline-md font-headline-md text-on-surface">15</p>
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
                <a href="/courses" className="flex items-center gap-3 px-3 py-2.5 bg-primary/5 hover:bg-primary/10 rounded-lg transition-colors text-body-sm font-body-sm text-on-surface">
                  <span className="material-symbols-outlined text-primary text-[20px]">add_circle</span>
                  Register for Courses
                </a>
                <a href="/timetable" className="flex items-center gap-3 px-3 py-2.5 hover:bg-surface-container rounded-lg transition-colors text-body-sm font-body-sm text-on-surface-variant">
                  <span className="material-symbols-outlined text-[20px]">calendar_today</span>
                  View Timetable
                </a>
                <a href="/my-courses" className="flex items-center gap-3 px-3 py-2.5 hover:bg-surface-container rounded-lg transition-colors text-body-sm font-body-sm text-on-surface-variant">
                  <span className="material-symbols-outlined text-[20px]">list_alt</span>
                  My Courses
                </a>
              </div>
            </div>

            {/* Today's Schedule */}
            <div className="lg:col-span-2 glass-card-no-hover rounded-xl p-[16px] flex flex-col gap-[16px]">
              <div className="flex justify-between items-center">
                <h3 className="font-headline-md text-[18px] text-on-surface font-semibold flex items-center gap-2">
                  <span className="material-symbols-outlined text-primary text-[20px]">schedule</span>
                  Today's Schedule
                </h3>
                <a href="/timetable" className="text-label-sm font-label-sm text-primary hover:underline">View Full →</a>
              </div>
              <div className="space-y-3">
                {/* Schedule Item 1 */}
                <div className="flex items-center gap-4 p-3 bg-primary/5 rounded-lg border-l-4 border-primary">
                  <div className="text-center min-w-[60px]">
                    <p className="text-label-md font-label-md text-primary">10:00</p>
                    <p className="text-[10px] text-on-surface-variant">AM</p>
                  </div>
                  <div className="flex-1">
                    <p className="text-body-md font-body-md font-semibold text-on-surface">Data Structures & Algorithms</p>
                    <p className="text-label-sm font-label-sm text-on-surface-variant">Room 402, Turing Hall • COMP 301</p>
                  </div>
                </div>
                {/* Schedule Item 2 */}
                <div className="flex items-center gap-4 p-3 bg-tertiary/5 rounded-lg border-l-4 border-tertiary">
                  <div className="text-center min-w-[60px]">
                    <p className="text-label-md font-label-md text-tertiary">01:00</p>
                    <p className="text-[10px] text-on-surface-variant">PM</p>
                  </div>
                  <div className="flex-1">
                    <p className="text-body-md font-body-md font-semibold text-on-surface">Multivariable Calculus</p>
                    <p className="text-label-sm font-label-sm text-on-surface-variant">Hall B, Science Bldg • MATH 250</p>
                  </div>
                </div>
              </div>
            </div>

            {/* Notifications Preview */}
            <div className="glass-card-no-hover rounded-xl p-[16px] flex flex-col gap-3">
              <h3 className="font-headline-md text-[18px] text-on-surface font-semibold flex items-center gap-2">
                <span className="material-symbols-outlined text-primary text-[20px]">notifications</span>
                Notifications
              </h3>
              <div className="flex flex-col gap-2 flex-1">
                <div className="p-2 bg-primary-fixed/20 rounded-lg">
                  <p className="text-body-sm font-body-sm text-on-surface">Course registration is now open</p>
                  <p className="text-[10px] text-on-surface-variant mt-1">2 hours ago</p>
                </div>
                <div className="p-2 rounded-lg">
                  <p className="text-body-sm font-body-sm text-on-surface-variant">Admission approved for Fall 2024</p>
                  <p className="text-[10px] text-on-surface-variant mt-1">1 day ago</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </main>
    </div>
  );
}

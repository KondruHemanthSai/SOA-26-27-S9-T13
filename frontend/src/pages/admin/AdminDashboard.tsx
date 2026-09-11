import AdminSidebar from '../../components/layout/AdminSidebar';
import { Link } from 'react-router-dom';

export default function AdminDashboard() {
  const stats = [
    { title: 'Total Students', value: '1,420', change: '+12% this term', icon: 'groups', color: 'primary' },
    { title: 'Pending Admissions', value: '48', change: '14 urgent reviews', icon: 'description', color: 'tertiary' },
    { title: 'Active Courses', value: '86', change: '92% avg capacity', icon: 'school', color: 'primary' },
    { title: 'Course Registrations', value: '3,890', change: '+5% vs last year', icon: 'how_to_reg', color: 'tertiary' },
  ];

  const recentApplications = [
    { id: 'ADM-8921', name: 'Alex Rivera', program: 'Computer Science, B.S.', gpa: '3.85', status: 'Under Review', date: 'Today, 10:24 AM' },
    { id: 'ADM-8920', name: 'Maya Patel', program: 'Data Science & AI', gpa: '3.92', status: 'Pending Docs', date: 'Yesterday' },
    { id: 'ADM-8919', name: 'Liam Zhang', program: 'Information Systems', gpa: '3.70', status: 'Approved', date: 'Aug 18, 2024' },
  ];

  return (
    <div className="flex h-screen bg-background text-on-surface">
      <AdminSidebar />
      <main className="ml-64 flex-1 flex flex-col min-h-screen relative overflow-y-auto">
        {/* Header */}
        <header className="bg-surface/60 backdrop-blur-lg border-b border-primary/10 h-16 flex items-center justify-between px-gutter sticky top-0 z-40">
          <div className="flex items-center gap-3">
            <span className="font-headline-md text-headline-md font-semibold text-primary">Overview Dashboard</span>
          </div>
          <div className="flex items-center gap-3">
            <Link to="/admin/courses" className="px-4 py-1.5 bg-primary/10 hover:bg-primary/20 text-primary font-label-md text-sm rounded-lg transition-colors">
              Manage Courses
            </Link>
            <Link to="/admin/admissions" className="px-4 py-1.5 bg-primary text-on-primary font-label-md text-sm rounded-lg shadow-sm hover:bg-primary/90 transition-colors">
              Review Admissions
            </Link>
          </div>
        </header>

        {/* Content */}
        <div className="p-margin-mobile md:p-margin-desktop max-w-container-max mx-auto w-full flex flex-col gap-stack-lg">
          {/* Stat Cards */}
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-gutter">
            {stats.map((stat, idx) => (
              <div key={idx} className="glass-card rounded-xl p-5 flex flex-col justify-between">
                <div className="flex justify-between items-start">
                  <span className="font-label-sm text-label-sm text-on-surface-variant uppercase tracking-wider">{stat.title}</span>
                  <div className="w-9 h-9 rounded-lg bg-primary/10 text-primary flex items-center justify-center">
                    <span className="material-symbols-outlined text-[20px]">{stat.icon}</span>
                  </div>
                </div>
                <div className="mt-4">
                  <h3 className="font-display-lg text-3xl font-bold text-on-surface">{stat.value}</h3>
                  <p className="font-body-sm text-xs text-on-surface-variant mt-1">{stat.change}</p>
                </div>
              </div>
            ))}
          </div>

          {/* Quick Sections Grid */}
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-gutter">
            {/* Recent Applications Table */}
            <div className="lg:col-span-2 glass-card rounded-xl p-6 flex flex-col">
              <div className="flex justify-between items-center mb-5">
                <div>
                  <h3 className="font-headline-md text-headline-md font-semibold text-on-surface">Recent Admission Applications</h3>
                  <p className="font-body-sm text-xs text-on-surface-variant">Review and process student intake requests</p>
                </div>
                <Link to="/admin/admissions" className="text-primary font-label-sm text-xs hover:underline flex items-center gap-1">
                  View All <span className="material-symbols-outlined text-[14px]">arrow_forward</span>
                </Link>
              </div>

              <div className="overflow-x-auto">
                <table className="w-full text-left border-collapse">
                  <thead>
                    <tr className="border-b border-outline-variant/20 text-on-surface-variant font-label-sm text-xs uppercase">
                      <th className="py-3 px-3">Applicant</th>
                      <th className="py-3 px-3">Program</th>
                      <th className="py-3 px-3">GPA</th>
                      <th className="py-3 px-3">Status</th>
                      <th className="py-3 px-3 text-right">Action</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-outline-variant/10 text-body-sm text-sm">
                    {recentApplications.map((app) => (
                      <tr key={app.id} className="hover:bg-primary/5 transition-colors">
                        <td className="py-3 px-3">
                          <div className="font-semibold text-on-surface">{app.name}</div>
                          <div className="font-label-sm text-xs text-on-surface-variant">{app.id}</div>
                        </td>
                        <td className="py-3 px-3 text-on-surface-variant">{app.program}</td>
                        <td className="py-3 px-3 font-label-md">{app.gpa}</td>
                        <td className="py-3 px-3">
                          <span className={`px-2.5 py-0.5 rounded-full text-xs font-label-sm ${
                            app.status === 'Approved'
                              ? 'bg-emerald-50 text-emerald-700'
                              : app.status === 'Under Review'
                              ? 'bg-primary/10 text-primary'
                              : 'bg-secondary-container text-on-secondary-container'
                          }`}>
                            {app.status}
                          </span>
                        </td>
                        <td className="py-3 px-3 text-right">
                          <Link to="/admin/admissions" className="text-primary hover:underline font-label-sm text-xs">
                            Review
                          </Link>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>

            {/* Quick Actions & System Health */}
            <div className="glass-card rounded-xl p-6 flex flex-col justify-between">
              <div>
                <h3 className="font-headline-md text-headline-md font-semibold text-on-surface mb-2">Campus Microservices</h3>
                <p className="font-body-sm text-xs text-on-surface-variant mb-4">Spring Cloud Discovery & Health</p>
                
                <div className="space-y-3">
                  {[
                    { name: 'Auth Service', port: '8081', status: 'UP' },
                    { name: 'Student Service', port: '8082', status: 'UP' },
                    { name: 'Admission Service', port: '8083', status: 'UP' },
                    { name: 'Course Service', port: '8084', status: 'UP' },
                    { name: 'Registration Service', port: '8085', status: 'UP' },
                    { name: 'Schedule Service', port: '8086', status: 'UP' },
                  ].map((svc) => (
                    <div key={svc.name} className="flex justify-between items-center p-2 rounded-lg bg-surface-container-low/40">
                      <div className="flex items-center gap-2">
                        <span className="w-2 h-2 rounded-full bg-emerald-500"></span>
                        <span className="font-label-md text-xs">{svc.name}</span>
                      </div>
                      <span className="font-label-sm text-[11px] text-on-surface-variant">:{svc.port}</span>
                    </div>
                  ))}
                </div>
              </div>

              <div className="pt-4 border-t border-outline-variant/20 mt-4">
                <a href="http://localhost:8761" target="_blank" rel="noreferrer" className="w-full py-2 bg-surface-container text-center block rounded-lg font-label-sm text-xs text-primary hover:bg-primary/10 transition-colors">
                  Open Eureka Dashboard ↗
                </a>
              </div>
            </div>
          </div>
        </div>
      </main>
    </div>
  );
}

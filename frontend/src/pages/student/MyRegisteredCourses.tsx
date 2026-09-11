import { useState } from 'react';
import StudentSidebar from '../../components/layout/StudentSidebar';
import TopNav from '../../components/layout/TopNav';

interface Course {
  id: string;
  name: string;
  code: string;
  credits: number;
  faculty: string;
  schedule: string;
  room: string;
  status: 'Registered' | 'Waitlisted (#2)';
}

export default function MyRegisteredCourses() {
  const [courses, setCourses] = useState<Course[]>([
    {
      id: '1',
      name: 'Data Structures',
      code: 'CS401',
      credits: 4,
      faculty: 'Dr. Rao',
      schedule: 'Mon 10:00 AM',
      room: 'Room 304, Tech Bldg',
      status: 'Registered',
    },
    {
      id: '2',
      name: 'Linear Algebra',
      code: 'MATH205',
      credits: 3,
      faculty: 'Prof. Jenkins',
      schedule: 'Tue/Thu 1:00 PM',
      room: 'Science Hall A',
      status: 'Registered',
    },
    {
      id: '3',
      name: 'Intro to Psychology',
      code: 'PSYCH101',
      credits: 3,
      faculty: 'Dr. Winters',
      schedule: 'Wed 9:00 AM',
      room: 'Online Asynchronous',
      status: 'Waitlisted (#2)',
    },
  ]);

  const handleDrop = (id: string) => {
    setCourses(courses.filter((c) => c.id !== id));
  };

  const totalCredits = courses
    .filter((c) => c.status === 'Registered')
    .reduce((sum, c) => sum + c.credits, 0);

  return (
    <div className="flex h-screen bg-background text-on-background">
      <StudentSidebar />
      <main className="ml-64 flex-1 flex flex-col min-h-screen relative overflow-y-auto">
        <TopNav searchPlaceholder="Search courses, professors..." />
        
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
              <span className="font-label-md text-label-md font-medium">No timetable conflicts</span>
            </div>
          </header>

          {/* Course List Glass Panel */}
          <section className="glass-panel overflow-hidden">
            {/* Desktop Table View */}
            <div className="hidden lg:block overflow-x-auto w-full">
              <table className="w-full text-left border-collapse">
                <thead>
                  <tr className="border-b border-outline-variant/30 text-on-surface-variant bg-surface-variant/20">
                    <th className="py-4 px-6 font-label-md text-label-md font-semibold">Course</th>
                    <th className="py-4 px-6 font-label-md text-label-md font-semibold">Credits</th>
                    <th className="py-4 px-6 font-label-md text-label-md font-semibold">Faculty</th>
                    <th className="py-4 px-6 font-label-md text-label-md font-semibold">Schedule</th>
                    <th className="py-4 px-6 font-label-md text-label-md font-semibold">Status</th>
                    <th className="py-4 px-6 font-label-md text-label-md font-semibold text-right">Action</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-outline-variant/20">
                  {courses.map((course) => (
                    <tr key={course.id} className="hover:bg-primary/5 transition-colors group">
                      <td className="py-5 px-6">
                        <div className="flex flex-col">
                          <span className="font-headline-md text-headline-md text-[18px] leading-snug text-on-surface">
                            {course.name}
                          </span>
                          <span className="font-label-sm text-label-sm text-primary mt-1">{course.code}</span>
                        </div>
                      </td>
                      <td className="py-5 px-6 font-body-md text-body-md text-on-surface-variant">{course.credits}</td>
                      <td className="py-5 px-6 font-body-md text-body-md text-on-surface-variant">
                        {course.faculty}
                      </td>
                      <td className="py-5 px-6 font-body-md text-body-md text-on-surface-variant">
                        <div className="flex flex-col gap-1">
                          <span className="flex items-center gap-1">
                            <span className="material-symbols-outlined text-[16px]">schedule</span> {course.schedule}
                          </span>
                          <span className="text-[12px] text-outline">{course.room}</span>
                        </div>
                      </td>
                      <td className="py-5 px-6">
                        <span
                          className={`px-3 py-1 rounded-full font-label-sm text-label-sm inline-flex items-center gap-1 ${
                            course.status === 'Registered'
                              ? 'bg-primary-container text-on-primary-container shadow-sm'
                              : 'bg-secondary-container text-on-secondary-container'
                          }`}
                        >
                          {course.status}
                        </span>
                      </td>
                      <td className="py-5 px-6 text-right">
                        <button
                          onClick={() => handleDrop(course.id)}
                          className="opacity-0 group-hover:opacity-100 transition-opacity font-label-sm text-label-sm px-4 py-2 border border-error/30 text-error rounded-md hover:bg-error/10 hover:border-error focus:opacity-100"
                        >
                          {course.status === 'Registered' ? 'Drop Course' : 'Leave Waitlist'}
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            {/* Mobile/Tablet Stacked View */}
            <div className="lg:hidden flex flex-col divide-y divide-outline-variant/20">
              {courses.map((course) => (
                <div key={course.id} className="p-4 flex flex-col gap-3">
                  <div className="flex justify-between items-start">
                    <div>
                      <h3 className="font-headline-md text-[18px] text-on-surface">{course.name}</h3>
                      <span className="font-label-sm text-label-sm text-primary">{course.code}</span>
                    </div>
                    <span
                      className={`px-3 py-1 rounded-full font-label-sm text-xs ${
                        course.status === 'Registered'
                          ? 'bg-primary-container text-on-primary-container'
                          : 'bg-secondary-container text-on-secondary-container'
                      }`}
                    >
                      {course.status}
                    </span>
                  </div>
                  <div className="grid grid-cols-2 gap-2 text-sm text-on-surface-variant">
                    <div><span className="text-outline">Credits:</span> {course.credits}</div>
                    <div><span className="text-outline">Faculty:</span> {course.faculty}</div>
                    <div className="col-span-2 flex items-center gap-1">
                      <span className="material-symbols-outlined text-[16px]">schedule</span> {course.schedule} • {course.room}
                    </div>
                  </div>
                  <button
                    onClick={() => handleDrop(course.id)}
                    className="mt-2 w-full font-label-sm text-label-sm px-4 py-2 border border-error/30 text-error rounded-md hover:bg-error/10 transition-colors"
                  >
                    {course.status === 'Registered' ? 'Drop Course' : 'Leave Waitlist'}
                  </button>
                </div>
              ))}
            </div>
          </section>

          {/* Summary Info */}
          <div className="flex justify-end pt-2">
            <p className="font-body-md text-body-md text-on-surface-variant">
              Total Registered Credits: <strong className="text-on-surface font-semibold">{totalCredits}</strong> / 15 Max
            </p>
          </div>
        </div>
      </main>
    </div>
  );
}

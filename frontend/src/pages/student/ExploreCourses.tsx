import StudentSidebar from '../../components/layout/StudentSidebar';
import TopNav from '../../components/layout/TopNav';

export default function ExploreCourses() {
  const courses = [
    { code: 'CS 301', name: 'Advanced Data Structures', desc: 'An in-depth study of complex data structures including B-trees, red-black trees, and graph algorithms.', schedule: 'Mon/Wed 10:00 AM', seats: '24/30', color: 'tertiary-fixed', textColor: 'on-tertiary-fixed' },
    { code: 'ENG 205', name: 'Modernist Literature', desc: 'Exploring key literary works from the early 20th century, focusing on themes of fragmentation and consciousness.', schedule: 'Tue/Thu 2:00 PM', seats: '12/25', color: 'secondary-container', textColor: 'on-secondary-container' },
    { code: 'MATH 410', name: 'Applied Cryptography', desc: 'Mathematical foundations of modern cryptographic protocols and their application in secure systems.', schedule: 'Fri 9:00 AM', seats: '5/20', color: 'primary-container/20', textColor: 'primary' },
    { code: 'PHYS 301', name: 'Quantum Mechanics', desc: 'Introduction to quantum mechanics covering wave functions, Schrödinger equation, and angular momentum.', schedule: 'Tue/Thu 11:00 AM', seats: '18/35', color: 'tertiary-fixed', textColor: 'on-tertiary-fixed' },
    { code: 'CS 501', name: 'Machine Learning', desc: 'An introduction to machine learning and statistical pattern recognition. Topics include supervised and unsupervised learning.', schedule: 'Mon/Wed 2:00 PM', seats: '38/40', color: 'primary-container/20', textColor: 'primary' },
    { code: 'PSYCH 101', name: 'Intro to Psychology', desc: 'Survey course covering major topics in psychology including perception, cognition, emotion, and social behavior.', schedule: 'Wed 9:00 AM', seats: '20/45', color: 'secondary-container', textColor: 'on-secondary-container' },
  ];

  return (
    <div className="flex h-screen">
      <StudentSidebar />
      <main className="ml-64 flex-1 flex flex-col min-h-screen relative">
        <TopNav searchPlaceholder="Search courses, codes..." />
        <div className="p-[24px] flex-1 flex flex-col gap-[32px] max-w-[1280px] mx-auto w-full">
          {/* Header */}
          <div className="flex justify-between items-end">
            <div>
              <h1 className="text-display-lg font-display-lg text-on-surface mb-2">Explore Courses</h1>
              <p className="text-body-lg font-body-lg text-on-surface-variant">Fall 2024 Semester Registration</p>
            </div>
            <div className="flex gap-2">
              <button className="px-4 py-2 rounded-lg bg-surface-container border border-outline-variant/30 text-on-surface font-label-md text-label-md flex items-center gap-2">
                <span className="material-symbols-outlined text-[18px]">filter_list</span>
                Filter
              </button>
            </div>
          </div>

          {/* Course Grid */}
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-[24px]">
            {courses.map((course) => (
              <div key={course.code} className="glass-panel bg-surface/40 rounded-xl p-[16px] flex flex-col gap-[8px] hover:shadow-lg transition-shadow cursor-pointer">
                <div className="flex justify-between items-start">
                  <span className={`bg-${course.color} text-${course.textColor} font-label-sm text-label-sm px-2 py-1 rounded`}>{course.code}</span>
                  <span className="material-symbols-outlined text-outline cursor-pointer hover:text-primary transition-colors">bookmark_border</span>
                </div>
                <h3 className="font-headline-md text-[20px] text-on-surface leading-tight mt-2">{course.name}</h3>
                <p className="font-body-sm text-body-sm text-on-surface-variant line-clamp-2 mt-1">{course.desc}</p>
                <div className="mt-auto pt-[8px] flex items-center gap-3 text-on-surface-variant font-label-sm text-label-sm">
                  <div className="flex items-center gap-1">
                    <span className="material-symbols-outlined text-[16px]">schedule</span>
                    {course.schedule}
                  </div>
                  <div className="flex items-center gap-1">
                    <span className="material-symbols-outlined text-[16px]">group</span>
                    {course.seats} Seats
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>
      </main>
    </div>
  );
}

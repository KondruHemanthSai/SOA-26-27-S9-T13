import { Link } from 'react-router-dom';
import AdminSidebar from '../../components/layout/AdminSidebar';

export default function AdminCourseManagement() {
  const courses = [
    {
      code: 'CS501',
      deptCode: 'CS',
      name: 'Machine Learning',
      department: 'Computer Science',
      credits: 4,
      capacity: '38/40',
      percent: '95%',
      status: 'Full',
      isFull: true,
    },
    {
      code: 'CS401',
      deptCode: 'CS',
      name: 'Data Structures',
      department: 'Computer Science',
      credits: 4,
      capacity: '32/40',
      percent: '80%',
      status: 'Open',
      isFull: false,
    },
    {
      code: 'MATH201',
      deptCode: 'MAT',
      name: 'Linear Algebra',
      department: 'Mathematics',
      credits: 3,
      capacity: '15/50',
      percent: '30%',
      status: 'Open',
      isFull: false,
    },
    {
      code: 'ENG210',
      deptCode: 'ENG',
      name: 'Modern World Literature',
      department: 'Humanities',
      credits: 3,
      capacity: '45/45',
      percent: '100%',
      status: 'Full',
      isFull: true,
    },
  ];

  return (
    <div className="font-body-md text-body-md bg-background text-on-surface antialiased overflow-x-hidden min-h-screen flex flex-col md:flex-row">
      <AdminSidebar />

      <main className="flex-1 w-full md:pl-64 flex flex-col min-h-screen">
        {/* TopNavBar */}
        <header className="bg-surface/60 backdrop-blur-lg text-primary font-body-md text-body-md fixed top-0 w-full z-40 border-b border-primary/10 shadow-sm flex justify-between items-center px-gutter h-16 md:w-[calc(100%-16rem)]">
          <div className="hidden md:flex flex-1 items-center max-w-md relative mr-4">
            <span className="material-symbols-outlined absolute left-3 text-on-surface-variant text-[20px] pointer-events-none">search</span>
            <input
              className="w-full bg-surface-container-low border-none rounded-full py-2 pl-10 pr-4 text-sm focus:ring-2 focus:ring-primary/20 transition-shadow outline-none placeholder:text-on-surface-variant/60"
              placeholder="Search courses, students..."
              type="text"
            />
          </div>

          <div className="flex items-center gap-2">
            <button className="p-2 text-on-surface-variant hover:bg-surface-container transition-colors rounded-full relative">
              <span className="material-symbols-outlined">notifications</span>
              <span className="absolute top-2 right-2 w-2 h-2 bg-error rounded-full"></span>
            </button>
            <button className="p-2 text-on-surface-variant hover:bg-surface-container transition-colors rounded-full mr-2">
              <span className="material-symbols-outlined">settings</span>
            </button>
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

          {/* Controls & Filters Bar */}
          <div className="glass-card rounded-xl p-4 mb-stack-md flex flex-col lg:flex-row gap-4 items-start lg:items-center justify-between shadow-sm">
            <div className="relative w-full lg:w-96">
              <span className="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-on-surface-variant">search</span>
              <input
                className="w-full bg-surface/50 border border-outline-variant/50 rounded-lg py-2 pl-10 pr-4 text-sm focus:border-primary focus:ring-1 focus:ring-primary transition-colors outline-none"
                placeholder="Search by course name or code..."
                type="text"
              />
            </div>
            <div className="flex flex-wrap items-center gap-2 w-full lg:w-auto">
              <button className="flex items-center gap-2 px-3 py-1.5 bg-surface-container-low border border-outline-variant/30 rounded-lg text-sm font-label-md hover:bg-surface-container transition-colors">
                Department <span className="material-symbols-outlined text-[16px]">expand_more</span>
              </button>
              <button className="flex items-center gap-2 px-3 py-1.5 bg-surface-container-low border border-outline-variant/30 rounded-lg text-sm font-label-md hover:bg-surface-container transition-colors">
                Semester <span className="material-symbols-outlined text-[16px]">expand_more</span>
              </button>
              <button className="flex items-center gap-2 px-3 py-1.5 bg-surface-container-low border border-outline-variant/30 rounded-lg text-sm font-label-md hover:bg-surface-container transition-colors">
                Status <span className="material-symbols-outlined text-[16px]">expand_more</span>
              </button>
            </div>
          </div>

          {/* Data Table Card */}
          <div className="glass-card rounded-xl shadow-sm overflow-hidden flex flex-col">
            <div className="overflow-x-auto w-full">
              <table className="w-full text-left border-collapse min-w-[800px]">
                <thead>
                  <tr className="border-b border-outline-variant/20 bg-surface-container-lowest/50 text-on-surface-variant font-label-sm text-label-sm uppercase tracking-wider">
                    <th className="py-4 px-6 font-semibold">Course</th>
                    <th className="py-4 px-6 font-semibold">Department</th>
                    <th className="py-4 px-6 font-semibold">Credits</th>
                    <th className="py-4 px-6 font-semibold">Capacity</th>
                    <th className="py-4 px-6 font-semibold">Status</th>
                    <th className="py-4 px-6 font-semibold text-right">Actions</th>
                  </tr>
                </thead>
                <tbody className="font-body-sm text-body-sm divide-y divide-outline-variant/10">
                  {courses.map((c) => (
                    <tr key={c.code} className="table-row-hover transition-colors group">
                      <td className="py-4 px-6">
                        <div className="flex items-center gap-3">
                          <div className="w-10 h-10 rounded-lg bg-primary/10 flex items-center justify-center text-primary font-label-md text-xs font-bold">
                            {c.deptCode}
                          </div>
                          <div>
                            <div className="font-semibold text-on-surface">{c.name}</div>
                            <div className="font-label-sm text-on-surface-variant mt-0.5">{c.code}</div>
                          </div>
                        </div>
                      </td>
                      <td className="py-4 px-6 text-on-surface-variant">{c.department}</td>
                      <td className="py-4 px-6 text-on-surface-variant font-label-md">{c.credits}</td>
                      <td className="py-4 px-6">
                        <div className="flex items-center gap-3">
                          <div className="w-24 h-2 bg-surface-container-high rounded-full overflow-hidden">
                            <div
                              className={`h-full ${c.isFull ? 'bg-error' : 'bg-tertiary-container'}`}
                              style={{ width: c.percent }}
                            ></div>
                          </div>
                          <span className="font-label-md text-xs text-on-surface-variant w-10 text-right">{c.capacity}</span>
                        </div>
                      </td>
                      <td className="py-4 px-6">
                        <span
                          className={`inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full font-label-sm text-xs ${
                            c.isFull ? 'bg-error/10 text-error' : 'bg-tertiary-container/10 text-tertiary'
                          }`}
                        >
                          <span className={`w-1.5 h-1.5 rounded-full ${c.isFull ? 'bg-error' : 'bg-tertiary-container'}`}></span>
                          {c.status}
                        </span>
                      </td>
                      <td className="py-4 px-6 text-right">
                        <div className="flex items-center justify-end gap-1 opacity-0 group-hover:opacity-100 transition-opacity">
                          <Link
                            to={`/admin/courses/${c.code}`}
                            className="p-1.5 text-on-surface-variant hover:text-primary hover:bg-primary/10 rounded-md transition-colors"
                            title="Edit / Prerequisites"
                          >
                            <span className="material-symbols-outlined text-[20px]">account_tree</span>
                          </Link>
                          <Link
                            to={`/admin/courses/${c.code}`}
                            className="p-1.5 text-on-surface-variant hover:text-primary hover:bg-primary/10 rounded-md transition-colors"
                            title="Edit"
                          >
                            <span className="material-symbols-outlined text-[20px]">edit</span>
                          </Link>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            {/* Table Pagination Footer */}
            <div className="border-t border-outline-variant/20 bg-surface-container-lowest/30 px-6 py-3 flex items-center justify-between mt-auto">
              <span className="text-sm text-on-surface-variant font-medium">Showing 1 to 4 of 4 entries</span>
              <div className="flex items-center gap-1">
                <button className="w-8 h-8 flex items-center justify-center rounded bg-primary text-on-primary font-label-md text-sm">
                  1
                </button>
              </div>
            </div>
          </div>
        </div>
      </main>
    </div>
  );
}

import StudentSidebar from '../../components/layout/StudentSidebar';
import TopNav from '../../components/layout/TopNav';

export default function WeeklyTimetable() {
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
              No timetable conflicts
            </div>
          </div>

          {/* Timetable Grid */}
          <div className="bg-surface-container-lowest rounded-xl border border-outline-variant/60 shadow-sm overflow-hidden flex flex-col">
            {/* Grid Header (Days) */}
            <div className="grid grid-cols-[60px_repeat(5,1fr)] md:grid-cols-[80px_repeat(5,1fr)] border-b border-outline-variant/60 bg-surface-container/30">
              <div className="p-4"></div>
              <div className="p-4 text-center font-label-md text-label-md text-on-surface-variant font-medium border-l border-outline-variant/30">Monday</div>
              <div className="p-4 text-center font-label-md text-label-md text-on-surface-variant font-medium border-l border-outline-variant/30">Tuesday</div>
              <div className="p-4 text-center font-label-md text-label-md text-on-surface-variant font-medium border-l border-outline-variant/30">Wednesday</div>
              <div className="p-4 text-center font-label-md text-label-md text-on-surface-variant font-medium border-l border-outline-variant/30">Thursday</div>
              <div className="p-4 text-center font-label-md text-label-md text-on-surface-variant font-medium border-l border-outline-variant/30">Friday</div>
            </div>

            {/* Grid Body */}
            <div className="relative grid grid-cols-[60px_repeat(5,1fr)] md:grid-cols-[80px_repeat(5,1fr)] grid-rows-8 h-[800px] bg-[linear-gradient(to_bottom,var(--color-surface-variant)_1px,transparent_1px)] bg-[size:100%_100px]">
              {/* Time Column */}
              <div className="row-span-8 flex flex-col border-r border-outline-variant/60 bg-surface-container-lowest z-10">
                <div className="h-[100px] flex items-start justify-end pr-3 pt-2 font-label-sm text-label-sm text-secondary">09:00</div>
                <div className="h-[100px] flex items-start justify-end pr-3 pt-2 font-label-sm text-label-sm text-secondary">10:00</div>
                <div className="h-[100px] flex items-start justify-end pr-3 pt-2 font-label-sm text-label-sm text-secondary">11:00</div>
                <div className="h-[100px] flex items-start justify-end pr-3 pt-2 font-label-sm text-label-sm text-secondary">12:00</div>
                <div className="h-[100px] flex items-start justify-end pr-3 pt-2 font-label-sm text-label-sm text-secondary">13:00</div>
                <div className="h-[100px] flex items-start justify-end pr-3 pt-2 font-label-sm text-label-sm text-secondary">14:00</div>
                <div className="h-[100px] flex items-start justify-end pr-3 pt-2 font-label-sm text-label-sm text-secondary">15:00</div>
                <div className="h-[100px] flex items-start justify-end pr-3 pt-2 font-label-sm text-label-sm text-secondary">16:00</div>
              </div>

              {/* Dividers */}
              <div className="col-start-2 row-span-8 border-l border-outline-variant/30"></div>
              <div className="col-start-3 row-span-8 border-l border-outline-variant/30"></div>
              <div className="col-start-4 row-span-8 border-l border-outline-variant/30"></div>
              <div className="col-start-5 row-span-8 border-l border-outline-variant/30"></div>
              <div className="col-start-6 row-span-8 border-l border-outline-variant/30"></div>

              {/* Course Block 1: Monday 10-12 */}
              <div className="absolute col-start-2 top-[100px] h-[200px] w-full p-1 z-20">
                <div className="h-full w-full bg-primary/10 backdrop-blur-md border border-primary/20 rounded-lg p-3 flex flex-col hover:shadow-md hover:scale-[1.02] transition-all cursor-pointer group">
                  <span className="font-label-sm text-label-sm text-primary mb-1">COMP 301</span>
                  <h3 className="font-body-sm text-body-sm font-semibold text-on-surface leading-tight">Data Structures & Algorithms</h3>
                  <div className="mt-auto flex items-center gap-1 font-label-sm text-label-sm text-secondary group-hover:text-primary transition-colors">
                    <span className="material-symbols-outlined text-[14px]">location_on</span>
                    Room 402, Turing Hall
                  </div>
                </div>
              </div>

              {/* Course Block 2: Tuesday 9-11 */}
              <div className="absolute col-start-3 top-[0px] h-[200px] w-full p-1 z-20">
                <div className="h-full w-full bg-tertiary/10 backdrop-blur-md border border-tertiary/20 rounded-lg p-3 flex flex-col hover:shadow-md hover:scale-[1.02] transition-all cursor-pointer group">
                  <span className="font-label-sm text-label-sm text-tertiary mb-1">MATH 250</span>
                  <h3 className="font-body-sm text-body-sm font-semibold text-on-surface leading-tight">Multivariable Calculus</h3>
                  <div className="mt-auto flex items-center gap-1 font-label-sm text-label-sm text-secondary group-hover:text-tertiary transition-colors">
                    <span className="material-symbols-outlined text-[14px]">location_on</span>
                    Hall B, Science Bldg
                  </div>
                </div>
              </div>

              {/* Course Block 3: Wednesday 13-15 */}
              <div className="absolute col-start-4 top-[400px] h-[200px] w-full p-1 z-20">
                <div className="h-full w-full bg-secondary-container/30 backdrop-blur-md border border-secondary-fixed/50 rounded-lg p-3 flex flex-col hover:shadow-md hover:scale-[1.02] transition-all cursor-pointer group">
                  <span className="font-label-sm text-label-sm text-on-secondary-container mb-1">PHIL 101</span>
                  <h3 className="font-body-sm text-body-sm font-semibold text-on-surface leading-tight">Intro to Ethics</h3>
                  <div className="mt-auto flex items-center gap-1 font-label-sm text-label-sm text-secondary group-hover:text-on-secondary-container transition-colors">
                    <span className="material-symbols-outlined text-[14px]">location_on</span>
                    Seminar Room 3
                  </div>
                </div>
              </div>

              {/* Course Block 4: Thursday 14-16 */}
              <div className="absolute col-start-5 top-[500px] h-[200px] w-full p-1 z-20">
                <div className="h-full w-full bg-primary/10 backdrop-blur-md border border-primary/20 rounded-lg p-3 flex flex-col hover:shadow-md hover:scale-[1.02] transition-all cursor-pointer group">
                  <span className="font-label-sm text-label-sm text-primary mb-1">COMP 301L</span>
                  <h3 className="font-body-sm text-body-sm font-semibold text-on-surface leading-tight">Data Structures Lab</h3>
                  <div className="mt-auto flex items-center gap-1 font-label-sm text-label-sm text-secondary group-hover:text-primary transition-colors">
                    <span className="material-symbols-outlined text-[14px]">computer</span>
                    Lab 2, Turing Hall
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </main>
    </div>
  );
}

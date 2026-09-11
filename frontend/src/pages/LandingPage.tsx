import { Link } from 'react-router-dom';

export default function LandingPage() {
  return (
    <div className="antialiased min-h-screen flex flex-col relative overflow-x-hidden">
      {/* Navigation */}
      <header className="bg-surface/60 backdrop-blur-xl sticky top-0 z-50 border-b border-primary/10 shadow-sm flex justify-between items-center w-full px-[40px] h-16">
        <div className="flex items-center gap-[8px]">
          <span className="material-symbols-outlined text-primary text-[28px]" style={{ fontVariationSettings: "'FILL' 1" }}>school</span>
          <span className="text-headline-md font-headline-md font-bold text-primary">Student Central</span>
        </div>
        {/* Desktop Nav Links */}
        <nav className="hidden md:flex gap-[24px]">
          <a className="text-on-surface-variant hover:bg-primary/5 transition-colors px-3 py-2 rounded-md text-label-md font-label-md" href="#">Overview</a>
          <a className="text-on-surface-variant hover:bg-primary/5 transition-colors px-3 py-2 rounded-md text-label-md font-label-md" href="#">Admission</a>
          <a className="text-on-surface-variant hover:bg-primary/5 transition-colors px-3 py-2 rounded-md text-label-md font-label-md" href="#">Courses</a>
        </nav>
        <div className="flex items-center gap-[16px]">
          <div className="hidden md:flex items-center gap-[8px] text-on-surface-variant">
            <span className="material-symbols-outlined cursor-pointer hover:bg-primary/5 p-2 rounded-full transition-colors">search</span>
            <span className="material-symbols-outlined cursor-pointer hover:bg-primary/5 p-2 rounded-full transition-colors">notifications</span>
            <span className="material-symbols-outlined cursor-pointer hover:bg-primary/5 p-2 rounded-full transition-colors">help</span>
          </div>
          <Link to="/login" className="bg-primary text-on-primary px-4 py-2 rounded-lg font-label-md text-label-md hover:bg-primary-container hover:text-on-primary-container transition-colors shadow-sm">
            Log In
          </Link>
        </div>
      </header>

      <main className="flex-grow">
        {/* Hero Section */}
        <section className="relative pt-24 pb-32 px-[16px] md:px-[40px] max-w-[1280px] mx-auto overflow-hidden flex flex-col lg:flex-row items-center gap-[32px]">
          {/* Decorative Background */}
          <div className="absolute top-0 left-0 w-full h-full overflow-hidden -z-10 pointer-events-none">
            <div className="absolute top-[-20%] left-[-10%] w-[50vw] h-[50vw] rounded-full bg-primary/5 blur-[100px]"></div>
            <div className="absolute bottom-[-20%] right-[-10%] w-[40vw] h-[40vw] rounded-full bg-tertiary-container/5 blur-[100px]"></div>
          </div>

          <div className="w-full lg:w-1/2 flex flex-col gap-[16px] relative z-10 text-center lg:text-left">
            <h1 className="text-display-lg font-display-lg text-on-surface glow-text">
              Your campus.<br />
              <span className="text-primary">One intelligent space.</span>
            </h1>
            <p className="text-body-lg font-body-lg text-on-surface-variant max-w-xl mx-auto lg:mx-0">
              From admission to course registration, manage your entire academic journey from one centralized platform. Designed for modern students and forward-thinking faculties.
            </p>
            <div className="flex flex-col sm:flex-row gap-[8px] justify-center lg:justify-start mt-[16px]">
              <Link to="/register" className="bg-primary text-on-primary px-6 py-3 rounded-lg font-label-md text-label-md hover:shadow-[0_0_15px_rgba(70,72,212,0.4)] transition-all transform hover:-translate-y-0.5">
                Get Started
              </Link>
              <Link to="/courses" className="bg-transparent border border-primary/20 text-primary px-6 py-3 rounded-lg font-label-md text-label-md hover:bg-primary/5 transition-all">
                Explore Courses
              </Link>
            </div>
          </div>

          {/* Hero Visual - Floating Dashboard */}
          <div className="w-full lg:w-1/2 relative z-10" style={{ perspective: '1000px' }}>
            <div className="glass-card rounded-xl p-[16px] relative shadow-2xl" style={{ transform: 'rotateY(-10deg) rotateX(5deg)' }}>
              {/* Mock Top Bar */}
              <div className="flex justify-between items-center border-b border-primary/10 pb-[8px] mb-[16px]">
                <div className="flex gap-2">
                  <div className="w-3 h-3 rounded-full bg-error/50"></div>
                  <div className="w-3 h-3 rounded-full bg-tertiary-container/50"></div>
                  <div className="w-3 h-3 rounded-full bg-primary/50"></div>
                </div>
                <div className="bg-surface-container-high rounded-full px-4 py-1 flex items-center gap-2">
                  <span className="material-symbols-outlined text-sm">search</span>
                  <span className="text-label-sm font-label-sm text-on-surface-variant">Search campus...</span>
                </div>
              </div>
              {/* Mock Content Grid */}
              <div className="grid grid-cols-2 gap-[8px]">
                {/* Admission Status */}
                <div className="col-span-2 glass-card-no-hover rounded-lg p-[8px] flex items-center justify-between">
                  <div className="flex items-center gap-[8px]">
                    <div className="w-10 h-10 rounded-full bg-primary/10 flex items-center justify-center text-primary">
                      <span className="material-symbols-outlined">how_to_reg</span>
                    </div>
                    <div>
                      <p className="text-label-sm font-label-sm text-on-surface-variant">Admission Status</p>
                      <p className="text-body-md font-body-md font-semibold text-on-surface">Approved</p>
                    </div>
                  </div>
                  <span className="bg-tertiary-container/10 text-tertiary px-2 py-1 rounded text-[10px] font-label-sm">Fall 2024</span>
                </div>
                {/* Course Cards */}
                <div className="glass-card-no-hover rounded-lg p-[8px]">
                  <p className="text-label-sm font-label-sm text-on-surface-variant mb-2">CS 101</p>
                  <p className="text-body-sm font-body-sm font-semibold mb-1">Intro to Programming</p>
                  <div className="w-full bg-surface-container h-1.5 rounded-full overflow-hidden">
                    <div className="bg-primary w-[75%] h-full rounded-full"></div>
                  </div>
                </div>
                <div className="glass-card-no-hover rounded-lg p-[8px]">
                  <p className="text-label-sm font-label-sm text-on-surface-variant mb-2">MATH 201</p>
                  <p className="text-body-sm font-body-sm font-semibold mb-1">Calculus II</p>
                  <div className="w-full bg-surface-container h-1.5 rounded-full overflow-hidden">
                    <div className="bg-tertiary-container w-[40%] h-full rounded-full"></div>
                  </div>
                </div>
                {/* Timetable Preview */}
                <div className="col-span-2 glass-card-no-hover rounded-lg p-[8px] mt-[8px]">
                  <p className="text-label-sm font-label-sm text-on-surface-variant mb-2 flex items-center gap-1">
                    <span className="material-symbols-outlined text-[14px]">calendar_month</span> Today's Schedule
                  </p>
                  <div className="space-y-2">
                    <div className="flex items-center gap-2 text-body-sm font-body-sm">
                      <div className="w-2 h-2 rounded-full bg-primary"></div>
                      <span className="text-on-surface-variant w-16">09:00 AM</span>
                      <span className="text-on-surface bg-primary/5 px-2 py-0.5 rounded">CS 101 Lecture</span>
                    </div>
                    <div className="flex items-center gap-2 text-body-sm font-body-sm">
                      <div className="w-2 h-2 rounded-full bg-tertiary-container"></div>
                      <span className="text-on-surface-variant w-16">11:30 AM</span>
                      <span className="text-on-surface bg-tertiary-container/5 px-2 py-0.5 rounded">MATH 201 Lab</span>
                    </div>
                  </div>
                </div>
              </div>
              {/* Floating element */}
              <div className="absolute -right-6 -top-6 glass-card-no-hover p-3 rounded-xl shadow-lg flex items-center gap-2 animate-bounce" style={{ animationDuration: '3s' }}>
                <span className="material-symbols-outlined text-primary">check_circle</span>
                <span className="text-label-sm font-label-sm font-semibold">Registration Open</span>
              </div>
            </div>
          </div>
        </section>
      </main>
    </div>
  );
}

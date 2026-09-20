import { Link, useLocation } from 'react-router-dom';

export default function RegistrationSuccess() {
  const location = useLocation();
  const state = location.state as { courseCode?: string; courseName?: string } | undefined;
  const courseText = state?.courseName
    ? `${state.courseCode ? state.courseCode + ' - ' : ''}${state.courseName}`
    : 'Your selected course';
  return (
    <div className="bg-background text-on-background min-h-screen flex items-center justify-center p-margin-mobile md:p-margin-desktop relative overflow-hidden font-sans">
      {/* Animated Glassmorphism Background Blobs */}
      <div className="absolute inset-0 z-0 overflow-hidden pointer-events-none">
        <div className="absolute top-1/4 left-1/4 w-[500px] h-[500px] bg-primary-fixed rounded-full mix-blend-multiply filter blur-[100px] opacity-60 animate-blob"></div>
        <div className="absolute top-1/3 right-1/4 w-[400px] h-[400px] bg-secondary-fixed rounded-full mix-blend-multiply filter blur-[100px] opacity-50 animate-blob animation-delay-2000"></div>
        <div className="absolute -bottom-32 left-1/3 w-[600px] h-[600px] bg-tertiary-fixed rounded-full mix-blend-multiply filter blur-[120px] opacity-40 animate-blob animation-delay-4000"></div>
      </div>

      {/* Main Content Container */}
      <main className="relative z-10 w-full max-w-[480px]">
        {/* Glassmorphism Success Card */}
        <div className="bg-surface/70 backdrop-blur-2xl border border-primary/10 shadow-[0_20px_50px_rgba(0,0,0,0.05)] rounded-2xl p-stack-lg md:p-[40px] flex flex-col items-center text-center relative overflow-hidden">
          {/* Subtle Top Glow */}
          <div className="absolute top-0 left-0 w-full h-1 bg-gradient-to-r from-transparent via-primary to-transparent opacity-30"></div>

          {/* Success Icon */}
          <div className="w-24 h-24 rounded-full bg-primary-container/20 flex items-center justify-center mb-stack-md relative shadow-inner">
            <div className="absolute inset-0 rounded-full border border-primary/20"></div>
            <span className="material-symbols-outlined text-[64px] text-primary" style={{ fontVariationSettings: "'FILL' 1" }}>
              check_circle
            </span>
          </div>

          {/* Typography */}
          <h1 className="font-headline-lg text-headline-lg text-on-surface mb-stack-sm tracking-tight">
            Course registered successfully 🎉
          </h1>
          <p className="font-body-md text-body-md text-on-surface-variant mb-stack-lg px-4">
            <span className="font-bold text-on-surface">{courseText}</span> has been added to your semester schedule.
          </p>

          {/* Actions */}
          <div className="flex flex-col w-full gap-stack-sm mt-auto">
            <Link
              to="/timetable"
              className="w-full h-12 bg-primary text-on-primary font-label-md text-label-md rounded-lg shadow-sm hover:bg-primary/90 hover:shadow-md hover:-translate-y-0.5 transition-all flex items-center justify-center gap-2 relative overflow-hidden group"
            >
              <span className="material-symbols-outlined text-[20px]">calendar_month</span>
              <span>View Timetable</span>
            </Link>
            <Link
              to="/courses"
              className="w-full h-12 bg-transparent border border-outline-variant text-on-surface font-label-md text-label-md rounded-lg hover:bg-surface-variant/50 hover:border-outline transition-colors flex items-center justify-center gap-2"
            >
              <span className="material-symbols-outlined text-[20px]">explore</span>
              <span>Continue Exploring</span>
            </Link>
          </div>
        </div>
      </main>
    </div>
  );
}

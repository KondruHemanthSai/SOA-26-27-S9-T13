import { Link, useLocation } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';

const navItems = [
  { path: '/dashboard', icon: 'dashboard', label: 'Overview' },
  { path: '/admission/apply', icon: 'school', label: 'Admission' },
  { path: '/courses', icon: 'library_books', label: 'Courses' },
  { path: '/timetable', icon: 'calendar_today', label: 'Timetable' },
  { path: '/my-courses', icon: 'folder', label: 'My Courses' },
];

export default function StudentSidebar() {
  const location = useLocation();
  const { logout } = useAuth();

  return (
    <nav className="hidden md:flex flex-col h-screen w-64 fixed left-0 top-0 z-50 border-r border-primary/5 shadow-2xl bg-surface-container-low/40 backdrop-blur-2xl p-[16px] gap-[8px]">
      {/* Brand */}
      <div className="flex items-center gap-3 mb-[32px] px-2 pt-2">
        <div className="w-8 h-8 rounded-sm bg-primary text-on-primary flex items-center justify-center font-bold text-sm">SC</div>
        <div className="flex flex-col">
          <span className="font-headline-lg text-[18px] font-black tracking-tighter text-primary leading-none">Student Central</span>
          <span className="text-[10px] text-on-surface-variant uppercase tracking-widest mt-1">University Portal</span>
        </div>
      </div>

      {/* Navigation Links */}
      <div className="flex flex-col gap-1 flex-1">
        {navItems.map((item) => {
          const isActive = location.pathname === item.path;
          return (
            <Link
              key={item.path}
              to={item.path}
              className={`flex items-center gap-3 px-3 py-2 rounded-lg transition-all duration-200 font-label-md text-label-md ${
                isActive
                  ? 'bg-primary-container/80 text-on-primary-container shadow-inner glow-active'
                  : 'text-on-surface-variant hover:text-primary hover:bg-secondary-container/30 hover:translate-x-1'
              }`}
            >
              <span
                className="material-symbols-outlined"
                style={isActive ? { fontVariationSettings: "'FILL' 1" } : {}}
              >
                {item.icon}
              </span>
              {item.label}
            </Link>
          );
        })}
      </div>

      {/* Footer */}
      <div className="mt-auto flex flex-col gap-[8px] pt-[16px] border-t border-outline-variant/20">
        <button className="w-full bg-secondary-container/50 hover:bg-secondary-container text-on-surface-variant font-label-md text-label-md py-2 rounded-lg transition-colors border border-primary/5 flex items-center justify-center gap-2">
          <span className="material-symbols-outlined text-[18px]">help</span>
          Help Center
        </button>
        <button
          onClick={logout}
          className="flex items-center gap-3 px-3 py-2 text-on-surface-variant hover:text-primary hover:bg-secondary-container/30 rounded-lg transition-all hover:translate-x-1 duration-200 font-label-md text-label-md w-full"
        >
          <span className="material-symbols-outlined">logout</span>
          Logout
        </button>
      </div>
    </nav>
  );
}

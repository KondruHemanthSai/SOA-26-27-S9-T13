import { Link, useLocation } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';

const navItems = [
  { path: '/admin/dashboard', icon: 'dashboard', label: 'Overview' },
  { path: '/admin/admissions', icon: 'description', label: 'Applications' },
  { path: '/admin/courses', icon: 'school', label: 'Courses' },
];

export default function AdminSidebar() {
  const location = useLocation();
  const { logout } = useAuth();

  return (
    <aside className="hidden md:flex flex-col fixed left-0 top-0 h-full w-64 z-50 bg-surface-container-low/40 backdrop-blur-2xl border-r border-primary/5 shadow-2xl p-4 gap-2">
      {/* Header */}
      <div className="flex items-center gap-4 mb-[32px] px-2 pt-2">
        <div className="w-10 h-10 rounded-full bg-primary/10 flex items-center justify-center text-primary">
          <span className="material-symbols-outlined" style={{ fontVariationSettings: "'FILL' 1" }}>admin_panel_settings</span>
        </div>
        <div>
          <h1 className="font-headline-md text-body-md text-primary font-semibold">Portal Admin</h1>
          <p className="font-label-sm text-label-sm text-on-surface-variant opacity-80 mt-1">Fall Semester 2024</p>
        </div>
      </div>

      {/* CTA Button */}
      <button className="w-full bg-primary text-on-primary hover:opacity-90 transition-opacity rounded-lg py-3 px-4 font-semibold flex items-center justify-center gap-2 shadow-sm mb-[16px] font-label-md text-label-md">
        <span className="material-symbols-outlined text-[18px]">add</span>
        New Application
      </button>

      {/* Navigation */}
      <nav className="flex-1 flex flex-col gap-1 overflow-y-auto">
        {navItems.map((item) => {
          const isActive = location.pathname === item.path || location.pathname.startsWith(item.path + '/');
          return (
            <Link
              key={item.path}
              to={item.path}
              className={`flex items-center gap-3 px-4 py-3 rounded-lg transition-all hover:translate-x-1 font-label-md text-label-md ${
                isActive
                  ? 'bg-primary/10 text-primary font-semibold'
                  : 'text-on-secondary-container hover:bg-surface-container-high'
              }`}
            >
              <span className="material-symbols-outlined" style={isActive ? { fontVariationSettings: "'FILL' 1" } : {}}>{item.icon}</span>
              {item.label}
            </Link>
          );
        })}
      </nav>

      {/* Footer */}
      <div className="mt-auto pt-4 border-t border-primary/5 flex flex-col gap-1">
        <a className="flex items-center gap-3 text-on-secondary-container px-4 py-3 hover:bg-surface-container-high transition-all hover:translate-x-1 rounded-lg font-label-md text-label-md" href="#">
          <span className="material-symbols-outlined">help_outline</span>
          Support
        </a>
        <button
          onClick={logout}
          className="flex items-center gap-3 text-on-secondary-container px-4 py-3 hover:bg-surface-container-high transition-all hover:translate-x-1 rounded-lg font-label-md text-label-md w-full text-left"
        >
          <span className="material-symbols-outlined">logout</span>
          Sign Out
        </button>
      </div>
    </aside>
  );
}

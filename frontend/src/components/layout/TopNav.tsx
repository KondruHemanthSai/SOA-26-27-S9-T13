import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { notificationApi } from '../../services/api';
import type { NotificationItem } from '../../types/api';

interface TopNavProps {
  searchPlaceholder?: string;
  profileImageUrl?: string;
}

export default function TopNav({ searchPlaceholder = 'Search...', profileImageUrl }: TopNavProps) {
  const { user } = useAuth();
  const [unreadCount, setUnreadCount] = useState<number>(0);
  const [showDropdown, setShowDropdown] = useState(false);
  const [quickNotifications, setQuickNotifications] = useState<NotificationItem[]>([]);

  useEffect(() => {
    let isMounted = true;
    const loadUnreadCount = async () => {
      try {
        const count = await notificationApi.getUnreadCount();
        if (isMounted) setUnreadCount(count);
      } catch {
        // silent fail on notification count
      }
    };

    loadUnreadCount();
    // Refresh unread count periodically every 60s
    const interval = setInterval(loadUnreadCount, 60000);
    return () => {
      isMounted = false;
      clearInterval(interval);
    };
  }, []);

  const handleToggleDropdown = async () => {
    const nextState = !showDropdown;
    setShowDropdown(nextState);
    if (nextState) {
      try {
        const unread = await notificationApi.getUnreadNotifications();
        setQuickNotifications(unread.slice(0, 5));
      } catch {
        // ignore
      }
    }
  };

  const initial = user?.name ? user.name.charAt(0).toUpperCase() : 'U';

  return (
    <header className="bg-surface/60 backdrop-blur-xl font-body-md text-body-md w-full sticky top-0 z-40 border-b border-primary/10 shadow-sm flex justify-between items-center px-[24px] py-[8px]">
      <div className="flex items-center gap-[16px]">
        {/* Search Bar */}
        <div className="relative w-64 group">
          <span className="material-symbols-outlined absolute left-3 top-1/2 transform -translate-y-1/2 text-on-surface-variant/50 group-focus-within:text-primary transition-colors text-[20px]">
            search
          </span>
          <input
            className="w-full bg-surface-container-low border-none rounded-full py-2 pl-10 pr-4 font-body-sm text-body-sm text-on-surface placeholder:text-on-surface-variant/50 focus:ring-1 focus:ring-primary focus:bg-surface transition-all outline-none"
            placeholder={searchPlaceholder}
            type="text"
          />
        </div>
      </div>

      <div className="flex items-center gap-[16px] relative">
        {/* Campus AI Link for Students */}
        {user?.role === 'STUDENT' && (
          <Link
            to="/student/ai-assistant"
            className="flex items-center gap-1.5 px-3 py-1.5 rounded-full bg-primary/10 text-primary hover:bg-primary/20 transition-all text-xs font-semibold"
            title="Campus AI Assistant"
          >
            <span className="material-symbols-outlined text-[18px]">smart_toy</span>
            <span className="hidden sm:inline">Ask AI</span>
          </Link>
        )}

        {/* Notification Bell */}
        <div className="relative">
          <button
            onClick={handleToggleDropdown}
            className="w-10 h-10 rounded-full flex items-center justify-center text-on-surface-variant hover:bg-primary-container/10 transition-colors relative"
            title="Notifications"
          >
            <span className="material-symbols-outlined">notifications</span>
            {unreadCount > 0 && (
              <span className="absolute top-1.5 right-1.5 min-w-[18px] h-[18px] px-1 bg-primary text-on-primary text-[10px] font-bold rounded-full flex items-center justify-center">
                {unreadCount > 99 ? '99+' : unreadCount}
              </span>
            )}
          </button>

          {/* Quick Notifications Dropdown */}
          {showDropdown && (
            <div className="absolute right-0 mt-2 w-80 bg-surface rounded-2xl shadow-2xl border border-primary/10 py-3 z-50 overflow-hidden">
              <div className="flex justify-between items-center px-4 pb-2 border-b border-outline-variant/20">
                <span className="font-headline-md text-sm font-semibold text-on-surface">Notifications</span>
                <Link
                  to={user?.role === 'ADMIN' ? '/admin/notifications' : '/notifications'}
                  onClick={() => setShowDropdown(false)}
                  className="text-xs text-primary font-label-md hover:underline"
                >
                  View All
                </Link>
              </div>

              <div className="max-h-64 overflow-y-auto divide-y divide-outline-variant/10">
                {quickNotifications.length === 0 ? (
                  <p className="p-4 text-xs text-center text-on-surface-variant">No unread notifications</p>
                ) : (
                  quickNotifications.map((n) => (
                    <div key={n.id || n.notificationId} className="p-3 hover:bg-primary/5 transition-colors">
                      <p className="text-xs font-semibold text-on-surface">{n.title}</p>
                      <p className="text-[11px] text-on-surface-variant line-clamp-2 mt-0.5">{n.message}</p>
                    </div>
                  ))
                )}
              </div>
            </div>
          )}
        </div>

        {/* User Profile / Initial */}
        <Link
          to={user?.role === 'ADMIN' ? '/admin/dashboard' : '/profile'}
          className="flex items-center gap-2 hover:opacity-80 transition-opacity"
        >
          <div className="w-8 h-8 rounded-full overflow-hidden border border-primary/20 flex items-center justify-center bg-primary/10 text-primary font-bold text-xs">
            {profileImageUrl ? (
              <img alt="User profile" className="w-full h-full object-cover" src={profileImageUrl} />
            ) : (
              initial
            )}
          </div>
          {user?.name && (
            <span className="hidden md:inline font-label-sm text-xs text-on-surface font-medium">
              {user.name}
            </span>
          )}
        </Link>
      </div>
    </header>
  );
}

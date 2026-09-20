import { useState, useEffect } from 'react';
import StudentSidebar from '../../components/layout/StudentSidebar';
import TopNav from '../../components/layout/TopNav';
import { notificationApi, getErrorMessage } from '../../services/api';
import type { NotificationItem } from '../../types/api';

export default function StudentNotifications() {
  const [notifications, setNotifications] = useState<NotificationItem[]>([]);
  const [filter, setFilter] = useState<'ALL' | 'UNREAD'>('ALL');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const fetchNotifications = async () => {
    try {
      setLoading(true);
      setError('');
      if (filter === 'UNREAD') {
        const unread = await notificationApi.getUnreadNotifications();
        setNotifications(unread);
      } else {
        const page = await notificationApi.getMyNotifications(0, 50);
        setNotifications(page.content || []);
      }
    } catch (err) {
      setError(getErrorMessage(err, 'Failed to load notifications.'));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchNotifications();
  }, [filter]);

  const handleMarkRead = async (id: string) => {
    try {
      await notificationApi.markAsRead(id);
      setNotifications((prev) =>
        prev.map((n) => (n.id === id || n.notificationId === id ? { ...n, isRead: true } : n))
      );
    } catch {
      // silent catch for quick action
    }
  };

  const handleMarkAllRead = async () => {
    try {
      await notificationApi.markAllAsRead();
      setNotifications((prev) => prev.map((n) => ({ ...n, isRead: true })));
    } catch (err) {
      setError(getErrorMessage(err, 'Failed to mark all as read.'));
    }
  };

  const handleDelete = async (id: string) => {
    try {
      await notificationApi.deleteNotification(id);
      setNotifications((prev) => prev.filter((n) => n.id !== id && n.notificationId !== id));
    } catch {
      // silent catch
    }
  };

  const unreadCount = notifications.filter((n) => !n.isRead).length;

  return (
    <div className="flex h-screen bg-background text-on-surface">
      <StudentSidebar />
      <main className="ml-64 flex-1 flex flex-col min-h-screen relative overflow-y-auto">
        <TopNav searchPlaceholder="Search notifications..." />

        <div className="p-margin-mobile md:p-margin-desktop max-w-container-max mx-auto w-full flex flex-col gap-stack-lg">
          {/* Header */}
          <div className="flex flex-col sm:flex-row sm:items-end justify-between gap-4">
            <div>
              <div className="flex items-center gap-3">
                <h1 className="font-display-lg text-display-lg text-on-surface">Notifications</h1>
                {unreadCount > 0 && (
                  <span className="px-2.5 py-0.5 rounded-full text-xs font-label-sm bg-primary text-on-primary font-bold">
                    {unreadCount} unread
                  </span>
                )}
              </div>
              <p className="font-body-lg text-body-lg text-on-surface-variant mt-1">
                Stay updated with campus admissions, course schedules, and registration alerts.
              </p>
            </div>

            <div className="flex items-center gap-2">
              <button
                onClick={handleMarkAllRead}
                disabled={unreadCount === 0}
                className="px-4 py-2 border border-outline-variant text-on-surface rounded-lg font-label-md text-sm hover:bg-surface-container disabled:opacity-40 disabled:cursor-not-allowed transition-all"
              >
                Mark all as read
              </button>
            </div>
          </div>

          {/* Filter tabs */}
          <div className="flex gap-2 border-b border-outline-variant/30 pb-2">
            <button
              onClick={() => setFilter('ALL')}
              className={`px-4 py-1.5 rounded-lg font-label-md text-sm transition-colors ${
                filter === 'ALL'
                  ? 'bg-primary/10 text-primary font-semibold'
                  : 'text-on-surface-variant hover:bg-surface-container'
              }`}
            >
              All Notifications
            </button>
            <button
              onClick={() => setFilter('UNREAD')}
              className={`px-4 py-1.5 rounded-lg font-label-md text-sm transition-colors ${
                filter === 'UNREAD'
                  ? 'bg-primary/10 text-primary font-semibold'
                  : 'text-on-surface-variant hover:bg-surface-container'
              }`}
            >
              Unread Only
            </button>
          </div>

          {error && (
            <div className="p-4 bg-error-container/30 border border-error/20 text-on-error-container rounded-xl font-body-sm text-body-sm flex items-center gap-3">
              <span className="material-symbols-outlined text-error">error</span>
              {error}
            </div>
          )}

          {/* Notification List */}
          {loading ? (
            <div className="glass-card rounded-2xl p-12 flex flex-col items-center justify-center">
              <div className="w-10 h-10 border-4 border-primary/20 border-t-primary rounded-full animate-spin"></div>
              <p className="mt-4 text-on-surface-variant font-body-md">Loading notifications...</p>
            </div>
          ) : notifications.length === 0 ? (
            <div className="glass-card rounded-2xl p-12 text-center flex flex-col items-center justify-center">
              <span className="material-symbols-outlined text-[48px] text-on-surface-variant/40 mb-3">
                notifications_off
              </span>
              <p className="text-on-surface font-headline-md text-lg font-semibold">No notifications</p>
              <p className="text-on-surface-variant text-sm mt-1">
                {filter === 'UNREAD' ? 'You have no unread notifications.' : 'Your notification inbox is clean.'}
              </p>
            </div>
          ) : (
            <div className="space-y-3">
              {notifications.map((n) => {
                const id = n.id || n.notificationId;
                return (
                  <div
                    key={id}
                    className={`glass-panel rounded-xl p-4 flex items-start justify-between gap-4 transition-all ${
                      !n.isRead ? 'border-l-4 border-l-primary bg-primary-fixed/10' : 'hover:bg-surface-container/30'
                    }`}
                  >
                    <div className="flex items-start gap-3 flex-1">
                      <div className="w-9 h-9 rounded-full bg-primary/10 text-primary flex items-center justify-center shrink-0 mt-0.5">
                        <span className="material-symbols-outlined text-[18px]">
                          {n.type === 'ADMISSION'
                            ? 'school'
                            : n.type === 'REGISTRATION'
                            ? 'how_to_reg'
                            : n.type === 'SCHEDULE'
                            ? 'schedule'
                            : 'notifications'}
                        </span>
                      </div>
                      <div className="flex-1">
                        <div className="flex items-center gap-2 flex-wrap">
                          <h4 className="font-headline-md text-sm font-semibold text-on-surface">{n.title}</h4>
                          <span
                            className={`px-2 py-0.5 rounded text-[10px] font-label-sm uppercase ${
                              n.priority === 'HIGH' || n.priority === 'URGENT'
                                ? 'bg-error/10 text-error'
                                : 'bg-surface-container text-on-surface-variant'
                            }`}
                          >
                            {n.priority}
                          </span>
                        </div>
                        <p className="font-body-sm text-sm text-on-surface-variant mt-1">{n.message}</p>
                        <p className="text-[11px] text-on-surface-variant/70 mt-2">
                          {n.createdAt ? new Date(n.createdAt).toLocaleString() : 'Recently'}
                        </p>
                      </div>
                    </div>

                    <div className="flex items-center gap-2 shrink-0">
                      {!n.isRead && (
                        <button
                          onClick={() => handleMarkRead(id)}
                          title="Mark as read"
                          className="p-1.5 text-primary hover:bg-primary/10 rounded-md transition-colors"
                        >
                          <span className="material-symbols-outlined text-[18px]">done</span>
                        </button>
                      )}
                      <button
                        onClick={() => handleDelete(id)}
                        title="Delete notification"
                        className="p-1.5 text-on-surface-variant hover:text-error hover:bg-error/10 rounded-md transition-colors"
                      >
                        <span className="material-symbols-outlined text-[18px]">delete</span>
                      </button>
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </div>
      </main>
    </div>
  );
}

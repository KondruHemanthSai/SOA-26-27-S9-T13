import { useState, useEffect } from 'react';
import AdminSidebar from '../../components/layout/AdminSidebar';
import { notificationApi, aiApi, getErrorMessage } from '../../services/api';
import type {
  NotificationItem,
  NotificationType,
  NotificationPriority,
  CreateNotificationRequest,
} from '../../types/api';

export default function AdminNotifications() {
  const [notifications, setNotifications] = useState<NotificationItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);

  // AI draft state
  const [showAiModal, setShowAiModal] = useState(false);
  const [aiTopic, setAiTopic] = useState('');
  const [aiAudience, setAiAudience] = useState('ALL_STUDENTS');
  const [aiTone, setAiTone] = useState<'URGENT' | 'INFORMATIVE' | 'FRIENDLY' | 'FORMAL'>('INFORMATIVE');
  const [aiKeyPoints, setAiKeyPoints] = useState('');
  const [isGeneratingAi, setIsGeneratingAi] = useState(false);
  const [aiResult, setAiResult] = useState<{ title: string; message: string; suggestedPriority?: string } | null>(null);
  const [aiError, setAiError] = useState('');

  // Form state
  const [userId, setUserId] = useState('');
  const [title, setTitle] = useState('');
  const [message, setMessage] = useState('');
  const [type, setType] = useState<NotificationType>('GENERAL');
  const [priority, setPriority] = useState<NotificationPriority>('MEDIUM');
  const [isBroadcast, setIsBroadcast] = useState(false);

  const handleGenerateAiAnnouncement = async () => {
    if (!aiTopic.trim()) return;
    setIsGeneratingAi(true);
    setAiError('');
    try {
      const res = await aiApi.generateAnnouncement(aiTopic, aiAudience, aiTone);
      setAiResult({
        title: res.title,
        message: res.message,
        suggestedPriority: aiTone === 'URGENT' ? 'URGENT' : 'MEDIUM',
      });
    } catch (err) {
      setAiError(getErrorMessage(err, 'Failed to generate announcement with AI.'));
    } finally {
      setIsGeneratingAi(false);
    }
  };

  const handleApplyAiDraft = () => {
    if (!aiResult) return;
    setTitle(aiResult.title);
    setMessage(aiResult.message);
    if (aiResult.suggestedPriority) {
      const p = aiResult.suggestedPriority.toUpperCase();
      if (['LOW', 'MEDIUM', 'HIGH', 'URGENT'].includes(p)) {
        setPriority(p as NotificationPriority);
      }
    }
    setShowAiModal(false);
    setAiResult(null);
  };

  const fetchNotifications = async () => {
    try {
      setLoading(true);
      setError('');
      const page = await notificationApi.getAdminNotifications({ size: 50 });
      setNotifications(page.content || []);
    } catch (err) {
      setError(getErrorMessage(err, 'Failed to fetch notification history.'));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchNotifications();
  }, []);

  const handleSend = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    setIsSubmitting(true);

    try {
      if (isBroadcast) {
        // Send broadcast announcement
        await notificationApi.broadcastNotification({
          userIds: userId.split(',').map((id) => id.trim()).filter(Boolean),
          title,
          message,
          type,
          priority,
        });
        setSuccess('Broadcast notification dispatched successfully.');
      } else {
        const payload: CreateNotificationRequest = {
          userId: userId.trim(),
          title,
          message,
          type,
          priority,
        };
        await notificationApi.createNotification(payload);
        setSuccess('Notification sent successfully.');
      }

      // Reset form & reload
      setTitle('');
      setMessage('');
      setUserId('');
      fetchNotifications();
      setTimeout(() => setSuccess(''), 4000);
    } catch (err) {
      setError(getErrorMessage(err, 'Failed to send notification.'));
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="flex h-screen bg-background text-on-surface">
      <AdminSidebar />
      <main className="ml-64 flex-1 flex flex-col min-h-screen relative overflow-y-auto">
        {/* Header */}
        <header className="bg-surface/60 backdrop-blur-lg border-b border-primary/10 h-16 flex items-center justify-between px-gutter sticky top-0 z-40">
          <div className="flex items-center gap-3">
            <span className="font-headline-md text-headline-md font-semibold text-primary">Campus Notifications</span>
          </div>
        </header>

        <div className="p-margin-mobile md:p-margin-desktop max-w-container-max mx-auto w-full flex flex-col gap-stack-lg">
          <div>
            <h1 className="font-display-lg text-display-lg text-on-surface">Notification Dispatcher</h1>
            <p className="font-body-lg text-body-lg text-on-surface-variant mt-1">
              Broadcast announcements or send targeted notifications to students.
            </p>
          </div>

          {error && (
            <div className="p-4 bg-error-container/30 border border-error/20 text-on-error-container rounded-xl font-body-sm text-body-sm flex items-center gap-3">
              <span className="material-symbols-outlined text-error">error</span>
              {error}
            </div>
          )}

          {success && (
            <div className="p-4 bg-emerald-50 border border-emerald-200 text-emerald-800 rounded-xl font-body-sm text-body-sm flex items-center gap-3">
              <span className="material-symbols-outlined text-emerald-600" style={{ fontVariationSettings: "'FILL' 1" }}>
                check_circle
              </span>
              {success}
            </div>
          )}

          <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
            {/* Create Notification Form */}
            <div className="glass-panel rounded-2xl p-6">
              <div className="flex items-center justify-between mb-4">
                <h3 className="font-headline-md text-lg font-semibold text-primary flex items-center gap-2">
                  <span className="material-symbols-outlined text-primary">campaign</span>
                  Compose Notification
                </h3>
                <button
                  type="button"
                  onClick={() => setShowAiModal(true)}
                  className="inline-flex items-center gap-1.5 px-3 py-1.5 bg-gradient-to-r from-primary/15 to-tertiary/15 hover:from-primary/25 hover:to-tertiary/25 text-primary text-xs font-semibold rounded-lg border border-primary/20 transition-all shadow-sm"
                >
                  <span className="material-symbols-outlined text-[16px]">auto_awesome</span>
                  Draft with AI
                </button>
              </div>

              <form onSubmit={handleSend} className="space-y-4">
                <div className="flex items-center gap-2 mb-2">
                  <input
                    type="checkbox"
                    id="broadcastToggle"
                    checked={isBroadcast}
                    onChange={(e) => setIsBroadcast(e.target.checked)}
                    className="rounded text-primary focus:ring-primary"
                  />
                  <label htmlFor="broadcastToggle" className="font-label-sm text-xs text-on-surface">
                    Broadcast to multiple students
                  </label>
                </div>

                <div className="space-y-1">
                  <label className="font-label-sm text-xs text-on-surface-variant">
                    {isBroadcast ? 'Target User IDs (comma separated)' : 'Student User ID'}
                  </label>
                  <input
                    type="text"
                    required
                    value={userId}
                    onChange={(e) => setUserId(e.target.value)}
                    placeholder={isBroadcast ? 'user1, user2, user3' : 'e.g. 64b8a1c9...'}
                    className="w-full rounded-lg glass-input px-3 py-2 font-body-sm text-on-surface"
                  />
                </div>

                <div className="space-y-1">
                  <label className="font-label-sm text-xs text-on-surface-variant">Notification Title</label>
                  <input
                    type="text"
                    required
                    value={title}
                    onChange={(e) => setTitle(e.target.value)}
                    placeholder="e.g. Registration Period Open"
                    className="w-full rounded-lg glass-input px-3 py-2 font-body-sm text-on-surface"
                  />
                </div>

                <div className="grid grid-cols-2 gap-3">
                  <div className="space-y-1">
                    <label className="font-label-sm text-xs text-on-surface-variant">Category</label>
                    <select
                      value={type}
                      onChange={(e) => setType(e.target.value as NotificationType)}
                      className="w-full rounded-lg glass-input px-3 py-2 font-body-sm text-on-surface"
                    >
                      <option value="GENERAL">General</option>
                      <option value="ADMISSION">Admission</option>
                      <option value="REGISTRATION">Registration</option>
                      <option value="SCHEDULE">Schedule</option>
                      <option value="ACADEMIC">Academic</option>
                      <option value="SYSTEM">System</option>
                    </select>
                  </div>

                  <div className="space-y-1">
                    <label className="font-label-sm text-xs text-on-surface-variant">Priority</label>
                    <select
                      value={priority}
                      onChange={(e) => setPriority(e.target.value as NotificationPriority)}
                      className="w-full rounded-lg glass-input px-3 py-2 font-body-sm text-on-surface"
                    >
                      <option value="LOW">Low</option>
                      <option value="MEDIUM">Medium</option>
                      <option value="HIGH">High</option>
                      <option value="URGENT">Urgent</option>
                    </select>
                  </div>
                </div>

                <div className="space-y-1">
                  <label className="font-label-sm text-xs text-on-surface-variant">Message Body</label>
                  <textarea
                    required
                    rows={4}
                    value={message}
                    onChange={(e) => setMessage(e.target.value)}
                    placeholder="Enter notification message..."
                    className="w-full rounded-lg glass-input px-3 py-2 font-body-sm text-on-surface resize-none"
                  ></textarea>
                </div>

                <button
                  type="submit"
                  disabled={isSubmitting}
                  className="w-full bg-primary text-on-primary font-label-md text-sm py-2.5 rounded-lg shadow-sm hover:bg-primary/90 transition-all flex items-center justify-center gap-2 disabled:opacity-50"
                >
                  <span className="material-symbols-outlined text-[18px]">send</span>
                  {isSubmitting ? 'Sending...' : 'Send Notification'}
                </button>
              </form>
            </div>

            {/* Notification History Log */}
            <div className="lg:col-span-2 glass-panel rounded-2xl p-6 flex flex-col">
              <h3 className="font-headline-md text-lg font-semibold text-primary mb-4 flex items-center gap-2">
                <span className="material-symbols-outlined text-primary">history</span>
                Dispatched Notification Log
              </h3>

              {loading ? (
                <div className="flex-1 flex flex-col items-center justify-center py-12">
                  <div className="w-8 h-8 border-4 border-primary/20 border-t-primary rounded-full animate-spin"></div>
                  <p className="mt-3 text-on-surface-variant text-sm">Loading logs...</p>
                </div>
              ) : notifications.length === 0 ? (
                <div className="flex-1 flex flex-col items-center justify-center py-12 text-center text-on-surface-variant">
                  <span className="material-symbols-outlined text-[40px] opacity-40 mb-2">inbox</span>
                  <p className="font-semibold text-sm">No notifications found</p>
                </div>
              ) : (
                <div className="space-y-3 overflow-y-auto max-h-[600px] pr-2">
                  {notifications.map((item) => (
                    <div
                      key={item.id || item.notificationId}
                      className="p-3.5 rounded-xl border border-outline-variant/30 bg-surface-container-low/40 flex flex-col gap-1.5"
                    >
                      <div className="flex justify-between items-start gap-2">
                        <div className="flex items-center gap-2">
                          <span className="font-semibold text-sm text-on-surface">{item.title}</span>
                          <span
                            className={`px-2 py-0.5 rounded text-[10px] font-label-sm uppercase ${
                              item.priority === 'URGENT' || item.priority === 'HIGH'
                                ? 'bg-error/10 text-error'
                                : 'bg-surface-container text-on-surface-variant'
                            }`}
                          >
                            {item.priority}
                          </span>
                        </div>
                        <span className="text-[11px] text-on-surface-variant shrink-0">
                          {item.createdAt ? new Date(item.createdAt).toLocaleDateString() : ''}
                        </span>
                      </div>
                      <p className="text-body-sm text-xs text-on-surface-variant">{item.message}</p>
                      <div className="flex items-center gap-3 text-[11px] text-on-surface-variant/70 mt-1">
                        <span>Recipient ID: {item.userId}</span>
                        <span>•</span>
                        <span>Type: {item.type}</span>
                        <span>•</span>
                        <span>Status: {item.isRead ? 'Read' : 'Unread'}</span>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>
        </div>

        {/* AI Announcement Generator Modal */}
        {showAiModal && (
          <div className="fixed inset-0 z-50 bg-black/50 backdrop-blur-sm flex items-center justify-center p-4">
            <div className="bg-surface border border-outline-variant/30 rounded-2xl max-w-xl w-full p-6 shadow-2xl flex flex-col gap-4 animate-in fade-in zoom-in duration-200">
              <div className="flex items-center justify-between border-b border-outline-variant/20 pb-3">
                <div className="flex items-center gap-2">
                  <span className="material-symbols-outlined text-primary text-[24px]">auto_awesome</span>
                  <h3 className="font-headline-md text-lg font-semibold text-on-surface">
                    AI Announcement Generator
                  </h3>
                </div>
                <button
                  type="button"
                  onClick={() => setShowAiModal(false)}
                  className="p-1 rounded-lg hover:bg-surface-container text-on-surface-variant transition-colors"
                >
                  <span className="material-symbols-outlined text-[20px]">close</span>
                </button>
              </div>

              {aiError && (
                <div className="p-3 bg-error-container/30 border border-error/20 text-on-error-container rounded-lg text-xs flex items-center gap-2">
                  <span className="material-symbols-outlined text-[16px]">error</span>
                  <span>{aiError}</span>
                </div>
              )}

              <div className="space-y-3">
                <div>
                  <label className="block text-xs font-semibold text-on-surface-variant mb-1">
                    Announcement Topic *
                  </label>
                  <input
                    type="text"
                    value={aiTopic}
                    onChange={(e) => setAiTopic(e.target.value)}
                    placeholder="e.g. Fall 2024 Course Add/Drop Deadline Extended"
                    className="w-full rounded-lg glass-input px-3 py-2 text-sm text-on-surface"
                  />
                </div>

                <div className="grid grid-cols-2 gap-3">
                  <div>
                    <label className="block text-xs font-semibold text-on-surface-variant mb-1">
                      Target Audience
                    </label>
                    <select
                      value={aiAudience}
                      onChange={(e) => setAiAudience(e.target.value)}
                      className="w-full rounded-lg glass-input px-3 py-2 text-sm text-on-surface"
                    >
                      <option value="ALL_STUDENTS">All Students</option>
                      <option value="FRESHMEN">First-Year Students</option>
                      <option value="SENIORS">Graduating Seniors</option>
                      <option value="UNDERGRADUATE">Undergraduates</option>
                      <option value="POSTGRADUATE">Postgraduates</option>
                      <option value="FACULTY">Faculty &amp; Staff</option>
                    </select>
                  </div>

                  <div>
                    <label className="block text-xs font-semibold text-on-surface-variant mb-1">
                      Tone
                    </label>
                    <select
                      value={aiTone}
                      onChange={(e) =>
                        setAiTone(e.target.value as 'URGENT' | 'INFORMATIVE' | 'FRIENDLY' | 'FORMAL')
                      }
                      className="w-full rounded-lg glass-input px-3 py-2 text-sm text-on-surface"
                    >
                      <option value="INFORMATIVE">Informative &amp; Clear</option>
                      <option value="URGENT">Urgent &amp; Time-Sensitive</option>
                      <option value="FRIENDLY">Friendly &amp; Encouraging</option>
                      <option value="FORMAL">Formal &amp; Official</option>
                    </select>
                  </div>
                </div>

                <div>
                  <label className="block text-xs font-semibold text-on-surface-variant mb-1">
                    Key Points (one per line, optional)
                  </label>
                  <textarea
                    rows={2}
                    value={aiKeyPoints}
                    onChange={(e) => setAiKeyPoints(e.target.value)}
                    placeholder="Deadline: Friday 5 PM&#10;Portal: studentcentral.edu/registration&#10;No late waivers permitted"
                    className="w-full rounded-lg glass-input px-3 py-2 text-xs text-on-surface resize-none"
                  ></textarea>
                </div>

                <button
                  type="button"
                  onClick={handleGenerateAiAnnouncement}
                  disabled={isGeneratingAi || !aiTopic.trim()}
                  className="w-full py-2 px-4 rounded-lg bg-primary text-on-primary font-semibold text-xs flex items-center justify-center gap-2 hover:bg-primary/90 transition-all disabled:opacity-50"
                >
                  {isGeneratingAi ? (
                    <>
                      <div className="w-3.5 h-3.5 border-2 border-on-primary border-t-transparent rounded-full animate-spin"></div>
                      <span>Generating with AI...</span>
                    </>
                  ) : (
                    <>
                      <span className="material-symbols-outlined text-[16px]">sparkles</span>
                      <span>Generate Professional Draft</span>
                    </>
                  )}
                </button>
              </div>

              {/* AI Generated Result Preview */}
              {aiResult && (
                <div className="mt-1 p-4 rounded-xl border border-primary/20 bg-primary/5 space-y-2">
                  <div className="flex items-center justify-between">
                    <span className="text-xs font-bold text-primary flex items-center gap-1">
                      <span className="material-symbols-outlined text-[14px]">check_circle</span>
                      Generated Draft
                    </span>
                    {aiResult.suggestedPriority && (
                      <span className="text-[10px] uppercase font-bold px-2 py-0.5 rounded bg-primary/10 text-primary">
                        Suggested Priority: {aiResult.suggestedPriority}
                      </span>
                    )}
                  </div>
                  <div>
                    <span className="text-xs font-semibold text-on-surface-variant">Title:</span>
                    <p className="text-xs font-bold text-on-surface">{aiResult.title}</p>
                  </div>
                  <div>
                    <span className="text-xs font-semibold text-on-surface-variant">Message:</span>
                    <p className="text-xs text-on-surface whitespace-pre-line leading-relaxed">
                      {aiResult.message}
                    </p>
                  </div>
                  <div className="pt-2 flex justify-end gap-2">
                    <button
                      type="button"
                      onClick={() => setAiResult(null)}
                      className="px-3 py-1 text-xs rounded text-on-surface-variant hover:bg-surface-container"
                    >
                      Discard
                    </button>
                    <button
                      type="button"
                      onClick={handleApplyAiDraft}
                      className="px-3 py-1.5 text-xs font-semibold rounded-lg bg-primary text-on-primary hover:bg-primary/90 transition-all flex items-center gap-1.5"
                    >
                      <span className="material-symbols-outlined text-[14px]">content_paste</span>
                      Apply to Form
                    </button>
                  </div>
                </div>
              )}
            </div>
          </div>
        )}
      </main>
    </div>
  );
}

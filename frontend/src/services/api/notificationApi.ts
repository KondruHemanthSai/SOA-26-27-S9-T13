import apiClient from './apiClient';
import type {
  NotificationItem,
  NotificationPageResponse,
  UnreadCountResponse,
  CreateNotificationRequest,
  BroadcastNotificationRequest,
  NotificationType,
  NotificationPriority,
  ApiResponse,
} from '../../types/api';

export interface AdminNotificationFilterParams {
  userId?: string;
  type?: NotificationType;
  priority?: NotificationPriority;
  isRead?: boolean;
  page?: number;
  size?: number;
}

export const notificationApi = {
  async getMyNotifications(page = 0, size = 20): Promise<NotificationPageResponse> {
    const res = await apiClient.get<ApiResponse<NotificationPageResponse>>('/api/notifications/my', {
      params: { page, size },
    });
    return res.data.data;
  },

  async getUnreadNotifications(): Promise<NotificationItem[]> {
    const res = await apiClient.get<ApiResponse<NotificationItem[]>>('/api/notifications/my/unread');
    return res.data.data || [];
  },

  async getUnreadCount(): Promise<number> {
    try {
      const res = await apiClient.get<ApiResponse<UnreadCountResponse>>('/api/notifications/my/unread-count');
      return res.data.data.unreadCount;
    } catch {
      return 0;
    }
  },

  async markAsRead(notificationId: string): Promise<NotificationItem> {
    const res = await apiClient.put<ApiResponse<NotificationItem>>(`/api/notifications/${notificationId}/read`);
    return res.data.data;
  },

  async markAllAsRead(): Promise<void> {
    await apiClient.put<ApiResponse<void>>('/api/notifications/my/read-all');
  },

  async deleteNotification(notificationId: string): Promise<void> {
    await apiClient.delete(`/api/notifications/${notificationId}`);
  },

  async createNotification(data: CreateNotificationRequest): Promise<NotificationItem> {
    const res = await apiClient.post<ApiResponse<NotificationItem>>('/api/notifications', data);
    return res.data.data;
  },

  async broadcastNotification(data: BroadcastNotificationRequest): Promise<NotificationItem[]> {
    const res = await apiClient.post<ApiResponse<NotificationItem[]>>('/api/notifications/broadcast', data);
    return res.data.data;
  },

  async getAdminNotifications(filters?: AdminNotificationFilterParams): Promise<NotificationPageResponse> {
    const params = new URLSearchParams();
    if (filters?.userId) params.append('userId', filters.userId);
    if (filters?.type) params.append('type', filters.type);
    if (filters?.priority) params.append('priority', filters.priority);
    if (filters?.isRead !== undefined && filters.isRead !== null) params.append('isRead', filters.isRead.toString());
    if (filters?.page !== undefined) params.append('page', filters.page.toString());
    if (filters?.size !== undefined) params.append('size', filters.size.toString());

    const res = await apiClient.get<ApiResponse<NotificationPageResponse>>('/api/notifications/admin', { params });
    return res.data.data;
  },
};

export default notificationApi;

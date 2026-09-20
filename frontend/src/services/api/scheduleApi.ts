import apiClient from './apiClient';
import type { ScheduleItem, CreateScheduleRequest, DayOfWeek } from '../../types/api';

export interface ScheduleFilterParams {
  dayOfWeek?: DayOfWeek;
  courseId?: string;
  semester?: number;
  academicYear?: string;
  classroom?: string;
  faculty?: string;
}

export const scheduleApi = {
  async getMySchedule(): Promise<ScheduleItem[]> {
    const res = await apiClient.get<ScheduleItem[]>('/api/schedules/my');
    return res.data || [];
  },

  async getCourseSchedule(courseId: string): Promise<ScheduleItem[]> {
    const res = await apiClient.get<ScheduleItem[]>(`/api/schedules/course/${courseId}`);
    return res.data || [];
  },

  async listSchedules(filters?: ScheduleFilterParams): Promise<ScheduleItem[]> {
    const params = new URLSearchParams();
    if (filters?.dayOfWeek) params.append('dayOfWeek', filters.dayOfWeek);
    if (filters?.courseId) params.append('courseId', filters.courseId);
    if (filters?.semester !== undefined && filters.semester !== null) {
      params.append('semester', filters.semester.toString());
    }
    if (filters?.academicYear) params.append('academicYear', filters.academicYear);
    if (filters?.classroom) params.append('classroom', filters.classroom);
    if (filters?.faculty) params.append('faculty', filters.faculty);

    const res = await apiClient.get<ScheduleItem[]>('/api/schedules', { params });
    return res.data || [];
  },

  async getScheduleById(scheduleId: string): Promise<ScheduleItem> {
    const res = await apiClient.get<ScheduleItem>(`/api/schedules/${scheduleId}`);
    return res.data;
  },

  async createSchedule(data: CreateScheduleRequest): Promise<ScheduleItem> {
    const res = await apiClient.post<ScheduleItem>('/api/schedules', data);
    return res.data;
  },

  async updateSchedule(scheduleId: string, data: Partial<CreateScheduleRequest>): Promise<ScheduleItem> {
    const res = await apiClient.put<ScheduleItem>(`/api/schedules/${scheduleId}`, data);
    return res.data;
  },

  async deleteSchedule(scheduleId: string): Promise<void> {
    await apiClient.delete(`/api/schedules/${scheduleId}`);
  },
};

export default scheduleApi;

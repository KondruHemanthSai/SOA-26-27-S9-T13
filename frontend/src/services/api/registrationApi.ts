import apiClient from './apiClient';
import type {
  Registration,
  RegistrationListResponse,
  CreateRegistrationRequest,
  RegistrationSuccessResponse,
  RegistrationStatus,
} from '../../types/api';

export const registrationApi = {
  async registerCourse(data: CreateRegistrationRequest): Promise<RegistrationSuccessResponse> {
    const res = await apiClient.post<RegistrationSuccessResponse>('/api/registrations', data);
    return res.data;
  },

  async getMyRegistrations(): Promise<Registration[]> {
    const res = await apiClient.get<RegistrationListResponse>('/api/registrations/my');
    return res.data.registrations || [];
  },

  async getMyActiveRegistrations(): Promise<Registration[]> {
    const res = await apiClient.get<RegistrationListResponse>('/api/registrations/my/active');
    return res.data.registrations || [];
  },

  async getMyHistory(): Promise<Registration[]> {
    const res = await apiClient.get<RegistrationListResponse>('/api/registrations/my/history');
    return res.data.registrations || [];
  },

  async getRegistrationById(registrationId: string): Promise<Registration> {
    const res = await apiClient.get<Registration>(`/api/registrations/${registrationId}`);
    return res.data;
  },

  async dropCourse(registrationId: string): Promise<Registration> {
    const res = await apiClient.delete<Registration>(`/api/registrations/${registrationId}`);
    return res.data;
  },

  async listAllRegistrations(
    courseId?: string,
    semester?: number,
    status?: RegistrationStatus
  ): Promise<Registration[]> {
    const params = new URLSearchParams();
    if (courseId) params.append('courseId', courseId);
    if (semester !== undefined && semester !== null) params.append('semester', semester.toString());
    if (status) params.append('status', status);
    const res = await apiClient.get<Registration[]>('/api/registrations', { params });
    return res.data;
  },

  async getRegistrationsByStudentId(studentId: string): Promise<Registration[]> {
    const res = await apiClient.get<Registration[]>(`/api/registrations/student/${studentId}`);
    return res.data;
  },
};

export default registrationApi;

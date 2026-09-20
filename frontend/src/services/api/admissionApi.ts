import apiClient from './apiClient';
import type {
  AdmissionApplication,
  CreateApplicationRequest,
  UpdateApplicationRequest,
  AdmissionDocument,
  UploadDocumentRequest,
  ApplicationReviewRequest,
  ApplicationStatus,
} from '../../types/api';

export const admissionApi = {
  async apply(data: CreateApplicationRequest): Promise<AdmissionApplication> {
    const res = await apiClient.post<AdmissionApplication>('/api/admissions/apply', data);
    return res.data;
  },

  async getMyApplication(): Promise<AdmissionApplication | null> {
    try {
      const res = await apiClient.get<AdmissionApplication>('/api/admissions/my-application');
      return res.data;
    } catch (error: any) {
      // 404 means no application exists yet for this student
      if (error.response?.status === 404) {
        return null;
      }
      throw error;
    }
  },

  async updateApplication(id: string, data: UpdateApplicationRequest): Promise<AdmissionApplication> {
    const res = await apiClient.put<AdmissionApplication>(`/api/admissions/${id}`, data);
    return res.data;
  },

  async submitApplication(id: string): Promise<AdmissionApplication> {
    const res = await apiClient.post<AdmissionApplication>(`/api/admissions/${id}/submit`);
    return res.data;
  },

  async uploadDocument(id: string, data: UploadDocumentRequest): Promise<AdmissionDocument> {
    const res = await apiClient.post<AdmissionDocument>(`/api/admissions/${id}/documents`, data);
    return res.data;
  },

  async getDocuments(id: string): Promise<AdmissionDocument[]> {
    const res = await apiClient.get<AdmissionDocument[]>(`/api/admissions/${id}/documents`);
    return res.data;
  },

  async deleteDocument(id: string, documentId: string): Promise<void> {
    await apiClient.delete(`/api/admissions/${id}/documents/${documentId}`);
  },

  async listApplications(department?: string, status?: ApplicationStatus): Promise<AdmissionApplication[]> {
    const params = new URLSearchParams();
    if (department) params.append('department', department);
    if (status) params.append('status', status);
    const res = await apiClient.get<AdmissionApplication[]>('/api/admissions', { params });
    return res.data;
  },

  async getApplicationById(id: string): Promise<AdmissionApplication> {
    const res = await apiClient.get<AdmissionApplication>(`/api/admissions/${id}`);
    return res.data;
  },

  async reviewApplication(id: string, review: ApplicationReviewRequest): Promise<AdmissionApplication> {
    const res = await apiClient.put<AdmissionApplication>(`/api/admissions/${id}/review`, review);
    return res.data;
  },
};

export default admissionApi;

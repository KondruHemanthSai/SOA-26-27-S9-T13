import apiClient from './apiClient';
import type {
  StudentProfile,
  CreateStudentRequest,
  UpdateStudentRequest,
  AcademicRecord,
  AdmissionStatus,
} from '../../types/api';

export const studentApi = {
  async getProfile(): Promise<StudentProfile> {
    const res = await apiClient.get<StudentProfile>('/api/students/profile');
    return res.data;
  },

  async createProfile(data: CreateStudentRequest): Promise<StudentProfile> {
    const res = await apiClient.post<StudentProfile>('/api/students/profile', data);
    return res.data;
  },

  async updateProfile(data: UpdateStudentRequest): Promise<StudentProfile> {
    const res = await apiClient.put<StudentProfile>('/api/students/profile', data);
    return res.data;
  },

  async getAcademicRecord(): Promise<AcademicRecord> {
    const res = await apiClient.get<AcademicRecord>('/api/students/profile/academic-record');
    return res.data;
  },

  async updateAcademicRecord(data: AcademicRecord): Promise<AcademicRecord> {
    const res = await apiClient.put<AcademicRecord>('/api/students/profile/academic-record', data);
    return res.data;
  },

  async getStudentById(id: string): Promise<StudentProfile> {
    const res = await apiClient.get<StudentProfile>(`/api/students/${id}`);
    return res.data;
  },

  async listStudents(department?: string, status?: AdmissionStatus): Promise<StudentProfile[]> {
    const params = new URLSearchParams();
    if (department) params.append('department', department);
    if (status) params.append('status', status);
    const res = await apiClient.get<StudentProfile[]>('/api/students', { params });
    return res.data;
  },
};

export default studentApi;

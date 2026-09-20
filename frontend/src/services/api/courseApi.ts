import apiClient from './apiClient';
import type {
  Course,
  CourseAvailability,
  PrerequisiteResponse,
  CreateCourseRequest,
  UpdateCourseRequest,
  CourseType,
  CourseStatus,
} from '../../types/api';

export interface CourseFilterParams {
  search?: string;
  department?: string;
  semester?: number;
  courseType?: CourseType;
  status?: CourseStatus;
  available?: boolean;
}

export const courseApi = {
  async getCourses(filters?: CourseFilterParams): Promise<Course[]> {
    const params = new URLSearchParams();
    if (filters?.search) params.append('search', filters.search);
    if (filters?.department) params.append('department', filters.department);
    if (filters?.semester !== undefined && filters.semester !== null) {
      params.append('semester', filters.semester.toString());
    }
    if (filters?.courseType) params.append('courseType', filters.courseType);
    if (filters?.status) params.append('status', filters.status);
    if (filters?.available !== undefined && filters.available !== null) {
      params.append('available', filters.available.toString());
    }

    const res = await apiClient.get<Course[]>('/api/courses', { params });
    return res.data;
  },

  async getCourse(courseId: string): Promise<Course> {
    const res = await apiClient.get<Course>(`/api/courses/${courseId}`);
    return res.data;
  },

  async getAvailability(courseId: string): Promise<CourseAvailability> {
    const res = await apiClient.get<CourseAvailability>(`/api/courses/${courseId}/availability`);
    return res.data;
  },

  async getPrerequisites(courseId: string): Promise<PrerequisiteResponse> {
    const res = await apiClient.get<PrerequisiteResponse>(`/api/courses/${courseId}/prerequisites`);
    return res.data;
  },

  async createCourse(data: CreateCourseRequest): Promise<Course> {
    const res = await apiClient.post<Course>('/api/courses', data);
    return res.data;
  },

  async updateCourse(courseId: string, data: UpdateCourseRequest): Promise<Course> {
    const res = await apiClient.put<Course>(`/api/courses/${courseId}`, data);
    return res.data;
  },

  async deleteCourse(courseId: string): Promise<void> {
    await apiClient.delete(`/api/courses/${courseId}`);
  },

  async activateCourse(courseId: string): Promise<Course> {
    const res = await apiClient.put<Course>(`/api/courses/${courseId}/activate`);
    return res.data;
  },

  async deactivateCourse(courseId: string): Promise<Course> {
    const res = await apiClient.put<Course>(`/api/courses/${courseId}/deactivate`);
    return res.data;
  },

  async addPrerequisite(courseId: string, prerequisiteCourseId: string): Promise<PrerequisiteResponse> {
    const res = await apiClient.post<PrerequisiteResponse>(`/api/courses/${courseId}/prerequisites`, {
      prerequisiteCourseId,
    });
    return res.data;
  },

  async deletePrerequisite(courseId: string, prerequisiteId: string): Promise<void> {
    await apiClient.delete(`/api/courses/${courseId}/prerequisites/${prerequisiteId}`);
  },
};

export default courseApi;

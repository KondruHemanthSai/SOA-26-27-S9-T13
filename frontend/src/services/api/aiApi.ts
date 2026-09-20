import apiClient from './apiClient';

export interface ChatMessage {
  role: 'user' | 'assistant';
  content: string;
  suggestedActions?: string[];
  toolsUsed?: string[];
  timestamp?: string;
}

export interface ChatResponseData {
  message: string;
  suggestedActions: string[];
  toolsUsed: string[];
  remainingRateLimit: number;
}

export interface CourseRecommendation {
  courseId: string;
  courseCode: string;
  courseName: string;
  credits: number;
  availableSeats: number;
  department: string;
  reason: string;
  confidence: number;
}

export interface StudentInsight {
  category: string;
  severity: 'INFO' | 'WARNING' | 'CRITICAL' | 'SUCCESS';
  title: string;
  message: string;
  actionableStep?: string;
}

export interface AdmissionGuidance {
  status: string;
  explanation: string;
  nextSteps: string[];
  remarks?: string;
}

export interface AnnouncementDraft {
  title: string;
  message: string;
  shortMessage: string;
  audience: string;
  tone: string;
}

export const aiApi = {
  async chat(message: string, conversationId?: string): Promise<ChatResponseData> {
    const res = await apiClient.post<{ success: boolean; data: ChatResponseData }>('/api/ai/chat', {
      message,
      conversationId,
    });
    return res.data.data;
  },

  async getCourseRecommendations(): Promise<CourseRecommendation[]> {
    const res = await apiClient.post<{ recommendations: CourseRecommendation[] }>('/api/ai/course-recommendations');
    return res.data.recommendations || [];
  },

  async getStudentInsights(): Promise<StudentInsight[]> {
    const res = await apiClient.post<{ insights: StudentInsight[] }>('/api/ai/student-insights');
    return res.data.insights || [];
  },

  async getAdmissionGuidance(): Promise<AdmissionGuidance> {
    const res = await apiClient.post<AdmissionGuidance>('/api/ai/admission-assistant');
    return res.data;
  },

  async generateAnnouncement(topic: string, audience = 'ALL_STUDENTS', tone = 'PROFESSIONAL'): Promise<AnnouncementDraft> {
    const res = await apiClient.post<AnnouncementDraft>('/api/ai/generate-announcement', {
      topic,
      audience,
      tone,
    });
    return res.data;
  },

  async getHealth(): Promise<{ status: string; service: string; provider: string; available: boolean }> {
    const res = await apiClient.get<{ status: string; service: string; provider: string; available: boolean }>('/api/ai/health');
    return res.data;
  },
};

export default aiApi;

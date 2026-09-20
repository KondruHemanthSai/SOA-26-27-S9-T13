import apiClient from './apiClient';
import type { AuthResponse, UserResponse, TokenValidationResponse, Role } from '../../types/api';

export interface LoginPayload {
  email: string;
  password: string;
}

export interface RegisterPayload {
  name: string;
  email: string;
  password: string;
  role?: Role;
}

export const authApi = {
  async login(payload: LoginPayload): Promise<AuthResponse> {
    const res = await apiClient.post<AuthResponse>('/api/auth/login', payload);
    return res.data;
  },

  async register(payload: RegisterPayload): Promise<UserResponse> {
    const res = await apiClient.post<UserResponse>('/api/auth/register', payload);
    return res.data;
  },

  async getMe(): Promise<UserResponse> {
    const res = await apiClient.get<UserResponse>('/api/auth/me');
    return res.data;
  },

  async validateToken(token?: string): Promise<TokenValidationResponse> {
    const config = token ? { headers: { Authorization: `Bearer ${token}` } } : undefined;
    const res = await apiClient.get<TokenValidationResponse>('/api/auth/validate', config);
    return res.data;
  },
};

export default authApi;

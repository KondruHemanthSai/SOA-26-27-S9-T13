import axios, { AxiosError } from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

export const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 15000,
});

// Request interceptor: attach JWT token if available
apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('sc_token');
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor: handle 401, 403, and network errors gracefully
apiClient.interceptors.response.use(
  (response) => response,
  (error: AxiosError<{ message?: string; error?: string; status?: number }>) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('sc_token');
      localStorage.removeItem('sc_user');
      // Only redirect if not already on public auth pages
      if (
        !window.location.pathname.startsWith('/login') &&
        !window.location.pathname.startsWith('/register') &&
        window.location.pathname !== '/'
      ) {
        window.location.href = '/login';
      }
    }
    return Promise.reject(error);
  }
);

/**
 * Extract user-friendly error message from API errors
 */
export function getErrorMessage(error: unknown, defaultMessage = 'An unexpected error occurred'): string {
  if (axios.isAxiosError(error)) {
    if (error.code === 'ERR_NETWORK' || !error.response) {
      return 'Unable to connect to Student Central services. Please ensure backend services and API Gateway are running.';
    }
    const data = error.response.data as { message?: string; error?: string };
    if (data?.message) return data.message;
    if (data?.error) return data.error;
    if (error.response.status === 403) return 'You do not have permission to perform this action.';
    if (error.response.status === 404) return 'The requested resource was not found.';
    if (error.response.status === 409) return data?.message || 'A conflicting record already exists.';
    if (error.response.status === 500) return 'Internal server error. Please try again later.';
  }
  if (error instanceof Error) {
    return error.message;
  }
  return defaultMessage;
}

export default apiClient;

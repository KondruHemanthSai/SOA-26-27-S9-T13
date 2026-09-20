import { createContext, useContext, useState, useEffect, useCallback, type ReactNode } from 'react';
import type { User, Role } from '../types/api';
import authApi from '../services/api/authApi';

interface AuthContextType {
  user: User | null;
  token: string | null;
  role: Role | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (token: string, user: User) => void;
  logout: () => void;
  restoreSession: () => Promise<void>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [token, setToken] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  const logout = useCallback(() => {
    setToken(null);
    setUser(null);
    localStorage.removeItem('sc_token');
    localStorage.removeItem('sc_user');
  }, []);

  const restoreSession = useCallback(async () => {
    const storedToken = localStorage.getItem('sc_token');
    const storedUser = localStorage.getItem('sc_user');

    if (!storedToken) {
      setIsLoading(false);
      return;
    }

    try {
      // Optimistically set from localStorage to prevent flash of unauthenticated UI
      setToken(storedToken);
      if (storedUser) {
        try {
          setUser(JSON.parse(storedUser));
        } catch {
          // ignore parse error, will be refreshed by getMe
        }
      }

      // Verify token with backend /api/auth/me
      const me = await authApi.getMe();
      const verifiedUser: User = {
        id: me.id,
        name: me.name,
        email: me.email,
        role: me.role,
        active: me.active,
        createdAt: me.createdAt,
      };

      setUser(verifiedUser);
      setToken(storedToken);
      localStorage.setItem('sc_user', JSON.stringify(verifiedUser));
    } catch {
      // Token is invalid/expired on backend
      logout();
    } finally {
      setIsLoading(false);
    }
  }, [logout]);

  useEffect(() => {
    restoreSession();
  }, [restoreSession]);

  const login = (newToken: string, newUser: User) => {
    setToken(newToken);
    setUser(newUser);
    localStorage.setItem('sc_token', newToken);
    localStorage.setItem('sc_user', JSON.stringify(newUser));
  };

  const role = user?.role || null;

  return (
    <AuthContext.Provider
      value={{
        user,
        token,
        role,
        isAuthenticated: !!token && !!user,
        isLoading,
        login,
        logout,
        restoreSession,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth(): AuthContextType {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}

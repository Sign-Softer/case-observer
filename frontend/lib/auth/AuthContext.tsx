'use client';

import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { authApi } from '../api/auth';
import { tokenStorage } from './tokenStorage';
import type { User, LoginCredentials, RegisterData, AuthState } from './types';

interface AuthContextType extends AuthState {
  login: (credentials: LoginCredentials) => Promise<void>;
  register: (data: RegisterData) => Promise<void>;
  logout: () => void;
  refreshToken: () => Promise<void>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Check if user is authenticated on mount
  useEffect(() => {
    const checkAuth = async () => {
      if (tokenStorage.hasTokens()) {
        // For now, we just check if tokens exist
        // In a real app, you might want to decode JWT to get user info
        // or have a /api/users/me endpoint
        setIsAuthenticated(true);
        // User info will be set on next login
      } else {
        setIsAuthenticated(false);
      }
      setIsLoading(false);
    };

    checkAuth();
  }, []);

  const login = useCallback(async (credentials: LoginCredentials) => {
    setIsLoading(true);
    setError(null);
    
    try {
      const response = await authApi.login(credentials);
      
      // Get user info after successful login
      // Note: Backend /api/users returns all users, so we create user from login response
      setUser({
        username: credentials.username,
        email: '', // Email not in login response, will be empty for now
        role: response.role,
      });
      
      setIsAuthenticated(true);
    } catch (err: any) {
      setError(err.message || 'Login failed');
      setIsAuthenticated(false);
      throw err;
    } finally {
      setIsLoading(false);
    }
  }, []);

  const register = useCallback(async (data: RegisterData) => {
    setIsLoading(true);
    setError(null);
    
    try {
      await authApi.register(data);
      // After registration, automatically log in
      await login({ username: data.username, password: data.password });
    } catch (err: any) {
      setError(err.message || 'Registration failed');
      throw err;
    } finally {
      setIsLoading(false);
    }
  }, [login]);

  const logout = useCallback(() => {
    authApi.logout();
    setUser(null);
    setIsAuthenticated(false);
    setError(null);
  }, []);

  const refreshToken = useCallback(async () => {
    try {
      await authApi.refreshToken();
    } catch (err) {
      // If refresh fails, logout user
      logout();
      throw err;
    }
  }, [logout]);

  const value: AuthContextType = {
    user,
    isAuthenticated,
    isLoading,
    error,
    login,
    register,
    logout,
    refreshToken,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}


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
  updateUser: (user: User) => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Check if user is authenticated on mount and load user data
  useEffect(() => {
    const checkAuth = async () => {
      if (tokenStorage.hasTokens()) {
        setIsAuthenticated(true);
        // Fetch user data if tokens exist - this ensures username and email are always present
        try {
          const { usersApi } = await import('../api/users');
          const userData = await usersApi.getCurrentUser();
          setUser({
            username: userData.username,
            email: userData.email,
            role: userData.role, // Role comes from API response
          });
        } catch (err) {
          // If fetching user fails, tokens might be invalid
          console.warn('Failed to fetch user data:', err);
          // Don't clear tokens here, let the user try to use the app
          // If API calls fail with 401, they'll be logged out automatically
        }
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
      
      // Get full user info after successful login - this ensures username and email are always present
      const { usersApi } = await import('../api/users');
      const userData = await usersApi.getCurrentUser();
      setUser({
        username: userData.username,
        email: userData.email,
        role: userData.role || response.role, // Prefer role from user API, fallback to login response
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

  const updateUser = useCallback((updatedUser: User) => {
    setUser(updatedUser);
  }, []);

  const value: AuthContextType = {
    user,
    isAuthenticated,
    isLoading,
    error,
    login,
    register,
    logout,
    refreshToken,
    updateUser,
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


import { apiClient } from './client';
import { tokenStorage } from '../auth/tokenStorage';
import type { LoginCredentials, RegisterData, AuthTokens, User } from '../auth/types';

export interface LoginResponse extends AuthTokens {
  role: string;
}

export interface RegisterResponse {
  message: string;
}

export const authApi = {
  /**
   * Login with username and password
   */
  async login(credentials: LoginCredentials): Promise<LoginResponse> {
    const response = await apiClient.post<LoginResponse>('/auth/login', credentials);
    
    // Store tokens
    tokenStorage.setTokens({
      accessToken: response.accessToken,
      refreshToken: response.refreshToken,
      tokenType: response.tokenType,
    });

    return response;
  },

  /**
   * Register a new user
   */
  async register(data: RegisterData): Promise<RegisterResponse> {
    return apiClient.post<RegisterResponse>('/auth/register', data);
  },

  /**
   * Refresh access token using refresh token
   */
  async refreshToken(): Promise<{ accessToken: string; tokenType: string }> {
    const refreshToken = tokenStorage.getRefreshToken();
    
    if (!refreshToken) {
      throw new Error('No refresh token available');
    }

    const response = await apiClient.post<{ accessToken: string; tokenType: string }>(
      '/auth/refresh',
      { refreshToken }
    );

    // Update stored access token
    const currentTokens = {
      accessToken: response.accessToken,
      refreshToken: refreshToken, // Keep the same refresh token
      tokenType: response.tokenType,
    };
    tokenStorage.setTokens(currentTokens);

    return response;
  },

  /**
   * Logout - clear tokens
   */
  logout(): void {
    tokenStorage.clearTokens();
  },

  /**
   * Get current user info
   * Note: Backend returns list of users, we'll get the first one for now
   * In production, you might want to add a /api/users/me endpoint
   */
  async getCurrentUser(): Promise<User> {
    // Backend returns array, we'll use the first user
    // This is a temporary solution - ideally backend should have /api/users/me
    const users = await apiClient.get<User[]>('/api/users');
    if (users && Array.isArray(users) && users.length > 0) {
      return users[0];
    }
    throw new Error('User not found');
  },
};


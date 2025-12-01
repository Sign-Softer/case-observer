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
   * @deprecated Use usersApi.getCurrentUser() instead
   */
  async getCurrentUser(): Promise<User> {
    // Use the new users API endpoint
    const { usersApi } = await import('./users');
    return usersApi.getCurrentUser();
  },
};


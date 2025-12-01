import { apiClient } from './client';

export interface User {
  id: number;
  username: string;
  email: string;
  role?: string; // Only for auth context, not from profile API
}

export interface UpdateProfileRequest {
  email: string;
}

export interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;
}

export interface ChangePasswordResponse {
  message: string;
}

export const usersApi = {
  /**
   * Get current authenticated user
   */
  async getCurrentUser(): Promise<User> {
    return apiClient.get<User>('/api/users/me');
  },

  /**
   * Update current user profile
   */
  async updateProfile(data: UpdateProfileRequest): Promise<User> {
    return apiClient.put<User>('/api/users/me', data);
  },

  /**
   * Change password for current user
   */
  async changePassword(data: ChangePasswordRequest): Promise<ChangePasswordResponse> {
    return apiClient.put<ChangePasswordResponse>('/api/users/me/password', data);
  },
};


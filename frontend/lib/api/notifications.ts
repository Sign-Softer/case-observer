import { apiClient } from './client';

export interface Notification {
  id: number;
  message: string;
  sentAt: string;
  caseNumber: string;
  caseId: number;
  read?: boolean;
}

export interface NotificationSettings {
  notificationIntervalMinutes: number;
  emailEnabled: boolean;
  smsEnabled: boolean;
  notifyOnHearingChanges: boolean;
  notifyOnStatusChanges: boolean;
  notifyOnPartyChanges: boolean;
  notifyOnProceduralStageChanges: boolean;
}

interface ApiResponse<T> {
  result: string;
  message: string;
  data: T;
}

export const notificationsApi = {
  /**
   * Get all notifications for the current user
   */
  async getNotifications(): Promise<Notification[]> {
    const response = await apiClient.get<ApiResponse<Notification[]>>('/api/monitoring/notifications');
    if (response.result !== 'SUCCESS') {
      throw new Error(response.message || 'Failed to fetch notifications');
    }
    return response.data || [];
  },

  /**
   * Get notifications for a specific case
   */
  async getCaseNotifications(caseId: number): Promise<Notification[]> {
    const response = await apiClient.get<ApiResponse<Notification[]>>(
      `/api/monitoring/notifications/cases/${caseId}`
    );
    if (response.result !== 'SUCCESS') {
      throw new Error(response.message || 'Failed to fetch case notifications');
    }
    return response.data || [];
  },

  /**
   * Mark a notification as read
   */
  async markAsRead(notificationId: number): Promise<void> {
    const response = await apiClient.post<ApiResponse<string>>(
      `/api/monitoring/notifications/${notificationId}/read`
    );
    if (response.result !== 'SUCCESS') {
      throw new Error(response.message || 'Failed to mark notification as read');
    }
  },

  /**
   * Get notification settings for a case
   * Note: If endpoint doesn't exist, returns default settings
   */
  async getCaseSettings(caseId: number): Promise<NotificationSettings> {
    try {
      const response = await apiClient.get<ApiResponse<NotificationSettings>>(
        `/api/monitoring/cases/${caseId}/settings`
      );
      if (response.result !== 'SUCCESS') {
        throw new Error(response.message || 'Failed to fetch notification settings');
      }
      return response.data;
    } catch (error: any) {
      // If endpoint doesn't exist (404), return default settings
      if (error.status === 404) {
        return {
          notificationIntervalMinutes: 60,
          emailEnabled: true,
          smsEnabled: false,
          notifyOnHearingChanges: true,
          notifyOnStatusChanges: true,
          notifyOnPartyChanges: true,
          notifyOnProceduralStageChanges: true,
        };
      }
      throw error;
    }
  },

  /**
   * Update notification settings for a case
   */
  async updateCaseSettings(caseId: number, settings: NotificationSettings): Promise<void> {
    const response = await apiClient.put<ApiResponse<string>>(
      `/api/monitoring/cases/${caseId}/settings`,
      settings
    );
    if (response.result !== 'SUCCESS') {
      throw new Error(response.message || 'Failed to update notification settings');
    }
  },
};


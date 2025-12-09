import { tokenStorage } from '../auth/tokenStorage';

export interface ApiError {
  message: string;
  status: number;
  statusText: string;
}

class ApiClient {
  private baseUrl: string;

  constructor() {
    // When NEXT_PUBLIC_API_URL is set, use it (behind nginx)
    // Otherwise use relative URLs (Next.js rewrites will handle it)
    this.baseUrl = process.env.NEXT_PUBLIC_API_URL || '';
  }

  private async request<T>(
    endpoint: string,
    options: RequestInit = {}
  ): Promise<T> {
    const url = `${this.baseUrl}${endpoint}`;
    
    // Add auth header if token exists
    const authHeader = tokenStorage.getAuthHeader();
    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
      ...(options.headers as Record<string, string>),
    };

    if (authHeader) {
      headers['Authorization'] = authHeader;
    }

    const config: RequestInit = {
      ...options,
      headers: headers as HeadersInit,
    };

    try {
      const response = await fetch(url, config);

      // Handle non-JSON responses
      const contentType = response.headers.get('content-type');
      const isJson = contentType?.includes('application/json');

      if (!response.ok) {
        let errorMessage = `Request failed: ${response.statusText}`;
        
        try {
          // Read response body as text first (can only read once)
          const responseText = await response.text();
          
          if (responseText) {
            // Try to parse as JSON first (backend returns JSON errors)
            try {
              const errorData = JSON.parse(responseText);
              errorMessage = errorData.message || (typeof errorData === 'string' ? errorData : errorMessage);
            } catch {
              // If JSON parsing fails, use text as-is (fallback for plain text errors)
              errorMessage = responseText;
            }
          }
        } catch (readError) {
          // If reading response fails, use default message
          console.error('Failed to read error response:', readError);
        }

        const error: ApiError = {
          message: errorMessage,
          status: response.status,
          statusText: response.statusText,
        };

        // Handle 401 Unauthorized - token might be expired
        if (response.status === 401) {
          tokenStorage.clearTokens();
          // Could trigger token refresh here in the future
        }

        throw error;
      }

      // Handle empty responses
      if (response.status === 204 || !isJson) {
        return {} as T;
      }

      return await response.json();
    } catch (error) {
      // If it's already an ApiError (has status property), re-throw it
      if (error && typeof error === 'object' && 'status' in error) {
        throw error;
      }
      
      // Network or other errors (fetch failures, etc.)
      throw {
        message: error instanceof Error ? error.message : 'Network error',
        status: 0,
        statusText: 'Network Error',
      } as ApiError;
    }
  }

  async get<T>(endpoint: string): Promise<T> {
    return this.request<T>(endpoint, { method: 'GET' });
  }

  async post<T>(endpoint: string, data?: unknown): Promise<T> {
    return this.request<T>(endpoint, {
      method: 'POST',
      body: data ? JSON.stringify(data) : undefined,
    });
  }

  async put<T>(endpoint: string, data?: unknown): Promise<T> {
    return this.request<T>(endpoint, {
      method: 'PUT',
      body: data ? JSON.stringify(data) : undefined,
    });
  }

  async delete<T>(endpoint: string): Promise<T> {
    return this.request<T>(endpoint, { method: 'DELETE' });
  }

  async patch<T>(endpoint: string, data?: unknown): Promise<T> {
    return this.request<T>(endpoint, {
      method: 'PATCH',
      body: data ? JSON.stringify(data) : undefined,
    });
  }
}

export const apiClient = new ApiClient();


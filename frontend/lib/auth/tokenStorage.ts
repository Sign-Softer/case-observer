import { AuthTokens } from './types';

// Store tokens in memory (more secure than localStorage)
// Fallback to sessionStorage for persistence across page refreshes
class TokenStorage {
  private accessToken: string | null = null;
  private refreshToken: string | null = null;
  private tokenType: string = 'Bearer';

  // Initialize from sessionStorage if available
  constructor() {
    if (typeof window !== 'undefined') {
      const stored = sessionStorage.getItem('auth_tokens');
      if (stored) {
        try {
          const tokens: AuthTokens = JSON.parse(stored);
          this.accessToken = tokens.accessToken;
          this.refreshToken = tokens.refreshToken;
          this.tokenType = tokens.tokenType || 'Bearer';
        } catch (e) {
          // Invalid stored data, clear it
          sessionStorage.removeItem('auth_tokens');
        }
      }
    }
  }

  setTokens(tokens: AuthTokens): void {
    this.accessToken = tokens.accessToken;
    this.refreshToken = tokens.refreshToken;
    this.tokenType = tokens.tokenType || 'Bearer';

    // Store in sessionStorage for persistence
    if (typeof window !== 'undefined') {
      sessionStorage.setItem('auth_tokens', JSON.stringify(tokens));
    }
  }

  getAccessToken(): string | null {
    return this.accessToken;
  }

  getRefreshToken(): string | null {
    return this.refreshToken;
  }

  getTokenType(): string {
    return this.tokenType;
  }

  getAuthHeader(): string | null {
    if (!this.accessToken) return null;
    return `${this.tokenType} ${this.accessToken}`;
  }

  clearTokens(): void {
    this.accessToken = null;
    this.refreshToken = null;
    this.tokenType = 'Bearer';

    if (typeof window !== 'undefined') {
      sessionStorage.removeItem('auth_tokens');
    }
  }

  hasTokens(): boolean {
    return !!this.accessToken && !!this.refreshToken;
  }
}

// Singleton instance
export const tokenStorage = new TokenStorage();


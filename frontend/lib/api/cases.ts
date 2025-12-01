import { apiClient } from './client';

export interface Hearing {
  id?: number;
  hearingDate?: string;        // Date of the hearing
  pronouncementDate?: string;   // Date when decision was pronounced
  judicialPanel?: string;        // Panel/judge handling the case
  solution?: string;            // Court decision/solution
  description?: string;         // Detailed description of the hearing
  // Legacy fields (for backward compatibility)
  date?: string;
  time?: string;
  type?: string;
  location?: string;
  notes?: string;
}

export interface Party {
  id?: number;
  name?: string;
  type?: string;
  role?: string;
}

export interface CourtCase {
  id: number;
  caseNumber: string;
  imposedName?: string;
  department?: string;
  proceduralStage?: string;
  category?: string;
  subject?: string;
  courtName?: string;
  status?: string;
  monitoringEnabled?: boolean;
  lastUpdated?: string;
  hearings?: Hearing[];
  parties?: Party[];
}

export interface CreateCaseRequest {
  caseNumber: string;
  institution: string;
  user?: string;
  customTitle?: string;
}

export interface CaseDetails {
  number?: string;
  institution?: string;
  department?: string;
  caseCategory?: string;
  caseCategoryName?: string;
  proceduralStage?: string;
  proceduralStageName?: string;
  subject?: string;
  modificationDateTime?: string;
  parties?: Party[];
  hearings?: Hearing[];
}

export interface CaseFilters {
  search?: string;
  status?: string;
  monitoringEnabled?: boolean;
  courtName?: string;
  sortBy?: 'lastUpdated' | 'caseNumber' | 'status';
}

export const casesApi = {
  /**
   * Get all cases for the current user with optional filters
   */
  async getCases(filters?: CaseFilters): Promise<CourtCase[]> {
    const params = new URLSearchParams();
    
    if (filters) {
      if (filters.search) params.append('search', filters.search);
      if (filters.status) params.append('status', filters.status);
      if (filters.monitoringEnabled !== undefined) {
        params.append('monitoringEnabled', filters.monitoringEnabled.toString());
      }
      if (filters.courtName) params.append('courtName', filters.courtName);
      if (filters.sortBy) params.append('sortBy', filters.sortBy);
    }
    
    const queryString = params.toString();
    const url = queryString ? `/api/cases?${queryString}` : '/api/cases';
    return apiClient.get<CourtCase[]>(url);
  },

  /**
   * Get a single case by ID
   */
  async getCase(id: number): Promise<CourtCase> {
    return apiClient.get<CourtCase>(`/api/cases/${id}`);
  },

  /**
   * Create a new case
   */
  async createCase(data: CreateCaseRequest): Promise<CourtCase> {
    return apiClient.post<CourtCase>('/api/cases', data);
  },

  /**
   * Fetch case data from portal (without saving)
   */
  async fetchCaseData(caseNumber: string, institution: string): Promise<CaseDetails> {
    return apiClient.get<CaseDetails>(
      `/api/cases/fetch?caseNumber=${encodeURIComponent(caseNumber)}&institution=${encodeURIComponent(institution)}`
    );
  },

  /**
   * Refetch and update a saved case
   */
  async refetchCase(id: number): Promise<CourtCase> {
    return apiClient.post<CourtCase>(`/api/cases/${id}/refetch`);
  },

  /**
   * Start monitoring a case
   */
  async startMonitoring(caseId: number, intervalMinutes: number = 60): Promise<void> {
    const response = await apiClient.post<{ result: string; message: string; data: any }>(
      `/api/monitoring/cases/${caseId}/start?intervalMinutes=${intervalMinutes}`
    );
    if (response.result !== 'SUCCESS') {
      throw new Error(response.message || 'Failed to start monitoring');
    }
  },

  /**
   * Stop monitoring a case
   */
  async stopMonitoring(caseId: number): Promise<void> {
    const response = await apiClient.post<{ result: string; message: string; data: any }>(
      `/api/monitoring/cases/${caseId}/stop`
    );
    if (response.result !== 'SUCCESS') {
      throw new Error(response.message || 'Failed to stop monitoring');
    }
  },
};


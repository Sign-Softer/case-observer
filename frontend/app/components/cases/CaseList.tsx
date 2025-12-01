'use client';

import { useEffect, useState, useMemo, useCallback, useRef } from 'react';
import { casesApi, type CourtCase, type CaseFilters } from '../../../lib/api/cases';
import CaseCard from './CaseCard';
import CaseSearchBar from './CaseSearchBar';
import CaseFiltersComponent from './CaseFilters';
import Button from '../common/Button';

export default function CaseList() {
  const [cases, setCases] = useState<CourtCase[]>([]);
  const [allCases, setAllCases] = useState<CourtCase[]>([]); // Store all cases for filter options
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [filters, setFilters] = useState<CaseFilters>({ sortBy: 'lastUpdated' });
  const [searchTerm, setSearchTerm] = useState('');
  const filtersRef = useRef<CaseFilters>({ sortBy: 'lastUpdated' });
  const searchTermRef = useRef<string>('');
  const prevFiltersStringRef = useRef<string>('');
  const prevSearchTermRef = useRef<string>('');

  // Extract unique values for filter dropdowns
  const availableCourts = useMemo(() => {
    const courts = new Set<string>();
    allCases.forEach((c) => {
      if (c.courtName) courts.add(c.courtName);
    });
    return Array.from(courts).sort();
  }, [allCases]);

  const availableStatuses = useMemo(() => {
    const statuses = new Set<string>();
    allCases.forEach((c) => {
      if (c.status) statuses.add(c.status);
    });
    return Array.from(statuses).sort();
  }, [allCases]);

  // Load all cases once to populate filter options
  useEffect(() => {
    loadAllCases();
  }, []);

  // Load filtered cases when filters or search change
  useEffect(() => {
    // Compare by value, not reference, to prevent infinite loops
    const filtersString = JSON.stringify(filters);
    const filtersChanged = filtersString !== prevFiltersStringRef.current;
    const searchChanged = searchTerm !== prevSearchTermRef.current;
    
    if (filtersChanged || searchChanged) {
      // Update refs
      filtersRef.current = filters;
      searchTermRef.current = searchTerm;
      prevFiltersStringRef.current = filtersString;
      prevSearchTermRef.current = searchTerm;
      
      // Load cases
      loadCases();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [filters, searchTerm]);

  const loadAllCases = async () => {
    try {
      const data = await casesApi.getCases();
      setAllCases(data);
    } catch (err: any) {
      console.error('Error loading all cases for filters:', err);
    }
  };

  const loadCases = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const filtersWithSearch: CaseFilters = {
        ...filtersRef.current,
        search: searchTermRef.current || undefined,
      };
      const data = await casesApi.getCases(filtersWithSearch);
      setCases(data);
    } catch (err: any) {
      setError(err.message || 'Failed to load cases');
      console.error('Error loading cases:', err);
    } finally {
      setLoading(false);
    }
  }, []);

  const handleFiltersChange = useCallback((newFilters: CaseFilters) => {
    setFilters(newFilters);
  }, []);

  if (loading) {
    return (
      <div className="flex items-center justify-center py-12">
        <div className="text-center">
          <svg
            className="animate-spin h-8 w-8 text-blue-600 mx-auto mb-4"
            xmlns="http://www.w3.org/2000/svg"
            fill="none"
            viewBox="0 0 24 24"
          >
            <circle
              className="opacity-25"
              cx="12"
              cy="12"
              r="10"
              stroke="currentColor"
              strokeWidth="4"
            />
            <path
              className="opacity-75"
              fill="currentColor"
              d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
            />
          </svg>
          <p className="text-gray-600">Loading cases...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="bg-red-50 border border-red-200 rounded-lg p-6 text-center">
        <p className="text-red-800 mb-4">{error}</p>
        <Button onClick={loadCases} variant="outline" size="sm">
          Try Again
        </Button>
      </div>
    );
  }

  if (cases.length === 0) {
    return (
      <div className="bg-white rounded-lg shadow p-8 sm:p-12 text-center">
        <svg
          className="mx-auto h-12 w-12 text-gray-400 mb-4"
          fill="none"
          stroke="currentColor"
          viewBox="0 0 24 24"
        >
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth={2}
            d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"
          />
        </svg>
        <h3 className="text-lg font-semibold text-gray-900 mb-2">No cases yet</h3>
        <p className="text-gray-600 mb-6">
          Start monitoring your first court case to get instant updates on status changes, hearings, and more.
        </p>
        <a href="/cases/add">
          <Button variant="primary">Add Your First Case</Button>
        </a>
      </div>
    );
  }

  return (
    <div className="space-y-4">
      {/* Search and Filters */}
      <div className="space-y-4">
        <CaseSearchBar onSearch={setSearchTerm} />
        <CaseFiltersComponent
          onFiltersChange={handleFiltersChange}
          availableCourts={availableCourts}
          availableStatuses={availableStatuses}
        />
      </div>

      {/* Header and Add Button */}
      <div className="flex items-center justify-between mb-6">
        <h2 className="text-xl sm:text-2xl font-bold text-gray-900">
          My Cases ({cases.length})
        </h2>
        <a href="/cases/add">
          <Button variant="primary" size="sm">
            + Add Case
          </Button>
        </a>
      </div>

      {/* Cases Grid */}
      {loading ? (
        <div className="flex items-center justify-center py-12">
          <div className="text-center">
            <svg
              className="animate-spin h-8 w-8 text-blue-600 mx-auto mb-4"
              xmlns="http://www.w3.org/2000/svg"
              fill="none"
              viewBox="0 0 24 24"
            >
              <circle
                className="opacity-25"
                cx="12"
                cy="12"
                r="10"
                stroke="currentColor"
                strokeWidth="4"
              />
              <path
                className="opacity-75"
                fill="currentColor"
                d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
              />
            </svg>
            <p className="text-gray-600">Loading cases...</p>
          </div>
        </div>
      ) : error ? (
        <div className="bg-red-50 border border-red-200 rounded-lg p-6 text-center">
          <p className="text-red-800 mb-4">{error}</p>
          <Button onClick={loadCases} variant="outline" size="sm">
            Try Again
          </Button>
        </div>
      ) : cases.length === 0 ? (
        <div className="bg-white rounded-lg shadow p-8 sm:p-12 text-center">
          <svg
            className="mx-auto h-12 w-12 text-gray-400 mb-4"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"
            />
          </svg>
          <h3 className="text-lg font-semibold text-gray-900 mb-2">
            {searchTerm || Object.keys(filters).some((k) => filters[k as keyof CaseFilters]) 
              ? 'No cases match your filters' 
              : 'No cases yet'}
          </h3>
          <p className="text-gray-600 mb-6">
            {searchTerm || Object.keys(filters).some((k) => filters[k as keyof CaseFilters])
              ? 'Try adjusting your search or filters to see more results.'
              : 'Start monitoring your first court case to get instant updates on status changes, hearings, and more.'}
          </p>
          {!searchTerm && !Object.keys(filters).some((k) => filters[k as keyof CaseFilters]) && (
            <a href="/cases/add">
              <Button variant="primary">Add Your First Case</Button>
            </a>
          )}
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4 sm:gap-6">
          {cases.map((caseData) => (
            <CaseCard key={caseData.id} caseData={caseData} />
          ))}
        </div>
      )}
    </div>
  );
}


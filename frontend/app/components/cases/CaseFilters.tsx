'use client';

import { useState, useEffect, useRef } from 'react';
import type { CaseFilters } from '../../../lib/api/cases';

interface CaseFiltersProps {
  onFiltersChange: (filters: CaseFilters) => void;
  availableCourts?: string[];
  availableStatuses?: string[];
}

export default function CaseFiltersComponent({
  onFiltersChange,
  availableCourts = [],
  availableStatuses = [],
}: CaseFiltersProps) {
  const [filters, setFilters] = useState<CaseFilters>({
    status: undefined,
    monitoringEnabled: undefined,
    courtName: undefined,
    sortBy: 'lastUpdated',
  });
  const [showFilters, setShowFilters] = useState(false);
  const prevFiltersRef = useRef<string>('');
  const isInitialMount = useRef(true);

  useEffect(() => {
    // Skip the initial mount to prevent calling onFiltersChange on first render
    if (isInitialMount.current) {
      isInitialMount.current = false;
      // Set the initial value in the ref so we can compare properly next time
      prevFiltersRef.current = JSON.stringify(filters);
      // Still call onFiltersChange once on mount to sync initial state
      onFiltersChange(filters);
      return;
    }

    // Only call onFiltersChange if filters actually changed (by value, not reference)
    const filtersString = JSON.stringify(filters);
    if (filtersString !== prevFiltersRef.current) {
      prevFiltersRef.current = filtersString;
      onFiltersChange(filters);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [filters]);

  const handleFilterChange = (key: keyof CaseFilters, value: any) => {
    setFilters((prev) => ({
      ...prev,
      [key]: value === '' || value === 'all' ? undefined : value,
    }));
  };

  const handleClearFilters = () => {
    setFilters({
      status: undefined,
      monitoringEnabled: undefined,
      courtName: undefined,
      sortBy: 'lastUpdated',
    });
  };

  const hasActiveFilters = filters.status || filters.monitoringEnabled !== undefined || filters.courtName;

  return (
    <div className="space-y-4">
      {/* Filter Toggle Button */}
      <div className="flex items-center justify-between">
        <button
          onClick={() => setShowFilters(!showFilters)}
          className="flex items-center gap-2 px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors"
        >
          <svg
            className={`w-4 h-4 transition-transform ${showFilters ? 'rotate-180' : ''}`}
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
          </svg>
          Filters
          {hasActiveFilters && (
            <span className="ml-1 px-2 py-0.5 text-xs font-semibold bg-blue-100 text-blue-800 rounded-full">
              Active
            </span>
          )}
        </button>

        {/* Sort Dropdown */}
        <div className="flex items-center gap-2">
          <label htmlFor="sortBy" className="text-sm text-gray-700 whitespace-nowrap">
            Sort by:
          </label>
          <select
            id="sortBy"
            value={filters.sortBy || 'lastUpdated'}
            onChange={(e) => handleFilterChange('sortBy', e.target.value)}
            className="px-3 py-2 text-sm border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
          >
            <option value="lastUpdated">Last Updated</option>
            <option value="caseNumber">Case Number</option>
            <option value="status">Status</option>
          </select>
        </div>
      </div>

      {/* Filter Panel */}
      {showFilters && (
        <div className="bg-gray-50 border border-gray-200 rounded-lg p-4 space-y-4">
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
            {/* Status Filter */}
            {availableStatuses.length > 0 && (
              <div>
                <label htmlFor="status" className="block text-sm font-medium text-gray-700 mb-1">
                  Status
                </label>
                <select
                  id="status"
                  value={filters.status || 'all'}
                  onChange={(e) => handleFilterChange('status', e.target.value)}
                  className="w-full px-3 py-2 text-sm border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                >
                  <option value="all">All Statuses</option>
                  {availableStatuses.map((status) => (
                    <option key={status} value={status}>
                      {status}
                    </option>
                  ))}
                </select>
              </div>
            )}

            {/* Monitoring Filter */}
            <div>
              <label htmlFor="monitoring" className="block text-sm font-medium text-gray-700 mb-1">
                Monitoring
              </label>
              <select
                id="monitoring"
                value={
                  filters.monitoringEnabled === undefined
                    ? 'all'
                    : filters.monitoringEnabled
                    ? 'on'
                    : 'off'
                }
                onChange={(e) => {
                  const value = e.target.value;
                  handleFilterChange(
                    'monitoringEnabled',
                    value === 'all' ? undefined : value === 'on'
                  );
                }}
                className="w-full px-3 py-2 text-sm border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
              >
                <option value="all">All</option>
                <option value="on">ON</option>
                <option value="off">OFF</option>
              </select>
            </div>

            {/* Court Filter */}
            {availableCourts.length > 0 && (
              <div>
                <label htmlFor="court" className="block text-sm font-medium text-gray-700 mb-1">
                  Court
                </label>
                <select
                  id="court"
                  value={filters.courtName || 'all'}
                  onChange={(e) => handleFilterChange('courtName', e.target.value)}
                  className="w-full px-3 py-2 text-sm border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                >
                  <option value="all">All Courts</option>
                  {availableCourts.map((court) => (
                    <option key={court} value={court}>
                      {court}
                    </option>
                  ))}
                </select>
              </div>
            )}
          </div>

          {/* Clear Filters Button */}
          {hasActiveFilters && (
            <div className="pt-2 border-t border-gray-200">
              <button
                onClick={handleClearFilters}
                className="text-sm text-blue-600 hover:text-blue-700 font-medium"
              >
                Clear all filters
              </button>
            </div>
          )}
        </div>
      )}
    </div>
  );
}


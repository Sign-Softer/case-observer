'use client';

import { useState } from 'react';

interface SearchResult {
  caseNumber: string;
  institution: string;
  status?: string;
  error?: string;
}

export default function CaseSearch() {
  const [caseNumber, setCaseNumber] = useState('');
  const [institution, setInstitution] = useState('');
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState<SearchResult | null>(null);

  const handleSearch = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!caseNumber || !institution) return;

    setLoading(true);
    setResult(null);

    try {
      const response = await fetch(
        `/api/cases/fetch?caseNumber=${encodeURIComponent(caseNumber)}&institution=${encodeURIComponent(institution)}`
      );

      if (response.ok) {
        const data = await response.json();
        setResult({
          caseNumber,
          institution,
          status: data.status || 'Found',
        });
      } else {
        setResult({
          caseNumber,
          institution,
          error: 'Case not found or error occurred',
        });
      }
    } catch (error) {
      setResult({
        caseNumber,
        institution,
        error: 'Unable to connect to the service',
      });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="w-full max-w-2xl mx-auto px-4 sm:px-0">
      <form onSubmit={handleSearch} className="space-y-4">
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <div>
            <label htmlFor="caseNumber" className="block text-sm font-medium text-gray-700 mb-2">
              Case Number
            </label>
            <input
              id="caseNumber"
              type="text"
              value={caseNumber}
              onChange={(e) => setCaseNumber(e.target.value)}
              placeholder="e.g., 12345/2025"
              className="w-full px-3 sm:px-4 py-2 sm:py-3 text-sm sm:text-base border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              required
            />
          </div>
          <div>
            <label htmlFor="institution" className="block text-sm font-medium text-gray-700 mb-2">
              Court Institution
            </label>
            <input
              id="institution"
              type="text"
              value={institution}
              onChange={(e) => setInstitution(e.target.value)}
              placeholder="e.g., TRIBUNALUL_BUCURESTI"
              className="w-full px-3 sm:px-4 py-2 sm:py-3 text-sm sm:text-base border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              required
            />
          </div>
        </div>
        <button
          type="submit"
          disabled={loading}
          className="w-full sm:w-auto px-6 sm:px-8 py-2 sm:py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors font-semibold text-sm sm:text-base disabled:bg-gray-400 disabled:cursor-not-allowed"
        >
          {loading ? 'Searching...' : 'Search Case'}
        </button>
      </form>

      {result && (
        <div className={`mt-4 sm:mt-6 p-3 sm:p-4 rounded-lg ${
          result.error 
            ? 'bg-red-50 border border-red-200' 
            : 'bg-green-50 border border-green-200'
        }`}>
          {result.error ? (
            <div className="flex items-center gap-2 text-sm sm:text-base text-red-800">
              <svg className="w-4 h-4 sm:w-5 sm:h-5 flex-shrink-0" fill="currentColor" viewBox="0 0 20 20">
                <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clipRule="evenodd" />
              </svg>
              <span>{result.error}</span>
            </div>
          ) : (
            <div className="flex items-center gap-2 text-sm sm:text-base text-green-800">
              <svg className="w-4 h-4 sm:w-5 sm:h-5 flex-shrink-0" fill="currentColor" viewBox="0 0 20 20">
                <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clipRule="evenodd" />
              </svg>
              <span>Case found! Status: {result.status}</span>
            </div>
          )}
        </div>
      )}

      <p className="mt-4 text-xs sm:text-sm text-gray-500 text-center px-4">
        Try searching for a case to see how it works. <a href="/register" className="text-blue-600 hover:underline font-medium">Sign up</a> to start monitoring cases automatically.
      </p>
    </div>
  );
}


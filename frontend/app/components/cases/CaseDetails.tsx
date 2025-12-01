'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { casesApi, type CourtCase } from '../../../lib/api/cases';
import Button from '../common/Button';
import MonitoringToggle from './MonitoringToggle';
import CaseParties from './CaseParties';
import CaseHearings from './CaseHearings';
import NotificationSettings from '../notifications/NotificationSettings';

interface CaseDetailsProps {
  caseId: number;
}

export default function CaseDetails({ caseId }: CaseDetailsProps) {
  const router = useRouter();
  const [caseData, setCaseData] = useState<CourtCase | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    loadCase();
  }, [caseId]);

  const loadCase = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await casesApi.getCase(caseId);
      setCaseData(data);
    } catch (err: any) {
      setError(err.message || 'Failed to load case details');
      console.error('Error loading case:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleRefetch = async () => {
    try {
      const updated = await casesApi.refetchCase(caseId);
      setCaseData(updated);
    } catch (err: any) {
      console.error('Error refetching case:', err);
      alert('Failed to refetch case data');
    }
  };

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
          <p className="text-gray-600">Loading case details...</p>
        </div>
      </div>
    );
  }

  if (error || !caseData) {
    return (
      <div className="bg-red-50 border border-red-200 rounded-lg p-6 text-center">
        <p className="text-red-800 mb-4">{error || 'Case not found'}</p>
        <div className="flex gap-3 justify-center">
          <Button onClick={() => router.push('/dashboard')} variant="outline" size="sm">
            Back to Dashboard
          </Button>
          <Button onClick={loadCase} variant="primary" size="sm">
            Try Again
          </Button>
        </div>
      </div>
    );
  }

  const formatDate = (dateString?: string) => {
    if (!dateString) return 'N/A';
    try {
      const date = new Date(dateString);
      return new Intl.DateTimeFormat('en-US', {
        month: 'long',
        day: 'numeric',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
      }).format(date);
    } catch {
      return dateString;
    }
  };

  const getStatusColor = (status?: string) => {
    if (!status) return 'bg-gray-100 text-gray-800';
    const statusLower = status.toLowerCase();
    if (statusLower.includes('fond') || statusLower.includes('closed')) {
      return 'bg-green-100 text-green-800';
    }
    if (statusLower.includes('procedura') || statusLower.includes('active')) {
      return 'bg-blue-100 text-blue-800';
    }
    if (statusLower.includes('suspended') || statusLower.includes('paused')) {
      return 'bg-yellow-100 text-yellow-800';
    }
    return 'bg-gray-100 text-gray-800';
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <button
            onClick={() => router.push('/dashboard')}
            className="text-blue-600 hover:text-blue-700 text-sm font-medium mb-2 flex items-center gap-1"
          >
            <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
            </svg>
            Back to Cases
          </button>
          <h1 className="text-2xl sm:text-3xl font-bold text-gray-900">
            {caseData.caseNumber}
          </h1>
          {caseData.imposedName && (
            <p className="text-lg text-gray-600 mt-1">{caseData.imposedName}</p>
          )}
        </div>
        <div className="flex gap-3">
          <Button onClick={handleRefetch} variant="outline" size="sm">
            Refresh Data
          </Button>
        </div>
      </div>

      {/* Case Overview Card */}
      <div className="bg-white rounded-lg shadow-md p-6">
        <h2 className="text-xl font-semibold text-gray-900 mb-4">Case Overview</h2>
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
          <div>
            <p className="text-sm text-gray-500 mb-1">Court</p>
            <p className="text-base font-medium text-gray-900">{caseData.courtName || 'N/A'}</p>
          </div>
          <div>
            <p className="text-sm text-gray-500 mb-1">Status</p>
            <span className={`inline-block px-3 py-1 text-sm font-semibold rounded-full ${getStatusColor(caseData.status)}`}>
              {caseData.status || 'Unknown'}
            </span>
          </div>
          <div>
            <p className="text-sm text-gray-500 mb-1">Monitoring</p>
            <span className={`inline-block px-3 py-1 text-sm font-semibold rounded-full ${
              caseData.monitoringEnabled 
                ? 'bg-green-100 text-green-800' 
                : 'bg-gray-100 text-gray-600'
            }`}>
              {caseData.monitoringEnabled ? 'ON' : 'OFF'}
            </span>
          </div>
          <div>
            <p className="text-sm text-gray-500 mb-1">Category</p>
            <p className="text-base font-medium text-gray-900">{caseData.category || 'N/A'}</p>
          </div>
          <div>
            <p className="text-sm text-gray-500 mb-1">Department</p>
            <p className="text-base font-medium text-gray-900">{caseData.department || 'N/A'}</p>
          </div>
          <div>
            <p className="text-sm text-gray-500 mb-1">Procedural Stage</p>
            <p className="text-base font-medium text-gray-900">{caseData.proceduralStage || 'N/A'}</p>
          </div>
          <div>
            <p className="text-sm text-gray-500 mb-1">Last Updated</p>
            <p className="text-base font-medium text-gray-900">{formatDate(caseData.lastUpdated)}</p>
          </div>
        </div>

        {caseData.subject && (
          <div className="mt-4 pt-4 border-t border-gray-200">
            <p className="text-sm text-gray-500 mb-1">Subject</p>
            <p className="text-base text-gray-900">{caseData.subject}</p>
          </div>
        )}

        {/* Monitoring Controls */}
        <div className="mt-6 pt-6 border-t border-gray-200">
          <MonitoringToggle caseId={caseId} monitoringEnabled={caseData.monitoringEnabled || false} onUpdate={loadCase} />
        </div>
      </div>

      {/* Notification Settings - Only show if monitoring is enabled */}
      {caseData.monitoringEnabled && (
        <NotificationSettings caseId={caseId} />
      )}

      {/* Parties */}
      {caseData.parties && caseData.parties.length > 0 && (
        <CaseParties parties={caseData.parties} />
      )}

      {/* Hearings */}
      {caseData.hearings && caseData.hearings.length > 0 && (
        <CaseHearings hearings={caseData.hearings} />
      )}
    </div>
  );
}


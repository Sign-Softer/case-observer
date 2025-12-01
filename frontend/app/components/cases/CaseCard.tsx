import Link from 'next/link';
import type { CourtCase } from '../../../lib/api/cases';

interface CaseCardProps {
  caseData: CourtCase;
}

export default function CaseCard({ caseData }: CaseCardProps) {
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

  const formatDate = (dateString?: string) => {
    if (!dateString) return 'N/A';
    try {
      const date = new Date(dateString);
      return new Intl.DateTimeFormat('en-US', {
        month: 'short',
        day: 'numeric',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
      }).format(date);
    } catch {
      return dateString;
    }
  };

  return (
    <a href={`/cases/${caseData.id}`}>
      <div className="bg-white rounded-lg shadow-md hover:shadow-lg transition-shadow p-4 sm:p-6 cursor-pointer border border-gray-200">
        <div className="flex items-start justify-between mb-4">
          <div className="flex-1">
            <h3 className="text-lg sm:text-xl font-semibold text-gray-900 mb-1">
              {caseData.caseNumber}
            </h3>
            {caseData.imposedName && (
              <p className="text-sm text-gray-600 mb-2">{caseData.imposedName}</p>
            )}
            <p className="text-sm font-medium text-gray-700">{caseData.courtName || 'Unknown Court'}</p>
          </div>
          <div className="flex flex-col items-end gap-2">
            <span className={`px-2 py-1 text-xs font-semibold rounded-full ${
              caseData.monitoringEnabled 
                ? 'bg-green-100 text-green-800' 
                : 'bg-gray-100 text-gray-600'
            }`}>
              Monitoring: {caseData.monitoringEnabled ? 'ON' : 'OFF'}
            </span>
            <span className={`px-2 py-1 text-xs font-semibold rounded-full ${getStatusColor(caseData.status)}`}>
              {caseData.status || 'Unknown'}
            </span>
          </div>
        </div>

        <div className="space-y-2 text-sm text-gray-600">
          {caseData.subject && (
            <div className="flex items-start gap-2">
              <span className="font-medium min-w-[80px]">Subject:</span>
              <span className="flex-1">{caseData.subject}</span>
            </div>
          )}
          {caseData.proceduralStage && (
            <div className="flex items-start gap-2">
              <span className="font-medium min-w-[80px]">Stage:</span>
              <span className="flex-1">{caseData.proceduralStage}</span>
            </div>
          )}
          {caseData.lastUpdated && (
            <div className="flex items-start gap-2">
              <span className="font-medium min-w-[80px]">Updated:</span>
              <span className="flex-1">{formatDate(caseData.lastUpdated)}</span>
            </div>
          )}
        </div>

        {caseData.parties && caseData.parties.length > 0 && (
          <div className="mt-4 pt-4 border-t border-gray-200">
            <p className="text-xs text-gray-500 mb-2">
              {caseData.parties.length} {caseData.parties.length === 1 ? 'party' : 'parties'}
            </p>
          </div>
        )}

        {caseData.hearings && caseData.hearings.length > 0 && (
          <div className="mt-2">
            <p className="text-xs text-gray-500">
              {caseData.hearings.length} {caseData.hearings.length === 1 ? 'hearing' : 'hearings'} scheduled
            </p>
          </div>
        )}
      </div>
    </a>
  );
}


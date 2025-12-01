import type { Hearing } from '../../../lib/api/cases';

interface CaseHearingsProps {
  hearings: Hearing[];
}

export default function CaseHearings({ hearings }: CaseHearingsProps) {
  const formatDate = (dateString?: string) => {
    if (!dateString) return 'Date not specified';
    try {
      const date = new Date(dateString);
      return new Intl.DateTimeFormat('en-US', {
        weekday: 'long',
        year: 'numeric',
        month: 'long',
        day: 'numeric',
      }).format(date);
    } catch {
      return dateString;
    }
  };

  const formatDateTime = (dateString?: string) => {
    if (!dateString) return null;
    try {
      const date = new Date(dateString);
      return new Intl.DateTimeFormat('en-US', {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
      }).format(date);
    } catch {
      return dateString;
    }
  };

  const getSolutionColor = (solution?: string) => {
    if (!solution) return 'bg-gray-100 text-gray-800';
    const sol = solution.toLowerCase();
    if (sol.includes('amână') || sol.includes('postpone')) {
      return 'bg-yellow-100 text-yellow-800';
    }
    if (sol.includes('încheiere') || sol.includes('decision')) {
      return 'bg-green-100 text-green-800';
    }
    if (sol.includes('pronunțare') || sol.includes('pronouncement')) {
      return 'bg-blue-100 text-blue-800';
    }
    return 'bg-gray-100 text-gray-800';
  };

  // Sort hearings by date (most recent first)
  const sortedHearings = [...hearings].sort((a, b) => {
    const dateA = a.hearingDate || a.date || '';
    const dateB = b.hearingDate || b.date || '';
    return dateB.localeCompare(dateA);
  });

  return (
    <div className="bg-white rounded-lg shadow-md p-6">
      <h2 className="text-xl font-semibold text-gray-900 mb-4">
        Hearings ({hearings.length})
      </h2>
      <div className="space-y-4">
        {sortedHearings.map((hearing, index) => {
          const hearingDate = hearing.hearingDate || hearing.date;
          const pronouncementDate = hearing.pronouncementDate;
          
          return (
            <div
              key={hearing.id || index}
              className="flex items-start gap-4 p-4 border-l-4 border-blue-500 bg-blue-50 rounded-r-lg hover:bg-blue-100 transition-colors"
            >
              <div className="flex-shrink-0">
                <div className="w-10 h-10 bg-blue-600 rounded-full flex items-center justify-center">
                  <svg className="w-5 h-5 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                  </svg>
                </div>
              </div>
              <div className="flex-1 min-w-0">
                <div className="flex flex-col sm:flex-row sm:items-start sm:justify-between gap-3">
                  <div className="flex-1">
                    <div className="flex flex-col gap-2">
                      <div>
                        <p className="text-base font-semibold text-gray-900">
                          Hearing Date: {formatDate(hearingDate)}
                        </p>
                        {pronouncementDate && pronouncementDate !== hearingDate && (
                          <p className="text-sm text-gray-600 mt-1">
                            Pronouncement: {formatDateTime(pronouncementDate)}
                          </p>
                        )}
                      </div>
                      
                      {hearing.judicialPanel && (
                        <p className="text-sm text-gray-700 mt-1">
                          <span className="font-medium">Panel:</span> {hearing.judicialPanel}
                        </p>
                      )}
                    </div>
                  </div>
                  
                  {hearing.solution && (
                    <span className={`px-3 py-1 text-xs font-semibold rounded-full whitespace-nowrap ${getSolutionColor(hearing.solution)}`}>
                      {hearing.solution}
                    </span>
                  )}
                </div>
                
                {hearing.description && (
                  <div className="mt-3 pt-3 border-t border-blue-200">
                    <p className="text-sm text-gray-700 whitespace-pre-wrap">{hearing.description}</p>
                  </div>
                )}
                
                {/* Legacy fields support */}
                {!hearing.description && hearing.notes && (
                  <div className="mt-3 pt-3 border-t border-blue-200">
                    <p className="text-sm text-gray-700">{hearing.notes}</p>
                  </div>
                )}
                
                {hearing.location && (
                  <p className="text-sm text-gray-600 mt-2">
                    <span className="font-medium">Location:</span> {hearing.location}
                  </p>
                )}
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}


import type { Party } from '../../../lib/api/cases';

interface CasePartiesProps {
  parties: Party[];
}

export default function CaseParties({ parties }: CasePartiesProps) {
  return (
    <div className="bg-white rounded-lg shadow-md p-6">
      <h2 className="text-xl font-semibold text-gray-900 mb-4">
        Parties ({parties.length})
      </h2>
      <div className="space-y-3">
        {parties.map((party, index) => (
          <div
            key={party.id || index}
            className="flex items-start gap-4 p-4 bg-gray-50 rounded-lg border border-gray-200"
          >
            <div className="flex-shrink-0 w-10 h-10 bg-blue-100 rounded-full flex items-center justify-center">
              <span className="text-blue-600 font-semibold text-sm">
                {party.name?.charAt(0).toUpperCase() || '?'}
              </span>
            </div>
            <div className="flex-1 min-w-0">
              <p className="text-base font-medium text-gray-900 truncate">
                {party.name || 'Unknown Party'}
              </p>
              {party.role && (
                <p className="text-sm text-gray-600 mt-1">
                  Role: <span className="font-medium">{party.role}</span>
                </p>
              )}
              {party.type && (
                <p className="text-xs text-gray-500 mt-1">
                  Type: {party.type}
                </p>
              )}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}


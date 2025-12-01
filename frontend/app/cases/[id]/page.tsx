import ProtectedRoute from '../../components/auth/ProtectedRoute';
import DashboardLayout from '../../components/layout/DashboardLayout';
import CaseDetails from '../../components/cases/CaseDetails';

interface CaseDetailsPageProps {
  params: {
    id: string;
  };
}

export default function CaseDetailsPage({ params }: CaseDetailsPageProps) {
  const caseId = parseInt(params.id, 10);

  if (isNaN(caseId)) {
    return (
      <ProtectedRoute>
        <DashboardLayout>
          <div className="bg-red-50 border border-red-200 rounded-lg p-6 text-center">
            <p className="text-red-800">Invalid case ID</p>
            <a href="/dashboard" className="text-blue-600 hover:underline mt-4 inline-block">
              Back to Dashboard
            </a>
          </div>
        </DashboardLayout>
      </ProtectedRoute>
    );
  }

  return (
    <ProtectedRoute>
      <DashboardLayout>
        <div className="max-w-7xl mx-auto">
          <CaseDetails caseId={caseId} />
        </div>
      </DashboardLayout>
    </ProtectedRoute>
  );
}


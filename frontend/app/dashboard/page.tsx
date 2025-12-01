import ProtectedRoute from '../components/auth/ProtectedRoute';
import DashboardLayout from '../components/layout/DashboardLayout';
import CaseList from '../components/cases/CaseList';

export default function DashboardPage() {
  return (
    <ProtectedRoute>
      <DashboardLayout>
        <div className="max-w-7xl mx-auto">
          <div className="mb-6 sm:mb-8">
            <h1 className="text-2xl sm:text-3xl font-bold text-gray-900 mb-2">
              Dashboard
            </h1>
            <p className="text-sm sm:text-base text-gray-600">
              Monitor and track all your court cases in one place
            </p>
          </div>
          <CaseList />
        </div>
      </DashboardLayout>
    </ProtectedRoute>
  );
}


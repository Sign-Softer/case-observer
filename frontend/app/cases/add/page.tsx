import ProtectedRoute from '../../components/auth/ProtectedRoute';
import DashboardLayout from '../../components/layout/DashboardLayout';
import AddCaseForm from '../../components/cases/AddCaseForm';

export default function AddCasePage() {
  return (
    <ProtectedRoute>
      <DashboardLayout>
        <div className="max-w-3xl mx-auto">
          <div className="mb-6 sm:mb-8">
            <h1 className="text-2xl sm:text-3xl font-bold text-gray-900 mb-2">
              Add New Case
            </h1>
            <p className="text-sm sm:text-base text-gray-600">
              Enter case details to start monitoring. We'll fetch the latest information from the court portal.
            </p>
          </div>
          <AddCaseForm />
        </div>
      </DashboardLayout>
    </ProtectedRoute>
  );
}


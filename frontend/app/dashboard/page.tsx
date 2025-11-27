import ProtectedRoute from '../components/auth/ProtectedRoute';

export default function DashboardPage() {
  return (
    <ProtectedRoute>
      <div className="min-h-screen bg-gray-50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
          <h1 className="text-2xl sm:text-3xl font-bold text-gray-900 mb-6">
            Dashboard
          </h1>
          <div className="bg-white rounded-lg shadow p-6">
            <p className="text-gray-600">
              Welcome to your dashboard! This is where you'll see all your monitored cases.
            </p>
            <p className="text-gray-600 mt-4">
              Case list and management features coming soon...
            </p>
          </div>
        </div>
      </div>
    </ProtectedRoute>
  );
}


'use client';

import { useEffect, useState } from 'react';
import { usersApi, type User } from '../../lib/api/users';
import DashboardLayout from '../components/layout/DashboardLayout';
import ProtectedRoute from '../components/auth/ProtectedRoute';
import ProfileForm from '../components/settings/ProfileForm';
import PasswordChangeForm from '../components/settings/PasswordChangeForm';
import { useAuth } from '../../lib/auth/AuthContext';
import Button from '../components/common/Button';
import { useRouter } from 'next/navigation';

export default function SettingsPage() {
  const { user: authUser, logout, updateUser } = useAuth();
  const router = useRouter();
  const [user, setUser] = useState<User | null>(authUser); // Initialize with authUser from context
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [activeTab, setActiveTab] = useState<'profile' | 'password'>('profile');

  useEffect(() => {
    // If authUser is available, set it directly, no need to load again
    if (authUser) {
      setUser(authUser);
      setLoading(false);
    } else {
      // If not in context, try to load (e.g., on direct page access)
      loadUser();
    }
  }, [authUser]); // Depend on authUser

  const loadUser = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await usersApi.getCurrentUser();
      setUser(data);
      updateUser(data); // Update context with full user data
    } catch (err: any) {
      setError(err.message || 'Failed to load user profile');
      console.error('Error loading user:', err);
      // If 401, redirect to login
      if (err.status === 401) {
        logout();
        router.push('/login');
      }
    } finally {
      setLoading(false);
    }
  };

  const handleProfileUpdate = (updatedUser: User) => {
    setUser(updatedUser);
    updateUser(updatedUser); // Update auth context with full user data including role
  };

  if (loading) {
    return (
      <ProtectedRoute>
        <DashboardLayout>
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
              <p className="text-gray-600">Loading settings...</p>
            </div>
          </div>
        </DashboardLayout>
      </ProtectedRoute>
    );
  }

  if (error || !user) {
    return (
      <ProtectedRoute>
        <DashboardLayout>
          <div className="bg-red-50 border border-red-200 rounded-lg p-6 text-center">
            <p className="text-red-800 mb-4">{error || 'Failed to load user profile'}</p>
            <Button onClick={loadUser} variant="outline" size="sm">
              Try Again
            </Button>
          </div>
        </DashboardLayout>
      </ProtectedRoute>
    );
  }

  return (
    <ProtectedRoute>
      <DashboardLayout>
        <div className="space-y-6">
          {/* Header */}
          <div>
            <h1 className="text-2xl sm:text-3xl font-bold text-gray-900">Settings</h1>
            <p className="text-gray-600 mt-2">Manage your account settings and preferences</p>
          </div>

          {/* Tabs */}
          <div className="border-b border-gray-200">
            <nav className="flex space-x-8">
              <button
                onClick={() => setActiveTab('profile')}
                className={`py-4 px-1 border-b-2 font-medium text-sm ${
                  activeTab === 'profile'
                    ? 'border-blue-500 text-blue-600'
                    : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                }`}
              >
                Profile
              </button>
              <button
                onClick={() => setActiveTab('password')}
                className={`py-4 px-1 border-b-2 font-medium text-sm ${
                  activeTab === 'password'
                    ? 'border-blue-500 text-blue-600'
                    : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                }`}
              >
                Password
              </button>
            </nav>
          </div>

          {/* Tab Content */}
          <div className="bg-white rounded-lg shadow-md p-6">
            {activeTab === 'profile' && (
              <div>
                <h2 className="text-xl font-semibold text-gray-900 mb-4">Profile Information</h2>
                <ProfileForm user={user} onUpdate={handleProfileUpdate} />
              </div>
            )}

            {activeTab === 'password' && (
              <div>
                <h2 className="text-xl font-semibold text-gray-900 mb-4">Change Password</h2>
                <PasswordChangeForm />
              </div>
            )}
          </div>

        </div>
      </DashboardLayout>
    </ProtectedRoute>
  );
}


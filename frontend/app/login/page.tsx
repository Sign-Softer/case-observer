import LoginForm from '../components/auth/LoginForm';
import Navigation from '../components/layout/Navigation';

export default function LoginPage() {
  return (
    <div className="min-h-screen bg-gray-50">
      <Navigation />
      <div className="flex items-center justify-center py-12 sm:py-16 md:py-20 px-4 sm:px-6 lg:px-8">
        <div className="w-full max-w-md">
          <div className="bg-white rounded-lg shadow-md p-6 sm:p-8 md:p-10">
            <div className="text-center mb-6 sm:mb-8">
              <h1 className="text-2xl sm:text-3xl font-bold text-gray-900 mb-2">
                Welcome Back
              </h1>
              <p className="text-sm sm:text-base text-gray-600">
                Sign in to your Case Observer account
              </p>
            </div>
            <LoginForm />
          </div>
        </div>
      </div>
    </div>
  );
}


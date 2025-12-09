'use client';

import { useState, FormEvent } from 'react';
import { useRouter } from 'next/navigation';
import { useAuth } from '@/lib/auth/AuthContext';
import { PASSWORD_REQUIREMENTS } from '@/lib/validation/password';
import Input from '../common/Input';
import Button from '../common/Button';
import Tooltip from '../common/Tooltip';

export default function RegisterForm() {
  const router = useRouter();
  const { register, isLoading, error: authError } = useAuth();
  const [formData, setFormData] = useState({
    username: '',
    email: '',
    password: '',
    confirmPassword: '',
  });
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [isSubmitting, setIsSubmitting] = useState(false);

  const validateEmail = (email: string): string | null => {
    if (!email.trim()) {
      return 'Email is required';
    }
    // Basic email validation regex
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(email)) {
      return 'Please enter a valid email address';
    }
    return null;
  };

  const handleEmailChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const email = e.target.value;
    setFormData({ ...formData, email });
    
    // Real-time email validation
    if (email && email.length > 0) {
      const emailError = validateEmail(email);
      if (emailError) {
        setErrors({ ...errors, email: emailError });
      } else {
        const { email: _, ...restErrors } = errors;
        setErrors(restErrors);
      }
    } else {
      const { email: _, ...restErrors } = errors;
      setErrors(restErrors);
    }
  };

  const validate = () => {
    const newErrors: Record<string, string> = {};

    // Email validation
    const emailError = validateEmail(formData.email);
    if (emailError) {
      newErrors.email = emailError;
    }

    // Only check confirm password match (backend handles all other validation)
    if (formData.password !== formData.confirmPassword) {
      newErrors.confirmPassword = 'Passwords do not match';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    
    if (!validate()) {
      return;
    }

    setIsSubmitting(true);
    try {
      await register({
        username: formData.username,
        email: formData.email,
        password: formData.password,
      });
      // Redirect to dashboard on success (register auto-logs in)
      router.push('/dashboard');
    } catch (error: any) {
      // Error is handled by AuthContext
      console.error('Registration error:', error);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-4 sm:space-y-6">
      {authError && (
        <div className="p-3 sm:p-4 bg-red-50 border border-red-200 rounded-lg">
          <p className="text-sm sm:text-base text-red-800">{authError}</p>
        </div>
      )}

      <div>
        <div className="flex items-center gap-2 mb-2">
          <label htmlFor="username" className="block text-sm font-medium text-gray-700">
            Username
          </label>
          <Tooltip
            content={
              <div>
                <p className="font-medium mb-1">Username requirements:</p>
                <ul className="list-disc list-inside space-y-0.5">
                  <li>8-50 characters</li>
                  <li>Letters, numbers, underscores, or hyphens only</li>
                </ul>
              </div>
            }
          >
            <button
              type="button"
              className="text-gray-400 hover:text-gray-600 focus:outline-none"
              aria-label="Username requirements"
            >
              <svg className="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
                <path
                  fillRule="evenodd"
                  d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-8-3a1 1 0 00-.867.5 1 1 0 11-1.731-1A3 3 0 0113 8a3.001 3.001 0 01-2 2.83V11a1 1 0 11-2 0v-1a1 1 0 011-1 1 1 0 100-2zm0 8a1 1 0 100-2 1 1 0 000 2z"
                  clipRule="evenodd"
                />
              </svg>
            </button>
          </Tooltip>
        </div>
        <Input
          id="username"
          type="text"
          value={formData.username}
          onChange={(e) => setFormData({ ...formData, username: e.target.value })}
          placeholder="Choose a username"
          error={errors.username}
          disabled={isSubmitting || isLoading}
          autoComplete="username"
          required
        />
      </div>

      <div>
        <div className="flex items-center gap-2 mb-2">
          <label htmlFor="email" className="block text-sm font-medium text-gray-700">
            Email
          </label>
          <Tooltip
            content={
              <div>
                <p className="font-medium mb-1">Email requirements:</p>
                <ul className="list-disc list-inside space-y-0.5">
                  <li>Must be a valid email format</li>
                  <li>Example: user@example.com</li>
                </ul>
              </div>
            }
          >
            <button
              type="button"
              className="text-gray-400 hover:text-gray-600 focus:outline-none"
              aria-label="Email requirements"
            >
              <svg className="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
                <path
                  fillRule="evenodd"
                  d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-8-3a1 1 0 00-.867.5 1 1 0 11-1.731-1A3 3 0 0113 8a3.001 3.001 0 01-2 2.83V11a1 1 0 11-2 0v-1a1 1 0 011-1 1 1 0 100-2zm0 8a1 1 0 100-2 1 1 0 000 2z"
                  clipRule="evenodd"
                />
              </svg>
            </button>
          </Tooltip>
        </div>
        <Input
          id="email"
          type="email"
          value={formData.email}
          onChange={handleEmailChange}
          placeholder="your.email@example.com"
          error={errors.email}
          disabled={isSubmitting || isLoading}
          autoComplete="email"
          required
        />
      </div>

      <div>
        <div className="flex items-center gap-2 mb-2">
          <label htmlFor="password" className="block text-sm font-medium text-gray-700">
            Password
          </label>
          <Tooltip
            content={
              <div>
                <p className="font-medium mb-1">Password requirements:</p>
                <ul className="list-disc list-inside space-y-0.5">
                  {PASSWORD_REQUIREMENTS.map((req, idx) => (
                    <li key={idx}>{req}</li>
                  ))}
                </ul>
              </div>
            }
          >
            <button
              type="button"
              className="text-gray-400 hover:text-gray-600 focus:outline-none"
              aria-label="Password requirements"
            >
              <svg className="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
                <path
                  fillRule="evenodd"
                  d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-8-3a1 1 0 00-.867.5 1 1 0 11-1.731-1A3 3 0 0113 8a3.001 3.001 0 01-2 2.83V11a1 1 0 11-2 0v-1a1 1 0 011-1 1 1 0 100-2zm0 8a1 1 0 100-2 1 1 0 000 2z"
                  clipRule="evenodd"
                />
              </svg>
            </button>
          </Tooltip>
        </div>
        <Input
          id="password"
          type="password"
          value={formData.password}
          onChange={(e) => setFormData({ ...formData, password: e.target.value })}
          placeholder="Create a password"
          error={errors.password}
          disabled={isSubmitting || isLoading}
          autoComplete="new-password"
          required
        />
      </div>

      <Input
        label="Confirm Password"
        type="password"
        value={formData.confirmPassword}
        onChange={(e) => setFormData({ ...formData, confirmPassword: e.target.value })}
        placeholder="Confirm your password"
        error={errors.confirmPassword}
        disabled={isSubmitting || isLoading}
        autoComplete="new-password"
        required
      />

      <Button
        type="submit"
        variant="primary"
        fullWidth
        loading={isSubmitting || isLoading}
        disabled={isSubmitting || isLoading}
      >
        Create Account
      </Button>

      <p className="text-center text-sm text-gray-600">
        Already have an account?{' '}
        <a href="/login" className="text-blue-600 hover:text-blue-700 font-semibold hover:underline">
          Sign in
        </a>
      </p>
    </form>
  );
}


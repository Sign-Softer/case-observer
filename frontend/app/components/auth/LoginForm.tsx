'use client';

import { useState, FormEvent } from 'react';
import { useRouter } from 'next/navigation';
import { useAuth } from '../../../lib/auth/AuthContext';
import Input from '../common/Input';
import Button from '../common/Button';

export default function LoginForm() {
  const router = useRouter();
  const { login, isLoading, error: authError } = useAuth();
  const [formData, setFormData] = useState({
    username: '',
    password: '',
  });
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [isSubmitting, setIsSubmitting] = useState(false);

  const validate = () => {
    const newErrors: Record<string, string> = {};

    if (!formData.username.trim()) {
      newErrors.username = 'Username is required';
    }

    if (!formData.password) {
      newErrors.password = 'Password is required';
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
      await login({
        username: formData.username,
        password: formData.password,
      });
      // Redirect to dashboard on success
      router.push('/dashboard');
    } catch (error: any) {
      // Error is handled by AuthContext
      console.error('Login error:', error);
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

      <Input
        label="Username"
        type="text"
        value={formData.username}
        onChange={(e) => setFormData({ ...formData, username: e.target.value })}
        placeholder="Enter your username"
        error={errors.username}
        disabled={isSubmitting || isLoading}
        autoComplete="username"
        required
      />

      <Input
        label="Password"
        type="password"
        value={formData.password}
        onChange={(e) => setFormData({ ...formData, password: e.target.value })}
        placeholder="Enter your password"
        error={errors.password}
        disabled={isSubmitting || isLoading}
        autoComplete="current-password"
        required
      />

      <div className="flex items-center justify-between text-sm">
        <a
          href="#"
          className="text-blue-600 hover:text-blue-700 hover:underline"
        >
          Forgot password?
        </a>
      </div>

      <Button
        type="submit"
        variant="primary"
        fullWidth
        loading={isSubmitting || isLoading}
        disabled={isSubmitting || isLoading}
      >
        Sign In
      </Button>

      <p className="text-center text-sm text-gray-600">
        Don't have an account?{' '}
        <a href="/register" className="text-blue-600 hover:text-blue-700 font-semibold hover:underline">
          Sign up
        </a>
      </p>
    </form>
  );
}


import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function RegisterPage() {
  const [firstName, setFirstName] = useState('');
  const [lastName, setLastName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');

    if (password !== confirmPassword) {
      setError('Passwords do not match');
      return;
    }

    setLoading(true);
    try {
      // TODO: Wire to Auth Service in Phase 2
      const mockUser = {
        id: '1',
        email,
        firstName,
        lastName,
        role: 'STUDENT' as const,
      };
      login('mock-jwt-token', mockUser);
      navigate('/dashboard');
    } catch {
      setError('Registration failed. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center p-[16px] md:p-[40px] relative overflow-hidden">
      {/* Animated Background Blobs */}
      <div className="absolute inset-0 z-0 overflow-hidden pointer-events-none">
        <div className="absolute top-1/4 left-1/4 w-[500px] h-[500px] bg-primary-fixed rounded-full mix-blend-multiply filter blur-[100px] opacity-60 animate-blob"></div>
        <div className="absolute top-1/3 right-1/4 w-[400px] h-[400px] bg-secondary-fixed rounded-full mix-blend-multiply filter blur-[100px] opacity-50 animate-blob animation-delay-2000"></div>
        <div className="absolute -bottom-32 left-1/3 w-[600px] h-[600px] bg-tertiary-fixed rounded-full mix-blend-multiply filter blur-[120px] opacity-40 animate-blob animation-delay-4000"></div>
      </div>

      <main className="relative z-10 w-full max-w-[440px]">
        <div className="bg-surface/70 backdrop-blur-2xl border border-primary/10 shadow-[0_20px_50px_rgba(0,0,0,0.05)] rounded-2xl p-[32px] md:p-[40px] flex flex-col items-center relative overflow-hidden">
          <div className="absolute top-0 left-0 w-full h-1 bg-gradient-to-r from-transparent via-primary to-transparent opacity-30"></div>

          <div className="flex items-center gap-[8px] mb-8">
            <span className="material-symbols-outlined text-primary text-[32px]" style={{ fontVariationSettings: "'FILL' 1" }}>school</span>
            <span className="text-headline-md font-headline-md font-bold text-primary">Student Central</span>
          </div>

          <h1 className="text-headline-lg font-headline-lg text-on-surface mb-2 text-center">Create Account</h1>
          <p className="text-body-md font-body-md text-on-surface-variant mb-8 text-center">Join the Student Central platform</p>

          {error && (
            <div className="w-full bg-error-container/30 text-on-error-container border border-error/20 rounded-lg px-4 py-3 mb-6 text-body-sm font-body-sm">
              {error}
            </div>
          )}

          <form onSubmit={handleSubmit} className="w-full flex flex-col gap-5">
            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-1">
                <label className="font-label-sm text-label-sm text-on-surface-variant">First Name</label>
                <input type="text" value={firstName} onChange={(e) => setFirstName(e.target.value)}
                  className="w-full rounded-lg glass-input px-4 py-3 font-body-md text-body-md text-on-surface" placeholder="John" required />
              </div>
              <div className="space-y-1">
                <label className="font-label-sm text-label-sm text-on-surface-variant">Last Name</label>
                <input type="text" value={lastName} onChange={(e) => setLastName(e.target.value)}
                  className="w-full rounded-lg glass-input px-4 py-3 font-body-md text-body-md text-on-surface" placeholder="Doe" required />
              </div>
            </div>

            <div className="space-y-1">
              <label className="font-label-sm text-label-sm text-on-surface-variant">Email Address</label>
              <input type="email" value={email} onChange={(e) => setEmail(e.target.value)}
                className="w-full rounded-lg glass-input px-4 py-3 font-body-md text-body-md text-on-surface" placeholder="you@university.edu" required />
            </div>

            <div className="space-y-1">
              <label className="font-label-sm text-label-sm text-on-surface-variant">Password</label>
              <input type="password" value={password} onChange={(e) => setPassword(e.target.value)}
                className="w-full rounded-lg glass-input px-4 py-3 font-body-md text-body-md text-on-surface" placeholder="••••••••" required />
            </div>

            <div className="space-y-1">
              <label className="font-label-sm text-label-sm text-on-surface-variant">Confirm Password</label>
              <input type="password" value={confirmPassword} onChange={(e) => setConfirmPassword(e.target.value)}
                className="w-full rounded-lg glass-input px-4 py-3 font-body-md text-body-md text-on-surface" placeholder="••••••••" required />
            </div>

            <button type="submit" disabled={loading}
              className="w-full bg-primary text-on-primary font-label-md text-label-md py-3 rounded-lg shadow-sm hover:bg-primary/90 hover:shadow-md hover:-translate-y-0.5 transition-all flex items-center justify-center gap-2 disabled:opacity-50 disabled:cursor-not-allowed mt-2">
              {loading ? 'Creating account...' : 'Create Account'}
              {!loading && <span className="material-symbols-outlined text-[18px]">arrow_forward</span>}
            </button>
          </form>

          <p className="text-body-sm font-body-sm text-on-surface-variant mt-6">
            Already have an account?{' '}
            <Link to="/login" className="text-primary font-semibold hover:underline">Sign In</Link>
          </p>

          <Link to="/" className="text-body-sm font-body-sm text-on-surface-variant mt-4 hover:text-primary transition-colors flex items-center gap-1">
            <span className="material-symbols-outlined text-[16px]">arrow_back</span>
            Back to home
          </Link>
        </div>
      </main>
    </div>
  );
}

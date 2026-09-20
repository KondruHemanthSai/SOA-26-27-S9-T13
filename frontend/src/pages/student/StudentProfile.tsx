import { useState, useEffect } from 'react';
import StudentSidebar from '../../components/layout/StudentSidebar';
import TopNav from '../../components/layout/TopNav';
import { studentApi, getErrorMessage } from '../../services/api';
import type { StudentProfile, UpdateStudentRequest } from '../../types/api';

export default function StudentProfilePage() {
  const [profile, setProfile] = useState<StudentProfile | null>(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  // Edit form state
  const [isEditing, setIsEditing] = useState(false);
  const [formData, setFormData] = useState<UpdateStudentRequest>({
    firstName: '',
    lastName: '',
    phone: '',
    dateOfBirth: '',
    address: '',
    program: '',
    department: '',
  });

  const fetchProfile = async () => {
    try {
      setLoading(true);
      setError('');
      const data = await studentApi.getProfile();
      setProfile(data);
      setFormData({
        firstName: data.firstName || '',
        lastName: data.lastName || '',
        phone: data.phone || '',
        dateOfBirth: data.dateOfBirth || '',
        address: data.address || '',
        program: data.program || '',
        department: data.department || '',
      });
    } catch (err) {
      setError(getErrorMessage(err, 'Failed to load profile. Please try again.'));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchProfile();
  }, []);

  const handleSave = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      setSaving(true);
      setError('');
      setSuccess('');
      const updated = await studentApi.updateProfile(formData);
      setProfile(updated);
      setIsEditing(false);
      setSuccess('Profile updated successfully.');
      setTimeout(() => setSuccess(''), 4000);
    } catch (err) {
      setError(getErrorMessage(err, 'Failed to update profile.'));
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="flex h-screen bg-background text-on-surface">
      <StudentSidebar />
      <main className="ml-64 flex-1 flex flex-col min-h-screen relative overflow-y-auto">
        <TopNav searchPlaceholder="Search student profile..." />

        <div className="p-margin-mobile md:p-margin-desktop max-w-container-max mx-auto w-full flex flex-col gap-stack-lg">
          {/* Header */}
          <div className="flex flex-col sm:flex-row sm:items-end justify-between gap-4">
            <div>
              <h1 className="font-display-lg text-display-lg text-on-surface">Student Profile</h1>
              <p className="font-body-lg text-body-lg text-on-surface-variant mt-1">
                Manage your personal, program, and academic information.
              </p>
            </div>
            {!isEditing && profile && (
              <button
                onClick={() => setIsEditing(true)}
                className="px-5 py-2.5 bg-primary text-on-primary rounded-lg font-label-md text-label-md shadow-sm hover:bg-primary/90 transition-all flex items-center gap-2 self-start sm:self-auto"
              >
                <span className="material-symbols-outlined text-[18px]">edit</span>
                Edit Profile
              </button>
            )}
          </div>

          {/* Feedback alerts */}
          {error && (
            <div className="p-4 bg-error-container/30 border border-error/20 text-on-error-container rounded-xl font-body-sm text-body-sm flex items-center gap-3">
              <span className="material-symbols-outlined text-error">error</span>
              {error}
            </div>
          )}

          {success && (
            <div className="p-4 bg-emerald-50 border border-emerald-200 text-emerald-800 rounded-xl font-body-sm text-body-sm flex items-center gap-3">
              <span className="material-symbols-outlined text-emerald-600" style={{ fontVariationSettings: "'FILL' 1" }}>
                check_circle
              </span>
              {success}
            </div>
          )}

          {loading ? (
            <div className="glass-card rounded-2xl p-12 flex flex-col items-center justify-center">
              <div className="w-10 h-10 border-4 border-primary/20 border-t-primary rounded-full animate-spin"></div>
              <p className="mt-4 text-on-surface-variant font-body-md">Loading student profile...</p>
            </div>
          ) : !profile ? (
            <div className="glass-card rounded-2xl p-12 text-center">
              <span className="material-symbols-outlined text-[48px] text-on-surface-variant/40 mb-3">person_off</span>
              <p className="text-on-surface font-headline-md text-lg font-semibold">No Profile Found</p>
              <p className="text-on-surface-variant text-sm mt-1 mb-4">
                Your student profile has not been initialized yet.
              </p>
              <button
                onClick={() => setIsEditing(true)}
                className="px-6 py-2.5 bg-primary text-on-primary rounded-lg font-label-md text-sm"
              >
                Create Profile
              </button>
            </div>
          ) : (
            <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
              {/* Profile Card & Quick Stats */}
              <div className="glass-card rounded-2xl p-6 flex flex-col items-center text-center">
                <div className="w-24 h-24 rounded-full bg-primary/10 text-primary flex items-center justify-center font-bold text-3xl font-display-lg mb-4 border-2 border-primary/20">
                  {profile.firstName ? profile.firstName[0] : 'S'}
                  {profile.lastName ? profile.lastName[0] : ''}
                </div>
                <h2 className="text-headline-md font-bold text-on-surface">
                  {profile.firstName} {profile.lastName}
                </h2>
                <p className="text-body-sm text-primary font-medium mt-0.5">ID: {profile.studentId || 'Pending'}</p>
                <p className="text-body-sm text-on-surface-variant mt-1">{profile.email}</p>

                <div className="w-full border-t border-outline-variant/20 my-5"></div>

                <div className="w-full space-y-3 text-left">
                  <div className="flex justify-between items-center text-sm">
                    <span className="text-on-surface-variant">Admission Status:</span>
                    <span className="px-2.5 py-0.5 rounded-full font-label-sm text-xs bg-primary/10 text-primary">
                      {profile.admissionStatus || 'N/A'}
                    </span>
                  </div>
                  <div className="flex justify-between items-center text-sm">
                    <span className="text-on-surface-variant">Program:</span>
                    <span className="font-medium text-on-surface text-right">{profile.program || 'Not assigned'}</span>
                  </div>
                  <div className="flex justify-between items-center text-sm">
                    <span className="text-on-surface-variant">Department:</span>
                    <span className="font-medium text-on-surface">{profile.department || 'N/A'}</span>
                  </div>
                  <div className="flex justify-between items-center text-sm">
                    <span className="text-on-surface-variant">Semester:</span>
                    <span className="font-medium text-on-surface">{profile.semester ?? 1}</span>
                  </div>
                </div>
              </div>

              {/* Form / Details Panel */}
              <div className="lg:col-span-2 glass-panel rounded-2xl p-6">
                {isEditing ? (
                  <form onSubmit={handleSave} className="space-y-5">
                    <h3 className="font-headline-md text-lg font-semibold text-on-surface flex items-center gap-2">
                      <span className="material-symbols-outlined text-primary">edit_note</span>
                      Edit Personal Details
                    </h3>

                    <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                      <div className="space-y-1">
                        <label className="font-label-sm text-xs text-on-surface-variant">First Name</label>
                        <input
                          type="text"
                          required
                          value={formData.firstName}
                          onChange={(e) => setFormData({ ...formData, firstName: e.target.value })}
                          className="w-full rounded-lg glass-input px-3 py-2 font-body-sm text-on-surface"
                        />
                      </div>
                      <div className="space-y-1">
                        <label className="font-label-sm text-xs text-on-surface-variant">Last Name</label>
                        <input
                          type="text"
                          required
                          value={formData.lastName}
                          onChange={(e) => setFormData({ ...formData, lastName: e.target.value })}
                          className="w-full rounded-lg glass-input px-3 py-2 font-body-sm text-on-surface"
                        />
                      </div>
                      <div className="space-y-1">
                        <label className="font-label-sm text-xs text-on-surface-variant">Phone Number</label>
                        <input
                          type="tel"
                          value={formData.phone}
                          onChange={(e) => setFormData({ ...formData, phone: e.target.value })}
                          className="w-full rounded-lg glass-input px-3 py-2 font-body-sm text-on-surface"
                          placeholder="+1 (555) 000-0000"
                        />
                      </div>
                      <div className="space-y-1">
                        <label className="font-label-sm text-xs text-on-surface-variant">Date of Birth</label>
                        <input
                          type="date"
                          value={formData.dateOfBirth}
                          onChange={(e) => setFormData({ ...formData, dateOfBirth: e.target.value })}
                          className="w-full rounded-lg glass-input px-3 py-2 font-body-sm text-on-surface"
                        />
                      </div>
                      <div className="space-y-1">
                        <label className="font-label-sm text-xs text-on-surface-variant">Program</label>
                        <input
                          type="text"
                          value={formData.program}
                          onChange={(e) => setFormData({ ...formData, program: e.target.value })}
                          className="w-full rounded-lg glass-input px-3 py-2 font-body-sm text-on-surface"
                          placeholder="Computer Science, B.S."
                        />
                      </div>
                      <div className="space-y-1">
                        <label className="font-label-sm text-xs text-on-surface-variant">Department</label>
                        <input
                          type="text"
                          value={formData.department}
                          onChange={(e) => setFormData({ ...formData, department: e.target.value })}
                          className="w-full rounded-lg glass-input px-3 py-2 font-body-sm text-on-surface"
                          placeholder="Computer Science"
                        />
                      </div>
                      <div className="sm:col-span-2 space-y-1">
                        <label className="font-label-sm text-xs text-on-surface-variant">Address</label>
                        <textarea
                          rows={3}
                          value={formData.address}
                          onChange={(e) => setFormData({ ...formData, address: e.target.value })}
                          className="w-full rounded-lg glass-input px-3 py-2 font-body-sm text-on-surface resize-none"
                          placeholder="Street, City, State, ZIP"
                        ></textarea>
                      </div>
                    </div>

                    <div className="flex justify-end gap-3 pt-4 border-t border-outline-variant/20">
                      <button
                        type="button"
                        onClick={() => setIsEditing(false)}
                        className="px-4 py-2 border border-outline-variant text-on-surface font-label-md text-sm rounded-lg hover:bg-surface-container transition-colors"
                      >
                        Cancel
                      </button>
                      <button
                        type="submit"
                        disabled={saving}
                        className="px-6 py-2 bg-primary text-on-primary font-label-md text-sm rounded-lg shadow-sm hover:bg-primary/90 transition-all flex items-center gap-2 disabled:opacity-50"
                      >
                        {saving ? 'Saving...' : 'Save Profile'}
                      </button>
                    </div>
                  </form>
                ) : (
                  <div className="space-y-6">
                    <div>
                      <h3 className="font-headline-md text-lg font-semibold text-primary mb-4 flex items-center gap-2">
                        <span className="material-symbols-outlined text-primary">badge</span>
                        Personal & Contact Information
                      </h3>
                      <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 text-body-sm text-sm">
                        <div className="p-3 bg-surface-container-low/50 rounded-lg">
                          <p className="text-xs text-on-surface-variant font-label-sm">Full Name</p>
                          <p className="font-semibold text-on-surface mt-1">
                            {profile.firstName} {profile.lastName}
                          </p>
                        </div>
                        <div className="p-3 bg-surface-container-low/50 rounded-lg">
                          <p className="text-xs text-on-surface-variant font-label-sm">Email Address</p>
                          <p className="font-semibold text-on-surface mt-1">{profile.email}</p>
                        </div>
                        <div className="p-3 bg-surface-container-low/50 rounded-lg">
                          <p className="text-xs text-on-surface-variant font-label-sm">Phone Number</p>
                          <p className="font-semibold text-on-surface mt-1">{profile.phone || 'Not provided'}</p>
                        </div>
                        <div className="p-3 bg-surface-container-low/50 rounded-lg">
                          <p className="text-xs text-on-surface-variant font-label-sm">Date of Birth</p>
                          <p className="font-semibold text-on-surface mt-1">{profile.dateOfBirth || 'Not provided'}</p>
                        </div>
                        <div className="sm:col-span-2 p-3 bg-surface-container-low/50 rounded-lg">
                          <p className="text-xs text-on-surface-variant font-label-sm">Residential Address</p>
                          <p className="font-semibold text-on-surface mt-1">{profile.address || 'Not provided'}</p>
                        </div>
                      </div>
                    </div>

                    {profile.academicRecord && (
                      <div className="pt-4 border-t border-outline-variant/20">
                        <h3 className="font-headline-md text-lg font-semibold text-primary mb-3 flex items-center gap-2">
                          <span className="material-symbols-outlined text-primary">school</span>
                          Academic Record
                        </h3>
                        <div className="grid grid-cols-1 sm:grid-cols-3 gap-3 text-center">
                          <div className="p-3 bg-surface-container-low/50 rounded-lg">
                            <p className="text-xs text-on-surface-variant">Class 10th</p>
                            <p className="text-lg font-bold text-primary mt-1">
                              {profile.academicRecord.tenthPercentage ? `${profile.academicRecord.tenthPercentage}%` : 'N/A'}
                            </p>
                          </div>
                          <div className="p-3 bg-surface-container-low/50 rounded-lg">
                            <p className="text-xs text-on-surface-variant">Class 12th</p>
                            <p className="text-lg font-bold text-tertiary mt-1">
                              {profile.academicRecord.twelfthPercentage ? `${profile.academicRecord.twelfthPercentage}%` : 'N/A'}
                            </p>
                          </div>
                          <div className="p-3 bg-surface-container-low/50 rounded-lg">
                            <p className="text-xs text-on-surface-variant">Current CGPA</p>
                            <p className="text-lg font-bold text-on-surface mt-1">
                              {profile.academicRecord.cgpa ? `${profile.academicRecord.cgpa} / 10.0` : 'N/A'}
                            </p>
                          </div>
                        </div>
                      </div>
                    )}
                  </div>
                )}
              </div>
            </div>
          )}
        </div>
      </main>
    </div>
  );
}

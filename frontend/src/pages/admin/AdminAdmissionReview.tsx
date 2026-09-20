import { useState, useEffect } from 'react';
import AdminSidebar from '../../components/layout/AdminSidebar';
import { admissionApi, getErrorMessage } from '../../services/api';
import type {
  AdmissionApplication,
  AdmissionDocument,
  ReviewAction,
  ApplicationStatus,
} from '../../types/api';

export default function AdminAdmissionReview() {
  const [applications, setApplications] = useState<AdmissionApplication[]>([]);
  const [selectedApp, setSelectedApp] = useState<AdmissionApplication | null>(null);
  const [documents, setDocuments] = useState<AdmissionDocument[]>([]);
  const [statusFilter, setStatusFilter] = useState<string>('');
  const [remarks, setRemarks] = useState<string>('');
  const [loading, setLoading] = useState(true);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const fetchApplications = async () => {
    try {
      setLoading(true);
      setError('');
      const data = await admissionApi.listApplications(
        undefined,
        statusFilter ? (statusFilter as ApplicationStatus) : undefined
      );
      setApplications(data || []);
      if (data && data.length > 0) {
        // Automatically select the first application if none selected or not in current list
        if (!selectedApp || !data.some((a: AdmissionApplication) => a.id === selectedApp.id)) {
          selectApplication(data[0]);
        }
      } else {
        setSelectedApp(null);
        setDocuments([]);
      }
    } catch (err) {
      setError(getErrorMessage(err, 'Failed to fetch admission applications.'));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchApplications();
  }, [statusFilter]);

  const selectApplication = async (app: AdmissionApplication) => {
    setSelectedApp(app);
    setRemarks(app.reviewerRemarks || '');
    try {
      const docs = await admissionApi.getDocuments(app.id);
      setDocuments(docs || []);
    } catch {
      setDocuments([]);
    }
  };

  const handleReviewAction = async (action: ReviewAction) => {
    if (!selectedApp) return;
    try {
      setIsSubmitting(true);
      setError('');
      setSuccess('');
      const updated = await admissionApi.reviewApplication(selectedApp.id, {
        action,
        remarks,
      });
      setSelectedApp(updated);
      setSuccess(`Application ${action.toLowerCase().replace('_', ' ')} successfully.`);
      // Update in applications list
      setApplications((prev) => prev.map((a) => (a.id === updated.id ? updated : a)));
      setTimeout(() => setSuccess(''), 4000);
    } catch (err) {
      setError(getErrorMessage(err, `Failed to ${action.toLowerCase()} application.`));
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="flex h-screen bg-background text-on-surface">
      <AdminSidebar />
      <main className="ml-64 flex-1 flex flex-col min-h-screen relative overflow-y-auto">
        {/* Top Header */}
        <header className="bg-surface/60 backdrop-blur-lg border-b border-primary/10 h-16 flex items-center justify-between px-gutter sticky top-0 z-40">
          <div className="flex items-center gap-3">
            <span className="font-headline-md text-headline-md font-semibold text-primary">
              Admission Review Panel
            </span>
          </div>
          <div className="flex items-center gap-3">
            <select
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value)}
              className="bg-surface/50 border border-outline-variant/40 rounded-lg px-3 py-1.5 font-label-md text-xs text-on-surface outline-none"
            >
              <option value="">All Statuses</option>
              <option value="SUBMITTED">Submitted</option>
              <option value="UNDER_REVIEW">Under Review</option>
              <option value="APPROVED">Approved</option>
              <option value="REJECTED">Rejected</option>
              <option value="CHANGES_REQUESTED">Changes Requested</option>
            </select>
          </div>
        </header>

        {/* Content Area */}
        <div className="p-margin-mobile md:p-margin-desktop max-w-container-max mx-auto w-full flex flex-col gap-stack-lg">
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
            <div className="glass-card rounded-xl p-16 flex flex-col items-center justify-center">
              <div className="w-10 h-10 border-4 border-primary/20 border-t-primary rounded-full animate-spin"></div>
              <p className="mt-4 text-on-surface-variant font-body-md">Loading applications...</p>
            </div>
          ) : applications.length === 0 ? (
            <div className="glass-card rounded-xl p-16 text-center text-on-surface-variant flex flex-col items-center justify-center">
              <span className="material-symbols-outlined text-[48px] opacity-40 mb-2">inbox</span>
              <p className="text-on-surface font-headline-md text-lg font-semibold">No Applications Found</p>
              <p className="text-sm mt-1">No candidate applications match the selected status.</p>
            </div>
          ) : (
            <div className="grid grid-cols-1 lg:grid-cols-4 gap-6">
              {/* Left Column: Application Selector List */}
              <div className="glass-card rounded-xl p-4 flex flex-col gap-2 max-h-[750px] overflow-y-auto">
                <h3 className="font-headline-md text-sm font-semibold text-primary px-2 mb-1">
                  Applications ({applications.length})
                </h3>
                {applications.map((app) => {
                  const isSelected = selectedApp?.id === app.id;
                  return (
                    <div
                      key={app.id}
                      onClick={() => selectApplication(app)}
                      className={`p-3 rounded-lg cursor-pointer transition-all ${
                        isSelected
                          ? 'bg-primary/15 border-l-4 border-l-primary'
                          : 'hover:bg-surface-container/50 border border-transparent'
                      }`}
                    >
                      <div className="flex justify-between items-start">
                        <p className="font-semibold text-xs text-on-surface">
                          {app.studentName || `Applicant ${app.userId.slice(0, 6)}`}
                        </p>
                        <span className="text-[10px] px-1.5 py-0.5 rounded bg-surface-container font-label-sm">
                          {app.status}
                        </span>
                      </div>
                      <p className="text-[11px] text-on-surface-variant truncate mt-0.5">{app.program}</p>
                      <p className="text-[10px] text-on-surface-variant/70 mt-1">
                        ID: {app.applicationId || app.id.slice(0, 8)}
                      </p>
                    </div>
                  );
                })}
              </div>

              {/* Right 3 Columns: Selected Application Details & Review Actions */}
              {selectedApp && (
                <div className="lg:col-span-3 space-y-6">
                  {/* Main Applicant Overview Card */}
                  <div className="glass-panel rounded-2xl p-6 flex flex-col lg:flex-row justify-between items-start lg:items-center gap-6">
                    <div className="flex items-center gap-5">
                      <div className="w-16 h-16 rounded-full bg-primary/10 text-primary flex items-center justify-center font-bold text-xl font-display-lg">
                        {selectedApp.studentName ? selectedApp.studentName.slice(0, 2).toUpperCase() : 'ST'}
                      </div>
                      <div>
                        <div className="flex items-center gap-3">
                          <h1 className="font-headline-lg text-2xl font-bold text-on-surface">
                            {selectedApp.studentName || `Applicant ${selectedApp.userId.slice(0, 8)}`}
                          </h1>
                          <span className="px-3 py-1 bg-primary/10 text-primary font-label-sm text-xs rounded-full">
                            {selectedApp.status.replace('_', ' ')}
                          </span>
                        </div>
                        <p className="font-body-md text-sm text-on-surface-variant mt-1">
                          Applying for: <strong className="text-on-surface">{selectedApp.program}</strong> • {selectedApp.department}
                        </p>
                        <div className="flex items-center gap-4 mt-2 font-label-sm text-xs text-on-surface-variant">
                          <span>
                            CGPA: <strong className="text-on-surface">{selectedApp.academicDetails?.cgpa || 'N/A'}</strong>
                          </span>
                          <span>•</span>
                          <span>
                            Prior School: <strong className="text-on-surface">{selectedApp.academicDetails?.previousInstitution || 'N/A'}</strong>
                          </span>
                        </div>
                      </div>
                    </div>

                    <div className="flex flex-wrap gap-2 self-stretch lg:self-auto justify-end">
                      <button
                        onClick={() => handleReviewAction('REQUEST_CHANGES')}
                        disabled={isSubmitting}
                        className="px-4 py-2 border border-outline-variant text-on-surface rounded-lg font-label-md text-xs hover:bg-surface-container transition-colors disabled:opacity-50"
                      >
                        Request Changes
                      </button>
                      <button
                        onClick={() => handleReviewAction('REJECT')}
                        disabled={isSubmitting}
                        className="px-4 py-2 border border-error/40 text-error rounded-lg font-label-md text-xs hover:bg-error/10 transition-colors disabled:opacity-50"
                      >
                        Reject
                      </button>
                      <button
                        onClick={() => handleReviewAction('APPROVE')}
                        disabled={isSubmitting}
                        className="px-6 py-2 bg-primary text-on-primary rounded-lg font-label-md text-xs shadow-sm hover:bg-primary/90 transition-colors flex items-center gap-1.5 disabled:opacity-50"
                      >
                        <span className="material-symbols-outlined text-[16px]">check</span>
                        Approve Admission
                      </button>
                    </div>
                  </div>

                  {/* Details Bento Grid */}
                  <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
                    {/* Left 2 Cols: Documents & Academic Record */}
                    <div className="lg:col-span-2 space-y-6">
                      {/* Uploaded Documents Check */}
                      <div className="glass-card rounded-xl p-6">
                        <h3 className="font-headline-md text-lg font-semibold text-primary mb-4 flex items-center gap-2">
                          <span className="material-symbols-outlined text-primary text-[20px]">folder_open</span>
                          Submitted Documents ({documents.length})
                        </h3>

                        {documents.length === 0 ? (
                          <p className="text-xs text-on-surface-variant">No documents uploaded for this application.</p>
                        ) : (
                          <div className="space-y-3">
                            {documents.map((doc) => (
                              <div
                                key={doc.id}
                                className="p-3 rounded-lg border border-outline-variant/30 flex justify-between items-center bg-surface-container-low/40"
                              >
                                <div className="flex items-center gap-3">
                                  <span className="material-symbols-outlined text-primary">description</span>
                                  <div>
                                    <p className="font-body-md text-sm font-semibold">{doc.fileName}</p>
                                    <p className="font-label-sm text-xs text-on-surface-variant">
                                      Type: {doc.type} • Status: {doc.status}
                                    </p>
                                  </div>
                                </div>
                                <span className="px-2.5 py-0.5 rounded text-xs font-label-sm bg-emerald-50 text-emerald-700">
                                  {doc.status}
                                </span>
                              </div>
                            ))}
                          </div>
                        )}
                      </div>
                    </div>

                    {/* Right Col: Admin Review Remarks */}
                    <div className="glass-card rounded-xl p-6 flex flex-col justify-between">
                      <div>
                        <h3 className="font-headline-md text-lg font-semibold text-primary mb-3 flex items-center gap-2">
                          <span className="material-symbols-outlined text-primary text-[20px]">rate_review</span>
                          Reviewer Notes
                        </h3>
                        <p className="font-body-sm text-xs text-on-surface-variant mb-4">
                          Internal review notes attached to this decision.
                        </p>
                        <textarea
                          rows={6}
                          value={remarks}
                          onChange={(e) => setRemarks(e.target.value)}
                          className="w-full glass-input rounded-lg p-3 font-body-sm text-sm text-on-surface resize-none"
                          placeholder="Enter evaluation notes..."
                        ></textarea>
                      </div>

                      {selectedApp.reviewedBy && (
                        <div className="mt-4 p-3 rounded-lg bg-primary/10 border border-primary/20 text-primary font-label-sm text-xs flex items-center gap-2">
                          <span className="material-symbols-outlined text-[18px]">info</span>
                          Last evaluated: {selectedApp.status}
                        </div>
                      )}
                    </div>
                  </div>
                </div>
              )}
            </div>
          )}
        </div>
      </main>
    </div>
  );
}

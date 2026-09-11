import { useState } from 'react';
import AdminSidebar from '../../components/layout/AdminSidebar';

export default function AdminAdmissionReview() {
  const [selectedStatus, setSelectedStatus] = useState<string>('UNDER_REVIEW');
  const [remarks, setRemarks] = useState<string>('Applicant has strong background in Mathematics and Computer Science.');
  const [decision, setDecision] = useState<'APPROVED' | 'REJECTED' | 'CHANGES_REQUESTED' | null>(null);

  return (
    <div className="flex h-screen bg-background text-on-surface">
      <AdminSidebar />
      <main className="ml-64 flex-1 flex flex-col min-h-screen relative overflow-y-auto">
        {/* Top Header */}
        <header className="bg-surface/60 backdrop-blur-lg border-b border-primary/10 h-16 flex items-center justify-between px-gutter sticky top-0 z-40">
          <div className="flex items-center gap-3">
            <span className="font-headline-md text-headline-md font-semibold text-primary">Admission Review Panel</span>
          </div>
          <div className="flex items-center gap-2">
            <span className="text-on-surface-variant font-label-sm text-xs">Application #ADM-2024-8921</span>
          </div>
        </header>

        {/* Content Area */}
        <div className="p-margin-mobile md:p-margin-desktop max-w-container-max mx-auto w-full flex flex-col gap-stack-lg">
          {/* Main Applicant Overview Card */}
          <div className="glass-panel rounded-2xl p-6 flex flex-col lg:flex-row justify-between items-start lg:items-center gap-6">
            <div className="flex items-center gap-5">
              <div className="w-16 h-16 rounded-full bg-primary/10 text-primary flex items-center justify-center font-bold text-xl font-display-lg">
                AR
              </div>
              <div>
                <div className="flex items-center gap-3">
                  <h1 className="font-headline-lg text-2xl font-bold text-on-surface">Alex Rivera</h1>
                  <span className="px-3 py-1 bg-primary/10 text-primary font-label-sm text-xs rounded-full">
                    {selectedStatus.replace('_', ' ')}
                  </span>
                </div>
                <p className="font-body-md text-sm text-on-surface-variant mt-1">
                  Applying for: <strong className="text-on-surface">Computer Science, B.S.</strong> • Fall 2024
                </p>
                <div className="flex items-center gap-4 mt-2 font-label-sm text-xs text-on-surface-variant">
                  <span>GPA: <strong className="text-on-surface">3.85 / 4.0</strong></span>
                  <span>•</span>
                  <span>Applied: Aug 18, 2024</span>
                  <span>•</span>
                  <span>Email: alex.rivera@example.com</span>
                </div>
              </div>
            </div>

            <div className="flex flex-wrap gap-2 self-stretch lg:self-auto justify-end">
              <button
                onClick={() => { setDecision('CHANGES_REQUESTED'); setSelectedStatus('CHANGES_REQUESTED'); }}
                className="px-4 py-2 border border-outline-variant text-on-surface rounded-lg font-label-md text-xs hover:bg-surface-container transition-colors"
              >
                Request Changes
              </button>
              <button
                onClick={() => { setDecision('REJECTED'); setSelectedStatus('REJECTED'); }}
                className="px-4 py-2 border border-error/40 text-error rounded-lg font-label-md text-xs hover:bg-error/10 transition-colors"
              >
                Reject
              </button>
              <button
                onClick={() => { setDecision('APPROVED'); setSelectedStatus('APPROVED'); }}
                className="px-6 py-2 bg-primary text-on-primary rounded-lg font-label-md text-xs shadow-sm hover:bg-primary/90 transition-colors flex items-center gap-1.5"
              >
                <span className="material-symbols-outlined text-[16px]">check</span>
                Approve Admission
              </button>
            </div>
          </div>

          {/* Details Bento Grid */}
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
            {/* Left 2 Cols: Statement & Academic Info */}
            <div className="lg:col-span-2 space-y-6">
              {/* Statement of Purpose */}
              <div className="glass-card rounded-xl p-6">
                <h3 className="font-headline-md text-lg font-semibold text-primary mb-3 flex items-center gap-2">
                  <span className="material-symbols-outlined text-primary text-[20px]">format_quote</span>
                  Statement of Purpose
                </h3>
                <p className="font-body-md text-sm text-on-surface leading-relaxed">
                  "I am passionate about software engineering and distributed systems. Having worked on robotics and competitive programming in high school, I want to delve deeper into cloud systems and algorithms at Lumina Academy..."
                </p>
              </div>

              {/* Uploaded Documents Check */}
              <div className="glass-card rounded-xl p-6">
                <h3 className="font-headline-md text-lg font-semibold text-primary mb-4 flex items-center gap-2">
                  <span className="material-symbols-outlined text-primary text-[20px]">folder_open</span>
                  Submitted Documents
                </h3>
                <div className="space-y-3">
                  <div className="p-3 rounded-lg border border-outline-variant/30 flex justify-between items-center bg-surface-container-low/40">
                    <div className="flex items-center gap-3">
                      <span className="material-symbols-outlined text-primary">description</span>
                      <div>
                        <p className="font-body-md text-sm font-semibold">High_School_Transcript_AlexRivera.pdf</p>
                        <p className="font-label-sm text-xs text-on-surface-variant">2.4 MB • Verified</p>
                      </div>
                    </div>
                    <button className="text-primary font-label-sm text-xs hover:underline flex items-center gap-1">
                      <span className="material-symbols-outlined text-[16px]">visibility</span> View
                    </button>
                  </div>

                  <div className="p-3 rounded-lg border border-outline-variant/30 flex justify-between items-center bg-surface-container-low/40">
                    <div className="flex items-center gap-3">
                      <span className="material-symbols-outlined text-tertiary">badge</span>
                      <div>
                        <p className="font-body-md text-sm font-semibold">Government_Passport_Copy.pdf</p>
                        <p className="font-label-sm text-xs text-on-surface-variant">1.1 MB • Verified</p>
                      </div>
                    </div>
                    <button className="text-primary font-label-sm text-xs hover:underline flex items-center gap-1">
                      <span className="material-symbols-outlined text-[16px]">visibility</span> View
                    </button>
                  </div>
                </div>
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
                  Add internal remarks for this admission decision.
                </p>
                <textarea
                  rows={6}
                  value={remarks}
                  onChange={(e) => setRemarks(e.target.value)}
                  className="w-full glass-input rounded-lg p-3 font-body-sm text-sm text-on-surface resize-none"
                  placeholder="Enter evaluation notes..."
                ></textarea>
              </div>

              {decision && (
                <div className="mt-4 p-3 rounded-lg bg-primary/10 border border-primary/20 text-primary font-label-sm text-xs flex items-center gap-2">
                  <span className="material-symbols-outlined text-[18px]">info</span>
                  Decision recorded: {decision}
                </div>
              )}
            </div>
          </div>
        </div>
      </main>
    </div>
  );
}

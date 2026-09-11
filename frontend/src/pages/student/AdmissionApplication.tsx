import { useState } from 'react';
import StudentSidebar from '../../components/layout/StudentSidebar';
import TopNav from '../../components/layout/TopNav';

export default function AdmissionApplication() {
  const [step, setStep] = useState(1);
  const [formData, setFormData] = useState({
    firstName: 'Alex',
    lastName: 'Rivera',
    email: 'alex.rivera@example.com',
    phone: '+1 (555) 019-2834',
    dob: '2004-05-14',
    program: 'Computer Science, B.S.',
    term: 'Fall 2024',
    highSchool: 'Westview High School',
    gpa: '3.85',
    statement: 'I am passionate about software engineering and distributed systems...',
  });

  const [submitted, setSubmitted] = useState(false);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setSubmitted(true);
  };

  return (
    <div className="flex h-screen bg-background text-on-surface">
      <StudentSidebar />
      <main className="ml-64 flex-1 flex flex-col min-h-screen relative overflow-y-auto">
        <TopNav searchPlaceholder="Search admission status, docs..." />
        
        <div className="p-margin-mobile md:p-margin-desktop max-w-container-max mx-auto w-full flex flex-col gap-stack-lg">
          {/* Header */}
          <div className="flex flex-col md:flex-row md:items-end justify-between gap-stack-md">
            <div>
              <div className="flex items-center gap-2 mb-2">
                <span className="px-2.5 py-1 bg-primary/10 text-primary rounded font-label-sm text-label-sm uppercase tracking-wider">
                  Undergraduate
                </span>
                <span className="text-on-surface-variant font-label-sm text-label-sm">• Application #ADM-2024-8921</span>
              </div>
              <h1 className="font-display-lg text-display-lg text-on-surface">University Admission Application</h1>
              <p className="font-body-lg text-body-lg text-on-surface-variant mt-1">
                Complete all required sections to submit your application for Fall 2024.
              </p>
            </div>
            
            {submitted && (
              <div className="inline-flex items-center gap-2 bg-emerald-50 text-emerald-800 border border-emerald-200 px-4 py-2 rounded-full font-label-md text-label-md">
                <span className="material-symbols-outlined text-[18px] text-emerald-600" style={{ fontVariationSettings: "'FILL' 1" }}>check_circle</span>
                Application Submitted
              </div>
            )}
          </div>

          {/* Stepper */}
          <div className="glass-card rounded-xl p-4 flex justify-between items-center relative overflow-hidden">
            <div className="flex items-center gap-4 flex-1">
              <button 
                onClick={() => setStep(1)}
                className={`flex items-center gap-3 text-left ${step === 1 ? 'text-primary font-semibold' : 'text-on-surface-variant'}`}
              >
                <div className={`w-8 h-8 rounded-full flex items-center justify-center font-label-sm ${step === 1 ? 'bg-primary text-on-primary' : 'bg-surface-container text-on-surface-variant'}`}>
                  1
                </div>
                <div>
                  <p className="font-label-sm text-xs uppercase">Step 1</p>
                  <p className="font-body-sm text-sm">Personal Info</p>
                </div>
              </button>

              <div className="flex-1 h-0.5 bg-outline-variant/30 hidden sm:block"></div>

              <button 
                onClick={() => setStep(2)}
                className={`flex items-center gap-3 text-left ${step === 2 ? 'text-primary font-semibold' : 'text-on-surface-variant'}`}
              >
                <div className={`w-8 h-8 rounded-full flex items-center justify-center font-label-sm ${step === 2 ? 'bg-primary text-on-primary' : 'bg-surface-container text-on-surface-variant'}`}>
                  2
                </div>
                <div>
                  <p className="font-label-sm text-xs uppercase">Step 2</p>
                  <p className="font-body-sm text-sm">Academics & Program</p>
                </div>
              </button>

              <div className="flex-1 h-0.5 bg-outline-variant/30 hidden sm:block"></div>

              <button 
                onClick={() => setStep(3)}
                className={`flex items-center gap-3 text-left ${step === 3 ? 'text-primary font-semibold' : 'text-on-surface-variant'}`}
              >
                <div className={`w-8 h-8 rounded-full flex items-center justify-center font-label-sm ${step === 3 ? 'bg-primary text-on-primary' : 'bg-surface-container text-on-surface-variant'}`}>
                  3
                </div>
                <div>
                  <p className="font-label-sm text-xs uppercase">Step 3</p>
                  <p className="font-body-sm text-sm">Documents & Submit</p>
                </div>
              </button>
            </div>
          </div>

          {/* Form Content */}
          <div className="glass-panel rounded-2xl p-stack-lg flex flex-col gap-6">
            {step === 1 && (
              <div className="space-y-6">
                <h2 className="font-headline-md text-headline-md text-on-surface flex items-center gap-2">
                  <span className="material-symbols-outlined text-primary">person</span>
                  Personal Information
                </h2>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
                  <div className="space-y-1">
                    <label className="font-label-sm text-label-sm text-on-surface-variant">First Name</label>
                    <input
                      type="text"
                      className="w-full rounded-lg glass-input px-3 py-2.5 font-body-md text-on-surface"
                      value={formData.firstName}
                      onChange={(e) => setFormData({ ...formData, firstName: e.target.value })}
                    />
                  </div>
                  <div className="space-y-1">
                    <label className="font-label-sm text-label-sm text-on-surface-variant">Last Name</label>
                    <input
                      type="text"
                      className="w-full rounded-lg glass-input px-3 py-2.5 font-body-md text-on-surface"
                      value={formData.lastName}
                      onChange={(e) => setFormData({ ...formData, lastName: e.target.value })}
                    />
                  </div>
                  <div className="space-y-1">
                    <label className="font-label-sm text-label-sm text-on-surface-variant">Email</label>
                    <input
                      type="email"
                      className="w-full rounded-lg glass-input px-3 py-2.5 font-body-md text-on-surface"
                      value={formData.email}
                      onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                    />
                  </div>
                  <div className="space-y-1">
                    <label className="font-label-sm text-label-sm text-on-surface-variant">Phone Number</label>
                    <input
                      type="tel"
                      className="w-full rounded-lg glass-input px-3 py-2.5 font-body-md text-on-surface"
                      value={formData.phone}
                      onChange={(e) => setFormData({ ...formData, phone: e.target.value })}
                    />
                  </div>
                  <div className="space-y-1">
                    <label className="font-label-sm text-label-sm text-on-surface-variant">Date of Birth</label>
                    <input
                      type="date"
                      className="w-full rounded-lg glass-input px-3 py-2.5 font-body-md text-on-surface"
                      value={formData.dob}
                      onChange={(e) => setFormData({ ...formData, dob: e.target.value })}
                    />
                  </div>
                </div>
                <div className="flex justify-end pt-4">
                  <button
                    type="button"
                    onClick={() => setStep(2)}
                    className="px-6 py-2.5 bg-primary text-on-primary font-label-md rounded-lg hover:bg-primary-container transition-colors flex items-center gap-2"
                  >
                    Next: Academic Details
                    <span className="material-symbols-outlined text-[18px]">arrow_forward</span>
                  </button>
                </div>
              </div>
            )}

            {step === 2 && (
              <div className="space-y-6">
                <h2 className="font-headline-md text-headline-md text-on-surface flex items-center gap-2">
                  <span className="material-symbols-outlined text-primary">school</span>
                  Academic Details & Program
                </h2>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
                  <div className="space-y-1">
                    <label className="font-label-sm text-label-sm text-on-surface-variant">Desired Program</label>
                    <select
                      className="w-full rounded-lg glass-input px-3 py-2.5 font-body-md text-on-surface"
                      value={formData.program}
                      onChange={(e) => setFormData({ ...formData, program: e.target.value })}
                    >
                      <option>Computer Science, B.S.</option>
                      <option>Data Science & AI, B.S.</option>
                      <option>Information Systems, B.S.</option>
                      <option>Mathematics & Computing, B.S.</option>
                    </select>
                  </div>
                  <div className="space-y-1">
                    <label className="font-label-sm text-label-sm text-on-surface-variant">Admission Term</label>
                    <select
                      className="w-full rounded-lg glass-input px-3 py-2.5 font-body-md text-on-surface"
                      value={formData.term}
                      onChange={(e) => setFormData({ ...formData, term: e.target.value })}
                    >
                      <option>Fall 2024</option>
                      <option>Spring 2025</option>
                    </select>
                  </div>
                  <div className="space-y-1">
                    <label className="font-label-sm text-label-sm text-on-surface-variant">High School / Prior College</label>
                    <input
                      type="text"
                      className="w-full rounded-lg glass-input px-3 py-2.5 font-body-md text-on-surface"
                      value={formData.highSchool}
                      onChange={(e) => setFormData({ ...formData, highSchool: e.target.value })}
                    />
                  </div>
                  <div className="space-y-1">
                    <label className="font-label-sm text-label-sm text-on-surface-variant">Cumulative GPA</label>
                    <input
                      type="text"
                      className="w-full rounded-lg glass-input px-3 py-2.5 font-body-md text-on-surface"
                      value={formData.gpa}
                      onChange={(e) => setFormData({ ...formData, gpa: e.target.value })}
                    />
                  </div>
                  <div className="col-span-1 md:col-span-2 space-y-1">
                    <label className="font-label-sm text-label-sm text-on-surface-variant">Statement of Purpose</label>
                    <textarea
                      rows={4}
                      className="w-full rounded-lg glass-input px-3 py-2.5 font-body-md text-on-surface resize-none"
                      value={formData.statement}
                      onChange={(e) => setFormData({ ...formData, statement: e.target.value })}
                    ></textarea>
                  </div>
                </div>
                <div className="flex justify-between pt-4">
                  <button
                    type="button"
                    onClick={() => setStep(1)}
                    className="px-6 py-2.5 border border-outline-variant text-on-surface font-label-md rounded-lg hover:bg-surface-container transition-colors"
                  >
                    Back
                  </button>
                  <button
                    type="button"
                    onClick={() => setStep(3)}
                    className="px-6 py-2.5 bg-primary text-on-primary font-label-md rounded-lg hover:bg-primary-container transition-colors flex items-center gap-2"
                  >
                    Next: Upload Documents
                    <span className="material-symbols-outlined text-[18px]">arrow_forward</span>
                  </button>
                </div>
              </div>
            )}

            {step === 3 && (
              <form onSubmit={handleSubmit} className="space-y-6">
                <h2 className="font-headline-md text-headline-md text-on-surface flex items-center gap-2">
                  <span className="material-symbols-outlined text-primary">upload_file</span>
                  Required Documents & Review
                </h2>
                
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div className="border-2 border-dashed border-outline-variant/60 rounded-xl p-6 flex flex-col items-center justify-center text-center hover:border-primary/50 transition-colors bg-surface-container-low/30 cursor-pointer">
                    <span className="material-symbols-outlined text-primary text-[36px] mb-2">description</span>
                    <p className="font-headline-md text-sm font-semibold">Official High School Transcript</p>
                    <p className="font-label-sm text-xs text-on-surface-variant mt-1">PDF format (Max 10MB)</p>
                    <span className="mt-3 px-3 py-1 bg-surface-container rounded-full text-xs text-primary font-label-sm">Browse File</span>
                  </div>

                  <div className="border-2 border-dashed border-outline-variant/60 rounded-xl p-6 flex flex-col items-center justify-center text-center hover:border-primary/50 transition-colors bg-surface-container-low/30 cursor-pointer">
                    <span className="material-symbols-outlined text-tertiary text-[36px] mb-2">badge</span>
                    <p className="font-headline-md text-sm font-semibold">Government ID / Passport</p>
                    <p className="font-label-sm text-xs text-on-surface-variant mt-1">PDF or JPG (Max 5MB)</p>
                    <span className="mt-3 px-3 py-1 bg-surface-container rounded-full text-xs text-tertiary font-label-sm">Browse File</span>
                  </div>
                </div>

                <div className="bg-surface-container-low/50 rounded-xl p-4 border border-outline-variant/30 flex items-start gap-3">
                  <input type="checkbox" id="declaration" className="mt-1 rounded text-primary focus:ring-primary" required />
                  <label htmlFor="declaration" className="text-body-sm text-on-surface-variant">
                    I declare that all information submitted in this application is accurate and complete to the best of my knowledge.
                  </label>
                </div>

                <div className="flex justify-between pt-4">
                  <button
                    type="button"
                    onClick={() => setStep(2)}
                    className="px-6 py-2.5 border border-outline-variant text-on-surface font-label-md rounded-lg hover:bg-surface-container transition-colors"
                  >
                    Back
                  </button>
                  <button
                    type="submit"
                    className="px-8 py-2.5 bg-primary text-on-primary font-label-md rounded-lg hover:bg-primary-container shadow-sm transition-all flex items-center gap-2"
                  >
                    <span className="material-symbols-outlined text-[18px]">send</span>
                    Submit Application
                  </button>
                </div>
              </form>
            )}
          </div>
        </div>
      </main>
    </div>
  );
}

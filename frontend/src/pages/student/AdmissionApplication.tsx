import { useState, useEffect } from 'react';
import StudentSidebar from '../../components/layout/StudentSidebar';
import TopNav from '../../components/layout/TopNav';
import { admissionApi, getErrorMessage } from '../../services/api';
import type {
  AdmissionApplication as AdmissionAppType,
  AdmissionDocument,
  DocumentType,
  CreateApplicationRequest,
} from '../../types/api';

export default function AdmissionApplication() {
  const [step, setStep] = useState(1);
  const [application, setApplication] = useState<AdmissionAppType | null>(null);
  const [documents, setDocuments] = useState<AdmissionDocument[]>([]);
  const [loading, setLoading] = useState(true);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [uploadingDoc, setUploadingDoc] = useState<DocumentType | null>(null);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    email: '',
    phone: '',
    dob: '',
    program: 'Computer Science, B.S.',
    department: 'Computer Science',
    term: 'Fall 2024',
    highSchool: '',
    gpa: '3.85',
    statement: '',
  });

  const loadApplication = async () => {
    try {
      setLoading(true);
      setError('');
      const app = await admissionApi.getMyApplication();
      if (app) {
        setApplication(app);
        setFormData((prev) => ({
          ...prev,
          program: app.program || prev.program,
          department: app.department || prev.department,
          highSchool: app.academicDetails?.previousInstitution || prev.highSchool,
          gpa: app.academicDetails?.cgpa?.toString() || prev.gpa,
        }));
        // Load documents
        const docs = await admissionApi.getDocuments(app.id);
        setDocuments(docs || []);
      }
    } catch (err) {
      setError(getErrorMessage(err, 'Failed to load admission application.'));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadApplication();
  }, []);

  const handleDocumentUpload = async (type: DocumentType, fileName: string) => {
    if (!application) {
      setError('Please complete academic details first to create your application draft before uploading documents.');
      return;
    }
    try {
      setUploadingDoc(type);
      setError('');
      const uploaded = await admissionApi.uploadDocument(application.id, {
        type,
        fileName,
        fileUrl: `mock-storage://admissions/${application.id}/${fileName}`,
      });
      setDocuments((prev) => [...prev.filter((d) => d.type !== type), uploaded]);
      setSuccess(`${fileName} uploaded successfully.`);
      setTimeout(() => setSuccess(''), 3000);
    } catch (err) {
      setError(getErrorMessage(err, 'Failed to upload document.'));
    } finally {
      setUploadingDoc(null);
    }
  };

  const handleDocumentDelete = async (docId: string) => {
    if (!application) return;
    try {
      await admissionApi.deleteDocument(application.id, docId);
      setDocuments((prev) => prev.filter((d) => d.id !== docId));
      setSuccess('Document removed successfully.');
      setTimeout(() => setSuccess(''), 3000);
    } catch (err) {
      setError(getErrorMessage(err, 'Failed to delete document.'));
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    setIsSubmitting(true);

    try {
      let currentApp = application;

      // If application draft does not exist yet, create it
      if (!currentApp) {
        const createPayload: CreateApplicationRequest = {
          program: formData.program,
          department: formData.department,
          academicDetails: {
            previousInstitution: formData.highSchool,
            cgpa: parseFloat(formData.gpa) || 3.5,
            graduationYear: 2024,
          },
        };
        currentApp = await admissionApi.apply(createPayload);
        setApplication(currentApp);
      }

      if (!currentApp) {
        throw new Error('Application could not be created.');
      }

      // Submit application
      const submittedApp = await admissionApi.submitApplication(currentApp.id);
      setApplication(submittedApp);
      setSuccess('Application submitted successfully for university review.');
    } catch (err) {
      setError(getErrorMessage(err, 'Failed to submit admission application.'));
    } finally {
      setIsSubmitting(false);
    }
  };

  const isSubmitted =
    application &&
    application.status !== 'DRAFT' &&
    application.status !== 'CHANGES_REQUESTED';

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
                <span className="text-on-surface-variant font-label-sm text-label-sm">
                  • {application ? `Application #${application.applicationId || application.id.slice(0, 8)}` : 'New Application'}
                </span>
              </div>
              <h1 className="font-display-lg text-display-lg text-on-surface">University Admission Application</h1>
              <p className="font-body-lg text-body-lg text-on-surface-variant mt-1">
                Complete all required sections to submit your application for Fall 2024.
              </p>
            </div>

            {application && (
              <div className="inline-flex items-center gap-2 bg-emerald-50 text-emerald-800 border border-emerald-200 px-4 py-2 rounded-full font-label-md text-label-md">
                <span className="material-symbols-outlined text-[18px] text-emerald-600" style={{ fontVariationSettings: "'FILL' 1" }}>
                  verified
                </span>
                Status: {application.status.replace('_', ' ')}
              </div>
            )}
          </div>

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
              <p className="mt-4 text-on-surface-variant font-body-md">Loading admission details...</p>
            </div>
          ) : (
            <>
              {/* Stepper */}
              <div className="glass-card rounded-xl p-4 flex justify-between items-center relative overflow-hidden">
                <div className="flex items-center gap-4 flex-1">
                  <button
                    onClick={() => setStep(1)}
                    className={`flex items-center gap-3 text-left ${step === 1 ? 'text-primary font-semibold' : 'text-on-surface-variant'}`}
                  >
                    <div
                      className={`w-8 h-8 rounded-full flex items-center justify-center font-label-sm ${
                        step === 1 ? 'bg-primary text-on-primary' : 'bg-surface-container text-on-surface-variant'
                      }`}
                    >
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
                    <div
                      className={`w-8 h-8 rounded-full flex items-center justify-center font-label-sm ${
                        step === 2 ? 'bg-primary text-on-primary' : 'bg-surface-container text-on-surface-variant'
                      }`}
                    >
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
                    <div
                      className={`w-8 h-8 rounded-full flex items-center justify-center font-label-sm ${
                        step === 3 ? 'bg-primary text-on-primary' : 'bg-surface-container text-on-surface-variant'
                      }`}
                    >
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
                          disabled={Boolean(isSubmitted)}
                          className="w-full rounded-lg glass-input px-3 py-2.5 font-body-md text-on-surface"
                          value={formData.firstName}
                          onChange={(e) => setFormData({ ...formData, firstName: e.target.value })}
                        />
                      </div>
                      <div className="space-y-1">
                        <label className="font-label-sm text-label-sm text-on-surface-variant">Last Name</label>
                        <input
                          type="text"
                          disabled={Boolean(isSubmitted)}
                          className="w-full rounded-lg glass-input px-3 py-2.5 font-body-md text-on-surface"
                          value={formData.lastName}
                          onChange={(e) => setFormData({ ...formData, lastName: e.target.value })}
                        />
                      </div>
                      <div className="space-y-1">
                        <label className="font-label-sm text-label-sm text-on-surface-variant">Email</label>
                        <input
                          type="email"
                          disabled={Boolean(isSubmitted)}
                          className="w-full rounded-lg glass-input px-3 py-2.5 font-body-md text-on-surface"
                          value={formData.email}
                          onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                        />
                      </div>
                      <div className="space-y-1">
                        <label className="font-label-sm text-label-sm text-on-surface-variant">Phone Number</label>
                        <input
                          type="tel"
                          disabled={Boolean(isSubmitted)}
                          className="w-full rounded-lg glass-input px-3 py-2.5 font-body-md text-on-surface"
                          value={formData.phone}
                          onChange={(e) => setFormData({ ...formData, phone: e.target.value })}
                        />
                      </div>
                      <div className="space-y-1">
                        <label className="font-label-sm text-label-sm text-on-surface-variant">Date of Birth</label>
                        <input
                          type="date"
                          disabled={Boolean(isSubmitted)}
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
                          disabled={Boolean(isSubmitted)}
                          className="w-full rounded-lg glass-input px-3 py-2.5 font-body-md text-on-surface"
                          value={formData.program}
                          onChange={(e) => {
                            const prog = e.target.value;
                            const dept = prog.includes('Computer Science')
                              ? 'Computer Science'
                              : prog.includes('Data')
                              ? 'Data Science'
                              : prog.includes('Information')
                              ? 'Information Systems'
                              : 'Mathematics';
                            setFormData({ ...formData, program: prog, department: dept });
                          }}
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
                          disabled={Boolean(isSubmitted)}
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
                          disabled={Boolean(isSubmitted)}
                          className="w-full rounded-lg glass-input px-3 py-2.5 font-body-md text-on-surface"
                          value={formData.highSchool}
                          onChange={(e) => setFormData({ ...formData, highSchool: e.target.value })}
                          placeholder="e.g. Westview High School"
                        />
                      </div>
                      <div className="space-y-1">
                        <label className="font-label-sm text-label-sm text-on-surface-variant">Cumulative GPA</label>
                        <input
                          type="text"
                          disabled={Boolean(isSubmitted)}
                          className="w-full rounded-lg glass-input px-3 py-2.5 font-body-md text-on-surface"
                          value={formData.gpa}
                          onChange={(e) => setFormData({ ...formData, gpa: e.target.value })}
                        />
                      </div>
                      <div className="col-span-1 md:col-span-2 space-y-1">
                        <label className="font-label-sm text-label-sm text-on-surface-variant">Statement of Purpose</label>
                        <textarea
                          rows={4}
                          disabled={Boolean(isSubmitted)}
                          className="w-full rounded-lg glass-input px-3 py-2.5 font-body-md text-on-surface resize-none"
                          value={formData.statement}
                          onChange={(e) => setFormData({ ...formData, statement: e.target.value })}
                          placeholder="Describe your academic goals and interests..."
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
                        onClick={async () => {
                          // If not created yet, create draft so documents can be attached
                          if (!application) {
                            try {
                              const created = await admissionApi.apply({
                                program: formData.program,
                                department: formData.department,
                                academicDetails: {
                                  previousInstitution: formData.highSchool,
                                  cgpa: parseFloat(formData.gpa) || 3.5,
                                  graduationYear: 2024,
                                },
                              });
                              setApplication(created);
                            } catch (err) {
                              setError(getErrorMessage(err, 'Could not save application draft.'));
                            }
                          }
                          setStep(3);
                        }}
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
                      {/* Transcript Upload */}
                      <div
                        onClick={() => {
                          if (!isSubmitted) handleDocumentUpload('MARKS_CERTIFICATE', 'High_School_Transcript.pdf');
                        }}
                        className={`border-2 border-dashed rounded-xl p-6 flex flex-col items-center justify-center text-center transition-colors bg-surface-container-low/30 ${
                          isSubmitted ? 'opacity-60 cursor-not-allowed border-outline-variant/40' : 'cursor-pointer hover:border-primary/50 border-outline-variant/60'
                        }`}
                      >
                        <span className="material-symbols-outlined text-primary text-[36px] mb-2">description</span>
                        <p className="font-headline-md text-sm font-semibold">Official High School Transcript</p>
                        <p className="font-label-sm text-xs text-on-surface-variant mt-1">PDF format (Max 10MB)</p>
                        <span className="mt-3 px-3 py-1 bg-surface-container rounded-full text-xs text-primary font-label-sm">
                          {uploadingDoc === 'MARKS_CERTIFICATE' ? 'Uploading...' : 'Upload Transcript'}
                        </span>
                      </div>

                      {/* Government ID Upload */}
                      <div
                        onClick={() => {
                          if (!isSubmitted) handleDocumentUpload('ID_PROOF', 'Government_Passport_Copy.pdf');
                        }}
                        className={`border-2 border-dashed rounded-xl p-6 flex flex-col items-center justify-center text-center transition-colors bg-surface-container-low/30 ${
                          isSubmitted ? 'opacity-60 cursor-not-allowed border-outline-variant/40' : 'cursor-pointer hover:border-tertiary/50 border-outline-variant/60'
                        }`}
                      >
                        <span className="material-symbols-outlined text-tertiary text-[36px] mb-2">badge</span>
                        <p className="font-headline-md text-sm font-semibold">Government ID / Passport</p>
                        <p className="font-label-sm text-xs text-on-surface-variant mt-1">PDF or JPG (Max 5MB)</p>
                        <span className="mt-3 px-3 py-1 bg-surface-container rounded-full text-xs text-tertiary font-label-sm">
                          {uploadingDoc === 'ID_PROOF' ? 'Uploading...' : 'Upload ID Proof'}
                        </span>
                      </div>
                    </div>

                    {/* Uploaded Documents List */}
                    {documents.length > 0 && (
                      <div className="space-y-2">
                        <h4 className="font-label-sm text-xs text-on-surface-variant uppercase tracking-wider">
                          Attached Documents ({documents.length})
                        </h4>
                        <div className="space-y-2">
                          {documents.map((doc) => (
                            <div
                              key={doc.id}
                              className="p-3 rounded-lg border border-outline-variant/30 bg-surface-container-low/40 flex items-center justify-between"
                            >
                              <div className="flex items-center gap-3">
                                <span className="material-symbols-outlined text-primary text-[20px]">
                                  check_circle
                                </span>
                                <div>
                                  <p className="font-semibold text-xs text-on-surface">{doc.fileName}</p>
                                  <p className="text-[10px] text-on-surface-variant">
                                    Type: {doc.type} • Status: {doc.status}
                                  </p>
                                </div>
                              </div>
                              {!isSubmitted && (
                                <button
                                  type="button"
                                  onClick={() => handleDocumentDelete(doc.id)}
                                  className="text-on-surface-variant hover:text-error p-1 transition-colors"
                                  title="Delete document"
                                >
                                  <span className="material-symbols-outlined text-[18px]">delete</span>
                                </button>
                              )}
                            </div>
                          ))}
                        </div>
                      </div>
                    )}

                    <div className="bg-surface-container-low/50 rounded-xl p-4 border border-outline-variant/30 flex items-start gap-3">
                      <input
                        type="checkbox"
                        id="declaration"
                        disabled={Boolean(isSubmitted)}
                        defaultChecked={Boolean(isSubmitted)}
                        className="mt-1 rounded text-primary focus:ring-primary"
                        required
                      />
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
                        disabled={Boolean(isSubmitted || isSubmitting)}
                        className="px-8 py-2.5 bg-primary text-on-primary font-label-md rounded-lg hover:bg-primary-container shadow-sm transition-all flex items-center gap-2 disabled:opacity-50 disabled:cursor-not-allowed"
                      >
                        <span className="material-symbols-outlined text-[18px]">send</span>
                        {isSubmitting
                          ? 'Submitting...'
                          : isSubmitted
                          ? 'Application Submitted'
                          : 'Submit Application'}
                      </button>
                    </div>
                  </form>
                )}
              </div>
            </>
          )}
        </div>
      </main>
    </div>
  );
}

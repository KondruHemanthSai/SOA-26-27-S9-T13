import { useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import AdminSidebar from '../../components/layout/AdminSidebar';

export default function AdminCourseEditor() {
  const { courseId } = useParams();
  const [courseCode, setCourseCode] = useState(courseId || 'CS501');
  const [courseName, setCourseName] = useState('Machine Learning');
  const [department, setDepartment] = useState('Computer Science');
  const [credits, setCredits] = useState('4');
  const [capacity, setCapacity] = useState('120');
  const [waitlistCap, setWaitlistCap] = useState('30');
  const [description, setDescription] = useState(
    'An introduction to machine learning and statistical pattern recognition. Topics include supervised learning, unsupervised learning, learning theory, reinforcement learning and adaptive control.'
  );

  const [prereqs, setPrereqs] = useState([
    { code: 'CS401', name: 'Data Structures', credits: 4, dept: 'CS' },
    { code: 'MATH102', name: 'Calculus II', credits: 3, dept: 'MA' },
  ]);

  const [saved, setSaved] = useState(false);

  const handleSave = () => {
    setSaved(true);
    setTimeout(() => setSaved(false), 3000);
  };

  const removePrereq = (code: string) => {
    setPrereqs(prereqs.filter((p) => p.code !== code));
  };

  return (
    <div className="bg-background text-on-surface font-body-md min-h-screen flex antialiased">
      <AdminSidebar />

      <div className="flex-1 md:ml-64 flex flex-col min-h-screen">
        {/* TopNavBar */}
        <header className="sticky top-0 w-full z-40 bg-surface/60 backdrop-blur-lg border-b border-primary/10 shadow-sm flex justify-between items-center px-gutter h-16">
          <div className="flex items-center gap-4">
            <Link to="/admin/courses" className="text-on-surface-variant hover:text-primary transition-colors flex items-center">
              <span className="material-symbols-outlined text-[20px]">arrow_back</span>
              <span className="ml-1 font-label-md text-label-md">Back to Courses</span>
            </Link>
          </div>
          {saved && (
            <span className="text-emerald-700 bg-emerald-50 px-3 py-1 rounded-full font-label-sm text-xs border border-emerald-200">
              Changes Saved Successfully
            </span>
          )}
        </header>

        {/* Canvas Content */}
        <main className="flex-1 p-margin-mobile md:p-margin-desktop overflow-y-auto">
          <div className="max-w-4xl mx-auto space-y-stack-lg pb-24">
            {/* Page Header */}
            <div className="flex flex-col md:flex-row md:items-end justify-between gap-4">
              <div>
                <div className="flex items-center gap-3 mb-2">
                  <span className="px-2.5 py-1 bg-tertiary-fixed-dim/20 text-tertiary font-label-sm text-label-sm uppercase tracking-wider rounded">
                    {courseCode}
                  </span>
                  <span className="px-2.5 py-1 bg-surface-container-high text-on-surface-variant rounded font-label-sm text-label-sm">
                    Active
                  </span>
                </div>
                <h1 className="font-display-lg text-display-lg text-on-surface">{courseName}</h1>
                <p className="font-body-lg text-body-lg text-on-surface-variant mt-2">
                  Edit course details, capacity, and prerequisite requirements.
                </p>
              </div>
              <div className="flex gap-3">
                <Link
                  to="/admin/courses"
                  className="px-4 py-2 border border-outline-variant text-on-surface font-label-md text-label-md rounded-lg hover:bg-surface-container transition-colors"
                >
                  Cancel
                </Link>
                <button
                  onClick={handleSave}
                  className="px-6 py-2 bg-primary text-on-primary font-label-md text-label-md font-semibold rounded-lg shadow-sm hover:bg-primary-container transition-colors flex items-center gap-2"
                >
                  <span className="material-symbols-outlined text-[18px]">save</span>
                  Save Changes
                </button>
              </div>
            </div>

            {/* Bento Grid Layout for Sections */}
            <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
              {/* General Info & Enrollment (Span 2) */}
              <div className="lg:col-span-2 space-y-6">
                {/* General Information Card */}
                <div className="glass-panel rounded-xl p-6">
                  <h3 className="font-headline-md text-body-lg font-semibold text-primary mb-6 flex items-center gap-2">
                    <span className="material-symbols-outlined text-primary">info</span>
                    General Information
                  </h3>
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
                    <div className="space-y-1">
                      <label className="font-label-sm text-label-sm text-on-surface-variant">Course Name</label>
                      <input
                        className="w-full rounded-lg glass-input px-3 py-2 font-body-md text-body-md text-on-surface"
                        type="text"
                        value={courseName}
                        onChange={(e) => setCourseName(e.target.value)}
                      />
                    </div>
                    <div className="space-y-1">
                      <label className="font-label-sm text-label-sm text-on-surface-variant">Course Code</label>
                      <input
                        className="w-full rounded-lg glass-input px-3 py-2 font-label-md text-body-md text-on-surface uppercase"
                        type="text"
                        value={courseCode}
                        onChange={(e) => setCourseCode(e.target.value)}
                      />
                    </div>
                    <div className="space-y-1">
                      <label className="font-label-sm text-label-sm text-on-surface-variant">Department</label>
                      <select
                        className="w-full rounded-lg glass-input px-3 py-2 font-body-md text-body-md text-on-surface"
                        value={department}
                        onChange={(e) => setDepartment(e.target.value)}
                      >
                        <option>Computer Science</option>
                        <option>Mathematics</option>
                        <option>Humanities</option>
                      </select>
                    </div>
                    <div className="space-y-1">
                      <label className="font-label-sm text-label-sm text-on-surface-variant">Credits</label>
                      <input
                        className="w-full rounded-lg glass-input px-3 py-2 font-body-md text-body-md text-on-surface"
                        type="number"
                        value={credits}
                        onChange={(e) => setCredits(e.target.value)}
                      />
                    </div>
                    <div className="col-span-1 md:col-span-2 space-y-1">
                      <label className="font-label-sm text-label-sm text-on-surface-variant">Description</label>
                      <textarea
                        className="w-full rounded-lg glass-input px-3 py-2 font-body-md text-body-md text-on-surface resize-none"
                        rows={4}
                        value={description}
                        onChange={(e) => setDescription(e.target.value)}
                      ></textarea>
                    </div>
                  </div>
                </div>

                {/* Enrollment Control Card */}
                <div className="glass-panel rounded-xl p-6">
                  <h3 className="font-headline-md text-body-lg font-semibold text-primary mb-6 flex items-center gap-2">
                    <span className="material-symbols-outlined text-primary">groups</span>
                    Enrollment Control
                  </h3>
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
                    <div className="space-y-1">
                      <label className="font-label-sm text-label-sm text-on-surface-variant">Total Capacity</label>
                      <input
                        className="w-full rounded-lg glass-input px-3 py-2 font-body-md text-body-md text-on-surface"
                        type="number"
                        value={capacity}
                        onChange={(e) => setCapacity(e.target.value)}
                      />
                    </div>
                    <div className="space-y-1">
                      <label className="font-label-sm text-label-sm text-on-surface-variant">Waitlist Cap</label>
                      <input
                        className="w-full rounded-lg glass-input px-3 py-2 font-body-md text-body-md text-on-surface"
                        type="number"
                        value={waitlistCap}
                        onChange={(e) => setWaitlistCap(e.target.value)}
                      />
                    </div>
                  </div>
                </div>
              </div>

              {/* Prerequisite Manager (Right Col) */}
              <div className="lg:col-span-1">
                <div className="glass-panel rounded-xl p-6 h-full flex flex-col relative overflow-hidden">
                  <div className="flex justify-between items-center mb-6 z-10">
                    <h3 className="font-headline-md text-body-lg font-semibold text-primary flex items-center gap-2">
                      <span className="material-symbols-outlined text-primary">account_tree</span>
                      Prerequisites
                    </h3>
                    <div className="bg-surface-container-low rounded-lg p-1 flex items-center border border-outline-variant/50">
                      <button className="px-3 py-1 rounded bg-white shadow-sm text-primary font-label-sm text-label-sm font-bold">
                        AND
                      </button>
                    </div>
                  </div>

                  <p className="font-body-sm text-body-sm text-on-surface-variant mb-4 z-10">
                    Students must complete these courses before enrolling.
                  </p>

                  <div className="space-y-3 mb-6 z-10 flex-1">
                    {prereqs.map((p, idx) => (
                      <div key={p.code}>
                        <div className="bg-surface-container/50 border border-outline-variant/30 rounded-lg p-3 flex justify-between items-center group hover:bg-surface-container transition-colors">
                          <div className="flex items-center gap-3">
                            <div className="w-8 h-8 rounded bg-primary/10 text-primary flex items-center justify-center font-bold text-xs font-label-sm">
                              {p.dept}
                            </div>
                            <div>
                              <p className="font-label-md text-label-md text-on-surface">{p.name}</p>
                              <p className="font-label-sm text-[11px] text-on-surface-variant">{p.code} • {p.credits} Credits</p>
                            </div>
                          </div>
                          <button
                            onClick={() => removePrereq(p.code)}
                            className="text-on-surface-variant hover:text-error opacity-0 group-hover:opacity-100 transition-opacity"
                          >
                            <span className="material-symbols-outlined text-[20px]">close</span>
                          </button>
                        </div>
                        {idx < prereqs.length - 1 && (
                          <div className="flex justify-center my-1 relative z-20">
                            <span className="bg-surface-container-high text-on-surface-variant px-2 py-0.5 rounded text-[10px] font-bold tracking-widest border border-surface">
                              AND
                            </span>
                          </div>
                        )}
                      </div>
                    ))}
                  </div>

                  <div className="mt-auto pt-4 border-t border-outline-variant/20 z-10">
                    <label className="font-label-sm text-label-sm text-on-surface-variant block mb-2">Add Requirement</label>
                    <input
                      className="w-full rounded-lg glass-input px-3 py-2 font-body-sm text-body-sm text-on-surface"
                      placeholder="Enter course code e.g. CS201..."
                      type="text"
                    />
                  </div>
                </div>
              </div>
            </div>
          </div>
        </main>
      </div>
    </div>
  );
}

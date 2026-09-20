import { useState, useEffect } from 'react';
import { Link, useParams, useNavigate } from 'react-router-dom';
import AdminSidebar from '../../components/layout/AdminSidebar';
import { courseApi, getErrorMessage } from '../../services/api';
import type {
  PrerequisiteItem,
  CourseType,
  CreateCourseRequest,
  UpdateCourseRequest,
} from '../../types/api';

export default function AdminCourseEditor() {
  const { courseId } = useParams();
  const navigate = useNavigate();
  const isNew = courseId === 'new' || !courseId;

  const [courseCode, setCourseCode] = useState('');
  const [courseName, setCourseName] = useState('');
  const [department, setDepartment] = useState('Computer Science');
  const [semester, setSemester] = useState('1');
  const [credits, setCredits] = useState('4');
  const [capacity, setCapacity] = useState('40');
  const [courseType, setCourseType] = useState<CourseType>('THEORY');
  const [faculty, setFaculty] = useState('');
  const [description, setDescription] = useState('');

  const [prereqs, setPrereqs] = useState<PrerequisiteItem[]>([]);
  const [newPrereqCode, setNewPrereqCode] = useState('');
  const [loading, setLoading] = useState(false);
  const [isSaving, setIsSaving] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  useEffect(() => {
    const currentCourseId = courseId;
    if (!isNew && currentCourseId) {
      async function loadCourse(id: string) {
        try {
          setLoading(true);
          setError('');
          const data = await courseApi.getCourse(id);
          setCourseCode(data.courseCode);
          setCourseName(data.courseName);
          setDepartment(data.department || 'Computer Science');
          setSemester(data.semester?.toString() || '1');
          setCredits(data.credits?.toString() || '4');
          setCapacity(data.capacity?.toString() || '40');
          setCourseType(data.courseType || 'THEORY');
          setFaculty(data.faculty || '');
          setDescription(data.description || '');

          // Load prerequisites
          try {
            const prereqRes = await courseApi.getPrerequisites(id);
            setPrereqs(prereqRes.prerequisites || []);
          } catch {
            setPrereqs([]);
          }
        } catch (err) {
          setError(getErrorMessage(err, 'Failed to load course details.'));
        } finally {
          setLoading(false);
        }
      }
      loadCourse(currentCourseId);
    }
  }, [courseId, isNew]);

  const handleSave = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    setIsSaving(true);

    try {
      if (isNew) {
        const createPayload: CreateCourseRequest = {
          courseCode: courseCode.trim().toUpperCase(),
          courseName: courseName.trim(),
          department,
          semester: parseInt(semester) || 1,
          credits: parseInt(credits) || 3,
          capacity: parseInt(capacity) || 40,
          courseType,
          faculty: faculty.trim() || undefined,
          description: description.trim() || undefined,
        };
        const created = await courseApi.createCourse(createPayload);
        setSuccess(`Course ${created.courseCode} created successfully!`);
        setTimeout(() => navigate('/admin/courses'), 1500);
      } else if (courseId) {
        const updatePayload: UpdateCourseRequest = {
          courseName: courseName.trim(),
          department,
          semester: parseInt(semester) || 1,
          credits: parseInt(credits) || 3,
          capacity: parseInt(capacity) || 40,
          courseType,
          faculty: faculty.trim() || undefined,
          description: description.trim() || undefined,
        };
        await courseApi.updateCourse(courseId, updatePayload);
        setSuccess('Course updated successfully.');
        setTimeout(() => setSuccess(''), 3500);
      }
    } catch (err) {
      setError(getErrorMessage(err, 'Failed to save course.'));
    } finally {
      setIsSaving(false);
    }
  };

  const handleAddPrereq = async () => {
    if (!newPrereqCode.trim() || !courseId || isNew) return;
    try {
      const res = await courseApi.addPrerequisite(courseId, newPrereqCode.trim().toUpperCase());
      setPrereqs(res.prerequisites || []);
      setNewPrereqCode('');
      setSuccess('Prerequisite added.');
      setTimeout(() => setSuccess(''), 3000);
    } catch (err) {
      setError(getErrorMessage(err, 'Failed to add prerequisite. Make sure the course exists.'));
    }
  };

  const handleRemovePrereq = async (prereqId: string) => {
    if (!courseId) return;
    try {
      await courseApi.deletePrerequisite(courseId, prereqId);
      setPrereqs((prev) => prev.filter((p) => p.id !== prereqId));
      setSuccess('Prerequisite removed.');
      setTimeout(() => setSuccess(''), 3000);
    } catch (err) {
      setError(getErrorMessage(err, 'Failed to remove prerequisite.'));
    }
  };

  return (
    <div className="bg-background text-on-surface font-body-md min-h-screen flex antialiased">
      <AdminSidebar />

      <div className="flex-1 md:ml-64 flex flex-col min-h-screen">
        {/* TopNavBar */}
        <header className="sticky top-0 w-full z-40 bg-surface/60 backdrop-blur-lg border-b border-primary/10 shadow-sm flex justify-between items-center px-gutter h-16">
          <div className="flex items-center gap-4">
            <Link
              to="/admin/courses"
              className="text-on-surface-variant hover:text-primary transition-colors flex items-center"
            >
              <span className="material-symbols-outlined text-[20px]">arrow_back</span>
              <span className="ml-1 font-label-md text-label-md">Back to Courses</span>
            </Link>
          </div>
          {success && (
            <span className="text-emerald-700 bg-emerald-50 px-3 py-1 rounded-full font-label-sm text-xs border border-emerald-200 flex items-center gap-1">
              <span className="material-symbols-outlined text-[16px] text-emerald-600">check_circle</span>
              {success}
            </span>
          )}
        </header>

        {/* Canvas Content */}
        <main className="flex-1 p-margin-mobile md:p-margin-desktop overflow-y-auto">
          <div className="max-w-4xl mx-auto space-y-stack-lg pb-24">
            {error && (
              <div className="p-4 bg-error-container/30 border border-error/20 text-on-error-container rounded-xl font-body-sm text-body-sm flex items-center gap-3">
                <span className="material-symbols-outlined text-error">error</span>
                {error}
              </div>
            )}

            {/* Page Header */}
            <div className="flex flex-col md:flex-row md:items-end justify-between gap-4">
              <div>
                <div className="flex items-center gap-3 mb-2">
                  <span className="px-2.5 py-1 bg-primary/10 text-primary font-label-sm text-label-sm uppercase tracking-wider rounded">
                    {courseCode || (isNew ? 'NEW COURSE' : 'COURSE')}
                  </span>
                  <span className="px-2.5 py-1 bg-surface-container-high text-on-surface-variant rounded font-label-sm text-label-sm">
                    {isNew ? 'Draft' : 'Active'}
                  </span>
                </div>
                <h1 className="font-display-lg text-display-lg text-on-surface">
                  {isNew ? 'Create New Course' : courseName}
                </h1>
                <p className="font-body-lg text-body-lg text-on-surface-variant mt-2">
                  {isNew
                    ? 'Fill out course code, academic department, and enrollment capacity.'
                    : 'Edit course details, capacity, and prerequisite requirements.'}
                </p>
              </div>
            </div>

            {loading ? (
              <div className="p-16 flex flex-col items-center justify-center">
                <div className="w-10 h-10 border-4 border-primary/20 border-t-primary rounded-full animate-spin"></div>
                <p className="mt-4 text-on-surface-variant font-body-md">Loading course details...</p>
              </div>
            ) : (
              <form onSubmit={handleSave} className="space-y-6">
                <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
                  {/* General Info & Enrollment (Span 2) */}
                  <div className="lg:col-span-2 space-y-6">
                    <div className="glass-panel rounded-xl p-6 space-y-4">
                      <h3 className="font-headline-md text-body-lg font-semibold text-primary mb-2 flex items-center gap-2">
                        <span className="material-symbols-outlined text-primary">info</span>
                        General Information
                      </h3>

                      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                        <div className="space-y-1">
                          <label className="font-label-sm text-xs text-on-surface-variant">Course Code</label>
                          <input
                            type="text"
                            required
                            disabled={!isNew}
                            placeholder="e.g. CS501"
                            value={courseCode}
                            onChange={(e) => setCourseCode(e.target.value)}
                            className="w-full rounded-lg glass-input px-3 py-2 font-body-sm text-on-surface disabled:opacity-60"
                          />
                        </div>
                        <div className="space-y-1">
                          <label className="font-label-sm text-xs text-on-surface-variant">Course Name</label>
                          <input
                            type="text"
                            required
                            placeholder="e.g. Machine Learning"
                            value={courseName}
                            onChange={(e) => setCourseName(e.target.value)}
                            className="w-full rounded-lg glass-input px-3 py-2 font-body-sm text-on-surface"
                          />
                        </div>
                        <div className="space-y-1">
                          <label className="font-label-sm text-xs text-on-surface-variant">Department</label>
                          <select
                            value={department}
                            onChange={(e) => setDepartment(e.target.value)}
                            className="w-full rounded-lg glass-input px-3 py-2 font-body-sm text-on-surface"
                          >
                            <option value="Computer Science">Computer Science</option>
                            <option value="Mathematics">Mathematics</option>
                            <option value="Physics">Physics</option>
                            <option value="Humanities">Humanities</option>
                          </select>
                        </div>
                        <div className="space-y-1">
                          <label className="font-label-sm text-xs text-on-surface-variant">Semester</label>
                          <select
                            value={semester}
                            onChange={(e) => setSemester(e.target.value)}
                            className="w-full rounded-lg glass-input px-3 py-2 font-body-sm text-on-surface"
                          >
                            <option value="1">Semester 1</option>
                            <option value="2">Semester 2</option>
                            <option value="3">Semester 3</option>
                            <option value="4">Semester 4</option>
                            <option value="5">Semester 5</option>
                          </select>
                        </div>
                        <div className="space-y-1">
                          <label className="font-label-sm text-xs text-on-surface-variant">Credits</label>
                          <input
                            type="number"
                            min="1"
                            max="8"
                            required
                            value={credits}
                            onChange={(e) => setCredits(e.target.value)}
                            className="w-full rounded-lg glass-input px-3 py-2 font-body-sm text-on-surface"
                          />
                        </div>
                        <div className="space-y-1">
                          <label className="font-label-sm text-xs text-on-surface-variant">Capacity</label>
                          <input
                            type="number"
                            min="1"
                            required
                            value={capacity}
                            onChange={(e) => setCapacity(e.target.value)}
                            className="w-full rounded-lg glass-input px-3 py-2 font-body-sm text-on-surface"
                          />
                        </div>
                        <div className="space-y-1">
                          <label className="font-label-sm text-xs text-on-surface-variant">Course Type</label>
                          <select
                            value={courseType}
                            onChange={(e) => setCourseType(e.target.value as CourseType)}
                            className="w-full rounded-lg glass-input px-3 py-2 font-body-sm text-on-surface"
                          >
                            <option value="THEORY">Theory</option>
                            <option value="LAB">Lab</option>
                            <option value="SEMINAR">Seminar</option>
                            <option value="PROJECT">Project</option>
                          </select>
                        </div>
                        <div className="space-y-1">
                          <label className="font-label-sm text-xs text-on-surface-variant">Lead Faculty</label>
                          <input
                            type="text"
                            placeholder="e.g. Dr. Jane Smith"
                            value={faculty}
                            onChange={(e) => setFaculty(e.target.value)}
                            className="w-full rounded-lg glass-input px-3 py-2 font-body-sm text-on-surface"
                          />
                        </div>
                        <div className="md:col-span-2 space-y-1">
                          <label className="font-label-sm text-xs text-on-surface-variant">Description</label>
                          <textarea
                            rows={4}
                            value={description}
                            onChange={(e) => setDescription(e.target.value)}
                            placeholder="Course outline and learning objectives..."
                            className="w-full rounded-lg glass-input px-3 py-2 font-body-sm text-on-surface resize-none"
                          ></textarea>
                        </div>
                      </div>
                    </div>
                  </div>

                  {/* Prerequisites Side Panel (Span 1) */}
                  <div className="space-y-6">
                    <div className="glass-panel rounded-xl p-6 flex flex-col justify-between">
                      <div>
                        <h3 className="font-headline-md text-base font-semibold text-primary mb-3 flex items-center gap-2">
                          <span className="material-symbols-outlined text-primary text-[20px]">account_tree</span>
                          Prerequisites
                        </h3>

                        {!isNew && (
                          <div className="flex gap-2 mb-4">
                            <input
                              type="text"
                              placeholder="e.g. CS401"
                              value={newPrereqCode}
                              onChange={(e) => setNewPrereqCode(e.target.value)}
                              className="flex-1 rounded-lg glass-input px-2.5 py-1.5 text-xs text-on-surface"
                            />
                            <button
                              type="button"
                              onClick={handleAddPrereq}
                              className="px-3 py-1.5 bg-primary/10 hover:bg-primary/20 text-primary rounded-lg text-xs font-label-md transition-colors"
                            >
                              Add
                            </button>
                          </div>
                        )}

                        <div className="space-y-2">
                          {prereqs.length === 0 ? (
                            <p className="text-xs text-on-surface-variant">
                              {isNew
                                ? 'Prerequisites can be configured once the course is created.'
                                : 'No prerequisites configured.'}
                            </p>
                          ) : (
                            prereqs.map((p) => (
                              <div
                                key={p.id}
                                className="p-2.5 rounded-lg bg-surface-container-low/50 border border-outline-variant/30 flex items-center justify-between text-xs"
                              >
                                <span className="font-semibold text-on-surface">
                                  {p.prerequisiteCourseCode || p.prerequisiteCourseId}
                                </span>
                                <button
                                  type="button"
                                  onClick={() => handleRemovePrereq(p.id)}
                                  className="text-on-surface-variant hover:text-error transition-colors"
                                  title="Remove prerequisite"
                                >
                                  <span className="material-symbols-outlined text-[16px]">close</span>
                                </button>
                              </div>
                            ))
                          )}
                        </div>
                      </div>
                    </div>
                  </div>
                </div>

                <div className="flex justify-end gap-3 pt-4 border-t border-outline-variant/20">
                  <Link
                    to="/admin/courses"
                    className="px-5 py-2.5 border border-outline-variant text-on-surface font-label-md text-sm rounded-lg hover:bg-surface-container transition-colors"
                  >
                    Cancel
                  </Link>
                  <button
                    type="submit"
                    disabled={isSaving}
                    className="px-7 py-2.5 bg-primary text-on-primary font-label-md text-sm font-semibold rounded-lg shadow-sm hover:bg-primary/90 transition-all flex items-center gap-2 disabled:opacity-50"
                  >
                    <span className="material-symbols-outlined text-[18px]">save</span>
                    {isSaving ? 'Saving...' : isNew ? 'Create Course' : 'Save Changes'}
                  </button>
                </div>
              </form>
            )}
          </div>
        </main>
      </div>
    </div>
  );
}

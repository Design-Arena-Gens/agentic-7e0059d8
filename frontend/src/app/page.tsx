'use client';

import { FormEvent, useCallback, useEffect, useMemo, useState } from "react";

type DepartmentSummary = {
  id: number;
  name: string;
  code: string;
  head: string | null;
  location: string | null;
  annualBudget: number | null;
  employeeCount: number;
  updatedAt: string;
};

type EmployeeSummary = {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  jobTitle: string | null;
  status: string;
  startDate: string | null;
  endDate: string | null;
};

type DepartmentDetail = {
  id: number;
  name: string;
  code: string;
  description: string | null;
  location: string | null;
  head: string | null;
  annualBudget: number | null;
  createdAt: string;
  updatedAt: string;
  employees: EmployeeSummary[];
};

type DepartmentForm = {
  name: string;
  code: string;
  description: string;
  location: string;
  head: string;
  annualBudget: string;
};

type EmployeeForm = {
  firstName: string;
  lastName: string;
  email: string;
  jobTitle: string;
  status: string;
  startDate: string;
  endDate: string;
};

const API_BASE = process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080";

export default function Home() {
  const [departments, setDepartments] = useState<DepartmentSummary[]>([]);
  const [selectedDepartment, setSelectedDepartment] = useState<DepartmentDetail | null>(null);
  const [departmentForm, setDepartmentForm] = useState<DepartmentForm>({
    name: "",
    code: "",
    description: "",
    location: "",
    head: "",
    annualBudget: "",
  });
  const [employeeForm, setEmployeeForm] = useState<EmployeeForm>({
    firstName: "",
    lastName: "",
    email: "",
    jobTitle: "",
    status: "ACTIVE",
    startDate: "",
    endDate: "",
  });
  const [isLoading, setIsLoading] = useState(false);
  const [isSubmittingDepartment, setIsSubmittingDepartment] = useState(false);
  const [isSubmittingEmployee, setIsSubmittingEmployee] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const apiUnavailable = useMemo(() => errorMessage?.includes("connect") ?? false, [errorMessage]);

  const request = useCallback(async <T,>(path: string, options?: RequestInit): Promise<T> => {
    const response = await fetch(`${API_BASE}${path}`, {
      headers: {
        "Content-Type": "application/json",
      },
      ...options,
    });
    if (!response.ok) {
      const body = await response.json().catch(() => ({}));
      const details = (body && (body.message ?? body.error)) || response.statusText;
      throw new Error(details);
    }
    return (await response.json()) as T;
  }, []);

  const handleSelectDepartment = useCallback(
    async (departmentId: number) => {
      try {
        setErrorMessage(null);
        const details = await request<DepartmentDetail>(`/api/departments/${departmentId}`);
        setSelectedDepartment(details);
      } catch (error) {
        if (error instanceof Error) {
          setErrorMessage(error.message);
        } else {
          setErrorMessage("Unable to load department");
        }
      }
    },
    [request],
  );

  const loadDepartments = useCallback(async () => {
    try {
      setIsLoading(true);
      setErrorMessage(null);
      const list = await request<DepartmentSummary[]>("/api/departments");
      setDepartments(list);
      if (list.length > 0) {
        void handleSelectDepartment(list[0].id);
      } else {
        setSelectedDepartment(null);
      }
    } catch (error) {
      if (error instanceof Error) {
        setErrorMessage(error.message);
      } else {
        setErrorMessage("Unable to load departments");
      }
    } finally {
      setIsLoading(false);
    }
  }, [handleSelectDepartment, request]);

  useEffect(() => {
    void loadDepartments();
  }, [loadDepartments]);

  async function handleCreateDepartment(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    try {
      setIsSubmittingDepartment(true);
      setErrorMessage(null);
      const payload = {
        ...departmentForm,
        annualBudget: departmentForm.annualBudget ? Number(departmentForm.annualBudget) : null,
      };
      const created = await request<DepartmentDetail>("/api/departments", {
        method: "POST",
        body: JSON.stringify(payload),
      });
      setDepartments((prev) => [...prev, mapDetailToSummary(created)].sort((a, b) => a.name.localeCompare(b.name)));
      setSelectedDepartment(created);
      setDepartmentForm({
        name: "",
        code: "",
        description: "",
        location: "",
        head: "",
        annualBudget: "",
      });
    } catch (error) {
      if (error instanceof Error) {
        setErrorMessage(error.message);
      } else {
        setErrorMessage("Unable to create department");
      }
    } finally {
      setIsSubmittingDepartment(false);
    }
  }

  async function handleAddEmployee(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!selectedDepartment) return;
    try {
      setIsSubmittingEmployee(true);
      setErrorMessage(null);
      const payload = {
        ...employeeForm,
        startDate: employeeForm.startDate || null,
        endDate: employeeForm.endDate || null,
      };
      const employee = await request<EmployeeSummary>(`/api/departments/${selectedDepartment.id}/employees`, {
        method: "POST",
        body: JSON.stringify(payload),
      });
      setSelectedDepartment({
        ...selectedDepartment,
        employees: [...selectedDepartment.employees, employee],
      });
      setDepartments((prev) =>
        prev.map((dept) =>
          dept.id === selectedDepartment.id
            ? { ...dept, employeeCount: dept.employeeCount + 1, updatedAt: new Date().toISOString() }
            : dept,
        ),
      );
      setEmployeeForm({
        firstName: "",
        lastName: "",
        email: "",
        jobTitle: "",
        status: "ACTIVE",
        startDate: "",
        endDate: "",
      });
    } catch (error) {
      if (error instanceof Error) {
        setErrorMessage(error.message);
      } else {
        setErrorMessage("Unable to add employee");
      }
    } finally {
      setIsSubmittingEmployee(false);
    }
  }

  async function handleRemoveEmployee(employeeId: number) {
    if (!selectedDepartment) return;
    try {
      setErrorMessage(null);
      const response = await fetch(`${API_BASE}/api/departments/${selectedDepartment.id}/employees/${employeeId}`, {
        method: "DELETE",
      });
      if (!response.ok) {
        const body = await response.json().catch(() => ({}));
        const details = (body && (body.message ?? body.error)) || response.statusText;
        throw new Error(details);
      }
      setSelectedDepartment({
        ...selectedDepartment,
        employees: selectedDepartment.employees.filter((employee) => employee.id !== employeeId),
      });
      setDepartments((prev) =>
        prev.map((dept) =>
          dept.id === selectedDepartment.id
            ? { ...dept, employeeCount: Math.max(dept.employeeCount - 1, 0), updatedAt: new Date().toISOString() }
            : dept,
        ),
      );
    } catch (error) {
      if (error instanceof Error) {
        setErrorMessage(error.message);
      } else {
        setErrorMessage("Unable to remove employee");
      }
    }
  }

  function mapDetailToSummary(detail: DepartmentDetail): DepartmentSummary {
    return {
      id: detail.id,
      name: detail.name,
      code: detail.code,
      head: detail.head,
      location: detail.location,
      annualBudget: detail.annualBudget,
      employeeCount: detail.employees.length,
      updatedAt: detail.updatedAt,
    };
  }

  return (
    <div className="min-h-screen bg-slate-950 px-6 py-10 text-slate-100">
      <div className="mx-auto flex w-full max-w-7xl flex-col gap-10 lg:flex-row">
        <aside className="w-full space-y-6 lg:w-80">
          <header>
            <h1 className="text-3xl font-bold tracking-tight text-white">Department Management</h1>
            <p className="mt-2 text-sm text-slate-400">
              View staffing, budgets, and leadership for each department. The console talks to a Spring Boot API. Set
              <code className="mx-1 rounded bg-slate-800 px-1 py-0.5 text-xs">NEXT_PUBLIC_API_BASE_URL</code>
              if the backend runs on another host.
            </p>
          </header>

          <section className="rounded-2xl border border-slate-800 bg-slate-900/60 p-5 shadow-lg">
            <div className="flex items-center justify-between">
              <h2 className="text-lg font-semibold text-white">Departments</h2>
              <button
                className="text-xs font-medium uppercase tracking-wide text-indigo-400"
                onClick={() => void loadDepartments()}
              >
                Refresh
              </button>
            </div>
            <div className="mt-4 space-y-2">
              {isLoading && <p className="text-sm text-slate-400">Loading departments…</p>}
              {!isLoading && departments.length === 0 && (
                <p className="text-sm text-slate-500">No departments yet. Create the first one.</p>
              )}
              {departments.map((department) => (
                <button
                  key={department.id}
                  onClick={() => void handleSelectDepartment(department.id)}
                  className={`w-full rounded-xl border px-4 py-3 text-left transition ${
                    selectedDepartment?.id === department.id
                      ? "border-indigo-500/70 bg-indigo-500/10 text-white"
                      : "border-transparent bg-slate-800/50 hover:border-slate-700 hover:bg-slate-800"
                  }`}
                >
                  <p className="font-medium text-white">
                    {department.name}{" "}
                    <span className="ml-2 rounded-full bg-slate-800 px-2 py-0.5 text-xs text-slate-300">
                      {department.code}
                    </span>
                  </p>
                  <p className="mt-1 text-xs uppercase tracking-wide text-slate-400">{department.location ?? "N/A"}</p>
                  <p className="mt-2 text-xs text-slate-400">
                    {department.employeeCount} employee{department.employeeCount === 1 ? "" : "s"}
                  </p>
                </button>
              ))}
            </div>
          </section>

          <section className="rounded-2xl border border-slate-800 bg-slate-900/60 p-5 shadow-lg">
            <h2 className="text-lg font-semibold text-white">Create Department</h2>
            <form className="mt-4 space-y-4" onSubmit={handleCreateDepartment}>
              <div>
                <label className="text-xs font-medium uppercase tracking-wide text-slate-400">Name</label>
                <input
                  required
                  value={departmentForm.name}
                  onChange={(event) => setDepartmentForm((prev) => ({ ...prev, name: event.target.value }))}
                  className="mt-1 w-full rounded-lg border border-slate-700 bg-slate-950 px-3 py-2 text-sm text-white focus:border-indigo-500 focus:outline-none"
                  placeholder="Product Engineering"
                />
              </div>
              <div>
                <label className="text-xs font-medium uppercase tracking-wide text-slate-400">Code</label>
                <input
                  required
                  value={departmentForm.code}
                  onChange={(event) => setDepartmentForm((prev) => ({ ...prev, code: event.target.value }))}
                  className="mt-1 w-full rounded-lg border border-slate-700 bg-slate-950 px-3 py-2 text-sm uppercase text-white focus:border-indigo-500 focus:outline-none"
                  placeholder="ENG"
                />
              </div>
              <div>
                <label className="text-xs font-medium uppercase tracking-wide text-slate-400">Location</label>
                <input
                  value={departmentForm.location}
                  onChange={(event) => setDepartmentForm((prev) => ({ ...prev, location: event.target.value }))}
                  className="mt-1 w-full rounded-lg border border-slate-700 bg-slate-950 px-3 py-2 text-sm text-white focus:border-indigo-500 focus:outline-none"
                  placeholder="New York"
                />
              </div>
              <div>
                <label className="text-xs font-medium uppercase tracking-wide text-slate-400">Head of Department</label>
                <input
                  value={departmentForm.head}
                  onChange={(event) => setDepartmentForm((prev) => ({ ...prev, head: event.target.value }))}
                  className="mt-1 w-full rounded-lg border border-slate-700 bg-slate-950 px-3 py-2 text-sm text-white focus:border-indigo-500 focus:outline-none"
                  placeholder="Ada Lovelace"
                />
              </div>
              <div>
                <label className="text-xs font-medium uppercase tracking-wide text-slate-400">Annual Budget</label>
                <input
                  type="number"
                  min={0}
                  step={1000}
                  value={departmentForm.annualBudget}
                  onChange={(event) => setDepartmentForm((prev) => ({ ...prev, annualBudget: event.target.value }))}
                  className="mt-1 w-full rounded-lg border border-slate-700 bg-slate-950 px-3 py-2 text-sm text-white focus:border-indigo-500 focus:outline-none"
                  placeholder="2500000"
                />
              </div>
              <div>
                <label className="text-xs font-medium uppercase tracking-wide text-slate-400">Description</label>
                <textarea
                  value={departmentForm.description}
                  onChange={(event) => setDepartmentForm((prev) => ({ ...prev, description: event.target.value }))}
                  className="mt-1 w-full rounded-lg border border-slate-700 bg-slate-950 px-3 py-2 text-sm text-white focus:border-indigo-500 focus:outline-none"
                  placeholder="Responsible for delivering product features and platform capabilities."
                  rows={3}
                />
              </div>
              <button
                type="submit"
                disabled={isSubmittingDepartment}
                className="w-full rounded-lg bg-indigo-500 px-4 py-2 text-sm font-semibold text-white transition hover:bg-indigo-400 disabled:cursor-not-allowed disabled:opacity-60"
              >
                {isSubmittingDepartment ? "Creating…" : "Create Department"}
              </button>
            </form>
          </section>
        </aside>

        <main className="flex-1 space-y-6">
          {errorMessage && (
            <div className="rounded-xl border border-rose-500/50 bg-rose-950/60 px-4 py-3 text-sm text-rose-200">
              {errorMessage}
              {apiUnavailable && (
                <p className="mt-2 text-xs text-rose-300">
                  Ensure the Spring Boot API is running locally on port 8080 or configure
                  <code className="mx-1 rounded bg-slate-800 px-1 py-0.5 text-[10px] uppercase tracking-wide">
                    NEXT_PUBLIC_API_BASE_URL
                  </code>
                  for production.
                </p>
              )}
            </div>
          )}

          {selectedDepartment ? (
            <section className="space-y-6 rounded-3xl border border-slate-800 bg-slate-900/80 p-8 shadow-2xl">
              <div className="flex flex-col gap-4 border-b border-slate-800 pb-6 md:flex-row md:items-center md:justify-between">
                <div>
                  <div className="flex items-center gap-3">
                    <h2 className="text-2xl font-semibold text-white">{selectedDepartment.name}</h2>
                    <span className="rounded-full bg-indigo-500/20 px-3 py-1 text-xs font-semibold uppercase text-indigo-200">
                      {selectedDepartment.code}
                    </span>
                  </div>
                  <p className="mt-2 text-sm text-slate-400">{selectedDepartment.description ?? "No description"}</p>
                </div>
                <div className="grid grid-cols-2 gap-4 text-sm text-slate-300 md:w-auto">
                  <div>
                    <p className="text-xs uppercase tracking-wide text-slate-500">Head</p>
                    <p>{selectedDepartment.head ?? "Unassigned"}</p>
                  </div>
                  <div>
                    <p className="text-xs uppercase tracking-wide text-slate-500">Location</p>
                    <p>{selectedDepartment.location ?? "Unknown"}</p>
                  </div>
                  <div>
                    <p className="text-xs uppercase tracking-wide text-slate-500">Budget</p>
                    <p>
                      {selectedDepartment.annualBudget
                        ? `$${selectedDepartment.annualBudget.toLocaleString()}`
                        : "Not set"}
                    </p>
                  </div>
                  <div>
                    <p className="text-xs uppercase tracking-wide text-slate-500">Employees</p>
                    <p>{selectedDepartment.employees.length}</p>
                  </div>
                </div>
              </div>

              <div className="grid gap-6 lg:grid-cols-[2fr_1fr]">
                <div>
                  <h3 className="text-lg font-semibold text-white">People</h3>
                  {selectedDepartment.employees.length === 0 ? (
                    <p className="mt-4 rounded-lg border border-dashed border-slate-700 bg-slate-900/40 px-4 py-8 text-center text-sm text-slate-400">
                      No employees assigned yet. Add the first team member using the form.
                    </p>
                  ) : (
                    <ul className="mt-4 space-y-3">
                      {selectedDepartment.employees.map((employee) => (
                        <li
                          key={employee.id}
                          className="flex items-start justify-between rounded-xl border border-slate-800 bg-slate-900/60 p-4"
                        >
                          <div>
                            <p className="text-sm font-semibold text-white">
                              {employee.firstName} {employee.lastName}
                            </p>
                            <p className="text-xs uppercase tracking-wide text-slate-400">
                              {employee.jobTitle ?? "Team Member"}
                            </p>
                            <p className="mt-1 text-xs text-slate-400">{employee.email}</p>
                            <div className="mt-2 flex flex-wrap items-center gap-2 text-[10px] uppercase tracking-wide text-slate-400">
                              <span className="rounded bg-slate-800 px-2 py-1 text-indigo-200">
                                {employee.status.replace("_", " ")}
                              </span>
                              {employee.startDate && (
                                <span className="rounded bg-slate-800 px-2 py-1">
                                  Start: {new Date(employee.startDate).toLocaleDateString()}
                                </span>
                              )}
                              {employee.endDate && (
                                <span className="rounded bg-slate-800 px-2 py-1">
                                  End: {new Date(employee.endDate).toLocaleDateString()}
                                </span>
                              )}
                            </div>
                          </div>
                          <button
                            onClick={() => void handleRemoveEmployee(employee.id)}
                            className="rounded-full border border-slate-700 bg-slate-950/60 px-3 py-1 text-xs font-semibold text-rose-200 transition hover:border-rose-500 hover:text-rose-100"
                          >
                            Remove
                          </button>
                        </li>
                      ))}
                    </ul>
                  )}
                </div>

                <div className="rounded-2xl border border-slate-800 bg-slate-900/60 p-5">
                  <h3 className="text-lg font-semibold text-white">Add Employee</h3>
                  <form className="mt-4 space-y-4" onSubmit={handleAddEmployee}>
                    <div>
                      <label className="text-xs font-medium uppercase tracking-wide text-slate-400">First Name</label>
                      <input
                        required
                        value={employeeForm.firstName}
                        onChange={(event) => setEmployeeForm((prev) => ({ ...prev, firstName: event.target.value }))}
                        className="mt-1 w-full rounded-lg border border-slate-700 bg-slate-950 px-3 py-2 text-sm text-white focus:border-indigo-500 focus:outline-none"
                      />
                    </div>
                    <div>
                      <label className="text-xs font-medium uppercase tracking-wide text-slate-400">Last Name</label>
                      <input
                        required
                        value={employeeForm.lastName}
                        onChange={(event) => setEmployeeForm((prev) => ({ ...prev, lastName: event.target.value }))}
                        className="mt-1 w-full rounded-lg border border-slate-700 bg-slate-950 px-3 py-2 text-sm text-white focus:border-indigo-500 focus:outline-none"
                      />
                    </div>
                    <div>
                      <label className="text-xs font-medium uppercase tracking-wide text-slate-400">Email</label>
                      <input
                        required
                        type="email"
                        value={employeeForm.email}
                        onChange={(event) => setEmployeeForm((prev) => ({ ...prev, email: event.target.value }))}
                        className="mt-1 w-full rounded-lg border border-slate-700 bg-slate-950 px-3 py-2 text-sm text-white focus:border-indigo-500 focus:outline-none"
                      />
                    </div>
                    <div>
                      <label className="text-xs font-medium uppercase tracking-wide text-slate-400">Job Title</label>
                      <input
                        value={employeeForm.jobTitle}
                        onChange={(event) => setEmployeeForm((prev) => ({ ...prev, jobTitle: event.target.value }))}
                        className="mt-1 w-full rounded-lg border border-slate-700 bg-slate-950 px-3 py-2 text-sm text-white focus:border-indigo-500 focus:outline-none"
                      />
                    </div>
                    <div>
                      <label className="text-xs font-medium uppercase tracking-wide text-slate-400">Status</label>
                      <select
                        value={employeeForm.status}
                        onChange={(event) => setEmployeeForm((prev) => ({ ...prev, status: event.target.value }))}
                        className="mt-1 w-full rounded-lg border border-slate-700 bg-slate-950 px-3 py-2 text-sm text-white focus:border-indigo-500 focus:outline-none"
                      >
                        <option value="ACTIVE">Active</option>
                        <option value="ON_LEAVE">On Leave</option>
                        <option value="INACTIVE">Inactive</option>
                      </select>
                    </div>
                    <div className="grid grid-cols-2 gap-4">
                      <div>
                        <label className="text-xs font-medium uppercase tracking-wide text-slate-400">
                          Start Date
                        </label>
                        <input
                          type="date"
                          value={employeeForm.startDate}
                          onChange={(event) => setEmployeeForm((prev) => ({ ...prev, startDate: event.target.value }))}
                          className="mt-1 w-full rounded-lg border border-slate-700 bg-slate-950 px-3 py-2 text-sm text-white focus:border-indigo-500 focus:outline-none"
                        />
                      </div>
                      <div>
                        <label className="text-xs font-medium uppercase tracking-wide text-slate-400">End Date</label>
                        <input
                          type="date"
                          value={employeeForm.endDate}
                          onChange={(event) => setEmployeeForm((prev) => ({ ...prev, endDate: event.target.value }))}
                          className="mt-1 w-full rounded-lg border border-slate-700 bg-slate-950 px-3 py-2 text-sm text-white focus:border-indigo-500 focus:outline-none"
                        />
                      </div>
                    </div>
                    <button
                      type="submit"
                      disabled={isSubmittingEmployee}
                      className="w-full rounded-lg bg-emerald-500 px-4 py-2 text-sm font-semibold text-white transition hover:bg-emerald-400 disabled:cursor-not-allowed disabled:opacity-60"
                    >
                      {isSubmittingEmployee ? "Adding…" : "Add Employee"}
                    </button>
                  </form>
                </div>
              </div>
            </section>
          ) : (
            <section className="rounded-3xl border border-slate-800 bg-slate-900/80 p-12 text-center text-slate-300">
              <h2 className="text-2xl font-semibold text-white">No Department Selected</h2>
              <p className="mt-4 text-sm text-slate-400">
                Select an existing department or create a new one to begin tracking headcount, hiring velocity, and
                budget utilization.
              </p>
            </section>
          )}
        </main>
      </div>
    </div>
  );
}

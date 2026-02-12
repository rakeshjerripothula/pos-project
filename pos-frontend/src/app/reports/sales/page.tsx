"use client";

import { useState, useEffect, useRef } from "react";
import { apiGet, apiPost, apiExport } from "@/lib/api";
import {
  SalesReportPageData,
  ClientData,
} from "@/lib/types";
import Link from "next/link";
import AuthGuard from "@/components/AuthGuard";
import Select from "react-select";
import toast from "react-hot-toast";

interface ClientOption {
  value: number;
  label: string;
}

export default function SalesReportPage() {
  const [data, setData] = useState<SalesReportPageData | null>(null);
  const [loading, setLoading] = useState(false);
  const [startDate, setStartDate] = useState("");
  const [endDate, setEndDate] = useState("");
  const [clientId, setClientId] = useState<number | "">("");
  const [clients, setClients] = useState<ClientData[]>([]);
  const clientsLoadedRef = useRef(false);
  const [page, setPage] = useState(0);
  const [mounted, setMounted] = useState(false);
  const initialLoadRef = useRef(false);
  const pageSize = 10;

  useEffect(() => {
    const end = new Date();
    const start = new Date();
    start.setDate(start.getDate() - 30);

    setStartDate(start.toISOString().split("T")[0]);
    setEndDate(end.toISOString().split("T")[0]);
  }, []);

  useEffect(() => {
    loadClients();
  }, []);

  useEffect(() => {
    setMounted(true);
  }, []);

  useEffect(() => {
    if (!initialLoadRef.current && startDate && endDate) {
      initialLoadRef.current = true;
      loadReport();
    }
  }, [startDate, endDate]);

  const clientOptions: ClientOption[] = clients
    .filter((c) => c.enabled)
    .map((c) => ({ value: c.id, label: c.clientName }));

  const selectedClient = clientOptions.find((o) => o.value === clientId) || null;

  async function loadClients() {
    if (clientsLoadedRef.current) return;
    clientsLoadedRef.current = true;
    try {
      const clientsData = await apiGet<ClientData[]>("/clients");
      setClients(clientsData);
    } catch (e: any) {
      console.error("Failed to load clients: " + e.message);
      toast.error("Failed to load clients: " + e.message);
    }
  }

  async function loadReport() {
    if (!startDate || !endDate) {
      return;
    }

    setLoading(true);
    try {
      const start = new Date(startDate);
      start.setHours(0, 0, 0, 0);
      const end = new Date(endDate);
      end.setHours(23, 59, 59, 999);

      const form: any = {
        startDate: start.toISOString(),
        endDate: end.toISOString(),
        page,
        pageSize,
      };

      if (clientId) {
        form.clientId = clientId;
      }

      const result = await apiPost<SalesReportPageData>(
        `/reports/sales`,
        form
      );

      setData(result);
    } catch (error: any) {
      toast.error("Failed to load report: " + error.message);
    } finally {
      setLoading(false);
    }
  }

  const totalRevenue = data?.rows.reduce(
    (sum, item) => sum + (item.revenue || 0),
    0
  ) || 0;
  const totalQuantity = data?.rows.reduce(
    (sum, item) => sum + (item.quantitySold || 0),
    0
  ) || 0;

  async function handleExportCsv() {
    if (!startDate || !endDate) {
      return;
    }

    const start = new Date(startDate);
    start.setHours(0, 0, 0, 0);
    const end = new Date(endDate);
    end.setHours(23, 59, 59, 999);

    const form: any = {
      startDate: start.toISOString(),
      endDate: end.toISOString(),
      page: 0,
      pageSize: 10000,
    };

    if (clientId) {
      form.clientId = clientId;
    }

    try {
      await apiExport("/reports/sales/export", form);
      toast.success("CSV downloaded successfully");
    } catch (error: any) {
      toast.error("Failed to export CSV: " + error.message);
    }
  }

  return (
    <AuthGuard requiredRole="SUPERVISOR">
      <div className="min-h-[calc(100vh-64px)] bg-slate-50 px-4 sm:px-30 p-3 sm:p-6">
        <div className="max-w-[1400px] mx-auto">
          <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between mb-4 sm:mb-6 gap-3">
            <h1 className="text-2xl sm:text-3xl font-bold text-slate-800">
              Sales Report
            </h1>
            <Link
              href="/reports"
              className="px-4 py-2 text-sm font-medium text-white bg-gray-500 rounded-lg hover:bg-gray-600 transition-colors no-underline"
            >
              Back to Reports
            </Link>
          </div>

          {/* Filters */}
          <div className="p-4 sm:p-5 mb-4 sm:mb-6 bg-white rounded-xl shadow-sm">
            <div className="flex flex-wrap gap-3">
              <div className="flex-1 min-w-[160px] max-w-[200px]">
                <label className="block mb-2 text-sm font-medium text-gray-700">
                  Start Date
                </label>
                <input
                  type="date"
                  value={startDate}
                  onChange={(e) => setStartDate(e.target.value)}
                  className="w-full px-3.5 py-2.5 text-sm border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent h-[42px]"
                />
              </div>

              <div className="flex-1 min-w-[160px] max-w-[200px]">
                <label className="block mb-2 text-sm font-medium text-gray-700">
                  End Date
                </label>
                <input
                  type="date"
                  value={endDate}
                  onChange={(e) => setEndDate(e.target.value)}
                  className="w-full px-3.5 py-2.5 text-sm border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent h-[42px]"
                />
              </div>

              <div className="flex-1 min-w-[160px] max-w-[220px]">
                <label className="block mb-2 text-sm font-medium text-gray-700">
                  Client
                </label>
                {mounted ? (
                  <Select
                    options={clientOptions}
                    value={selectedClient}
                    isClearable
                    placeholder="Select client..."
                    onChange={(option) =>
                      setClientId(option ? option.value : "")
                    }
                    menuPortalTarget={typeof document !== 'undefined' ? document.body : undefined}
                    className="w-full"
                    classNamePrefix="react-select"
                    styles={{
                      control: (base) => ({
                        ...base,
                        borderRadius: "0.5rem",
                        border: "1px solid #d1d5db",
                        fontSize: "14px",
                        minHeight: "42px",
                        backgroundColor: "white",
                      }),
                      placeholder: (base) => ({
                        ...base,
                        fontSize: "16px",
                      }),
                      menuPortal: (base: any) => ({ ...base, zIndex: 9999 }),
                      menu: (base) => ({
                        ...base,
                        borderRadius: "0.5rem",
                        boxShadow: "0 1px 3px rgba(0,0,0,0.1)",
                      }),
                      option: (base, state) => ({
                        ...base,
                        fontSize: "14px",
                        backgroundColor: state.isSelected 
                          ? "#667eea" 
                          : state.isFocused 
                            ? "#f3f4f6" 
                            : "white",
                        color: state.isSelected ? "white" : "#374151",
                        padding: "10px 14px",
                      }),
                    }}
                  />
                ) : (
                  <div className="px-3.5 py-2.5 text-sm text-slate-500 bg-gray-50 border border-gray-300 rounded-lg h-[42px] flex items-center">
                    Loading...
                  </div>
                )}
              </div>

              <div className="flex gap-2 items-end">
                <button
                  onClick={() => {
                    setPage(0);
                    loadReport();
                  }}
                  disabled={loading}
                  className={`px-5 py-2.5 text-sm font-medium text-white rounded-lg transition-colors h-[42px] ${
                    loading 
                      ? "bg-blue-400 cursor-not-allowed" 
                      : "bg-blue-500 hover:bg-blue-600 cursor-pointer"
                  }`}
                >
                  {loading ? "Loading..." : "Generate Report"}
                </button>
                <button
                  onClick={handleExportCsv}
                  className="px-4 py-2.5 text-sm font-medium text-white bg-green-500 rounded-lg hover:bg-green-600 transition-colors cursor-pointer flex items-center gap-2 h-[42px]"
                >
                  <svg xmlns="http://www.w3.org/2000/svg" className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4" />
                  </svg>
                  Export
                </button>
              </div>
            </div>
          </div>

          {/* Summary */}
          {data && data.rows.length > 0 && (
            <div className="mb-4 sm:mb-6 p-4 sm:p-5 bg-white rounded-xl shadow-sm">
              <div className="flex flex-wrap justify-between gap-4">
                <div>
                  <div className="text-sm text-slate-500">Total Products</div>
                  <div className="text-xl sm:text-2xl font-bold text-slate-800">{data.totalElements}</div>
                </div>
                <div>
                  <div className="text-sm text-slate-500">Total Quantity Sold</div>
                  <div className="text-xl sm:text-2xl font-bold text-slate-800">{totalQuantity}</div>
                </div>
                <div className="text-right">
                  <div className="text-sm text-slate-500">Total Revenue</div>
                  <div className="text-xl sm:text-2xl font-bold text-slate-800">₹{totalRevenue.toFixed(2)}</div>
                </div>
              </div>
            </div>
          )}


          {/* Report Table */}
          {loading ? (
            <div className="py-12 text-center text-slate-500">Loading...</div>
          ) : data && data.rows.length === 0 ? (
            <div className="p-12 text-center text-slate-500 bg-white rounded-xl shadow-sm">
              No sales data available for the selected date range.
            </div>
          ) : data ? (
            <>
              <div className="p-4 sm:p-6 bg-white rounded-xl shadow-sm overflow-x-auto">
                <table className="w-full border-collapse min-w-[400px]">
                  <thead>
                    <tr className="border-b-2 border-gray-200">
                      <th className="px-3 py-3 text-sm sm:text-base font-semibold text-left text-gray-700">Product Name</th>
                      <th className="px-3 py-3 text-sm sm:text-base font-semibold text-left text-gray-700">Quantity Sold</th>
                      <th className="px-3 py-3 text-sm sm:text-base font-semibold text-left text-gray-700">Revenue</th>
                    </tr>
                  </thead>
                  <tbody>
                    {data.rows.map((item, idx) => (
                      <tr key={idx} className="border-b border-gray-100">
                        <td className="px-3 py-3 text-sm sm:text-base">{item.productName}</td>
                        <td className="px-3 py-3 text-sm sm:text-base">{item.quantitySold}</td>
                        <td className="px-3 py-3 text-sm sm:text-base">₹{Number(item.revenue).toFixed(2)}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>

              {/* Pagination */}
              {data.totalElements > pageSize && (
                <div className="flex flex-wrap items-center justify-between mt-4 sm:mt-6 p-4 sm:p-6 bg-white rounded-xl shadow-sm gap-y-3">
                  <div className="text-sm text-slate-500 order-2 sm:order-1">
                    Showing {data.rows.length} of {data.totalElements} products
                  </div>
                  <div className="flex flex-wrap gap-2 order-1 sm:order-2">
                    <button
                      onClick={() => setPage(Math.max(0, page - 1))}
                      disabled={page === 0}
                      className={`px-3 py-2 text-sm border border-gray-300 rounded-lg ${
                        page === 0 
                          ? "bg-white text-gray-400 cursor-not-allowed opacity-50" 
                          : "bg-white text-gray-700 hover:bg-gray-50 cursor-pointer"
                      }`}
                    >
                      Previous
                    </button>
                    <span className="px-3 py-2 text-sm text-gray-700">
                      {page + 1} / {Math.ceil(data.totalElements / pageSize)}
                    </span>
                    <button
                      onClick={() => setPage(page + 1)}
                      disabled={(page + 1) * pageSize >= data.totalElements}
                      className={`px-3 py-2 text-sm border border-gray-300 rounded-lg ${
                        (page + 1) * pageSize >= data.totalElements 
                          ? "bg-white text-gray-400 cursor-not-allowed opacity-50" 
                          : "bg-white text-gray-700 hover:bg-gray-50 cursor-pointer"
                      }`}
                    >
                      Next
                    </button>
                  </div>
                </div>
              )}
            </>
          ) : null}
        </div>
      </div>
    </AuthGuard>
  );
}


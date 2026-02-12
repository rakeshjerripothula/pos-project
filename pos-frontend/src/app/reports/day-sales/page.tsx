"use client";

import { useState, useEffect, useRef } from "react";
import { apiPost, apiExport } from "@/lib/api";
import { DaySalesPageData } from "@/lib/types";
import Link from "next/link";
import AuthGuard from "@/components/AuthGuard";
import toast from "react-hot-toast";

interface DaySalesForm {
  startDate: string;
  endDate: string;
  page?: number;
  pageSize?: number;
}

export default function DaySalesReportPage() {
  const [data, setData] = useState<DaySalesPageData | null>(null);
  const [loading, setLoading] = useState(false);
  const [startDate, setStartDate] = useState("");
  const [endDate, setEndDate] = useState("");
  const [page, setPage] = useState(0);
  const initialLoadRef = useRef(false);
  const [mounted, setMounted] = useState(false);
  const pageSize = 10;

  useEffect(() => {
    // Set default date range to last 30 days
    const end = new Date();
    const start = new Date();
    start.setDate(start.getDate() - 30);

    setStartDate(start.toISOString().split("T")[0]);
    setEndDate(end.toISOString().split("T")[0]);
  }, []);

  useEffect(() => {
    setMounted(true);
  }, []);

  // Initial load of report - load once when dates are set
  useEffect(() => {
    if (!initialLoadRef.current && startDate && endDate) {
      initialLoadRef.current = true;
      loadReport();
    }
  }, [startDate, endDate]);

  async function loadReport() {
    if (!startDate || !endDate) {
      return;
    }

    setLoading(true);
    try {
      const start = new Date(startDate);
      const end = new Date(endDate);

      const form: DaySalesForm = {
        startDate: start.toISOString().split("T")[0],
        endDate: end.toISOString().split("T")[0],
        page,
        pageSize,
      };

      const result = await apiPost<DaySalesPageData>(
        `/reports/day-sales`,
        form
      );

      setData(result);
    } catch (error: any) {
      toast.error("Failed to load report: " + error.message);
    } finally {
      setLoading(false);
    }
  }

  const totalRevenue = data?.content.reduce(
    (sum, item) => sum + (item.totalRevenue || 0),
    0
  ) || 0;
  const totalOrders = data?.content.reduce(
    (sum, item) => sum + (item.invoicedOrdersCount || 0),
    0
  ) || 0;
  const totalItems = data?.content.reduce(
    (sum, item) => sum + (item.invoicedItemsCount || 0),
    0
  ) || 0;
  const avgDailyRevenue =
    data && data.content.length > 0 ? totalRevenue / data.content.length : 0;

  async function handleExportCsv() {
    if (!startDate || !endDate) {
      return;
    }

    const start = new Date(startDate);
    const end = new Date(endDate);

    const form = {
      startDate: start.toISOString().split("T")[0],
      endDate: end.toISOString().split("T")[0],
      page: 0,
      pageSize: 10000,
    };

    try {
      await apiExport("/reports/day-sales/export", form);
      toast.success("CSV downloaded successfully");
    } catch (error: any) {
      toast.error("Failed to export CSV: " + error.message);
    }
  }

  return (
    <AuthGuard requiredRole="SUPERVISOR">
      <div className="min-h-[calc(100vh-64px)] bg-slate-50 px-4 sm:px-6 p-3 sm:p-6">
        <div className="max-w-[1400px] mx-auto">
          <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between mb-4 sm:mb-6 gap-3">
            <h1 className="text-2xl sm:text-3xl font-bold text-slate-800">
              Day-on-Day Sales Report
            </h1>
            <Link
              href="/reports"
              className="px-5 py-2.5 text-sm font-medium text-white bg-gray-500 rounded-lg hover:bg-gray-600 transition-colors no-underline"
            >
              Back to Reports
            </Link>
          </div>

          {/* Filters */}
          <div className="p-5 mb-6 bg-white rounded-xl shadow-sm">
            <div className="grid grid-cols-1 gap-4 sm:grid-cols-[200px_200px_auto]">
              <div>
                <label className="block mb-2 text-sm font-medium text-gray-700">
                  Start Date
                </label>
                <input
                  type="date"
                  value={startDate}
                  onChange={(e) => {
                    setStartDate(e.target.value);
                    setPage(0);
                  }}
                  className="w-full px-3.5 py-2.5 text-sm border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                />
              </div>

              <div>
                <label className="block mb-2 text-sm font-medium text-gray-700">
                  End Date
                </label>
                <input
                  type="date"
                  value={endDate}
                  onChange={(e) => {
                    setEndDate(e.target.value);
                    setPage(0);
                  }}
                  className="w-full px-3.5 py-2.5 text-sm border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                />
              </div>

              <div className="flex items-end">
                <button
                  onClick={() => {
                    setPage(0);
                    loadReport();
                  }}
                  disabled={loading}
                  className={`px-5 py-2.5 text-sm font-medium text-white rounded-lg transition-colors ${
                    loading 
                      ? "bg-blue-400 cursor-not-allowed" 
                      : "bg-blue-500 hover:bg-blue-600 cursor-pointer"
                  }`}
                >
                  {loading ? "Loading..." : "Generate Report"}
                </button>
              </div>
            </div>
          </div>

          {/* Summary */}
          {data && data.content.length > 0 && (
            <div className="grid grid-cols-2 gap-3 mb-4 sm:mb-6 md:grid-cols-6 p-4 sm:p-5 bg-white rounded-xl shadow-sm">
              <div>
                <div className="text-xs text-slate-500">Total Days</div>
                <div className="text-xl sm:text-2xl font-bold text-slate-800">{data.totalElements}</div>
              </div>
              <div>
                <div className="text-xs text-slate-500">Total Orders</div>
                <div className="text-xl sm:text-2xl font-bold text-slate-800">{totalOrders}</div>
              </div>
              <div>
                <div className="text-xs text-slate-500">Total Items Sold</div>
                <div className="text-xl sm:text-2xl font-bold text-slate-800">{totalItems}</div>
              </div>
              <div>
                <div className="text-xs text-slate-500">Total Revenue</div>
                <div className="text-xl sm:text-2xl font-bold text-slate-800">₹{totalRevenue.toFixed(2)}</div>
              </div>
              <div>
                <div className="text-xs text-slate-500">Avg Daily Revenue</div>
                <div className="text-xl sm:text-2xl font-bold text-slate-800">₹{avgDailyRevenue.toFixed(2)}</div>
              </div>
              <div className="flex items-center justify-end">
                <button
                  onClick={handleExportCsv}
                  className="px-3 py-2 text-xs sm:text-sm font-medium text-white bg-green-500 rounded-lg hover:bg-green-600 transition-colors cursor-pointer flex items-center gap-1.5"
                >
                  <svg xmlns="http://www.w3.org/2000/svg" className="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4" />
                  </svg>
                  Export CSV
                </button>
              </div>
            </div>
          )}


          {/* Report Table */}
          {loading ? (
            <div className="py-12 text-center text-slate-500">Loading...</div>
          ) : data && data.content.length === 0 ? (
            <div className="p-12 text-center text-slate-500 bg-white rounded-xl shadow-sm">
              No data available. Please select date range and generate report.
            </div>
          ) : data ? (
            <>
              <div className="p-4 sm:p-6 bg-white rounded-xl shadow-sm overflow-x-auto">
                <table className="w-full border-collapse min-w-[450px]">
                  <thead>
                    <tr className="border-b-2 border-gray-200">
                      <th className="px-2 py-2 sm:px-3 py-2.5 text-xs sm:text-sm font-semibold text-left text-gray-700">Date</th>
                      <th className="px-2 py-2 sm:px-3 py-2.5 text-xs sm:text-sm font-semibold text-left text-gray-700">Orders</th>
                      <th className="px-2 py-2 sm:px-3 py-2.5 text-xs sm:text-sm font-semibold text-left text-gray-700">Items Sold</th>
                      <th className="px-2 py-2 sm:px-3 py-2.5 text-xs sm:text-sm font-semibold text-left text-gray-700">Revenue</th>
                      <th className="px-2 py-2 sm:px-3 py-2.5 text-xs sm:text-sm font-semibold text-left text-gray-700">Avg Order Value</th>
                    </tr>
                  </thead>
                  <tbody>
                    {data.content.map((item, idx) => {
                      const avgOrderValue =
                        item.invoicedOrdersCount > 0
                          ? item.totalRevenue / item.invoicedOrdersCount
                          : 0;
                      return (
                        <tr key={idx} className="border-b border-gray-100">
                          <td className="px-2 py-2 sm:px-3 py-2.5 text-xs sm:text-sm">
                            {mounted
                              ? new Date(item.date).toLocaleDateString("en-IN", {
                                  year: "numeric",
                                  month: "long",
                                  day: "numeric",
                                })
                              : ""}
                          </td>
                          <td className="px-2 py-2 sm:px-3 py-2.5 text-xs sm:text-sm">{item.invoicedOrdersCount}</td>
                          <td className="px-2 py-2 sm:px-3 py-2.5 text-xs sm:text-sm">{item.invoicedItemsCount}</td>
                          <td className="px-2 py-2 sm:px-3 py-2.5 text-xs sm:text-sm">₹{Number(item.totalRevenue).toFixed(2)}</td>
                          <td className="px-2 py-2 sm:px-3 py-2.5 text-xs sm:text-sm">₹{avgOrderValue.toFixed(2)}</td>
                        </tr>
                      );
                    })}
                  </tbody>
                </table>
              </div>

              {/* Pagination */}
              {data.totalElements > pageSize && (
                <div className="flex flex-wrap items-center justify-between mt-4 sm:mt-6 p-4 sm:p-6 bg-white rounded-xl shadow-sm gap-y-3">
                  <div className="text-xs sm:text-sm text-slate-500 order-2 sm:order-1">
                    Showing {data.content.length} of {data.totalElements} days
                  </div>
                  <div className="flex flex-wrap gap-1.5 sm:gap-2 order-1 sm:order-2">
                    <button
                      onClick={() => setPage(Math.max(0, page - 1))}
                      disabled={page === 0}
                      className={`px-2.5 py-1.5 text-xs sm:text-sm border border-gray-300 rounded-lg ${
                        page === 0 
                          ? "bg-white text-gray-400 cursor-not-allowed opacity-50" 
                          : "bg-white text-gray-700 hover:bg-gray-50 cursor-pointer"
                      }`}
                    >
                      Previous
                    </button>
                    <span className="px-2 py-1.5 text-xs sm:text-sm text-gray-700">
                      {page + 1} / {Math.ceil(data.totalElements / pageSize)}
                    </span>
                    <button
                      onClick={() => setPage(page + 1)}
                      disabled={(page + 1) * pageSize >= data.totalElements}
                      className={`px-2.5 py-1.5 text-xs sm:text-sm border border-gray-300 rounded-lg ${
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


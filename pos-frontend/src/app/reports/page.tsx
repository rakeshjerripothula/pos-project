"use client";

import { useState, useEffect, useRef } from "react";
import { apiGet, apiPost, apiExport } from "@/lib/api";
import { SalesReportPageData, DaySalesPageData, ClientData } from "@/lib/types";
import AuthGuard from "@/components/AuthGuard";
import Select from "react-select";
import toast from "react-hot-toast";

type ReportType = "sales" | "daySales";

interface ClientOption {
  value: number;
  label: string;
}

interface DaySalesForm {
  startDate: string;
  endDate: string;
  page?: number;
  pageSize?: number;
}

export default function ReportsPage() {
  const [activeReport, setActiveReport] = useState<ReportType>("sales");

  // Sales report state
  const [salesData, setSalesData] = useState<SalesReportPageData | null>(null);
  const [salesLoading, setSalesLoading] = useState(false);
  const [salesStartDate, setSalesStartDate] = useState("");
  const [salesEndDate, setSalesEndDate] = useState("");
  const [salesClientId, setSalesClientId] = useState<number | "">("");
  const [clients, setClients] = useState<ClientData[]>([]);
  const clientsLoadedRef = useRef(false);
  const [salesPage, setSalesPage] = useState(0);
  const [salesMounted, setSalesMounted] = useState(false);
  const salesPageSize = 10;

  // Day sales report state
  const [daySalesData, setDaySalesData] = useState<DaySalesPageData | null>(null);
  const [daySalesLoading, setDaySalesLoading] = useState(false);
  const [daySalesStartDate, setDaySalesStartDate] = useState("");
  const [daySalesEndDate, setDaySalesEndDate] = useState("");
  const [daySalesPage, setDaySalesPage] = useState(0);
  const initialLoadRef = useRef(false);
  const [daySalesMounted, setDaySalesMounted] = useState(false);
  const daySalesPageSize = 10;

  useEffect(() => {
    // Set default date range to last 30 days for both
    const end = new Date();
    const start = new Date();
    start.setDate(start.getDate() - 30);

    const startDateStr = start.toISOString().split("T")[0];
    const endDateStr = end.toISOString().split("T")[0];

    setSalesStartDate(startDateStr);
    setSalesEndDate(endDateStr);
    setDaySalesStartDate(startDateStr);
    setDaySalesEndDate(endDateStr);
  }, []);

  useEffect(() => {
    loadClients();
  }, []);

  useEffect(() => {
    setSalesMounted(true);
  }, []);

  useEffect(() => {
    setDaySalesMounted(true);
  }, []);

  // Initial load of day sales report
  useEffect(() => {
    if (!initialLoadRef.current && daySalesStartDate && daySalesEndDate) {
      initialLoadRef.current = true;
      loadDaySalesReport();
    }
  }, [daySalesStartDate, daySalesEndDate]);

  // Load sales report when filters change
  useEffect(() => {
    if (salesStartDate && salesEndDate) {
      loadSalesReport();
    }
  }, [salesStartDate, salesEndDate, salesPage, salesClientId]);

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

  async function loadSalesReport() {
    if (!salesStartDate || !salesEndDate) return;

    setSalesLoading(true);
    try {
      const start = new Date(salesStartDate);
      start.setHours(0, 0, 0, 0);
      const end = new Date(salesEndDate);
      end.setHours(23, 59, 59, 999);

      const form: any = {
        startDate: start.toISOString(),
        endDate: end.toISOString(),
        page: salesPage,
        pageSize: salesPageSize,
      };

      if (salesClientId) {
        form.clientId = salesClientId;
      }

      const result = await apiPost<SalesReportPageData>("/reports/sales", form);
      setSalesData(result);
    } catch (error: any) {
      toast.error("Failed to load sales report: " + error.message);
    } finally {
      setSalesLoading(false);
    }
  }

  async function loadDaySalesReport() {
    if (!daySalesStartDate || !daySalesEndDate) return;

    setDaySalesLoading(true);
    try {
      const start = new Date(daySalesStartDate);
      const end = new Date(daySalesEndDate);

      const form: DaySalesForm = {
        startDate: start.toISOString().split("T")[0],
        endDate: end.toISOString().split("T")[0],
        page: daySalesPage,
        pageSize: daySalesPageSize,
      };

      const result = await apiPost<DaySalesPageData>("/reports/day-sales", form);
      setDaySalesData(result);
    } catch (error: any) {
      toast.error("Failed to load day sales report: " + error.message);
    } finally {
      setDaySalesLoading(false);
    }
  }

  async function handleSalesExportCsv() {
    if (!salesStartDate || !salesEndDate) return;

    const start = new Date(salesStartDate);
    start.setHours(0, 0, 0, 0);
    const end = new Date(salesEndDate);
    end.setHours(23, 59, 59, 999);

    const form: any = {
      startDate: start.toISOString(),
      endDate: end.toISOString(),
    };

    if (salesClientId) {
      form.clientId = salesClientId;
    }

    try {
      await apiExport("/reports/sales/export", form);
      toast.success("CSV downloaded successfully");
    } catch (error: any) {
      toast.error("Failed to export CSV: " + error.message);
    }
  }

  async function handleDaySalesExportCsv() {
    if (!daySalesStartDate || !daySalesEndDate) return;

    const start = new Date(daySalesStartDate);
    const end = new Date(daySalesEndDate);

    const form = {
      startDate: start.toISOString().split("T")[0],
      endDate: end.toISOString().split("T")[0],
    };

    try {
      await apiExport("/reports/day-sales/export", form);
      toast.success("CSV downloaded successfully");
    } catch (error: any) {
      toast.error("Failed to export CSV: " + error.message);
    }
  }

  const clientOptions: ClientOption[] = clients
    .filter((c) => c.enabled)
    .sort((a, b) => a.clientName.localeCompare(b.clientName))
    .map((c) => ({ value: c.id, label: c.clientName }));

  const selectedClient = clientOptions.find((o) => o.value === salesClientId) || null;

  const salesTotalRevenue = salesData?.rows.reduce(
    (sum, item) => sum + (item.revenue || 0),
    0
  ) || 0;
  const salesTotalQuantity = salesData?.rows.reduce(
    (sum, item) => sum + (item.quantitySold || 0),
    0
  ) || 0;

  const daySalesTotalRevenue = daySalesData?.content.reduce(
    (sum, item) => sum + (item.totalRevenue || 0),
    0
  ) || 0;
  const daySalesTotalOrders = daySalesData?.content.reduce(
    (sum, item) => sum + (item.invoicedOrdersCount || 0),
    0
  ) || 0;
  const daySalesTotalItems = daySalesData?.content.reduce(
    (sum, item) => sum + (item.invoicedItemsCount || 0),
    0
  ) || 0;
  const daySalesAvgDailyRevenue =
    daySalesData && daySalesData.content.length > 0
      ? daySalesTotalRevenue / daySalesData.content.length
      : 0;

  return (
    <AuthGuard requiredRole="SUPERVISOR">
      <div className="min-h-[calc(100vh-64px)] bg-slate-50 p-6">
        <div className="max-w-[1400px] mx-auto">
          {/* Header with Toggle */}
          <div className="flex items-center justify-between mb-6">
            <h1 className="text-3xl font-bold text-slate-800">
              {activeReport === "sales" ? "Sales Report" : "Day-on-Day Sales Report"}
            </h1>
            
            {/* Toggle Switch */}
            <div className="flex bg-white rounded-lg shadow-sm p-1">
              <button
                onClick={() => setActiveReport("sales")}
                className={`px-5 py-2 text-base font-medium rounded-md transition-colors ${
                  activeReport === "sales"
                    ? "bg-blue-500 text-white"
                    : "text-gray-600 hover:text-gray-900 hover:bg-gray-100"
                }`}
              >
                Sales Report
              </button>
              <button
                onClick={() => setActiveReport("daySales")}
                className={`px-5 py-2 text-base font-medium rounded-md transition-colors ${
                  activeReport === "daySales"
                    ? "bg-blue-500 text-white"
                    : "text-gray-600 hover:text-gray-900 hover:bg-gray-100"
                }`}
              >
                Day Sales
              </button>
            </div>
          </div>

          {/* Sales Report Content */}
          {activeReport === "sales" && (
            <>
              {/* Filters */}
              <div className="p-5 mb-6 bg-white rounded-xl shadow-sm">
                <div className="grid grid-cols-1 gap-4 sm:grid-cols-[200px_200px_200px_160px_140px]">
                  <div>
                    <label className="block mb-2 text-sm font-medium text-gray-700">
                      Start Date
                    </label>
                    <input
                      type="date"
                      value={salesStartDate}
                      onChange={(e) => {
                        setSalesStartDate(e.target.value);
                        setSalesPage(0);
                      }}
                      className="w-full px-3.5 py-2.5 text-base border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent h-[42px]"
                    />
                  </div>

                  <div>
                    <label className="block mb-2 text-sm font-medium text-gray-700">
                      End Date
                    </label>
                    <input
                      type="date"
                      value={salesEndDate}
                      onChange={(e) => {
                        setSalesEndDate(e.target.value);
                        setSalesPage(0);
                      }}
                      className="w-full px-3.5 py-2.5 text-base border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent h-[42px]"
                    />
                  </div>

                  <div>
                    <label className="block mb-2 text-sm font-medium text-gray-700">
                      Client
                    </label>
                    {salesMounted ? (
                      <Select
                        options={clientOptions}
                        value={selectedClient}
                        isClearable
                        placeholder="Select client..."
                        onChange={(option) =>
                          setSalesClientId(option ? option.value : "")
                        }
                        menuPortalTarget={typeof document !== "undefined" ? document.body : undefined}
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
                        Loading clients...
                      </div>
                    )}
                  </div>

                  <div className="flex items-end">
                    <button
                      onClick={() => {
                        setSalesPage(0);
                        loadSalesReport();
                      }}
                      disabled={salesLoading}
                      className={`w-full px-5 py-2.5 text-base font-medium text-white rounded-lg transition-colors h-[42px] ${
                        salesLoading
                          ? "bg-blue-400 cursor-not-allowed"
                          : "bg-blue-500 hover:bg-blue-600 cursor-pointer"
                      }`}
                    >
                      {salesLoading ? "Loading..." : "Generate Report"}
                    </button>
                  </div>

                  <div className="flex items-end">
                    <button
                      onClick={handleSalesExportCsv}
                      className="w-full px-4 py-2.5 text-base font-medium text-white bg-green-500 rounded-lg hover:bg-green-600 transition-colors cursor-pointer flex items-center justify-center gap-2 h-[42px]"
                    >
                      <svg
                        xmlns="http://www.w3.org/2000/svg"
                        className="w-4 h-4"
                        fill="none"
                        viewBox="0 0 24 24"
                        stroke="currentColor"
                      >
                        <path
                          strokeLinecap="round"
                          strokeLinejoin="round"
                          strokeWidth={2}
                          d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4"
                        />
                      </svg>
                      Export CSV
                    </button>
                  </div>
                </div>
              </div>

              {/* Summary */}
              {salesData && salesData.rows.length > 0 && (
                <div className="mb-4 sm:mb-6 p-4 sm:p-5 bg-white rounded-xl shadow-sm">
                  <div className="flex justify-between">
                    <div>
                      <div className="text-sm text-slate-500">Total Products</div>
                      <div className="text-xl sm:text-2xl font-bold text-slate-800">
                        {salesData.totalElements}
                      </div>
                    </div>
                    <div>
                      <div className="text-sm text-slate-500">Total Quantity Sold</div>
                      <div className="text-xl sm:text-2xl font-bold text-slate-800">
                        {salesTotalQuantity}
                      </div>
                    </div>
                    <div className="text-right">
                      <div className="text-sm text-slate-500">Total Revenue</div>
                      <div className="text-xl sm:text-2xl font-bold text-slate-800">
                        ₹{salesTotalRevenue.toFixed(2)}
                      </div>
                    </div>
                  </div>
                </div>
              )}

              {/* Report Table */}
              {salesLoading ? (
                <div className="py-12 text-center text-slate-500">Loading...</div>
              ) : salesData && salesData.rows.length === 0 ? (
                <div className="p-12 text-center text-slate-500 bg-white rounded-xl shadow-sm">
                  No sales data available for the selected date range.
                </div>
              ) : salesData ? (
                <>
                  <div className="p-6 bg-white rounded-xl shadow-sm overflow-x-auto">
                    <table className="w-full border-collapse">
                      <thead>
                        <tr className="border-b-2 border-gray-200">
                          <th className="px-3 py-3 text-base font-semibold text-left text-gray-700">
                            Product Name
                          </th>
                          <th className="px-3 py-3 text-base font-semibold text-left text-gray-700">
                            Quantity Sold
                          </th>
                          <th className="px-3 py-3 text-base font-semibold text-left text-gray-700">
                            Revenue
                          </th>
                        </tr>
                      </thead>
                      <tbody>
                        {salesData.rows.map((item, idx) => (
                          <tr key={idx} className="border-b border-gray-100">
                            <td className="px-3 py-3 text-base">{item.productName}</td>
                            <td className="px-3 py-3 text-base">{item.quantitySold}</td>
                            <td className="px-3 py-3 text-base">
                              ₹{Number(item.revenue).toFixed(2)}
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>

                  {/* Pagination */}
                  {salesData.totalElements > salesPageSize && (
                    <div className="flex items-center justify-between mt-6 p-6 bg-white rounded-xl shadow-sm">
                      <div className="text-base text-slate-500">
                        Showing {salesData.rows.length} of {salesData.totalElements} products
                      </div>
                      <div className="flex gap-2">
                        <button
                          onClick={() => setSalesPage(Math.max(0, salesPage - 1))}
                          disabled={salesPage === 0}
                          className={`px-4 py-2 text-base border border-gray-300 rounded-lg ${
                            salesPage === 0
                              ? "bg-white text-gray-400 cursor-not-allowed opacity-50"
                              : "bg-white text-gray-700 hover:bg-gray-50 cursor-pointer"
                          }`}
                        >
                          Previous
                        </button>
                        <span className="px-4 py-2 text-base text-gray-700">
                          Page {salesPage + 1} of{" "}
                          {Math.ceil(salesData.totalElements / salesPageSize)}
                        </span>
                        <button
                          onClick={() => setSalesPage(salesPage + 1)}
                          disabled={(salesPage + 1) * salesPageSize >= salesData.totalElements}
                          className={`px-4 py-2 text-base border border-gray-300 rounded-lg ${
                            (salesPage + 1) * salesPageSize >= salesData.totalElements
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
            </>
          )}

          {/* Day Sales Report Content */}
          {activeReport === "daySales" && (
            <>
              {/* Filters */}
              <div className="p-5 mb-6 bg-white rounded-xl shadow-sm">
                <div className="grid grid-cols-1 gap-4 sm:grid-cols-[200px_200px_160px_140px]">
                  <div>
                    <label className="block mb-2 text-sm font-medium text-gray-700">
                      Start Date
                    </label>
                    <input
                      type="date"
                      value={daySalesStartDate}
                      onChange={(e) => {
                        setDaySalesStartDate(e.target.value);
                        setDaySalesPage(0);
                      }}
                      className="w-full px-3.5 py-2.5 text-base border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent h-[42px]"
                    />
                  </div>

                  <div>
                    <label className="block mb-2 text-sm font-medium text-gray-700">
                      End Date
                    </label>
                    <input
                      type="date"
                      value={daySalesEndDate}
                      onChange={(e) => {
                        setDaySalesEndDate(e.target.value);
                        setDaySalesPage(0);
                      }}
                      className="w-full px-3.5 py-2.5 text-base border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent h-[42px]"
                    />
                  </div>

                  <div className="flex items-end">
                    <button
                      onClick={() => {
                        setDaySalesPage(0);
                        loadDaySalesReport();
                      }}
                      disabled={daySalesLoading}
                      className={`w-full px-5 py-2.5 text-base font-medium text-white rounded-lg transition-colors h-[42px] ${
                        daySalesLoading
                          ? "bg-blue-400 cursor-not-allowed"
                          : "bg-blue-500 hover:bg-blue-600 cursor-pointer"
                      }`}
                    >
                      {daySalesLoading ? "Loading..." : "Generate Report"}
                    </button>
                  </div>

                  <div className="flex items-end">
                    <button
                      onClick={handleDaySalesExportCsv}
                      className="w-full px-4 py-2.5 text-base font-medium text-white bg-green-500 rounded-lg hover:bg-green-600 transition-colors cursor-pointer flex items-center justify-center gap-2 h-[42px]"
                    >
                      <svg
                        xmlns="http://www.w3.org/2000/svg"
                        className="w-4 h-4"
                        fill="none"
                        viewBox="0 0 24 24"
                        stroke="currentColor"
                      >
                        <path
                          strokeLinecap="round"
                          strokeLinejoin="round"
                          strokeWidth={2}
                          d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4"
                        />
                      </svg>
                      Export CSV
                    </button>
                  </div>
                </div>
              </div>

              {/* Summary */}
              {daySalesData && daySalesData.content.length > 0 && (
                <div className="mb-4 sm:mb-6 p-4 sm:p-5 bg-white rounded-xl shadow-sm">
                  <div className="flex justify-between">
                    <div>
                      <div className="text-sm text-slate-500">Total Days</div>
                      <div className="text-xl sm:text-2xl font-bold text-slate-800">
                        {daySalesData.totalElements}
                      </div>
                    </div>
                    <div>
                      <div className="text-sm text-slate-500">Total Orders</div>
                      <div className="text-xl sm:text-2xl font-bold text-slate-800">
                        {daySalesTotalOrders}
                      </div>
                    </div>
                    <div>
                      <div className="text-sm text-slate-500">Total Items Sold</div>
                      <div className="text-xl sm:text-2xl font-bold text-slate-800">
                        {daySalesTotalItems}
                      </div>
                    </div>
                    <div>
                      <div className="text-sm text-slate-500">Total Revenue</div>
                      <div className="text-xl sm:text-2xl font-bold text-slate-800">
                        ₹{daySalesTotalRevenue.toFixed(2)}
                      </div>
                    </div>
                    <div className="text-right">
                      <div className="text-sm text-slate-500">Avg Daily Revenue</div>
                      <div className="text-xl sm:text-2xl font-bold text-slate-800">
                        ₹{daySalesAvgDailyRevenue.toFixed(2)}
                      </div>
                    </div>
                  </div>
                </div>
              )}

              {/* Report Table */}
              {daySalesLoading ? (
                <div className="py-12 text-center text-slate-500">Loading...</div>
              ) : daySalesData && daySalesData.content.length === 0 ? (
                <div className="p-12 text-center text-slate-500 bg-white rounded-xl shadow-sm">
                  No data available. Please select date range and generate report.
                </div>
              ) : daySalesData ? (
                <>
                  <div className="p-6 bg-white rounded-xl shadow-sm overflow-x-auto">
                    <table className="w-full border-collapse">
                      <thead>
                        <tr className="border-b-2 border-gray-200">
                          <th className="px-3 py-3 text-base font-semibold text-left text-gray-700">
                            Date
                          </th>
                          <th className="px-3 py-3 text-base font-semibold text-left text-gray-700">
                            Orders
                          </th>
                          <th className="px-3 py-3 text-base font-semibold text-left text-gray-700">
                            Items Sold
                          </th>
                          <th className="px-3 py-3 text-base font-semibold text-left text-gray-700">
                            Revenue
                          </th>
                          <th className="px-3 py-3 text-base font-semibold text-left text-gray-700">
                            Avg Order Value
                          </th>
                        </tr>
                      </thead>
                      <tbody>
                        {daySalesData.content.map((item, idx) => {
                          const avgOrderValue =
                            item.invoicedOrdersCount > 0
                              ? item.totalRevenue / item.invoicedOrdersCount
                              : 0;
                          return (
                            <tr key={idx} className="border-b border-gray-100">
                              <td className="px-3 py-3 text-base">
                                {daySalesMounted
                                  ? new Date(item.date).toLocaleDateString("en-IN", {
                                      year: "numeric",
                                      month: "long",
                                      day: "numeric",
                                    })
                                  : ""}
                              </td>
                              <td className="px-3 py-3 text-base">
                                {item.invoicedOrdersCount}
                              </td>
                              <td className="px-3 py-3 text-base">
                                {item.invoicedItemsCount}
                              </td>
                              <td className="px-3 py-3 text-base">
                                ₹{Number(item.totalRevenue).toFixed(2)}
                              </td>
                              <td className="px-3 py-3 text-base">
                                ₹{avgOrderValue.toFixed(2)}
                              </td>
                            </tr>
                          );
                        })}
                      </tbody>
                    </table>
                  </div>

                  {/* Pagination */}
                  {daySalesData.totalElements > daySalesPageSize && (
                    <div className="flex items-center justify-between mt-6 p-6 bg-white rounded-xl shadow-sm">
                      <div className="text-base text-slate-500">
                        Showing {daySalesData.content.length} of{" "}
                        {daySalesData.totalElements} days
                      </div>
                      <div className="flex gap-2">
                        <button
                          onClick={() =>
                            setDaySalesPage(Math.max(0, daySalesPage - 1))
                          }
                          disabled={daySalesPage === 0}
                          className={`px-4 py-2 text-base border border-gray-300 rounded-lg ${
                            daySalesPage === 0
                              ? "bg-white text-gray-400 cursor-not-allowed opacity-50"
                              : "bg-white text-gray-700 hover:bg-gray-50 cursor-pointer"
                          }`}
                        >
                          Previous
                        </button>
                        <span className="px-4 py-2 text-base text-gray-700">
                          Page {daySalesPage + 1} of{" "}
                          {Math.ceil(daySalesData.totalElements / daySalesPageSize)}
                        </span>
                        <button
                          onClick={() => setDaySalesPage(daySalesPage + 1)}
                          disabled={
                            (daySalesPage + 1) * daySalesPageSize >=
                            daySalesData.totalElements
                          }
                          className={`px-4 py-2 text-base border border-gray-300 rounded-lg ${
                            (daySalesPage + 1) * daySalesPageSize >=
                            daySalesData.totalElements
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
            </>
          )}
        </div>
      </div>
    </AuthGuard>
  );
}


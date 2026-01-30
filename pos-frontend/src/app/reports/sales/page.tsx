"use client";

import { useState, useEffect } from "react";
import { apiGet, apiPost } from "@/lib/api";
import {
  SalesReportPageData,
  ClientData,
} from "@/lib/types";
import Link from "next/link";
import AuthGuard from "@/components/AuthGuard";
import Select from "react-select";

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
  const [page, setPage] = useState(0);
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
    loadClients();
  }, []);

  useEffect(() => {
    setMounted(true);
  }, []);

  useEffect(() => {
    if (startDate && endDate) {
      loadReport();
    }
  }, [startDate, endDate, page, clientId]);

  const clientOptions: ClientOption[] = clients
    .filter((c) => c.enabled)
    .sort((a, b) => a.clientName.localeCompare(b.clientName))
    .map((c) => ({ value: c.id, label: c.clientName }));

  const selectedClient = clientOptions.find((o) => o.value === clientId) || null;

  async function loadClients() {
    try {
      const clientsData = await apiGet<ClientData[]>("/clients");
      setClients(clientsData);
    } catch (e: any) {
      console.error("Failed to load clients: " + e.message);
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
      alert("Failed to load report: " + error.message);
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

  return (
    <AuthGuard>
      <div
        style={{
          minHeight: "calc(100vh - 64px)",
          backgroundColor: "#f8fafc",
          padding: 24,
        }}
      >
        <div style={{ maxWidth: 1400, margin: "0 auto" }}>
          <div
            style={{
              display: "flex",
              justifyContent: "space-between",
              alignItems: "center",
              marginBottom: 24,
            }}
          >
            <h1
              style={{
                fontSize: 28,
                fontWeight: "bold",
                color: "#1e293b",
              }}
            >
              Sales Report
            </h1>
            <Link
              href="/reports"
              style={{
                padding: "10px 20px",
                backgroundColor: "#6b7280",
                color: "white",
                textDecoration: "none",
                borderRadius: 8,
                fontSize: 14,
                fontWeight: 500,
              }}
            >
              Back to Reports
            </Link>
          </div>

          {/* Filters */}
          <div
            style={{
              backgroundColor: "white",
              borderRadius: 12,
              padding: 20,
              boxShadow: "0 1px 3px rgba(0,0,0,0.1)",
              marginBottom: 24,
            }}
          >
            <div
              style={{
                display: "grid",
                gridTemplateColumns: "repeat(auto-fit, minmax(200px, 1fr))",
                gap: 16,
              }}
            >
              <div>
                <label
                  style={{
                    display: "block",
                    marginBottom: 8,
                    fontSize: 14,
                    fontWeight: 500,
                    color: "#374151",
                  }}
                >
                  Start Date
                </label>
                <input
                  type="date"
                  value={startDate}
                  onChange={(e) => {
                    setStartDate(e.target.value);
                    setPage(0);
                  }}
                  style={{
                    width: "100%",
                    padding: "10px 14px",
                    border: "1px solid #d1d5db",
                    borderRadius: 8,
                    fontSize: 14,
                  }}
                />
              </div>

              <div>
                <label
                  style={{
                    display: "block",
                    marginBottom: 8,
                    fontSize: 14,
                    fontWeight: 500,
                    color: "#374151",
                  }}
                >
                  End Date
                </label>
                <input
                  type="date"
                  value={endDate}
                  onChange={(e) => {
                    setEndDate(e.target.value);
                    setPage(0);
                  }}
                  style={{
                    width: "100%",
                    padding: "10px 14px",
                    border: "1px solid #d1d5db",
                    borderRadius: 8,
                    fontSize: 14,
                  }}
                />
              </div>

              <div>
                <label
                  style={{
                    display: "block",
                    marginBottom: 8,
                    fontSize: 14,
                    fontWeight: 500,
                    color: "#374151",
                  }}
                >
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
                    styles={{
                      menuPortal: (base: any) => ({ ...base, zIndex: 9999 }),
                    }}
                  />
                ) : (
                  <div style={{
                    padding: "10px 14px",
                    border: "1px solid #d1d5db",
                    borderRadius: 8,
                    fontSize: 14,
                    color: "#6b7280",
                    backgroundColor: "#f9fafb",
                  }}>
                    Loading clients...
                  </div>
                )}
              </div>

              <div
                style={{
                  display: "flex",
                  alignItems: "flex-end",
                }}
              >
                <button
                  onClick={() => {
                    setPage(0);
                    loadReport();
                  }}
                  disabled={loading}
                  style={{
                    padding: "10px 20px",
                    backgroundColor: "#667eea",
                    color: "white",
                    border: "none",
                    borderRadius: 8,
                    cursor: loading ? "not-allowed" : "pointer",
                    fontSize: 14,
                    fontWeight: 500,
                  }}
                >
                  {loading ? "Loading..." : "Generate Report"}
                </button>
              </div>
            </div>
          </div>

          {/* Summary */}
          {data && data.rows.length > 0 && (
            <div
              style={{
                backgroundColor: "white",
                borderRadius: 12,
                padding: 20,
                boxShadow: "0 1px 3px rgba(0,0,0,0.1)",
                marginBottom: 24,
                display: "grid",
                gridTemplateColumns: "repeat(auto-fit, minmax(200px, 1fr))",
                gap: 16,
              }}
            >
              <div>
                <div style={{ fontSize: 12, color: "#6b7280" }}>
                  Total Products
                </div>
                <div
                  style={{ fontSize: 24, fontWeight: "bold", color: "#1e293b" }}
                >
                  {data.totalElements}
                </div>
              </div>
              <div>
                <div style={{ fontSize: 12, color: "#6b7280" }}>
                  Total Quantity Sold
                </div>
                <div
                  style={{ fontSize: 24, fontWeight: "bold", color: "#1e293b" }}
                >
                  {totalQuantity}
                </div>
              </div>
              <div>
                <div style={{ fontSize: 12, color: "#6b7280" }}>
                  Total Revenue
                </div>
                <div
                  style={{ fontSize: 24, fontWeight: "bold", color: "#1e293b" }}
                >
                  ₹{totalRevenue.toFixed(2)}
                </div>
              </div>
            </div>
          )}

          {/* Report Table */}
          {loading ? (
            <div style={{ textAlign: "center", padding: 48 }}>Loading...</div>
          ) : data && data.rows.length === 0 ? (
            <div
              style={{
                backgroundColor: "white",
                borderRadius: 12,
                padding: 48,
                textAlign: "center",
                color: "#6b7280",
                boxShadow: "0 1px 3px rgba(0,0,0,0.1)",
              }}
            >
              No sales data available for the selected date range.
            </div>
          ) : data ? (
            <>
              <div
                style={{
                  backgroundColor: "white",
                  borderRadius: 12,
                  padding: 24,
                  boxShadow: "0 1px 3px rgba(0,0,0,0.1)",
                  overflowX: "auto",
                }}
              >
                <table
                  style={{
                    width: "100%",
                    borderCollapse: "collapse",
                  }}
                >
                  <thead>
                    <tr style={{ borderBottom: "2px solid #e5e7eb" }}>
                      <th
                        style={{
                          padding: "12px",
                          textAlign: "left",
                          fontWeight: 600,
                          color: "#374151",
                          fontSize: 14,
                        }}
                      >
                        Product Name
                      </th>
                      <th
                        style={{
                          padding: "12px",
                          textAlign: "left",
                          fontWeight: 600,
                          color: "#374151",
                          fontSize: 14,
                        }}
                      >
                        Quantity Sold
                      </th>
                      <th
                        style={{
                          padding: "12px",
                          textAlign: "left",
                          fontWeight: 600,
                          color: "#374151",
                          fontSize: 14,
                        }}
                      >
                        Revenue
                      </th>
                    </tr>
                  </thead>
                  <tbody>
                    {data.rows.map((item, idx) => (
                      <tr
                        key={idx}
                        style={{
                          borderBottom: "1px solid #e5e7eb",
                        }}
                      >
                        <td style={{ padding: "12px" }}>{item.productName}</td>
                        <td style={{ padding: "12px" }}>{item.quantitySold}</td>
                        <td style={{ padding: "12px" }}>
                          ₹{Number(item.revenue).toFixed(2)}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>

              {/* Pagination */}
              {data.totalElements > pageSize && (
                <div
                  style={{
                    display: "flex",
                    justifyContent: "space-between",
                    alignItems: "center",
                    marginTop: 24,
                    padding: "16px 24px",
                    backgroundColor: "white",
                    borderRadius: 12,
                    boxShadow: "0 1px 3px rgba(0,0,0,0.1)",
                  }}
                >
                  <div style={{ color: "#6b7280", fontSize: 14 }}>
                    Showing {data.rows.length} of {data.totalElements} products
                  </div>
                  <div style={{ display: "flex", gap: 8 }}>
                    <button
                      onClick={() => setPage(Math.max(0, page - 1))}
                      disabled={page === 0}
                      style={{
                        padding: "8px 16px",
                        border: "1px solid #d1d5db",
                        borderRadius: 8,
                        cursor: page === 0 ? "not-allowed" : "pointer",
                        opacity: page === 0 ? 0.5 : 1,
                        backgroundColor: "white",
                        fontSize: 14,
                      }}
                    >
                      Previous
                    </button>
                    <span
                      style={{
                        padding: "8px 16px",
                        color: "#374151",
                        fontSize: 14,
                      }}
                    >
                      Page {page + 1} of{" "}
                      {Math.ceil(data.totalElements / pageSize)}
                    </span>
                    <button
                      onClick={() => setPage(page + 1)}
                      disabled={(page + 1) * pageSize >= data.totalElements}
                      style={{
                        padding: "8px 16px",
                        border: "1px solid #d1d5db",
                        borderRadius: 8,
                        cursor:
                          (page + 1) * pageSize >= data.totalElements
                            ? "not-allowed"
                            : "pointer",
                        opacity:
                          (page + 1) * pageSize >= data.totalElements
                            ? 0.5
                            : 1,
                        backgroundColor: "white",
                        fontSize: 14,
                      }}
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


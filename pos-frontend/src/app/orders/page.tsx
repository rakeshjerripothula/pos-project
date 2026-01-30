
"use client";

import { useState, useEffect, useRef } from "react";
import { apiGet, apiPost } from "@/lib/api";
import { OrderPageData, OrderItemData, OrderStatus, ClientData } from "@/lib/types";
import { utcToIst } from "@/lib/utils";
import Link from "next/link";
import AuthGuard from "@/components/AuthGuard";
import React from "react";
import Select from "react-select";

export default function OrdersPage() {
  const [data, setData] = useState<OrderPageData | null>(null);
  const [loading, setLoading] = useState(true);
  const [expandedOrders, setExpandedOrders] = useState<Set<number>>(new Set());
  const [orderItemsCache, setOrderItemsCache] = useState<  Map<number, OrderItemData[]>>(new Map());
  const [clientsLoaded, setClientsLoaded] = useState(false);

  // Filters
  const [status, setStatus] = useState<OrderStatus | "">("");
  const [clientId, setClientId] = useState("");
  const [clients, setClients] = useState<ClientData[] | []>([]);
  const [orderId, setOrderId] = useState("");
  const [startDate, setStartDate] = useState("");
  const [endDate, setEndDate] = useState("");
  const [page, setPage] = useState(0);
  const pageSize = 10;

  const clientsLoadedRef = useRef(false);

  useEffect(() => {
    // Load clients only once
    if (!clientsLoadedRef.current) {
      clientsLoadedRef.current = true;
      loadClients();
    }
  }, []);

  useEffect(() => {
    loadOrders();
  }, [status, startDate, endDate, page, clientId]);

  const clientOptions = clients.filter((c) => c.enabled).sort((a, b) => a.clientName.localeCompare(b.clientName))
                        .map((c) => ({value: c.id,label: c.clientName,}));

  const selectedClient = clientOptions.find((o) => o.value === Number(clientId)) || null;

  async function loadClients() {
    try {
      const clientsData = await apiGet<ClientData[]>("/clients");
      setClients(clientsData);
    } catch (e: any) {
      // Silently handle client loading errors - they are optional for the orders page
      console.warn("Failed to load clients, continuing without client names");
    }
  }

  async function loadOrders() {
    setLoading(true);
    try {
      const params = new URLSearchParams();
      params.append("page", page.toString());
      params.append("pageSize", pageSize.toString());

      if (clientId) {
        params.append("clientId", clientId);
      }
      if (status) {
        params.append("status", status);
      }
      if (startDate) {
        const start = new Date(startDate);
        params.append("startDate", start.toISOString());
      }
      if (endDate) {
        const end = new Date(endDate);
        end.setHours(23, 59, 59, 999);
        params.append("endDate", end.toISOString());
      }

      const result = await apiGet<OrderPageData>(`/orders?${params.toString()}`);

      if (orderId) {
        result.content = result.content.filter(
          (order) => (order.id || order.orderId)?.toString() === orderId
        );
      }

      setData(result);
    } catch (error: any) {
      // Only show error if it's not a network error
      if (error.message !== "Failed to fetch") {
        console.error("Failed to load orders:", error);
      }
      // Set empty data to prevent infinite loading
      setData({ content: [], page: 0, pageSize: pageSize, totalElements: 0 });
    } finally {
      setLoading(false);
    }
  }

  async function toggleOrder(orderId: number) {
    const newExpanded = new Set(expandedOrders);
    if (newExpanded.has(orderId)) {
      newExpanded.delete(orderId);
    } else {
      newExpanded.add(orderId);
      if (!orderItemsCache.has(orderId)) {
        try {
          const items = await apiGet<OrderItemData[]>(`/orders/${orderId}/items`);
          setOrderItemsCache((prev) => {
            const newCache = new Map(prev);
            newCache.set(orderId, items);
            return newCache;
          });
        } catch (error: any) {
          alert("Failed to load order items: " + error.message);
        }
      }
    }
    setExpandedOrders(newExpanded);
  }

  async function downloadInvoice(orderId: number) {
    try {
      const userStr = sessionStorage.getItem("pos_user");
      const userId = userStr ? JSON.parse(userStr).id.toString() : "";

      const response = await fetch(
        `http://localhost:8080/orders/${orderId}/invoice/download`,
        {
          headers: {
            "X-User-Id": userId,
          },
        }
      );

      if (!response.ok) {
        throw new Error("Failed to download invoice");
      }

      const blob = await response.blob();
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement("a");
      a.href = url;
      a.download = `invoice-${orderId}.pdf`;
      document.body.appendChild(a);
      a.click();
      window.URL.revokeObjectURL(url);
      document.body.removeChild(a);
    } catch (error: any) {
      alert("Failed to download invoice: " + error.message);
    }
  }

  async function generateInvoice(orderId: number) {
    try {
      await apiPost(`/orders/${orderId}/invoice`, {});
      alert("Invoice generated successfully!");
      loadOrders();
    } catch (error: any) {
      alert("Failed to generate invoice: " + error.message);
    }
  }

  async function cancelOrder(orderId: number) {
    if (!confirm("Are you sure you want to cancel this order?")) {
      return;
    }
    try {
      await apiPost(`/orders/${orderId}/cancel`, {});
      alert("Order cancelled successfully!");
      loadOrders();
    } catch (error: any) {
      alert("Failed to cancel order: " + error.message);
    }
  }

  function handleSearch() {
    setPage(0);
    loadOrders();
  }

  function clearFilters() {
    setStatus("");
    setClientId("");
    setOrderId("");
    setStartDate("");
    setEndDate("");
    setPage(0);
  }

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
              Orders
            </h1>
            <Link
              href="/orders/create"
              style={{
                padding: "10px 20px",
                backgroundColor: "#667eea",
                color: "white",
                textDecoration: "none",
                borderRadius: 8,
                fontSize: 14,
                fontWeight: 500,
              }}
            >
              Create Order
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
                  Order ID
                </label>
                <input
                  type="text"
                  value={orderId}
                  onChange={(e) => setOrderId(e.target.value)}
                  placeholder="Search by Order ID"
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
                  Status
                </label>
                <select
                  value={status}
                  onChange={(e) => setStatus(e.target.value as OrderStatus | "")}
                  style={{
                    width: "100%",
                    padding: "10px 14px",
                    border: "1px solid #d1d5db",
                    borderRadius: 8,
                    fontSize: 14,
                  }}
                >
                  <option value="">All</option>
                  <option value="CREATED">Created</option>
                  <option value="INVOICED">Invoiced</option>
                  <option value="CANCELLED">Cancelled</option>
                </select>
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
                <Select
                  options={clientOptions}
                  value={selectedClient}
                  isClearable
                  placeholder="Select client..."
                  onChange={(option) =>
                    setClientId(option ? String(option.value) : "")
                  }
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
                  Start Date
                </label>
                <input
                  type="date"
                  value={startDate}
                  onChange={(e) => setStartDate(e.target.value)}
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
                  onChange={(e) => setEndDate(e.target.value)}
                  style={{
                    width: "100%",
                    padding: "10px 14px",
                    border: "1px solid #d1d5db",
                    borderRadius: 8,
                    fontSize: 14,
                  }}
                />
              </div>

              <div
                style={{
                  display: "flex",
                  gap: 8,
                  alignItems: "flex-end",
                }}
              >
                <button
                  onClick={handleSearch}
                  style={{
                    padding: "10px 20px",
                    backgroundColor: "#667eea",
                    color: "white",
                    border: "none",
                    borderRadius: 8,
                    cursor: "pointer",
                    fontSize: 14,
                    fontWeight: 500,
                  }}
                >
                  Search
                </button>
                <button
                  onClick={clearFilters}
                  style={{
                    padding: "10px 20px",
                    backgroundColor: "#6b7280",
                    color: "white",
                    border: "none",
                    borderRadius: 8,
                    cursor: "pointer",
                    fontSize: 14,
                    fontWeight: 500,
                  }}
                >
                  Clear
                </button>
              </div>
            </div>
          </div>

          {/* Orders Table */}
          {loading && !data ? (
            <div style={{ textAlign: "center", padding: 48 }}>Loading...</div>
          ) : data && data.content.length > 0 ? (
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
                        Order ID
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
                        Client ID
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
                        Status
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
                        Created At (IST)
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
                        Actions
                      </th>
                    </tr>
                  </thead>
                  <tbody>
                    {data.content.map((order) => {
                      const orderIdValue = order.id || order.orderId || 0;
                      return (
                        <React.Fragment key={orderIdValue}>
                          <tr
                            key={orderIdValue}
                            style={{
                              borderBottom: "1px solid #e5e7eb",
                            }}
                          >
                            <td style={{ padding: "12px" }}>
                              <button
                                onClick={() => toggleOrder(orderIdValue)}
                                style={{
                                  background: "none",
                                  border: "none",
                                  cursor: "pointer",
                                  color: "#667eea",
                                  textDecoration: "underline",
                                  fontSize: 14,
                                  fontWeight: 500,
                                }}
                              >
                                {expandedOrders.has(orderIdValue) ? "▼" : "▶"}{" "}
                                {orderIdValue}
                              </button>
                            </td>
                            <td style={{ padding: "12px" }}>
                              {clients.find((c) => c.id === order.clientId)?.clientName || order.clientId}
                            </td>
                            <td style={{ padding: "12px" }}>
                              <span
                                style={{
                                  padding: "4px 12px",
                                  borderRadius: 12,
                                  fontSize: 12,
                                  fontWeight: 500,
                                  backgroundColor:
                                    order.status === "INVOICED"
                                      ? "#dbeafe"
                                      : order.status === "CANCELLED"
                                      ? "#fee2e2"
                                      : "#fef3c7",
                                  color:
                                    order.status === "INVOICED"
                                      ? "#1e40af"
                                      : order.status === "CANCELLED"
                                      ? "#991b1b"
                                      : "#92400e",
                                }}
                              >
                                {order.status}
                              </span>
                            </td>
                            <td style={{ padding: "12px" }}>
                              {utcToIst(order.createdAt)}
                            </td>
                            <td style={{ padding: "12px" }}>
                              {order.status === "CREATED" && (
                                <>
                                  <button
                                    onClick={() => generateInvoice(orderIdValue)}
                                    style={{
                                      padding: "6px 12px",
                                      backgroundColor: "#10b981",
                                      color: "white",
                                      border: "none",
                                      borderRadius: 6,
                                      cursor: "pointer",
                                      fontSize: 12,
                                      fontWeight: 500,
                                      marginRight: 8,
                                    }}
                                  >
                                    Generate Invoice
                                  </button>
                                  <button
                                    onClick={() => cancelOrder(orderIdValue)}
                                    style={{
                                      padding: "6px 12px",
                                      backgroundColor: "#ef4444",
                                      color: "white",
                                      border: "none",
                                      borderRadius: 6,
                                      cursor: "pointer",
                                      fontSize: 12,
                                      fontWeight: 500,
                                    }}
                                  >
                                    Cancel Order
                                  </button>
                                </>
                              )}
                              {order.status === "INVOICED" && (
                                <button
                                  onClick={() => downloadInvoice(orderIdValue)}
                                  style={{
                                    padding: "6px 12px",
                                    backgroundColor: "#10b981",
                                    color: "white",
                                    border: "none",
                                    borderRadius: 6,
                                    cursor: "pointer",
                                    fontSize: 12,
                                    fontWeight: 500,
                                  }}
                                >
                                  Download Invoice
                                </button>
                              )}
                              {order.status === "CANCELLED" && (
                                <span
                                  style={{
                                    color: "#6b7280",
                                    fontSize: 12,
                                  }}
                                >
                                  No actions available
                                </span>
                              )}
                            </td>
                          </tr>
                          {expandedOrders.has(orderIdValue) && (
                            <tr>
                              <td colSpan={5} style={{ padding: 20 }}>
                                <div>
                                  <h4
                                    style={{
                                      marginBottom: 12,
                                      fontSize: 16,
                                      fontWeight: 600,
                                      color: "#1e293b",
                                    }}
                                  >
                                    Order Items
                                  </h4>
                                  {orderItemsCache.has(orderIdValue) ? (
                                    <table
                                      style={{
                                        width: "100%",
                                        borderCollapse: "collapse",
                                      }}
                                    >
                                      <thead>
                                        <tr
                                          style={{
                                            backgroundColor: "#f9fafb",
                                            borderBottom: "1px solid #e5e7eb",
                                          }}
                                        >
                                          <th
                                            style={{
                                              padding: "8px",
                                              textAlign: "left",
                                              fontSize: 13,
                                              fontWeight: 600,
                                              color: "#6b7280",
                                            }}
                                          >
                                            Product ID
                                          </th>
                                          <th
                                            style={{
                                              padding: "8px",
                                              textAlign: "left",
                                              fontSize: 13,
                                              fontWeight: 600,
                                              color: "#6b7280",
                                            }}
                                          >
                                            Product Name
                                          </th>
                                          <th
                                            style={{
                                              padding: "8px",
                                              textAlign: "left",
                                              fontSize: 13,
                                              fontWeight: 600,
                                              color: "#6b7280",
                                            }}
                                          >
                                            Quantity
                                          </th>
                                          <th
                                            style={{
                                              padding: "8px",
                                              textAlign: "left",
                                              fontSize: 13,
                                              fontWeight: 600,
                                              color: "#6b7280",
                                            }}
                                          >
                                            Selling Price
                                          </th>
                                          <th
                                            style={{
                                              padding: "8px",
                                              textAlign: "left",
                                              fontSize: 13,
                                              fontWeight: 600,
                                              color: "#6b7280",
                                            }}
                                          >
                                            Total
                                          </th>
                                        </tr>
                                      </thead>
                                      <tbody>
                                        {orderItemsCache
                                          .get(orderIdValue)!
                                          .map((item, idx) => (
                                            <tr
                                              key={idx}
                                              style={{
                                                borderBottom: "1px solid #f3f4f6",
                                              }}
                                            >
                                              <td style={{ padding: "8px" }}>
                                                {item.productId}
                                              </td>
                                              <td style={{ padding: "8px" }}>
                                                {item.productName}
                                              </td>
                                              <td style={{ padding: "8px" }}>
                                                {item.quantity}
                                              </td>
                                              <td style={{ padding: "8px" }}>
                                                ₹{Number(item.sellingPrice).toFixed(2)}
                                              </td>
                                              <td style={{ padding: "8px" }}>
                                                ₹
                                                {(
                                                  item.quantity *
                                                  Number(item.sellingPrice)
                                                ).toFixed(2)}
                                              </td>
                                            </tr>
                                          ))}
                                      </tbody>
                                    </table>
                                  ) : (
                                    <div>Loading items...</div>
                                  )}
                                </div>
                              </td>
                            </tr>
                          )}
                        </React.Fragment>
                      );
                    })}
                  </tbody>
                </table>
              </div>

              {/* Pagination */}
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
                  Showing {data.content.length} of {data.totalElements} orders
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
                    Page {page + 1} of {Math.ceil(data.totalElements / pageSize)}
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
                        (page + 1) * pageSize >= data.totalElements ? 0.5 : 1,
                      backgroundColor: "white",
                      fontSize: 14,
                    }}
                  >
                    Next
                  </button>
                </div>
              </div>
            </>
          ) : (
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
              No orders found
            </div>
          )}
        </div>
      </div>
    </AuthGuard>
  );
}


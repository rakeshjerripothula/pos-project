"use client";

import { useState, useEffect, useRef } from "react";
import { apiGet, apiPost } from "@/lib/api";
import { OrderPageData, OrderItemData, OrderStatus, ClientData } from "@/lib/types";
import { utcToIst } from "@/lib/utils";
import Link from "next/link";
import AuthGuard from "@/components/AuthGuard";
import ConfirmModal from "@/components/ConfirmModal";
import React from "react";
import Select from "react-select";
import toast from "react-hot-toast";

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

  // Cancel modal state
  const [showCancelModal, setShowCancelModal] = useState(false);
  const [orderToCancel, setOrderToCancel] = useState<number | null>(null);
  const [cancelLoading, setCancelLoading] = useState(false);

  const clientsLoadedRef = useRef(false);

  useEffect(() => {
    // Load clients only once
    if (!clientsLoadedRef.current) {
      clientsLoadedRef.current = true;
      loadClients();
    }
  }, []);

  // Initial load of orders
  useEffect(() => {
    loadOrders();
  }, []); // Empty dependency array - only run once on mount

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
        toast.error(error.message);
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
          toast.error("Failed to load order items: " + error.message);
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
      toast.error("Failed to download invoice: " + error.message);
    }
  }

  async function generateInvoice(orderId: number) {
    try {
      await apiPost(`/orders/${orderId}/invoice`, {});
      toast.success("Invoice generated successfully!");
      loadOrders();
    } catch (error: any) {
      toast.error("Failed to generate invoice: " + error.message);
    }
  }

async function cancelOrder(orderId: number) {
    setCancelLoading(true);
    try {
      await apiPost(`/orders/${orderId}/cancel`, {});
      toast.success("Order cancelled successfully!");
      setShowCancelModal(false);
      setOrderToCancel(null);
      loadOrders();
    } catch (error: any) {
      toast.error("Failed to cancel order: " + error.message);
    } finally {
      setCancelLoading(false);
    }
  }

  function handleCancelClick(orderId: number) {
    setOrderToCancel(orderId);
    setShowCancelModal(true);
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
      <div className="min-h-[calc(100vh-64px)] bg-slate-50 p-4">
        <div className="max-w-[1400px] mx-auto">
          <div className="flex items-center justify-between mb-4">
            <h1 className="text-2xl font-bold text-slate-800">
              Orders
            </h1>
            <Link
              href="/orders/create"
              className="px-4 py-2 text-sm font-medium text-white bg-blue-500 rounded-md hover:bg-blue-600 transition-colors no-underline"
            >
              Create Order
            </Link>
          </div>

          {/* Filters */}
          <div className="p-4 mb-4 bg-white rounded-lg shadow-sm">
            <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-6">
              <div>
                <label className="block mb-1.5 text-xs font-medium text-gray-700">
                  Order ID
                </label>
                <input
                  type="text"
                  value={orderId}
                  onChange={(e) => setOrderId(e.target.value)}
                  placeholder="Search by Order ID"
                  className="w-full px-3 py-2 text-sm border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                />
              </div>

              <div>
                <label className="block mb-1.5 text-xs font-medium text-gray-700">
                  Status
                </label>
                <select
                  value={status}
                  onChange={(e) => setStatus(e.target.value as OrderStatus | "")}
                  className="w-full px-3 py-2 text-sm border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent bg-white"
                >
                  <option value="">All</option>
                  <option value="CREATED">Created</option>
                  <option value="INVOICED">Invoiced</option>
                  <option value="CANCELLED">Cancelled</option>
                </select>
              </div>

              <div>
                <label className="block mb-1.5 text-xs font-medium text-gray-700">
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
                  className="w-full"
                  classNamePrefix="react-select"
                  styles={{
                    control: (base) => ({
                      ...base,
                      borderRadius: "0.375rem",
                      border: "1px solid #d1d5db",
                      fontSize: "14px",
                      minHeight: "36px",
                      backgroundColor: "white",
                    }),
                    menu: (base) => ({
                      ...base,
                      borderRadius: "0.375rem",
                      boxShadow: "0 1px 3px rgba(0,0,0,0.1)",
                    }),
                    option: (base, state) => ({
                      ...base,
                      fontSize: "14px",
                      backgroundColor: state.isSelected 
                        ? "#3b82f6" 
                        : state.isFocused 
                          ? "#f3f4f6" 
                          : "white",
                      color: state.isSelected ? "white" : "#374151",
                      padding: "8px 12px",
                    }),
                  }}
                />
              </div>

              <div>
                <label className="block mb-1.5 text-xs font-medium text-gray-700">
                  Start Date
                </label>
                <input
                  type="date"
                  value={startDate}
                  onChange={(e) => setStartDate(e.target.value)}
                  className="w-full px-3 py-2 text-sm border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                />
              </div>

              <div>
                <label className="block mb-1.5 text-xs font-medium text-gray-700">
                  End Date
                </label>
                <input
                  type="date"
                  value={endDate}
                  onChange={(e) => setEndDate(e.target.value)}
                  className="w-full px-3 py-2 text-sm border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                />
              </div>

              <div className="flex gap-2 items-end">
                <button
                  onClick={handleSearch}
                  className="flex-1 px-4 h-[36px] text-sm font-medium text-white bg-blue-500 rounded-md hover:bg-blue-600 transition-colors cursor-pointer"
                >
                  Search
                </button>
                <button
                  onClick={clearFilters}
                  className="flex-1 px-4 h-[36px] text-sm font-medium text-white bg-gray-500 rounded-md hover:bg-gray-600 transition-colors cursor-pointer"
                >
                  Clear
                </button>
              </div>
            </div>
          </div>

          {/* Orders Table */}
          {loading && !data ? (
            <div className="py-8 text-center text-slate-500">Loading...</div>
          ) : data && data.content.length > 0 ? (
            <>
              <div className="p-4 bg-white rounded-lg shadow-sm overflow-x-auto">
                <table className="w-full border-collapse">
                  <thead>
                    <tr className="border-b-2 border-gray-200">
                      <th className="px-2 py-2.5 text-xs font-semibold text-left text-gray-700">Order ID</th>
                      <th className="px-2 py-2.5 text-xs font-semibold text-left text-gray-700">Client ID</th>
                      <th className="px-2 py-2.5 text-xs font-semibold text-left text-gray-700">Status</th>
                      <th className="px-2 py-2.5 text-xs font-semibold text-left text-gray-700">Created At (IST)</th>
                      <th className="px-2 py-2.5 text-xs font-semibold text-left text-gray-700">Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {data.content.map((order) => {
                      const orderIdValue = order.id || order.orderId || 0;
                      return (
                        <React.Fragment key={orderIdValue}>
                          <tr className="border-b border-gray-100">
                            <td className="px-2 py-2.5">
                              <button
                                onClick={() => toggleOrder(orderIdValue)}
                                className="bg-transparent border-none cursor-pointer text-gray-700 text-sm font-medium hover:text-blue-500"
                              >
                                {expandedOrders.has(orderIdValue) ? "▼" : "▶"}{" "}
                                {orderIdValue}
                              </button>
                            </td>
                            <td className="px-2 py-2.5 text-sm">
                              {clients.find((c) => c.id === order.clientId)?.clientName || order.clientId}
                            </td>
                            <td className="px-2 py-2.5">
                              <span className={`px-2.5 py-1 text-xs font-medium rounded-full ${
                                order.status === "INVOICED"
                                  ? "bg-blue-100 text-blue-800"
                                  : order.status === "CANCELLED"
                                  ? "bg-red-100 text-red-800"
                                  : "bg-amber-100 text-amber-800"
                              }`}>
                                {order.status}
                              </span>
                            </td>
                            <td className="px-2 py-2.5 text-sm">{utcToIst(order.createdAt)}</td>
                            <td className="px-2 py-2.5">
{order.status === "CREATED" && (
                                <>
                                  <button
                                    onClick={() => generateInvoice(orderIdValue)}
                                    className="px-3 py-1.5 text-xs text-white bg-blue-500 rounded-md hover:bg-blue-600 transition-colors cursor-pointer mr-1.5"
                                  >
                                    Generate Invoice
                                  </button>
                                  <button
                                    onClick={() => handleCancelClick(orderIdValue)}
                                    className="px-3 py-1.5 text-xs text-white bg-red-500 rounded-md hover:bg-red-600 transition-colors cursor-pointer"
                                  >
                                    Cancel Order
                                  </button>
                                </>
                              )}
                              {order.status === "INVOICED" && (
                                <button
                                  onClick={() => downloadInvoice(orderIdValue)}
                                  className="px-3 py-1.5 text-xs text-white bg-blue-500 rounded-md hover:bg-blue-600 transition-colors cursor-pointer"
                                >
                                  Download Invoice
                                </button>
                              )}
                              {order.status === "CANCELLED" && (
                                <span className="text-xs text-gray-500">
                                  No actions available
                                </span>
                              )}
                            </td>
                          </tr>
                          {expandedOrders.has(orderIdValue) && (
                            <tr>
                              <td colSpan={5} className="p-4">
                                <div>
                                  <h4 className="mb-2 text-sm font-semibold text-slate-800">
                                    Order Items
                                  </h4>
                                  {orderItemsCache.has(orderIdValue) ? (
                                    <table className="w-full border-collapse">
                                      <thead>
                                        <tr className="bg-gray-50 border-b border-gray-200">
                                          <th className="px-2 py-2 text-xs font-semibold text-left text-slate-500">Product ID</th>
                                          <th className="px-2 py-2 text-xs font-semibold text-left text-slate-500">Product Name</th>
                                          <th className="px-2 py-2 text-xs font-semibold text-left text-slate-500">Quantity</th>
                                          <th className="px-2 py-2 text-xs font-semibold text-left text-slate-500">Selling Price</th>
                                          <th className="px-2 py-2 text-xs font-semibold text-left text-slate-500">Total</th>
                                        </tr>
                                      </thead>
                                      <tbody>
                                        {orderItemsCache
                                          .get(orderIdValue)!
                                          .map((item, idx) => (
                                            <tr key={idx} className="border-b border-gray-100">
                                              <td className="px-2 py-2 text-sm">{item.productId}</td>
                                              <td className="px-2 py-2 text-sm">{item.productName}</td>
                                              <td className="px-2 py-2 text-sm">{item.quantity}</td>
                                              <td className="px-2 py-2 text-sm">₹{Number(item.sellingPrice).toFixed(2)}</td>
                                              <td className="px-2 py-2 text-sm">₹{(item.quantity * Number(item.sellingPrice)).toFixed(2)}</td>
                                            </tr>
                                          ))}
                                      </tbody>
                                    </table>
                                  ) : (
                                    <div className="text-sm text-slate-500">Loading items...</div>
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
              <div className="flex items-center justify-between mt-4 p-4 bg-white rounded-lg shadow-sm">
                <div className="text-sm text-slate-500">
                  Showing {data.content.length} of {data.totalElements} orders
                </div>
                <div className="flex gap-2">
                  <button
                    onClick={() => setPage(Math.max(0, page - 1))}
                    disabled={page === 0}
                    className={`px-4 py-1.5 text-sm border border-gray-300 rounded-md ${
                      page === 0 
                        ? "bg-white text-gray-400 cursor-not-allowed opacity-50" 
                        : "bg-white text-gray-700 hover:bg-gray-50 cursor-pointer"
                    }`}
                  >
                    Previous
                  </button>
                  <span className="px-3 py-1.5 text-sm text-gray-700">
                    Page {page + 1} of {Math.ceil(data.totalElements / pageSize)}
                  </span>
                  <button
                    onClick={() => setPage(page + 1)}
                    disabled={(page + 1) * pageSize >= data.totalElements}
                    className={`px-4 py-1.5 text-sm border border-gray-300 rounded-md ${
                      (page + 1) * pageSize >= data.totalElements 
                        ? "bg-white text-gray-400 cursor-not-allowed opacity-50" 
                        : "bg-white text-gray-700 hover:bg-gray-50 cursor-pointer"
                    }`}
                  >
                    Next
                  </button>
                </div>
              </div>
            </>
) : (
            <div className="p-10 text-center text-slate-500 bg-white rounded-lg shadow-sm">
              No orders found
            </div>
          )}


          {/* Cancel Order Confirmation Modal */}
          <ConfirmModal
            isOpen={showCancelModal}
            onClose={() => {
              setShowCancelModal(false);
              setOrderToCancel(null);
            }}
            onConfirm={() => {
              if (orderToCancel) {
                cancelOrder(orderToCancel);
              }
            }}
            title="Cancel Order"
            message="Are you sure you want to cancel this order? This action cannot be undone and the order status will change to CANCELLED."
            confirmText="Yes, Cancel Order"
            cancelText="Keep Order"
            confirmStyle="danger"
            isLoading={cancelLoading}
          />
        </div>
      </div>
    </AuthGuard>
  );
}


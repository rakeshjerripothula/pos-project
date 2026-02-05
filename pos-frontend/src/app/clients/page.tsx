"use client";

import { useState, useEffect, useMemo } from "react";
import { apiGet, apiPost, apiPatch, apiPut } from "@/lib/api";
import { ClientData, PagedResponse, ClientSearchForm } from "@/lib/types";
import ClientTable from "@/components/ClientTable";
import AddClient from "@/components/AddClient";
import AuthGuard, { isOperator } from "@/components/AuthGuard";
import toast from "react-hot-toast";

export default function ClientsPage() {
  const [allClients, setAllClients] = useState<ClientData[]>([]);
  const [loading, setLoading] = useState(true);
  const [isUserOperator, setIsUserOperator] = useState(false);

  // Pagination state
  const [page, setPage] = useState(0);
  const pageSize = 10;

  // Search/Filter state (client-side)
  const [searchTerm, setSearchTerm] = useState("");
  const [filterEnabled, setFilterEnabled] = useState<boolean | null>(null);

  useEffect(() => {
    setIsUserOperator(isOperator());
    loadClients();
  }, []);

  async function loadClients() {
    setLoading(true);
    try {
      // Load ALL clients for client-side filtering and pagination
      let allClients: ClientData[] = [];
      let page = 0;
      const pageSize = 100;
      let hasMore = true;

      while (hasMore) {
        const form: ClientSearchForm = {
          page,
          pageSize,
        };

        const data = await apiPost<PagedResponse<ClientData>>("/clients/list", form);
        allClients = [...allClients, ...data.data];
        hasMore = (page + 1) * pageSize < data.total;
        page++;
      }

      setAllClients(allClients);
      // Reset to first page when reloading
      setPage(0);
    } catch (error: any) {
      toast.error("Failed to load clients: " + error.message);
    } finally {
      setLoading(false);
    }
  }

  // Reset page when filters change
  useEffect(() => {
    setPage(0);
  }, [searchTerm, filterEnabled]);

  // Client-side filtering and pagination
  const { filteredClients, paginatedClients, totalElements } = useMemo(() => {
    const filtered = allClients.filter((c) => {
      const matchesSearch =
        !searchTerm ||
        c.clientName.toLowerCase().includes(searchTerm.toLowerCase());
      const matchesEnabled =
        filterEnabled === null || c.enabled === filterEnabled;
      return matchesSearch && matchesEnabled;
    });

    const total = filtered.length;
    const start = page * pageSize;
    const end = start + pageSize;
    const paginated = filtered.slice(start, end);

    return { filteredClients: filtered, paginatedClients: paginated, totalElements: total };
  }, [allClients, searchTerm, filterEnabled, page]);

  const totalPages = Math.ceil(totalElements / pageSize);

async function toggleClient(id: number, enabled: boolean) {
    try {
      // Update the client and get the returned updated client
      const updatedClient = await apiPatch<ClientData>(`/clients/${id}/toggle`, { enabled });
      
      // Update local state directly instead of reloading all clients
      setAllClients(prev => 
        prev.map(client => client.id === id ? updatedClient : client)
      );
      
      // Show success toast
      toast.success(enabled ? "Client enabled" : "Client disabled");
    } catch (error: any) {
      toast.error("Failed to toggle client: " + error.message);
    }
  }

async function addClient(clientName: string) {
    await apiPost("/clients", { clientName });
    await loadClients();
    toast.success("Client created successfully");
  }

async function updateClient(id: number, clientName: string) {
    await apiPut(`/clients/${id}`, { clientName });
    await loadClients();
    toast.success("Client updated successfully");
  }

  function clearFilters() {
    setSearchTerm("");
    setFilterEnabled(null);
  }

  return (
    <AuthGuard>
      <div className="min-h-[calc(100vh-64px)] bg-slate-50 p-4">
        <div className="max-w-[1400px] mx-auto">
          <div className="flex items-center justify-between mb-4">
            <h1 className="text-2xl font-bold text-slate-800">
              Clients
            </h1>
          </div>

          {/* Hide AddClient section for OPERATORs */}
          {!isUserOperator && (
            <div className="p-4 mb-4 bg-white rounded-lg shadow-sm">
              <AddClient onAdd={addClient} />
            </div>
          )}

          {loading && !allClients.length ? (
            <div className="py-8 text-center text-slate-500">Loading...</div>
          ) : (
            <div className="p-4 bg-white rounded-lg shadow-sm">
              {/* Search Bar */}
              <div className="grid grid-cols-1 gap-3 mb-4 sm:grid-cols-[180px_180px_auto]">
                <div>
                  <label className="block mb-1.5 text-xs font-medium text-gray-700">
                    Search Client
                  </label>
                  <input
                    type="text"
                    value={searchTerm}
                    onChange={(e) => setSearchTerm(e.target.value)}
                    placeholder="Search by client name..."
                    className="w-full px-3 py-2 text-sm border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent"
                  />
                </div>

                <div>
                  <label className="block mb-1.5 text-xs font-medium text-gray-700">
                    Filter by Status
                  </label>
                  <select
                    value={
                      filterEnabled === null
                        ? ""
                        : filterEnabled
                        ? "enabled"
                        : "disabled"
                    }
                    onChange={(e) => {
                      const value = e.target.value;
                      setFilterEnabled(
                        value === "" ? null : value === "enabled"
                      );
                    }}
                    className="w-full px-3 py-2 text-sm border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent bg-white"
                  >
                    <option value="">All</option>
                    <option value="enabled">Enabled</option>
                    <option value="disabled">Disabled</option>
                  </select>
                </div>

                <div className="flex items-end">
                  <button
                    onClick={clearFilters}
                    className="px-4 py-2 text-sm font-medium text-white bg-gray-500 rounded-md hover:bg-gray-600 transition-colors cursor-pointer"
                  >
                    Clear Filters
                  </button>
                </div>
              </div>

              <ClientTable
                clients={paginatedClients}
                onToggle={toggleClient}
                onUpdate={updateClient}
              />

              {/* Pagination */}
              {totalElements > 0 && (
                <div className="flex items-center justify-between mt-4 pt-4 border-t border-gray-200">
                  <div className="text-sm text-slate-500">
                    Showing {paginatedClients.length} of {totalElements} clients
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
                      Page {page + 1} of {totalPages || 1}
                    </span>
                    <button
                      onClick={() => setPage(page + 1)}
                      disabled={page >= totalPages - 1}
                      className={`px-4 py-1.5 text-sm border border-gray-300 rounded-md ${
                        page >= totalPages - 1 
                          ? "bg-white text-gray-400 cursor-not-allowed opacity-50" 
                          : "bg-white text-gray-700 hover:bg-gray-50 cursor-pointer"
                      }`}
                    >
                      Next
                    </button>
                  </div>
                </div>
              )}

              {totalElements === 0 && !loading && (
                <div className="py-10 text-center text-slate-500">
                  No clients found
                </div>
              )}
            </div>
          )}
        </div>
      </div>
    </AuthGuard>
  );
}


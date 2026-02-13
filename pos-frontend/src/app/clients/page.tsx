"use client";

import { useState, useEffect } from "react";
import { apiGet, apiPost, apiPatch, apiPut } from "@/lib/api";
import { ClientData, PagedResponse, ClientSearchForm } from "@/lib/types";
import ClientTable from "@/components/ClientTable";
import AddClientModal from "@/components/AddClientModal";
import AuthGuard, { isOperator } from "@/components/AuthGuard";
import toast from "react-hot-toast";

export default function ClientsPage() {
  const [clients, setClients] = useState<ClientData[]>([]);
  const [loading, setLoading] = useState(true);
  const [isUserOperator, setIsUserOperator] = useState(false);
  const [showAddModal, setShowAddModal] = useState(false);

  // Pagination state
  const [page, setPage] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const pageSize = 10;

  // Search/Filter state
  const [searchTerm, setSearchTerm] = useState("");
  const [filterEnabled, setFilterEnabled] = useState<boolean | null>(null);

  useEffect(() => {
    setIsUserOperator(isOperator());
  }, []);

  // Load clients on page load
  useEffect(() => {
    let mounted = true;
    async function loadClients() {
      setLoading(true);
      try {
        const form: ClientSearchForm = {
          page,
          pageSize,
          clientName: searchTerm || undefined,
          enabled: filterEnabled !== null ? filterEnabled : undefined,
        };

        const data = await apiPost<PagedResponse<ClientData>>("/clients/list", form);
        if (mounted) {
          setClients(data.data);
          setTotalElements(data.total);
        }
      } catch (error: any) {
        if (mounted) {
          toast.error("Failed to load clients: " + error.message);
        }
      } finally {
        if (mounted) {
          setLoading(false);
        }
      }
    }
    loadClients();
    return () => { mounted = false; };
  }, [page]);

  // Search handler - only triggers on button click
  async function handleSearch() {
    setPage(0);
    setLoading(true);
    try {
      const form: ClientSearchForm = {
        page: 0,
        pageSize,
        clientName: searchTerm || undefined,
        enabled: filterEnabled !== null ? filterEnabled : undefined,
      };

      const data = await apiPost<PagedResponse<ClientData>>("/clients/list", form);
      setClients(data.data);
      setTotalElements(data.total);
    } catch (error: any) {
      toast.error("Failed to search clients: " + error.message);
    } finally {
      setLoading(false);
    }
  }

  const totalPages = Math.ceil(totalElements / pageSize);

  async function toggleClient(id: number, enabled: boolean) {
    try {
      await apiPatch(`/clients/${id}/toggle`, { enabled });
      toast.success(enabled ? "Client enabled" : "Client disabled");
      await handleSearch();
    } catch (error: any) {
      toast.error("Failed to toggle client: " + error.message);
    }
  }

  async function addClient(clientName: string) {
    await apiPost("/clients", { clientName });
    setPage(0);
    await handleSearch();
    toast.success("Client created successfully");
  }

  async function updateClient(id: number, clientName: string) {
    await apiPut(`/clients/${id}`, { clientName });
    await handleSearch();
    toast.success("Client updated successfully");
  }

  function clearFilters() {
    setSearchTerm("");
    setFilterEnabled(null);
    setPage(0);
    handleSearch();
  }

  return (
    <AuthGuard>
      <div className="min-h-[calc(100vh-64px)] bg-slate-50 px-4 sm:px-30 p-3 sm:p-4">
        <div className="max-w-[1400px] mx-auto">
          <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between mb-4 gap-3">
            <h1 className="text-2xl sm:text-3xl font-bold text-slate-800">
              Clients
            </h1>
            {!isUserOperator && (
              <button
                onClick={() => setShowAddModal(true)}
                className="px-4 py-2 text-base font-medium text-white bg-blue-500 rounded-md hover:bg-blue-600 transition-colors cursor-pointer"
              >
                + Add Client
              </button>
            )}
          </div>

          <div className="p-3 sm:p-4 mb-4 bg-white rounded-lg shadow-sm">
            <div className="flex flex-wrap gap-3">
              <div className="flex-1 min-w-[160px] max-w-[220px]">
                <label className="block mb-1.5 text-sm font-medium text-gray-700">
                  Search Client
                </label>
                <input
                  type="text"
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  onKeyDown={(e) => {
                    if (e.key === "Enter") {
                      handleSearch();
                    }
                  }}
                  placeholder="Search by client name..."
                  className="w-full px-3 py-2 text-base border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent h-[42px]"
                />
              </div>

              <div className="flex-1 min-w-[140px] max-w-[180px]">
                <label className="block mb-1.5 text-sm font-medium text-gray-700">
                  Status
                </label>
                <select
                  value={filterEnabled === null ? "" : filterEnabled ? "enabled" : "disabled"}
                  onChange={(e) => {
                    const value = e.target.value;
                    setFilterEnabled(value === "" ? null : value === "enabled");
                  }}
                  className="w-full px-3 py-2 text-base border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent bg-white h-[42px]"
                >
                  <option value="">All</option>
                  <option value="enabled">Enabled</option>
                  <option value="disabled">Disabled</option>
                </select>
              </div>

              <div className="flex gap-2 items-end">
                <button
                  onClick={handleSearch}
                  className="px-4 py-2 text-base font-medium text-white bg-blue-500 rounded-md hover:bg-blue-600 transition-colors cursor-pointer h-[42px]"
                >
                  Search
                </button>
                <button
                  onClick={clearFilters}
                  className="px-4 py-2 text-base font-medium text-white bg-gray-500 rounded-md hover:bg-gray-600 transition-colors cursor-pointer h-[42px]"
                >
                  Clear
                </button>
              </div>
            </div>
          </div>

          {loading && !clients.length ? (
            <div className="py-8 text-center text-slate-500">Loading...</div>
          ) : (
            <div className="p-4 bg-white rounded-lg shadow-sm overflow-x-auto">
              <ClientTable clients={clients} onToggle={toggleClient} onUpdate={updateClient} page={page} pageSize={pageSize} />
              {totalElements > 0 && (
                <div className="flex flex-wrap items-center justify-between mt-4 pt-4 border-t border-gray-200 gap-y-3">
                  <div className="text-xs sm:text-sm md:text-base text-slate-500 order-2 sm:order-1">
                    Showing {clients.length} of {totalElements} clients
                  </div>
                  <div className="flex flex-wrap gap-1.5 sm:gap-2 order-1 sm:order-2">
                    <button
                      onClick={() => setPage(Math.max(0, page - 1))}
                      disabled={page === 0}
                      className={`px-2.5 py-1.5 text-xs sm:text-sm md:text-base border border-gray-300 rounded-md ${
                        page === 0 
                          ? "bg-white text-gray-400 cursor-not-allowed opacity-50" 
                          : "bg-white text-gray-700 hover:bg-gray-50 cursor-pointer"
                      }`}
                    >
                      Previous
                    </button>
                    <span className="px-2 py-1.5 text-xs sm:text-sm md:text-base text-gray-700">
                      {page + 1} / {totalPages || 1}
                    </span>
                    <button
                      onClick={() => setPage(page + 1)}
                      disabled={page >= totalPages - 1}
                      className={`px-2.5 py-1.5 text-xs sm:text-sm md:text-base border border-gray-300 rounded-md ${
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

          {showAddModal && (
            <AddClientModal
              onClose={() => setShowAddModal(false)}
              onAdd={addClient}
            />
          )}
        </div>
      </div>
    </AuthGuard>
  );
}
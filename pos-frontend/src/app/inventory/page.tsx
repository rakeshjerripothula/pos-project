"use client";

import { useState, useEffect, useMemo } from "react";
import { apiGet, apiPost } from "@/lib/api";
import { InventoryData, ProductData, PagedResponse, InventorySearchForm, ProductSearchForm } from "@/lib/types";
import UpdateInventory from "@/components/UpdateInventory";
import AddInventory from "@/components/AddInventory";
import InventoryTsvUpload from "@/components/InventoryTsvUpload";
import AuthGuard, { isOperator } from "@/components/AuthGuard";
import toast from "react-hot-toast";

export default function InventoryPage() {
  const [allInventory, setAllInventory] = useState<InventoryData[]>([]);
  const [products, setProducts] = useState<ProductData[]>([]);
  const [loading, setLoading] = useState(true);
  const [isUserOperator, setIsUserOperator] = useState(false);

  // Pagination state
  const [page, setPage] = useState(0);
  const pageSize = 10;

  // Filters (client-side)
  const [searchTerm, setSearchTerm] = useState("");
  const [filterBarcode, setFilterBarcode] = useState("");

  useEffect(() => {
    setIsUserOperator(isOperator());
    loadAllData();
  }, []);

  async function loadAllData() {
    setLoading(true);
    try {
      // Load ALL products for barcode mapping
      let allProducts: ProductData[] = [];
      let productPage = 0;
      const productPageSize = 100;
      let productHasMore = true;

      while (productHasMore) {
        const form: ProductSearchForm = { page: productPage, pageSize: productPageSize };
        try {
          const data = await apiPost<PagedResponse<ProductData>>("/products/list", form);
          allProducts = [...allProducts, ...data.data];
          productHasMore = (productPage + 1) * productPageSize < data.total;
          productPage++;
        } catch (e) {
          break;
        }
      }
      setProducts(allProducts);

      // Load ALL inventory for client-side filtering and pagination
      let allInventoryData: InventoryData[] = [];
      let invPage = 0;
      const invPageSize = 100;
      let invHasMore = true;

      while (invHasMore) {
        const invForm: InventorySearchForm = { page: invPage, pageSize: invPageSize };
        try {
          const data = await apiPost<PagedResponse<InventoryData>>("/inventory/list", invForm);
          allInventoryData = [...allInventoryData, ...data.data];
          invHasMore = (invPage + 1) * invPageSize < data.total;
          invPage++;
        } catch (e) {
          break;
        }
      }
      setAllInventory(allInventoryData);
      
      // Reset to first page when reloading
      setPage(0);
    } catch (error: any) {
      console.warn("Failed to load data: " + error.message);
    } finally {
      setLoading(false);
    }
  }

  // Reset page when filters change
  useEffect(() => {
    setPage(0);
  }, [searchTerm, filterBarcode]);

  // Client-side filtering and pagination
  const { filteredInventory, paginatedInventory, totalElements, barcodeMap } = useMemo(() => {
    // Create a barcode map for quick lookup
    const map = new Map<number, string>();
    products.forEach((p) => {
      map.set(p.id, p.barcode);
    });

    const filtered = allInventory.filter((i) => {
      const matchesSearch =
        !searchTerm ||
        i.productName.toLowerCase().includes(searchTerm.toLowerCase());

      const barcode = map.get(i.productId) || "";
      const matchesBarcode =
        !filterBarcode ||
        barcode.toLowerCase().includes(filterBarcode.toLowerCase());

      return matchesSearch && matchesBarcode;
    });

    const total = filtered.length;
    const start = page * pageSize;
    const end = start + pageSize;
    const paginated = filtered.slice(start, end);

    return { 
      filteredInventory: filtered, 
      paginatedInventory: paginated, 
      totalElements: total,
      barcodeMap: map
    };
  }, [allInventory, searchTerm, filterBarcode, page, products]);

  const totalPages = Math.ceil(totalElements / pageSize);

  function clearFilters() {
    setSearchTerm("");
    setFilterBarcode("");
  }

async function updateInventoryQuantity(productId: number, quantity: number) {
    await apiPost("/inventory", { productId, quantity });
    loadAllData();
    toast.success("Inventory updated successfully");
  }

  async function createInventory(productId: number, quantity: number) {
    await apiPost("/inventory", { productId, quantity });
    loadAllData();
    toast.success("Inventory added successfully");
  }

  async function uploadInventoryTsv(file: File) {
    const formData = new FormData();
    formData.append("file", file);

    const res = await fetch("http://localhost:8080/inventory/upload/tsv", {
      method: "POST",
      body: formData,
    });

    if (!res.ok) {
      const text = await res.text();
      throw new Error(text || `HTTP error! status: ${res.status}`);
    }

    // Reload inventory
    loadAllData();
  }

  return (
    <AuthGuard>
      <div className="min-h-[calc(100vh-64px)] bg-slate-50 p-4">
        <div className="max-w-[1400px] mx-auto">
          <h1 className="mb-4 text-2xl font-bold text-slate-800">
            Inventory
          </h1>

          {/* Hide TSV upload section for OPERATORs */}
          {!isUserOperator && (
            <div className="p-4 mb-4 bg-white rounded-lg shadow-sm">
              <InventoryTsvUpload onUpload={uploadInventoryTsv} />
              <div className="mt-3">
                <AddInventory products={products} onAdd={createInventory} />
              </div>
            </div>
          )}

          {/* Filters */}
          <div className="p-4 mb-4 bg-white rounded-lg shadow-sm">
            <div className="grid grid-cols-1 gap-3 sm:grid-cols-[180px_180px_auto]">
              <div>
                <label className="block mb-1.5 text-xs font-medium text-gray-700">
                  Search Product Name
                </label>
                <input
                  type="text"
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  placeholder="Search products..."
                  className="w-full px-3 py-2 text-sm border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                />
              </div>

              <div>
                <label className="block mb-1.5 text-xs font-medium text-gray-700">
                  Search by Barcode
                </label>
                <input
                  type="text"
                  value={filterBarcode}
                  onChange={(e) => setFilterBarcode(e.target.value)}
                  placeholder="Enter barcode..."
                  className="w-full px-3 py-2 text-sm border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                />
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
          </div>

          {loading && !allInventory.length ? (
            <div className="py-8 text-center text-slate-500">Loading...</div>
          ) : (
            <div className="p-4 bg-white rounded-lg shadow-sm overflow-x-auto">
              <table className="w-full border-collapse">
                <thead>
                  <tr className="border-b-2 border-gray-200">
                    <th className="px-2 py-2.5 text-xs font-semibold text-left text-gray-700">Product ID</th>
                    <th className="px-2 py-2.5 text-xs font-semibold text-left text-gray-700">Product Name</th>
                    <th className="px-2 py-2.5 text-xs font-semibold text-left text-gray-700">Barcode</th>
                    <th className="px-2 py-2.5 text-xs font-semibold text-left text-gray-700">Quantity</th>
                    {!isUserOperator && (
                      <th className="px-2 py-2.5 text-xs font-semibold text-left text-gray-700">Actions</th>
                    )}
                  </tr>
                </thead>
                <tbody>
                  {paginatedInventory.length === 0 ? (
                    <tr>
                      <td colSpan={isUserOperator ? 4 : 5} className="py-10 text-center text-slate-500">
                        No inventory found
                      </td>
                    </tr>
                  ) : (
                    paginatedInventory.map((i) => (
                      <tr key={i.productId} className="border-b border-gray-100">
                        <td className="px-2 py-2.5 text-sm">{i.productId}</td>
                        <td className="px-2 py-2.5 text-sm">{i.productName}</td>
                        <td className="px-2 py-2.5 text-sm">{barcodeMap.get(i.productId) || '-'}</td>
                        <td className="px-2 py-2.5 text-sm">{i.quantity}</td>
                        {!isUserOperator && (
                          <td className="px-2 py-2.5">
                            <UpdateInventory
                              productId={i.productId}
                              initialQuantity={i.quantity}
                              onUpdate={updateInventoryQuantity}
                            />
                          </td>
                        )}
                      </tr>
                    ))
                  )}
                </tbody>
              </table>

              {/* Pagination */}
              {totalElements > 0 && (
                <div className="flex items-center justify-between mt-4 pt-4 border-t border-gray-200">
                  <div className="text-sm text-slate-500">
                    Showing {paginatedInventory.length} of {totalElements} inventory items
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
            </div>
          )}
        </div>
      </div>
    </AuthGuard>
  );
}


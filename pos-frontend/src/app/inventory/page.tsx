"use client";

import { useState, useEffect, useMemo, useRef } from "react";
import { apiGet, apiPost } from "@/lib/api";
import { InventoryData, ProductData, PagedResponse, InventorySearchForm } from "@/lib/types";
import UpdateInventory from "@/components/UpdateInventory";
import AddInventoryModal from "@/components/AddInventoryModal";
import AuthGuard, { isOperator } from "@/components/AuthGuard";
import toast from "react-hot-toast";

export default function InventoryPage() {
  const [inventory, setInventory] = useState<InventoryData[]>([]);
  const [products, setProducts] = useState<ProductData[]>([]);
  const [loading, setLoading] = useState(true);
  const [isUserOperator, setIsUserOperator] = useState(false);
  const [showAddModal, setShowAddModal] = useState(false);

  // Pagination state
  const [page, setPage] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const pageSize = 10;

  // Filters
  const [searchTerm, setSearchTerm] = useState("");
  const [filterBarcode, setFilterBarcode] = useState("");
  const [searchTriggered, setSearchTriggered] = useState(false);
  
  // Track if data is already loaded to prevent duplicate calls
  const loadedRef = useRef(false);

  useEffect(() => {
    setIsUserOperator(isOperator());
  }, []);

  // Load products once for barcode lookup
  useEffect(() => {
    let mounted = true;
    async function loadProducts() {
      try {
        const data = await apiGet<ProductData[]>("/products");
        if (mounted) {
          setProducts(data);
        }
      } catch (error: any) {
        if (mounted) {
          console.warn("Failed to load products: " + error.message);
        }
      }
    }
    loadProducts();
    return () => { mounted = false; };
  }, []);

  // Load inventory when page or search is triggered
  useEffect(() => {
    let mounted = true;
    
    // Only skip if already loaded initial data and no search was triggered
    if (loadedRef.current && !searchTriggered) {
      setLoading(false);
      return;
    }
    
    async function loadInventory() {
      setLoading(true);
      try {
        const form: InventorySearchForm = {
          page,
          pageSize,
          productName: searchTerm || undefined,
          barcode: filterBarcode || undefined,
        };

        const data = await apiPost<PagedResponse<InventoryData>>("/inventory/list", form);
        if (mounted) {
          setInventory(data.data);
          setTotalElements(data.total);
          setSearchTriggered(false); // Reset after search
        }
      } catch (error: any) {
        if (mounted) {
          console.warn("Failed to load inventory: " + error.message);
        }
      } finally {
        if (mounted) {
          setLoading(false);
        }
      }
    }
    loadInventory();
    return () => { mounted = false; };
  }, [page, searchTriggered]);

  // Create barcode map for quick lookup
  const barcodeMap = useMemo(() => {
    const map = new Map<number, string>();
    products.forEach((p) => {
      map.set(p.id, p.barcode);
    });
    return map;
  }, [products]);

  const totalPages = Math.ceil(totalElements / pageSize);

  function clearFilters() {
    setSearchTerm("");
    setFilterBarcode("");
    setPage(0);
    setSearchTriggered(true);
  }

  async function updateInventoryQuantity(productId: number, quantity: number) {
    await apiPost("/inventory", { productId, quantity });
    setLoading(true);
    try {
      const form: InventorySearchForm = {
        page,
        pageSize,
        productName: searchTerm || undefined,
        barcode: filterBarcode || undefined,
      };
      const data = await apiPost<PagedResponse<InventoryData>>("/inventory/list", form);
      setInventory(data.data);
      setTotalElements(data.total);
    } catch (error: any) {
      toast.error("Failed to reload inventory: " + error.message);
    } finally {
      setLoading(false);
    }
    toast.success("Inventory updated successfully");
  }

  async function createInventory(productId: number, quantity: number) {
    await apiPost("/inventory", { productId, quantity });
    setLoading(true);
    try {
      const form: InventorySearchForm = {
        page,
        pageSize,
        productName: searchTerm || undefined,
        barcode: filterBarcode || undefined,
      };
      const data = await apiPost<PagedResponse<InventoryData>>("/inventory/list", form);
      setInventory(data.data);
      setTotalElements(data.total);
    } catch (error: any) {
      toast.error("Failed to reload inventory: " + error.message);
    } finally {
      setLoading(false);
    }
    toast.success("Inventory added successfully");
  }

  async function uploadInventoryTsv(file: File) {
    const formData = new FormData();
    formData.append("file", file);

    const res = await fetch("http://localhost:8080/inventory/upload/tsv", {
      method: "POST",
      body: formData,
    });

    if (res.ok) {
      const data = await res.json();
      const count = data.length || 0;
      setPage(0);
      setLoading(true);
      try {
        const form: InventorySearchForm = {
          page: 0,
          pageSize,
          productName: "",
          barcode: "",
        };
        const data = await apiPost<PagedResponse<InventoryData>>("/inventory/list", form);
        setInventory(data.data);
        setTotalElements(data.total);
      } catch (error: any) {
        toast.error("Failed to reload inventory: " + error.message);
      } finally {
        setLoading(false);
      }
      toast.success(`${count} inventory record${count !== 1 ? 's' : ''} uploaded successfully`);
    } else if (res.status === 400) {
      const blob = await res.blob();
      const filename = res.headers.get("Content-Disposition")?.match(/filename="(.+)"/)?.[1] || "inventory-upload-errors.tsv";
      
      const text = await blob.text();
      const errorLines = text.split('\n').filter(line => line.trim());
      
      const errors = errorLines.slice(1).map(line => {
        const parts = line.split('\t');
        if (parts.length >= 3) {
          const rowNum = parts[0];
          const originalData = parts[1];
          const errorMsg = parts[2];
          return { rowNum, originalData, errorMsg };
        }
        return null;
      }).filter(Boolean);

      const url = window.URL.createObjectURL(blob);
      const a = document.createElement("a");
      a.href = url;
      a.download = filename;
      document.body.appendChild(a);
      a.click();
      window.URL.revokeObjectURL(url);
      document.body.removeChild(a);

      if (errors.length > 0) {
        throw new Error(`${errors.length} error(s) found. Error file downloaded.`);
      } else {
        throw new Error("Upload failed with errors. Error file downloaded.");
      }
    } else {
      const text = await res.text();
      throw new Error(text || `HTTP error! status: ${res.status}`);
    }
  }

  return (
    <AuthGuard>
      <div className="min-h-[calc(100vh-64px)] bg-slate-50 p-4">
        <div className="max-w-[1400px] mx-auto">
          <div className="flex items-center justify-between mb-4">
            <h1 className="text-2xl font-bold text-slate-800">
              Inventory
            </h1>
            {!isUserOperator && (
              <button
                onClick={() => setShowAddModal(true)}
                className="px-4 py-2 text-sm font-medium text-white bg-blue-500 rounded-md hover:bg-blue-600 transition-colors cursor-pointer"
              >
                + Add Inventory
              </button>
            )}
          </div>

          <div className="p-3 sm:p-4 mb-4 bg-white rounded-lg shadow-sm">
            {/* Mobile-first: stacked layout, becomes grid on sm+ */}
            <div className="grid grid-cols-1 gap-3 sm:grid-cols-[1fr_1fr_120px] lg:grid-cols-[200px_200px_120px]">
              <div>
                <label className="block mb-1.5 text-xs font-medium text-gray-700">
                  Search Product Name
                </label>
                <input
                  type="text"
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  placeholder="Search products..."
                  className="w-full px-3 py-2 text-sm border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent h-[42px]"
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
                  className="w-full px-3 py-2 text-sm border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent h-[42px]"
                />
              </div>

              <div className="flex items-end gap-2">
                <button
                  onClick={() => {
                    setPage(0);
                    setSearchTriggered(true);
                  }}
                  className="flex-1 px-4 py-2 text-sm font-medium text-white bg-blue-500 rounded-md hover:bg-blue-600 transition-colors cursor-pointer h-[42px]"
                >
                  Search
                </button>
                <button
                  onClick={clearFilters}
                  className="flex-1 px-4 py-2 text-sm font-medium text-white bg-gray-500 rounded-md hover:bg-gray-600 transition-colors cursor-pointer h-[42px]"
                >
                  Clear
                </button>
              </div>
            </div>
          </div>

          {loading && !inventory.length ? (
            <div className="py-8 text-center text-slate-500">Loading...</div>
          ) : (
            <div className="p-4 bg-white rounded-lg shadow-sm overflow-x-auto">
              <table className="w-full border-collapse">
                <thead>
                  <tr className="border-b-2 border-gray-200">
                    <th className="px-2 py-2.5 text-xs font-semibold text-left text-gray-700">Product ID</th>
                    <th className="px-2 py-2.5 text-xs font-semibold text-left text-gray-700">Product Name</th>
                    <th className="px-2 py-2.5 text-xs font-semibold text-left text-gray-700">Barcode</th>
                    <th className="px-2 py-2.5 text-xs font-semibold text-left text-gray-700">Available Quantity</th>
                    {!isUserOperator && (
                      <th className="px-2 py-2.5 text-xs font-semibold text-left text-gray-700">Actions</th>
                    )}
                  </tr>
                </thead>
                <tbody>
                  {inventory.length === 0 ? (
                    <tr>
                      <td colSpan={isUserOperator ? 4 : 5} className="py-10 text-center text-slate-500">
                        No inventory found
                      </td>
                    </tr>
                  ) : (
                    inventory.map((i) => (
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

              {totalElements > 0 && (
                <div className="flex items-center justify-between mt-4 pt-4 border-t border-gray-200">
                  <div className="text-sm text-slate-500">
                    Showing {inventory.length} of {totalElements} inventory items
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

          {showAddModal && (
            <AddInventoryModal
              products={products}
              onClose={() => setShowAddModal(false)}
              onAdd={createInventory}
              onUploadTsv={uploadInventoryTsv}
            />
          )}
        </div>
      </div>
    </AuthGuard>
  );
}


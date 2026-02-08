"use client";

import { useState, useEffect, useMemo, useRef } from "react";
import { apiGet, apiPost, apiPut } from "@/lib/api";
import { ProductData, InventoryData, ClientData, PagedResponse, ProductSearchForm } from "@/lib/types";
import AddProductModal from "@/components/AddProductModal";
import EditProductModal from "@/components/EditProductModal";
import AuthGuard, { isOperator } from "@/components/AuthGuard";
import ClientSelect from "@/components/ClientSelect";
import toast from "react-hot-toast";

export default function ProductsPage() {
  const [products, setProducts] = useState<ProductData[]>([]);
  const [clients, setClients] = useState<ClientData[]>([]);
  const [inventory, setInventory] = useState<InventoryData[]>([]);
  const [loading, setLoading] = useState(true);
  const [editingProduct, setEditingProduct] = useState<ProductData | null>(null);
  const [isUserOperator, setIsUserOperator] = useState(false);
  const [showAddModal, setShowAddModal] = useState(false);

  // Pagination state
  const [page, setPage] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const pageSize = 10;

  // Filter state
  const [searchTerm, setSearchTerm] = useState("");
  const [filterClientId, setFilterClientId] = useState<number | null>(null);
  const [filterBarcode, setFilterBarcode] = useState("");

  // Track if data is already loaded to prevent duplicate calls
  const loadedRef = useRef({
    clients: false,
    inventory: false,
    products: false,
  });

  useEffect(() => {
    setIsUserOperator(isOperator());
  }, []);

  // Load clients once
  useEffect(() => {
    if (loadedRef.current.clients) return;
    loadedRef.current.clients = true;
    
    let mounted = true;
    async function loadClients() {
      try {
        const data = await apiGet<ClientData[]>("/clients");
        if (mounted) {
          setClients(data);
        }
      } catch (error: any) {
        if (mounted) {
          console.warn("Failed to load clients: " + error.message);
        }
      }
    }
    loadClients();
    return () => { mounted = false; };
  }, []);

  // Load inventory once
  useEffect(() => {
    if (loadedRef.current.inventory) return;
    loadedRef.current.inventory = true;
    
    let mounted = true;
    async function loadInventory() {
      try {
        const data = await apiGet<InventoryData[]>("/inventory");
        if (mounted) {
          setInventory(data);
        }
      } catch (error: any) {
        if (mounted) {
          console.warn("Failed to load inventory: " + error.message);
        }
      }
    }
    loadInventory();
    return () => { mounted = false; };
  }, []);

  // Load products when page or filters change
  useEffect(() => {
    let mounted = true;
    
    // Skip if already loaded initial data and no filter changes
    if (loadedRef.current.products && page === 0 && !searchTerm && !filterClientId && !filterBarcode) {
      setLoading(false);
      return;
    }
    
    async function loadProducts() {
      setLoading(true);
      try {
        const form: ProductSearchForm = {
          page,
          pageSize,
          productName: searchTerm || undefined,
          barcode: filterBarcode || undefined,
          clientId: filterClientId || undefined,
        };

        const data = await apiPost<PagedResponse<ProductData>>("/products/list", form);
        if (mounted) {
          setProducts(data.data);
          setTotalElements(data.total);
          // Mark as loaded only for initial load (page 0, no filters)
          if (page === 0 && !searchTerm && !filterClientId && !filterBarcode) {
            loadedRef.current.products = true;
          }
        }
      } catch (error: any) {
        if (mounted) {
          toast.error("Failed to load products: " + error.message);
        }
      } finally {
        if (mounted) {
          setLoading(false);
        }
      }
    }
    loadProducts();
    return () => { mounted = false; };
  }, [page, searchTerm, filterClientId, filterBarcode]);

  // Create inventory map for quick lookup
  const inventoryMap = useMemo(() => {
    const map = new Map<number, number>();
    inventory.forEach((inv) => {
      map.set(inv.productId, inv.quantity);
    });
    return map;
  }, [inventory]);

  const totalPages = Math.ceil(totalElements / pageSize);

  function clearFilters() {
    setSearchTerm("");
    setFilterClientId(null);
    setFilterBarcode("");
    setPage(0);
  }

  async function addProduct(product: {
    productName: string;
    barcode: string;
    mrp: number;
    clientId: number;
    imageUrl?: string;
  }) {
    await apiPost("/products", product);
    setPage(0);
    loadedRef.current.products = false; // Allow reload
    // Trigger reload
    setLoading(true);
    try {
      const form: ProductSearchForm = {
        page: 0,
        pageSize,
        productName: "",
        barcode: "",
        clientId: null,
      };
      const data = await apiPost<PagedResponse<ProductData>>("/products/list", form);
      setProducts(data.data);
      setTotalElements(data.total);
      loadedRef.current.products = true;
    } catch (error: any) {
      toast.error("Failed to reload products: " + error.message);
    } finally {
      setLoading(false);
    }
    toast.success("Product created successfully");
  }

  async function updateProduct(
    id: number,
    product: {
      productName: string;
      barcode: string;
      mrp: number;
      clientId: number;
      imageUrl?: string;
    }
  ) {
    await apiPut(`/products/${id}`, product);
    // Reload current page
    setLoading(true);
    try {
      const form: ProductSearchForm = {
        page,
        pageSize,
        productName: searchTerm || undefined,
        barcode: filterBarcode || undefined,
        clientId: filterClientId || undefined,
      };
      const data = await apiPost<PagedResponse<ProductData>>("/products/list", form);
      setProducts(data.data);
      setTotalElements(data.total);
    } catch (error: any) {
      toast.error("Failed to reload products: " + error.message);
    } finally {
      setLoading(false);
    }
    toast.success("Product updated successfully");
  }

  async function uploadProductTsv(file: File) {
    const formData = new FormData();
    formData.append("file", file);

    const res = await fetch("http://localhost:8080/products/upload/tsv", {
      method: "POST",
      body: formData,
    });

    if (res.ok) {
      const data = await res.json();
      const count = data.length || 0;
      setPage(0);
      loadedRef.current.products = false; // Allow reload
      setLoading(true);
      try {
        const form: ProductSearchForm = {
          page: 0,
          pageSize,
          productName: "",
          barcode: "",
          clientId: null,
        };
        const data = await apiPost<PagedResponse<ProductData>>("/products/list", form);
        setProducts(data.data);
        setTotalElements(data.total);
        loadedRef.current.products = true;
      } catch (error: any) {
        toast.error("Failed to reload products: " + error.message);
      } finally {
        setLoading(false);
      }
      toast.success(`${count} product${count !== 1 ? 's' : ''} uploaded successfully`);
    } else if (res.status === 400) {
      const blob = await res.blob();
      const filename = res.headers.get("Content-Disposition")?.match(/filename="(.+)"/)?.[1] || "products-upload-errors.tsv";
      
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
        toast.error(`${errors.length} error(s) found. Error file downloaded.`);
      } else {
        toast.error("Upload failed with errors. Error file downloaded.");
        return;
      }
    } else {
      const text = await res.text();
      toast.error(text || `HTTP error! status: ${res.status}`);
    }
  }

  return (
    <AuthGuard>
      <div className="min-h-[calc(100vh-64px)] bg-slate-50 p-4">
        <div className="max-w-[1400px] mx-auto">
          <div className="flex items-center justify-between mb-4">
            <h1 className="text-2xl font-bold text-slate-800">
              Products
            </h1>
            {!isUserOperator && (
              <button
                onClick={() => setShowAddModal(true)}
                className="px-4 py-2 text-sm font-medium text-white bg-blue-500 rounded-md hover:bg-blue-600 transition-colors cursor-pointer"
              >
                + Add Product
              </button>
            )}
          </div>

          <div className="p-4 mb-4 bg-white rounded-lg shadow-sm">
            <div className="grid grid-cols-1 gap-3 sm:grid-cols-[180px_minmax(180px,280px)_180px_auto] max-w-6xl">
              <div>
                <label className="block mb-1.5 text-xs font-medium text-gray-700">
                  Search Name
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
                  Filter by Client
                </label>
                <ClientSelect
                  clients={clients}
                  value={filterClientId}
                  onChange={setFilterClientId}
                  placeholder="Search client..."
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

          {loading ? (
            <div className="py-8 text-center text-slate-500">Loading...</div>
          ) : (
            <div className="p-4 bg-white rounded-lg shadow-sm overflow-x-auto">
              <table className="w-full border-collapse">
                <thead>
                  <tr className="border-b-2 border-gray-200">
                    <th className="px-2 py-2.5 text-xs font-semibold text-left text-gray-700">ID</th>
                    <th className="px-2 py-2.5 text-xs font-semibold text-left text-gray-700">Image</th>
                    <th className="px-2 py-2.5 text-xs font-semibold text-left text-gray-700">Product Name</th>
                    <th className="px-2 py-2.5 text-xs font-semibold text-left text-gray-700">Barcode</th>
                    <th className="px-2 py-2.5 text-xs font-semibold text-left text-gray-700">MRP</th>
                    <th className="px-2 py-2.5 text-xs font-semibold text-left text-gray-700">Client</th>
                    <th className="px-2 py-2.5 text-xs font-semibold text-left text-gray-700">Inventory</th>
                    {!isUserOperator && (
                      <th className="px-2 py-2.5 text-xs font-semibold text-left text-gray-700">Actions</th>
                    )}
                  </tr>
                </thead>
                <tbody>
                  {products.length === 0 ? (
                    <tr>
                      <td colSpan={isUserOperator ? 7 : 8} className="py-10 text-center text-slate-500">
                        No products found
                      </td>
                    </tr>
                  ) : (
                    products.map((p) => {
                      const client = clients.find((c) => c.id === p.clientId);
                      return (
                        <tr key={p.id} className="border-b border-gray-100">
                          <td className="px-2 py-2.5 text-sm">{p.id}</td>
                          <td className="px-2 py-2.5">
                            {p.imageUrl ? (
                              <img
                                src={p.imageUrl}
                                alt={p.productName}
                                className="w-10 h-10 object-cover border border-gray-200 rounded-md"
                                onError={(e) => {
                                  (e.target as HTMLImageElement).src =
                                    "https://placehold.co/40x40/e5e7eb/9ca3af?text=No+Img";
                                }}
                              />
                            ) : (
                              <img
                                src="https://placehold.co/40x40/e5e7eb/9ca3af?text=No+Img"
                                alt="No image"
                                className="w-10 h-10 object-cover border border-gray-200 rounded-md"
                              />
                            )}
                          </td>
                          <td className="px-2 py-2.5 text-sm">{p.productName}</td>
                          <td className="px-2 py-2.5 text-sm">{p.barcode}</td>
                          <td className="px-2 py-2.5 text-sm">₹{Number(p.mrp).toFixed(2)}</td>
                          <td className="px-2 py-2.5 text-sm">
                            {client?.clientName || `Client ${p.clientId}`}
                          </td>
                          <td className="px-2 py-2.5 text-sm">
                            {inventoryMap.get(p.id) ?? 0}
                          </td>
                          {!isUserOperator && (
                            <td className="px-2 py-2.5">
                              <button
                                onClick={() => setEditingProduct(p)}
                                className="px-3 py-1.5 text-xs text-white bg-blue-500 rounded-md hover:bg-blue-600 transition-colors cursor-pointer"
                              >
                                Edit
                              </button>
                            </td>
                          )}
                        </tr>
                      );
                    })
                  )}
                </tbody>
              </table>

              {totalElements > 0 && (
                <div className="flex items-center justify-between mt-4 pt-4 border-t border-gray-200">
                  <div className="text-sm text-slate-500">
                    Showing {products.length} of {totalElements} products
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
            <AddProductModal
              clients={clients}
              onClose={() => setShowAddModal(false)}
              onAdd={addProduct}
              onUploadTsv={uploadProductTsv}
            />
          )}

          {editingProduct && (
            <EditProductModal
              product={editingProduct}
              clients={clients}
              inventory={
                inventory.find(
                  (inv) => inv.productId === editingProduct.id
                ) || null
              }
              onClose={() => setEditingProduct(null)}
              onUpdate={updateProduct}
            />
          )}
        </div>
      </div>
    </AuthGuard>
  );
}


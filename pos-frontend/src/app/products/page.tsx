"use client";

import { useState, useEffect, useMemo } from "react";
import { apiGet, apiPost, apiPut } from "@/lib/api";
import { ProductData, InventoryData, ClientData, PagedResponse, ProductSearchForm, InventorySearchForm } from "@/lib/types";
import AddProduct from "@/components/AddProduct";
import ProductTsvUpload from "@/components/ProductTsvUpload";
import EditProductModal from "@/components/EditProductModal";
import AuthGuard, { isOperator } from "@/components/AuthGuard";
import ClientSelect from "@/components/ClientSelect";
import toast from "react-hot-toast";

export default function ProductsPage() {
  const [allProducts, setAllProducts] = useState<ProductData[]>([]);
  const [clients, setClients] = useState<ClientData[]>([]);
  const [inventory, setInventory] = useState<InventoryData[]>([]);
  const [loading, setLoading] = useState(true);
  const [editingProduct, setEditingProduct] = useState<ProductData | null>(null);
  const [isUserOperator, setIsUserOperator] = useState(false);

  // Pagination state
  const [page, setPage] = useState(0);
  const pageSize = 10;

  // Filter state (client-side)
  const [searchTerm, setSearchTerm] = useState("");
  const [filterClientId, setFilterClientId] = useState<number | null>(null);
  const [filterBarcode, setFilterBarcode] = useState("");

  useEffect(() => {
    setIsUserOperator(isOperator());
    loadAllData();
  }, []);

  async function loadAllData() {
    setLoading(true);
    try {
      // Load ALL clients first (needed for dropdowns and display)
      const clientsData = await apiGet<ClientData[]>("/clients");
      setClients(clientsData);

      // Load ALL products for client-side filtering and pagination
      let allProductsData: ProductData[] = [];
      let productPage = 0;
      const productPageSize = 100;
      let productHasMore = true;

      while (productHasMore) {
        const form: ProductSearchForm = { page: productPage, pageSize: productPageSize };
        try {
          const data = await apiPost<PagedResponse<ProductData>>("/products/list", form);
          allProductsData = [...allProductsData, ...data.data];
          productHasMore = (productPage + 1) * productPageSize < data.total;
          productPage++;
        } catch (e) {
          break;
        }
      }
      setAllProducts(allProductsData);

      // Load ALL inventory for mapping
      let allInventory: InventoryData[] = [];
      let invPage = 0;
      const invPageSize = 100;
      let invHasMore = true;

      while (invHasMore) {
        const invForm: InventorySearchForm = { page: invPage, pageSize: invPageSize };
        try {
          const invData = await apiPost<PagedResponse<InventoryData>>("/inventory/list", invForm);
          allInventory = [...allInventory, ...invData.data];
          invHasMore = (invPage + 1) * invPageSize < invData.total;
          invPage++;
        } catch (e) {
          break;
        }
      }
      setInventory(allInventory);
      
      // Reset to first page when reloading
      setPage(0);
    } catch (error: any) {
      toast.error("Failed to load data: " + error.message);
    } finally {
      setLoading(false);
    }
  }

  // Reset page when filters change
  useEffect(() => {
    setPage(0);
  }, [searchTerm, filterClientId, filterBarcode]);

  // Client-side filtering and pagination
  const { filteredProducts, paginatedProducts, totalElements, inventoryMap } = useMemo(() => {
    const filtered = allProducts.filter((p) => {
      const matchesSearch =
        !searchTerm ||
        p.productName.toLowerCase().includes(searchTerm.toLowerCase()) ||
        p.barcode.toLowerCase().includes(searchTerm.toLowerCase());

      const matchesClient = filterClientId === null || p.clientId === filterClientId;

      const matchesBarcode =
        !filterBarcode ||
        p.barcode.toLowerCase().includes(filterBarcode.toLowerCase());

      return matchesSearch && matchesClient && matchesBarcode;
    });

    const total = filtered.length;
    const start = page * pageSize;
    const end = start + pageSize;
    const paginated = filtered.slice(start, end);

    // Create inventory map
    const map = new Map<number, number>();
    inventory.forEach((inv) => {
      map.set(inv.productId, inv.quantity);
    });

    return { 
      filteredProducts: filtered, 
      paginatedProducts: paginated, 
      totalElements: total,
      inventoryMap: map 
    };
  }, [allProducts, searchTerm, filterClientId, filterBarcode, page, inventory]);

  const totalPages = Math.ceil(totalElements / pageSize);

  function clearFilters() {
    setSearchTerm("");
    setFilterClientId(null);
    setFilterBarcode("");
  }

async function addProduct(product: {
    productName: string;
    barcode: string;
    mrp: number;
    clientId: number;
  }) {
    await apiPost("/products", product);
    loadAllData();
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
    loadAllData();
    toast.success("Product updated successfully");
  }

async function updateInventory(productId: number, quantity: number) {
    await apiPost("/inventory", { productId, quantity });
    // Reload inventory
    let allInventory: InventoryData[] = [];
    let page = 0;
    const pageSize = 100;
    let hasMore = true;

    while (hasMore) {
      const form: InventorySearchForm = { page, pageSize };
      const data = await apiPost<PagedResponse<InventoryData>>("/inventory/list", form);
      allInventory = [...allInventory, ...data.data];
      hasMore = (page + 1) * pageSize < data.total;
      page++;
    }
    setInventory(allInventory);
    toast.success("Inventory updated successfully");
  }

  async function uploadProductTsv(file: File) {
    const formData = new FormData();
    formData.append("file", file);

    const res = await fetch("http://localhost:8080/products/upload/tsv", {
      method: "POST",
      body: formData,
    });

    if (!res.ok) {
      const text = await res.text();
      throw new Error(text || `HTTP error! status: ${res.status}`);
    }

    // Reload products
    loadAllData();
  }

  return (
    <AuthGuard>
      <div className="min-h-[calc(100vh-64px)] bg-slate-50 p-4">
        <div className="max-w-[1400px] mx-auto">
          <h1 className="mb-4 text-2xl font-bold text-slate-800">
            Products
          </h1>

          {/* Hide TSV upload section for OPERATORs */}
          {!isUserOperator && (
            <div className="p-4 mb-4 bg-white rounded-lg shadow-sm">
              <ProductTsvUpload onUpload={uploadProductTsv} />
              <div className="mt-3">
                <AddProduct clients={clients} onAdd={addProduct} />
              </div>
            </div>
          )}

          {/* Filters */}
          <div className="p-4 mb-4 bg-white rounded-lg shadow-sm">
            <div className="grid grid-cols-1 gap-3 sm:grid-cols-[180px_1fr_180px_auto]">
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

          {/* Products Table */}
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
                  {paginatedProducts.length === 0 ? (
                    <tr>
                      <td colSpan={isUserOperator ? 7 : 8} className="py-10 text-center text-slate-500">
                        No products found
                      </td>
                    </tr>
                  ) : (
                    paginatedProducts.map((p) => {
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
                                  (e.target as HTMLImageElement).style.display =
                                    "none";
                                }}
                              />
                            ) : (
                              <div className="w-10 h-10 bg-gray-100 flex items-center justify-content-center text-xs text-gray-400 border border-gray-200 rounded-md">
                                No Image
                              </div>
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

              {/* Pagination */}
              {totalElements > 0 && (
                <div className="flex items-center justify-between mt-4 pt-4 border-t border-gray-200">
                  <div className="text-sm text-slate-500">
                    Showing {paginatedProducts.length} of {totalElements} products
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
              onUpdateInventory={updateInventory}
            />
          )}
        </div>
      </div>
    </AuthGuard>
  );
}


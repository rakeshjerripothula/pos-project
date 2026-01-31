"use client";

import { useState, useEffect, useMemo } from "react";
import { apiGet } from "@/lib/api";
import { ProductData, InventoryData, ClientData } from "@/lib/types";
import AddProduct from "@/components/AddProduct";
import ProductTsvUpload from "@/components/ProductTsvUpload";
import EditProductModal from "@/components/EditProductModal";
import AuthGuard, { isOperator } from "@/components/AuthGuard";

export default function ProductsPage() {
  const [products, setProducts] = useState<ProductData[]>([]);
  const [clients, setClients] = useState<ClientData[]>([]);
  const [inventory, setInventory] = useState<InventoryData[]>([]);
  const [loading, setLoading] = useState(true);
  const [editingProduct, setEditingProduct] = useState<ProductData | null>(null);
  const [isUserOperator, setIsUserOperator] = useState(false);

  // Filters
  const [searchTerm, setSearchTerm] = useState("");
  const [filterClientId, setFilterClientId] = useState<number | "">("");
  const [filterBarcode, setFilterBarcode] = useState("");

  useEffect(() => {
    setIsUserOperator(isOperator());
    loadData();
  }, []);

  async function loadData() {
    setLoading(true);
    try {
      const [productsData, clientsData, inventoryData] = await Promise.all([
        apiGet<ProductData[]>("/products"),
        apiGet<ClientData[]>("/clients"),
        apiGet<InventoryData[]>("/inventory"),
      ]);

      setProducts(productsData);
      setClients(clientsData);
      setInventory(inventoryData);
    } catch (error: any) {
      alert("Failed to load data: " + error.message);
    } finally {
      setLoading(false);
    }
  }

  const inventoryMap = useMemo(() => {
    const map = new Map<number, number>();
    inventory.forEach((inv) => {
      map.set(inv.productId, inv.quantity);
    });
    return map;
  }, [inventory]);

  const filteredProducts = useMemo(() => {
    return products.filter((p) => {
      const matchesSearch =
        !searchTerm ||
        p.productName.toLowerCase().includes(searchTerm.toLowerCase()) ||
        p.barcode.toLowerCase().includes(searchTerm.toLowerCase());

      const matchesClient = !filterClientId || p.clientId === filterClientId;

      const matchesBarcode =
        !filterBarcode ||
        p.barcode.toLowerCase().includes(filterBarcode.toLowerCase());

      return matchesSearch && matchesClient && matchesBarcode;
    });
  }, [products, searchTerm, filterClientId, filterBarcode]);

  async function addProduct(product: {
    productName: string;
    barcode: string;
    mrp: number;
    clientId: number;
  }) {
    const res = await fetch("http://localhost:8080/products", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(product),
    });

    if (!res.ok) {
      throw new Error(await res.text());
    }

    await loadData();
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
    const res = await fetch(`http://localhost:8080/products/${id}`, {
      method: "PUT",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(product),
    });

    if (!res.ok) {
      throw new Error(await res.text());
    }

    await loadData();
  }

  async function updateInventory(productId: number, quantity: number) {
    const res = await fetch("http://localhost:8080/inventory", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ productId, quantity }),
    });

    if (!res.ok) {
      throw new Error(await res.text());
    }

    await loadData();
  }

  async function uploadProductTsv(file: File) {
    const formData = new FormData();
    formData.append("file", file);

    const res = await fetch("http://localhost:8080/products/upload/tsv", {
      method: "POST",
      body: formData,
    });

    if (!res.ok) {
      throw new Error(await res.text());
    }

    await loadData();
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
          <h1
            style={{
              fontSize: 28,
              fontWeight: "bold",
              color: "#1e293b",
              marginBottom: 24,
            }}
          >
            Products
          </h1>

          <div
            style={{
              backgroundColor: "white",
              borderRadius: 12,
              padding: 24,
              boxShadow: "0 1px 3px rgba(0,0,0,0.1)",
              marginBottom: 24,
            }}
          >
            {/* Hide TSV upload for OPERATORs */}
            {!isUserOperator && <ProductTsvUpload onUpload={uploadProductTsv} />}
            <div style={{ marginTop: 16 }}>
              {/* Hide AddProduct for OPERATORs */}
              {!isUserOperator ? (
                <AddProduct clients={clients} onAdd={addProduct} />
              ) : (
                <div
                  style={{
                    padding: 16,
                    backgroundColor: "#f8fafc",
                    borderRadius: 8,
                    color: "#64748b",
                    fontSize: 14,
                  }}
                >
                  Viewing products in read-only mode. Contact a supervisor to add or edit products.
                </div>
              )}
            </div>
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
                  Search (Name/Barcode)
                </label>
                <input
                  type="text"
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  placeholder="Search products..."
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
                  Filter by Client
                </label>
                <select
                  value={filterClientId}
                  onChange={(e) =>
                    setFilterClientId(e.target.value ? Number(e.target.value) : "")
                  }
                  style={{
                    width: "100%",
                    padding: "10px 14px",
                    border: "1px solid #d1d5db",
                    borderRadius: 8,
                    fontSize: 14,
                  }}
                >
                  <option value="">All Clients</option>
                  {clients
                    .filter((c) => c.enabled)
                    .map((c) => (
                      <option key={c.id} value={c.id}>
                        {c.clientName}
                      </option>
                    ))}
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
                  Filter by Barcode
                </label>
                <input
                  type="text"
                  value={filterBarcode}
                  onChange={(e) => setFilterBarcode(e.target.value)}
                  placeholder="Enter barcode..."
                  style={{
                    width: "100%",
                    padding: "10px 14px",
                    border: "1px solid #d1d5db",
                    borderRadius: 8,
                    fontSize: 14,
                  }}
                />
              </div>

              <div style={{ display: "flex", alignItems: "flex-end" }}>
                <button
                  onClick={() => {
                    setSearchTerm("");
                    setFilterClientId("");
                    setFilterBarcode("");
                  }}
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
                  Clear Filters
                </button>
              </div>
            </div>
          </div>

          {/* Products Table */}
          {loading ? (
            <div style={{ textAlign: "center", padding: 48 }}>Loading...</div>
          ) : (
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
                      ID
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
                      Image
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
                      Barcode
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
                      MRP
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
                      Client
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
                      Inventory
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
                  {filteredProducts.length === 0 ? (
                    <tr>
                      <td
                        colSpan={8}
                        style={{
                          textAlign: "center",
                          padding: 48,
                          color: "#6b7280",
                        }}
                      >
                        No products found
                      </td>
                    </tr>
                  ) : (
                    filteredProducts.map((p) => {
                      const client = clients.find((c) => c.id === p.clientId);
                      return (
                        <tr
                          key={p.id}
                          style={{
                            borderBottom: "1px solid #e5e7eb",
                          }}
                        >
                          <td style={{ padding: "12px" }}>{p.id}</td>
                          <td style={{ padding: "12px" }}>
                            {p.imageUrl ? (
                              <img
                                src={p.imageUrl}
                                alt={p.productName}
                                style={{
                                  width: 50,
                                  height: 50,
                                  objectFit: "cover",
                                  border: "1px solid #e5e7eb",
                                  borderRadius: 6,
                                }}
                                onError={(e) => {
                                  (e.target as HTMLImageElement).style.display =
                                    "none";
                                }}
                              />
                            ) : (
                              <div
                                style={{
                                  width: 50,
                                  height: 50,
                                  backgroundColor: "#f3f4f6",
                                  display: "flex",
                                  alignItems: "center",
                                  justifyContent: "center",
                                  fontSize: 10,
                                  color: "#9ca3af",
                                  border: "1px solid #e5e7eb",
                                  borderRadius: 6,
                                }}
                              >
                                No Image
                              </div>
                            )}
                          </td>
                          <td style={{ padding: "12px" }}>{p.productName}</td>
                          <td style={{ padding: "12px" }}>{p.barcode}</td>
                          <td style={{ padding: "12px" }}>
                            ₹{Number(p.mrp).toFixed(2)}
                          </td>
                          <td style={{ padding: "12px" }}>
                            {client?.clientName || p.clientId}
                          </td>
                          <td style={{ padding: "12px" }}>
                            {inventoryMap.get(p.id) ?? 0}
                          </td>
                          <td style={{ padding: "12px" }}>
                            {/* Hide Edit button for OPERATORs */}
                            {!isUserOperator && (
                              <button
                                onClick={() => setEditingProduct(p)}
                                style={{
                                  padding: "6px 12px",
                                  backgroundColor: "#667eea",
                                  color: "white",
                                  border: "none",
                                  borderRadius: 6,
                                  cursor: "pointer",
                                  fontSize: 13,
                                  fontWeight: 500,
                                }}
                              >
                                Edit
                              </button>
                            )}
                            {isUserOperator && (
                              <span
                                style={{
                                  color: "#9ca3af",
                                  fontSize: 13,
                                }}
                              >
                                View only
                              </span>
                            )}
                          </td>
                        </tr>
                      );
                    })
                  )}
                </tbody>
              </table>
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

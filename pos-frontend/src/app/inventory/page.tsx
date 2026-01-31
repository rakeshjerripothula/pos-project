"use client";

import { useState, useEffect } from "react";
import { apiGet } from "@/lib/api";
import { InventoryData, ProductData } from "@/lib/types";
import UpdateInventory from "@/components/UpdateInventory";
import AddInventory from "@/components/AddInventory";
import InventoryTsvUpload from "@/components/InventoryTsvUpload";
import AuthGuard, { isOperator } from "@/components/AuthGuard";

export default function InventoryPage() {
  const [inventory, setInventory] = useState<InventoryData[]>([]);
  const [products, setProducts] = useState<ProductData[]>([]);
  const [loading, setLoading] = useState(true);
  const [isUserOperator, setIsUserOperator] = useState(false);

  useEffect(() => {
    setIsUserOperator(isOperator());
    loadData();
  }, []);

  async function loadData() {
    setLoading(true);
    try {
      const [inventoryData, productsData] = await Promise.all([
        apiGet<InventoryData[]>("/inventory"),
        apiGet<ProductData[]>("/products"),
      ]);
      setInventory(inventoryData);
      setProducts(productsData);
    } catch (error: any) {
      alert("Failed to load data: " + error.message);
    } finally {
      setLoading(false);
    }
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

  async function createInventory(productId: number, quantity: number) {
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

  async function uploadInventoryTsv(file: File) {
    const formData = new FormData();
    formData.append("file", file);

    const res = await fetch("http://localhost:8080/inventory/upload/tsv", {
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
            Inventory
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
            {!isUserOperator && <InventoryTsvUpload onUpload={uploadInventoryTsv} />}
            <div style={{ marginTop: 16 }}>
              {/* Hide AddInventory for OPERATORs */}
              {!isUserOperator ? (
                <AddInventory products={products} onAdd={createInventory} />
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
                  Viewing inventory in read-only mode. Contact a supervisor to update inventory.
                </div>
              )}
            </div>
          </div>

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
                      Product ID
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
                      Quantity
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
                  {inventory.length === 0 ? (
                    <tr>
                      <td
                        colSpan={4}
                        style={{
                          textAlign: "center",
                          padding: 48,
                          color: "#6b7280",
                        }}
                      >
                        No inventory found
                      </td>
                    </tr>
                  ) : (
                    inventory.map((i) => (
                      <tr
                        key={i.productId}
                        style={{
                          borderBottom: "1px solid #e5e7eb",
                        }}
                      >
                        <td style={{ padding: "12px" }}>{i.productId}</td>
                        <td style={{ padding: "12px" }}>{i.productName}</td>
                        <td style={{ padding: "12px" }}>{i.quantity}</td>
                        <td style={{ padding: "12px" }}>
                          {/* Hide UpdateInventory for OPERATORs */}
                          {!isUserOperator ? (
                            <UpdateInventory
                              productId={i.productId}
                              initialQuantity={i.quantity}
                              onUpdate={updateInventory}
                            />
                          ) : (
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
                    ))
                  )}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>
    </AuthGuard>
  );
}

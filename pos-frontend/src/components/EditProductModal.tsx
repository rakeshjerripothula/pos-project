"use client";

import { useState, useEffect } from "react";
import { ProductData, ClientData, InventoryData } from "@/lib/types";

interface EditProductModalProps {
  product: ProductData;
  clients: ClientData[];
  inventory: InventoryData | null;
  onClose: () => void;
  onUpdate: (
    id: number,
    product: {
      productName: string;
      barcode: string;
      mrp: number;
      clientId: number;
      imageUrl?: string;
    }
  ) => Promise<void>;
  onUpdateInventory?: (productId: number, quantity: number) => Promise<void>;
}

export default function EditProductModal({
  product,
  clients,
  inventory,
  onClose,
  onUpdate,
  onUpdateInventory,
}: EditProductModalProps) {
  const [productName, setProductName] = useState(product.productName);
  const [barcode, setBarcode] = useState(product.barcode);
  const [mrp, setMrp] = useState(product.mrp.toString());
  const [clientId, setClientId] = useState(product.clientId.toString());
  const [imageUrl, setImageUrl] = useState(product.imageUrl || "");
  const [inventoryQty, setInventoryQty] = useState(
    inventory?.quantity.toString() || "0"
  );
  const [loading, setLoading] = useState(false);

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setLoading(true);

    try {
      await onUpdate(product.id, {
        productName,
        barcode,
        mrp: Number(mrp),
        clientId: Number(clientId),
        imageUrl: imageUrl.trim() || undefined,
      });

      if (onUpdateInventory && inventoryQty !== inventory?.quantity.toString()) {
        await onUpdateInventory(product.id, Number(inventoryQty));
      }

      onClose();
    } catch (error: any) {
      alert("Failed to update: " + error.message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <div
      style={{
        position: "fixed",
        top: 0,
        left: 0,
        right: 0,
        bottom: 0,
        backgroundColor: "rgba(0,0,0,0.5)",
        display: "flex",
        justifyContent: "center",
        alignItems: "center",
        zIndex: 1000,
      }}
      onClick={onClose}
    >
      <div
        style={{
          backgroundColor: "white",
          padding: 24,
          borderRadius: 8,
          maxWidth: 600,
          width: "90%",
          maxHeight: "90vh",
          overflow: "auto",
        }}
        onClick={(e) => e.stopPropagation()}
      >
        <div
          style={{
            display: "flex",
            justifyContent: "space-between",
            alignItems: "center",
            marginBottom: 24,
          }}
        >
          <h2 style={{ fontSize: 20, fontWeight: "bold" }}>Edit Product</h2>
          <button
            onClick={onClose}
            style={{
              background: "none",
              border: "none",
              fontSize: 24,
              cursor: "pointer",
            }}
          >
            ×
          </button>
        </div>

        <form onSubmit={handleSubmit}>
          <div style={{ marginBottom: 16 }}>
            <label style={{ display: "block", marginBottom: 4 }}>
              Product Name *
            </label>
            <input
              type="text"
              value={productName}
              onChange={(e) => setProductName(e.target.value)}
              required
              disabled={loading}
              style={{
                width: "100%",
                padding: 8,
                border: "1px solid #ddd",
                borderRadius: 4,
              }}
            />
          </div>

          <div style={{ marginBottom: 16 }}>
            <label style={{ display: "block", marginBottom: 4 }}>Barcode *</label>
            <input
              type="text"
              value={barcode}
              onChange={(e) => setBarcode(e.target.value)}
              required
              disabled={loading}
              style={{
                width: "100%",
                padding: 8,
                border: "1px solid #ddd",
                borderRadius: 4,
              }}
            />
          </div>

          <div style={{ marginBottom: 16 }}>
            <label style={{ display: "block", marginBottom: 4 }}>MRP *</label>
            <input
              type="number"
              step="0.01"
              min="0"
              value={mrp}
              onChange={(e) => setMrp(e.target.value)}
              required
              disabled={loading}
              style={{
                width: "100%",
                padding: 8,
                border: "1px solid #ddd",
                borderRadius: 4,
              }}
            />
          </div>

          <div style={{ marginBottom: 16 }}>
            <label style={{ display: "block", marginBottom: 4 }}>
              Client *
            </label>
            <select
              value={clientId}
              onChange={(e) => setClientId(e.target.value)}
              required
              disabled={loading}
              style={{
                width: "100%",
                padding: 8,
                border: "1px solid #ddd",
                borderRadius: 4,
              }}
            >
              {clients
                .filter((c) => c.enabled)
                .map((c) => (
                  <option key={c.id} value={c.id}>
                    {c.clientName}
                  </option>
                ))}
            </select>
          </div>

          <div style={{ marginBottom: 16 }}>
            <label style={{ display: "block", marginBottom: 4 }}>
              Image URL
            </label>
            <input
              type="url"
              value={imageUrl}
              onChange={(e) => setImageUrl(e.target.value)}
              disabled={loading}
              placeholder="https://example.com/image.jpg"
              style={{
                width: "100%",
                padding: 8,
                border: "1px solid #ddd",
                borderRadius: 4,
              }}
            />
            {imageUrl && (
              <img
                src={imageUrl}
                alt="Product preview"
                style={{
                  marginTop: 8,
                  maxWidth: "100%",
                  maxHeight: 200,
                  border: "1px solid #ddd",
                  borderRadius: 4,
                }}
                onError={(e) => {
                  (e.target as HTMLImageElement).style.display = "none";
                }}
              />
            )}
          </div>

          {onUpdateInventory && (
            <div style={{ marginBottom: 16 }}>
              <label style={{ display: "block", marginBottom: 4 }}>
                Inventory Quantity *
              </label>
              <input
                type="number"
                min="0"
                value={inventoryQty}
                onChange={(e) => setInventoryQty(e.target.value)}
                required
                disabled={loading}
                style={{
                  width: "100%",
                  padding: 8,
                  border: "1px solid #ddd",
                  borderRadius: 4,
                }}
              />
            </div>
          )}

          <div style={{ display: "flex", gap: 8, justifyContent: "flex-end" }}>
            <button
              type="button"
              onClick={onClose}
              disabled={loading}
              style={{
                padding: "8px 16px",
                backgroundColor: "#666",
                color: "white",
                border: "none",
                borderRadius: 4,
                cursor: loading ? "not-allowed" : "pointer",
              }}
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={loading}
              style={{
                padding: "8px 16px",
                backgroundColor: "#0070f3",
                color: "white",
                border: "none",
                borderRadius: 4,
                cursor: loading ? "not-allowed" : "pointer",
              }}
            >
              {loading ? "Saving..." : "Save"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

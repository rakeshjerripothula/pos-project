"use client";

import { useState } from "react";
import { ProductData, ClientData, InventoryData, FieldError } from "@/lib/types";
import { ApiValidationError } from "@/lib/api";
import toast from "react-hot-toast";

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
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});

  function getFieldError(field: string): string {
    return fieldErrors[field] || "";
  }

  function clearFieldError(field: string) {
    setFieldErrors((prev) => {
      const next = { ...prev };
      delete next[field];
      return next;
    });
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setLoading(true);
    setFieldErrors({});

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
      if (error instanceof ApiValidationError) {
        // Set field-specific errors
        const errors: Record<string, string> = {};
        error.fieldErrors.forEach((fe) => {
          errors[fe.field] = fe.message;
        });
        setFieldErrors(errors);
        
        // Show toast with main message
        toast.error(error.message);
      } else {
        toast.error("Failed to update: " + error.message);
      }
    } finally {
      setLoading(false);
    }
  }

  return (
    <div
      className="fixed inset-0 bg-black/50 flex items-start justify-center z-[1000] pt-20"
      onClick={onClose}
    >
      <div
        className="bg-white p-6 rounded-lg max-w-lg w-[90%] max-h-[90vh] overflow-auto"
        onClick={(e) => e.stopPropagation()}
      >
        <div className="flex items-center justify-between mb-6">
          <h2 className="text-2xl font-bold">Edit Product</h2>
          <button
            onClick={onClose}
            className="text-2xl bg-transparent border-none cursor-pointer hover:text-gray-600"
          >
            ×
          </button>
        </div>

        <form onSubmit={handleSubmit}>
          <div className="mb-4">
            <label className="block mb-1 text-base font-medium text-gray-700">
              Product Name *
            </label>
            <input
              type="text"
              value={productName}
              onChange={(e) => {
                setProductName(e.target.value);
                clearFieldError("productName");
              }}
              required
              disabled={loading}
              className={`w-full px-3 py-2 text-base border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 ${
                getFieldError("productName") 
                  ? "border-red-500 bg-red-50" 
                  : "border-gray-300"
              }`}
            />
            {getFieldError("productName") && (
              <p className="mt-1 text-sm text-red-600">{getFieldError("productName")}</p>
            )}
          </div>

          <div className="mb-4">
            <label className="block mb-1 text-base font-medium text-gray-700">
              Barcode *
            </label>
            <input
              type="text"
              value={barcode}
              onChange={(e) => {
                setBarcode(e.target.value);
                clearFieldError("barcode");
              }}
              required
              disabled={loading}
              className={`w-full px-3 py-2 text-base border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 ${
                getFieldError("barcode") 
                  ? "border-red-500 bg-red-50" 
                  : "border-gray-300"
              }`}
            />
            {getFieldError("barcode") && (
              <p className="mt-1 text-sm text-red-600">{getFieldError("barcode")}</p>
            )}
          </div>

          <div className="mb-4">
            <label className="block mb-1 text-base font-medium text-gray-700">
              MRP *
            </label>
            <input
              type="number"
              step="0.01"
              min="0"
              value={mrp}
              onChange={(e) => {
                setMrp(e.target.value);
                clearFieldError("mrp");
              }}
              required
              disabled={loading}
              className={`w-full px-3 py-2 text-base border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 ${
                getFieldError("mrp") 
                  ? "border-red-500 bg-red-50" 
                  : "border-gray-300"
              }`}
            />
            {getFieldError("mrp") && (
              <p className="mt-1 text-sm text-red-600">{getFieldError("mrp")}</p>
            )}
          </div>

          <div className="mb-4">
            <label className="block mb-1 text-base font-medium text-gray-700">
              Client *
            </label>
            <select
              value={clientId}
              onChange={(e) => {
                setClientId(e.target.value);
                clearFieldError("clientId");
              }}
              required
              disabled={loading}
              className={`w-full px-3 py-2 text-base border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 bg-white ${
                getFieldError("clientId") 
                  ? "border-red-500 bg-red-50" 
                  : "border-gray-300"
              }`}
            >
              {clients
                .filter((c) => c.enabled)
                .map((c) => (
                  <option key={c.id} value={c.id}>
                    {c.clientName}
                  </option>
                ))}
            </select>
            {getFieldError("clientId") && (
              <p className="mt-1 text-sm text-red-600">{getFieldError("clientId")}</p>
            )}
          </div>

          <div className="mb-4">
            <label className="block mb-1 text-base font-medium text-gray-700">
              Image URL
            </label>
            <input
              type="url"
              value={imageUrl}
              onChange={(e) => setImageUrl(e.target.value)}
              disabled={loading}
              placeholder="https://example.com/image.jpg"
              className="w-full px-3 py-2 text-base border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
            {imageUrl && (
              <img
                src={imageUrl}
                alt="Product preview"
                className="mt-2 max-w-full h-48 object-cover border border-gray-200 rounded-md"
                onError={(e) => {
                  (e.target as HTMLImageElement).style.display = "none";
                }}
              />
            )}
          </div>

          {onUpdateInventory && (
            <div className="mb-4">
              <label className="block mb-1 text-base font-medium text-gray-700">
                Inventory Quantity *
              </label>
              <input
                type="number"
                min="0"
                value={inventoryQty}
                onChange={(e) => setInventoryQty(e.target.value)}
                required
                disabled={loading}
                className={`w-full px-3 py-2 text-base border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 ${
                  getFieldError("quantity") 
                    ? "border-red-500 bg-red-50" 
                    : "border-gray-300"
                }`}
              />
              {getFieldError("quantity") && (
                <p className="mt-1 text-sm text-red-600">{getFieldError("quantity")}</p>
              )}
            </div>
          )}

          <div className="flex gap-2 justify-end">
            <button
              type="button"
              onClick={onClose}
              disabled={loading}
              className={`px-4 py-2 text-base text-white rounded-md transition-colors ${
                loading 
                  ? "bg-gray-400 cursor-not-allowed" 
                  : "bg-gray-500 hover:bg-gray-600 cursor-pointer"
              }`}
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={loading}
              className={`px-4 py-2 text-base text-white rounded-md transition-colors ${
                loading 
                  ? "bg-gray-400 cursor-not-allowed" 
                  : "bg-blue-500 hover:bg-blue-600 cursor-pointer"
              }`}
            >
              {loading ? "Saving..." : "Save"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}


"use client";

import { useState } from "react";
import { ClientData, FieldError } from "@/lib/types";
import { ApiValidationError } from "@/lib/api";
import ClientSelect from "@/components/ClientSelect";
import toast from "react-hot-toast";

export default function AddProduct({
  clients,
  onAdd,
}: {
  clients: ClientData[];
  onAdd: (product: {
    productName: string;
    barcode: string;
    mrp: number;
    clientId: number;
    imageUrl?: string;
  }) => Promise<void>;
}) {
  const [productName, setProductName] = useState("");
  const [barcode, setBarcode] = useState("");
  const [mrp, setMrp] = useState("");
  const [clientId, setClientId] = useState<number | null>(null);
  const [imageUrl, setImageUrl] = useState("");
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

  async function submit() {
    if (!productName || !barcode || !mrp || !clientId) {
      toast.error("All fields are required");
      return;
    }

    setLoading(true);
    setFieldErrors({});

    try {
      await onAdd({
        productName,
        barcode,
        mrp: Number(mrp),
        clientId: clientId,
        imageUrl: imageUrl || undefined,
      });

      setProductName("");
      setBarcode("");
      setMrp("");
      setClientId(null);
      setImageUrl("");
    } catch (e: any) {
      if (e instanceof ApiValidationError) {
        // Set field-specific errors
        const errors: Record<string, string> = {};
        e.fieldErrors.forEach((fe: FieldError) => {
          errors[fe.field] = fe.message;
        });
        setFieldErrors(errors);
        
        // Show toast with main message
        toast.error(e.message);
      } else {
        toast.error(e.message);
      }
    } finally {
      setLoading(false);
    }
  }

  return (
    <div>
      <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-6 items-end">
        <div>
          <label className="block mb-1.5 text-xs font-medium text-gray-700">
            Product Name
          </label>
          <input
            placeholder="Product Name"
            value={productName}
            onChange={(e) => {
              setProductName(e.target.value);
              clearFieldError("productName");
            }}
            className={`w-full px-3 py-2 text-sm border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent ${
              getFieldError("productName")
                ? "border-red-500 bg-red-50" 
                : "border-gray-300"
            }`}
          />
          {getFieldError("productName") && (
            <p className="mt-1 text-xs text-red-600">{getFieldError("productName")}</p>
          )}
        </div>

        <div>
          <label className="block mb-1.5 text-xs font-medium text-gray-700">
            Barcode
          </label>
          <input
            placeholder="Barcode"
            value={barcode}
            onChange={(e) => {
              setBarcode(e.target.value);
              clearFieldError("barcode");
            }}
            className={`w-full px-3 py-2 text-sm border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent ${
              getFieldError("barcode")
                ? "border-red-500 bg-red-50" 
                : "border-gray-300"
            }`}
          />
          {getFieldError("barcode") && (
            <p className="mt-1 text-xs text-red-600">{getFieldError("barcode")}</p>
          )}
        </div>

        <div>
          <label className="block mb-1.5 text-xs font-medium text-gray-700">
            MRP
          </label>
          <input
            placeholder="MRP"
            type="number"
            value={mrp}
            onChange={(e) => {
              setMrp(e.target.value);
              clearFieldError("mrp");
            }}
            className={`w-full px-3 py-2 text-sm border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent ${
              getFieldError("mrp")
                ? "border-red-500 bg-red-50" 
                : "border-gray-300"
            }`}
          />
          {getFieldError("mrp") && (
            <p className="mt-1 text-xs text-red-600">{getFieldError("mrp")}</p>
          )}
        </div>

        <div>
          <label className="block mb-1.5 text-xs font-medium text-gray-700">
            Image URL
          </label>
          <input
            placeholder="Image Url"
            value={imageUrl}
            onChange={(e) => setImageUrl(e.target.value)}
            className="w-full px-3 py-2 text-sm border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
          />
        </div>

        <div>
          <label className="block mb-1.5 text-xs font-medium text-gray-700">
            Client
          </label>
          <ClientSelect
            clients={clients}
            value={clientId}
            onChange={(val) => {
              setClientId(val);
              clearFieldError("clientId");
            }}
            placeholder="Select Client"
            isClearable={false}
            className={getFieldError("clientId") ? "border-red-500 bg-red-50" : ""}
          />
          {getFieldError("clientId") && (
            <p className="mt-1 text-xs text-red-600">{getFieldError("clientId")}</p>
          )}
        </div>

        <div>
        <button
            onClick={submit}
            disabled={loading}
            className={`w-full px-4 py-2 text-sm font-medium text-white rounded-md transition-colors whitespace-nowrap ${
              loading 
                ? "bg-gray-400 cursor-not-allowed"
                : "bg-blue-500 hover:bg-blue-600 cursor-pointer"
            }`}
          >
            {loading ? "Adding..." : "Add Product"}
          </button>
        </div>
      </div>
    </div>
  );
}


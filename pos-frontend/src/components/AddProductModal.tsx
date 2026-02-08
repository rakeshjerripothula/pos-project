"use client";

import { useState } from "react";
import { ClientData } from "@/lib/types";
import AddProduct from "@/components/AddProduct";
import ProductTsvUpload from "@/components/ProductTsvUpload";
import toast from "react-hot-toast";

interface AddProductModalProps {
  clients: ClientData[];
  onClose: () => void;
  onAdd: (product: {
    productName: string;
    barcode: string;
    mrp: number;
    clientId: number;
    imageUrl?: string;
  }) => Promise<void>;
  onUploadTsv: (file: File) => Promise<void>;
}

type Tab = "form" | "tsv";

export default function AddProductModal({
  clients,
  onClose,
  onAdd,
  onUploadTsv,
}: AddProductModalProps) {
  const [activeTab, setActiveTab] = useState<Tab>("form");
  const [tsvFile, setTsvFile] = useState<File | null>(null);

  async function handleAdd(
    product: {
      productName: string;
      barcode: string;
      mrp: number;
      clientId: number;
      imageUrl?: string;
    }
  ) {
    await onAdd(product);
    onClose();
  }

  async function handleTsvUpload() {
    if (!tsvFile) {
      toast.error("Please select a TSV file");
      return;
    }
    try {
      await onUploadTsv(tsvFile);
      onClose();
    } catch (e: any) {
      // Error handling is done in the upload function
    }
  }

  function handleTsvSelect(file: File) {
    setTsvFile(file);
  }

  return (
    <div
      className="fixed inset-0 bg-black/50 flex items-center justify-center z-[1000]"
      onClick={onClose}
    >
      <div
        className="bg-white p-6 rounded-lg max-w-4xl w-[90%] max-h-[90vh] overflow-auto"
        onClick={(e) => e.stopPropagation()}
      >
        <div className="flex items-center justify-between mb-6">
          <h2 className="text-xl font-bold">Add Product</h2>
          <button
            onClick={onClose}
            className="text-2xl bg-transparent border-none cursor-pointer hover:text-gray-600"
          >
            ×
          </button>
        </div>

        {/* Tabs */}
        <div className="flex border-b border-gray-200 mb-6">
          <button
            onClick={() => setActiveTab("form")}
            className={`px-4 py-2 text-sm font-medium border-b-2 transition-colors cursor-pointer ${
              activeTab === "form"
                ? "border-blue-500 text-blue-600"
                : "border-transparent text-gray-500 hover:text-gray-700"
            }`}
          >
            Form
          </button>
          <button
            onClick={() => setActiveTab("tsv")}
            className={`px-4 py-2 text-sm font-medium border-b-2 transition-colors cursor-pointer ${
              activeTab === "tsv"
                ? "border-blue-500 text-blue-600"
                : "border-transparent text-gray-500 hover:text-gray-700"
            }`}
          >
            TSV Upload
          </button>
        </div>

        {/* Tab Content */}
        <div className="mb-6">
          {activeTab === "form" && (
            <AddProduct clients={clients} onAdd={handleAdd} />
          )}
          {activeTab === "tsv" && (
            <ProductTsvUpload onUpload={onUploadTsv} onFileSelect={handleTsvSelect} />
          )}
        </div>

        {/* Footer */}
        <div className="flex gap-2 justify-end">
          <button
            onClick={onClose}
            className="px-4 py-2 text-sm text-white bg-gray-500 rounded-md hover:bg-gray-600 transition-colors cursor-pointer"
          >
            Cancel
          </button>
          {activeTab === "form" && (
            <button
              onClick={() => {
                const form = document.querySelector('form');
                if (form) {
                  const submitEvent = new Event('submit', { cancelable: true, bubbles: true });
                  form.dispatchEvent(submitEvent);
                }
              }}
              className="px-4 py-2 text-sm text-white bg-blue-500 rounded-md hover:bg-blue-600 transition-colors cursor-pointer"
            >
              Add Product
            </button>
          )}
          {activeTab === "tsv" && (
            <button
              onClick={handleTsvUpload}
              className="px-4 py-2 text-sm text-white bg-blue-500 rounded-md hover:bg-blue-600 transition-colors cursor-pointer"
            >
              Add
            </button>
          )}
        </div>
      </div>
    </div>
  );
}


"use client";

import { useState } from "react";
import { ProductData } from "@/lib/types";
import AddInventory from "@/components/AddInventory";
import InventoryTsvUpload from "@/components/InventoryTsvUpload";
import toast from "react-hot-toast";

interface AddInventoryModalProps {
  products: ProductData[];
  onClose: () => void;
  onAdd: (productId: number, quantity: number) => Promise<void>;
  onUploadTsv: (file: File) => Promise<void>;
}

type Tab = "form" | "tsv";

export default function AddInventoryModal({
  products,
  onClose,
  onAdd,
  onUploadTsv,
}: AddInventoryModalProps) {
  const [activeTab, setActiveTab] = useState<Tab>("form");
  const [tsvFile, setTsvFile] = useState<File | null>(null);

  async function handleAdd(productId: number, quantity: number) {
    await onAdd(productId, quantity);
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
      className="fixed inset-0 bg-black/50 flex items-start justify-center z-[1000] pt-20"
      onClick={onClose}
    >
      <div
        className="bg-white p-6 rounded-lg max-w-4xl w-[90%] max-h-[90vh] overflow-y-auto"
        onClick={(e) => e.stopPropagation()}
      >
        <div className="flex items-center justify-between mb-6">
          <h2 className="text-xl font-bold">Add Inventory</h2>
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
            <AddInventory products={products} onAdd={handleAdd} />
          )}
          {activeTab === "tsv" && (
            <InventoryTsvUpload onUpload={onUploadTsv} onFileSelect={handleTsvSelect} />
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
              Add Inventory
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


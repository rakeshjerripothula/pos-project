"use client";

import { useState } from "react";
import toast from "react-hot-toast";

interface AddClientModalProps {
  onClose: () => void;
  onAdd: (clientName: string) => Promise<void>;
}

export default function AddClientModal({ onClose, onAdd }: AddClientModalProps) {
  const [clientName, setClientName] = useState("");
  const [loading, setLoading] = useState(false);

  async function handleAdd() {
    if (!clientName.trim()) {
      toast.error("Client name is required");
      return;
    }

    setLoading(true);
    try {
      await onAdd(clientName);
      onClose();
    } catch (e: any) {
      toast.error(e.message);
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
        className="bg-white p-6 rounded-lg max-w-md w-[90%] max-h-[90vh] overflow-auto"
        onClick={(e) => e.stopPropagation()}
      >
        <div className="flex items-center justify-between mb-6">
          <h2 className="text-xl font-bold">Add Client</h2>
          <button
            onClick={onClose}
            className="text-2xl bg-transparent border-none cursor-pointer hover:text-gray-600"
          >
            ×
          </button>
        </div>

        <div className="mb-6">
          <label className="block mb-1.5 text-sm font-medium text-gray-700">
            Client Name <span className="text-red-500">*</span>
          </label>
          <input
            type="text"
            value={clientName}
            onChange={(e) => setClientName(e.target.value)}
            onKeyDown={(e) => {
              if (e.key === "Enter") {
                handleAdd();
              }
            }}
            placeholder="Enter client name"
            disabled={loading}
            className="w-full px-3 py-2 text-sm border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
          />
        </div>

        {/* Footer with Cancel and Add buttons */}
        <div className="flex gap-2 justify-end">
          <button
            onClick={onClose}
            className="px-4 py-2 text-sm text-white bg-gray-500 rounded-md hover:bg-gray-600 transition-colors cursor-pointer"
          >
            Cancel
          </button>
          <button
            onClick={handleAdd}
            disabled={loading}
            className={`px-4 py-2 text-sm text-white rounded-md transition-colors ${
              loading
                ? "bg-gray-400 cursor-not-allowed"
                : "bg-blue-500 hover:bg-blue-600 cursor-pointer"
            }`}
          >
            {loading ? "Adding..." : "Add"}
          </button>
        </div>
      </div>
    </div>
  );
}


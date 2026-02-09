"use client";

import { useState } from "react";
import toast from "react-hot-toast";

export default function AddClient({
  onAdd,
}: {
  onAdd: (clientName: string) => Promise<void>;
}) {
  const [clientName, setClientName] = useState("");
  const [loading, setLoading] = useState(false);

  async function submit() {
    if (!clientName.trim()) {
      toast.error("Client name is required");
      return;
    }

    setLoading(true);
    try {
      await onAdd(clientName);
      setClientName("");
    } catch (e: any) {
      toast.error(e.message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="flex items-center gap-3 max-w-md">
      <input
        value={clientName}
        onChange={(e) => setClientName(e.target.value)}
        placeholder="Enter client name"
        disabled={loading}
        onKeyDown={(e) => {
          if (e.key === "Enter") {
            submit();
          }
        }}
        className="flex-1 px-4 py-2 text-base transition-all border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
      />
      <button
        onClick={submit}
        disabled={loading}
        className={`px-5 py-2 text-base font-medium text-white rounded-md transition-all whitespace-nowrap ${
          loading 
            ? "bg-gray-400 cursor-not-allowed"
            : "bg-blue-500 hover:bg-blue-600 cursor-pointer"
        }`}
      >
        {loading ? "Adding..." : "Add Client"}
      </button>
    </div>
  );
}


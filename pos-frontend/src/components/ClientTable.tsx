"use client";

import { useState } from "react";
import { ClientData } from "@/lib/types";
import { isOperator } from "@/components/AuthGuard";
import toast from "react-hot-toast";

export default function ClientTable({
  clients,
  onToggle,
  onUpdate,
}: {
  clients: ClientData[];
  onToggle: (id: number, enabled: boolean) => void;
  onUpdate: (id: number, clientName: string) => Promise<void>;
}) {
  const [editingId, setEditingId] = useState<number | null>(null);
  const [editValue, setEditValue] = useState("");
  const [loading, setLoading] = useState(false);
  const isUserOperator = isOperator();

  function startEdit(client: ClientData) {
    setEditingId(client.id);
    setEditValue(client.clientName);
  }

  function cancelEdit() {
    setEditingId(null);
    setEditValue("");
  }

  async function saveEdit(id: number) {
    if (!editValue.trim()) {
      toast.error("Client name cannot be empty");
      return;
    }

    setLoading(true);
    try {
      await onUpdate(id, editValue.trim());
      setEditingId(null);
      setEditValue("");
    } catch (error: any) {
      toast.error("Failed to update client: " + error.message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="overflow-x-auto">
              <table className="w-full border-collapse">
        <thead>
          <tr className="border-b-2 border-gray-200">
            <th className="px-2 py-2.5 text-base font-semibold text-left text-gray-700">ID</th>
            <th className="px-2 py-2.5 text-base font-semibold text-left text-gray-700">Client Name</th>
            <th className="px-2 py-2.5 text-base font-semibold text-left text-gray-700">Enabled</th>
            {!isUserOperator && (
              <th className="px-2 py-2.5 text-base font-semibold text-left text-gray-700">Actions</th>
            )}
          </tr>
        </thead>
        <tbody>
          {clients.map((c) => (
            <tr key={c.id} className="border-b border-gray-100">
              <td className="px-2 py-2.5 text-base">{c.id}</td>
              <td className="px-2 py-2.5">
                {editingId === c.id ? (
                  <input
                    type="text"
                    value={editValue}
                    onChange={(e) => setEditValue(e.target.value)}
                    disabled={loading}
                    className="w-full px-2 py-1 text-base border border-blue-500 rounded"
                    onKeyDown={(e) => {
                      if (e.key === "Enter") {
                        saveEdit(c.id);
                      } else if (e.key === "Escape") {
                        cancelEdit();
                      }
                    }}
                    autoFocus
                  />
                ) : (
                  <span
                    onClick={() => startEdit(c)}
                    className="inline-block px-2 py-1 text-base transition-colors rounded cursor-pointer hover:bg-gray-100"
                  >
                    {c.clientName}
                  </span>
                )}
              </td>
              <td className="px-2 py-2.5 text-base">
                <span className={`px-2 py-0.5 text-sm rounded-full ${
                  c.enabled 
                    ? "bg-green-100 text-green-800" 
                    : "bg-gray-100 text-gray-600"
                }`}>
                  {c.enabled ? "Yes" : "No"}
                </span>
              </td>
              {!isUserOperator && (
                <td className="px-2 py-2.5">
                  {editingId === c.id ? (
                    <div className="flex gap-1.5">
                      <button
                        onClick={() => saveEdit(c.id)}
                        disabled={loading}
                        className={`px-2.5 py-1 text-sm text-white rounded ${
                          loading 
                            ? "bg-gray-400 cursor-not-allowed" 
                            : "bg-blue-500 hover:bg-blue-600 cursor-pointer"
                        }`}
                      >
                        ✓
                      </button>
                      <button
                        onClick={cancelEdit}
                        disabled={loading}
                        className={`px-2.5 py-1 text-sm text-white rounded ${
                          loading 
                            ? "bg-gray-400 cursor-not-allowed" 
                            : "bg-gray-500 hover:bg-gray-600 cursor-pointer"
                        }`}
                      >
                        ✕
                      </button>
                    </div>
                  ) : (
                    <div className="flex gap-1.5">
                      <button
                        onClick={() => startEdit(c)}
                        className="px-2.5 py-1 text-sm text-white transition-colors bg-blue-500 rounded hover:bg-blue-600 cursor-pointer"
                      >
                        Edit
                      </button>
                      <button
                        onClick={() => onToggle(c.id, !c.enabled)}
                        className={`px-2.5 py-1 text-sm text-white rounded transition-colors cursor-pointer ${
                          c.enabled 
                            ? "bg-red-500 hover:bg-red-600" 
                            : "bg-green-500 hover:bg-green-600"
                        }`}
                      >
                        {c.enabled ? "Disable" : "Enable"}
                      </button>
                    </div>
                  )}
                </td>
              )}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}


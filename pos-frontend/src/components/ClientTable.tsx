"use client";

import { useState } from "react";
import { ClientData } from "@/lib/types";

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
      alert("Client name cannot be empty");
      return;
    }

    setLoading(true);
    try {
      await onUpdate(id, editValue.trim());
      setEditingId(null);
      setEditValue("");
    } catch (error: any) {
      alert("Failed to update client: " + error.message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <table border={1} cellPadding={8} style={{ width: "100%" }}>
      <thead>
        <tr>
          <th>ID</th>
          <th>Client Name</th>
          <th>Enabled</th>
          <th>Actions</th>
        </tr>
      </thead>
      <tbody>
        {clients.map((c) => (
          <tr key={c.id}>
            <td>{c.id}</td>
            <td>
              {editingId === c.id ? (
                <input
                  type="text"
                  value={editValue}
                  onChange={(e) => setEditValue(e.target.value)}
                  disabled={loading}
                  style={{
                    padding: 4,
                    width: "100%",
                    border: "1px solid #0070f3",
                  }}
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
                  style={{
                    cursor: "pointer",
                    padding: "4px 8px",
                    borderRadius: 4,
                    display: "inline-block",
                  }}
                  onMouseEnter={(e) => {
                    e.currentTarget.style.backgroundColor = "#f0f0f0";
                  }}
                  onMouseLeave={(e) => {
                    e.currentTarget.style.backgroundColor = "transparent";
                  }}
                >
                  {c.clientName}
                </span>
              )}
            </td>
            <td>{c.enabled ? "Yes" : "No"}</td>
            <td>
              {editingId === c.id ? (
                <div style={{ display: "flex", gap: 4 }}>
                  <button
                    onClick={() => saveEdit(c.id)}
                    disabled={loading}
                    style={{
                      padding: "4px 8px",
                      backgroundColor: "#28a745",
                      color: "white",
                      border: "none",
                      borderRadius: 4,
                      cursor: loading ? "not-allowed" : "pointer",
                      fontSize: 12,
                    }}
                  >
                    ✓
                  </button>
                  <button
                    onClick={cancelEdit}
                    disabled={loading}
                    style={{
                      padding: "4px 8px",
                      backgroundColor: "#dc3545",
                      color: "white",
                      border: "none",
                      borderRadius: 4,
                      cursor: loading ? "not-allowed" : "pointer",
                      fontSize: 12,
                    }}
                  >
                    ✕
                  </button>
                </div>
              ) : (
                <div style={{ display: "flex", gap: 4 }}>
                  <button
                    onClick={() => startEdit(c)}
                    style={{
                      padding: "4px 8px",
                      backgroundColor: "#0070f3",
                      color: "white",
                      border: "none",
                      borderRadius: 4,
                      cursor: "pointer",
                      fontSize: 12,
                    }}
                  >
                    Edit
                  </button>
                  <button
                    onClick={() => onToggle(c.id, c.enabled)}
                    style={{
                      padding: "4px 8px",
                      backgroundColor: c.enabled ? "#ffc107" : "#28a745",
                      color: "white",
                      border: "none",
                      borderRadius: 4,
                      cursor: "pointer",
                      fontSize: 12,
                    }}
                  >
                    {c.enabled ? "Disable" : "Enable"}
                  </button>
                </div>
              )}
            </td>
          </tr>
        ))}
      </tbody>
    </table>
  );
}

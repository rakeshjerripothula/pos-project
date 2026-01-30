"use client";

import { useState } from "react";

export default function AddClient({
  onAdd,
}: {
  onAdd: (clientName: string) => Promise<void>;
}) {
  const [clientName, setClientName] = useState("");
  const [loading, setLoading] = useState(false);

  async function submit() {
    if (!clientName.trim()) {
      alert("Client name is required");
      return;
    }

    setLoading(true);
    try {
      await onAdd(clientName);
      setClientName("");
    } catch (e: any) {
      alert(e.message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <div style={{ display: "flex", gap: 12, alignItems: "center" }}>
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
        style={{
          flex: 1,
          padding: "12px 16px",
          border: "1px solid #d1d5db",
          borderRadius: 8,
          fontSize: 16,
          transition: "all 0.2s",
        }}
        onFocus={(e) => {
          e.currentTarget.style.borderColor = "#667eea";
          e.currentTarget.style.outline = "none";
          e.currentTarget.style.boxShadow = "0 0 0 3px rgba(102, 126, 234, 0.1)";
        }}
        onBlur={(e) => {
          e.currentTarget.style.borderColor = "#d1d5db";
          e.currentTarget.style.boxShadow = "none";
        }}
      />
      <button
        onClick={submit}
        disabled={loading}
        style={{
          padding: "12px 24px",
          backgroundColor: loading ? "#9ca3af" : "#667eea",
          color: "white",
          border: "none",
          borderRadius: 8,
          fontSize: 16,
          fontWeight: 500,
          cursor: loading ? "not-allowed" : "pointer",
          transition: "all 0.2s",
          whiteSpace: "nowrap",
        }}
        onMouseEnter={(e) => {
          if (!loading) {
            e.currentTarget.style.backgroundColor = "#5568d3";
          }
        }}
        onMouseLeave={(e) => {
          if (!loading) {
            e.currentTarget.style.backgroundColor = "#667eea";
          }
        }}
      >
        {loading ? "Adding..." : "Add Client"}
      </button>
    </div>
  );
}

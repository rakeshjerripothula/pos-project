"use client";

import { useState } from "react";

export default function ProductTsvUpload({
  onUpload,
}: {
  onUpload: (file: File) => Promise<void>;
}) {
  const [file, setFile] = useState<File | null>(null);
  const [loading, setLoading] = useState(false);

  async function submit() {
    if (!file) {
      alert("Please select a TSV file");
      return;
    }

    setLoading(true);
    try {
      await onUpload(file);
      setFile(null);
      alert("Product TSV uploaded successfully");
    } catch (e: any) {
      alert(e.message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <div style={{ marginBottom: 16 }}>
      <input
        type="file"
        accept=".tsv"
        onChange={(e) => setFile(e.target.files?.[0] ?? null)}
      />
      <button onClick={submit} disabled={loading}>
        {loading ? "Uploading..." : "Upload Product TSV"}
      </button>
    </div>
  );
}

"use client";

import { useState } from "react";
import toast from "react-hot-toast";

export default function ProductTsvUpload({
  onUpload,
}: {
  onUpload: (file: File) => Promise<void>;
}) {
  const [file, setFile] = useState<File | null>(null);
  const [loading, setLoading] = useState(false);

  async function submit() {
    if (!file) {
      toast.error("Please select a TSV file");
      return;
    }

    setLoading(true);
    try {
      await onUpload(file);
      setFile(null);
      toast.success("Product TSV uploaded successfully");
    } catch (e: any) {
      toast.error(e.message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <div>
      <div className="flex flex-wrap items-center gap-4">
        <div className="flex-shrink-0">
          <input
            type="file"
            accept=".tsv"
            onChange={(e) => setFile(e.target.files?.[0] ?? null)}
            className="px-3 py-2 text-sm bg-white border border-gray-300 rounded-lg"
          />
        </div>
        <div className="flex-shrink-0">
          <button
            onClick={submit}
            disabled={loading}
            className={`px-5 py-2.5 text-sm font-medium text-white rounded-lg transition-colors ${
              loading 
                ? "bg-red-400 cursor-not-allowed" 
                : "bg-emerald-500 hover:bg-emerald-600 cursor-pointer"
            }`}
          >
            {loading ? "Uploading..." : "Upload Product TSV"}
          </button>
        </div>
        {file && (
          <div className="flex-1 text-sm text-gray-700">
            Selected: {file.name}
          </div>
        )}
      </div>
    </div>
  );
}


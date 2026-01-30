"use client";

import { useState } from "react";
import { ClientData } from "@/lib/types";

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
  const [clientId, setClientId] = useState("");
  const [imageUrl, setImageUrl] = useState("");
  const [loading, setLoading] = useState(false);

  async function submit() {
    if (!productName || !barcode || !mrp || !clientId) {
      alert("All fields are required");
      return;
    }

    setLoading(true);
    try {
      await onAdd({
        productName,
        barcode,
        mrp: Number(mrp),
        clientId: Number(clientId),
        imageUrl: imageUrl || undefined,
      });

      setProductName("");
      setBarcode("");
      setMrp("");
      setClientId("");
      setImageUrl("");
    } catch (e: any) {
      alert(e.message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <div style={{ marginBottom: 16 }}>
      <input
        placeholder="Product Name"
        value={productName}
        onChange={(e) => setProductName(e.target.value)}
      />

      <input
        placeholder="Barcode"
        value={barcode}
        onChange={(e) => setBarcode(e.target.value)}
      />

      <input
        placeholder="MRP"
        type="number"
        value={mrp}
        onChange={(e) => setMrp(e.target.value)}
      />

      <input
        placeholder="Image Url"
        value={imageUrl}
        onChange={(e) => setImageUrl(e.target.value)}
      />

      <select
        value={clientId}
        onChange={(e) => setClientId(e.target.value)}
      >
        <option value="">Select Client</option>
        {clients
          .filter((c) => c.enabled)
          .map((c) => (
            <option key={c.id} value={c.id}>
              {c.clientName}
            </option>
          ))}
      </select>

      <button onClick={submit} disabled={loading}>
        {loading ? "Adding..." : "Add Product"}
      </button>
    </div>
  );
}

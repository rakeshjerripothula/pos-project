"use client";

import { useState } from "react";
import { ProductData } from "@/lib/types";

export default function AddInventory({
  products,
  onAdd,
}: {
  products: ProductData[];
  onAdd: (productId: number, quantity: number) => Promise<void>;
}) {
  const [productId, setProductId] = useState("");
  const [quantity, setQuantity] = useState("");
  const [loading, setLoading] = useState(false);

  async function submit() {
    if (!productId || !quantity) {
      alert("All fields required");
      return;
    }

    setLoading(true);
    try {
      await onAdd(Number(productId), Number(quantity));
      setProductId("");
      setQuantity("");
    } catch (e: any) {
      alert(e.message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <div style={{ marginBottom: 16 }}>
      <select
        value={productId}
        onChange={(e) => setProductId(e.target.value)}
      >
        <option value="">Select Product</option>
        {products.map((p) => (
          <option key={p.id} value={p.id}>
            {p.productName}
          </option>
        ))}
      </select>

      <input
        type="number"
        placeholder="Quantity"
        value={quantity}
        onChange={(e) => setQuantity(e.target.value)}
        style={{ marginLeft: 8 }}
      />

      <button onClick={submit} disabled={loading}>
        {loading ? "Adding..." : "Add Inventory"}
      </button>
    </div>
  );
}

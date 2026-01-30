"use client";

import { useState } from "react";

export default function UpdateInventory({
  productId,
  initialQuantity,
  onUpdate,
}: {
  productId: number;
  initialQuantity: number;
  onUpdate: (productId: number, quantity: number) => Promise<void>;
}) {
  const [quantity, setQuantity] = useState(initialQuantity);
  const [loading, setLoading] = useState(false);

  async function submit() {
    if (quantity < 0) {
      alert("Quantity cannot be negative");
      return;
    }

    setLoading(true);
    try {
      await onUpdate(productId, quantity);
    } catch (e: any) {
      alert(e.message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <div>
      <input
        type="number"
        value={quantity}
        onChange={(e) => setQuantity(Number(e.target.value))}
        style={{ width: 80 }}
      />
      <button onClick={submit} disabled={loading}>
        Update
      </button>
    </div>
  );
}

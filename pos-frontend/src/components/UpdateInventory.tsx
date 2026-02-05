"use client";

import { useState } from "react";
import toast from "react-hot-toast";

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
      toast.error("Quantity cannot be negative");
      return;
    }

    setLoading(true);
    try {
      await onUpdate(productId, quantity);
    } catch (e: any) {
      toast.error(e.message);
    } finally {
      setLoading(false);
    }
  }

  function handleQuantityChange(value: number) {
    if (value < 0) return;
    setQuantity(value);
  }

  return (
    <div className="flex items-center gap-2">
      <input
        type="number"
        min={0}
        value={quantity}
        onChange={(e) => handleQuantityChange(Number(e.target.value))}
        className="w-20 px-3 py-1.5 text-sm border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
      />
      <button
        onClick={submit}
        disabled={loading || quantity < 0}
        className={`px-4 py-1.5 text-sm font-medium text-white rounded-md transition-colors ${
          loading || quantity < 0
            ? "bg-gray-400 cursor-not-allowed"
            : "bg-blue-500 hover:bg-blue-600 cursor-pointer"
        }`}
      >
        {loading ? "Updating..." : "Update"}
      </button>
    </div>
  );
}


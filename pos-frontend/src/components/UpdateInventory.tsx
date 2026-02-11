"use client";

import { useState } from "react";
import toast from "react-hot-toast";

export default function UpdateInventory({
  productId,
  initialQuantity,
  isEditing,
  onEdit,
  onCancel,
  onUpdate,
}: {
  productId: number;
  initialQuantity: number;
  isEditing: boolean;
  onEdit: () => void;
  onCancel: () => void;
  onUpdate: (productId: number, quantity: number) => Promise<void>;
}) {
  const [quantity, setQuantity] = useState(initialQuantity);
  const [loading, setLoading] = useState(false);

  async function saveEdit() {
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

  function cancelEdit() {
    setQuantity(initialQuantity);
    onCancel();
  }

  return (
    <div className="flex items-center gap-1.5">
      {isEditing ? (
        <>
          <input
            type="number"
            min={0}
            value={quantity}
            onChange={(e) => handleQuantityChange(Number(e.target.value))}
            disabled={loading}
            onKeyDown={(e) => {
              if (e.key === "Enter") {
                saveEdit();
              } else if (e.key === "Escape") {
                cancelEdit();
              }
            }}
            className="w-20 px-2 py-1 text-base border border-blue-500 rounded focus:outline-none"
            autoFocus
          />
          <div className="flex gap-1">
            <button
              onClick={saveEdit}
              disabled={loading || quantity < 0}
              className={`px-2.5 py-1 text-sm text-white rounded ${
                loading || quantity < 0
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
        </>
      ) : (
        <>
          <span
            onClick={onEdit}
            className="inline-block w-20 px-2 py-1 text-base text-center transition-colors rounded cursor-pointer hover:bg-gray-100"
          >
            {initialQuantity}
          </span>
          <button
            onClick={onEdit}
            className="px-2.5 py-1 text-sm text-white transition-colors bg-blue-500 rounded hover:bg-blue-600 cursor-pointer"
          >
            Edit
          </button>
        </>
      )}
    </div>
  );
}


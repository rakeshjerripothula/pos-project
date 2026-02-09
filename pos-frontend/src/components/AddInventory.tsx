"use client";

import { useState } from "react";
import { ProductData } from "@/lib/types";
import ProductSelect from "@/components/ProductSelect";
import toast from "react-hot-toast";

export default function AddInventory({
  products,
  onAdd,
}: {
  products: ProductData[];
  onAdd: (productId: number, quantity: number) => Promise<void>;
}) {
  const [productId, setProductId] = useState<number | null>(null);
  const [quantity, setQuantity] = useState("");
  const [loading, setLoading] = useState(false);

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    const qty = Number(quantity);
    if (!productId || !quantity) {
      toast.error("All fields required");
      return;
    }

    if (qty < 0) {
      toast.error("Quantity cannot be negative");
      return;
    }

    setLoading(true);
    try {
      await onAdd(productId, qty);
      setProductId(null);
      setQuantity("");
    } catch (e: any) {
      toast.error(e.message);
    } finally {
      setLoading(false);
    }
  }

  function handleQuantityChange(value: string) {
    const numValue = Number(value);
    if (numValue < 0) return;
    setQuantity(value);
  }

  return (
    <form onSubmit={handleSubmit}>
      <div className="grid grid-cols-1 gap-3 sm:grid-cols-[1fr_140px] items-end max-w-2xl">
        <div>
          <label className="block mb-1.5 text-sm font-medium text-gray-700">
            Product
          </label>
          <ProductSelect
            products={products}
            value={productId}
            onChange={setProductId}
            placeholder="Search product..."
            isClearable={true}
          />
        </div>

        <div>
          <label className="block mb-1.5 text-sm font-medium text-gray-700">
            Quantity
          </label>
          <input
            type="number"
            min={0}
            placeholder="Qty"
            value={quantity}
            onChange={(e) => handleQuantityChange(e.target.value)}
            className="w-full px-3 py-2.5 text-base border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
          />
        </div>
      </div>
    </form>
  );
}


"use client";

import { useState, useEffect } from "react";
import { ProductData } from "@/lib/types";
import toast from "react-hot-toast";

interface OrderItemRow {
  barcode: string;
  quantity: number;
  sellingPrice: number;
}

const ORDER_DRAFT_KEY = "pos_order_draft";

interface CreateOrderFormProps {
  products: ProductData[];
  onCreate: (items: {
    productId: number;
    quantity: number;
    sellingPrice: number;
  }[]) => Promise<void>;
  onCancel?: () => void;
}

export default function CreateOrderForm({
  products,
  onCreate,
  onCancel,
}: CreateOrderFormProps) {
  const [items, setItems] = useState<OrderItemRow[]>([
    { barcode: "", quantity: 1, sellingPrice: 0 },
  ]);
  const [loading, setLoading] = useState(false);
  const [mounted, setMounted] = useState(false);

  // Load draft from localStorage on mount
  useEffect(() => {
    setMounted(true);
    const savedDraft = localStorage.getItem(ORDER_DRAFT_KEY);
    if (savedDraft) {
      try {
        const parsed = JSON.parse(savedDraft);
        if (Array.isArray(parsed) && parsed.length > 0) {
          setItems(parsed);
        }
      } catch (e) {
        console.error("Failed to parse order draft:", e);
      }
    }
  }, []);

  // Save draft to localStorage whenever items change (only after mounted)
  useEffect(() => {
    if (mounted) {
      localStorage.setItem(ORDER_DRAFT_KEY, JSON.stringify(items));
    }
  }, [items, mounted]);

  function updateItem(index: number, field: string, value: any) {
    const copy = [...items];
    (copy[index] as any)[field] = value;
    setItems(copy);
  }

  function addRow() {
    setItems([...items, { barcode: "", quantity: 1, sellingPrice: 0 }]);
  }

  function removeRow(index: number) {
    setItems(items.filter((_, i) => i !== index));
  }

  async function submit() {
    try {
      const orderItems = items.map((item) => {
        const product = products.find(
          (p) => p.barcode === item.barcode
        );

        if (!product) {
          throw new Error(`Invalid barcode: ${item.barcode}`);
        }

        return {
          productId: product.id,
          quantity: item.quantity,
          sellingPrice: item.sellingPrice,
        };
      });

      setLoading(true);
      await onCreate(orderItems);

      // Clear draft after successful order creation
      if (mounted) {
        localStorage.removeItem(ORDER_DRAFT_KEY);
      }
      setItems([{ barcode: "", quantity: 1, sellingPrice: 0 }]);
    } catch (e: any) {
      toast.error(e.message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <div>
      <div className="overflow-x-auto">
<table className="w-full border-collapse">
          <thead>
            <tr className="border-b-2 border-gray-200">
              <th className="px-2 py-2.5 text-base font-semibold text-left text-gray-700">Barcode</th>
              <th className="px-2 py-2.5 text-base font-semibold text-left text-gray-700">Quantity</th>
              <th className="px-2 py-2.5 text-base font-semibold text-left text-gray-700">Selling Price</th>
              {items.length > 1 && (
                <th className="px-2 py-2.5 text-base font-semibold text-left text-gray-700 w-16">Action</th>
              )}
            </tr>
          </thead>
          <tbody>
            {items.map((item, i) => (
              <tr key={i} className="border-b border-gray-100">
                <td className="px-2 py-2.5">
                  <input
                    value={item.barcode}
                    onChange={(e) =>
                      updateItem(i, "barcode", e.target.value)
                    }
                    className="w-full px-3 py-2 text-base border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    placeholder="Enter barcode..."
                  />
                </td>
                <td className="px-2 py-2.5">
                  <input
                    type="number"
                    min={1}
                    value={item.quantity}
                    onChange={(e) =>
                      updateItem(i, "quantity", Number(e.target.value))
                    }
                    className="w-full px-3 py-2 text-base border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  />
                </td>
                <td className="px-2 py-2.5">
                  <input
                    type="number"
                    min={0}
                    value={item.sellingPrice}
                    onChange={(e) =>
                      updateItem(i, "sellingPrice", Number(e.target.value))
                    }
                    className="w-full px-3 py-2 text-base border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  />
                </td>
                <td className="px-2 py-2.5">
                  {items.length > 1 && (
                    <button
                      onClick={() => removeRow(i)}
                      className="px-3 py-1.5 text-sm text-white transition-colors bg-red-500 rounded-md hover:bg-red-600 cursor-pointer"
                    >
                      Remove
                    </button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <div className="mt-4 flex justify-between items-center">
        <button
          onClick={addRow}
          className="px-4 py-2 text-base font-medium text-white bg-blue-500 rounded-md hover:bg-blue-600 transition-colors cursor-pointer"
        >
          + Add Item
        </button>
        <button
          onClick={submit}
          disabled={loading}
          className={`px-4 py-2 text-base font-medium text-white rounded-md transition-colors ${
            loading 
              ? "bg-gray-400 cursor-not-allowed opacity-60" 
              : "bg-blue-500 hover:bg-blue-600 cursor-pointer"
          }`}
        >
          {loading ? "Creating..." : "Create Order"}
        </button>
      </div>
    </div>
  );
}


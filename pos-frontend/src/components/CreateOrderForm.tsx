"use client";

import { useState } from "react";
import { ProductData } from "@/lib/types";

interface OrderItemRow {
  barcode: string;
  quantity: number;
  sellingPrice: number;
}

export default function CreateOrderForm({
  products,
  onCreate,
}: {
  products: ProductData[];
  onCreate: (items: {
    productId: number;
    quantity: number;
    sellingPrice: number;
  }[]) => Promise<void>;
}) {
  const [items, setItems] = useState<OrderItemRow[]>([
    { barcode: "", quantity: 1, sellingPrice: 0 },
  ]);
  const [loading, setLoading] = useState(false);

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

      alert("Order created successfully");
      setItems([{ barcode: "", quantity: 1, sellingPrice: 0 }]);
    } catch (e: any) {
      alert(e.message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <div>
      <table border={1} cellPadding={8} style={{ marginTop: 16 }}>
        <thead>
          <tr>
            <th>Barcode</th>
            <th>Quantity</th>
            <th>Selling Price</th>
            <th>Action</th>
          </tr>
        </thead>
        <tbody>
          {items.map((item, i) => (
            <tr key={i}>
              <td>
                <input
                  value={item.barcode}
                  onChange={(e) =>
                    updateItem(i, "barcode", e.target.value)
                  }
                />
              </td>
              <td>
                <input
                  type="number"
                  min={1}
                  value={item.quantity}
                  onChange={(e) =>
                    updateItem(i, "quantity", Number(e.target.value))
                  }
                />
              </td>
              <td>
                <input
                  type="number"
                  min={0}
                  value={item.sellingPrice}
                  onChange={(e) =>
                    updateItem(i, "sellingPrice", Number(e.target.value))
                  }
                />
              </td>
              <td>
                {items.length > 1 && (
                  <button onClick={() => removeRow(i)}>❌</button>
                )}
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      <div style={{ marginTop: 12 }}>
        <button onClick={addRow}>+ Add Item</button>
      </div>

      <div style={{ marginTop: 16 }}>
        <button onClick={submit} disabled={loading}>
          {loading ? "Creating..." : "Create Order"}
        </button>
      </div>
    </div>
  );
}

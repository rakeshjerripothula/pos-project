"use client";

import { useState, useEffect } from "react";
import { apiGet, apiPost } from "@/lib/api";
import { ProductData } from "@/lib/types";
import CreateOrderForm from "@/components/CreateOrderForm";
import { useRouter } from "next/navigation";
import AuthGuard from "@/components/AuthGuard";
import toast from "react-hot-toast";

export default function CreateOrderPage() {
  const [products, setProducts] = useState<ProductData[]>([]);
  const [loading, setLoading] = useState(true);
  const router = useRouter();

  useEffect(() => {
    loadProducts();
  }, []);

  async function loadProducts() {
    try {
      const data = await apiGet<ProductData[]>("/products");
      setProducts(data);
    } catch (error: any) {
      toast.error("Failed to load products: " + error.message);
    } finally {
      setLoading(false);
    }
  }

  async function createOrder(orderItems: {
    productId: number;
    quantity: number;
    sellingPrice: number;
  }[]) {
    await apiPost("/orders", { items: orderItems });
    router.push("/orders");
  }

  return (
    <AuthGuard>
      <div className="min-h-[calc(100vh-64px)] bg-slate-50 p-4">
        <div className="max-w-[1400px] mx-auto">
          <div className="flex items-center justify-between mb-4">
            <h1 className="text-2xl font-bold text-slate-800">
              Create Order
            </h1>
            <button
              onClick={() => router.push("/orders")}
              className="px-4 py-2 text-sm font-medium text-white bg-blue-500 rounded-md hover:bg-blue-600 transition-colors cursor-pointer"
            >
              View All Orders
            </button>
          </div>

          {loading ? (
            <div className="py-8 text-center text-slate-500">Loading...</div>
          ) : (
            <div className="p-4 bg-white rounded-lg shadow-sm">
              <CreateOrderForm products={products} onCreate={createOrder} />
            </div>
          )}
        </div>
      </div>
    </AuthGuard>
  );
}



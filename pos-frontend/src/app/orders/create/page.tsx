"use client";

import { useState, useEffect } from "react";
import { apiGet } from "@/lib/api";
import { ProductData } from "@/lib/types";
import CreateOrderForm from "@/components/CreateOrderForm";
import { useRouter } from "next/navigation";
import AuthGuard from "@/components/AuthGuard";

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
      alert("Failed to load products: " + error.message);
    } finally {
      setLoading(false);
    }
  }

  async function createOrder(orderItems: {
    productId: number;
    quantity: number;
    sellingPrice: number;
  }[]) {
    const res = await fetch("http://localhost:8080/orders", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ items: orderItems }),
    });

    if (!res.ok) {
      throw new Error(await res.text());
    }

    router.push("/orders");
  }

  return (
    <AuthGuard>
      <div
        style={{
          minHeight: "calc(100vh - 64px)",
          backgroundColor: "#f8fafc",
          padding: 24,
        }}
      >
        <div style={{ maxWidth: 1400, margin: "0 auto" }}>
          <h1
            style={{
              fontSize: 28,
              fontWeight: "bold",
              color: "#1e293b",
              marginBottom: 24,
            }}
          >
            Create Order
          </h1>

          {loading ? (
            <div style={{ textAlign: "center", padding: 48 }}>Loading...</div>
          ) : (
            <div
              style={{
                backgroundColor: "white",
                borderRadius: 12,
                padding: 24,
                boxShadow: "0 1px 3px rgba(0,0,0,0.1)",
              }}
            >
              <CreateOrderForm products={products} onCreate={createOrder} />
            </div>
          )}
        </div>
      </div>
    </AuthGuard>
  );
}

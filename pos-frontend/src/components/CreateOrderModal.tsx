"use client";

import { useState, useEffect } from "react";
import { apiGet, apiPost } from "@/lib/api";
import { ProductData } from "@/lib/types";
import CreateOrderForm from "@/components/CreateOrderForm";
import toast from "react-hot-toast";

interface CreateOrderModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSuccess: () => void;
}

export default function CreateOrderModal({ isOpen, onClose, onSuccess }: CreateOrderModalProps) {
  const [products, setProducts] = useState<ProductData[]>([]);
  const [loading, setLoading] = useState(true);

  // Load products when modal opens
  useEffect(() => {
    if (isOpen) {
      loadProducts();
    }
  }, [isOpen]);

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

  // Don't render if modal is not open
  if (!isOpen) return null;

  async function createOrder(orderItems: {
    productId: number;
    quantity: number;
    sellingPrice: number;
  }[]) {
    try {
      await apiPost("/orders", { items: orderItems });
      toast.success("Order created successfully");
      onSuccess();
      onClose();
    } catch (error: any) {
      throw error; // Re-throw to let form handle error display
    }
  }

  return (
    <div
      className="fixed inset-0 bg-black/50 flex items-start justify-center z-[1000] pt-20"
      onClick={(e) => {
        // Only close if clicking the overlay, not the modal content
        if (e.target === e.currentTarget) {
          onClose();
        }
      }}
    >
      <div
        className="bg-white p-6 rounded-lg max-w-[90%] w-[900px] max-h-[90vh] overflow-auto"
        onClick={(e) => e.stopPropagation()}
      >
        <div className="flex items-center justify-between mb-6">
          <h2 className="text-xl font-bold text-slate-800">Create Order</h2>
          <button
            onClick={onClose}
            className="text-2xl bg-transparent border-none cursor-pointer hover:text-gray-600 text-gray-500"
          >
            ×
          </button>
        </div>

        {loading ? (
          <div className="py-8 text-center text-slate-500">Loading products...</div>
        ) : (
          <CreateOrderForm 
            products={products} 
            onCreate={createOrder}
            onCancel={onClose}
          />
        )}
      </div>
    </div>
  );
}


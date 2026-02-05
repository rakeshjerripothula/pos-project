"use client";

import { useEffect } from "react";
import { useRouter } from "next/navigation";
import { getUser } from "@/lib/auth";
import Link from "next/link";

export default function Home() {
  const router = useRouter();

  useEffect(() => {
    const user = getUser();
    if (user) {
      router.push("/orders");
    }
  }, [router]);

  return (
    <div className="flex min-h-screen">
      {/* Left Panel - Main Content */}
      <div className="flex-1 flex flex-col items-center justify-center px-12 py-12 bg-gradient-to-br from-blue-500 to-purple-600">
        <div className="max-w-lg text-center">
          {/* Logo */}
          <div className="flex items-center justify-center gap-4 mb-8">
            <div className="w-16 h-16 bg-white rounded-xl flex items-center justify-center text-blue-500 text-3xl font-bold shadow-lg">
              P
            </div>
            <h1 className="text-5xl font-bold text-white">
              PoS System
            </h1>
          </div>

          <p className="text-xl text-white/90 mb-12 leading-relaxed">
            Point of Sale Management System
          </p>

          <div className="flex flex-wrap justify-center gap-4">
            <Link
              href="/login"
              className="inline-block px-8 py-3.5 bg-white text-blue-500 no-underline rounded-lg text-base font-semibold shadow-md hover:shadow-lg hover:-translate-y-0.5 transition-all duration-200"
            >
              Sign In
            </Link>

            <Link
              href="/signup"
              className="inline-block px-8 py-3.5 text-white no-underline rounded-lg text-base font-semibold border-2 border-white hover:bg-white/10 transition-all duration-200"
            >
              Create Account
            </Link>
          </div>
        </div>
      </div>

      {/* Right Panel - Features - Hidden on small screens */}
      <div className="flex-1 hidden lg:flex items-center justify-center px-12 py-12 bg-slate-50">
        <div className="max-w-md">
          <h2 className="text-3xl font-bold text-slate-800 mb-8">
            Streamline Your Sales Operations
          </h2>

          <div className="flex flex-col gap-6">
            {[
              {
                title: "Client Management",
                description: "Track and manage your customers with ease",
                icon: "👥",
              },
              {
                title: "Product Catalog",
                description: "Organize and manage your product inventory",
                icon: "📦",
              },
              {
                title: "Order Processing",
                description: "Create and track orders efficiently",
                icon: "🛒",
              },
              {
                title: "Sales Reports",
                description: "Generate insights with detailed reports",
                icon: "📊",
              },
            ].map((feature) => (
              <div
                key={feature.title}
                className="flex items-start gap-4 p-5 bg-white rounded-xl shadow-sm"
              >
                <div className="w-12 h-12 bg-slate-100 rounded-lg flex items-center justify-center text-2xl flex-shrink-0">
                  {feature.icon}
                </div>
                <div>
                  <h3 className="text-lg font-semibold text-slate-800 mb-1">
                    {feature.title}
                  </h3>
                  <p className="text-sm text-slate-500">
                    {feature.description}
                  </p>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}


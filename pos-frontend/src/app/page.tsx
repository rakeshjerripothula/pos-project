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
    <div style={{ display: "flex", minHeight: "100vh" }}>
      {/* Left Panel - Main Content */}
      <div
        style={{
          flex: 1,
          display: "flex",
          flexDirection: "column",
          alignItems: "center",
          justifyContent: "center",
          padding: "48px",
          background: "linear-gradient(135deg, #667eea 0%, #764ba2 100%)",
        }}
      >
        <div style={{ maxWidth: 600, textAlign: "center" }}>
          {/* Logo */}
          <div
            style={{
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
              gap: 16,
              marginBottom: 32,
            }}
          >
            <div
              style={{
                width: 64,
                height: 64,
                borderRadius: 16,
                backgroundColor: "white",
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
                color: "#667eea",
                fontSize: 32,
                fontWeight: "bold",
              }}
            >
              P
            </div>
            <h1
              style={{
                fontSize: 48,
                fontWeight: "bold",
                color: "white",
              }}
            >
              PoS System
            </h1>
          </div>

          <p
            style={{
              fontSize: 20,
              color: "rgba(255,255,255,0.9)",
              marginBottom: 48,
              lineHeight: 1.6,
            }}
          >
            Point of Sale Management System
          </p>

          <div
            style={{
              display: "flex",
              gap: 16,
              justifyContent: "center",
              flexWrap: "wrap",
            }}
          >
            <Link
              href="/login"
              style={{
                display: "inline-block",
                padding: "14px 32px",
                backgroundColor: "white",
                color: "#667eea",
                textDecoration: "none",
                borderRadius: 8,
                fontSize: 16,
                fontWeight: 600,
                transition: "all 0.2s",
                boxShadow: "0 4px 6px rgba(0,0,0,0.1)",
              }}
              onMouseEnter={(e) => {
                e.currentTarget.style.transform = "translateY(-2px)";
                e.currentTarget.style.boxShadow = "0 6px 12px rgba(0,0,0,0.15)";
              }}
              onMouseLeave={(e) => {
                e.currentTarget.style.transform = "translateY(0)";
                e.currentTarget.style.boxShadow = "0 4px 6px rgba(0,0,0,0.1)";
              }}
            >
              Sign In
            </Link>

            <Link
              href="/signup"
              style={{
                display: "inline-block",
                padding: "14px 32px",
                backgroundColor: "transparent",
                color: "white",
                textDecoration: "none",
                borderRadius: 8,
                fontSize: 16,
                fontWeight: 600,
                border: "2px solid white",
                transition: "all 0.2s",
              }}
              onMouseEnter={(e) => {
                e.currentTarget.style.backgroundColor = "rgba(255,255,255,0.1)";
              }}
              onMouseLeave={(e) => {
                e.currentTarget.style.backgroundColor = "transparent";
              }}
            >
              Create Account
            </Link>
          </div>
        </div>
      </div>

      {/* Right Panel - Features */}
      <div
        style={{
          flex: 1,
          display: "none",
          alignItems: "center",
          justifyContent: "center",
          padding: 48,
          backgroundColor: "#f8fafc",
        }}
        className="lg:flex"
      >
        <div style={{ maxWidth: 500 }}>
          <h2
            style={{
              fontSize: 32,
              fontWeight: "bold",
              color: "#1e293b",
              marginBottom: 32,
            }}
          >
            Streamline Your Sales Operations
          </h2>

          <div style={{ display: "flex", flexDirection: "column", gap: 24 }}>
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
                style={{
                  display: "flex",
                  alignItems: "flex-start",
                  gap: 16,
                  padding: 20,
                  backgroundColor: "white",
                  borderRadius: 12,
                  boxShadow: "0 2px 4px rgba(0,0,0,0.05)",
                }}
              >
                <div
                  style={{
                    width: 48,
                    height: 48,
                    borderRadius: 12,
                    backgroundColor: "#f1f5f9",
                    display: "flex",
                    alignItems: "center",
                    justifyContent: "center",
                    fontSize: 24,
                    flexShrink: 0,
                  }}
                >
                  {feature.icon}
                </div>
                <div>
                  <h3
                    style={{
                      fontSize: 18,
                      fontWeight: 600,
                      color: "#1e293b",
                      marginBottom: 4,
                    }}
                  >
                    {feature.title}
                  </h3>
                  <p style={{ fontSize: 14, color: "#64748b" }}>
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

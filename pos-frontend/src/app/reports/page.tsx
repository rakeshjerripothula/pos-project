"use client";

import Link from "next/link";
import AuthGuard from "@/components/AuthGuard";

export default function ReportsPage() {
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
            Reports
          </h1>

          <div
            style={{
              display: "grid",
              gridTemplateColumns: "repeat(auto-fit, minmax(300px, 1fr))",
              gap: 24,
            }}
          >
            <Link
              href="/reports/sales"
              style={{
                padding: 32,
                backgroundColor: "white",
                borderRadius: 12,
                textDecoration: "none",
                color: "inherit",
                display: "block",
                boxShadow: "0 1px 3px rgba(0,0,0,0.1)",
                transition: "all 0.2s",
              }}
              onMouseEnter={(e) => {
                e.currentTarget.style.transform = "translateY(-4px)";
                e.currentTarget.style.boxShadow = "0 4px 12px rgba(0,0,0,0.15)";
              }}
              onMouseLeave={(e) => {
                e.currentTarget.style.transform = "translateY(0)";
                e.currentTarget.style.boxShadow = "0 1px 3px rgba(0,0,0,0.1)";
              }}
            >
              <h2
                style={{
                  fontSize: 22,
                  fontWeight: "bold",
                  marginBottom: 12,
                  color: "#1e293b",
                }}
              >
                Sales Report
              </h2>
              <p style={{ color: "#64748b", fontSize: 14, lineHeight: 1.6 }}>
                View sales aggregated by client for a specified date range
              </p>
            </Link>

            <Link
              href="/reports/day-sales"
              style={{
                padding: 32,
                backgroundColor: "white",
                borderRadius: 12,
                textDecoration: "none",
                color: "inherit",
                display: "block",
                boxShadow: "0 1px 3px rgba(0,0,0,0.1)",
                transition: "all 0.2s",
              }}
              onMouseEnter={(e) => {
                e.currentTarget.style.transform = "translateY(-4px)";
                e.currentTarget.style.boxShadow = "0 4px 12px rgba(0,0,0,0.15)";
              }}
              onMouseLeave={(e) => {
                e.currentTarget.style.transform = "translateY(0)";
                e.currentTarget.style.boxShadow = "0 1px 3px rgba(0,0,0,0.1)";
              }}
            >
              <h2
                style={{
                  fontSize: 22,
                  fontWeight: "bold",
                  marginBottom: 12,
                  color: "#1e293b",
                }}
              >
                Day-on-Day Sales Report
              </h2>
              <p style={{ color: "#64748b", fontSize: 14, lineHeight: 1.6 }}>
                View daily sales totals for invoiced orders
              </p>
            </Link>
          </div>
        </div>
      </div>
    </AuthGuard>
  );
}

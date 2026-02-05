"use client";

import Link from "next/link";
import AuthGuard from "@/components/AuthGuard";

export default function ReportsPage() {
  return (
    <AuthGuard requiredRole="SUPERVISOR">
      <div className="min-h-[calc(100vh-64px)] bg-slate-50 p-4">
        <div className="max-w-[1400px] mx-auto">
          <h1 className="mb-4 text-2xl font-bold text-slate-800">
            Reports
          </h1>

          <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
            <Link
              href="/reports/sales"
              className="block p-5 text-white transition-all bg-blue-500 rounded-lg hover:bg-blue-600 hover:shadow-md no-underline"
            >
              <h2 className="mb-2 text-lg font-bold">
                Sales Report
              </h2>
              <p className="text-sm text-white/80">
                View sales aggregated by client for a specified date range
              </p>
            </Link>

            <Link
              href="/reports/day-sales"
              className="block p-5 text-white transition-all bg-blue-500 rounded-lg hover:bg-blue-600 hover:shadow-md no-underline"
            >
              <h2 className="mb-2 text-lg font-bold">
                Day-on-Day Sales Report
              </h2>
              <p className="text-sm text-white/80">
                View daily sales totals for invoiced orders
              </p>
            </Link>
          </div>
        </div>
      </div>
    </AuthGuard>
  );
}


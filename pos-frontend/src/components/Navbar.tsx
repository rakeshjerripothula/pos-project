"use client";

import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { getUser, clearAllAuth, User } from "@/lib/auth";
import { useEffect, useState } from "react";
import { isOperator } from "@/components/AuthGuard";

// Event name for user updates
const USER_UPDATE_EVENT = "pos-user-update";

export default function Navbar() {
  const pathname = usePathname();
  const router = useRouter();
  const [user, setUser] = useState<User | null>(null);
  const [mounted, setMounted] = useState(false);

  useEffect(() => {
    setMounted(true);
    
    // Get initial user from global variable set by AuthGuard
    const globalUser = (window as any).__CURRENT_USER__;
    if (globalUser) {
      setUser(globalUser);
    } else {
      // Fallback to sessionStorage
      setUser(getUser());
    }

    // Listen for user updates (from logout/login)
    const handleUserUpdate = () => {
      // First check global user, then sessionStorage
      const updatedUser = (window as any).__CURRENT_USER__ || getUser();
      setUser(updatedUser);
    };

    window.addEventListener(USER_UPDATE_EVENT, handleUserUpdate);

    return () => {
      window.removeEventListener(USER_UPDATE_EVENT, handleUserUpdate);
    };
  }, []);

  function handleLogout() {
    clearAllAuth();
    // Clear global user
    (window as any).__CURRENT_USER__ = null;
    // Dispatch event to update all components
    window.dispatchEvent(new Event(USER_UPDATE_EVENT));
    // Redirect to login page
    router.replace("/login");
  }

  // Don't show navbar on auth pages
  if (pathname === "/login" || pathname === "/signup" || pathname === "/") {
    return null;
  }

  if (!mounted) {
    return null;
  }

  const navItems = [
    { href: "/clients", label: "Clients" },
    { href: "/products", label: "Products" },
    { href: "/inventory", label: "Inventory" },
    { href: "/orders", label: "Orders" },
    // Hide Reports for OPERATORs - they cannot access reports
    ...(!isOperator() ? [{ href: "/reports", label: "Reports" }] as const : []),
  ];

  return (
    <nav className="px-6 py-4 bg-slate-800 shadow-md">
      <div className="flex items-center justify-between max-w-[1400px] mx-auto">
        <div className="flex items-center gap-8">
          <Link
            href="/orders"
            className="text-xl font-bold text-white no-underline"
          >
            PoS System
          </Link>
          <div className="flex gap-1">
            {navItems.map((item) => (
              <Link
                key={item.href}
                href={item.href}
                className={`px-4 py-2 rounded-md transition-all duration-200 text-sm font-medium no-underline border-b-2 ${
                  pathname === item.href
                    ? "border-blue-400 text-white bg-white/10"
                    : "border-transparent text-white hover:bg-white/10"
                }`}
              >
                {item.label}
              </Link>
            ))}
          </div>
        </div>
        <div className="flex items-center gap-4">
          {user ? (
            <>
              <div className="flex items-center gap-2 text-sm">
                <span className="text-slate-400">{user.email}</span>
                <span className={`px-2 py-0.5 text-xs rounded-full ${
                  user.role === "SUPERVISOR" 
                    ? "bg-emerald-500" 
                    : "bg-blue-500"
                }`}>
                  {user.role}
                </span>
              </div>
              <button
                onClick={handleLogout}
                className="px-4 py-1.5 text-sm text-white transition-colors bg-red-500 border-none rounded-md hover:bg-red-600 cursor-pointer"
              >
                Logout
              </button>
            </>
          ) : (
            <Link
              href="/login"
              className="px-4 py-1.5 text-sm font-medium text-white no-underline rounded-md bg-blue-500 hover:bg-blue-600 transition-colors"
            >
              Sign In
            </Link>
          )}
        </div>
      </div>
    </nav>
  );
}


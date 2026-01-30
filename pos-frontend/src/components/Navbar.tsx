
"use client";

import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { getUser, clearAllAuth, User } from "@/lib/auth";
import { useEffect, useState } from "react";

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
    { href: "/reports", label: "Reports" },
  ];

  return (
    <nav
      style={{
        backgroundColor: "#1e293b",
        color: "white",
        padding: "16px 24px",
        boxShadow: "0 2px 4px rgba(0,0,0,0.1)",
      }}
    >
      <div
        style={{
          maxWidth: 1400,
          margin: "0 auto",
          display: "flex",
          justifyContent: "space-between",
          alignItems: "center",
        }}
      >
        <div style={{ display: "flex", alignItems: "center", gap: 32 }}>
          <Link
            href="/orders"
            style={{
              fontSize: 20,
              fontWeight: "bold",
              color: "white",
              textDecoration: "none",
            }}
          >
            PoS System
          </Link>
          <div style={{ display: "flex", gap: 16 }}>
            {navItems.map((item) => (
              <Link
                key={item.href}
                href={item.href}
                style={{
                  color: pathname === item.href ? "#60a5fa" : "white",
                  textDecoration: "none",
                  padding: "8px 12px",
                  borderRadius: 4,
                  backgroundColor:
                    pathname === item.href ? "rgba(96, 165, 250, 0.1)" : "transparent",
                  transition: "all 0.2s",
                }}
                onMouseEnter={(e) => {
                  if (pathname !== item.href) {
                    e.currentTarget.style.backgroundColor = "rgba(255,255,255,0.1)";
                  }
                }}
                onMouseLeave={(e) => {
                  if (pathname !== item.href) {
                    e.currentTarget.style.backgroundColor = "transparent";
                  }
                }}
              >
                {item.label}
              </Link>
            ))}
          </div>
        </div>
        <div style={{ display: "flex", alignItems: "center", gap: 16 }}>
          {user ? (
            <>
              <div style={{ fontSize: 14, display: "flex", alignItems: "center", gap: 8 }}>
                <span style={{ color: "#94a3b8" }}>{user.email}</span>
                <span
                  style={{
                    padding: "2px 8px",
                    backgroundColor: user.role === "SUPERVISOR" ? "#10b981" : "#3b82f6",
                    borderRadius: 12,
                    fontSize: 12,
                  }}
                >
                  {user.role}
                </span>
              </div>
              <button
                onClick={handleLogout}
                style={{
                  padding: "6px 16px",
                  backgroundColor: "#ef4444",
                  color: "white",
                  border: "none",
                  borderRadius: 4,
                  cursor: "pointer",
                  fontSize: 14,
                  transition: "all 0.2s",
                }}
                onMouseEnter={(e) => {
                  e.currentTarget.style.backgroundColor = "#dc2626";
                }}
                onMouseLeave={(e) => {
                  e.currentTarget.style.backgroundColor = "#ef4444";
                }}
              >
                Logout
              </button>
            </>
          ) : (
            <Link
              href="/login"
              style={{
                padding: "6px 16px",
                backgroundColor: "#667eea",
                color: "white",
                textDecoration: "none",
                borderRadius: 4,
                fontSize: 14,
                fontWeight: 500,
                transition: "all 0.2s",
              }}
              onMouseEnter={(e) => {
                e.currentTarget.style.backgroundColor = "#5568d3";
              }}
              onMouseLeave={(e) => {
                e.currentTarget.style.backgroundColor = "#667eea";
              }}
            >
              Sign In
            </Link>
          )}
        </div>
      </div>
    </nav>
  );
}


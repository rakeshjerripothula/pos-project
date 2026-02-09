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
  const [isMenuOpen, setIsMenuOpen] = useState(false);

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
    // Close menu on mobile
    setIsMenuOpen(false);
  }

  function closeMenu() {
    setIsMenuOpen(false);
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
    <>
      <nav className="px-4 md:px-6 py-3 md:py-4 bg-slate-800 shadow-md sticky top-0 z-50">
        <div className="flex items-center justify-between max-w-[1400px] mx-auto">
          {/* Logo + Navigation Links - Left side */}
          <div className="flex items-center gap-4 md:gap-8">
            <Link
              href="/orders"
              className="text-lg md:text-xl font-bold text-white no-underline"
            >
              PoS System
            </Link>
            
            {/* Desktop Navigation Links - Hidden on mobile */}
            <div className="hidden md:flex gap-1">
              {navItems.map((item) => (
                <Link
                  key={item.href}
                  href={item.href}
                  className={`px-3 lg:px-4 py-2 rounded-md transition-all duration-200 text-sm font-medium no-underline border-b-2 ${
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

          {/* User Section - Right side */}
          <div className="hidden md:flex items-center gap-3 lg:gap-4">
            {user ? (
              <>
                <div className="flex items-center gap-2 text-sm">
                  <span className="text-slate-400 hidden lg:inline">{user.email}</span>
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
                  className="px-3 lg:px-4 py-1.5 text-sm text-white transition-colors bg-red-500 border-none rounded-md hover:bg-red-600 cursor-pointer"
                >
                  Logout
                </button>
              </>
            ) : (
              <Link
                href="/login"
                className="px-3 lg:px-4 py-1.5 text-sm font-medium text-white no-underline rounded-md bg-blue-500 hover:bg-blue-600 transition-colors"
              >
                Sign In
              </Link>
            )}
          </div>

          {/* Mobile Hamburger Button - Hidden on desktop */}
          <button
            onClick={() => setIsMenuOpen(!isMenuOpen)}
            className="md:hidden p-2 text-white hover:bg-white/10 rounded-md transition-colors cursor-pointer"
            aria-label="Toggle menu"
          >
            {isMenuOpen ? (
              <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
              </svg>
            ) : (
              <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 12h16M4 18h16" />
              </svg>
            )}
          </button>
        </div>
      </nav>

      {/* Mobile Menu Overlay */}
      {isMenuOpen && (
        <div className="fixed inset-0 z-40 md:hidden">
          {/* Backdrop */}
          <div 
            className="absolute inset-0 bg-black/50"
            onClick={closeMenu}
          />
          {/* Menu Content */}
          <div className="absolute top-[57px] left-0 right-0 bg-slate-800 shadow-lg border-t border-slate-700">
            <div className="flex flex-col py-2">
              {/* Nav Links - Right aligned */}
              <div className="flex flex-col">
                {navItems.map((item) => (
                  <Link
                    key={item.href}
                    href={item.href}
                    onClick={closeMenu}
                    className={`px-4 py-3 transition-all duration-200 text-base font-medium no-underline border-l-4 ${
                      pathname === item.href
                        ? "border-blue-400 text-white bg-white/10"
                        : "border-transparent text-white hover:bg-white/10"
                    }`}
                  >
                    {item.label}
                  </Link>
                ))}
              </div>
              
              {/* Divider */}
              <div className="my-2 border-t border-slate-700" />
              
              {/* User Section - Right aligned */}
              {user ? (
                <div className="px-4 py-2 flex flex-col gap-2">
                  <div className="flex items-center gap-2 justify-end">
                    <span className="text-slate-400 text-sm">{user.email}</span>
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
                    className="px-4 py-2 text-sm font-medium text-white transition-colors bg-red-500 border-none rounded-md hover:bg-red-600 cursor-pointer text-right"
                  >
                    Logout
                  </button>
                </div>
              ) : (
                <div className="px-4 py-2">
                  <Link
                    href="/login"
                    onClick={closeMenu}
                    className="block px-4 py-2 text-sm font-medium text-white no-underline rounded-md bg-blue-500 hover:bg-blue-600 transition-colors text-center"
                  >
                    Sign In
                  </Link>
                </div>
              )}
            </div>
          </div>
        </div>
      )}
    </>
  );
}


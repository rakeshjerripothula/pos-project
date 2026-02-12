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

  // Helper function to get user initials safely
  function getUserInitials(email: string | undefined): string {
    if (!email || typeof email !== 'string') return '?';
    return email.charAt(0).toUpperCase();
  }

  return (
    <>
      <nav className="w-full px-4 sm:px-6 md:px-8 py-2.5 bg-slate-900 shadow-lg sticky top-0 z-50">
        <div className="w-full max-w-[1400px] mx-auto">
          {/* Logo + Navigation Links - Left side */}
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-6 md:gap-10">
              <Link
                href="/orders"
                className="no-underline flex items-center"
              >
                <span className="text-xl md:text-2xl font-bold text-white tracking-wide">
                  PoS System
                </span>
              </Link>
              
              {/* Desktop Navigation Links - Hidden on mobile */}
              <div className="hidden md:flex gap-3">
                {navItems.map((item) => (
                  <Link
                    key={item.href}
                    href={item.href}
                    className={`px-4 py-2 rounded-lg transition-all duration-200 text-lg font-semibold no-underline tracking-wide ${
                      pathname === item.href
                        ? "bg-white/15 text-white shadow-sm backdrop-blur-sm"
                        : "text-slate-300 hover:bg-white/10 hover:text-white"
                    }`}
                  >
                    {item.label}
                  </Link>
                ))}
              </div>
            </div>

            {/* User Section - Right side */}
            <div className="hidden md:flex items-center gap-4">
              {user ? (
                <>
                  <div className="flex items-center gap-3">
                    <div className="flex flex-col items-end">
                      <span className="text-sm text-slate-300 font-medium hidden lg:block max-w-[150px] truncate">{user.email}</span>
                      <span className={`px-2.5 py-0.5 text-xs font-bold rounded-full uppercase tracking-wider ${
                        user.role === "SUPERVISOR" 
                          ? "bg-gradient-to-r from-emerald-500 to-emerald-600 text-white shadow-lg shadow-emerald-500/30" 
                          : "bg-gradient-to-r from-blue-500 to-blue-600 text-white shadow-lg shadow-blue-500/30"
                      }`}>
                        {user.role}
                      </span>
                    </div>
                    <div className={`w-10 h-10 rounded-full flex items-center justify-center text-white font-bold text-sm shadow-lg ${
                      user.role === "SUPERVISOR" 
                        ? "bg-gradient-to-br from-emerald-400 to-emerald-600" 
                        : "bg-gradient-to-br from-blue-400 to-blue-600"
                    }`}>
                      {getUserInitials(user.email)}
                    </div>
                  </div>
                  <div className="w-px h-8 bg-slate-600 mx-2" />
                  <button
                    onClick={handleLogout}
                    className="px-4 py-2 text-sm font-semibold text-white transition-all duration-200 bg-gradient-to-r from-red-500 to-red-600 border-none rounded-lg hover:from-red-600 hover:to-red-700 cursor-pointer shadow-lg shadow-red-500/30 hover:shadow-red-500/50 active:scale-95"
                  >
                    Logout
                  </button>
                </>
              ) : (
                <Link
                  href="/login"
                  className="px-5 py-2 text-sm font-semibold text-white no-underline rounded-lg bg-gradient-to-r from-blue-500 to-blue-600 hover:from-blue-600 hover:to-blue-700 transition-all duration-200 shadow-lg shadow-blue-500/30 hover:shadow-blue-500/50 active:scale-95"
                >
                  Sign In
                </Link>
              )}
            </div>

            {/* Mobile Hamburger Button - Hidden on desktop */}
            <button
              onClick={() => setIsMenuOpen(!isMenuOpen)}
              className="md:hidden p-2.5 text-white hover:bg-white/10 rounded-lg transition-colors cursor-pointer active:scale-95"
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
        </div>
      </nav>

      {/* Mobile Menu Overlay */}
      {isMenuOpen && (
        <div className="fixed inset-0 z-40 md:hidden">
          {/* Backdrop */}
          <div 
            className="absolute inset-0 bg-black/60 backdrop-blur-sm"
            onClick={closeMenu}
          />
          {/* Menu Content */}
          <div className="absolute top-[65px] left-4 right-4 bg-slate-800/95 backdrop-blur-xl shadow-2xl rounded-2xl border border-slate-700/50 overflow-hidden">
            <div className="flex flex-col py-3">
              {/* Nav Links */}
              <div className="flex flex-col px-2 gap-1">
                {navItems.map((item) => (
                  <Link
                    key={item.href}
                    href={item.href}
                    onClick={closeMenu}
                    className={`px-4 py-3.5 transition-all duration-200 text-base font-medium no-underline rounded-xl ${
                      pathname === item.href
                        ? "bg-white/15 text-white"
                        : "text-slate-300 hover:bg-white/10 hover:text-white"
                    }`}
                  >
                    {item.label}
                  </Link>
                ))}
              </div>
              
              {/* Divider */}
              <div className="my-3 border-t border-slate-700/50" />
              
              {/* User Section */}
              {user ? (
                <div className="px-4 py-2.5 flex flex-col gap-3">
                  <div className="flex items-center gap-3 justify-between">
                    <div className="flex items-center gap-3">
                      <div className={`w-11 h-11 rounded-full flex items-center justify-center text-white font-bold text-sm shadow-lg ${
                        user.role === "SUPERVISOR" 
                          ? "bg-gradient-to-br from-emerald-400 to-emerald-600" 
                          : "bg-gradient-to-br from-blue-400 to-blue-600"
                      }`}>
                        {getUserInitials(user.email)}
                      </div>
                      <div className="flex flex-col">
                        <span className="text-sm text-slate-300 font-medium max-w-[180px] truncate">{user.email}</span>
                        <span className={`px-2 py-0.5 text-xs font-bold rounded-full uppercase tracking-wider inline-block w-fit ${
                          user.role === "SUPERVISOR" 
                            ? "bg-emerald-500 text-white" 
                            : "bg-blue-500 text-white"
                        }`}>
                          {user.role}
                        </span>
                      </div>
                    </div>
                  </div>
                  <button
                    onClick={handleLogout}
                    className="w-full px-4 py-3 text-base font-semibold text-white transition-all duration-200 bg-gradient-to-r from-red-500 to-red-600 border-none rounded-xl hover:from-red-600 hover:to-red-700 cursor-pointer shadow-lg"
                  >
                    Logout
                  </button>
                </div>
              ) : (
                <div className="px-4 py-3">
                  <Link
                    href="/login"
                    onClick={closeMenu}
                    className="block w-full px-4 py-3 text-base font-semibold text-white no-underline rounded-xl bg-gradient-to-r from-blue-500 to-blue-600 hover:from-blue-600 hover:to-blue-700 transition-all duration-200 text-center shadow-lg"
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


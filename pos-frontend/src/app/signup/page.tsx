"use client";

import { useState, useEffect, useRef } from "react";
import { useRouter } from "next/navigation";
import { 
  saveUser, 
  getUser, 
  isAuthCheckInProgress,
  setAuthCheckStarted,
  setAuthCheckComplete,
  User 
} from "@/lib/auth";
import toast from "react-hot-toast";

// Event name for user updates
const USER_UPDATE_EVENT = "pos-user-update";

export default function SignupPage() {
  const [email, setEmail] = useState("");
  const [loading, setLoading] = useState(false);
  const router = useRouter();
  const initialized = useRef(false);

  useEffect(() => {
    // Only check once on mount
    if (initialized.current) {
      return;
    }
    initialized.current = true;

    const user = getUser();
    if (user) {
      router.replace("/orders");
    }
  }, [router]);

  // Notify components about user update
  function notifyUserUpdate(user: User | null) {
    if (typeof window !== "undefined") {
      (window as any).__CURRENT_USER__ = user;
      window.dispatchEvent(new Event(USER_UPDATE_EVENT));
    }
  }

async function handleSignup(e: React.FormEvent) {
    e.preventDefault();

    // Prevent multiple signups
    if (isAuthCheckInProgress()) {
      return;
    }

    setLoading(true);
    setAuthCheckStarted();

    try {
      // Store email in lowercase and trimmed
      const trimmedEmail = email.trim().toLowerCase();

      const res = await fetch("http://localhost:8080/users/signup", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ 
          email: trimmedEmail, 
        }),
      });

      if (res.status === 409) {
        // User already exists → redirect to login page
        setAuthCheckComplete();
        router.replace("/login");
        return;
      }

      if (!res.ok) {
        const errorData = await res.json();
        throw new Error(errorData.message || "Signup failed");
      }

      const user: User = await res.json();
      saveUser(user);
      setAuthCheckComplete();
      
      // Notify Navbar and other components immediately
      notifyUserUpdate(user);

      router.replace("/orders");
    } catch (err: any) {
      setAuthCheckComplete();
      toast.error(err.message || "Authentication failed. Please try again.");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="flex min-h-screen">
      {/* Left Panel - Form */}
      <div className="flex-1 flex items-center justify-center px-12 py-12 bg-white">
        <div className="w-full max-w-sm">
          {/* Logo */}
          <div className="flex items-center gap-3 mb-10">
            <div className="w-12 h-12 bg-gradient-to-br from-blue-500 to-purple-600 rounded-xl flex items-center justify-center text-white text-xl font-bold">
              P
            </div>
            <span className="text-2xl font-bold text-slate-800">
              PoS System
            </span>
          </div>

          {/* Header */}
          <div className="mb-8">
            <h1 className="text-3xl font-bold text-slate-800 mb-2">
              Create account
            </h1>
<p className="text-base text-slate-500">
              Sign up to get started with PoS System
            </p>
          </div>

          {/* Form */}
          <form onSubmit={handleSignup} className="flex flex-col gap-6">
            <div className="flex flex-col gap-2">
              <label
                htmlFor="email"
                className="text-sm font-medium text-gray-700"
              >
                Email
              </label>
              <input
                id="email"
                type="email"
                placeholder="name@company.com"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
                disabled={loading}
                className="w-full px-4 py-3 text-base border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all"
              />
            </div>

            <button
              type="submit"
              disabled={loading}
              className={`w-full py-3 text-base font-semibold text-white rounded-md transition-all ${
                loading 
                  ? "bg-gray-400 cursor-not-allowed" 
                  : "bg-blue-500 hover:bg-blue-600 cursor-pointer shadow-md hover:shadow-md"
              }`}
            >
              {loading ? "Creating account..." : "Create Account"}
            </button>
          </form>

          {/* Role Info */}
          <div className="mt-6 p-4 rounded-lg bg-slate-50 border border-slate-200">
            <p className="text-sm text-slate-600">
              <strong>Role Assignment:</strong>
            </p>
            <p className="text-sm text-slate-600 mt-1">
              Supervisor: <code className="px-2 py-0.5 bg-slate-200 rounded text-xs">admin@pos.com</code>
            </p>
            <p className="text-sm text-slate-600 mt-1">
              Operator: <em>Any other email</em>
            </p>
          </div>

          {/* Sign in link */}
          <div className="mt-5 text-center">
            <p className="text-sm text-slate-500">
              Already have an account?{" "}
              <a
                href="/login"
                className="text-blue-500 no-underline font-medium hover:underline"
              >
                Sign in
              </a>
            </p>
          </div>
        </div>
      </div>

      {/* Right Panel - Branding - Hidden on small screens */}
      <div className="flex-1 hidden lg:flex items-center justify-center px-12 py-12 bg-gradient-to-br from-blue-500 to-purple-600">
        <div className="max-w-sm text-center">
          <h2 className="text-4xl font-bold text-white mb-6">
            Streamline Your Sales Operations
          </h2>
          <p className="text-lg text-white/80 leading-relaxed mb-12">
            Manage clients, products, inventory, and orders all in one powerful platform.
            Built for efficiency and designed for growth.
          </p>
          <div className="grid grid-cols-3 gap-8">
            <div>
              <p className="text-4xl font-bold text-white">500+</p>
              <p className="text-sm text-white/70 mt-1">Clients</p>
            </div>
            <div>
              <p className="text-4xl font-bold text-white">10K+</p>
              <p className="text-sm text-white/70 mt-1">Products</p>
            </div>
            <div>
              <p className="text-4xl font-bold text-white">200+</p>
              <p className="text-sm text-white/70 mt-1">Orders/Day</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}


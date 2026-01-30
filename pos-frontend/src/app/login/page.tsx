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

// Event name for user updates
const USER_UPDATE_EVENT = "pos-user-update";

export default function LoginPage() {
  const [email, setEmail] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
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

  async function handleLogin(e: React.FormEvent) {
    e.preventDefault();
    setError("");

    // Prevent multiple logins
    if (isAuthCheckInProgress()) {
      return;
    }

    setLoading(true);
    setAuthCheckStarted();

    try {
      // Store email in lowercase and trimmed
      const trimmedEmail = email.trim().toLowerCase();

      const res = await fetch("http://localhost:8080/users/login", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ 
          email: trimmedEmail, 
        }),
      });

      if (!res.ok) {
        const errorData = await res.json();
        throw new Error(errorData.message || "Login failed");
      }

      const user: User = await res.json();
      saveUser(user);
      setAuthCheckComplete();
      
      // Notify Navbar and other components immediately
      notifyUserUpdate(user);

      router.replace("/orders");
    } catch (err: any) {
      setAuthCheckComplete();
      setError(err.message || "Login failed. Please try again.");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div style={{ display: "flex", minHeight: "100vh" }}>
      {/* Left Panel - Form */}
      <div
        style={{
          flex: 1,
          display: "flex",
          alignItems: "center",
          justifyContent: "center",
          padding: "48px",
          backgroundColor: "white",
        }}
      >
        <div style={{ width: "100%", maxWidth: 450 }}>
          {/* Logo */}
          <div style={{ display: "flex", alignItems: "center", gap: 12, marginBottom: 40 }}>
            <div
              style={{
                width: 48,
                height: 48,
                borderRadius: 12,
                background: "linear-gradient(135deg, #667eea 0%, #764ba2 100%)",
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
                color: "white",
                fontSize: 24,
                fontWeight: "bold",
              }}
            >
              P
            </div>
            <span style={{ fontSize: 24, fontWeight: "bold", color: "#1e293b" }}>
              PoS System
            </span>
          </div>

          {/* Header */}
          <div style={{ marginBottom: 32 }}>
            <h1
              style={{
                fontSize: 32,
                fontWeight: "bold",
                color: "#1e293b",
                marginBottom: 8,
              }}
            >
              Welcome back
            </h1>
            <p style={{ fontSize: 16, color: "#64748b" }}>
              Sign in to access the PoS System
            </p>
          </div>

          {/* Error Message */}
          {error && (
            <div
              style={{
                padding: 12,
                marginBottom: 20,
                backgroundColor: "#fee2e2",
                color: "#dc2626",
                borderRadius: 8,
                border: "1px solid #fecaca",
                fontSize: 14,
              }}
            >
              {error}
            </div>
          )}

          {/* Form */}
          <form onSubmit={handleLogin} style={{ display: "flex", flexDirection: "column", gap: 24 }}>
            <div style={{ display: "flex", flexDirection: "column", gap: 8 }}>
              <label
                htmlFor="email"
                style={{
                  fontWeight: 500,
                  color: "#374151",
                  fontSize: 14,
                }}
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
                style={{
                  width: "100%",
                  padding: "12px 16px",
                  border: "1px solid #d1d5db",
                  borderRadius: 8,
                  fontSize: 16,
                  transition: "all 0.2s",
                  boxSizing: "border-box",
                }}
                onFocus={(e) => {
                  e.currentTarget.style.borderColor = "#667eea";
                  e.currentTarget.style.outline = "none";
                  e.currentTarget.style.boxShadow = "0 0 0 3px rgba(102, 126, 234, 0.1)";
                }}
                onBlur={(e) => {
                  e.currentTarget.style.borderColor = "#d1d5db";
                  e.currentTarget.style.boxShadow = "none";
                }}
              />
            </div>

            <button
              type="submit"
              disabled={loading}
              style={{
                width: "100%",
                padding: 14,
                backgroundColor: loading ? "#9ca3af" : "#667eea",
                color: "white",
                border: "none",
                borderRadius: 8,
                fontSize: 16,
                fontWeight: 600,
                cursor: loading ? "not-allowed" : "pointer",
                transition: "all 0.2s",
                boxShadow: loading ? "none" : "0 4px 6px rgba(102, 126, 234, 0.3)",
              }}
              onMouseEnter={(e) => {
                if (!loading) {
                  e.currentTarget.style.backgroundColor = "#5568d3";
                  e.currentTarget.style.transform = "translateY(-1px)";
                }
              }}
              onMouseLeave={(e) => {
                if (!loading) {
                  e.currentTarget.style.backgroundColor = "#667eea";
                  e.currentTarget.style.transform = "translateY(0)";
                }
              }}
            >
              {loading ? "Signing in..." : "Sign In"}
            </button>
          </form>

          {/* Role Info */}
          <div
            style={{
              marginTop: 24,
              padding: 16,
              borderRadius: 8,
              backgroundColor: "#f8fafc",
              border: "1px solid #e2e8f0",
            }}
          >
            <p style={{ fontSize: 14, color: "#64748b" }}>
              <strong>Role Assignment:</strong>
            </p>
            <p style={{ fontSize: 14, color: "#64748b", marginTop: 4 }}>
              Supervisor: <code style={{ backgroundColor: "#e2e8f0", padding: "2px 6px", borderRadius: 4 }}>admin@pos.com</code>
            </p>
            <p style={{ fontSize: 14, color: "#64748b", marginTop: 4 }}>
              Operator: <em>Any other email</em>
            </p>
          </div>

          {/* Sign up link */}
          <div style={{ marginTop: 20, textAlign: "center" }}>
            <p style={{ fontSize: 14, color: "#64748b" }}>
              Don't have an account?{" "}
              <a
                href="/signup"
                style={{
                  color: "#667eea",
                  textDecoration: "none",
                  fontWeight: 500,
                }}
                onMouseEnter={(e) => {
                  e.currentTarget.style.textDecoration = "underline";
                }}
                onMouseLeave={(e) => {
                  e.currentTarget.style.textDecoration = "none";
                }}
              >
                Sign up
              </a>
            </p>
          </div>
        </div>
      </div>

      {/* Right Panel - Branding */}
      <div
        style={{
          flex: 1,
          display: "none",
          alignItems: "center",
          justifyContent: "center",
          padding: 48,
          background: "linear-gradient(135deg, #667eea 0%, #764ba2 100%)",
        }}
        className="lg:flex"
      >
        <div style={{ maxWidth: 400, textAlign: "center" }}>
          <h2
            style={{
              fontSize: 36,
              fontWeight: "bold",
              color: "white",
              marginBottom: 24,
            }}
          >
            Streamline Your Sales Operations
          </h2>
          <p
            style={{
              fontSize: 18,
              color: "rgba(255,255,255,0.8)",
              lineHeight: 1.6,
            }}
          >
            Manage clients, products, inventory, and orders all in one powerful platform.
            Built for efficiency and designed for growth.
          </p>
          <div
            style={{
              marginTop: 48,
              display: "grid",
              gridTemplateColumns: "repeat(3, 1fr)",
              gap: 32,
            }}
          >
            <div>
              <p style={{ fontSize: 32, fontWeight: "bold", color: "white" }}>500+</p>
              <p style={{ fontSize: 14, color: "rgba(255,255,255,0.7)", marginTop: 4 }}>Clients</p>
            </div>
            <div>
              <p style={{ fontSize: 32, fontWeight: "bold", color: "white" }}>10K+</p>
              <p style={{ fontSize: 14, color: "rgba(255,255,255,0.7)", marginTop: 4 }}>Products</p>
            </div>
            <div>
              <p style={{ fontSize: 32, fontWeight: "bold", color: "white" }}>200+</p>
              <p style={{ fontSize: 14, color: "rgba(255,255,255,0.7)", marginTop: 4 }}>Orders/Day</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}


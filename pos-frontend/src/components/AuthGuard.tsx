"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { 
  getAuth, 
  isAuthFresh, 
  isAuthCheckInProgress,
  setAuthCheckStarted,
  setAuthCheckComplete,
  updateAuthTimestamp, 
  clearAuth,
  User 
} from "@/lib/auth";

interface AuthCheckResponse {
  id: number;
  email: string;
  role: "OPERATOR" | "SUPERVISOR";
}

// Event name for user updates
const USER_UPDATE_EVENT = "pos-user-update";

// User role type
export type UserRole = "OPERATOR" | "SUPERVISOR";

interface AuthGuardProps {
  children: React.ReactNode;
  requiredRole?: UserRole;
}

// Helper functions for role checking
export function isOperator(): boolean {
  if (typeof window === "undefined") return false;
  const auth = getAuth();
  return auth?.role === "OPERATOR";
}

export function isSupervisor(): boolean {
  if (typeof window === "undefined") return false;
  const auth = getAuth();
  return auth?.role === "SUPERVISOR";
}

export function canAccess(requiredRole: UserRole): boolean {
  if (requiredRole === "SUPERVISOR") {
    return isSupervisor();
  }
  return true; // Both OPERATOR and SUPERVISOR can access OPERATOR-level pages
}

export default function AuthGuard({ children, requiredRole }: AuthGuardProps) {
  const router = useRouter();
  const [isLoading, setIsLoading] = useState(true);
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [currentUser, setCurrentUser] = useState<User | null>(null);
  const [accessDenied, setAccessDenied] = useState(false);

  // Dispatch event to notify components that user is set
  function notifyUserUpdate(user: User | null) {
    if (typeof window !== "undefined") {
      (window as any).__CURRENT_USER__ = user;
      window.dispatchEvent(new Event(USER_UPDATE_EVENT));
    }
  }

  useEffect(() => {
    // Prevent multiple auth checks
    if (isAuthCheckInProgress()) {
      return;
    }

    async function checkAuth() {
      setAuthCheckStarted();
      
      const auth = getAuth();
      
      if (!auth) {
        // No auth cached, redirect to login
        setAuthCheckComplete();
        notifyUserUpdate(null);
        router.replace("/login");
        return;
      }

      // Check if auth is fresh (within 5-minute window)
      if (isAuthFresh()) {
        // Within 5-minute window, use cached user
        const user: User = {
          id: auth.userId,
          email: auth.email,
          role: auth.role,
        };
        setCurrentUser(user);
        setIsAuthenticated(true);
        
        // Check role-based access
        if (requiredRole && user.role === "OPERATOR" && requiredRole === "SUPERVISOR") {
          setAccessDenied(true);
          setIsLoading(false);
          setAuthCheckComplete();
          notifyUserUpdate(user);
          return;
        }
        
        setIsLoading(false);
        setAuthCheckComplete();
        notifyUserUpdate(user);
        return;
      }

      // Auth expired, need to verify with backend
      try {
        const res = await fetch(`http://localhost:8080/auth/check?userId=${auth.userId}`);
        
        if (!res.ok) {
          // Auth check failed, clear auth and redirect to login
          clearAuth();
          setAuthCheckComplete();
          notifyUserUpdate(null);
          router.replace("/login");
          return;
        }

        const verifiedUser: AuthCheckResponse = await res.json();
        
        // Update cached auth with fresh timestamp
        updateAuthTimestamp();
        
        // Update user data with fresh data from backend
        const user: User = {
          id: verifiedUser.id,
          email: verifiedUser.email,
          role: verifiedUser.role,
        };
        
        setCurrentUser(user);
        setIsAuthenticated(true);
        setAuthCheckComplete();
        notifyUserUpdate(user);
      } catch (error) {
        // Network error - still allow access if user exists in cache
        console.warn("Auth check failed, using cached user:", error);
        const user: User = {
          id: auth.userId,
          email: auth.email,
          role: auth.role,
        };
        setCurrentUser(user);
        setIsAuthenticated(true);
        setAuthCheckComplete();
        notifyUserUpdate(user);
      } finally {
        setIsLoading(false);
      }
    }

    checkAuth();
  }, [router]);

  if (isLoading) {
    return (
      <div
        style={{
          display: "flex",
          justifyContent: "center",
          alignItems: "center",
          minHeight: "100vh",
          backgroundColor: "#f8fafc",
        }}
      >
        <div
          style={{
            textAlign: "center",
          }}
        >
          <div
            style={{
              width: 40,
              height: 40,
              border: "3px solid #e2e8f0",
              borderTopColor: "#667eea",
              borderRadius: "50%",
              animation: "spin 1s linear infinite",
              margin: "0 auto 16px",
            }}
          />
          <p style={{ color: "#64748b", fontSize: 14 }}>Verifying authentication...</p>
        </div>
        <style jsx>{`
          @keyframes spin {
            0% { transform: rotate(0deg); }
            100% { transform: rotate(360deg); }
          }
        `}</style>
      </div>
    );
  }

  if (!isAuthenticated) {
    return null;
  }

  // Show access denied page for OPERATORs trying to access SUPERVISOR-only pages
  if (accessDenied) {
    return (
      <div
        style={{
          display: "flex",
          flexDirection: "column",
          justifyContent: "center",
          alignItems: "center",
          minHeight: "100vh",
          backgroundColor: "#f8fafc",
          padding: 24,
        }}
      >
        <div
          style={{
            textAlign: "center",
            maxWidth: 400,
          }}
        >
          <div
            style={{
              width: 64,
              height: 64,
              backgroundColor: "#fee2e2",
              borderRadius: "50%",
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
              margin: "0 auto 16px",
              fontSize: 32,
            }}
          >
            🔒
          </div>
          <h1
            style={{
              fontSize: 24,
              fontWeight: "bold",
              color: "#991b1b",
              marginBottom: 8,
            }}
          >
            Access Denied
          </h1>
          <p
            style={{
              color: "#64748b",
              fontSize: 16,
              marginBottom: 24,
            }}
          >
            You do not have permission to access this page. Please contact your administrator if you believe this is an error.
          </p>
          <button
            onClick={() => router.replace("/orders")}
            style={{
              padding: "10px 24px",
              backgroundColor: "#667eea",
              color: "white",
              border: "none",
              borderRadius: 8,
              cursor: "pointer",
              fontSize: 14,
              fontWeight: 500,
            }}
          >
            Go to Orders
          </button>
        </div>
      </div>
    );
  }

  return <>{children}</>;
}


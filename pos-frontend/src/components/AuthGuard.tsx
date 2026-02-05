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
      <div className="flex items-center justify-center min-h-screen bg-slate-50">
        <div className="text-center">
          <div className="w-10 h-10 mx-auto mb-4 border-3 border-slate-200 rounded-full border-t-blue-500 animate-spin" />
          <p className="text-sm text-slate-500">Verifying authentication...</p>
        </div>
      </div>
    );
  }

  if (!isAuthenticated) {
    return null;
  }

  // Show access denied page for OPERATORs trying to access SUPERVISOR-only pages
  if (accessDenied) {
    return (
      <div className="flex flex-col items-center justify-center min-h-screen bg-slate-50 p-6">
        <div className="text-center max-w-md">
          <div className="w-16 h-16 mx-auto mb-4 flex items-center justify-center bg-red-100 rounded-full text-3xl">
            🔒
          </div>
          <h1 className="mb-2 text-2xl font-bold text-red-800">
            Access Denied
          </h1>
          <p className="mb-6 text-slate-500">
            You do not have permission to access this page. Please contact your administrator if you believe this is an error.
          </p>
          <button
            onClick={() => router.replace("/orders")}
            className="px-6 py-2.5 text-white bg-blue-500 rounded-lg text-sm font-medium hover:bg-blue-600 transition-colors cursor-pointer"
          >
            Go to Orders
          </button>
        </div>
      </div>
    );
  }

  return <>{children}</>;
}


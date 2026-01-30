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

export default function AuthGuard({ children }: { children: React.ReactNode }) {
  const router = useRouter();
  const [isLoading, setIsLoading] = useState(true);
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [currentUser, setCurrentUser] = useState<User | null>(null);

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

  return <>{children}</>;
}


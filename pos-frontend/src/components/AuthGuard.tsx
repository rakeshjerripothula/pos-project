"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import {
  getCredentials,
  clearCredentials,
  clearAllAuth,
  User,
} from "@/lib/auth";

// Events
const USER_UPDATE_EVENT = "pos-user-update";
const AUTH_FAILURE_EVENT = "pos-auth-failure";

// Roles
export type UserRole = "OPERATOR" | "SUPERVISOR";

interface AuthGuardProps {
  children: React.ReactNode;
  requiredRole?: UserRole;
}

// Storage key
const USER_INFO_KEY = "pos_user_info";

/* =========================
   Storage Helpers
========================= */

function getUserInfo(): User | null {
  if (typeof window === "undefined") return null;

  try {
    const raw = sessionStorage.getItem(USER_INFO_KEY);
    return raw ? (JSON.parse(raw) as User) : null;
  } catch {
    return null;
  }
}

function saveUserInfo(user: User): void {
  if (typeof window !== "undefined") {
    sessionStorage.setItem(USER_INFO_KEY, JSON.stringify(user));
  }
}

function clearUserInfo(): void {
  if (typeof window !== "undefined") {
    sessionStorage.removeItem(USER_INFO_KEY);
  }
}

/* =========================
   Role Helpers
========================= */

export function isOperator(): boolean {
  return getUserInfo()?.role === "OPERATOR";
}

export function isSupervisor(): boolean {
  return getUserInfo()?.role === "SUPERVISOR";
}

function isAccessDenied(user: User, requiredRole?: UserRole): boolean {
  return (
    requiredRole === "SUPERVISOR" &&
    user.role === "OPERATOR"
  );
}

/* =========================
   AuthGuard Component
========================= */

export default function AuthGuard({
  children,
  requiredRole,
}: AuthGuardProps) {
  const router = useRouter();

  const [isLoading, setIsLoading] = useState(true);
  const [currentUser, setCurrentUser] = useState<User | null>(null);
  const [accessDenied, setAccessDenied] = useState(false);

  function notifyUserUpdate(user: User | null) {
    if (typeof window !== "undefined") {
      (window as any).__CURRENT_USER__ = user;
      window.dispatchEvent(new Event(USER_UPDATE_EVENT));
    }
  }

  /* =========================
     Initial Auth Check
  ========================= */

  useEffect(() => {
    async function checkAuth() {
      const credentials = getCredentials();

      if (!credentials) {
        clearAllAuth();
        notifyUserUpdate(null);
        router.replace("/login");
        return;
      }

      const cachedUser = getUserInfo();

      // Use cached user immediately
      if (cachedUser) {
        setCurrentUser(cachedUser);

        if (isAccessDenied(cachedUser, requiredRole)) {
          setAccessDenied(true);
          setIsLoading(false);
          notifyUserUpdate(cachedUser);
          return;
        }

        setIsLoading(false);
        notifyUserUpdate(cachedUser);

        // Background verification
        verifyCredentials(credentials);
        return;
      }

      // Fetch /users/me if no cache
      try {
        const authHeader = `Basic ${btoa(
          `${credentials.email}:${credentials.password}`
        )}`;

        const res = await fetch("http://localhost:8080/users/me", {
          method: "GET",
          headers: {
            Authorization: authHeader,
          },
        });

        if (res.status === 401) {
          window.dispatchEvent(new Event(AUTH_FAILURE_EVENT));
          return;
        }

        if (!res.ok) {
          setIsLoading(false);
          return;
        }

        const user: User = await res.json();
        saveUserInfo(user);
        setCurrentUser(user);
        notifyUserUpdate(user);

        if (isAccessDenied(user, requiredRole)) {
          setAccessDenied(true);
        }

        setIsLoading(false);
      } catch (err) {
        console.error("Auth check failed:", err);
        setIsLoading(false);
      }
    }

    checkAuth();
  }, [router, requiredRole]);

  /* =========================
     Background Verification
  ========================= */

  async function verifyCredentials(credentials: {
    email: string;
    password: string;
  }) {
    try {
      const authHeader = `Basic ${btoa(
        `${credentials.email}:${credentials.password}`
      )}`;

      const res = await fetch("http://localhost:8080/users/me", {
        method: "GET",
        headers: {
          Authorization: authHeader,
        },
      });

      if (res.status === 401) {
        window.dispatchEvent(new Event(AUTH_FAILURE_EVENT));
        return;
      }

      if (res.ok) {
        const user: User = await res.json();
        saveUserInfo(user);
        setCurrentUser(user);
        notifyUserUpdate(user);
      }
    } catch (err) {
      console.warn("Background auth verification failed:", err);
    }
  }

  /* =========================
     Global Auth Failure
  ========================= */

  useEffect(() => {
    const handleAuthFailure = () => {
      clearAllAuth();
      notifyUserUpdate(null);
      router.replace("/login");
    };

    window.addEventListener(AUTH_FAILURE_EVENT, handleAuthFailure);
    return () => {
      window.removeEventListener(AUTH_FAILURE_EVENT, handleAuthFailure);
    };
  }, [router]);

  /* =========================
     Render States
  ========================= */

  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-screen bg-slate-50">
        <div className="text-center">
          <div className="w-10 h-10 mx-auto mb-4 border-3 border-slate-200 rounded-full border-t-blue-500 animate-spin" />
          <p className="text-sm text-slate-500">
            Verifying authentication...
          </p>
        </div>
      </div>
    );
  }

  if (!currentUser) {
    return null;
  }

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
            You do not have permission to access this page.
          </p>
          <button
            onClick={() => router.replace("/orders")}
            className="px-6 py-2.5 text-white bg-blue-500 rounded-lg text-sm font-medium hover:bg-blue-600 transition-colors"
          >
            Go to Orders
          </button>
        </div>
      </div>
    );
  }

  return <>{children}</>;
}

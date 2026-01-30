"use client";

// AuthUser interface for stored auth metadata
export interface AuthUser {
  userId: number;
  email: string;
  role: "OPERATOR" | "SUPERVISOR";
  lastCheckedTime: number;
}

// User interface for backward compatibility
export interface User {
  id: number;
  email: string;
  role: "OPERATOR" | "SUPERVISOR";
}

// Storage key
const AUTH_KEY = "pos_auth";

// Global flag to prevent multiple auth checks (persists across component mounts)
let authCheckInProgress = false;
let authCheckComplete = false;

/**
 * Check if an auth check is already in progress
 */
export function isAuthCheckInProgress(): boolean {
  return authCheckInProgress;
}

/**
 * Mark auth check as started
 */
export function setAuthCheckStarted(): void {
  authCheckInProgress = true;
}

/**
 * Mark auth check as complete
 */
export function setAuthCheckComplete(): void {
  authCheckInProgress = false;
  authCheckComplete = true;
}

/**
 * Reset auth check state (for logout)
 */
export function resetAuthCheckState(): void {
  authCheckInProgress = false;
  authCheckComplete = false;
}

/**
 * Check if auth check was already completed (for page re-visits)
 */
export function wasAuthCheckCompleted(): boolean {
  return authCheckComplete;
}

/**
 * Save auth with metadata (called after successful signup/login)
 */
export function saveAuth(user: { id: number; email: string; role: "OPERATOR" | "SUPERVISOR" }): void {
  if (typeof window !== "undefined") {
    const authData: AuthUser = {
      userId: user.id,
      email: user.email,
      role: user.role,
      lastCheckedTime: Date.now(),
    };
    localStorage.setItem(AUTH_KEY, JSON.stringify(authData));
  }
}

/**
 * Get stored auth metadata
 */
export function getAuth(): AuthUser | null {
  if (typeof window === "undefined") {
    return null;
  }

  const raw = localStorage.getItem(AUTH_KEY);
  if (!raw) {
    return null;
  }

  try {
    return JSON.parse(raw) as AuthUser;
  } catch {
    return null;
  }
}

/**
 * Check if auth is fresh (within 5-minute window)
 */
export function isAuthFresh(): boolean {
  const auth = getAuth();
  if (!auth) return false;
  return Date.now() - auth.lastCheckedTime < 5 * 60 * 1000;
}

/**
 * Clear all auth data (on logout or auth failure)
 */
export function clearAuth(): void {
  if (typeof window !== "undefined") {
    localStorage.removeItem(AUTH_KEY);
  }
}

/**
 * Update lastCheckedTime to keep auth fresh (called after /auth/check)
 */
export function updateAuthTimestamp(): void {
  const auth = getAuth();
  if (auth) {
    saveAuth({ id: auth.userId, email: auth.email, role: auth.role });
  }
}

// ===== Backward Compatibility Functions =====

/**
 * Get user from auth storage (for backward compatibility)
 */
export function getUser(): User | null {
  const auth = getAuth();
  if (!auth) return null;
  return {
    id: auth.userId,
    email: auth.email,
    role: auth.role,
  };
}

/**
 * Clear all auth data (on logout)
 */
export function clearAllAuth(): void {
  clearAuth();
}

/**
 * Save user (wrapper around saveAuth for backward compatibility)
 */
export function saveUser(user: User): void {
  saveAuth({ id: user.id, email: user.email, role: user.role });
}


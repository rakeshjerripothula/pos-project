"use client";

// User interface
export interface User {
  id: number;
  email: string;
  role: "OPERATOR" | "SUPERVISOR";
}

// Storage key for credentials
const CREDENTIALS_KEY = "pos_credentials";

// Global flag to prevent multiple auth checks (persists across component mounts)
let authCheckInProgress = false;
let authCheckComplete = false;

/**
 * Generate Basic Auth header value from email and password
 */
export function generateBasicAuthHeader(email: string, password: string): string {
  const credentials = btoa(`${email}:${password}`);
  return `Basic ${credentials}`;
}

/**
 * Get stored credentials from sessionStorage
 */
export function getCredentials(): { email: string; password: string } | null {
  if (typeof window === "undefined" || !window.sessionStorage) {
    return null;
  }

  try {
    const raw = sessionStorage.getItem(CREDENTIALS_KEY);
    if (!raw) return null;
    return JSON.parse(raw) as { email: string; password: string };
  } catch {
    return null;
  }
}

/**
 * Store credentials in sessionStorage
 */
export function saveCredentials(email: string, password: string): void {
  if (typeof window !== "undefined" && window.sessionStorage) {
    sessionStorage.setItem(CREDENTIALS_KEY, JSON.stringify({ email, password }));
  }
}

/**
 * Clear stored credentials (on logout or auth failure)
 */
export function clearCredentials(): void {
  if (typeof window !== "undefined" && window.sessionStorage) {
    sessionStorage.removeItem(CREDENTIALS_KEY);
  }
}

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
 * Check if user is logged in (has valid credentials)
 */
export function isLoggedIn(): boolean {
  return getCredentials() !== null;
}

/**
 * Clear all auth data (on logout)
 */
export function clearAllAuth(): void {
  clearCredentials();
}

// ===== Backward Compatibility Functions =====

/**
 * Get user from credentials (needs to call /users/me to get user info)
 * Returns null if no credentials - caller should handle by calling /users/me
 */
export function getUser(): null {
  // We no longer store user in localStorage - credentials are enough
  // Return null and let the caller fetch user info from /users/me if needed
  return null;
}

/**
 * Save user (no-op now - we don't store user in localStorage)
 */
export function saveUser(_user: User): void {
  // No longer needed - credentials are stored in sessionStorage
}

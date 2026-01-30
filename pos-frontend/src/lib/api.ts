
import { getAuth } from "./auth";

const BASE_URL = "http://localhost:8080";

function getHeaders(): HeadersInit {
  const headers: HeadersInit = {
    "Content-Type": "application/json",
  };

  // Add userId header if user is logged in (only on client side)
  if (typeof window !== "undefined" && window.sessionStorage) {
    try {
      const auth = getAuth();
      if (auth) {
        headers["X-User-Id"] = auth.userId.toString();
      }
    } catch (e) {
      // sessionStorage not available
    }
  }

  return headers;
}

// Helper to check if we're in a browser environment
function isBrowser(): boolean {
  return typeof window !== "undefined" && typeof fetch !== "undefined";
}

export async function apiGet<T>(path: string): Promise<T> {
  // Don't make API calls during SSR or if fetch is not available
  if (!isBrowser()) {
    throw new Error("API calls require browser environment");
  }

  try {
    const res = await fetch(`${BASE_URL}${path}`, {
      cache: "no-store",
      headers: getHeaders(),
    });

    if (!res.ok) {
      const text = await res.text();
      throw new Error(text || `HTTP error! status: ${res.status}`);
    }

    return res.json();
  } catch (error) {
    // Re-throw with more context
    if (error instanceof TypeError && error.message === "Failed to fetch") {
      throw new Error("Failed to connect to backend. Please ensure the server is running.");
    }
    throw error;
  }
}

export async function apiPost<T>(path: string, body: any): Promise<T> {
  // Don't make API calls during SSR or if fetch is not available
  if (!isBrowser()) {
    throw new Error("API calls require browser environment");
  }

  try {
    const res = await fetch(`${BASE_URL}${path}`, {
      method: "POST",
      headers: getHeaders(),
      body: JSON.stringify(body),
    });

    if (!res.ok) {
      const text = await res.text();
      throw new Error(text || `HTTP error! status: ${res.status}`);
    }

    return res.json();
  } catch (error) {
    // Re-throw with more context
    if (error instanceof TypeError && error.message === "Failed to fetch") {
      throw new Error("Failed to connect to backend. Please ensure the server is running.");
    }
    throw error;
  }
}

export async function apiPut<T>(path: string, body?: any): Promise<T> {
  // Don't make API calls during SSR or if fetch is not available
  if (!isBrowser()) {
    throw new Error("API calls require browser environment");
  }

  try {
    const headers = getHeaders();
    const init: RequestInit = {
      method: "PUT",
      headers,
    };

    if (body) {
      init.body = JSON.stringify(body);
    }

    const res = await fetch(`${BASE_URL}${path}`, init);

    if (!res.ok) {
      const text = await res.text();
      throw new Error(text || `HTTP error! status: ${res.status}`);
    }

    return res.json();
  } catch (error) {
    // Re-throw with more context
    if (error instanceof TypeError && error.message === "Failed to fetch") {
      throw new Error("Failed to connect to backend. Please ensure the server is running.");
    }
    throw error;
  }
}


import { getCredentials, generateBasicAuthHeader, clearCredentials, clearAllAuth } from "./auth";
import { ApiError, FieldError } from "./types";

const BASE_URL = "http://localhost:8080";

// Event name for auth failures - to notify components to redirect to login
const AUTH_FAILURE_EVENT = "pos-auth-failure";

export class ApiValidationError extends Error {
  fieldErrors: FieldError[];

  constructor(message: string, fieldErrors: FieldError[] = []) {
    super(message);
    this.name = "ApiValidationError";
    this.fieldErrors = fieldErrors;
  }
}

/**
 * Dispatch auth failure event and clear credentials
 */
function handleAuthFailure(): void {
  clearCredentials();
  clearAllAuth();
  if (typeof window !== "undefined") {
    (window as any).__CURRENT_USER__ = null;
    window.dispatchEvent(new Event(AUTH_FAILURE_EVENT));
  }
}

/**
 * Check if response is 401 (Unauthorized - no/invalid credentials)
 */
function isUnauthorized(status: number): boolean {
  return status === 401;
}

/**
 * Check if response is 403 (Forbidden - authenticated but not authorized)
 */
function isForbidden(status: number): boolean {
  return status === 403;
}

function getHeaders(includeAuth: boolean = true): HeadersInit {
  const headers: HeadersInit = {
    "Content-Type": "application/json",
  };

  // Add Basic Auth header if user is logged in (only on client side)
  if (includeAuth && typeof window !== "undefined" && window.sessionStorage) {
    try {
      const credentials = getCredentials();
      if (credentials) {
        headers["Authorization"] = generateBasicAuthHeader(credentials.email, credentials.password);
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

// Parse error response from backend
async function parseErrorResponse(res: Response): Promise<Error> {
  const contentType = res.headers.get("content-type");
  
  // Try to parse as JSON first (our structured error format)
  if (contentType?.includes("application/json")) {
    try {
      const error: ApiError = await res.json();
      // Check if there are field-level errors
      if (error.fieldErrors && error.fieldErrors.length > 0) {
        return new ApiValidationError(
          error.message || "Validation failed",
          error.fieldErrors
        );
      }
      // Use the message from backend error response
      return new Error(error.message || `HTTP error! status: ${res.status}`);
    } catch {
      // Fallback to text if JSON parsing fails
      const text = await res.text();
      return new Error(text || `HTTP error! status: ${res.status}`);
    }
  }
  
  // Fallback to raw text for HTML responses
  const text = await res.text();
  return new Error(text || `HTTP error! status: ${res.status}`);
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
      // Handle 401 Unauthorized - redirect to login
      if (isUnauthorized(res.status)) {
        handleAuthFailure();
        // We'll let the component handle the redirect, but throw a specific error
        const error = await parseErrorResponse(res);
        throw new Error("Unauthorized: " + error.message);
      }
      // Handle 403 Forbidden - show permission error (don't clear credentials)
      if (isForbidden(res.status)) {
        const error = await parseErrorResponse(res);
        throw new Error("Forbidden: " + error.message);
      }
      throw await parseErrorResponse(res);
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
      // Handle 401 Unauthorized - redirect to login
      if (isUnauthorized(res.status)) {
        handleAuthFailure();
        const error = await parseErrorResponse(res);
        throw new Error("Unauthorized: " + error.message);
      }
      // Handle 403 Forbidden - show permission error (don't clear credentials)
      if (isForbidden(res.status)) {
        const error = await parseErrorResponse(res);
        throw new Error("Forbidden: " + error.message);
      }
      throw await parseErrorResponse(res);
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

export async function apiPatch<T>(path: string, body: any): Promise<T> {
  // Don't make API calls during SSR or if fetch is not available
  if (!isBrowser()) {
    throw new Error("API calls require browser environment");
  }

  try {
    const res = await fetch(`${BASE_URL}${path}`, {
      method: "PATCH",
      headers: getHeaders(),
      body: JSON.stringify(body),
    });

    if (!res.ok) {
      // Handle 401 Unauthorized - redirect to login
      if (isUnauthorized(res.status)) {
        handleAuthFailure();
        const error = await parseErrorResponse(res);
        throw new Error("Unauthorized: " + error.message);
      }
      // Handle 403 Forbidden - show permission error (don't clear credentials)
      if (isForbidden(res.status)) {
        const error = await parseErrorResponse(res);
        throw new Error("Forbidden: " + error.message);
      }
      throw await parseErrorResponse(res);
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
      // Handle 401 Unauthorized - redirect to login
      if (isUnauthorized(res.status)) {
        handleAuthFailure();
        const error = await parseErrorResponse(res);
        throw new Error("Unauthorized: " + error.message);
      }
      // Handle 403 Forbidden - show permission error (don't clear credentials)
      if (isForbidden(res.status)) {
        const error = await parseErrorResponse(res);
        throw new Error("Forbidden: " + error.message);
      }
      throw await parseErrorResponse(res);
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

// Export function for downloading binary files (CSV, etc.)
export async function apiExport(path: string, body: any): Promise<void> {
  // Don't make API calls during SSR or if fetch is not available
  if (!isBrowser()) {
    throw new Error("Export requires browser environment");
  }

  try {
    const headers = getHeaders();
    const res = await fetch(`${BASE_URL}${path}`, {
      method: "POST",
      headers,
      body: JSON.stringify(body),
    });

    if (!res.ok) {
      // Handle 401 Unauthorized - redirect to login
      if (isUnauthorized(res.status)) {
        handleAuthFailure();
        const error = await parseErrorResponse(res);
        throw new Error("Unauthorized: " + error.message);
      }
      // Handle 403 Forbidden - show permission error (don't clear credentials)
      if (isForbidden(res.status)) {
        const error = await parseErrorResponse(res);
        throw new Error("Forbidden: " + error.message);
      }
      throw await parseErrorResponse(res);
    }

    // Get the blob from response
    const blob = await res.blob();

    // Create download link
    const contentDisposition = res.headers.get("Content-Disposition");
    let filename = "download.csv";
    if (contentDisposition) {
      const filenameMatch = contentDisposition.match(/filename="(.+)"/);
      if (filenameMatch) {
        filename = filenameMatch[1];
      }
    }

    // Trigger download
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = filename;
    document.body.appendChild(a);
    a.click();
    window.URL.revokeObjectURL(url);
    document.body.removeChild(a);
  } catch (error) {
    if (error instanceof TypeError && error.message === "Failed to fetch") {
      throw new Error("Failed to connect to backend. Please ensure the server is running.");
    }
    throw error;
  }
}

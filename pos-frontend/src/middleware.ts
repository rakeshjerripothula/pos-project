import { NextResponse } from "next/server";
import type { NextRequest } from "next/server";

export function middleware(request: NextRequest) {
  // Public routes that don't require authentication
  const publicRoutes = ["/signup", "/"];
  const isPublicRoute = publicRoutes.some((route) =>
    request.nextUrl.pathname === route || request.nextUrl.pathname.startsWith(route + "/")
  );

  // Check if user is authenticated (check sessionStorage via cookie or header)
  // Since middleware runs on server, we'll check on client side in layout
  // This middleware just ensures signup page is accessible
  if (request.nextUrl.pathname === "/" && !isPublicRoute) {
    // Allow home page to show navigation
    return NextResponse.next();
  }

  return NextResponse.next();
}

export const config = {
  matcher: [
    /*
     * Match all request paths except for the ones starting with:
     * - api (API routes)
     * - _next/static (static files)
     * - _next/image (image optimization files)
     * - favicon.ico (favicon file)
     */
    "/((?!api|_next/static|_next/image|favicon.ico).*)",
  ],
};

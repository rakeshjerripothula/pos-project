# Pagination Fix Plan

## Problem Summary
Pagination is not working properly because the code maintains a `page` state but never actually uses it to slice the displayed data. The pagination UI is misleading as clicking Next/Previous doesn't change what's displayed.

## Files to Fix
1. `pos-frontend/src/app/clients/page.tsx`
2. `pos-frontend/src/app/products/page.tsx`
3. `pos-frontend/src/app/inventory/page.tsx`

## Solution: Option A - Client-side pagination with proper slicing

### Clients Page Fix
1. Remove the server-side pagination loop (load all clients at once or with reasonable limit)
2. Apply client-side filters to get `filteredClients`
3. Slice `filteredClients` based on current page: `const paginatedClients = filteredClients.slice(page * pageSize, (page + 1) * pageSize)`
4. Pass `paginatedClients` to ClientTable instead of all filtered clients
5. Update totalElements to reflect filtered count, not total loaded

### Products Page Fix
1. Load all products (server-side pagination loop to get complete dataset)
2. Apply client-side filters to get `filteredProducts`
3. Slice filtered products for display based on current page
4. Update pagination UI to reflect filtered count

### Inventory Page Fix
1. Load all inventory (server-side pagination loop to get complete dataset)
2. Apply client-side filters to get `filteredInventory`
3. Slice filtered inventory for display based on current page
4. Update pagination UI to reflect filtered count

## Implementation Steps
1. Read each file again to understand exact structure
2. Implement fixes for all three pages
3. Test the changes

## Key Changes
- `displayedData` = `filteredData.slice(page * pageSize, (page + 1) * pageSize)`
- Pass `displayedData` to table components
- `totalElements` = `filteredData.length` (not total from API)
- Reset `page` to 0 when filters change


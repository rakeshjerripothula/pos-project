export type OrderStatus = "CREATED" | "INVOICED" | "CANCELLED";

export interface FieldError {
  field: string;
  message: string;
}

export interface ApiError {
  code: string;
  message: string;
  fieldErrors?: FieldError[];
}

export interface OrderData {
  id?: number;
  orderId?: number;
  clientId: number;
  status: OrderStatus;
  createdAt: string;
  items?: OrderItemData[];
}

export interface OrderPageData {
  content: OrderData[];
  page: number;
  pageSize: number;
  totalElements: number;
}

export interface ClientData {
  id: number;
  clientName: string;
  enabled: boolean;
}

export interface PagedResponse<T> {
  data: T[];
  total: number;
}

export interface ClientSearchForm {
  page: number;
  pageSize: number;
  clientName?: string;
  enabled?: boolean;
}

export interface ProductSearchForm {
  page: number;
  pageSize: number;
  clientId?: number;
  barcode?: string;
  productName?: string;
}

export interface InventorySearchForm {
  page: number;
  pageSize: number;
}

export interface ProductData {
  id: number;
  productName: string;
  mrp: number;
  clientId: number;
  barcode: string;
  imageUrl?: string;
}

export interface InventoryData {
  productId: number;
  productName: string;
  quantity: number;
}

export interface UserData {
  id: number;
  email: string;
  role: "OPERATOR" | "SUPERVISOR";
}

export interface OrderItemData {
  productId: number;
  productName: string;
  quantity: number;
  sellingPrice: number;
}

export interface DaySalesData {
  date: string;
  invoicedOrdersCount: number;
  invoicedItemsCount: number;
  totalRevenue: number;
}

export interface DaySalesPageData {
  content: DaySalesData[];
  page: number;
  pageSize: number;
  totalElements: number;
}

export interface SalesReportRowData {
  productName: string;
  quantitySold: number;
  revenue: number;
}

export interface SalesReportPageData {
  rows: SalesReportRowData[];
  page: number;
  pageSize: number;
  totalElements: number;
}

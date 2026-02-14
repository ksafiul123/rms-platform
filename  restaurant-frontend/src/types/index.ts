// Export all types
export * from "./user";
export * from "./auth";
export * from "./api";
export * from "./permission";
export * from "./menu";
export * from "./order";

// Common UI Types
export interface SelectOption {
  label: string;
  value: string | number;
}

export interface TableColumn<T = any> {
  key: string;
  label: string;
  sortable?: boolean;
  render?: (value: any, row: T) => React.ReactNode;
}

export interface FilterOption {
  key: string;
  label: string;
  type: "text" | "select" | "date" | "daterange";
  options?: SelectOption[];
}

export interface PaginationState {
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface SortState {
  field: string;
  direction: "asc" | "desc";
}

export interface FilterState {
  [key: string]: any;
}
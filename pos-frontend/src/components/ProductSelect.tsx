"use client";

import { useMemo } from "react";
import Select, { SingleValue } from "react-select";
import { ProductData } from "@/lib/types";

export interface ProductOption {
  value: number;
  label: string;
}

interface ProductSelectProps {
  products: ProductData[];
  value: number | null;
  onChange: (value: number | null) => void;
  placeholder?: string;
  isClearable?: boolean;
  isSearchable?: boolean;
  className?: string;
}

export default function ProductSelect({
  products,
  value,
  onChange,
  placeholder = "Search product...",
  isClearable = true,
  isSearchable = true,
  className,
}: ProductSelectProps) {
  const options: ProductOption[] = useMemo(
    () =>
      products
        .sort((a, b) => a.productName.localeCompare(b.productName))
        .map((p) => ({
          value: p.id,
          label: `${p.productName} (${p.barcode})`,
        })),
    [products]
  );

  const selectedOption = useMemo(
    () => options.find((o) => o.value === value) || null,
    [options, value]
  );

  function handleChange(option: SingleValue<ProductOption>) {
    onChange(option ? option.value : null);
  }

  return (
    <div className={className}>
      <Select
        options={options}
        value={selectedOption}
        onChange={handleChange}
        placeholder={placeholder}
        isClearable={isClearable}
        isSearchable={isSearchable}
        className="w-full"
        classNamePrefix="react-select"
        menuPortalTarget={document.body}
        styles={{
          control: (base) => ({
            ...base,
            borderRadius: "0.5rem",
            border: "1px solid #d1d5db",
            fontSize: "14px",
            minHeight: "42px",
            backgroundColor: "white",
          }),
          placeholder: (base) => ({
            ...base,
            fontSize: "14px",
          }),
          menuPortal: (base) => ({
            ...base,
            zIndex: 9999,
          }),
          menu: (base) => ({
            ...base,
            borderRadius: "0.5rem",
            boxShadow: "0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06)",
          }),
          option: (base, state) => ({
            ...base,
            fontSize: "14px",
            backgroundColor: state.isSelected 
              ? "#667eea" 
              : state.isFocused 
                ? "#f3f4f6" 
                : "white",
            color: state.isSelected ? "white" : "#374151",
            padding: "10px 14px",
          }),
        }}
      />
    </div>
  );
}


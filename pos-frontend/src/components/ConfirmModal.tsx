"use client";

import { useEffect, useRef } from "react";

interface ConfirmModalProps {
  isOpen: boolean;
  onClose: () => void;
  onConfirm: () => void;
  title: string;
  message: string;
  confirmText?: string;
  cancelText?: string;
  confirmStyle?: "danger" | "primary" | "warning";
  isLoading?: boolean;
}

export default function ConfirmModal({
  isOpen,
  onClose,
  onConfirm,
  title,
  message,
  confirmText = "Confirm",
  cancelText = "Cancel",
  confirmStyle = "primary",
  isLoading = false,
}: ConfirmModalProps) {
  const modalRef = useRef<HTMLDivElement>(null);

  // Close on escape key
  useEffect(() => {
    const handleEscape = (e: KeyboardEvent) => {
      if (e.key === "Escape" && isOpen && !isLoading) {
        onClose();
      }
    };

    document.addEventListener("keydown", handleEscape);
    return () => document.removeEventListener("keydown", handleEscape);
  }, [isOpen, onClose, isLoading]);

  // Focus trap for accessibility
  useEffect(() => {
    if (isOpen && modalRef.current) {
      modalRef.current.focus();
    }
  }, [isOpen]);

  // Prevent body scroll when modal is open
  useEffect(() => {
    if (isOpen) {
      document.body.style.overflow = "hidden";
    } else {
      document.body.style.overflow = "unset";
    }
    return () => {
      document.body.style.overflow = "unset";
    };
  }, [isOpen]);

  if (!isOpen) return null;

  const getConfirmButtonClass = () => {
    switch (confirmStyle) {
      case "danger":
        return "bg-red-500 hover:bg-red-600 focus:ring-red-500";
      case "warning":
        return "bg-amber-500 hover:bg-amber-600 focus:ring-amber-500";
      case "primary":
      default:
        return "bg-blue-500 hover:bg-blue-600 focus:ring-blue-500";
    }
  };

  return (
    <div
      className="fixed inset-0 bg-black/50 flex items-start justify-center z-[1000] pt-20"
      onClick={(e) => {
        if (e.target === e.currentTarget && !isLoading) {
          onClose();
        }
      }}
    >
      <div
        ref={modalRef}
        className="bg-white rounded-lg max-w-md w-[90%] max-h-[90vh] overflow-auto shadow-xl"
        tabIndex={-1}
        role="dialog"
        aria-modal="true"
        aria-labelledby="modal-title"
      >
        {/* Header */}
        <div className="flex items-center justify-between p-5 border-b border-gray-200">
          <h3
            id="modal-title"
            className="text-xl font-semibold text-slate-800"
          >
            {title}
          </h3>
          <button
            onClick={onClose}
            disabled={isLoading}
            className="text-gray-400 hover:text-gray-600 transition-colors bg-transparent border-none cursor-pointer text-2xl leading-none"
            aria-label="Close modal"
          >
            ×
          </button>
        </div>

        {/* Body */}
        <div className="p-5">
          <p className="text-base text-slate-600 leading-relaxed">
            {message}
          </p>
        </div>

        {/* Footer */}
        <div className="flex justify-end gap-3 p-5 border-t border-gray-200 bg-gray-50 rounded-b-lg">
          <button
            onClick={onClose}
            disabled={isLoading}
            className={`px-4 py-2 text-base font-medium rounded-md transition-colors ${
              isLoading
                ? "bg-gray-300 text-gray-500 cursor-not-allowed"
                : "bg-gray-200 text-gray-700 hover:bg-gray-300 cursor-pointer"
            }`}
          >
            {cancelText}
          </button>
          <button
            onClick={onConfirm}
            disabled={isLoading}
            className={`px-4 py-2.5 text-base font-medium text-white rounded-lg transition-colors focus:outline-none focus:ring-2 focus:ring-offset-2 ${
              isLoading
                ? "bg-gray-400 cursor-not-allowed"
                : `${getConfirmButtonClass()} cursor-pointer focus:ring-offset-2`
            }`}
          >
            {isLoading ? (
              <span className="flex items-center gap-2">
                <svg
                  className="animate-spin h-4 w-4 text-white"
                  xmlns="http://www.w3.org/2000/svg"
                  fill="none"
                  viewBox="0 0 24 24"
                >
                  <circle
                    className="opacity-25"
                    cx="12"
                    cy="12"
                    r="10"
                    stroke="currentColor"
                    strokeWidth="4"
                  ></circle>
                  <path
                    className="opacity-75"
                    fill="currentColor"
                    d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
                  ></path>
                </svg>
                Processing...
              </span>
            ) : (
              confirmText
            )}
          </button>
        </div>
      </div>
    </div>
  );
}


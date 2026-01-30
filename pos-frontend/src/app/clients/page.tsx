"use client";

import { useState, useEffect } from "react";
import { apiGet } from "@/lib/api";
import { ClientData } from "@/lib/types";
import ClientTable from "@/components/ClientTable";
import AddClient from "@/components/AddClient";
import AuthGuard from "@/components/AuthGuard";

export default function ClientsPage() {
  const [clients, setClients] = useState<ClientData[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadClients();
  }, []);

  async function loadClients() {
    try {
      const data = await apiGet<ClientData[]>("/clients");
      setClients(data);
    } catch (error: any) {
      alert("Failed to load clients: " + error.message);
    } finally {
      setLoading(false);
    }
  }

  async function toggleClient(id: number, enabled: boolean) {
    // Use the backend toggle endpoint
    try {
      await fetch(`http://localhost:8080/clients/client/${id}/toggle`, {
        method: "PATCH",
      });
      await loadClients();
    } catch (error: any) {
      alert("Failed to toggle client: " + error.message);
    }
  }

  async function addClient(clientName: string) {
    const res = await fetch("http://localhost:8080/clients", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({ clientName }),
    });

    if (!res.ok) {
      const message = await res.text();
      throw new Error(message);
    }

    await loadClients();
  }

  async function updateClient(id: number, clientName: string) {
    const res = await fetch(`http://localhost:8080/clients/${id}`, {
      method: "PUT",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({ clientName }),
    });

    if (!res.ok) {
      const message = await res.text();
      throw new Error(message);
    }

    await loadClients();
  }

  return (
    <AuthGuard>
      <div
        style={{
          minHeight: "calc(100vh - 64px)",
          backgroundColor: "#f8fafc",
          padding: 24,
        }}
      >
        <div style={{ maxWidth: 1400, margin: "0 auto" }}>
          <div
            style={{
              display: "flex",
              justifyContent: "space-between",
              alignItems: "center",
              marginBottom: 24,
            }}
          >
            <h1
              style={{
                fontSize: 28,
                fontWeight: "bold",
                color: "#1e293b",
              }}
            >
              Clients
            </h1>
          </div>

          <div
            style={{
              backgroundColor: "white",
              borderRadius: 12,
              padding: 24,
              boxShadow: "0 1px 3px rgba(0,0,0,0.1)",
              marginBottom: 24,
            }}
          >
            <AddClient onAdd={addClient} />
          </div>

          {loading ? (
            <div style={{ textAlign: "center", padding: 48 }}>Loading...</div>
          ) : (
            <div
              style={{
                backgroundColor: "white",
                borderRadius: 12,
                padding: 24,
                boxShadow: "0 1px 3px rgba(0,0,0,0.1)",
              }}
            >
              <ClientTable
                clients={clients}
                onToggle={toggleClient}
                onUpdate={updateClient}
              />
            </div>
          )}
        </div>
      </div>
    </AuthGuard>
  );
}

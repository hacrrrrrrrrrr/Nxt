"use client";

import { useEffect, useState } from "react";
import { createClientComponentClient } from "@supabase/auth-helpers-nextjs";

type Transaction = {
  id: string;
  user_id: string;
  amount: number;
  type: string;
  utr_id: string;
  screenshot_url: string;
  status: string;
  created_at: string;
};

export default function PendingDeposits() {
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [loading, setLoading] = useState(true);
  const [processingId, setProcessingId] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  const supabase = createClientComponentClient();

  useEffect(() => {
    fetchPendingDeposits();
  }, []);

  const fetchPendingDeposits = async () => {
    setLoading(true);
    setError(null);
    try {
      const { data, error } = await supabase
        .from("transactions")
        .select("*")
        .eq("status", "pending")
        .eq("type", "deposit")
        .order("created_at", { ascending: true });

      if (error) throw error;
      setTransactions(data || []);
    } catch (err: any) {
      setError(err.message || "Failed to fetch pending deposits.");
    } finally {
      setLoading(false);
    }
  };

  const handleApprove = async (transactionId: string) => {
    setProcessingId(transactionId);
    setError(null);
    try {
      const { error } = await supabase.rpc("approve_deposit", {
        transaction_id: transactionId,
      });

      if (error) throw error;

      // Successfully approved, remove it from the list
      setTransactions((prev) => prev.filter((t) => t.id !== transactionId));
    } catch (err: any) {
      setError(err.message || "Failed to approve deposit.");
    } finally {
      setProcessingId(null);
    }
  };

  return (
    <div className="p-6 max-w-6xl mx-auto">
      <h1 className="text-2xl font-bold mb-6 text-gray-900">Pending Deposits</h1>

      {error && (
        <div className="bg-red-50 text-red-600 p-4 rounded-lg mb-6 border border-red-200">
          {error}
        </div>
      )}

      {loading ? (
        <div className="text-gray-500 animate-pulse">Loading pending deposits...</div>
      ) : transactions.length === 0 ? (
        <div className="text-gray-500">No pending deposits require approval.</div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {transactions.map((tx) => (
            <div
              key={tx.id}
              className="bg-white p-5 rounded-2xl shadow-sm border border-gray-200 flex flex-col gap-4"
            >
              <div className="flex justify-between items-start">
                <div>
                  <p className="text-sm text-gray-500 font-medium">Amount</p>
                  <p className="text-2xl font-black text-gray-900">₹{tx.amount}</p>
                </div>
                <span className="bg-orange-100 text-orange-600 text-xs px-2 py-1 rounded-full font-bold uppercase tracking-wider">
                  Pending
                </span>
              </div>

              <div>
                <p className="text-xs text-gray-500 font-medium mb-1">UTR ID</p>
                <p className="font-mono text-sm bg-gray-50 p-2 rounded-lg border border-gray-100">
                  {tx.utr_id || "N/A"}
                </p>
              </div>

              <div>
                <p className="text-xs text-gray-500 font-medium mb-1">Payment Proof</p>
                {tx.screenshot_url ? (
                  <a
                    href={tx.screenshot_url}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="block relative h-32 rounded-lg overflow-hidden bg-gray-100 border border-gray-200 hover:opacity-90 transition-opacity"
                  >
                    {/* eslint-disable-next-line @next/next/no-img-element */}
                    <img
                      src={tx.screenshot_url}
                      alt="Payment Screenshot"
                      className="object-cover w-full h-full"
                    />
                  </a>
                ) : (
                  <div className="h-32 rounded-lg bg-gray-50 border border-dashed border-gray-300 flex items-center justify-center text-gray-400 text-sm">
                    No screenshot provided
                  </div>
                )}
              </div>

              <div className="pt-2 mt-auto">
                <button
                  onClick={() => handleApprove(tx.id)}
                  disabled={processingId === tx.id}
                  className="w-full bg-green-600 hover:bg-green-700 disabled:bg-green-400 disabled:cursor-not-allowed text-white font-bold py-2.5 rounded-xl transition-colors shadow-sm"
                >
                  {processingId === tx.id ? "Approving..." : "Approve Deposit"}
                </button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

-- ==============================================================================
-- Supabase Database Schema: NXT E-SPORTS
-- ==============================================================================

-- 1. Profiles Table
CREATE TABLE IF NOT EXISTS public.profiles (
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    uid TEXT UNIQUE NOT NULL,
    in_game_name TEXT NOT NULL,
    wallet_balance NUMERIC DEFAULT 0,
    role TEXT DEFAULT 'user'
);

-- 2. Tournaments Table
CREATE TABLE IF NOT EXISTS public.tournaments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title TEXT NOT NULL,
    mode TEXT NOT NULL,
    entry_fee NUMERIC NOT NULL,
    prize_pool NUMERIC NOT NULL,
    max_players INT NOT NULL,
    current_players INT DEFAULT 0,
    start_time TIMESTAMPTZ NOT NULL,
    room_id TEXT,
    room_pass TEXT,
    status TEXT DEFAULT 'UPCOMING'
);

-- 3. Transactions Table
CREATE TABLE IF NOT EXISTS public.transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES public.profiles(id) NOT NULL,
    amount NUMERIC NOT NULL,
    type TEXT NOT NULL CHECK (type IN ('deposit', 'withdrawal')),
    utr_id TEXT UNIQUE,
    screenshot_url TEXT,
    status TEXT DEFAULT 'pending' CHECK (status IN ('pending', 'completed', 'rejected')),
    created_at TIMESTAMPTZ DEFAULT now()
);

-- 4. Match Participants Table
CREATE TABLE IF NOT EXISTS public.match_participants (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    match_id UUID REFERENCES public.tournaments(id) ON DELETE CASCADE,
    user_id UUID REFERENCES public.profiles(id) ON DELETE CASCADE,
    UNIQUE(match_id, user_id)
);

-- ==============================================================================
-- Storage Setup
-- ==============================================================================
-- Create the payment_proofs bucket
INSERT INTO storage.buckets (id, name, public) 
VALUES ('payment_proofs', 'payment_proofs', false);

-- Enable RLS on storage bucket
CREATE POLICY "Authenticated users can upload payment proofs"
ON storage.objects FOR INSERT TO authenticated
WITH CHECK (
    bucket_id = 'payment_proofs' AND 
    (storage.foldername(name))[1] = auth.uid()::text
);

CREATE POLICY "Admins can view all payment proofs"
ON storage.objects FOR SELECT TO authenticated
USING (
    bucket_id = 'payment_proofs' AND 
    EXISTS (SELECT 1 FROM public.profiles WHERE id = auth.uid() AND role = 'admin')
);

-- ==============================================================================
-- RPC Function: approve_deposit
-- ==============================================================================
-- This safely completes a transaction and increments the user's wallet atomically
CREATE OR REPLACE FUNCTION public.approve_deposit(transaction_id UUID)
RETURNS void
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
DECLARE
    txn_amount NUMERIC;
    txn_user UUID;
    txn_status TEXT;
BEGIN
    -- 1. Fetch the transaction with a row-level lock to prevent race conditions
    SELECT amount, user_id, status 
    INTO txn_amount, txn_user, txn_status 
    FROM public.transactions 
    WHERE id = transaction_id 
    FOR UPDATE;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Transaction not found';
    END IF;

    IF txn_status != 'pending' THEN
        RAISE EXCEPTION 'Transaction is not in pending state';
    END IF;

    -- 2. Update transaction status
    UPDATE public.transactions 
    SET status = 'completed' 
    WHERE id = transaction_id;

    -- 3. Increment the user's wallet balance
    UPDATE public.profiles 
    SET wallet_balance = wallet_balance + txn_amount 
    WHERE id = txn_user;
END;
$$;

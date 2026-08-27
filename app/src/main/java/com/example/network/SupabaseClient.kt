package com.example.network

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.compose.auth.ComposeAuth
import io.github.jan.supabase.compose.auth.googleNativeLogin

object SupabaseClient {
    val client = createSupabaseClient(
        supabaseUrl = "https://itgvqejnuuuwlrwglbue.supabase.co",
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Iml0Z3ZxZWpudXV1d2xyd2dsYnVlIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODc3ODI1NzAsImV4cCI6MjEwMzM1ODU3MH0.Ffl01enZNTQc6YJE-bb01jxyCBS6QVoFsSQD-iIH1B8"
    ) {
        install(Postgrest)
        install(Storage)
        install(Auth) {
            scheme = "com.jod.esports"
            host = "login"
        }
        install(ComposeAuth) {
            googleNativeLogin(serverClientId = "476115501321-16q5a16a1nn9s0stgajcnquf9k54avm7.apps.googleusercontent.com")
        }
    }
}

package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.network.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

class WalletViewModel : ViewModel() {
    private val _requests = MutableStateFlow<List<WalletRequest>>(emptyList())
    val requests: StateFlow<List<WalletRequest>> = _requests.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun fetchRequests() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val session = SupabaseClient.client.auth.currentSessionOrNull()
                if (session != null) {
                    val result = SupabaseClient.client.postgrest["wallet_requests"]
                        .select { 
                            filter { eq("user_id", session.user!!.id) }
                            order("created_at", order = io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                        }.decodeList<WalletRequest>()
                    _requests.value = result
                }
            } catch (e: Exception) {
                _message.value = "Failed to load history"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun requestWithdraw(amount: Int, upiId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val session = SupabaseClient.client.auth.currentSessionOrNull()
                if (session != null) {
                    val userId = session.user!!.id
                    try {
                        val email = session.user!!.email ?: ""
                        val name = email.substringBefore("@").ifEmpty { "player_${userId.take(6)}" }
                        SupabaseClient.client.postgrest["profiles"].upsert(
                            mapOf("id" to userId, "uid" to userId, "in_game_name" to name)
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    val request = WalletRequestInsert(
                        user_id = userId,
                        type = "WITHDRAW",
                        amount = amount,
                        upi_id = upiId
                    )
                    SupabaseClient.client.postgrest["wallet_requests"].insert(request)
                    _message.value = "Withdrawal request submitted successfully!"
                    fetchRequests()
                }
            } catch (e: Exception) {
                _message.value = e.message ?: "Failed to submit request"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun clearMessage() {
        _message.value = null
    }
}

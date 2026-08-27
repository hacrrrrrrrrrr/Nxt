package com.example.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class TransactionInsert(
    val user_id: String,
    val amount: Double,
    val type: String,
    val utr_id: String,
    val screenshot_url: String,
    val status: String = "pending"
)

class AddMoneyViewModel(private val supabase: SupabaseClient) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _isSuccess = MutableStateFlow(false)
    val isSuccess: StateFlow<Boolean> = _isSuccess.asStateFlow()

    fun submitDeposit(
        userId: String,
        amount: String,
        utrId: String,
        imageBytes: ByteArray
    ) {
        if (amount.isEmpty() || utrId.length != 12 || imageBytes.isEmpty()) {
            _errorMessage.value = "Please fill all fields and upload a screenshot."
            return
        }

        val amountDouble = amount.toDoubleOrNull()
        if (amountDouble == null || amountDouble <= 0) {
            _errorMessage.value = "Please enter a valid amount."
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                // 1. Upload screenshot to Supabase Storage
                val bucket = supabase.storage["payment_proofs"]
                val fileName = "${userId}/${UUID.randomUUID()}.jpg"
                
                bucket.upload(fileName, imageBytes) {
                    upsert = false
                }
                
                // 2. Retrieve public URL
                val publicUrl = bucket.publicUrl(fileName)

                // 3. Insert transaction record into Supabase Database
                val transaction = TransactionInsert(
                    user_id = userId,
                    amount = amountDouble,
                    type = "deposit",
                    utr_id = utrId,
                    screenshot_url = publicUrl,
                    status = "pending"
                )

                supabase.postgrest["transactions"].insert(transaction)

                // 4. Update success state to trigger navigation
                _isSuccess.value = true
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "Failed to process deposit."
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun resetState() {
        _isSuccess.value = false
        _errorMessage.value = null
        _isLoading.value = false
    }
}

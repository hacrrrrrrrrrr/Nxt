package com.example.ui

import kotlinx.serialization.Serializable

@Serializable
data class WalletRequest(
    val id: String,
    val user_id: String,
    val type: String,
    val amount: Int,
    val upi_id: String? = null,
    val screenshot_url: String? = null,
    val status: String,
    val reason: String? = null,
    val created_at: String
)

@Serializable
data class WalletRequestInsert(
    val user_id: String,
    val type: String,
    val amount: Int,
    val upi_id: String? = null,
    val screenshot_url: String? = null,
    val status: String = "PENDING"
)

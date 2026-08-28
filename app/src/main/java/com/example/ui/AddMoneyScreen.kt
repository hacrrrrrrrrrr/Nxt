package com.example.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BorderGray
import com.example.ui.theme.PrimaryOrange
import com.example.ui.theme.SurfaceWhite
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextGray
import kotlinx.coroutines.launch
import com.example.network.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMoneyScreen(
    onNavigateBack: () -> Unit,
    onNavigateToConfirmation: (String) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var utrId by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val context = androidx.compose.ui.platform.LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Money", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceWhite)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // QR Code
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text("Scan to Pay", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDark)
                Spacer(modifier = Modifier.height(8.dp))
                coil.compose.AsyncImage(
                    model = SupabaseClient.client.storage["admin"].publicUrl("qr_code.png"),
                    contentDescription = "Admin QR Code",
                    modifier = Modifier.size(200.dp).clip(RoundedCornerShape(12.dp))
                )
            }
            // Amount Input
            Column {
                Text("Deposit Amount (₹)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextGray)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    placeholder = { Text("e.g. 500") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // UTR ID Input
            Column {
                Text("12-Digit UTR / Transaction ID", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextGray)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = utrId,
                    onValueChange = { utrId = it.take(12) },
                    placeholder = { Text("Enter 12 digit UTR") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Screenshot Picker
            Column {
                Text("Payment Screenshot", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextGray)
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceWhite)
                        .border(1.dp, BorderGray, RoundedCornerShape(12.dp))
                        .clickable { imagePickerLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedImageUri != null) {
                        Text("Screenshot Selected", color = PrimaryOrange, fontWeight = FontWeight.Bold)
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Image, contentDescription = null, tint = TextGray)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Tap to upload screenshot", fontSize = 12.sp, color = TextGray)
                        }
                    }
                }
            }

            if (errorMessage != null) {
                Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.weight(1f))

            // Submit Button
            Button(
                onClick = {
                    if (amount.isEmpty() || utrId.length != 12 || selectedImageUri == null) {
                        errorMessage = "Please fill all fields and upload a screenshot."
                        return@Button
                    }
                    errorMessage = null
                    isLoading = true

                    coroutineScope.launch {
                        try {
                            val session = SupabaseClient.client.auth.currentSessionOrNull()
                            if (session != null) {
                                val amountInt = amount.toIntOrNull() ?: throw Exception("Invalid amount")
                                
                                // Upload image
                                val bucket = SupabaseClient.client.storage["payment_receipts"]
                                val filename = "receipt_${System.currentTimeMillis()}.jpg"
                                val inputStream = context.contentResolver.openInputStream(selectedImageUri!!)
                                val bytes = inputStream?.readBytes() ?: throw Exception("Failed to read image")
                                bucket.upload(filename, bytes) {
                                    upsert = false
                                }
                                val screenshotUrl = bucket.publicUrl(filename)

                                val request = WalletRequestInsert(
                                    user_id = session.user!!.id,
                                    type = "ADD",
                                    amount = amountInt,
                                    upi_id = utrId,
                                    screenshot_url = screenshotUrl
                                )
                                SupabaseClient.client.postgrest["wallet_requests"].insert(request)
                            }

                            isLoading = false
                            onNavigateToConfirmation(amount)
                        } catch (e: Exception) {
                            isLoading = false
                            errorMessage = e.message ?: "Failed to submit request"
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange),
                shape = RoundedCornerShape(12.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = SurfaceWhite)
                } else {
                    Text("SUBMIT DEPOSIT", fontWeight = FontWeight.Black, fontSize = 14.sp, color = SurfaceWhite)
                }
            }
        }
    }
}

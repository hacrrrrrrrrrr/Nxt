package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PrimaryOrange
import com.example.ui.theme.SurfaceWhite
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextGray
import com.example.network.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.jsonPrimitive

@Composable
fun ProfileScreen(onLogout: () -> Unit = {}) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    var ffName by remember { mutableStateOf("") }
    var ffUid by remember { mutableStateOf("") }
    var googleName by remember { mutableStateOf("") }
    
    var isEditing by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val user = SupabaseClient.client.auth.currentSessionOrNull()?.user
        val metadata = user?.userMetadata
        if (metadata != null) {
            ffName = metadata["ff_name"]?.jsonPrimitive?.content ?: ""
            ffUid = metadata["ff_uid"]?.jsonPrimitive?.content ?: ""
            googleName = metadata["full_name"]?.jsonPrimitive?.content ?: user.email ?: ""
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // Avatar Placeholder
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(PrimaryOrange.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Avatar",
                tint = PrimaryOrange,
                modifier = Modifier.size(60.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = googleName,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
            color = TextGray
        )
        
        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Game Profile",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = TextDark
                    )
                    IconButton(onClick = { 
                        if (isEditing) {
                            // Save
                            coroutineScope.launch {
                                isSaving = true
                                try {
                                    val currentUser = SupabaseClient.client.auth.currentSessionOrNull()?.user
                                    if (currentUser != null) {
                                        // Update Metadata
                                        SupabaseClient.client.auth.updateUser {
                                            data = buildJsonObject {
                                                put("ff_name", ffName)
                                                put("ff_uid", ffUid)
                                            }
                                        }
                                        
                                        // Update profiles table
                                        SupabaseClient.client.postgrest["profiles"].update(
                                                mapOf(
                                                    "in_game_name" to ffName,
                                                    "in_game_uid" to ffUid
                                                )
                                            ) {
                                                filter { eq("id", currentUser.id) }
                                            }
                                    }
                                    isEditing = false
                                } catch (e: Exception) {
                                    android.widget.Toast.makeText(context, "Failed to save: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                                } finally {
                                    isSaving = false
                                }
                            }
                        } else {
                            isEditing = true
                        }
                    }) {
                        if (isSaving) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp, color = PrimaryOrange)
                        } else if (isEditing) {
                            Icon(Icons.Default.Check, contentDescription = "Save", tint = PrimaryOrange)
                        } else {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = TextGray)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = ffName,
                    onValueChange = { ffName = it },
                    label = { Text("Free Fire Name") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = isEditing,
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = ffUid,
                    onValueChange = { ffUid = it },
                    label = { Text("Free Fire UID") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = isEditing,
                    singleLine = true
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Support",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = TextDark
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { 
                        try {
                            val intent = android.content.Intent(android.content.Intent.ACTION_SENDTO).apply {
                                data = android.net.Uri.parse("mailto:nxtesportssupport@gmail.com")
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            android.widget.Toast.makeText(context, "No email client found", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = "Email",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Customer Support")
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onLogout,
            colors = ButtonDefaults.buttonColors(containerColor = androidx.compose.ui.graphics.Color.Red.copy(alpha = 0.1f)),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(text = "LOGOUT", color = androidx.compose.ui.graphics.Color.Red, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // MANDATORY ATTRIBUTE
        Text(
            text = "developed with love by oP",
            color = TextGray.copy(alpha = 0.6f),
            fontSize = 12.sp,
            modifier = Modifier.padding(bottom = 80.dp)
        )
    }
}

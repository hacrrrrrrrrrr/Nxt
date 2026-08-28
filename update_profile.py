import re

with open("app/src/main/java/com/example/ui/ProfileScreen.kt", "r") as f:
    content = f.read()

# Add imports
imports = """
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import java.util.UUID
"""

content = content.replace("import androidx.compose.ui.unit.sp", "import androidx.compose.ui.unit.sp" + imports)

# Add variables
state_vars = """
    var isEditing by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var avatarUrl by remember { mutableStateOf("") }
    var isShuffling by remember { mutableStateOf(false) }
"""
content = re.sub(r"var isEditing by remember \{ mutableStateOf\(false\) \}\n\s*var isSaving by remember \{ mutableStateOf\(false\) \}", state_vars, content)

# Update LaunchedEffect
launched_effect = """
    LaunchedEffect(Unit) {
        val user = SupabaseClient.client.auth.currentSessionOrNull()?.user
        val metadata = user?.userMetadata
        if (metadata != null) {
            ffName = metadata["ff_name"]?.jsonPrimitive?.content ?: ""
            ffUid = metadata["ff_uid"]?.jsonPrimitive?.content ?: ""
            googleName = metadata["full_name"]?.jsonPrimitive?.content ?: user.email ?: ""
            avatarUrl = metadata["avatar_url"]?.jsonPrimitive?.content ?: "https://api.dicebear.com/9.x/adventurer/png?seed=${user.id}"
        }
    }
"""

content = re.sub(r"LaunchedEffect\(Unit\) \{.*?googleName = metadata\[\"full_name\"\]\?.jsonPrimitive\?.content \?: user\.email \?: \"\"\n\s*\}\n\s*\}", launched_effect.strip(), content, flags=re.DOTALL)

# Replace Box
old_box = r"// Avatar Placeholder\n\s*Box\(\n\s*modifier = Modifier\n\s*\.size\(100\.dp\)\n\s*\.clip\(CircleShape\)\n\s*\.background\(PrimaryOrange\.copy\(alpha = 0\.2f\)\),\n\s*contentAlignment = Alignment\.Center\n\s*\) \{\n\s*Icon\(\n\s*imageVector = Icons\.Default\.Person,\n\s*contentDescription = \"Avatar\",\n\s*tint = PrimaryOrange,\n\s*modifier = Modifier\.size\(60\.dp\)\n\s*\)\n\s*\}"

new_box = """// Avatar Component
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(PrimaryOrange.copy(alpha = 0.2f))
                .clickable {
                    if (isShuffling) return@clickable
                    coroutineScope.launch {
                        isShuffling = true
                        try {
                            val currentUser = SupabaseClient.client.auth.currentSessionOrNull()?.user
                            if (currentUser != null) {
                                val newSeed = java.util.UUID.randomUUID().toString()
                                val newUrl = "https://api.dicebear.com/9.x/adventurer/png?seed=$newSeed"
                                avatarUrl = newUrl
                                
                                // Update metadata
                                SupabaseClient.client.auth.updateUser {
                                    data = buildJsonObject {
                                        put("avatar_url", newUrl)
                                    }
                                }
                                // Update profile
                                SupabaseClient.client.postgrest["profiles"].update(
                                    mapOf("avatar_url" to newUrl)
                                ) {
                                    filter { eq("id", currentUser.id) }
                                }
                            }
                        } catch (e: Exception) {
                            android.widget.Toast.makeText(context, "Failed to update avatar", android.widget.Toast.LENGTH_SHORT).show()
                        } finally {
                            isShuffling = false
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            if (avatarUrl.isNotEmpty()) {
                AsyncImage(
                    model = avatarUrl,
                    contentDescription = "Avatar",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Avatar",
                    tint = PrimaryOrange,
                    modifier = Modifier.size(60.dp)
                )
            }
            
            // Refresh Overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                if (isShuffling) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = SurfaceWhite, strokeWidth = 2.dp)
                } else {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Shuffle Avatar",
                        tint = SurfaceWhite,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        Text("Tap to shuffle avatar", fontSize = 10.sp, color = TextGray)
"""

content = re.sub(old_box, new_box, content)

with open("app/src/main/java/com/example/ui/ProfileScreen.kt", "w") as f:
    f.write(content)

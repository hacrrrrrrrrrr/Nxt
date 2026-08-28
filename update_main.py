import re

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

imports = """
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await
"""
content = content.replace("import io.github.jan.supabase.postgrest.postgrest", "import io.github.jan.supabase.postgrest.postgrest" + imports)

# Request permission in onCreate
request_permission_code = """
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission is granted, we can send notifications
        }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                // Granted
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                // Show educational UI
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun updateFcmToken(userId: String) {
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            try {
                val token = FirebaseMessaging.getInstance().token.await()
                SupabaseClient.client.postgrest["profiles"].update(
                    mapOf("fcm_token" to token)
                ) {
                    filter { eq("id", userId) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
"""

content = content.replace("override fun onCreate(savedInstanceState: Bundle?) {", request_permission_code)
content = content.replace("super.onCreate(savedInstanceState)", "super.onCreate(savedInstanceState)\n        askNotificationPermission()")

# Update the session authentication logic to update the FCM token
session_auth = """
                                if (session != null) {
                                    val userId = session.user!!.id
                                    updateFcmToken(userId)
"""
content = content.replace("if (session != null) {\n                                    val userId = session.user!!.id", session_auth)

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)

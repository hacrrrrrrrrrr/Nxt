import re

with open("app/src/main/java/com/example/services/MyFirebaseMessagingService.kt", "r") as f:
    content = f.read()

new_func = """
    private fun showNotification(title: String, message: String) {
        // We MUST use a new Channel ID because Android caches channel settings (like sounds).
        // If we reuse the old ID, the new custom sound will be ignored.
        val channelId = "jod_esports_custom_sound_channel" 
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_ONE_SHOT
        )

        // Point to the custom sound in res/raw/notification.mp3
        val soundUri = android.net.Uri.parse(android.content.ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + packageName + "/" + R.raw.notification)

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setSound(soundUri)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Jod Esports Notifications",
                NotificationManager.IMPORTANCE_HIGH
            )
            
            // Set the custom sound on the channel
            val audioAttributes = android.media.AudioAttributes.Builder()
                .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(android.media.AudioAttributes.USAGE_NOTIFICATION)
                .build()
            channel.setSound(soundUri, audioAttributes)
            
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }
"""

content = re.sub(r"private fun showNotification.*?\}\n\}", new_func.strip() + "\n}", content, flags=re.DOTALL)

with open("app/src/main/java/com/example/services/MyFirebaseMessagingService.kt", "w") as f:
    f.write(content)

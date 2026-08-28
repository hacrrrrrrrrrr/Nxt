import re

with open("app/src/main/AndroidManifest.xml", "r") as f:
    content = f.read()

permissions = """
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
"""

content = content.replace("<application", permissions + "\n    <application")

service = """
        <service
            android:name=".services.MyFirebaseMessagingService"
            android:exported="true">
            <intent-filter>
                <action android:name="com.google.firebase.MESSAGING_EVENT" />
            </intent-filter>
        </service>
"""

content = content.replace("</application>", service + "\n    </application>")

with open("app/src/main/AndroidManifest.xml", "w") as f:
    f.write(content)

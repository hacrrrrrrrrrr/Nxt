import org.gradle.api.tasks.testing.Test
plugins {
    id("com.android.application")
}
android {
  testOptions {
    unitTests {
      isIncludeAndroidResources = true
      all {
        jvmArgs("-Drobolectric.enabledSdks=36")
      }
    }
  }
}

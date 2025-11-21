# =====================
# Glide
# =====================
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.AppGlideModule
-keep public class * extends com.bumptech.glide.annotation.GlideExtension
-keep public class * extends com.bumptech.glide.annotation.GlideModule
-keep class com.bumptech.glide.GeneratedAppGlideModuleImpl { *; }

# =====================
# TensorFlow Lite
# =====================
-keep class org.tensorflow.lite.** { *; }
-keep class org.tensorflow.lite.task.** { *; }
-keep class org.tensorflow.lite.support.** { *; }

# =====================
# CameraX
# =====================
-keep class androidx.camera.core.** { *; }
-keep class androidx.camera.lifecycle.** { *; }
-keep class androidx.camera.view.** { *; }

# =====================
# Kotlin
# =====================
-keepclassmembers class kotlin.Metadata { *; }

# =====================
# AndroidX Annotations
# =====================
-keep class androidx.annotation.** { *; }

# =====================
# General
# =====================
-dontwarn org.checkerframework.**
-dontwarn kotlinx.coroutines.**

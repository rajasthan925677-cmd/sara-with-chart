# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile





# Add project specific ProGuard rules here.
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Preserve annotations, signatures, and exceptions
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes Exceptions
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute Source

# Firebase General
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# Firebase Firestore
-keep class com.google.firebase.firestore.** { *; }
-dontwarn com.google.firebase.firestore.**

# Firebase Cloud Messaging (FCM)
-keep class com.google.firebase.messaging.** { *; }
-dontwarn com.google.firebase.messaging.**

# Firebase Crashlytics
-keep class com.google.firebase.crashlytics.** { *; }
-dontwarn com.google.firebase.crashlytics.**

# Firebase Authentication
-keep class com.google.firebase.auth.** { *; }
-dontwarn com.google.firebase.auth.**

# Firebase Realtime Database
-keep class com.google.firebase.database.** { *; }
-dontwarn com.google.firebase.database.**

# Firebase Storage
-keep class com.google.firebase.storage.** { *; }
-dontwarn com.google.firebase.storage.**

# Google Play Services
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

# Jetpack Compose
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# Coroutines
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**

# Keep baseline profile classes
-keep class androidx.profileinstaller.** { *; }
-dontwarn androidx.profileinstaller.**

# Keep ALL app classes to cover all data classes and prevent obfuscation
-keep class com.example.sara567.** { *; }
-dontwarn com.example.sara567.**

# Keep Kotlin data class properties and getters/setters
-keepclassmembers class ** {
    *** get*();
    void set*(***);
}

# Additional rule for Firestore serialization (handles nested objects, lists, etc.)
-keepclassmembers class com.example.sara567.** {
    public private protected *;
}



# Firestore model classes ko shrink/obfuscate mat karo
-keepclassmembers class com.example.sara567.model.** {
    public <init>();
    public <init>(...);
    <fields>;
}


# Firestore requires classes to have a public no-arg constructor
-keepclassmembers class * {
    public <init>();
}


# Firebase model classes ko preserve karo
-keepclassmembers class firebase.** {
    <init>();
    <fields>;
}



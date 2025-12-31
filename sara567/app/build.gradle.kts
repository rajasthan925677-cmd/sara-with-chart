plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.google.firebase.crashlytics)
}

android {
    namespace = "com.example.sara567"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.sara567"
        minSdk = 23
        targetSdk = 35
        versionCode = 2
        versionName = "2.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }












    signingConfigs {
        create("release") {
            storeFile = file(properties["storeFile"] as String)
            storePassword = properties["storePassword"] as String
            keyAlias = properties["keyAlias"] as String
           keyPassword = properties["keyPassword"] as String

//            storeFile = rootProject.file(project.property("storeFile") as String)
//            storePassword = project.properties["storePassword"].toString()
//            keyAlias = project.properties["keyAlias"].toString()
//            keyPassword = project.properties["keyPassword"].toString()



            enableV1Signing = true // Kotlin DSL में camelCase
            enableV2Signing = true // Kotlin DSL में camelCase

        }
    }








    buildTypes {
        getByName("release") {
            isMinifyEnabled = true                    // Kotlin DSL mein `isMinifyEnabled` hi use hota hai
            isShrinkResources = true        // `shrinkResources` ke liye `isShrinkResources`
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            signingConfig = signingConfigs.getByName("release") // Yeh line
        }
    }


    compileOptions {

        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}



dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.runtime.android)
    implementation(libs.navigation.compose)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.database.ktx)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.storage)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    implementation(libs.androidx.compose.material.icons.extended)



    implementation("androidx.compose.material:material:1.6.8")

    implementation("com.google.android.gms:play-services-base:18.7.0")
    implementation("com.google.android.gms:play-services-auth:21.3.0")

    implementation("com.google.android.gms:play-services-tasks:18.3.0")
    implementation("androidx.media3:media3-common-ktx:1.8.0")
    implementation("androidx.compose.foundation:foundation-layout-android:1.7.4")

    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.3")

    implementation("com.google.code.gson:gson:2.10.1")

    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
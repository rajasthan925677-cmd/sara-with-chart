package com.example.sara567

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import firebase.SharedPrefHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        //Log.d("MyApplication", "MyApplication onCreate called")

        // Clear SharedPreferences on fresh install
        if (!SharedPrefHelper.isLoggedIn(this)) {
            SharedPrefHelper.logout(this)
            //Log.d("MyApplication", "Cleared SharedPreferences on fresh install")
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                FirebaseApp.initializeApp(this@MyApplication)
                FirebaseFirestore.getInstance().firestoreSettings = FirebaseFirestoreSettings.Builder()
                    .setPersistenceEnabled(true)
                    .build()
                FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)
                //Log.d("MyApplication", "Firebase initialized on background thread")
            } catch (e: Exception) {
                //Log.e("MyApplication", "Firebase initialization failed: ${e.message}")
            }
        }
    }
}
package com.example.sara567

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.sara567.ui.theme.sara567Theme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.AuthStateListener
import com.google.firebase.messaging.FirebaseMessaging
import firebase.FirebaseHelper
import firebase.SharedPrefHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import navigation.AppNavGraph
import navigation.Screen
import viewmodal.LocalDrawerState


class MainActivity : ComponentActivity() {
    private var navController: NavHostController? = null
    private var isNavigating = false
    private val auth = FirebaseAuth.getInstance()
    private val coroutineScope = CoroutineScope(Dispatchers.Main + Job())
    private var isSigningUp by mutableStateOf(false) // Flag for signup process

    private fun <T> debounce(
        waitMs: Long = 1500L,
        scope: CoroutineScope,
        destination: suspend (T) -> Unit
    ): (T) -> Unit {
        var debounceJob: Job? = null
        return { param: T ->
            debounceJob?.cancel()
            debounceJob = scope.launch {
                delay(waitMs)
                destination(param)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // ============ NOTIFICATION PERMISSION (Android 13+) ============
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    101
                )
            }
        }

//        // ============ INSTALL UNKNOWN APPS PERMISSION (GitHub APK ke liye) ============
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            if (!packageManager.canRequestPackageInstalls()) {
//                val uri = Uri.fromParts("package", packageName, null)
//                val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, uri)
//                //                  ^^^^^^^^   ← YE SAHI HAI (Settings, FontVariation nahi!)
//                startActivity(intent)
//                Toast.makeText(this, "Please allow 'Install from this app'", Toast.LENGTH_LONG).show()
//            }
//        }

        // ============ FCM TOKEN ============
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val token = FirebaseMessaging.getInstance().token.await()
                FirebaseMessaging.getInstance().subscribeToTopic("main_notifications").await()
                FirebaseMessaging.getInstance().subscribeToTopic("game_notifications").await()
            } catch (e: Exception) { }
        }

//        setContent {
//            sara567Theme {
//                val nav: NavHostController = rememberNavController()
//                navController = nav
//                var isSignedOut by remember {
//                    mutableStateOf(auth.currentUser == null || !SharedPrefHelper.isLoggedIn(this@MainActivity))
//                }
//
//                fun navigateToSignIn() {
//                    if (!isNavigating && !isSigningUp) {
//                        isNavigating = true
//                        //Log.d("MainActivity", "Navigating to SignIn")
//                        SharedPrefHelper.logout(this@MainActivity)
//                        FirebaseHelper().stopAllListeners()
//                        nav.navigate(Screen.SignInPage.route) {
//                            popUpTo(nav.graph.startDestinationId) { inclusive = true }
//                            launchSingleTop = true
//                        }
//                        coroutineScope.launch {
//                            delay(1500)
//                            isNavigating = false
//                        }
//                    }
//                }
//
//                suspend fun checkTokenAndNavigate() {
//                    // Retry mechanism to handle async delays
//                    repeat(3) { attempt ->
//                        try {
//                            auth.currentUser?.getIdToken(true)?.await()
//                            withContext(Dispatchers.Main) {
//                                //Log.d("MainActivity", "Token check successful on attempt $attempt")
//                                if (!SharedPrefHelper.isLoggedIn(this@MainActivity) && !isSigningUp) {
//                                    //Log.d("MainActivity", "SharedPref indicates not logged in, navigating to SignIn")
//                                    isSignedOut = true
//                                    navigateToSignIn()
//                                } else {
//                                    isSignedOut = false
//                                }
//                            }
//                            return // Exit if successful
//                        } catch (e: Exception) {
//                            //Log.e("MainActivity", "Token check failed on attempt $attempt: ${e.message}")
//                            if (attempt < 2) delay(500) // Wait before retrying
//                        }
//                    }
//                    // After retries, if still failed
//                    withContext(Dispatchers.Main) {
//                        if (!isSigningUp) {
//                            isSignedOut = true
//                            navigateToSignIn()
//                        }
//                    }
//                }
//
//                val debouncedTokenCheck = debounce<Unit>(1500L, coroutineScope) {
//                    CoroutineScope(Dispatchers.IO).launch {
//                        checkTokenAndNavigate()
//                    }
//                }
//
//                // Initial auth check
//                LaunchedEffect(Unit) {
//                    if ((auth.currentUser == null || !SharedPrefHelper.isLoggedIn(this@MainActivity)) && !isSigningUp) {
//                        //Log.d("MainActivity", "No user or SharedPref not logged in, navigating to SignIn")
//                        isSignedOut = true
//                        navigateToSignIn()
//                    } else {
//                        debouncedTokenCheck(Unit)
//                    }
//                }
//
//                val authStateListener = remember {
//                    AuthStateListener { firebaseAuth ->
//                        if ((firebaseAuth.currentUser == null || !SharedPrefHelper.isLoggedIn(this@MainActivity)) && !isSigningUp) {
//                            //Log.d("MainActivity", "AuthStateListener: User signed out or SharedPref not logged in")
//                            isSignedOut = true
//                            navigateToSignIn()
//                        }
//                    }
//                }
//
//                DisposableEffect(auth) {
//                    auth.addAuthStateListener(authStateListener)
//                    onDispose {
//                        auth.removeAuthStateListener(authStateListener)
//                        //Log.d("MainActivity", "AuthStateListener removed")
//                    }
//                }
//
//                val startDestination = if (isSignedOut) Screen.SignInPage.route else Screen.HomePage.route
//                AppNavGraph(navController = nav, startDestination = startDestination)
//            }
//        }

//        setContent {
//            sara567Theme {
//                val nav: NavHostController = rememberNavController()
//                navController = nav
//
//                var isSignedOut by remember { mutableStateOf(auth.currentUser == null) }
//
////                fun navigateToSignIn() {
////                    if (!isNavigating) {
////                        isNavigating = true
////                        SharedPrefHelper.logout(this@MainActivity)
////                        FirebaseHelper().stopAllListeners()
////                        nav.navigate(Screen.SignInPage.route) {
////                            popUpTo(nav.graph.startDestinationId) { inclusive = true }
////                            launchSingleTop = true
////                        }
////                        coroutineScope.launch {
////                            delay(1000)
////                            isNavigating = false
////                        }
////                    }
////                }
//
//                fun navigateToSignIn() {
//                    if (!isNavigating && navController?.currentDestination?.route != Screen.SignInPage.route) {
//                        isNavigating = true
//                        SharedPrefHelper.logout(this@MainActivity)
//                        FirebaseHelper().stopAllListeners()
//                        navController?.navigate(Screen.SignInPage.route) {
//                            popUpTo(navController!!.graph.startDestinationId) { inclusive = true }
//                            launchSingleTop = true
//                        }
//                        coroutineScope.launch {
//                            delay(1000)
//                            isNavigating = false
//                        }
//                    }
//                }
//
//
//
//
//                // Initial check (app start pe)
//                LaunchedEffect(Unit) {
//                    if (auth.currentUser == null) {
//                        isSignedOut = true
//                        navigateToSignIn()
//                    } else {
//                        isSignedOut = false
//                    }
//                }
//
//                // Ye wala AuthStateListener bilkul perfect hai — isko mat chhedna!
//                val authStateListener = remember {
//                    AuthStateListener { firebaseAuth ->
//                        if (firebaseAuth.currentUser == null) {
//                            isSignedOut = true
//                            navigateToSignIn()
//                        }
//                    }
//                }
//
//                DisposableEffect(auth) {
//                    auth.addAuthStateListener(authStateListener)
//                    onDispose {
//                        auth.removeAuthStateListener(authStateListener)
//                    }
//                }
//
//                val startDestination = if (isSignedOut) Screen.SignInPage.route else Screen.HomePage.route
//                AppNavGraph(navController = nav, startDestination = startDestination)
//            }
//        }

//        setContent {
//            sara567Theme {
//                val nav: NavHostController = rememberNavController()
//                navController = nav
//                var isSignedOut by remember { mutableStateOf(auth.currentUser == null) }
//
//                // YE FUNCTION BILKUL SAHI — SharedPref logout hata diya
//                fun navigateToSignIn() {
//                    if (!isNavigating && navController?.currentDestination?.route != Screen.SignInPage.route) {
//                        isNavigating = true
//                        FirebaseHelper().stopAllListeners()  // Sirf listeners band
//                        navController?.navigate(Screen.SignInPage.route) {
//                            popUpTo(navController!!.graph.startDestinationId) { inclusive = true }
//                            launchSingleTop = true
//                        }
//                        coroutineScope.launch {
//                            delay(1000)
//                            isNavigating = false
//                        }
//                    }
//                }
//
//                // Initial check
//                LaunchedEffect(Unit) {
//                    if (auth.currentUser == null) {
//                        SharedPrefHelper.logout(this@MainActivity)
//                        isSignedOut = true
//                        navigateToSignIn()
//                    } else {
//                        isSignedOut = false
//                    }
//                }
//
//                // Auth listener
//                val authStateListener = remember {
//                    AuthStateListener { firebaseAuth ->
//                        if (firebaseAuth.currentUser == null) {
//                            SharedPrefHelper.logout(this@MainActivity)
//                            isSignedOut = true
//                            navigateToSignIn()
//                        }
//                    }
//                }
//
//                DisposableEffect(auth) {
//                    auth.addAuthStateListener(authStateListener)
//                    onDispose { auth.removeAuthStateListener(authStateListener) }
//                }
//
//                val startDestination = if (isSignedOut) Screen.SignInPage.route else Screen.HomePage.route
//                AppNavGraph(navController = nav, startDestination = startDestination)
//            }
//        }

//        setContent {
//            sara567Theme {
//                val nav: NavHostController = rememberNavController()
//                navController = nav
//
//                // Pehle assume karo user logout hai
//                var isSignedOut by remember { mutableStateOf(true) }
//
//                fun navigateToSignIn() {
//                    if (!isNavigating && navController?.currentDestination?.route != Screen.SignInPage.route) {
//                        isNavigating = true
//                        FirebaseHelper().stopAllListeners()
//                        navController?.navigate(Screen.SignInPage.route) {
//                            popUpTo(0) { inclusive = true }  // Pura backstack clear
//                            launchSingleTop = true
//                        }
//                        coroutineScope.launch {
//                            delay(1000)
//                            isNavigating = false
//                        }
//                    }
//                }
//
//                // YE HAI ASLI FIX — Token check karo, currentUser nahi!
//                LaunchedEffect(Unit) {
//                    val user = auth.currentUser
//                    if (user == null) {
//                        // Seedha logout
//                        SharedPrefHelper.logout(this@MainActivity)
//                        isSignedOut = true
//                    } else {
//                        // Token valid hai ya nahi?
//                        try {
//                            user.getIdToken(true).await()  // Force refresh
//                            // Token valid → user sach mein logged in hai
//                            isSignedOut = false
//                        } catch (e: Exception) {
//                            // Token invalid ya expired → logout karo
//                            auth.signOut()
//                            SharedPrefHelper.logout(this@MainActivity)
//                            isSignedOut = true
//                            navigateToSignIn()
//                        }
//                    }
//                }
//
//                // AuthStateListener sirf backup ke liye
//                val authStateListener = remember {
//                    AuthStateListener { firebaseAuth ->
//                        if (firebaseAuth.currentUser == null) {
//                            SharedPrefHelper.logout(this@MainActivity)
//                            isSignedOut = true
//                            navigateToSignIn()
//                        }
//                    }
//                }
//
//                DisposableEffect(auth) {
//                    auth.addAuthStateListener(authStateListener)
//                    onDispose { auth.removeAuthStateListener(authStateListener) }
//                }
//
//                val startDestination = if (isSignedOut) Screen.SignInPage.route else Screen.HomePage.route
//                AppNavGraph(navController = nav, startDestination = startDestination)
//            }
//        }






        setContent {
            sara567Theme {
                val nav: NavHostController = rememberNavController()
                navController = nav

//
// YE 3 LINES YAHAN DAAL DO — GLOBAL DRAWER STATE
                val drawerState = androidx.compose.material3.rememberDrawerState(
                    initialValue = androidx.compose.material3.DrawerValue.Closed
                )
                val scope = rememberCoroutineScope()

                // Ye flash ko 100% rok dega
//                LaunchedEffect(drawerState) {
//                    drawerState.snapTo(androidx.compose.material3.DrawerValue.Closed)
//                }
                LaunchedEffect(Unit) {
                    withContext(Dispatchers.IO) {
                        SharedPrefHelper.updateShareLinkFromFirebase(applicationContext)
                    }
                }


                // Ye flag batayega ki humne check kar liya
                var authChecked by remember { mutableStateOf(false) }
                var isSignedOut by remember { mutableStateOf(true) }

                fun navigateToSignIn() {
                    if (!isNavigating && navController?.currentDestination?.route != Screen.SignInPage.route) {
                        isNavigating = true
                        FirebaseHelper().stopAllListeners()
                        navController?.navigate(Screen.SignInPage.route) {
                            popUpTo(0) { inclusive = true }
                            launchSingleTop = true
                        }
                        coroutineScope.launch {
                            delay(1000)
                            isNavigating = false
                        }
                    }
                }

                // YE HAI SABSE MAST FIX — Pehle SharedPref check, phir Firebase
                LaunchedEffect(Unit) {
                    if (authChecked) return@LaunchedEffect

                    // Sabse pehle SharedPref check karo
                    if (SharedPrefHelper.isLoggedIn(this@MainActivity)) {
                        // Agar SharedPref mein login hai → maan lo user login hi hai
                        // Chahe internet ho ya na ho!
                        if (auth.currentUser != null) {
                            isSignedOut = false   // ← Direct Home kholo!
                        } else {
                            // Bahut rare case — SharedPref mein hai par Firebase mein nahi
                            SharedPrefHelper.logout(this@MainActivity)
                            isSignedOut = true
                        }
                    } else {
                        // SharedPref mein hi nahi hai → SignIn page
                        isSignedOut = true
                    }

                    authChecked = true
                }
                // Backup AuthStateListener — agar beech mein logout ho jaye
                val authStateListener = remember {
                    AuthStateListener { firebaseAuth ->
                        if (firebaseAuth.currentUser == null) {
                            SharedPrefHelper.logout(this@MainActivity)
                            isSignedOut = true
                            navigateToSignIn()
                        }
                    }
                }

                DisposableEffect(auth) {
                    auth.addAuthStateListener(authStateListener)
                    onDispose { auth.removeAuthStateListener(authStateListener) }
                }
                CompositionLocalProvider(LocalDrawerState provides drawerState) {
                    // Sirf tab UI dikhao jab check complete ho jaye
                    if (authChecked) {
                        val startDestination =
                            if (isSignedOut) Screen.SignInPage.route else Screen.HomePage.route
                        AppNavGraph(navController = nav, startDestination = startDestination)
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFFFCFDFD)),  // background color
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.welcomesara777),
                                contentDescription = "Sara777 Welcome Screen",
                                modifier = Modifier
                                    .fillMaxWidth(0.8f)        // 90% width tak jayega
                                    .fillMaxHeight(0.6f)      // 70% height tak jayega (safe area)
                                    .align(Alignment.Center), // center mein rakhega
                                contentScale = ContentScale.Fit   // ← YE SABSE ZAROORI HAI!
                            )

                            // Optional: Loading indicator niche daal do
                            CircularProgressIndicator(
                                color = Color(0xFFFABE0F),
                                strokeWidth = 5.dp,
                                modifier = Modifier
                                    // .size(50.dp)
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 80.dp)
                            )
                        }
                    }
                }
            }

        }
    }

    // Function to set signup state
    fun setSigningUpState(isSigningUp: Boolean) {
        this.isSigningUp = isSigningUp
        //Log.d("MainActivity", "isSigningUp set to: $isSigningUp")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
        super.onResume()
        //Log.d("MainActivity", "onResume triggered")

//        val debouncedTokenCheck = debounce<Unit>(1500L, coroutineScope) {
//            CoroutineScope(Dispatchers.IO).launch {
//                if ((auth.currentUser == null || !SharedPrefHelper.isLoggedIn(this@MainActivity)) && !isSigningUp) {
//                    withContext(Dispatchers.Main) {
//                        if (!isNavigating) {
//                            isNavigating = true
//                            //Log.d("MainActivity", "No user or SharedPref not logged in in onResume, navigating to SignIn")
//                            SharedPrefHelper.logout(this@MainActivity)
//                            FirebaseHelper().stopAllListeners()
//                            navController?.navigate(Screen.SignInPage.route) {
//                                popUpTo(navController?.graph?.startDestinationId ?: 0) { inclusive = true }
//                                launchSingleTop = true
//                            }
//                            coroutineScope.launch {
//                                delay(1500)
//                                isNavigating = false
//                            }
//                        }
//                    }
//                } else {
//                    try {
//                        auth.currentUser?.getIdToken(true)?.await()
//                        withContext(Dispatchers.Main) {
//                            //Log.d("MainActivity", "onResume token check successful")
//                        }
//                    } catch (e: Exception) {
//                        withContext(Dispatchers.Main) {
//                            //Log.e("MainActivity", "onResume token check failed: ${e.message}")
//                            if (!isNavigating && !isSigningUp) {
//                                isNavigating = true
//                                auth.signOut()
//                                SharedPrefHelper.logout(this@MainActivity)
//                                FirebaseHelper().stopAllListeners()
//                                navController?.navigate(Screen.SignInPage.route) {
//                                    popUpTo(navController?.graph?.startDestinationId ?: 0) { inclusive = true }
//                                    launchSingleTop = true
//                                }
//                                coroutineScope.launch {
//                                    delay(1500)
//                                    isNavigating = false
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }
//
//        debouncedTokenCheck(Unit)
    }

    override fun onDestroy() {
        coroutineScope.coroutineContext.cancelChildren()
        super.onDestroy()
    }
}
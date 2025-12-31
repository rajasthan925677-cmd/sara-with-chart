//package screens
//
//import android.content.Intent
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxHeight
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.size
//import androidx.compose.foundation.layout.width
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.automirrored.filled.ExitToApp
//import androidx.compose.material.icons.filled.Add
//import androidx.compose.material.icons.filled.BarChart
//import androidx.compose.material.icons.filled.CurrencyRupee
//import androidx.compose.material.icons.filled.Gavel
//import androidx.compose.material.icons.filled.Home
//import androidx.compose.material.icons.filled.Person
//import androidx.compose.material.icons.filled.Settings
//import androidx.compose.material.icons.filled.Share
//
//import androidx.compose.material3.Icon
//import androidx.compose.material3.ModalDrawerSheet
//import androidx.compose.material3.ModalNavigationDrawer
//import androidx.compose.material3.Switch
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.setValue
//import androidx.compose.material3.ModalNavigationDrawer
//import androidx.compose.material3.ModalDrawerSheet
//import androidx.compose.material3.DrawerState
//import androidx.compose.material3.DrawerValue
//import androidx.compose.material3.rememberDrawerState
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.vector.ImageVector
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.platform.LocalDensity
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.navigation.NavController
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.firestore.BuildConfig
//import com.google.firebase.firestore.FirebaseFirestore
//import com.google.firebase.messaging.FirebaseMessaging
//import firebase.SharedPrefHelper
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.tasks.await
//import kotlinx.coroutines.withContext
//
//@Composable
//fun NavigationDrawer(
//    navController: NavController,
//    shareLink: String,
//    drawerState: DrawerState,
//    scope: CoroutineScope,
//    content: @Composable () -> Unit
//) {
//    val context = LocalContext.current
//    val density = LocalDensity.current
//
//    var showSettings by remember { mutableStateOf(false) }
//    var mainNotification by remember { mutableStateOf(true) }
//    var gameNotification by remember { mutableStateOf(true) }
//
//    var mobNo by remember { mutableStateOf(SharedPrefHelper.getMobile(context) ?: "XXXXXXXXXX") }
//    val userName = SharedPrefHelper.getUsername(context) ?: "User"
//    val drawerWidth = with(density) { 250.dp * density.density / 2 }
//
//    var dynamicShareLink by remember { mutableStateOf(shareLink) }
//
//    LaunchedEffect(Unit) {
//        withContext(Dispatchers.IO) {
//            try {
//                val db = FirebaseFirestore.getInstance()
//                val document = db.collection("appInfo").document("share").get().await()
//                withContext(Dispatchers.Main) {
//                    if (document != null && document.exists()) {
//                        dynamicShareLink = document.getString("link") ?: ""
//                    }
//                }
//            } catch (e: Exception) {
//                println("Error fetching share link: ${e.message}")
//            }
//        }
//    }
//
//    ModalNavigationDrawer(
//        drawerState = drawerState,
//        drawerContent = {
//            ModalDrawerSheet(
//                modifier = Modifier
//                    .fillMaxHeight()
//                    .width(drawerWidth),
//                drawerContainerColor = Color(0xFFFFFFFF)
//            ) {
//                LazyColumn {
//                    item {
//                        Row(
//                            verticalAlignment = Alignment.CenterVertically,
//                            modifier = Modifier.padding(with(density) { 16.dp * density.density / 2 })
//                        ) {
//                            Icon(
//                                imageVector = Icons.Default.Person,
//                                contentDescription = "User",
//                                tint = Color.Gray,
//                                modifier = Modifier.size(with(density) { 40.dp * density.density / 2 })
//                            )
//                            Spacer(modifier = Modifier.width(with(density) { 8.dp * density.density / 2 }))
//                            Column {
//                                Text(
//                                    userName,
//                                    fontWeight = FontWeight.Bold,
//                                    fontSize = with(density) { 16.sp * density.density / 2 }
//                                )
//                                Text(
//                                    mobNo,
//                                    fontSize = with(density) { 12.sp * density.density / 2 },
//                                    color = Color.Gray
//                                )
//                            }
//                        }
//                        Spacer(modifier = Modifier.height(with(density) { 10.dp * density.density / 2 }))
//                    }
//
//                    // ←←← यहाँ नया "Chart" बटन जोड़ा गया है ←←←
//                    val drawerItems = listOf(
//                        "Home" to Icons.Default.Home,
//                        "My Bids" to Icons.Default.Gavel ,
//                        "Funds" to Icons.Default.Add,
//
//
//                        "Chart" to Icons.Default.BarChart,
//
//                        "Game Rate" to Icons.Default.CurrencyRupee,
//                        "Settings" to Icons.Default.Settings,
//                        "Share App" to Icons.Default.Share,
//                        "Logout" to Icons.AutoMirrored.Filled.ExitToApp
//                    )
//
//                    items(drawerItems) { (title, icon) ->
//                        DrawerItem(title, icon) {
//                            when (title) {
//                                "Settings" -> showSettings = !showSettings
//                                else -> {
//                                    scope.launch {
//                                        drawerState.close()
//                                    }
//                                    when (title) {
//                                        "Home" -> navController.navigate("home")
//                                        "My Bids" -> navController.navigate("bids")
//                                        "Funds" -> navController.navigate("funds")
//                                        "Chart" -> navController.navigate("chart")          // ← नया रूट
//                                        "Game Rate" -> navController.navigate("game_rate")
//                                        "Share App" -> {
//                                            val intent = Intent(Intent.ACTION_SEND).apply {
//                                                type = "text/plain"
//                                                putExtra(
//                                                    Intent.EXTRA_TEXT,
//                                                    "Download our app via this link - safe, secure and trusted platform since 2007 \n : $dynamicShareLink"
//                                                )
//                                            }
//                                            context.startActivity(Intent.createChooser(intent, "Share via"))
//                                        }
////                                        "Logout" -> {
////                                            SharedPrefHelper.logout(context)
////                                            navController.navigate("signin") {
////                                                popUpTo("home") { inclusive = true }
////                                            }
////                                        }
//
//                                        "Logout" -> {
//                                            // YE 2 LINES DAAL DO — BAS!
//                                            FirebaseAuth.getInstance().signOut()           // ← YE THA MISSING!
//                                            SharedPrefHelper.logout(context)
//
//                                            navController.navigate("signin") {
//                                                popUpTo(0) { inclusive = true }  // Pura backstack hatao
//                                                launchSingleTop = true
//                                            }
//                                        }
//
//
//                                    }
//                                }
//                            }
//                        }
//
//                        if (title == "Settings" && showSettings) {
//                            Column(
//                                modifier = Modifier
//                                    .fillMaxWidth()
//                                    .padding(
//                                        start = with(density) { 24.dp * density.density / 2 },
//                                        top = with(density) { 8.dp * density.density / 2 },
//                                        bottom = with(density) { 8.dp * density.density / 2 },
//                                        end = with(density) { 16.dp * density.density / 2 }
//                                    )
//                            ) {
//                                Row(
//                                    modifier = Modifier
//                                        .fillMaxWidth()
//                                        .padding(vertical = with(density) { 6.dp * density.density / 2 }),
//                                    verticalAlignment = Alignment.CenterVertically
//                                ) {
//                                    Text(
//                                        "Main Notification",
//                                        fontSize = with(density) { 14.sp * density.density / 2 },
//                                        modifier = Modifier.weight(1f)
//                                    )
//                                    Switch(
//                                        checked = mainNotification,
//                                        onCheckedChange = { checked ->
//                                            mainNotification = checked
//                                            CoroutineScope(Dispatchers.IO).launch {
//                                                try {
//                                                    if (checked) {
//                                                        FirebaseMessaging.getInstance()
//                                                            .subscribeToTopic("main_notifications")
//                                                            .await()
//                                                    } else {
//                                                        FirebaseMessaging.getInstance()
//                                                            .unsubscribeFromTopic("main_notifications")
//                                                            .await()
//                                                    }
//                                                } catch (e: Exception) {
//                                                    println("Error updating main notification subscription: ${e.message}")
//                                                }
//                                            }
//                                        }
//                                    )
//                                }
//
//                                Row(
//                                    modifier = Modifier
//                                        .fillMaxWidth()
//                                        .padding(vertical = with(density) { 6.dp * density.density / 2 }),
//                                    verticalAlignment = Alignment.CenterVertically
//                                ) {
//                                    Text(
//                                        "Game Notification",
//                                        fontSize = with(density) { 14.sp * density.density / 2 },
//                                        modifier = Modifier.weight(1f)
//                                    )
//                                    Switch(
//                                        checked = gameNotification,
//                                        onCheckedChange = { checked ->
//                                            gameNotification = checked
//                                            CoroutineScope(Dispatchers.IO).launch {
//                                                try {
//                                                    if (checked) {
//                                                        FirebaseMessaging.getInstance()
//                                                            .subscribeToTopic("game_notifications")
//                                                            .await()
//                                                    } else {
//                                                        FirebaseMessaging.getInstance()
//                                                            .unsubscribeFromTopic("game_notifications")
//                                                            .await()
//                                                    }
//                                                } catch (e: Exception) {
//                                                    println("Error updating game notification subscription: ${e.message}")
//                                                }
//                                            }
//                                        }
//                                    )
//                                }
//                            }
//
//                        }
//
//
//
//
//
//                    }
//
//
//                    item {
//                        val context = LocalContext.current
//                        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
//
//                        val vName = packageInfo.versionName ?: "2.0"
//                        val vCode = if (android.os.Build.VERSION.SDK_INT >= 28)
//                            packageInfo.longVersionCode.toString()
//                          else
//                            @Suppress("DEPRECATION") packageInfo.versionCode.toString()
//
//                        Spacer(modifier = Modifier.height(30.dp))
//                        Text(
//                            text = "Version:$vName",
//                            fontSize = 12.sp,
//                            color = Color.Gray,
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .padding(vertical = 20.dp),
//                            textAlign = TextAlign.Center
//                        )
//                    }
//
//
//
//
//
//
//
//
//
//                }
//            }
//        }
//    ) {
//        content()
//    }
//}
//
//@Composable
//fun DrawerItem(title: String, icon: ImageVector, onClick: () -> Unit) {
//    val density = LocalDensity.current
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .clickable { onClick() }
//            .padding(with(density) { 12.dp * density.density / 2 }),
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//        Icon(
//            imageVector = icon,
//            contentDescription = title,
//            tint = Color.Black,
//            modifier = Modifier.size(with(density) { 24.dp * density.density / 2 })
//        )
//        Spacer(modifier = Modifier.width(with(density) { 16.dp * density.density / 2 }))
//        Text(
//            text = title,
//            fontSize = with(density) { 16.sp * density.density / 2 }
//        )
//    }
//}


package screens

import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PersonPin
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import firebase.SharedPrefHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import viewmodal.LocalDrawerState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationDrawer(
    navController: NavController,
   // drawerState: DrawerState,
   // scope: CoroutineScope,
    shareLink: String = "https://sara777.com",
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val drawerState = LocalDrawerState.current
    val scope = rememberCoroutineScope()


    // YE HAI JAADU — Screen width ka 78% drawer banega!
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val drawerWidth = remember(screenWidth) {
        (screenWidth * 0.75f).coerceAtMost(360.dp)  // Max 360dp tak ja sakta hai
    }



    var showSettings by remember { mutableStateOf(false) }
    var mainNotification by remember { mutableStateOf(true) }
    var gameNotification by remember { mutableStateOf(true) }

    val userName = SharedPrefHelper.getUsername(context) ?: "User"
    val mobNo = SharedPrefHelper.getMobile(context) ?: "XXXXXXXXXX"

//    var dynamicShareLink by remember { mutableStateOf(shareLink) }
//
//    LaunchedEffect(Unit) {
//        withContext(Dispatchers.IO) {
//            try {
//                val link = FirebaseFirestore.getInstance()
//                    .collection("appInfo").document("share")
//                    .get().await().getString("link") ?: shareLink
//                withContext(Dispatchers.Main) { dynamicShareLink = link }
//            } catch (e: Exception) { }
//        }
//    }



    val dynamicShareLink = SharedPrefHelper.getShareLink(context)  // 0ms load!

    // YE HAI ASLI JAADU — Modal + gesturesEnabled = false + snapTo(Closed)
    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = drawerState.isOpen, // sirf khula ho tab hi swipe se band ho

        scrimColor = Color.Black.copy(alpha = 0.6f), // Pro dark overlay

        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(drawerWidth), // ← YE RESPONSIVE HAI!
                drawerContainerColor = Color.White
            ) {
                LazyColumn(modifier = Modifier.padding(10.dp)) {
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically , ) {
                            Icon(Icons.Default.PersonPin, null, tint = Color(0xFFF8A324), modifier = Modifier.size(68.dp))
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(userName, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Text(mobNo, fontSize = 14.sp, color = Color.Gray)
                            }
                        }
                        Spacer(Modifier.height(20.dp))
                    }

                    val items = listOf(
                        "Home" to Icons.Default.Home,
                        "My Bids" to Icons.Default.Gavel,
                        "Funds" to Icons.Default.Add,
                        "Chart" to Icons.Default.BarChart,
                        "Game Rate" to Icons.Default.CurrencyRupee,
                        "Settings" to Icons.Default.Settings,
                        "Share App" to Icons.Default.Share,
                        "Logout" to Icons.AutoMirrored.Filled.Logout
                    )

                    items(items) { (title, icon) ->
                        NavigationDrawerItem(
                            icon = { Icon(icon, contentDescription = title) },
                            label = { Text(title ,fontWeight = FontWeight.Bold,) },
                            selected = false,
                            onClick = {
                                when (title) {
                                    "Settings" -> showSettings = !showSettings
                                    "Logout" -> {
                                        FirebaseAuth.getInstance().signOut()
                                        SharedPrefHelper.logout(context)
                                        navController.navigate("signin") {
                                            popUpTo(0) { inclusive = true }
                                            launchSingleTop = true
                                        }
                                    }
                                    "Share App" -> {
                                        val intent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_TEXT, "Download Sara777 App: $dynamicShareLink")
                                        }
                                        context.startActivity(Intent.createChooser(intent, "Share via"))
                                    }
                                    else -> {
                                        scope.launch { drawerState.close() }
                                        when (title) {
                                           // "Home" -> navController.navigate("home") { launchSingleTop = true }
                                            "Home" -> {
                                                scope.launch { drawerState.close() }

                                                // YE HAI ASLI JAADU — YE 3 LINES LAGA DE!
                                                if (navController.currentDestination?.route != "home") {
                                                    navController.navigate("home") {
                                                        popUpTo(navController.graph.startDestinationId)
                                                        launchSingleTop = true
                                                    }
                                                }
                                                // Agar already home pe hai to kuch nahi karo (drawer bas band ho jayega)
                                            }
                                            "My Bids" -> navController.navigate("bids")
                                            "Funds" -> navController.navigate("funds")
                                            "Chart" -> navController.navigate("chart")
                                            "Game Rate" -> navController.navigate("game_rate")
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                        )

                        if (title == "Settings" && showSettings) {
                            // Settings toggles
                            Column(Modifier.padding(start = 16.dp)) {
                                Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text("Main Notification", Modifier.weight(1f))
                                    Switch(checked = mainNotification, onCheckedChange = { mainNotification = it
                                        scope.launch(Dispatchers.IO) {
                                            try { if (it) FirebaseMessaging.getInstance().subscribeToTopic("main_notifications").await()
                                            else FirebaseMessaging.getInstance().unsubscribeFromTopic("main_notifications").await()
                                            } catch (e: Exception) { }
                                        }
                                    })
                                }
                                Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text("Game Notification", Modifier.weight(1f))
                                    Switch(checked = gameNotification, onCheckedChange = { gameNotification = it
                                        scope.launch(Dispatchers.IO) {
                                            try { if (it) FirebaseMessaging.getInstance().subscribeToTopic("game_notifications").await()
                                            else FirebaseMessaging.getInstance().unsubscribeFromTopic("game_notifications").await()
                                            } catch (e: Exception) { }
                                        }
                                    })
                                }
                            }
                        }
                    }

                    item {
                        Spacer(Modifier.height(40.dp))
                        val version = context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0"
                        Text("Version: $version", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center)
                    }
                }
            }
        }
    ) {
        content()
    }
}
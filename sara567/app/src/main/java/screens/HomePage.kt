package screens


import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AddCard
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Downloading
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Whatsapp
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.CurrencyRupee
import androidx.compose.material.icons.outlined.Gavel
import androidx.compose.material.icons.outlined.HeadsetMic
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.sara567.R
import firebase.SharedPrefHelper
import firebase.VersionUpdateHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import viewmodal.HomeTickerViewModel
import viewmodal.HomeViewModel
import viewmodal.LocalDrawerState
import viewmodal.WalletViewModel
import java.text.ParseException
import java.util.Locale
import kotlin.math.max
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun HomePage(
    navController: NavController,
  //  drawerState: DrawerState,
  //  scope: CoroutineScope,
    viewModel: HomeViewModel,
    walletViewModel: WalletViewModel,
   // tickerViewModel: HomeTickerViewModel = HomeTickerViewModel()
) {


    val drawerState = LocalDrawerState.current
    val scope = rememberCoroutineScope()

    val context = LocalContext.current

    val tickerViewModel: HomeTickerViewModel = remember {
        HomeTickerViewModel(context = context)
    }


    // ────────────────────── UPDATE POPUP — HAR BAAR DIKHEGA (Fresh Check) ──────────────────────



    var showUpdateDialog by remember { mutableStateOf(false) }
    var isForceUpdate by remember { mutableStateOf(false) }
    var downloadUrl by remember { mutableStateOf("") }

    var showNoBrowserDialog by remember { mutableStateOf(false) }
    var showInvalidUrlDialog by remember { mutableStateOf(false) }

    val currentAppVersion = remember {
        try {
            val ver = context.packageManager
                .getPackageInfo(context.packageName, 0)
                .let {
                    if (android.os.Build.VERSION.SDK_INT >= 28) it.longVersionCode.toInt()
                    else @Suppress("DEPRECATION") it.versionCode
                }
            //Log.d("UPDATE_CHECK", "Current App versionCode = $ver")
            ver
        } catch (e: Exception) {
           // Log.e("UPDATE_CHECK", "Error getting current version", e)
            1
        }
    }

    LaunchedEffect(Unit) {
       // Log.d("UPDATE_CHECK", "LaunchedEffect started")

        val (cachedVersion, cachedForce, cachedUrl) = SharedPrefHelper.getSavedVersionInfo(context)
        //Log.d("UPDATE_CHECK", "Cached version = $cachedVersion")

        // Sirf strictly greater hone par hi cache se popup dikhao
        if (cachedVersion > currentAppVersion) {
         //   Log.d("UPDATE_CHECK", "Popup from CACHE")
            showUpdateDialog = true
            isForceUpdate = cachedForce
            downloadUrl = cachedUrl
        }

        //Log.d("UPDATE_CHECK", "Firebase se data fetch kar rahe hain...")
        VersionUpdateHelper.getUpdateInfo { data ->
            if (data == null) return@getUpdateInfo

          //  Log.d("UPDATE_CHECK", "Firebase → version=${data.latestVersion}")

            // HAMESHA CACHE UPDATE KARO — ye line sabse important hai!


            SharedPrefHelper.saveVersionInfo(
                context = context,
                latestVersion = data.latestVersion,
                forceUpdate = data.forceUpdate,
                downloadUrl = data.downloadUrl
            )

            // Sirf naye version par hi dialog dikhao


            if (data.latestVersion > currentAppVersion) {
                showUpdateDialog = true
                isForceUpdate = data.forceUpdate
                downloadUrl = data.downloadUrl
            }
            // Agar same hai → kuch mat karo, cache already updated hai
        }
    }

// UPDATE DIALOG — Simple, Clean, Har Baar Dikhega



    if (showUpdateDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = {

                // Force update me back press se bhi band nahi hoga


                if (!isForceUpdate) showUpdateDialog = false
            },
            title = {
                Text(
                    text = "Update Required",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFABE0F),
                    fontSize = 20.sp,
                )
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Downloading,
                        contentDescription = null,
                        tint = Color(0xFFFABE0F),
                        modifier = Modifier.size(60.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "A new version of Sara777 is available!\nकृपया पहले ऐप अपडेट करे ।",
                        textAlign = TextAlign.Center,
                        fontSize = 16.sp
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val updateUrl = downloadUrl.trim()

                        // 1. Agar URL empty ya invalid hai → dialog band mat karo!


                        if (updateUrl.isEmpty()) {
                            showInvalidUrlDialog = true
                            return@TextButton
                        }
                        if (!updateUrl.startsWith("http://") && !updateUrl.startsWith("https://")) {
                            showInvalidUrlDialog = true
                            return@TextButton
                        }

                        // 2. Ab URL valid hai → tabhi dialog band karo

                        showUpdateDialog = false

                        // 3. Browser kholo


                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(updateUrl))
                        try {
                            context.startActivity(intent)
                        } catch (e: ActivityNotFoundException) {
                            showNoBrowserDialog = true
                        }

                        // 4. Force update hai aur URL se download start ho gaya → tab band karo


                        if (isForceUpdate) {
                            CoroutineScope(Dispatchers.Main).launch {
                                delay(2000) // user ko browser khulne ka time do
                                (context as? Activity)?.finishAffinity()
                            }
                        }
                    }
                ) {
                    Text(
                        "Update Now",
                        color = Color(0xFFFABE0F),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = if (!isForceUpdate) {
                {
                    TextButton(onClick = { showUpdateDialog = false }) {
                        Text(
                            "Skip",
                            color = Color.Red,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            } else null, // Force update me Later button hi nahi dikhega
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White
        )
    }


    // ─────── NO BROWSER DIALOG ───────


    if (showNoBrowserDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showNoBrowserDialog = false },
            title = { Text("No Browser Found") },
            text = { Text("Please install Chrome or any browser to open the update link.") },
            confirmButton = {
                TextButton(onClick = { showNoBrowserDialog = false }) {
                    Text("OK")
                }
            }
        )
    }

// ─────── INVALID URL DIALOG ───────


    if (showInvalidUrlDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = {
                showInvalidUrlDialog = false
                // Force update mein bhi band mat karo jab tak URL na ho!
                // Sirf message dikhao
            },
            title = { Text("Update Not Available", color = Color.Red) },
            text = {
                Text(
                    "New update is required but download link is missing.\n\n" +
                            "Please contact admin on WhatsApp.\n\n" +
                            "App will work normally until link is fixed.",
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                TextButton(onClick = { showInvalidUrlDialog = false }) {
                    Text("OK, Continue")
                }
            },
            dismissButton = null // OK ke alawa band na ho sake
        )
    }
// ─────────────────────────────────────────────────────────────────────────────────────────────


    LaunchedEffect(Unit) {
        walletViewModel.loadUserData(context)
    }


    BackHandler { (context as? Activity)?.finish() }

    val homeState by viewModel.homeState.collectAsState()
    val tickerState by tickerViewModel.state.collectAsState()


    val pullRefreshState = rememberPullRefreshState(
        refreshing = homeState.isLoading,
        onRefresh = { viewModel.refreshGames() }
    )



    Scaffold(
        topBar = { TopBar(navController, drawerState, scope, walletViewModel) },
        bottomBar = { BottomNavigationBar(navController) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            RedTicker(tickerState.adminMessage)
            FundCardsRow(navController)
            Spacer(modifier = Modifier.padding(vertical = 2.5.dp))
            WhatsAppRow(
                whatsapp1 = tickerState.whatsapp1,
                whatsapp2 = tickerState.whatsapp2
            )
            Spacer(modifier = Modifier.padding(vertical = 3.75.dp))


//            Box(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .weight(1f)
//                    .pullRefresh(pullRefreshState)           // YE LINE ADD HUI
//                    .verticalScroll(rememberScrollState())
//            ) {
//                if (homeState.games.isEmpty() && homeState.isLoading) {
//                    CircularProgressIndicator(
//                        color = Color(0xFFFABE0F),
//                        modifier = Modifier.align(Alignment.Center)
//                    )
//                } else if (homeState.error != null) {
//                    Text(
//                        text = homeState.error ?: "Something went wrong",
//                        color = Color.Red,
//                        fontWeight = FontWeight.Bold,
//                        textAlign = TextAlign.Center,
//                        modifier = Modifier.align(Alignment.Center).padding(16.dp)
//                    )
//                } else {
//                    GameCardsColumn(navController, homeState.games)
//
//
//                    // ✅ Lazy कंपोनेंट का उपयोग करें
//                   // LazyGameCardsList(navController, homeState.games)
//                }
//
//                // GOLDEN PULL TO REFRESH CIRCLE
//                PullRefreshIndicator(
//                    refreshing = homeState.isLoading,
//                    state = pullRefreshState,
//                    modifier = Modifier.align(Alignment.TopCenter),
//                    backgroundColor = Color(0xFFFABE0F),  // ← YE DIRECT DAAL DO
//                    contentColor = Color.White
//                )
//            }


            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .pullRefresh(pullRefreshState)
            ) {
                val listState = rememberLazyListState()

                // Jab bhi data aaye ya refresh ho → top pe scroll kar do
                LaunchedEffect(homeState.games) {
                    listState.scrollToItem(0)
                }

                // Agar games empty hain aur loading chal rahi hai → kuch mat dikhao center mein
                // Sirf PullRefreshIndicator dikhega top pe
                if (homeState.error != null) {
                    Text(
                        text = homeState.error ?: "Something went wrong",
                        color = Color.Red,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    )
                } else if (homeState.games.isEmpty() && !homeState.isLoading) {
                    // Jab data hi nahi hai (rare case)
                    Text(
                        text = "No games available",
                        fontSize = 18.sp,
                        color = Color.Gray,
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    // Normal case: List dikhao
                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        items(homeState.games, key = { it.id }) { game ->
                            GameCard(
                                navController = navController,
                                game = game,
                                viewModel = viewModel
                            )
                        }
                    }
                }

                // Ye hamesha dikhega jab refreshing hoga — top pe golden circle

                PullRefreshIndicator(
                    refreshing = homeState.isLoading,
                    state = pullRefreshState,
                    modifier = Modifier.align(Alignment.TopCenter),
                    backgroundColor = Color(0xFFFDFDFC),
                    contentColor = Color(0xFFFABE0F),
                    scale = true  // thoda bada dikhega, sundar lagega
                )
            }


        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    navController: NavController,
    drawerState: DrawerState,
    scope: CoroutineScope,
    walletViewModel: WalletViewModel
) {
    val context = LocalContext.current  // ← Yaha pe bahar nikaal diya
    val walletState by walletViewModel.walletState.collectAsState()

    // Ab LaunchedEffect mein context use kar sakte hain


//    LaunchedEffect(Unit) {
//        val userId = FirebaseAuth.getInstance().currentUser?.uid
//        if (userId != null) {
//            walletViewModel.loadUserData(context)  // Safe hai ab
//        }
//    }


//    LaunchedEffect(Unit) {
//        walletViewModel.loadUserData(context)
//    }


    TopAppBar(
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // Yellow Circle
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(color = Color(0xFFFABE0F), shape = CircleShape)
                )

                Row(
                    modifier = Modifier
                        .offset(x = (-38).dp)
                        .padding(start = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "S",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black)
                    Box {
                        Text(text = "ara",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black,
                            modifier = Modifier.align(Alignment.BottomCenter))
                        Box(modifier = Modifier.align(Alignment.TopCenter)
                            .offset(y = 4.dp)
                            .width(35.dp)
                            .height(3.dp)
                            .background(Color.Black, RoundedCornerShape(2.dp)))
                    }
                    Text(text = "777",
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.Black,
                        letterSpacing = (-1).sp,
                        modifier = Modifier.padding(start = 0.dp))
                }
            }
        },

        navigationIcon = {
            IconButton(onClick = { scope.launch {
                if (drawerState.isClosed) drawerState.open() else drawerState.close()
            }
            }) {
                Icon(painterResource(R.drawable.menu), "Menu",
                    tint = Color(0xFF333333),
                    modifier = Modifier.size(28.dp))
            }
        },

        actions = {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end = 2.dp)) {
                // Real-time Balance from Firebase

                    Text(
                        text = "₹${walletState.balance}",   // ← sirf balance dikhao, baaki hata do
                        color = Color(0xFF333333),
                        fontWeight = FontWeight.Normal,
                        fontSize = 17.sp,
                        letterSpacing =(-0.5).sp
                    )



                Spacer(modifier = Modifier.width(0.dp))

                IconButton(onClick = { navController.navigate("funds") }) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = "Notifications",
                        tint = Color(0xFF333333),
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        },

        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF9F9FC))
    )
}




@Composable
fun RedTicker(adminMessage: String) {
    val message = adminMessage.ifEmpty {
        "Welcome to Sara 777 App Latest Results Live Here Keep Playing & Enjoy Big Jackpots Everyday"
    }

    var textWidth by remember { mutableStateOf(0) }

    // YE LINE HATA DI — density wala bakwas band!
    // val density = LocalDensity.current

    val infiniteTransition = rememberInfiniteTransition(label = "ticker")
    val offsetX by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -(textWidth + 80f), // 80dp gap (fixed, no density)
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "tickerOffset"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFEEEEEE))
            .height(28.dp) // fixed height — padding hata diya
    ) {
        Row(
            modifier = Modifier
                .wrapContentWidth(unbounded = true) // yeh rakhna zaroori hai
                .offset { IntOffset(offsetX.roundToInt(), 0) }
        ) {
            Text(
                text = message,
                color = Color.Red,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp, // fixed 14sp — har phone pe same dikhega
                maxLines = 1,
                softWrap = false,
                modifier = Modifier.onGloballyPositioned {
                    textWidth = it.size.width
                }
            )
            Spacer(modifier = Modifier.width(100.dp)) // fixed 80dp gap
            Text(
                text = message,
                color = Color.Red,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp, // fixed
                maxLines = 1,
                softWrap = false
            )
        }
    }
}








//
//@Composable
//fun WhatsAppRow(whatsapp1: String, whatsapp2: String) {
//    val density = LocalDensity.current
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(horizontal = with(density) { 12.dp * density.density / 2 }, vertical = 1.dp),
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//        listOf(whatsapp1, whatsapp2).forEach { num ->
//            Row(
//                modifier = Modifier.weight(1f),
//                verticalAlignment = Alignment.CenterVertically,
//                horizontalArrangement = Arrangement.Center
//            ) {
//                Image(
//                    painter = painterResource(id = R.drawable.whatsapp),
//                    contentDescription = "WhatsApp",
//                    modifier = Modifier.size(with(density) { 22.dp * density.density / 2 })
//                )
//                Spacer(modifier = Modifier.width(with(density) { 6.dp * density.density / 2 }))
//                Text(
//                    text = if (num.isNotEmpty()) num else "+91",
//                    fontWeight = FontWeight.Bold,
//                    maxLines = 1,
//                    fontSize = with(density) { 14.sp * density.density / 2 }
//                )
//            }
//        }
//    }
//}






@Composable
fun WhatsAppRow(whatsapp1: String, whatsapp2: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 6.dp)
            .height(28.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        listOf(whatsapp1, whatsapp2).forEach { num ->
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {

                Icon(
                    imageVector = Icons.Filled.Whatsapp,   // Official Material 3 filled icon
                    contentDescription = "whatsapp",
                    tint = Color.Green,                     // Yellow background pe black best lagta hai
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = num.ifBlank { "+91" },
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}










//
//@Composable
//fun FundCardsRow(navController: NavController) {
//    val density = LocalDensity.current
//    val screenWidth = with(density) { density.density * 360.dp.value } // Approximate screen width in pixels
//    var textScaleFactor by remember { mutableStateOf(1f) }
//
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(horizontal = with(density) { 12.dp * density.density / 2 }),
//        horizontalArrangement = Arrangement.SpaceBetween
//    ) {
//        // Add Fund Card
//        Card(
//            modifier = Modifier
//                .weight(1f)
//                .height(with(density) { 50.dp * density.density / 2 })
//                .padding(end = with(density) { 6.dp * density.density / 2 })
//                .clickable { navController.navigate("add_fund") },
//            shape = RoundedCornerShape(with(density) { 40.dp * density.density / 2 }),
//            colors = CardDefaults.cardColors(containerColor = Color(0xFFFABE0F)),
//            elevation = CardDefaults.elevatedCardElevation(16.dp)
//        ) {
//            Row(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(with(density) { 12.dp * density.density / 2 }),
//                verticalAlignment = Alignment.CenterVertically,
//                horizontalArrangement = Arrangement.Center
//            ) {
//                Icon(
//                    painter = painterResource(id = R.drawable.rupee),
//                    contentDescription = "Add Fund",
//                    modifier = Modifier.size(with(density) { 24.dp * density.density / 2 }),
//                    tint = Color.White
//                )
//                Spacer(modifier = Modifier.width(with(density) { 8.dp * density.density / 2 }))
//                Text(
//                    text = "Add Fund",
//                    fontWeight = FontWeight.Bold,
//                    fontSize = with(density) {
//                        val scaledSize = 18f * density.density / 2 * textScaleFactor
//                        scaledSize.coerceAtMost(18f).sp // Convert to sp after coerceAtMost
//                    },
//                    color = Color.DarkGray,
//                    textAlign = TextAlign.Center,
//                    maxLines = 1,
//                    modifier = Modifier
//                        .wrapContentWidth()
//                        .onGloballyPositioned { coords ->
//                            // Calculate scale factor based on text width and available space
//                            val textWidth = coords.size.width.toFloat()
//                            val availableWidth = screenWidth / 2 - with(density) { (24.dp + 8.dp + 12.dp).toPx() }
//                            if (textWidth > availableWidth) {
//                                textScaleFactor = (availableWidth / textWidth).coerceAtMost(1f)
//                            }
//                        }
//                )
//            }
//        }
//
//        // Withdraw Fund Card
//        Card(
//            modifier = Modifier
//                .weight(1f)
//                .height(with(density) { 50.dp * density.density / 2 })
//                .padding(start = with(density) { 6.dp * density.density / 2 })
//                .clickable { navController.navigate("withdraw_fund") },
//            shape = RoundedCornerShape(with(density) { 40.dp * density.density / 2 }),
//            colors = CardDefaults.cardColors(containerColor = Color(0xFFFABE0F)),
//            elevation = CardDefaults.elevatedCardElevation(16.dp)
//        ) {
//            Row(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(with(density) { 12.dp * density.density / 2 }),
//                verticalAlignment = Alignment.CenterVertically,
//                horizontalArrangement = Arrangement.Center
//            ) {
//                Icon(
//                    painter = painterResource(id = R.drawable.account_balance_wallet),
//                    contentDescription = "Withdraw Fund",
//                    modifier = Modifier.size(with(density) { 24.dp * density.density / 2 }),
//                    tint = Color.White
//                )
//                Spacer(modifier = Modifier.width(with(density) { 8.dp * density.density / 2 }))
//                Text(
//                    text = "Withdraw Fund",
//                    fontWeight = FontWeight.Bold,
//                    fontSize = with(density) {
//                        val scaledSize = 18f * density.density / 2 * textScaleFactor
//                        scaledSize.coerceAtMost(18f).sp // Convert to sp after coerceAtMost
//                    },
//                    color = Color.DarkGray,
//                    textAlign = TextAlign.Center,
//                    maxLines = 1,
//                    modifier = Modifier
//                        .wrapContentWidth()
//                        .onGloballyPositioned { coords ->
//                            // Calculate scale factor based on text width and available space
//                            val textWidth = coords.size.width.toFloat()
//                            val availableWidth = screenWidth / 2 - with(density) { (24.dp + 8.dp + 12.dp).toPx() }
//                            if (textWidth > availableWidth) {
//                                textScaleFactor = (availableWidth / textWidth).coerceAtMost(1f)
//                            }
//                        }
//                )
//            }
//        }
//    }
//}


@Composable
fun FundCardsRow(navController: NavController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(11.dp)
    ) {

        // ===================== ADD FUND =====================
        Card(
            onClick = { navController.navigate("add_fund") },
            modifier = Modifier
                .weight(1f)
                .height(52.dp),
            shape = RoundedCornerShape(30.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFABE0F)),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(35.dp)                // ⬅️ Yeh background circle ka size
                        .background(Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.AddCard,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),  // Icon size
                        tint = Color.DarkGray
                    )
                }

                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = "Add Fund",
                    color = Color.DarkGray,
                    maxLines = 1,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // ===================== WITHDRAW FUND =====================
        Card(
            onClick = { navController.navigate("withdraw_fund") },
            modifier = Modifier
                .weight(1f)
                .height(52.dp),
            shape = RoundedCornerShape(30.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFABE0F)),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(35.dp)                // ⬅️ Yeh background circle ka size
                        .background(Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.CurrencyRupee,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),  // Icon size
                        tint = Color.DarkGray
                    )
                }
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = "Withdraw",
                    color = Color.DarkGray,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}













//unused function after lazt column


//@Composable
//fun GameCardsColumn(navController: NavController, games: List<firebase.Game>) {
//    val density = LocalDensity.current
//    Column(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(horizontal = with(density) { 12.dp * density.density / 2 })
//    ) {
//        if (games.isEmpty()) {
//            Text(
//                text = "Loading..",
//                fontWeight = FontWeight.Bold,
//                fontSize = with(density) { 16.sp * density.density / 2 },
//                color = Color.Green,
//                modifier = Modifier.fillMaxWidth(),
//                textAlign = TextAlign.Center
//            )
//        } else {
//            games.forEach { game ->
//                GameCard(navController, game ,HomeViewModel() )
//                Spacer(modifier = Modifier.padding(vertical = 1.5.dp))
//            }
//        }
//    }
//}


//@Composable
//fun LazyGameCardsList(navController: NavController, games: List<firebase.Game>) {
//    // Note: मैंने आपके पुराने कोड से density-आधारित जटिल padding को हटा दिया है।
//    // Compose में dp और sp ही उपयोग करना सबसे अच्छा अभ्यास है।
//    // आपके पुराने कोड की गणना के अनुसार 12.dp * density / 2 लगभग 6.dp होता है।
//
//    // LazyColumn का उपयोग करें जो verticalScroll को replace करेगा
//    LazyColumn(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(horizontal = 6.dp) // 12.dp * density / 2 के बजाय 6.dp उपयोग किया गया है
//            .padding(top = 1.dp) // थोड़ी ऊपर की padding ताकि PullRefresh Indicator से overlap न हो
//    ) {
//        if (games.isEmpty()) {
//            item {
//                Text(
//                    text = "Loading..",
//                    fontWeight = FontWeight.Bold,
//                    fontSize = 16.sp,
//                    color = Color.Green,
//                    modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
//                    textAlign = TextAlign.Center
//                )
//            }
//        } else {
//            // games.forEach की जगह items का उपयोग करें
//            items(games, key = { it.id }) { game ->
//                GameCard(navController, game)
//                // हर कार्ड के बाद Spacer
//                Spacer(modifier = Modifier.padding(vertical = 1.5.dp))
//            }
//        }
//    }
//}



















@Composable
fun GameCard(navController: NavController, game: firebase.Game , viewModel: HomeViewModel) {
    val sdf24 = java.text.SimpleDateFormat("HH:mm", Locale.getDefault())
    val sdf12 = java.text.SimpleDateFormat("hh:mm a", Locale.getDefault())
    val density = LocalDensity.current

    // Safe parsing for openTime (optimized - parse only once)
    val openFormatted = remember(game.openTime) {
        val openTimeStr = game.openTime.trim()
        if (openTimeStr.isNotEmpty()) {
            try {
                val parsed = sdf24.parse(openTimeStr)
                if (parsed != null) sdf12.format(parsed) else "N/A"
            } catch (e: ParseException) {
                "N/A" // Fallback display
            }
        } else {
            "N/A" // Fallback display
        }
    }

    // Safe parsing for closeTime (optimized - parse only once)
    val closeFormatted = remember(game.closeTime) {
        val closeTimeStr = game.closeTime.trim()
        if (closeTimeStr.isNotEmpty()) {
            try {
                val parsed = sdf24.parse(closeTimeStr)
                if (parsed != null) sdf12.format(parsed) else "N/A"
            } catch (e: ParseException) {
                "N/A" // Fallback display
            }
        } else {
            "N/A" // Fallback display
        }
    }

   val isCardActive = game.isCardActive // ViewModel calculates using server time


























    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 1.dp, vertical = 2.dp)
            .clickable(enabled = isCardActive) {
                val safeName = game.gameName.ifBlank { "Market" }
                navController.navigate("game_type/$safeName/${game.openTime}/${game.closeTime}")
            },
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(10.dp),
        border = BorderStroke(1.5.dp, Color(0xFFFFFFFE))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Game Name - Center & Safe
                Text(
                    text = game.gameName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF212121),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp)
                )

                // Result + Play Button Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Result: ${game.todayResult.ifEmpty { "***-**-***" }}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF57C00),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 12.dp) // play button se safe distance
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            painter = painterResource(id = R.drawable.play_circle),
                            contentDescription = "play",
                            tint = if (isCardActive) Color(0xFFFFC107) else Color.Gray,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = if (!isCardActive) "Game Closed" else "Play Game  ",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (!isCardActive) Color.Red else Color(0xFF4CAF50),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Open & Close Bids - Super Safe
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Open: $openFormatted",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF212121),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = "Close: $closeFormatted",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF212121),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        containerColor = Color(0xFFE8E8E8),
        tonalElevation = 28.dp
    ) {
        val items = listOf(
            BottomItem("Bids", "bids", Icons.Outlined.Gavel),
            BottomItem("Rate", "game_rate", Icons.Outlined.CurrencyRupee),
            BottomItem("Home", "home", Icons.Filled.Home),
            BottomItem("Funds", "funds", Icons.Outlined.AccountBalance),
            BottomItem("Support", "support", Icons.Outlined.HeadsetMic)
        )

        items.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = {
                    // YE SABSE BADA CHANGE — AGAR WAHI PAGE PE HAI TO NAVIGATE MAT KARO
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    }
                },
                icon = {
                    if (item.route == "home" && currentRoute == "home") {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .background(color = Color(0xFFFABE0F), shape = CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Home, "Home", tint = Color.White, modifier = Modifier.size(28.dp))
                        }
                    } else if (item.route == "home") {
                        Icon(Icons.Outlined.Home, "Home", modifier = Modifier.size(20.dp))
                    } else {
                        Icon(item.icon, item.label, modifier = Modifier.size(26.dp))
                    }
                },
                label = {
                    Text(
                        text = item.label,
                        fontSize = 10.sp,
                        fontWeight = if (currentRoute == item.route) FontWeight.Bold else FontWeight.Medium
                    )
                },
                alwaysShowLabel = true,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = if (item.route == "home") Color.White else Color(0xFFFABE0F),
                    selectedTextColor = Color(0xFFFABE0F),
                    unselectedIconColor = Color(0xFF666666),
                    unselectedTextColor = Color(0xFF666666),
                    indicatorColor = if (item.route == "home") Color.Transparent else Color(0xFFFFF3C4)
                )
            )
        }
    }
}

// Same data class
data class BottomItem(val label: String, val route: String, val icon: ImageVector)
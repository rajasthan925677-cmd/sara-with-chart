package navigation

import android.app.Activity
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import screens.AddFundHistoryScreen
import screens.AddFundScreen
import screens.BankDetailsScreen
import screens.BidHistoryScreen
import screens.ChartGameListScreen
import screens.DoublePanaScreen
import screens.ForgotPasswordPage
import screens.FullSangamScreen
import screens.FundScreen
import screens.GameRatePage
import screens.GameTypePage
import screens.HalfSangamScreen
import screens.HomePage
import screens.JodiScreen
import screens.LoginPage
import screens.NavigationDrawer
import screens.PanaChartScreen
import screens.QRPayHistoryScreen
import screens.SignupPage
import screens.SingleAnkScreen
import screens.SinglePannaScreen
import screens.SupportPage
import screens.TriplePanaScreen
import screens.WithdrawFundHistoryScreen
import screens.WithdrawFundScreen
import viewmodal.AuthViewModel
import viewmodal.HomeViewModel
import viewmodal.WalletViewModel
import viewmodel.BidViewModel
import java.lang.reflect.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import viewmodal.LocalDrawerState

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: String // Added startDestination parameter
) {

    // YE GLOBAL STATE LE LO — SIRF EK BAAR BANEGA!
    val drawerState = LocalDrawerState.current
    val scope = rememberCoroutineScope()
    val context = LocalContext.current



    // Shared ViewModels for consistent lifecycle management
    val walletViewModel: WalletViewModel = viewModel()
    val bidViewModel: BidViewModel = viewModel()

    NavHost(navController = navController, startDestination = startDestination) {
        // -------- Auth Screens --------
        composable(Screen.SignInPage.route) {
            val authViewModel: AuthViewModel = viewModel()
            LoginPage(navController = navController, viewModel = authViewModel)
        }

        composable(Screen.SignUpPage.route) {
            val authViewModel: AuthViewModel = viewModel()
            SignupPage(navController = navController, viewModel = authViewModel)
        }

        composable(Screen.ForgetPasswordPage.route) {
            val authViewModel: AuthViewModel = viewModel()
            ForgotPasswordPage(navController = navController, viewModel = authViewModel)
        }

       //  -------- Home Page --------


        composable(Screen.HomePage.route) {
            val activity = (context as? Activity)
            BackHandler { activity?.finish() }

            val homeViewModel: HomeViewModel = viewModel()

            NavigationDrawer(
                navController = navController,
                shareLink = "https://example.com",
               // drawerState = drawerState,
               // scope = scope
            ) {
                HomePage(
                    navController = navController,
                  //  drawerState = drawerState,
                   // scope = scope,
                    viewModel = homeViewModel,
                    walletViewModel = walletViewModel
                )
            }
         }



        // -------- Funds & Transactions --------
        composable(Screen.FundsPage.route) {
            FundScreen(navController = navController, walletViewModel = walletViewModel)
        }
        composable(Screen.AddFundPage.route) {
            AddFundScreen(
                navController = navController,
                walletViewModel = walletViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.WithdrawFundPage.route) {
            WithdrawFundScreen(
                navController = navController,
                walletViewModel = walletViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.WithdrawHistoryPage.route) {
            WithdrawFundHistoryScreen(navController = navController)
        }
        composable(Screen.AddHistoryPage.route) {
            AddFundHistoryScreen(navController = navController)
        }
        composable(Screen.BankDetailPage.route) {
            BankDetailsScreen(navController = navController)
        }
        composable("qr_pay_history") {
            QRPayHistoryScreen(navController = navController)
        }

        // -------- Bid History --------
        composable(Screen.BidsPage.route) {
            val homeViewModel: HomeViewModel = viewModel()
            BidHistoryScreen(
                navController = navController,
                bidViewModel = bidViewModel,
                homeViewModel = homeViewModel
            )
        }

        // -------- Game Screens --------
        composable(Screen.GameRatePage.route) {
            GameRatePage(navController = navController)
        }

        // Game Type
        composable(
            route = "game_type/{marketName}/{openTime}/{closeTime}",
            arguments = listOf(
                navArgument("marketName") { type = NavType.StringType },
                navArgument("openTime") { type = NavType.StringType },
                navArgument("closeTime") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val marketName = backStackEntry.arguments?.getString("marketName") ?: ""
            val openTime = backStackEntry.arguments?.getString("openTime") ?: ""
            val closeTime = backStackEntry.arguments?.getString("closeTime") ?: ""

            GameTypePage(
                navController = navController,
                marketName = marketName,
                openTime = openTime,
                closeTime = closeTime,
                walletViewModel = walletViewModel
            )
        }

        // -------- Single Ank --------
        composable(
            route = "single_ank/{marketName}/{gameType}/{openTime}/{closeTime}",
            arguments = listOf(
                navArgument("marketName") { type = NavType.StringType },
                navArgument("gameType") { type = NavType.StringType },
                navArgument("openTime") { type = NavType.StringType },
                navArgument("closeTime") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val marketName = backStackEntry.arguments?.getString("marketName") ?: "SARA777"
            val gameType = backStackEntry.arguments?.getString("gameType") ?: "Single Ank"
            val openTime = backStackEntry.arguments?.getString("openTime") ?: ""
            val closeTime = backStackEntry.arguments?.getString("closeTime") ?: ""

            SingleAnkScreen(
                navController = navController,
                viewModel = bidViewModel,
                walletViewModel = walletViewModel,
                marketName = marketName,
                gameType = gameType,
                openTime = openTime,
                closeTime = closeTime
            )
        }

        // -------- Jodi --------
        composable(
            route = "jodi/{marketName}/{gameType}/{openTime}/{closeTime}",
            arguments = listOf(
                navArgument("marketName") { type = NavType.StringType },
                navArgument("gameType") { type = NavType.StringType },
                navArgument("openTime") { type = NavType.StringType },
                navArgument("closeTime") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val marketName = backStackEntry.arguments?.getString("marketName") ?: ""
            val gameType = backStackEntry.arguments?.getString("gameType") ?: "Jodi"
            val openTime = backStackEntry.arguments?.getString("openTime") ?: ""
            val closeTime = backStackEntry.arguments?.getString("closeTime") ?: ""

            JodiScreen(
                navController = navController,
                marketName = marketName,
                gameType = gameType,
                openTime = openTime,
                closeTime = closeTime,
                viewModel = bidViewModel,
                walletViewModel = walletViewModel
            )
        }

        // -------- Single Pana --------
        composable(
            route = "single_pana/{marketName}/{gameType}/{openTime}/{closeTime}",
            arguments = listOf(
                navArgument("marketName") { type = NavType.StringType },
                navArgument("gameType") { type = NavType.StringType },
                navArgument("openTime") { type = NavType.StringType },
                navArgument("closeTime") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val marketName = backStackEntry.arguments?.getString("marketName") ?: "MAIN BAZAAR"
            val gameType = backStackEntry.arguments?.getString("gameType") ?: "Single Pana"
            val openTime = backStackEntry.arguments?.getString("openTime") ?: ""
            val closeTime = backStackEntry.arguments?.getString("closeTime") ?: ""

            SinglePannaScreen(
                navController = navController,
                marketName = marketName,
                gameType = gameType,
                openTime = openTime,
                closeTime = closeTime,
                viewModel = bidViewModel,
                walletViewModel = walletViewModel
            )
        }

        // -------- Double Pana --------
        composable(
            route = "double_pana/{marketName}/{gameType}/{openTime}/{closeTime}",
            arguments = listOf(
                navArgument("marketName") { type = NavType.StringType },
                navArgument("gameType") { type = NavType.StringType },
                navArgument("openTime") { type = NavType.StringType },
                navArgument("closeTime") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val marketName = backStackEntry.arguments?.getString("marketName") ?: "MAIN BAZAAR"
            val gameType = backStackEntry.arguments?.getString("gameType") ?: "Double Pana"
            val openTime = backStackEntry.arguments?.getString("openTime") ?: ""
            val closeTime = backStackEntry.arguments?.getString("closeTime") ?: ""

            DoublePanaScreen(
                navController = navController,
                marketName = marketName,
                gameType = gameType,
                openTime = openTime,
                closeTime = closeTime,
                bidViewModel = bidViewModel,
                walletViewModel = walletViewModel
            )
        }

        // -------- Triple Pana --------
        composable(
            route = "triple_pana/{marketName}/{gameType}/{openTime}/{closeTime}",
            arguments = listOf(
                navArgument("marketName") { type = NavType.StringType },
                navArgument("gameType") { type = NavType.StringType },
                navArgument("openTime") { type = NavType.StringType },
                navArgument("closeTime") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val marketName = backStackEntry.arguments?.getString("marketName") ?: "MAIN BAZAAR"
            val gameType = backStackEntry.arguments?.getString("gameType") ?: "Triple Pana"
            val openTime = backStackEntry.arguments?.getString("openTime") ?: ""
            val closeTime = backStackEntry.arguments?.getString("closeTime") ?: ""

            TriplePanaScreen(
                navController = navController,
                marketName = marketName,
                gameType = gameType,
                openTime = openTime,
                closeTime = closeTime,
                bidViewModel = bidViewModel,
                walletViewModel = walletViewModel
            )
        }

        // -------- Half Sangam --------
        composable(
            route = "half_sangam/{marketName}/{gameType}/{openTime}/{closeTime}",
            arguments = listOf(
                navArgument("marketName") { type = NavType.StringType },
                navArgument("gameType") { type = NavType.StringType },
                navArgument("openTime") { type = NavType.StringType },
                navArgument("closeTime") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val marketName = backStackEntry.arguments?.getString("marketName") ?: "MAIN BAZAAR"
            val gameType = backStackEntry.arguments?.getString("gameType") ?: "Half Sangam"
            val openTime = backStackEntry.arguments?.getString("openTime") ?: ""
            val closeTime = backStackEntry.arguments?.getString("closeTime") ?: ""

            HalfSangamScreen(
                navController = navController,
                marketName = marketName,
                gameType = gameType,
                openTime = openTime,
                closeTime = closeTime,
                bidViewModel = bidViewModel,
                walletViewModel = walletViewModel
            )
        }

        // -------- Full Sangam --------
        composable(
            route = "full_sangam/{marketName}/{gameType}/{openTime}/{closeTime}",
            arguments = listOf(
                navArgument("marketName") { type = NavType.StringType },
                navArgument("gameType") { type = NavType.StringType },
                navArgument("openTime") { type = NavType.StringType },
                navArgument("closeTime") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val marketName = backStackEntry.arguments?.getString("marketName") ?: "MAIN BAZAAR"
            val gameType = backStackEntry.arguments?.getString("gameType") ?: "Full Sangam"
            val openTime = backStackEntry.arguments?.getString("openTime") ?: ""
            val closeTime = backStackEntry.arguments?.getString("closeTime") ?: ""

            FullSangamScreen(
                navController = navController,
                marketName = marketName,
                gameType = gameType,
                openTime = openTime,
                closeTime = closeTime,
                bidViewModel = bidViewModel,
                walletViewModel = walletViewModel
            )
        }

        // -------- Support --------
        composable(Screen.SupportPage.route) {
            SupportPage(navController = navController)
        }



        composable("chart") {
            ChartGameListScreen(navController = navController)
        }


        composable(
            route = "panel_chart/{gameName}",
            arguments = listOf(navArgument("gameName") { type = NavType.StringType })
        ) { backStackEntry ->
            val gameName = backStackEntry.arguments?.getString("gameName") ?: return@composable
            PanaChartScreen(
                navController = navController,
                gameName = gameName
            )
        }




    }
}
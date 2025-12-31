package navigation


sealed class Screen(val route: String) {
    object SignInPage : Screen("signin")
    object SignUpPage : Screen("signup")
    object ForgetPasswordPage : Screen("forget_password")

    object HomePage : Screen("home")
    object FundsPage : Screen("funds")
    object AddFundPage : Screen("add_fund")
    object WithdrawFundPage : Screen("withdraw_fund")
    object WithdrawHistoryPage : Screen("withdraw_history")
    object AddHistoryPage : Screen("add_history")
    object BankDetailPage : Screen("bank_detail")

    object BidsPage : Screen("bids")
    object GameRatePage : Screen("game_rate")
    object GameTypePage : Screen("game_type")
    object SingleAnkPage : Screen("single_ank")
    object SinglePanaPage : Screen("single_pana")
    object DoublePanaPage : Screen("double_pana")
    object TriplePanaPage : Screen("triple_pana")
    object JodiPage : Screen("jodi")
    object HalfSangamPage : Screen("half_sangam")
    object FullSangamPage : Screen("full_sangam")

    object SupportPage : Screen("support")
    object SideDrawerPage : Screen("side_drawer") // agar drawer ko alag screen rakhna ho
}

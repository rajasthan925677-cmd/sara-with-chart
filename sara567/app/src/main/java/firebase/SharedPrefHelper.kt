package firebase

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

object SharedPrefHelper {

    private const val PREF_NAME = "MyAppPref"
    private const val KEY_IS_LOGGED_IN = "isLoggedIn"
    private const val KEY_MOBILE = "loggedInMobile"
    private const val KEY_UID = "loggedInUID" // Added for UID storage
    private const val KEY_USERNAME = "loggedInUsername"
    private const val KEY_BALANCE = "userBalance"

    fun setLoggedIn(context: Context, mobile: String, uid: String, username: String? = null) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .putString(KEY_MOBILE, mobile)
            .putString(KEY_UID, uid) // Store UID
            .putString(KEY_USERNAME, username)
            .apply()
    }

    fun isLoggedIn(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun getMobile(context: Context): String? {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_MOBILE, null)
    }

    fun getUID(context: Context): String? {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_UID, null)
    }

    fun getUsername(context: Context): String? {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_USERNAME, null)
    }

    fun setBalance(context: Context, balance: Long) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putLong(KEY_BALANCE, balance).apply()
    }

    fun getBalance(context: Context): Long {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getLong(KEY_BALANCE, 0L)
    }

    fun logout(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }









    // ================== TICKER DATA CACHING ==================
    private const val KEY_ADMIN_MESSAGE = "ticker_admin_message"
    private const val KEY_WHATSAPP_1     = "ticker_whatsapp_1"
    private const val KEY_WHATSAPP_2     = "ticker_whatsapp_2"

    fun saveTickerData(
        context: Context,
        adminMessage: String,
        whatsapp1: String,
        whatsapp2: String
    ) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_ADMIN_MESSAGE, adminMessage)
            .putString(KEY_WHATSAPP_1, whatsapp1)
            .putString(KEY_WHATSAPP_2, whatsapp2)
            .apply()
    }

    fun getSavedTickerData(context: Context): Triple<String, String, String> {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val msg = prefs.getString(KEY_ADMIN_MESSAGE, "") ?: ""
        val w1  = prefs.getString(KEY_WHATSAPP_1, "") ?: ""
        val w2  = prefs.getString(KEY_WHATSAPP_2, "") ?: ""
        return Triple(msg, w1, w2)
    }
    // =========================================================

    // ================== VERSION UPDATE CACHING ==================
    private const val KEY_LATEST_VERSION = "latestVersion"
    private const val KEY_FORCE_UPDATE   = "forceUpdate"
    private const val KEY_DOWNLOAD_URL   = "downloadUrl"



    fun saveVersionInfo(
        context: Context,
        latestVersion: Int,
        forceUpdate: Boolean,
        downloadUrl: String
    ) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putInt(KEY_LATEST_VERSION, latestVersion)
            .putBoolean(KEY_FORCE_UPDATE, forceUpdate)
            .putString(KEY_DOWNLOAD_URL, downloadUrl)
            .apply()
    }

    fun getSavedVersionInfo(context: Context): Triple<Int, Boolean, String> {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val version = prefs.getInt(KEY_LATEST_VERSION, 1)
        val force   = prefs.getBoolean(KEY_FORCE_UPDATE, false)
        val url     = prefs.getString(KEY_DOWNLOAD_URL, "") ?: ""
        return Triple(version, force, url)
    }
// ============================================================


// SharedPrefHelper.kt (teri purani file mein hi add kar dena)

    private const val KEY_SHARE_LINK = "cached_share_link"
    private const val DEFAULT_SHARE_LINK = "https://sara777.com"

    fun getShareLink(context: Context): String {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_SHARE_LINK, DEFAULT_SHARE_LINK) ?: DEFAULT_SHARE_LINK
    }

    fun saveShareLink(context: Context, link: String) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_SHARE_LINK, link).apply()  // apply() = async & safe
    }

    // YE HAI JAADU — Background mein silently update karega
    fun updateShareLinkFromFirebase(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val newLink = FirebaseFirestore.getInstance()
                    .collection("appInfo")
                    .document("share")
                    .get()
                    .await()
                    .getString("link") ?: DEFAULT_SHARE_LINK

                // Sirf tab save karo jab change hua ho
                if (newLink != getShareLink(context)) {
                    saveShareLink(context, newLink)
                }
            } catch (e: Exception) {
                // Fail ho gaya? Koi baat nahi — purana cached link chalega
            }
        }
    }










    // Add to existing SharedPrefHelper.kt
    fun setBankDetails(context: Context, upiId: String, bankName: String, accountNo: String, ifsc: String, holder: String) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString("upiId", upiId)
            .putString("bankName", bankName)
            .putString("accountNo", accountNo)
            .putString("ifsc", ifsc)
            .putString("holder", holder)
            .apply()
    }

    fun getBankDetails(context: Context): Map<String, String> {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return mapOf(
            "upiId" to (prefs.getString("upiId", "") ?: ""),
            "bankName" to (prefs.getString("bankName", "") ?: ""),
            "accountNo" to (prefs.getString("accountNo", "") ?: ""),
            "ifsc" to (prefs.getString("ifsc", "") ?: ""),
            "holder" to (prefs.getString("holder", "") ?: "")
        )
    }
}
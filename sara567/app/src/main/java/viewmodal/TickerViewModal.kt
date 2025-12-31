//package viewmodal

//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.google.firebase.firestore.IgnoreExtraProperties
//import firebase.FirebaseTickerHelper
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.flow.catch
//import kotlinx.coroutines.launch
//
//@IgnoreExtraProperties
//data class HomeTickerState(
//    var adminMessage: String = "Welcome to Sara 777.",
//    var whatsapp1: String = "",
//    var whatsapp2: String = "",
//    val isLoading: Boolean = true,
//    val error: String? = null
//)
//
//class HomeTickerViewModel(
//    private val firebaseHelper: FirebaseTickerHelper = FirebaseTickerHelper()
//) : ViewModel() {
//
//    private val _state = MutableStateFlow(HomeTickerState())
//    val state: StateFlow<HomeTickerState> = _state
//
//    init {
//        loadTicker()
//    }
//
//    private fun loadTicker() {
//        viewModelScope.launch {
//            firebaseHelper.getHomeTicker()
//                .catch { e ->
//                 //   Log.e("HomeTickerViewModel", "Error fetching ticker: ${e.message}")
//                    _state.value = _state.value.copy(
//                        isLoading = false,
//                        error = "Failed to load ticker data: ${e.message}"
//                    )
//                }
//                .collect { ticker ->
//                    _state.value = HomeTickerState(
//                        adminMessage = ticker.adminMessage,
//                        whatsapp1 = ticker.whatsapp1,
//                        whatsapp2 = ticker.whatsapp2,
//                        isLoading = false,
//                        error = null
//                    )
//                }
//        }
//    }
//}










package viewmodal

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import firebase.FirebaseTickerHelper
import firebase.SharedPrefHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

data class HomeTickerState(
    val adminMessage: String = "Welcome to Sara 777.",
    val whatsapp1: String = "",
    val whatsapp2: String = "",
    val isLoading: Boolean = false,   // अब loading false ही रखेंगे शुरू में
    val error: String? = null
)

class HomeTickerViewModel(
    private val context: Context,                       // ← नया: context चाहिए
    private val firebaseHelper: FirebaseTickerHelper = FirebaseTickerHelper()
) : ViewModel() {

    private val _state = MutableStateFlow(HomeTickerState())
    val state: StateFlow<HomeTickerState> = _state

    init {
        // 1. सबसे पहले SharedPreferences से पुराना डेटा दिखा दो (instant UI)
        val (savedMsg, savedW1, savedW2) = SharedPrefHelper.getSavedTickerData(context)
        if (savedMsg.isNotEmpty()) {
            _state.value = HomeTickerState(
                adminMessage = savedMsg,
                whatsapp1 = savedW1,
                whatsapp2 = savedW2,
                isLoading = false
            )
        }

        // 2. बैकग्राउंड में Firebase से real-time sync शुरू करो
        startFirebaseSync()
    }

    private fun startFirebaseSync() {
        viewModelScope.launch {
            firebaseHelper.getHomeTicker()
                .catch { e ->
                    // अगर नेटवर्क नहीं है तो भी पुराना डेटा दिखता रहेगा
                }
                .collect { ticker ->
                    // नया डेटा आया → SharedPref में save करो + UI update करो
                    SharedPrefHelper.saveTickerData(
                        context = context,
                        adminMessage = ticker.adminMessage,
                        whatsapp1 = ticker.whatsapp1,
                        whatsapp2 = ticker.whatsapp2
                    )

                    _state.value = HomeTickerState(
                        adminMessage = ticker.adminMessage,
                        whatsapp1 = ticker.whatsapp1,
                        whatsapp2 = ticker.whatsapp2,
                        isLoading = false
                    )
                }
        }
    }
}














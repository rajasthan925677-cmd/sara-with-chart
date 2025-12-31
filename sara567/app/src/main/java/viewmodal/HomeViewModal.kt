package viewmodal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import firebase.FirebaseHelper
import firebase.Game
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class HomeState(
    val games: List<Game> = emptyList(),
    val isLoading: Boolean = false,
    val message: String? = null,
    val error: String? = null
)

class HomeViewModel : ViewModel() {
    private val firebaseHelper = FirebaseHelper()
    private val _homeState = MutableStateFlow(HomeState())
    val homeState: StateFlow<HomeState> = _homeState
    private val lastActiveStates = mutableMapOf<String, Boolean>()
    private val _serverTime = MutableStateFlow<Date?>(null)
    private var serverTimeJob: Job? = null
    private var initialLoadDone = false

    init {

        _homeState.value = HomeState(isLoading = true)


        viewModelScope.launch {
            try {
                firebaseHelper.ensureServerTimeDoc()
                monitorServerTime()
                listenToGames()
            } catch (e: Exception) {
                _homeState.value = _homeState.value.copy(
                    isLoading = false,
                    error = "Failed to initialize data: ${e.message}"
                )
            }
        }
    }

    private suspend fun monitorServerTime() {
        serverTimeJob?.cancel()
        serverTimeJob = viewModelScope.launch(Dispatchers.IO) {
            while (isActive) {
                try {
                    val serverTime = firebaseHelper.getServerTime()
                    withContext(Dispatchers.Main) {
                        _serverTime.value = serverTime
                    }
                } catch (e: Exception) {
                    if (e.message?.contains("PERMISSION_DENIED") == true) {
                        serverTimeJob?.cancel()
                        return@launch
                    }
                    withContext(Dispatchers.Main) {
                        _serverTime.value = Date()
                    }
                }
                delay(30000)
            }
        }
    }

    private fun listenToGames() {
//        if (!initialLoadDone) {
//            _homeState.value = HomeState(isLoading = true)
//        }

        firebaseHelper.listenGames { games ->
            viewModelScope.launch(Dispatchers.Default) {
                val serverTime = _serverTime.value ?: Date()

                //val serverTime = _serverTime.value ?: return@launch


                val updatedGames = games.map { game ->
                    val isActive = checkCardActive(game, serverTime)
                    // Log to verify data
                   // println("Updating game: ${game.gameName}, isActive: $isActive, enabled: ${game.enabled} , isEditing :${game.isEditing}")
//                    if (lastActiveStates[game.id] == false && isActive) {
//                        firebaseHelper.resetResults(game.id) { success ->
//                            if (!success) {
//                                _homeState.value = _homeState.value.copy(error = "Failed to reset results")
//                            }
//                        }
//                    }
                    lastActiveStates[game.id] = isActive
                    game.copy(isCardActive = isActive)
                }

                withContext(Dispatchers.Main) {
                    _homeState.value = HomeState(
                        games = updatedGames,
                        isLoading = false,
                        message = if (updatedGames.isEmpty()) "No games found!" else null,
                        error = null
                    )
                    initialLoadDone = true
                }
            }
        }
    }





    private fun checkCardActive(game: Game, serverTime: Date?): Boolean {

        if (serverTime == null) return false

        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())

        val openTimeStr = game.openTime.trim()
        val openCal = if (openTimeStr.isNotEmpty()) {
            try {
                Calendar.getInstance().apply {
                    time = sdf.parse(openTimeStr) ?: Date()
                }
            } catch (e: ParseException) {
                Calendar.getInstance().apply { time = serverTime }
            }
        } else {
            Calendar.getInstance().apply { time = serverTime }
        }

        val closeTimeStr = game.closeTime.trim()
        val closeCal = if (closeTimeStr.isNotEmpty()) {
            try {
                Calendar.getInstance().apply {
                    time = sdf.parse(closeTimeStr) ?: Date()
                }
            } catch (e: ParseException) {
                Calendar.getInstance().apply {
                    time = serverTime
                    add(Calendar.HOUR_OF_DAY, 1)
                }
            }
        } else {
            Calendar.getInstance().apply {
                time = serverTime
                add(Calendar.HOUR_OF_DAY, 1)
            }
        }

        val todayCal = Calendar.getInstance().apply { time = serverTime }
        openCal.set(Calendar.YEAR, todayCal.get(Calendar.YEAR))
        openCal.set(Calendar.MONTH, todayCal.get(Calendar.MONTH))
        openCal.set(Calendar.DAY_OF_MONTH, todayCal.get(Calendar.DAY_OF_MONTH))

        closeCal.set(Calendar.YEAR, todayCal.get(Calendar.YEAR))
        closeCal.set(Calendar.MONTH, todayCal.get(Calendar.MONTH))
        closeCal.set(Calendar.DAY_OF_MONTH, todayCal.get(Calendar.DAY_OF_MONTH))

        openCal.add(Calendar.HOUR_OF_DAY, -12)

        val timeActive = serverTime.after(openCal.time) && serverTime.before(closeCal.time)

        // Log to debug
       // println("Game: ${game.gameName}, timeActive: $timeActive, enabled: ${game.enabled}, isCardActive: ${timeActive && game.enabled} , isEditing :${game.isEditing}")

        return timeActive && game.enabled  // Changed to game.enabled
    }



//
//    // HomeViewModel.kt (नया फ़ंक्शन - parseTimeToMinutes के तुरंत बाद पेस्ट करें)
//    private fun checkCardActive(game: Game, serverTime: Date): Boolean {
//        // 1. Basic Check
//        if (!game.enabled) return false
//
//        // 2. सर्वर समय को दिन के मिनटों में प्राप्त करें (सबसे हल्का तरीका)
//        val nowCal = Calendar.getInstance().apply { time = serverTime }
//        // सर्वर टाइम के घंटे और मिनट को दिन के कुल मिनटों में बदलें
//        val nowMinuteOfDay = nowCal.get(Calendar.HOUR_OF_DAY) * 60 + nowCal.get(Calendar.MINUTE)
//
//        // 3. ओपन और क्लोज टाइम को दिन के मिनटों में बदलें
//        val openMinuteOfDay = parseTimeToMinutes(game.openTime)
//        val closeMinuteOfDay = parseTimeToMinutes(game.closeTime)
//
//        // 4. पुराने लॉजिक का अनुकरण: openCal.add(Calendar.HOUR_OF_DAY, -12)
//        // 12 घंटे = 720 मिनट
//        var activeStartMinute = openMinuteOfDay - (12 * 60)
//
//        // यदि activeStartMinute नेगेटिव हो जाता है, तो यह पिछले दिन का समय दिखाता है (रात भर चलने वाले गेम के लिए आवश्यक)
//        if (activeStartMinute < 0) {
//            activeStartMinute += (24 * 60) // 1440 मिनट जोड़ें
//        }
//
//        // 5. एक्टिव स्टेटस की गणना (रात भर चलने वाले गेम को संभालना)
//        val timeActiveCondition: Boolean
//
//        // यह चेक करता है कि एक्टिव स्टार्ट टाइम क्लोज टाइम से बड़ा है या नहीं (जैसे रात 10:00 से सुबह 4:00)
//        if (activeStartMinute > closeMinuteOfDay) {
//            // रात भर वाला गेम (Previous Day Open / Current Day Close)
//            // Active है यदि समय Active Start के बाद हो (आज) OR Close Minute से पहले हो (अगले दिन)
//            timeActiveCondition = nowMinuteOfDay >= activeStartMinute || nowMinuteOfDay < closeMinuteOfDay
//        } else {
//            // सामान्य मामला (दिन के भीतर)
//            // Active है यदि समय Active Start और Close Minute के बीच हो
//            timeActiveCondition = nowMinuteOfDay >= activeStartMinute && nowMinuteOfDay < closeMinuteOfDay
//        }
//
//        // Log to debug (Optional: आप इसे हटा सकते हैं या बनाए रख सकते हैं)
//        println("Game: ${game.gameName}, Now: $nowMinuteOfDay, Start: $activeStartMinute, Close: $closeMinuteOfDay, Active: $timeActiveCondition")
//
//        // 6. अंतिम परिणाम
//        return timeActiveCondition && game.enabled
//    }
//
//
//
//
//
//// HomeViewModel.kt (नया फ़ंक्शन - कहीं भी पेस्ट करें जहां पुराना checkCardActive था)
//    /**
//     * Parses a time string (HH:mm) into minutes elapsed since midnight (00:00).
//     */
//    private fun parseTimeToMinutes(timeStr: String): Int {
//        val parts = timeStr.trim().split(":")
//        return try {
//            if (parts.size == 2) {
//                // Hours (0-23) * 60 + Minutes
//                parts[0].toInt() * 60 + parts[1].toInt()
//            } else {
//                0
//            }
//        } catch (e: NumberFormatException) {
//            0
//        }
//    }






    fun refreshGames() {
        // Step 1: Loading dikhao
        _homeState.value = _homeState.value.copy(isLoading = true)

        // Step 2: Purana listener hatao (important for fresh data)
        firebaseHelper.stopListening()

        // Step 3: Thoda delay (UI ke liye smooth feel)
        viewModelScope.launch {
            delay(600) // indicator dikhne ka time milega

            // Step 4: Dubara listener lagao → fresh data aayega Firebase se
            listenToGames()

            // Optional: Agar server time bhi refresh karna hai
            monitorServerTime()
        }
    }







    fun updateGame(gameId: String, updatedGame: Game) {
        firebaseHelper.updateGame(gameId, updatedGame) { success ->
            if (!success) {
                _homeState.value = _homeState.value.copy(error = "Failed to update game")
            }
        }
    }

    fun addGame(game: Game) {
        firebaseHelper.addGame(game) { success ->
            if (!success) {
                _homeState.value = _homeState.value.copy(error = "Failed to add game")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        firebaseHelper.stopListening()
        serverTimeJob?.cancel()
    }
}
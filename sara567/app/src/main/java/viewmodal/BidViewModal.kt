package viewmodel

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import firebase.BidModel
import firebase.FirebaseBidHelper
import firebase.SharedPrefHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import viewmodal.WalletViewModel
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import java.util.UUID

data class BidState(
    val bids: List<BidModel> = emptyList(),
    val isLoading: Boolean = false,
    val message: String? = null
)

class BidViewModel : ViewModel() {
    private val helper = FirebaseBidHelper()
    private val db = FirebaseFirestore.getInstance()
    private val _bidState = MutableStateFlow(BidState())
    val bidState: StateFlow<BidState> = _bidState
    private val _filteredBids = MutableStateFlow<List<BidModel>>(emptyList())
    val filteredBids: StateFlow<List<BidModel>> = _filteredBids
    private val _serverTime = MutableStateFlow<Date?>(null)
    val serverTime: StateFlow<Date?> = _serverTime
    private var serverTimeJob: Job? = null

    init {
        viewModelScope.launch { monitorServerTime() }
    }

    private suspend fun monitorServerTime() {
        serverTimeJob?.cancel()
        serverTimeJob = viewModelScope.launch(Dispatchers.IO) {
            while (isActive) {
                try {
                    val serverTimeDoc = db.collection("utility").document("serverTime").get().await()
                    val timestamp = serverTimeDoc.getTimestamp("timestamp")
                    withContext(Dispatchers.Main) {
                        _serverTime.value = timestamp?.toDate()
                    }
                } catch (e: Exception) {
                    //Log.e("BidViewModel", "Error fetching server time: ${e.message}")
                    if (e.message?.contains("PERMISSION_DENIED") == true) {
                        serverTimeJob?.cancel()
                        //Log.d("BidViewModel", "Server time monitoring stopped due to permission denied")
                        return@launch
                    }
                    withContext(Dispatchers.Main) {
                        _serverTime.value = Date() // Fallback to device time
                    }
                }
                delay(15000) // 5 seconds delay
            }
        }
    }

    fun stopServerTimeMonitoring() {
        //Log.d("BidViewModel", "Stopping server time monitoring")
        serverTimeJob?.cancel()
        _serverTime.value = null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun isGameClosed(closeTime: String): Boolean {
        val formatter = DateTimeFormatter.ofPattern("HH:mm")
        val now = LocalTime.now()
        val close = try { LocalTime.parse(closeTime, formatter) } catch (_: Exception) { null }
        return close != null && now.isAfter(close)
    }

    fun resetMessage() {
        _bidState.value = _bidState.value.copy(message = null)
    }

    fun loadUserBidsRealtime(context: Context, gameType: String, gameId: String): ListenerRegistration {
        val userId = SharedPrefHelper.getUID(context) ?: ""
        return helper.fetchUserBidsRealtime(userId, gameType, gameId) { bids ->
            viewModelScope.launch(Dispatchers.Main) {
                _bidState.value = BidState(bids = bids, isLoading = false)
            }
        }
    }

    fun loadFilteredBids(context: Context, gameId: String, gameType: String, date: String) {
        val userId = SharedPrefHelper.getUID(context) ?: ""
        viewModelScope.launch(Dispatchers.IO) {
            repeat(3) { attempt ->
                try {
                    helper.fetchFilteredBids(userId, gameId, gameType, date) { bids ->
                        viewModelScope.launch(Dispatchers.Main) {
                            _filteredBids.value = bids
                        }
                    }
                } catch (e: Exception) {
                    viewModelScope.launch(Dispatchers.Main) {
                        _filteredBids.value = emptyList()
                    }
                    if (attempt < 2) delay(2000)
                }
            }
        }
    }

    fun submitBids(
        context: Context,
        gameId: String,
        gameType: String,
        session: String,
        bidMap: Map<Int, Int>,
        walletViewModel: WalletViewModel,
        onComplete: (Boolean) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val userId = SharedPrefHelper.getUID(context) ?: ""
            val mobile = SharedPrefHelper.getMobile(context) ?: ""
            val dateStr = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH).format(Date())
            val timeStr = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
            var totalAmount: Long = 0L
            bidMap.forEach { (_, amount) -> totalAmount += amount.toLong() }
            val currentBalance = walletViewModel.walletState.value.balance

            if (totalAmount > currentBalance) {
                withContext(Dispatchers.Main) {
                    _bidState.value = BidState(message = "Insufficient balance")
                    onComplete(false)
                }
                return@launch
            }

            try {
                db.runTransaction { transaction ->
                    val userRef = db.collection("users").document(userId)
                    val userDoc = transaction.get(userRef)
                    val currentBalanceInDb = userDoc.getLong("balance") ?: 0L

                    if (totalAmount > currentBalanceInDb) {
                        throw Exception("Insufficient balance in database")
                    }

                    // Update balance
                    transaction.update(userRef, "balance", currentBalanceInDb - totalAmount)

                    // Create bids
                    bidMap.forEach { (digit, amount) ->
                        val uniqueId = UUID.randomUUID().toString()
                        val bid = BidModel(
                            id = uniqueId,
                            userId = userId,
                            gameId = gameId,
                            gameType = gameType,
                            session = session,
                            bidDigit = digit,
                            bidAmount = amount,
                            date = dateStr,
                            time = timeStr,
                            timestamp = System.currentTimeMillis(),
                            mobile = mobile
                        )
                        val bidRef = db.collection("bids").document(uniqueId)
                        transaction.set(bidRef, bid)
                    }
                }.await()

                withContext(Dispatchers.Main) {
                    _bidState.value = BidState(message = "Bids submitted successfully")
                    onComplete(true)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _bidState.value = BidState(message = "Error submitting bids: ${e.message}")
                    onComplete(false)
                }
            }
        }
    }

    fun submitSangamBids(
        context: Context,
        gameId: String,
        gameType: String,
        session: String,
        bidMap: Map<Pair<Int, Int>, Int>,
        walletViewModel: WalletViewModel,
        onComplete: (Boolean) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val userId = SharedPrefHelper.getUID(context) ?: ""
            val mobile = SharedPrefHelper.getMobile(context) ?: ""
            val dateStr = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH).format(Date())
            val timeStr = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
            var totalAmount: Long = 0L
            bidMap.forEach { (_, amount) -> totalAmount += amount.toLong() }
            val currentBalance = walletViewModel.walletState.value.balance

            if (totalAmount > currentBalance) {
                withContext(Dispatchers.Main) {
                    _bidState.value = BidState(message = "Insufficient balance")
                    onComplete(false)
                }
                return@launch
            }

            try {
                db.runTransaction { transaction ->
                    val userRef = db.collection("users").document(userId)
                    val userDoc = transaction.get(userRef)
                    val currentBalanceInDb = userDoc.getLong("balance") ?: 0L

                    if (totalAmount > currentBalanceInDb) {
                        throw Exception("Insufficient balance in database")
                    }

                    // Update balance
                    transaction.update(userRef, "balance", currentBalanceInDb - totalAmount)

                    // Create bids
                    bidMap.forEach { (pair, amount) ->
                        val (first, second) = pair
                        val uniqueId = UUID.randomUUID().toString()
                        val bid = if (gameType == "Half Sangam") {
                            BidModel(
                                id = uniqueId,
                                userId = userId,
                                gameId = gameId,
                                gameType = gameType,
                                session = session,
                                singleDigit = first,
                                panaDigit = second,
                                bidAmount = amount,
                                date = dateStr,
                                time = timeStr,
                                timestamp = System.currentTimeMillis(),
                                mobile = mobile
                            )
                        } else {
                            BidModel(
                                id = uniqueId,
                                userId = userId,
                                gameId = gameId,
                                gameType = gameType,
                                session = session,
                                openPana = first,
                                closePana = second,
                                bidAmount = amount,
                                date = dateStr,
                                time = timeStr,
                                timestamp = System.currentTimeMillis(),
                                mobile = mobile
                            )
                        }
                        val bidRef = db.collection("bids").document(uniqueId)
                        transaction.set(bidRef, bid)
                    }
                }.await()

                withContext(Dispatchers.Main) {
                    _bidState.value = BidState(message = "Sangam bids submitted successfully")
                    onComplete(true)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _bidState.value = BidState(message = "Error submitting sangam bids: ${e.message}")
                    onComplete(false)
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        //Log.d("BidViewModel", "Clearing BidViewModel")
        stopServerTimeMonitoring()
    }
}
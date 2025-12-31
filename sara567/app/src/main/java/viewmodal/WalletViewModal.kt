package viewmodal

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import firebase.FirebaseWalletHelper
import firebase.SharedPrefHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class WalletState(
    val balance: Long = 0L
)

class WalletViewModel : ViewModel() {

    private val firebaseWalletHelper = FirebaseWalletHelper()
    private var balanceListenerJob: Job? = null



    private var isListenerActive = false        // YE NAEE LINE ADD KARO




    private val _walletState = MutableStateFlow(WalletState())
    val walletState: StateFlow<WalletState> = _walletState.asStateFlow()

    private val _userUID = MutableStateFlow<String?>(null)
    val userUID: StateFlow<String?> = _userUID.asStateFlow()

    @Deprecated("Use walletState.value.balance instead")
    val walletBalance: StateFlow<Long> get() = MutableStateFlow(_walletState.value.balance)

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    private val _adminUpiId = MutableStateFlow<String?>(null)
    val adminUpiId: StateFlow<String?> = _adminUpiId.asStateFlow()






    fun loadUserData(context: Context) {
        val uid = SharedPrefHelper.getUID(context) ?: return
        _userUID.value = uid





        if (uid != null) {
            _walletState.value =
                _walletState.value.copy(balance = SharedPrefHelper.getBalance(context))

            // YE 4 NAEE LINE DAAL DO (sirf ek baar listener lagega)
            if (!isListenerActive) {
                startBalanceListener(context, uid)
                isListenerActive = true
            }
        }

    }










    fun startBalanceListener(context: Context, userId: String) {
        stopBalanceListener()
        balanceListenerJob = viewModelScope.launch {
            firebaseWalletHelper.getUserBalanceFlow(userId)
                .catch { e ->
                    withContext(Dispatchers.Main) {
                        _toastMessage.value = "Error fetching balance: ${e.message}"
                        _walletState.value = _walletState.value.copy(balance = SharedPrefHelper.getBalance(context))
                    }
                }
                .collect { newBalance ->
                    withContext(Dispatchers.Main) {
                        _walletState.value = _walletState.value.copy(balance = newBalance)
                        SharedPrefHelper.setBalance(context, newBalance)
                    }
                }
        }
    }





    fun stopBalanceListener() {
        balanceListenerJob?.cancel()
        balanceListenerJob = null
        isListenerActive = false        // YE EK LINE ADD KARO
    }









    fun loadAdminUpi() {
        viewModelScope.launch(Dispatchers.IO) {  // YE ADD KAR – BACKGROUND THREAD
            val result = firebaseWalletHelper.getAdminUpi()
            withContext(Dispatchers.Main) {  // UI UPDATE MAIN THREAD PE
                _adminUpiId.value = result
            }
        }
    }

    fun submitAddFundRequest(
        amount: Double,
        upiStatus: String,
        transactionId: String,
        transactionRefId: String,
        paymentDateTime: String,
        mobile: String
    ) {
        _userUID.value?.let { uid ->
            if (amount < 200) {
                _toastMessage.value = "Minimum add fund amount is ₹200."
                return
            }
            viewModelScope.launch(Dispatchers.IO) {  // YE ADD KAR – BACKGROUND THREAD
                val result = firebaseWalletHelper.submitAddFundRequest(
                    userId = uid,
                    mobile = mobile,
                    amount = amount,
                    upiStatus = upiStatus,
                    transactionId = transactionId,
                    transactionRefId = transactionRefId,
                    paymentDateTime = paymentDateTime
                )
                withContext(Dispatchers.Main) {  // UI UPDATE MAIN THREAD PE
                    if (result.isSuccess) {
                        _toastMessage.value = "Add fund request submitted successfully!"
                    } else {
                        _toastMessage.value = "Failed to submit add fund request: ${result.exceptionOrNull()?.message}"
                    }
                }
            }
        } ?: run {
            _toastMessage.value = "User not logged in"
        }
    }

    fun submitWithdrawRequest(
        amount: Double,
        upiId: String,
        bankDetails: Map<String, String>,
        mobile: String
    ) {
        _userUID.value?.let { uid ->
            if (amount > _walletState.value.balance) {
                _toastMessage.value = "Withdrawal amount cannot be more than wallet balance."
                return
            }
            if (amount < 500) {
                _toastMessage.value = "Minimum withdrawal amount is ₹500."
                return
            }
            viewModelScope.launch(Dispatchers.IO) {  // YE ADD KAR – BACKGROUND THREAD
                val result = firebaseWalletHelper.submitWithdrawRequest(
                    userId = uid,
                    mobile = mobile,
                    amount = amount,
                    upiId = upiId,
                    bankDetails = bankDetails
                )
                withContext(Dispatchers.Main) {  // UI UPDATE MAIN THREAD PE
                    if (result.isSuccess) {
                        _toastMessage.value = "Withdraw request submitted successfully."
                    } else {
                        _toastMessage.value = "Failed to submit withdraw request: ${result.exceptionOrNull()?.message}"
                    }
                }
            }
        } ?: run {
            _toastMessage.value = "User not logged in"
        }
    }

    fun submitQRPayRequest(
        amount: Double,
        upiStatus: String,
        transactionId: String,
        transactionRefId: String,
        paymentDateTime: String,
        mobile: String
    ) {
        _userUID.value?.let { uid ->
            if (amount < 500) {
                _toastMessage.value = "Minimum QR pay amount is ₹500."
                return
            }
            viewModelScope.launch(Dispatchers.IO) {  // YE ADD KAR – BACKGROUND THREAD
                val result = firebaseWalletHelper.submitQRPayRequest(
                    userId = uid,
                    mobile = mobile,
                    amount = amount,
                    upiStatus = upiStatus,
                    transactionId = transactionId,
                    transactionRefId = transactionRefId,
                    paymentDateTime = paymentDateTime
                )
                withContext(Dispatchers.Main) {  // UI UPDATE MAIN THREAD PE
                    if (result.isSuccess) {
                        _toastMessage.value = "QR pay request submitted successfully!"
                    } else {
                        _toastMessage.value = "Failed to submit QR pay request: ${result.exceptionOrNull()?.message}"
                    }
                }
            }
        } ?: run {
            _toastMessage.value = "User not logged in"
        }
    }

    fun onToastShown() {
        _toastMessage.value = null
    }

    override fun onCleared() {
        super.onCleared()
     //   Log.d("WalletViewModel", "Clearing WalletViewModel, stopping listeners")
        stopBalanceListener()
    }
}
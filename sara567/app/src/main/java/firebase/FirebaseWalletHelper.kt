package firebase

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestoreSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FirebaseWalletHelper {

    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")
    private val addRequestsCollection = db.collection("add_requests")
    private val withdrawRequestsCollection = db.collection("withdraw_requests")
    private val qrPayRequestsCollection = db.collection("QRpayRequest")
    private val adminUpiCollection = db.collection("admin_upi")

    init {
        // Enable Firestore offline persistence
        db.firestoreSettings = firestoreSettings {
            isPersistenceEnabled = true
        }
    }

    fun getUserBalanceFlow(userId: String): Flow<Long> = callbackFlow {
        val docRef = usersCollection.document(userId)
        val listener = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val balance = snapshot?.takeIf { it.exists() }?.getLong("balance") ?: 0L
            trySend(balance).isSuccess
        }
        awaitClose { listener.remove() }
    }

    suspend fun getAdminUpi(): String? = withContext(Dispatchers.IO) {
        try {
            val snapshot = adminUpiCollection.document("upi_details").get().await()
            snapshot.getString("adminupi")?.takeIf { it.isNotBlank() }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun submitAddFundRequest(
        userId: String,
        mobile: String,
        amount: Double,
        upiStatus: String,
        transactionId: String,
        transactionRefId: String,
        paymentDateTime: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Validation checks without direct return
            when {
                userId.isBlank() -> Result.failure(Exception("User ID is required"))
                mobile.isBlank() -> Result.failure(Exception("Mobile number is required"))
                amount < 200 -> Result.failure(Exception("Minimum amount is ₹200"))
                else -> {
                    val requestStatus = if (upiStatus.equals("success", ignoreCase = true)) "pending" else "rejected"
                    val sdfDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val sdfTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                    val now = Date()

                    val requestData = hashMapOf(
                        "userId" to userId,
                        "mobile" to mobile,
                        "amount" to amount,
                        "upiStatus" to upiStatus,
                        "requestStatus" to requestStatus,
                        "paymentDateTime" to paymentDateTime,
                        "paymentDate" to sdfDate.format(now),
                        "paymentTime" to sdfTime.format(now),
                        "transactionId" to transactionId,
                        "utr" to transactionRefId,
                        "timestamp" to System.currentTimeMillis()
                    )

                    val docId = "$userId-${System.currentTimeMillis()}"
                    addRequestsCollection.document(docId).set(requestData).await()
                    Result.success(Unit)
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun submitWithdrawRequest(
        userId: String,
        mobile: String,
        amount: Double,
        upiId: String,
        bankDetails: Map<String, String>
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Validation checks without direct return
            when {
                userId.isBlank() -> Result.failure(Exception("User ID is required"))
                mobile.isBlank() -> Result.failure(Exception("Mobile number is required"))
                amount < 500 -> Result.failure(Exception("Minimum withdrawal amount is ₹500"))
                upiId.isBlank() -> Result.failure(Exception("UPI ID is required"))
                else -> {
                    val sdfDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val sdfTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                    val now = Date()

                    val requestData = hashMapOf(
                        "userId" to userId,
                        "mobile" to mobile,
                        "amount" to amount,
                        "upiId" to upiId,
                        "accountHolderName" to (bankDetails["holderName"]?.takeIf { it.isNotBlank() } ?: ""),
                        "accountNumber" to (bankDetails["accountNo"]?.takeIf { it.isNotBlank() } ?: ""),
                        "bankName" to (bankDetails["bankName"]?.takeIf { it.isNotBlank() } ?: ""),
                        "ifscCode" to (bankDetails["ifsc"]?.takeIf { it.isNotBlank() } ?: ""),
                        "requestStatus" to "pending",
                        "requestDate" to sdfDate.format(now),
                        "paymentTime" to sdfTime.format(now),
                        "timestamp" to System.currentTimeMillis()
                    )

                    val docId = "$userId-${System.currentTimeMillis()}"
                    withdrawRequestsCollection.document(docId).set(requestData).await()
                    Result.success(Unit)
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun submitQRPayRequest(
        userId: String,
        mobile: String,
        amount: Double,
        upiStatus: String,
        transactionId: String,
        transactionRefId: String,
        paymentDateTime: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Validation checks without direct return
            when {
                userId.isBlank() -> Result.failure(Exception("User ID is required"))
                mobile.isBlank() -> Result.failure(Exception("Mobile number is required"))
                amount < 500 -> Result.failure(Exception("Minimum QR pay amount is ₹500"))
                transactionId.isBlank() -> Result.failure(Exception("Transaction ID is required"))
                else -> {
                    val requestStatus = if (upiStatus.equals("Success", ignoreCase = true)) "pending" else "rejected"
                    val sdfDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val sdfTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                    val now = Date()

                    val requestData = hashMapOf(
                        "userId" to userId,
                        "mobile" to mobile,
                        "amount" to amount,
                        "upiStatus" to upiStatus,
                        "requestStatus" to requestStatus,
                        "paymentDateTime" to paymentDateTime,
                        "paymentDate" to sdfDate.format(now),
                        "paymentTime" to sdfTime.format(now),
                        "transactionId" to transactionId,
                        "transactionRefId" to transactionRefId.ifEmpty { "N/A" },
                        "timestamp" to System.currentTimeMillis()
                    )

                    val docId = "$userId-${System.currentTimeMillis()}"
                    qrPayRequestsCollection.document(docId).set(requestData).await()
                    Result.success(Unit)
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
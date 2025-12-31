package firebase

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

data class BidModel(
    val id: String = "",
    val userId: String = "",
    val gameId: String = "",
    val gameType: String = "",
    val session: String = "",
    val bidDigit: Int? = null,
    val singleDigit: Int? = null,
    val panaDigit: Int? = null,
    val openPana: Int? = null,
    val closePana: Int? = null,
    val bidAmount: Int = 0,
    val date: String = "",
    val time: String = "",
    val timestamp: Long = 0,
    val status: String = "pending",
    val mobile: String = "",
    val payoutAmount:Int =0
)

class FirebaseBidHelper {
    private val db = FirebaseFirestore.getInstance()

    suspend fun submitBid(bid: BidModel, onResult: (Boolean, String) -> Unit) {
        withContext(Dispatchers.IO) {
            try {
                db.collection("bids").add(bid).await()
                onResult(true, "Bid submitted successfully")
            } catch (e: Exception) {
                onResult(false, e.message ?: "Error submitting bid")
            }
        }
    }

    fun fetchUserBidsRealtime(
        userId: String,
        gameType: String,
        gameId: String,
        onResult: (List<BidModel>) -> Unit
    ): ListenerRegistration {
        return db.collection("bids")
            .whereEqualTo("userId", userId)
            .whereEqualTo("gameType", gameType)
            .whereEqualTo("gameId", gameId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onResult(emptyList())
                    return@addSnapshotListener
                }
                val bids = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(BidModel::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                onResult(bids)
            }
    }

    suspend fun fetchFilteredBids(
        userId: String,
        gameId: String,
        gameType: String,
        date: String,
        onResult: (List<BidModel>) -> Unit
    ) {
        withContext(Dispatchers.IO) {
            var query = db.collection("bids").whereEqualTo("userId", userId)
            if (gameId.isNotEmpty()) {
                query = query.whereEqualTo("gameId", gameId)
            }
            if (gameType.isNotEmpty()) {
                query = query.whereEqualTo("gameType", gameType)
            }
            if (date.isNotEmpty()) {
                query = query.whereEqualTo("date", date)
            }
            try {
                val snapshot = query.get().await()
                val bids = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(BidModel::class.java)?.copy(id = doc.id)
                }
                onResult(bids)
            } catch (e: Exception) {
                onResult(emptyList())
            }
        }
    }
}
package firebase



import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

data class HomeTicker(
    val adminMessage: String = "",
    val whatsapp1: String = "",
    val whatsapp2: String = ""
)

class FirebaseTickerHelper(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private val tickerCollection = db.collection("homeTicker")

    // ✅ Real-time stream for admin message + WhatsApp numbers
    fun getHomeTicker(): Flow<HomeTicker> = callbackFlow {
        val listener = tickerCollection.document("info")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    close(e)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val data = snapshot.toObject(HomeTicker::class.java)
                    trySend(data ?: HomeTicker())
                }
            }
        awaitClose { listener.remove() }
    }

    // ✅ Update function (for admin panel use)
    fun updateHomeTicker(ticker: HomeTicker, onResult: (Boolean) -> Unit) {
        tickerCollection.document("info")
            .set(ticker)
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }
}

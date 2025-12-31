package firebase

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import viewmodel.BidViewModel
import java.util.Date

data class Game(
    val id: String = "",
    val gameName: String = "",
    val openTime: String = "10:00",
    val closeTime: String = "18:00",
    val openResult: String = "",
    val closeResult: String = "",
    val enabled: Boolean = true, // Changed from isEnabled to enabled (no "is" prefix)
    var isCardActive: Boolean = false,
    val lastResetAt: Timestamp? = null,
    @PropertyName("isEditing")
    val isEditing: Boolean = false


    ,

    @PropertyName("openResultDate") val openResultDate: String? = null,
    @PropertyName("closeResultDate") val closeResultDate: String? = null
) {
    val todayResult: String
        get() {
            val openSum = if (openResult.length == 3) {
                openResult.map { it.toString().toIntOrNull() ?: 0 }.sum() % 10
            } else null
            val closeSum = if (closeResult.length == 3) {
                closeResult.map { it.toString().toIntOrNull() ?: 0 }.sum() % 10
            } else null
            return when {
                openResult.isNotEmpty() && closeResult.isNotEmpty() ->
                    "${openResult} _ ${openSum ?: "*"}${closeSum ?: "*"} _ ${closeResult}"
                openResult.isNotEmpty() ->
                    "${openResult} _ ${openSum ?: "*"} * _ ***"
                closeResult.isNotEmpty() ->
                    "*** _ * ${closeSum ?: "_*"} _ ${closeResult}"
                else -> "*** _ ** _ ***"
            }
        }
}

class FirebaseHelper {
    private val db = FirebaseFirestore.getInstance()
    private var gameListener: ListenerRegistration? = null
    private var serverTimeJob: Job? = null
    private var cachedServerTime: Date? = null
    private var lastFetchTime: Long = 0
    private val cacheDurationMs = 5000L // 5 seconds cache

    fun listenGames(onResult: (List<Game>) -> Unit) {
        gameListener = db.collection("games")
            .orderBy("openTime", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                   // println("Error listening to games: ${error.message}")
                    onResult(emptyList())
                    return@addSnapshotListener
                }
                if (snapshot == null) {
                  //  println("Games snapshot is null")
                    onResult(emptyList())
                    return@addSnapshotListener
                }
                val games = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Game::class.java)?.copy(id = doc.id).also {
                        // Log to check enabled value
                    //    println("Game: ${it?.gameName}, enabled: ${it?.enabled}")
                    }
                }
               // println("Fetched ${games.size} games")
                onResult(games)
            }
    }

    fun addGame(game: Game, onComplete: (Boolean) -> Unit) {
        db.collection("games")
            .add(game.copy(id = "", enabled = true)) // Ensure enabled is set
            .addOnSuccessListener {
               // println("Game added successfully")
                onComplete(true)
            }
            .addOnFailureListener { e ->
                //println("Error adding game: ${e.message}")
                onComplete(false)
            }
    }

    fun updateGame(gameId: String, updatedGame: Game, onComplete: (Boolean) -> Unit) {
        db.collection("games")
            .document(gameId)
            .set(updatedGame.copy(id = gameId))
            .addOnSuccessListener {
                //println("Game updated successfully")
                onComplete(true)
            }
            .addOnFailureListener { e ->
                //println("Error updating game: ${e.message}")
                onComplete(false)
            }
    }

//    fun resetResults(gameId: String, onComplete: (Boolean) -> Unit) {
//        db.collection("games")
//            .document(gameId)
//            .update(mapOf("openResult" to "", "closeResult" to ""))
//            .addOnSuccessListener {
//                println("Results reset successfully")
//                onComplete(true)
//            }
//            .addOnFailureListener { e ->
//                println("Error resetting results: ${e.message}")
//                onComplete(false)
//            }
//    }

    fun stopListening() {
        //println("Stopping game listener and server time job")
        gameListener?.remove()
        serverTimeJob?.cancel()
    }

    fun stopAllListeners() {
        //println("Stopping all Firestore listeners and jobs")
        stopListening()
        BidViewModel().stopServerTimeMonitoring()
    }

    suspend fun ensureServerTimeDoc() {
        try {
            val docRef = db.collection("serverTime").document("current")
            val snapshot = docRef.get().await()
            if (!snapshot.exists()) {
                docRef.set(mapOf("timestamp" to FieldValue.serverTimestamp())).await()
          //      println("Server time document created")
            }
            startServerTimeUpdate()
        } catch (e: Exception) {
            //println("Error ensuring server time doc: ${e.message}")
            startServerTimeUpdate()
        }
    }

    private fun startServerTimeUpdate() {
        serverTimeJob?.cancel()
        serverTimeJob = CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                try {
                    val docRef = db.collection("serverTime").document("current")
                    val snapshot = docRef.get().await()
                    cachedServerTime = snapshot.getTimestamp("timestamp")?.toDate() ?: Date()
                    lastFetchTime = System.currentTimeMillis()
              //      println("Server time updated: $cachedServerTime")
                    docRef.update("timestamp", FieldValue.serverTimestamp()).await()
                } catch (e: Exception) {
                //    println("Error updating server time: ${e.message}")
                    cachedServerTime = Date()
                }
                delay(15000L)
            }
        }
    }

    suspend fun getServerTime(): Date {
        val currentTime = System.currentTimeMillis()
        if (cachedServerTime != null && currentTime - lastFetchTime < cacheDurationMs) {
            return cachedServerTime!!
        }
        return withContext(Dispatchers.IO) {
            try {
                val docRef = db.collection("serverTime").document("current")
                val snapshot = docRef.get().await()
                cachedServerTime = snapshot.getTimestamp("timestamp")?.toDate() ?: Date()
                lastFetchTime = System.currentTimeMillis()
                //println("Server time fetched: $cachedServerTime")
                cachedServerTime!!
            } catch (e: Exception) {
                //println("Error fetching server time: ${e.message}")
                Date()
            }
        }
    }
}
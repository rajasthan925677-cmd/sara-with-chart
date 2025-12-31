package firebase


import com.google.firebase.firestore.FirebaseFirestore

data class VersionUpdateModel(
    val latestVersion: Int = 1,
    val forceUpdate: Boolean = false,
    val downloadUrl: String = ""
)

object VersionUpdateHelper {

    fun getUpdateInfo(
        onResult: (VersionUpdateModel?) -> Unit
    ) {
        val doc = FirebaseFirestore.getInstance()
            .collection("appInfo")
            .document("version") // ya aapka document name

        doc.get().addOnSuccessListener { snap ->
            if (snap.exists()) {
                val data = VersionUpdateModel(
                    latestVersion = snap.getLong("latestVersion")?.toInt() ?: 1,
                    forceUpdate = snap.getBoolean("forceUpdate") ?: false,
                    downloadUrl = snap.getString("downloadUrl") ?: ""
                )
                onResult(data)
            } else {
                onResult(null)
            }
        }.addOnFailureListener {
            onResult(null)
        }
    }
}
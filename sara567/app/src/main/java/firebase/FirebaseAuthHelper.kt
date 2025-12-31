package firebase

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FirebaseAuthHelper {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")

    // Convert mobile number to email format (lowercase)
    private fun mobileToEmail(mobile: String): String {
        return "${mobile.lowercase()}@myapp.com"
    }

    // Sign Up
    fun signUp(
        name: String,
        mobile: String,
        password: String,
        onResult: (success: Boolean, message: String) -> Unit
    ) {
        val email = mobileToEmail(mobile)

        //Log.d("SIGNUP_FLOW", "Attempting sign-up with email=$email, name=$name")
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val uid = authResult.user?.uid ?: ""
                //Log.d("SIGNUP_FLOW", "Auth user created with uid=$uid")
                val userData = hashMapOf(
                    "name" to name,
                    "mobile" to mobile,
                    "balance" to 9L,
                    "role" to "user"
                )
                usersCollection.document(uid).set(userData)
                    .addOnSuccessListener {
                        //Log.d("SIGNUP_FLOW", "User data saved in Firestore for uid=$uid")
                        onResult(true, "Sign Up Successful")
                    }
                    .addOnFailureListener { e ->
                        //Log.e("SIGNUP_FLOW", "Failed to save user data in Firestore: ${e.message}", e)
                        auth.currentUser?.delete()?.addOnCompleteListener {
                            //Log.d("SIGNUP_FLOW", "Auth user deleted due to Firestore failure")
                        }
                        onResult(false, e.message ?: "Error saving user data. Please sign up again.")
                    }
            }
            .addOnFailureListener { e ->
                //Log.e("SIGNUP_FLOW", "Auth sign-up failed: ${e.message}", e)
                val errorMessage = if (e.message?.contains("email address is already in use") == true) {
                    "User already registered with this mobile number"
                } else {
                    e.message ?: "Error signing up user"
                }
                onResult(false, errorMessage)
            }
    }

    // Sign In
    fun signIn(
        mobile: String,
        password: String,
        onResult: (success: Boolean, message: String) -> Unit
    ) {
        val email = mobileToEmail(mobile)
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val uid = authResult.user?.uid ?: ""
                usersCollection.document(uid).get()
                    .addOnSuccessListener { doc ->
                        if (doc.exists()) {
                            val role = doc.getString("role") ?: "user"
                            if (role == "user") {
                                onResult(true, "Sign In Successful")
                            } else {
                                onResult(false, "Access Denied")
                                auth.signOut()
                            }
                        } else {
                            onResult(false, "User data not found")
                        }
                    }
                    .addOnFailureListener { e ->
                        onResult(false, e.message ?: "Error fetching user data")
                    }
            }
            .addOnFailureListener { e ->
                onResult(false, e.message ?: "Authentication failed")
            }
    }

    // Forgot Password
    fun forgotPassword(
        mobile: String,
        newPassword: String,
        onResult: (success: Boolean, message: String) -> Unit
    ) {
        val email = mobileToEmail(mobile)
        //Log.d("FORGOT_FLOW", "Checking Auth for email=$email, newPassword=$newPassword")

        if (auth == null) {
            //Log.e("FORGOT_FLOW", "FirebaseAuth instance is null")
            onResult(false, "Authentication service unavailable")
            return
        }

        auth.fetchSignInMethodsForEmail(email)
            .addOnSuccessListener { result ->
                //Log.d("FORGOT_FLOW", "fetchSignInMethodsForEmail result: methods=${result.signInMethods}, email=$email, isValid=${result.signInMethods?.isNotEmpty()}")
                val isRegistered = !result.signInMethods.isNullOrEmpty()
                if (isRegistered) {
                    //Log.d("FORGOT_FLOW", "User is registered with email=$email")
                    onResult(true, "User registered|$newPassword")
                } else {
                    //Log.d("FORGOT_FLOW", "No user found for email=$email")
                    db.collection("users").whereEqualTo("mobile", mobile).get()
                        .addOnSuccessListener { querySnapshot ->
                            if (!querySnapshot.isEmpty) {
                                //Log.d("FORGOT_FLOW", "User found in Firestore but not in Auth: mobile=$mobile, uid=${querySnapshot.documents[0].id}")
                            }
                        }
                    onResult(false, "User not registered")
                }
            }
            .addOnFailureListener { e ->
                //Log.e("FORGOT_FLOW", "fetchSignInMethodsForEmail failed: ${e.message}", e)
                val errorMessage = when {
                    e.message?.contains("INVALID_EMAIL") == true -> "Invalid email format"
                    e.message?.contains("network") == true -> "Network error, please try again"
                    else -> "Error checking user: ${e.message}"
                }
                onResult(false, errorMessage)
            }
    }

    // Get Current User Data
    fun getCurrentUserData(
        onResult: (name: String?, mobile: String?) -> Unit
    ) {
        val uid = auth.currentUser?.uid ?: ""
        usersCollection.document(uid).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val name = doc.getString("name")
                    val mobileNo = doc.getString("mobile")
                    onResult(name, mobileNo)
                } else {
                    onResult(null, null)
                }
            }
            .addOnFailureListener {
                onResult(null, null)
            }
    }

    // Add Balance Field to Existing Users
    fun addBalanceFieldToExistingUsers() {
        usersCollection.get()
            .addOnSuccessListener { result ->
                result.documents.forEach { doc ->
                    if (!doc.contains("balance")) {
                        doc.reference.update("balance", 0L)
                    }
                }
            }
    }
}
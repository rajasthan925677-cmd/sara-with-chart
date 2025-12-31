package viewmodal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import firebase.FirebaseAuthHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AuthState(
    val success: Boolean = false,
    val message: String? = null,
    val isLoading: Boolean = false
)

class AuthViewModel : ViewModel() {

    private val firebaseAuthHelper = FirebaseAuthHelper()

    private val _signUpState = MutableStateFlow(AuthState())
    val signUpState: StateFlow<AuthState> = _signUpState

    private val _signInState = MutableStateFlow(AuthState())
    val signInState: StateFlow<AuthState> = _signInState

    private val _forgotPasswordState = MutableStateFlow(AuthState())
    val forgotPasswordState: StateFlow<AuthState> = _forgotPasswordState

    // -------------------------------
    // Sign Up
    // -------------------------------
    fun signUp(name: String, mobile: String, password: String) {
        _signUpState.value = AuthState(isLoading = true)
        viewModelScope.launch {
            firebaseAuthHelper.signUp(name, mobile, password) { success, message ->
                _signUpState.value = AuthState(success = success, message = message)
            }
        }
    }

    fun clearSignUpMessage() {
        _signUpState.value = _signUpState.value.copy(message = null)
    }

    // -------------------------------
    // Sign In
    // -------------------------------
    fun signIn(mobile: String, password: String) {
        _signInState.value = AuthState(isLoading = true)
        viewModelScope.launch {
            firebaseAuthHelper.signIn(mobile, password) { success, message ->
                _signInState.value = AuthState(success = success, message = message)
            }
        }
    }

    fun clearSignInMessage() {
        _signInState.value = _signInState.value.copy(message = null)
    }

    // -------------------------------
    // Forgot Password
    // -------------------------------
    // -------------------------------
// Forgot Password (Updated to pass newPassword)
// -------------------------------
    fun forgotPassword(mobile: String, newPassword: String) { // <--- newPassword जोड़ा
        //android.util.Log.d("FORGOT_FLOW", "ViewModel.forgotPassword called with mobile=$mobile")
        _forgotPasswordState.value = AuthState(isLoading = true)
        viewModelScope.launch {
            // FirebaseAuthHelper को newPassword पास किया
            firebaseAuthHelper.forgotPassword(mobile, newPassword) { success, message ->
               // android.util.Log.d("FORGOT_FLOW", "FirebaseAuthHelper callback => success=$success, message=$message")
                _forgotPasswordState.value = AuthState(success = success, message = message)
            }
        }
    }

    fun clearForgotPasswordMessage() {
        _forgotPasswordState.value = _forgotPasswordState.value.copy(message = null)
    }
}
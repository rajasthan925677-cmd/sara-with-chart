package screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.sara567.MainActivity
import com.example.sara567.R
import com.google.firebase.auth.FirebaseAuth
import firebase.FirebaseAuthHelper
import firebase.SharedPrefHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import viewmodal.AuthViewModel

@Composable
fun SignupPage(navController: NavController, viewModel: AuthViewModel) {
    var userName by remember { mutableStateOf("") }
    var mobno by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val context = LocalContext.current
    val mainActivity = context as? MainActivity
    val signUpState by viewModel.signUpState.collectAsState()
    val density = LocalDensity.current

    // Define rememberCoroutineScope at the top level
    val coroutineScope = rememberCoroutineScope()

    // State to hold fetched name and mobile
    var fetchedName by remember { mutableStateOf<String?>(null) }
    var fetchedMobile by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(signUpState.success) {
        if (signUpState.success) {
            mainActivity?.setSigningUpState(true) // Set isSigningUp to true
            coroutineScope.launch {
                // Small delay to ensure FirebaseAuth sync
                delay(500)
                FirebaseAuthHelper().getCurrentUserData { name, mobile ->
                    fetchedName = name
                    fetchedMobile = mobile
                    if (mobile != null && name != null && FirebaseAuth.getInstance().currentUser != null) {
                        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                        SharedPrefHelper.setLoggedIn(context, mobile, uid, name)
                        FirebaseAuth.getInstance().currentUser?.getIdToken(true)
                        //Log.d("SignupPage", "Navigating to HomePage after signup, UID: $uid")
                        navController.navigate("home") {
                            popUpTo("signup") { inclusive = true }
                            launchSingleTop = true
                        }
                        mainActivity?.setSigningUpState(false) // Reset isSigningUp
                    } else {
                        Toast.makeText(context, "Error fetching user data after signup", Toast.LENGTH_SHORT).show()
                        mainActivity?.setSigningUpState(false) // Reset isSigningUp on error
                        navController.navigate("signin") {
                            popUpTo("signup") { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }
            }
        }
    }

    LaunchedEffect(signUpState.message) {
        signUpState.message?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearSignUpMessage()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F7F8))
            .verticalScroll(rememberScrollState()),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(horizontal = with(density) { 20.dp * density.density / 2 }),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(with(density) { 20.dp * density.density / 2 }))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(top = with(density) { 10.dp * density.density / 2 }),
                shape = RoundedCornerShape(with(density) { 40.dp * density.density / 2 }),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.elevatedCardElevation(with(density) { 15.dp * density.density / 2 })
            ) {





                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(with(density) { 180.dp * density.density / 2 })
                        .padding(with(density) { 20.dp * density.density / 2 }),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Spacer(modifier = Modifier.width(32.dp))

                        // Yellow Circle
                        Box(
                            modifier = Modifier
                                .size(68.dp * density.density / 2)
                                .background(color = Color(0xFFF7B500), shape = CircleShape)
                        )

                        // Text Part - अब बिल्कुल सही, कोई red line नहीं आएगी
                        Row(
                            modifier = Modifier
                                .offset(x = (-48).dp * density.density / 2)   // यही सही है
                                .padding(start = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "S",
                                fontSize = 52.sp * density.density / 2,
                                fontWeight = FontWeight.Medium,
                                color = Color.Black
                            )

                            Box {
                                Text(
                                    text = "ara",
                                    fontSize = 36.sp * density.density / 2,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.Black,
                                    modifier = Modifier.align(Alignment.BottomCenter)
                                )

                                // Yellow line ऊपर
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopCenter)
                                        .offset(y = 6.dp)
                                        .width(44.dp * density.density / 2)
                                        .height(4.dp * density.density / 2)
                                        .background(Color(0xFF0C0C0C), RoundedCornerShape(4.dp))
                                )
                            }

                            Text(
                                text = "777",
                                fontSize = 46.sp * density.density / 2,
                                fontWeight = FontWeight.Normal,
                                color = Color.Black,
                                letterSpacing = -1.5.sp,
                                modifier = Modifier
                                    .padding(start = 2.dp)
                                    .offset(y = 6.dp)
                            )
                        }
                    }
                }
            }






            Spacer(modifier = Modifier.height(with(density) { 15.dp * density.density / 2 }))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(bottom = with(density) { 20.dp * density.density / 2 }),
                shape = RoundedCornerShape(with(density) { 40.dp * density.density / 2 }),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.elevatedCardElevation(with(density) { 15.dp * density.density / 2 })
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(with(density) { 15.dp * density.density / 2 }),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Welcome To Sara777",
                        fontSize = with(density) { 22.sp * density.density / 2 },
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFABE0F)
                    )

                    Spacer(modifier = Modifier.height(with(density) { 20.dp * density.density / 2 }))

                    OutlinedTextField(
                        value = userName,
                        onValueChange = { userName = it },
                        label = { Text("User Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(with(density) { 40.dp * density.density / 2 })
                    )

                    Spacer(modifier = Modifier.height(with(density) { 15.dp * density.density / 2 }))

                    OutlinedTextField(
                        value = mobno,
                        onValueChange = { if (it.length <= 10 && it.all { c -> c.isDigit() }) mobno = it },
                        label = { Text("Mobile Number") },
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(with(density) { 40.dp * density.density / 2 })
                    )

                    Spacer(modifier = Modifier.height(with(density) { 15.dp * density.density / 2 }))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(with(density) { 40.dp * density.density / 2 })
                    )

                    Spacer(modifier = Modifier.height(with(density) { 15.dp * density.density / 2 }))

                    Button(
                        onClick = {
                            if (userName.isNotBlank() && mobno.length == 10 && password.isNotBlank()) {
                                mainActivity?.setSigningUpState(true) // Set isSigningUp before signup
                                viewModel.signUp(userName, mobno, password)
                            } else {
                                Toast.makeText(context, "Fill all fields correctly", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(with(density) { 50.dp * density.density / 2 }),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFABE0F)),
                        shape = RoundedCornerShape(with(density) { 40.dp * density.density / 2 })
                    ) {
                        Text(
                            text = "Sign Up",
                            color = Color.White,
                            fontSize = with(density) { 18.sp * density.density / 2 }
                        )
                    }

                    Spacer(modifier = Modifier.height(with(density) { 20.dp * density.density / 2 }))

                    TextButton(onClick = { navController.navigate("signin") }) {
                        Text(
                            text = "Already have an Account? Log In",
                            color = Color(0xFF5D75E7),
                            fontSize = with(density) { 14.sp * density.density / 2 }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(with(density) { 20.dp * density.density / 2 }))
        }
    }
}
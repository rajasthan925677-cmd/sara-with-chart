package screens

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.sara567.R
import com.google.firebase.auth.FirebaseAuth
import firebase.FirebaseAuthHelper
import firebase.SharedPrefHelper
import kotlinx.coroutines.launch
import navigation.Screen
import viewmodal.AuthViewModel

@Composable
fun LoginPage(navController: NavController, viewModel: AuthViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current

    val mobno = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    val isError = remember { mutableStateOf(false) }

    val signInState by viewModel.signInState.collectAsState()

    BackHandler {
        (context as? Activity)?.finish()
    }

    LaunchedEffect(signInState.message) {
        signInState.message?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearSignInMessage()

            if (signInState.success) {
                coroutineScope.launch {
                    FirebaseAuthHelper().getCurrentUserData { name, mobile ->
                        if (mobile != null && name != null) {
                            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                            SharedPrefHelper.setLoggedIn(context, mobile, uid, name)


                          //  SharedPrefHelper.setLoggedIn(context, mobile, uid, name)
                            FirebaseAuth.getInstance().currentUser?.getIdToken(true) // Force token refresh (optional but good)

                            navController.navigate(Screen.HomePage.route) {
                                popUpTo(Screen.SignInPage.route) { inclusive = true }
                            }
                        } else {
                            Toast.makeText(context, "Error fetching user data", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFCfd8dc))
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

                        // Yellow Circle (बिल्कुल वैसा ही)
                        Box(
                            modifier = Modifier
                                .size(68.dp * density.density / 2)
                                .background(color = Color(0xFFF7B500), shape = CircleShape)
                        )

                        // Text: Sara777 (तुम्हारी दी हुई exact style में)
                        Row(
                            modifier = Modifier
                                .offset(x = (-48).dp * density.density / 2)  // ओवरलैप कराने के लिए
                                .padding(start = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // S बड़ा और ऊपर
                            Text(
                                text = "S",
                                fontSize = 52.sp * density.density / 2,
                                fontWeight = FontWeight.Medium,
                                color = Color.Black
                            )

                            // ara + उसके ऊपर yellow line
                            Box {
                                Text(
                                    text = "ara",
                                    fontSize = 36.sp * density.density / 2,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.Black,
                                    modifier = Modifier.align(Alignment.BottomCenter)
                                )

                                // यही वो yellow line है जो तुम चाहते थे
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopCenter)
                                        .offset(y = 6.dp)
                                        .width(44.dp * density.density / 2)
                                        .height(4.dp * density.density / 2)
                                        .background(
                                            color = Color(0xFF070707),  // yellow line
                                            shape = RoundedCornerShape(4.dp)
                                        )
                                )
                            }

                            // 777 थोड़ा नीचे और पास में
                            Text(
                                text = "777",
                                fontSize = 46.sp * density.density / 2,
                                fontWeight = FontWeight.Normal,
                                color = Color.Black,
                                letterSpacing = -1.5.sp,
                                modifier = Modifier
                                    .padding(start = 2.dp)
                                    .offset(y = 6.dp)  // थोड़ा नीचे
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
                        value = mobno.value,
                        onValueChange = { input ->
                            if (input.all { it.isDigit() } && input.length <= 10) {
                                mobno.value = input
                            }
                            isError.value = mobno.value.length != 10
                        },
                        label = { Text("Mobile Number") },
                        isError = isError.value,
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(with(density) { 40.dp * density.density / 2 })
                    )

                    Spacer(modifier = Modifier.height(with(density) { 15.dp * density.density / 2 }))

                    OutlinedTextField(
                        value = password.value,
                        onValueChange = { password.value = it },
                        label = { Text("Password") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(with(density) { 40.dp * density.density / 2 })
                    )

                    Spacer(modifier = Modifier.height(with(density) { 20.dp * density.density / 2 }))

                    Button(
                        onClick = {
                            if (mobno.value.length == 10 && password.value.isNotBlank()) {
                                viewModel.signIn(mobno.value, password.value)
                            } else {
                                Toast.makeText(context, "Enter valid details", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(with(density) { 50.dp * density.density / 2 }),
                        shape = RoundedCornerShape(with(density) { 40.dp * density.density / 2 }),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFABE0F))
                    ) {
                        Text(
                            text = "Log In",
                            fontSize = with(density) { 18.sp * density.density / 2 },
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(with(density) { 25.dp * density.density / 2 }))

                    Text(
                        text = "OR",
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        fontSize = with(density) { 18.sp * density.density / 2 },
                        color = Color.DarkGray,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(with(density) { 15.dp * density.density / 2 }))

                    Button(
                        onClick = { navController.navigate("signup") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(with(density) { 50.dp * density.density / 2 }),
                        shape = RoundedCornerShape(with(density) { 40.dp * density.density / 2 }),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFABE0F))
                    ) {
                        Text(
                            text = "New User",
                            fontSize = with(density) { 18.sp * density.density / 2 },
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(with(density) { 10.dp * density.density / 2 }))

                    TextButton(onClick = { navController.navigate("forget_password") }) {
                        Text(
                            text = "Forgot Password?",
                            fontSize = with(density) { 14.sp * density.density / 2 },
                            color = Color(0xFF5D75E7),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(with(density) { 20.dp * density.density / 2 }))
        }
    }
}
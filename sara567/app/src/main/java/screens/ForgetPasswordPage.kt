package screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.sara567.R
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import viewmodal.AuthViewModel

@Composable
fun ForgotPasswordPage(navController: NavController, viewModel: AuthViewModel) {
    val context = LocalContext.current

    val density = LocalDensity.current   // ← ये लाइन जोड़ो

    val coroutineScope = CoroutineScope(Dispatchers.IO)
    val forgotPasswordState by viewModel.forgotPasswordState.collectAsState()

    var mobno by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }

    // Show toast for messages
    LaunchedEffect(forgotPasswordState.message) {
        forgotPasswordState.message?.let { msg ->
            // Only show toast for error messages (when success is false)
            if (!forgotPasswordState.success) {
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            }
            viewModel.clearForgotPasswordMessage()
        }
    }

    // Handle WhatsApp redirect on success
    LaunchedEffect(forgotPasswordState.success) {
      //  Log.d("FORGOT_FLOW", "forgotPasswordState changed: success=${forgotPasswordState.success}, message=${forgotPasswordState.message}")
        if (forgotPasswordState.success) {
            coroutineScope.launch {
                try {
                    val db = FirebaseFirestore.getInstance()
                    val tickerDoc = db.collection("homeTicker").document("info").get().await()
                    val adminWhatsApp = tickerDoc.getString("whatsapp1") ?: ""
                    //android.util.Log.d("FORGOT_FLOW", "Fetched adminWhatsApp=$adminWhatsApp")

                    if (adminWhatsApp.isNotBlank()) {
                        // Parse the message to extract newPassword
                        val messageParts = forgotPasswordState.message?.split("|") ?: listOf("")
                        val newPass = if (messageParts.size > 1) messageParts[1] else newPassword
                        val msg = "Hello Admin, I want to reset my password for mobile: $mobno. New password: $newPass"
                        val uri = Uri.parse("https://wa.me/+91$adminWhatsApp?text=${Uri.encode(msg)}")
                        val intent = Intent(Intent.ACTION_VIEW, uri)
                        context.startActivity(intent)
                        //android.util.Log.d("FORGOT_FLOW", "WhatsApp redirect triggered with msg=$msg")
                    } else {
                        Toast.makeText(context, "Admin WhatsApp number not found", Toast.LENGTH_LONG).show()
                       // android.util.Log.e("FORGOT_FLOW", "Admin WhatsApp number is blank")
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Error fetching admin data: ${e.message}", Toast.LENGTH_LONG).show()
                   // android.util.Log.e("FORGOT_FLOW", "Exception while fetching admin data", e)
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4F6F6))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                shape = RoundedCornerShape(40.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.elevatedCardElevation(20.dp)
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
                                        .background(Color(0xFF0A0A0A), RoundedCornerShape(4.dp))
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

            Spacer(modifier = Modifier.height(20.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                shape = RoundedCornerShape(40.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.elevatedCardElevation(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Forgot Password",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFABE0F),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    OutlinedTextField(
                        value = mobno,
                        onValueChange = {
                            if (it.length <= 10 && it.all { c -> c.isDigit() }) mobno = it
                        },
                        label = { Text("Enter Mobile Number") },
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(40.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("Enter New Password") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(40.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            if (mobno.length == 10 && newPassword.length >= 6) { // Added password length validation
                               // android.util.Log.d("FORGOT_FLOW", "Submit clicked with mobile=$mobno, newPassword=$newPassword")
                                viewModel.forgotPassword(mobno, newPassword) // Pass newPassword
                            } else {
                                Toast.makeText(
                                    context,
                                    "Enter a 10-digit mobile number and a password with at least 6 characters",
                                    Toast.LENGTH_LONG
                                ).show()
                              //  android.util.Log.e("FORGOT_FLOW", "Invalid input: mobno=$mobno, newPassword=$newPassword")
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(55.dp),
                        shape = RoundedCornerShape(40.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFABE0F))
                    ) {
                        Text(text = "Submit", color = Color.White, fontSize = 18.sp)
                    }
                }
            }
        }
    }
}
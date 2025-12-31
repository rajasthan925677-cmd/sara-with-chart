
package screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import firebase.SharedPrefHelper
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BankDetailsScreen(navController: NavController) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid

    var upiId by remember { mutableStateOf("") }
    var accountNo by remember { mutableStateOf("") }
    var ifsc by remember { mutableStateOf("") }
    var holder by remember { mutableStateOf("") }
    var bankName by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(userId) {
        if (userId != null) {
            // Load cached details first
            val cachedDetails = SharedPrefHelper.getBankDetails(context)
            upiId = cachedDetails["upiId"] ?: ""
            accountNo = cachedDetails["accountNo"] ?: ""
            ifsc = cachedDetails["ifsc"] ?: ""
            holder = cachedDetails["holder"] ?: ""
            bankName = cachedDetails["bankName"] ?: ""

            // Sync with Firestore in background
            try {
                val doc = db.collection("users").document(userId).get().await()
                if (doc.exists()) {
                    val newUpiId = doc.getString("upiId") ?: ""
                    val newAccountNo = doc.getString("accountNo") ?: ""
                    val newIfsc = doc.getString("ifsc") ?: ""
                    val newHolder = doc.getString("holder") ?: ""
                    val newBankName = doc.getString("bankName") ?: ""
                    upiId = newUpiId
                    accountNo = newAccountNo
                    ifsc = newIfsc
                    holder = newHolder
                    bankName = newBankName
                    // Update cache
                    SharedPrefHelper.setBankDetails(context, newUpiId, newBankName, newAccountNo, newIfsc, newHolder)
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to sync details: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bank Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            OutlinedTextField(
                value = upiId,
                onValueChange = { upiId = it },
                label = { Text("UPI ID *") },
                singleLine = true,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = bankName,
                onValueChange = { bankName = it },
                label = { Text("Bank Name") },
                singleLine = true,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = accountNo,
                onValueChange = { accountNo = it },
                label = { Text("Account Number") },
                singleLine = true,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = ifsc,
                onValueChange = { ifsc = it },
                label = { Text("IFSC Code") },
                singleLine = true,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = holder,
                onValueChange = { holder = it },
                label = { Text("Account Holder Name") },
                singleLine = true,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            )
            errorMessage?.let { Text(text = it, color = Color.Red) }
            Button(
                onClick = {
                    if (userId == null) {
                        Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (upiId.isBlank()) {
                        errorMessage = "Please enter UPI ID"
                        return@Button
                    }
                    errorMessage = null
                    val data = mapOf(
                        "upiId" to upiId,
                        "bankName" to bankName,
                        "accountNo" to accountNo,
                        "ifsc" to ifsc,
                        "holder" to holder
                    )
                    db.collection("users").document(userId)
                        .set(data, SetOptions.merge())
                        .addOnSuccessListener {
                            // Update cache
                            SharedPrefHelper.setBankDetails(context, upiId, bankName, accountNo, ifsc, holder)
                            Toast.makeText(context, "Details updated", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(context, "Update failed: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                },
                modifier = Modifier.width(200.dp).align(Alignment.CenterHorizontally),
                colors = ButtonDefaults.buttonColors(Color(0xFFFABE0F))
            ) {
                Text("Update Details")
            }
        }
    }
}

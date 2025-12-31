package screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.sara567.R
import firebase.SharedPrefHelper
import viewmodal.HomeTickerViewModel
import viewmodal.WalletViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WithdrawFundScreen(
    navController: NavController,
    walletViewModel: WalletViewModel = viewModel(),
   // tickerViewModel: HomeTickerViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current

    val tickerViewModel: HomeTickerViewModel = remember {
        HomeTickerViewModel(context = context.applicationContext)
    }

    val walletState by walletViewModel.walletState.collectAsState()
    val toastMessage by walletViewModel.toastMessage.collectAsState()
    val tickerState by tickerViewModel.state.collectAsState()

    var amount by remember { mutableStateOf("") }
    var amountError by remember { mutableStateOf<String?>(null) }
    var showWithdrawForm by remember { mutableStateOf(false) }

    // Get mobile number for UI display and submission
    val userMobile = SharedPrefHelper.getMobile(context) ?: "N/A"

    LaunchedEffect(Unit) {
        walletViewModel.loadUserData(context)
    }

    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            walletViewModel.onToastShown()
            if (it.contains("successfully", ignoreCase = true)) {
                showWithdrawForm = false
                amount = ""
                onNavigateBack()
            }
        }
    }

    if (showWithdrawForm) {
        WithdrawalFormDialog(
            onDismiss = { showWithdrawForm = false },
            onSubmit = { upiId, bankDetails ->
                walletViewModel.submitWithdrawRequest(
                    amount = amount.toDoubleOrNull() ?: 0.0,
                    upiId = upiId,
                    bankDetails = bankDetails,
                    mobile = userMobile // ✅ Pass mobile number
                )
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Withdraw Fund") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                shape = RoundedCornerShape(30.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.Start) {
                        Text(text = "Mobile Number", style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = userMobile,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                        Text(text = "Wallet Balance", style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = "₹ ${walletState.balance}",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(40.dp)) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it; amountError = null },
                        label = { Text("Enter Amount ₹") },
                        singleLine = true,
                        shape = RoundedCornerShape(20.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        isError = amountError != null,
                        supportingText = { amountError?.let { Text(it) } }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            val amountValue = amount.toDoubleOrNull()
                            if (amountValue == null) {
                                amountError = "Please enter a valid amount."
                            } else if (amountValue < 500) {
                                amountError = "Minimum withdrawal amount is ₹500."
                            } else if (amountValue > walletState.balance) {
                                amountError = "Amount cannot be more than your balance."
                            } else {
                                showWithdrawForm = true
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(Color(0xFFFABE0F))
                    ) {
                        Text("Withdraw Fund", fontSize = 16.sp)
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "For Withdrawal Related Query:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                    if (!tickerState.isLoading && tickerState.whatsapp1.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable {
                                val number = tickerState.whatsapp1.replace("+", "").replace(" ", "")
                                val url = "https://wa.me/$number"
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                context.startActivity(intent)
                            }
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.whatsapp),
                                contentDescription = "WhatsApp",
                                tint = Color(0xFF07F860),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = tickerState.whatsapp1,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF10EC20)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WithdrawalFormDialog(
    onDismiss: () -> Unit,
    onSubmit: (upiId: String, bankDetails: Map<String, String>) -> Unit
) {
    var upiId by remember { mutableStateOf("") }
    var holderName by remember { mutableStateOf("") }
    var accountNo by remember { mutableStateOf("") }
    var bankName by remember { mutableStateOf("") }
    var ifsc by remember { mutableStateOf("") }
    var upiIdError by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState())
            ) {
                Text("Enter Withdrawal Details", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = upiId,
                    onValueChange = { upiId = it; upiIdError = null },
                    label = { Text("UPI ID (Mandatory)") },
                    isError = upiIdError != null,
                    singleLine = true,
                    shape = RoundedCornerShape(20.dp),
                    supportingText = { upiIdError?.let { Text(it) } }
                )
                Text(
                    text = "OR (Optional)",
                    modifier = Modifier.padding(vertical = 8.dp),
                    style = MaterialTheme.typography.bodySmall
                )
                OutlinedTextField(
                    value = holderName,
                    onValueChange = { holderName = it },
                    label = { Text("A/C Holder Name") },
                    singleLine = true,
                    shape = RoundedCornerShape(20.dp)
                )
                OutlinedTextField(
                    value = accountNo,
                    onValueChange = { accountNo = it },
                    label = { Text("Bank Account Number") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(20.dp)
                )
                OutlinedTextField(
                    value = bankName,
                    onValueChange = { bankName = it },
                    label = { Text("Bank Name") },
                    singleLine = true,
                    shape = RoundedCornerShape(20.dp)
                )
                OutlinedTextField(
                    value = ifsc,
                    onValueChange = { ifsc = it },
                    label = { Text("IFSC Code") },
                    singleLine = true,
                    shape = RoundedCornerShape(20.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (upiId.isBlank()) {
                                upiIdError = "UPI ID is required."
                            } else {
                                val bankDetails = mapOf(
                                    "holderName" to holderName,
                                    "accountNo" to accountNo,
                                    "bankName" to bankName,
                                    "ifsc" to ifsc
                                )
                                onSubmit(upiId, bankDetails)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(Color(0xFF5D75E7))
                    ) {
                        Text("Submit Request")
                    }
                }
            }
        }
    }
}
package screens

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.sara567.R
import com.google.firebase.firestore.FirebaseFirestore
import firebase.PaymentUtils
import firebase.SharedPrefHelper
import kotlinx.coroutines.tasks.await
import viewmodal.HomeTickerViewModel
import viewmodal.WalletViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFundScreen(
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
    val adminUpiId by walletViewModel.adminUpiId.collectAsState()
    val tickerState by tickerViewModel.state.collectAsState()




    var amount by remember { mutableStateOf("") }
    var amountError by remember { mutableStateOf<String?>(null) }
    var upiStatus by remember { mutableStateOf<String?>(null) }
    var transactionId by remember { mutableStateOf<String?>(null) }
    var transactionRefId by remember { mutableStateOf<String?>(null) }
    var paymentDateTime by remember { mutableStateOf<String?>(null) }
    var showResponseForm by remember { mutableStateOf(false) }

    var QRpayAmount by remember { mutableStateOf("") }
    var QRpayAmountError by remember { mutableStateOf<String?>(null) }
    var QRpayUpi by remember { mutableStateOf<String?>(null) }
    var qrCodeBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var QRpayUpiStatus by remember { mutableStateOf("") }
    var QRpayTransactionId by remember { mutableStateOf("") }
    var QRpayTransactionIdError by remember { mutableStateOf<String?>(null) }
    var QRpayTransactionRefId by remember { mutableStateOf("") }
    var QRpayPaymentDate by remember { mutableStateOf("") }
    var QRpayPaymentTime by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var generatedQRAmount by remember { mutableStateOf("") }

    // Get mobile number for UI display and submission
    val userMobile = SharedPrefHelper.getMobile(context) ?: "N/A"

    LaunchedEffect(Unit) {
        walletViewModel.loadUserData(context)
        walletViewModel.loadAdminUpi()
        try {
            val snapshot = FirebaseFirestore.getInstance()
                .collection("admin_QRpay")
                .get()
                .await()
            val gpayDoc = snapshot.documents.firstOrNull()
            QRpayUpi = gpayDoc?.getString("QRpayUpi")
        } catch (e: Exception) {
            QRpayUpi = null
        }
    }

    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            walletViewModel.onToastShown()
            if (it.contains("successfully", ignoreCase = true)) {
                onNavigateBack()
            }
        }
    }

    val upiLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val now = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())
        paymentDateTime = now
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val response = result.data?.getStringExtra("response")
            if (!response.isNullOrEmpty()) {
                val paramMap = response.split("&").associate {
                    val parts = it.split("=")
                    if (parts.size == 2) parts[0] to parts[1] else parts[0] to ""
                }
                upiStatus = paramMap["Status"]?.lowercase() ?: "failed"
                transactionId = paramMap["txnId"] ?: "N/A"
                transactionRefId = paramMap["txnRef"] ?: "N/A"
            } else {
                upiStatus = "failed"
                transactionId = "N/A"
                transactionRefId = "N/A"
            }
        } else {
            upiStatus = "failed"
            transactionId = "N/A"
            transactionRefId = "N/A"
        }
        showResponseForm = upiStatus == "success"
        if (!showResponseForm) {
            Toast.makeText(context, "Payment failed", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Fund") },
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
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
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

            if (!showResponseForm) {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                    shape = RoundedCornerShape(40.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        OutlinedTextField(
                            value = amount,
                            onValueChange = { amount = it; amountError = null },
                            label = { Text("Enter Amount ₹") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.fillMaxWidth(),
                            isError = amountError != null,
                            supportingText = { amountError?.let { Text(it) } }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                val amountValue = amount.toDoubleOrNull()
                                if (amountValue == null || amountValue < 200) {
                                    amountError = "Please enter a valid amount of at least ₹200."
                                } else if (adminUpiId.isNullOrBlank()) {
                                    amountError = "Admin UPI not available. Try again later or pay with QR below."
                                } else {
                                    val uri = Uri.parse("upi://pay").buildUpon()
                                        .appendQueryParameter("pa", adminUpiId)
                                        .appendQueryParameter("pn", "Admin")
                                        .appendQueryParameter("am", amountValue.toString())
                                        .appendQueryParameter("cu", "INR")
                                        .appendQueryParameter("tn", "Fund add for $userMobile")
                                        .appendQueryParameter("tr", UUID.randomUUID().toString())
                                        .build()
                                    val intent = Intent(Intent.ACTION_VIEW, uri)
                                    val chooser = Intent.createChooser(intent, "Pay with")
                                    if (chooser.resolveActivity(context.packageManager) != null) {
                                        upiLauncher.launch(chooser)
                                    } else {
                                        Toast.makeText(context, "No UPI app found", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(Color(0xFFFABE0F))
                        ) {
                            Text("Add Fund", fontSize = 16.sp)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        if (!tickerState.isLoading && tickerState.whatsapp1.isNotEmpty()) {
                            Column(
                                modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier.fillMaxWidth().wrapContentHeight()
                                ) {
                                    Text(
                                        text = "For Add Fund Related Query:",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.Gray,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier.fillMaxWidth().wrapContentHeight().clickable {
                                        val number = tickerState.whatsapp1.replace("+", "").replace(" ", "")
                                        val url = "https://wa.me/$number"
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                        context.startActivity(intent)
                                    }
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.whatsapp),
                                        contentDescription = "WhatsApp",
                                        tint = Color(0xFF25D366),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = tickerState.whatsapp1,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF50E75B),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }

                ElevatedCard(
                    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                    shape = RoundedCornerShape(40.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Pay with QR Code",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = QRpayAmount,
                            onValueChange = { QRpayAmount = it; QRpayAmountError = null },
                            label = { Text("Enter Amount ₹") },
                            singleLine = true,
                            shape = RoundedCornerShape(20.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            isError = QRpayAmountError != null,
                            supportingText = { QRpayAmountError?.let { Text(it) } }
                        )
                        Spacer(modifier = Modifier.height(5.dp))
                        Button(
                            onClick = {
                                val amountValue = QRpayAmount.toDoubleOrNull()
                                if (amountValue == null || amountValue < 500) {
                                    QRpayAmountError = "Please enter a valid amount of at least ₹500."
                                } else if (QRpayUpi.isNullOrBlank()) {
                                    QRpayAmountError = "UPI not available. Try again later or pay with add fund above."
                                } else {
                                    generatedQRAmount = QRpayAmount
                                    QRpayUpiStatus = ""
                                    QRpayTransactionId = ""
                                    QRpayTransactionIdError = null
                                    QRpayTransactionRefId = ""
                                    QRpayPaymentDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
                                    QRpayPaymentTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
                                    qrCodeBitmap = PaymentUtils.createUpiQRCode(
                                        upiId = QRpayUpi!!,
                                        amount = amountValue
                                    )
                                    if (qrCodeBitmap == null) {
                                        QRpayAmountError = "Failed to generate QR code."
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(Color(0xFFFABE0F))
                        ) {
                            Text("Generate QR Code", fontSize = 16.sp)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        qrCodeBitmap?.let { bitmap ->
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "UPI QR Code",
                                modifier = Modifier.size(170.dp).padding(8.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Scan QR with any UPI app to make payment.After payment, submit these details (UTR mandatory)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                color = Color(0xFF32E583),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text(
                                text = "सबसे पहले इस QR का स्क्रीनशॉट ले , ओर अपने UPI ऐप से पेमेंट करे , पेमेंट के बाद UTR नंबर को नीचे भरो, ओर सबमिट करे",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                color = Color(0xFFEE8A06),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedTextField(
                                    value = generatedQRAmount,
                                    onValueChange = {},
                                    label = { Text("Amount") },
                                    readOnly = true,
                                    shape = RoundedCornerShape(20.dp),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Box {
                                    OutlinedTextField(
                                        value = QRpayUpiStatus,
                                        onValueChange = {},
                                        label = { Text("UPI Status") },
                                        readOnly = true,
                                        singleLine = true,
                                        shape = RoundedCornerShape(20.dp),
                                        modifier = Modifier.fillMaxWidth(),
                                        trailingIcon = {
                                            IconButton(onClick = { expanded = true }) {
                                                Icon(Icons.Filled.ArrowDropDown, contentDescription = "Select Status")
                                            }
                                        }
                                    )
                                    DropdownMenu(
                                        expanded = expanded,
                                        onDismissRequest = { expanded = false }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("Success") },
                                            onClick = { QRpayUpiStatus = "Success"; expanded = false }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Failed") },
                                            onClick = { QRpayUpiStatus = "Failed"; expanded = false }
                                        )
                                    }
                                }
                                OutlinedTextField(
                                    value = QRpayTransactionId,
                                    onValueChange = {
                                        QRpayTransactionId = it
                                        QRpayTransactionIdError = if (it.isBlank()) "Transaction ID is required" else null
                                    },
                                    label = { Text("Transaction ID or UTR *", color = Color(0xFFF53E07)) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(20.dp),
                                    modifier = Modifier.fillMaxWidth(),
                                    isError = QRpayTransactionIdError != null,
                                    supportingText = { QRpayTransactionIdError?.let { Text(it) } }
                                )
//                                OutlinedTextField(
//                                    value = QRpayTransactionRefId,
//                                    onValueChange = { QRpayTransactionRefId = it },
//                                    label = { Text("Transaction Ref ID") },
//                                    singleLine = true,
//                                    shape = RoundedCornerShape(20.dp),
//                                    modifier = Modifier.fillMaxWidth()
//                                )
                                OutlinedTextField(
                                    value = QRpayPaymentDate,
                                    onValueChange = {},
                                    label = { Text("Payment Date (dd/MM/yyyy)") },
                                    readOnly = true,
                                    singleLine = true,
                                    shape = RoundedCornerShape(20.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(
                                    value = QRpayPaymentTime,
                                    onValueChange = {},
                                    label = { Text("Payment Time (HH:mm:ss)") },
                                    readOnly = true,
                                    singleLine = true,
                                    shape = RoundedCornerShape(20.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = {
                                        if (QRpayUpiStatus.isBlank()) {
                                            Toast.makeText(context, "Please select UPI status", Toast.LENGTH_SHORT).show()
                                            return@Button
                                        }
                                        if (QRpayTransactionId.isBlank()) {
                                            QRpayTransactionIdError = "Transaction ID is required"
                                            return@Button
                                        }
                                        val amountDouble = generatedQRAmount.toDoubleOrNull() ?: 0.0
                                        walletViewModel.submitQRPayRequest(
                                            amount = amountDouble,
                                            upiStatus = QRpayUpiStatus,
                                            transactionId = QRpayTransactionId,
                                            transactionRefId = QRpayTransactionRefId,
                                            paymentDateTime = "$QRpayPaymentDate $QRpayPaymentTime".trim(),
                                            mobile = userMobile
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(Color(0xFFFABE0F))
                                ) {
                                    Text("Submit", fontSize = 16.sp)
                                }
                            }
                        }
                    }
                }
            } else {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                    shape = RoundedCornerShape(40.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = amount,
                            onValueChange = {},
                            label = { Text("Amount") },
                            readOnly = true,
                            singleLine = true,
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = upiStatus ?: "",
                            onValueChange = {},
                            label = { Text("Status") },
                            readOnly = true,
                            singleLine = true,
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = transactionId ?: "",
                            onValueChange = {},
                            label = { Text("Transaction ID") },
                            readOnly = true,
                            singleLine = true,
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = transactionRefId ?: "",
                            onValueChange = {},
                            label = { Text("Transaction Ref ID") },
                            readOnly = true,
                            singleLine = true,
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                val amountDouble = amount.toDoubleOrNull() ?: 0.0
                                walletViewModel.submitAddFundRequest(
                                    amount = amountDouble,
                                    upiStatus = upiStatus ?: "Failed",
                                    transactionId = transactionId ?: "N/A",
                                    transactionRefId = transactionRefId ?: "N/A",
                                    paymentDateTime = paymentDateTime ?: "",
                                    mobile = userMobile // ✅ Pass mobile number
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(Color(0xFF5D75E7))
                        ) {
                            Text("Submit", fontSize = 16.sp)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
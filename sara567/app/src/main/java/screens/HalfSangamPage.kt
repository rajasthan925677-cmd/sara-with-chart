package screens

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import firebase.SharedPrefHelper
import viewmodal.WalletViewModel
import viewmodel.BidViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HalfSangamScreen(
    navController: NavController,
    marketName: String,
    gameType: String = "Half Sangam",
    openTime: String,
    closeTime: String,
    bidViewModel: BidViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    walletViewModel: WalletViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val context = LocalContext.current
    val userId = SharedPrefHelper.getMobile(context) ?: ""
    val walletState by walletViewModel.walletState.collectAsState()
    val bidState by bidViewModel.bidState.collectAsState()

    // Load user data and bids
    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            walletViewModel.loadUserData(context)
            bidViewModel.loadUserBidsRealtime(context, gameType, marketName)
        }
    }

    // Cleanup listener
    DisposableEffect(Unit) {
        onDispose { walletViewModel.stopBalanceListener() }
    }

    // Check if game is closed
    val isGameClosed = bidViewModel.isGameClosed(closeTime)

    var digitInput by remember { mutableStateOf("") }
    var pannaInput by remember { mutableStateOf("") }
    var amountInput by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("OpenPanna+CloseDigit") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Toast for bid submission messages
    LaunchedEffect(bidState.message) {
        bidState.message?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            bidViewModel.resetMessage()
        }
    }

    // Filter bids for current date
    val currentDate = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH).format(Date())
    val filteredBids = bidState.bids.filter { bid -> bid.date == currentDate }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "$marketName - $gameType",
                        color = Color.DarkGray,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back" , tint = Color.DarkGray)
                    }
                },
                actions = {
                    Text(
                        text = "₹${walletState.balance}",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.DarkGray,
                        modifier = Modifier.padding(end = 12.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFEBECEF),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(12.dp)
        ) {

            Text(
                text = currentDate,
                style = MaterialTheme.typography.titleMedium, color = Color(0xFFFABE0F),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .background(Color(0xFFE9E9EE)),
                textAlign = TextAlign.Center
            )


            // Type Selection
            ElevatedCard(
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF6F7F8))
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text("Select Type", style = MaterialTheme.typography.titleMedium)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = selectedType == "OpenPanna+CloseDigit",
                            onClick = { selectedType = "OpenPanna+CloseDigit" }
                        )
                        Text("Open Pana + Close Digit (A)")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = selectedType == "ClosePanna+OpenDigit",
                            onClick = { selectedType = "ClosePanna+OpenDigit" }
                        )
                        Text("Close Pana + Open Digit (B)")
                    }
                }
            }

            // Input Section
            ElevatedCard(
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFBFCFD))
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = digitInput,
                        onValueChange = { if (it.all { ch -> ch.isDigit() } && it.length <= 1) digitInput = it },
                        label = { Text("Single Digit (0-9)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(40.dp)
                    )
                    OutlinedTextField(
                        value = pannaInput,
                        onValueChange = { if (it.all { ch -> ch.isDigit() } && it.length <= 3) pannaInput = it },
                        label = { Text("Pana (100-999)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(40.dp)
                    )
                    OutlinedTextField(
                        value = amountInput,
                        onValueChange = { if (it.all { ch -> ch.isDigit() }) amountInput = it },
                        label = { Text("Enter Amount") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(40.dp)
                    )

                    // Error message
                    errorMessage?.let {
                        Text(
                            text = it,
                            color = Color.Red,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }

                    Button(
                        onClick = {
                            val digit = digitInput.toIntOrNull()
                            val panna = pannaInput.toIntOrNull()
                            val amount = amountInput.toIntOrNull()

                            errorMessage = when {
                                isGameClosed -> "Bidding closed for this session"
                                userId.isEmpty() -> "Please log in to place bids"
                                digit == null || digit !in 0..9 -> "Invalid Digit (0-9)"
                                panna == null || panna !in 100..999 -> "Invalid Panna (100-999)"
                                amount == null || amount < 10 -> "Minimum bid ₹10"
                                amount > walletState.balance -> "Insufficient Balance"
                                else -> null
                            }

                            if (errorMessage == null) {
                                bidViewModel.submitSangamBids(
                                    context = context,
                                    gameId = marketName,
                                    gameType = gameType,
                                    session = selectedType,
                                    bidMap = mapOf(Pair(digit!!, panna!!) to amount!!),
                                    walletViewModel = walletViewModel
                                ) { success ->
                                    if (success) {
                                        digitInput = ""
                                        pannaInput = ""
                                        amountInput = ""
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFABE0F)),
                        enabled = !isGameClosed && userId.isNotEmpty()
                    ) {
                        Text(
                            text = if (isGameClosed) "Half Sangam Closed" else "Submit Bid",
                            color = Color.White
                        )
                    }
                }
            }

            // Submitted Bids
            Text(
                text = "Submitted Bids (Today): ${filteredBids.size}",
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (filteredBids.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    items(filteredBids, key = { bid -> bid.id }) { bid ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Type: ${bid.session}")
                                    Text("₹${bid.bidAmount}", fontWeight = FontWeight.Bold , color = Color(0xFFFABE0F))

                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    if (bid.singleDigit != null) Text("Digit: ${bid.singleDigit}")
                                    if (bid.panaDigit != null) Text("Panna: ${bid.panaDigit}")
                                    Text(bid.status, color = Color.Gray)
                                    Text("+₹ ${bid.payoutAmount}", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            } else {
                Text(
                    text = "No bids for today",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Gray,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp), textAlign = TextAlign.Center
                )
            }
        }
    }
}
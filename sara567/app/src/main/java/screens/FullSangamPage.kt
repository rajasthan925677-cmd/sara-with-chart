package screens

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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
fun FullSangamScreen(
    navController: NavController,
    marketName: String,
    gameType: String = "Full Sangam",
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

    var openPana by remember { mutableStateOf("") }
    var closePana by remember { mutableStateOf("") }
    var bidAmount by remember { mutableStateOf("") }
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
                    Column {
                        Text(text = marketName, fontWeight = FontWeight.Bold, fontSize = 20.sp
                            ,color = Color.DarkGray)
                        Text(text = gameType, fontSize = 18.sp, fontWeight = FontWeight.Bold,
                            color = Color.DarkGray)
                    }
                },
                actions = {
                    Text(
                        text = "₹${walletState.balance}",
                        color = Color.DarkGray,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.DarkGray)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFEBECEF))
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
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


            // Input Fields Card
            Card(
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFBFBFC)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = openPana,
                        onValueChange = { value ->
                            if (value.length <= 3 && value.all { it.isDigit() }) openPana = value
                        },
                        label = { Text("Enter Open Pana (100-999)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(40.dp)
                    )
                    OutlinedTextField(
                        value = closePana,
                        onValueChange = { value ->
                            if (value.length <= 3 && value.all { it.isDigit() }) closePana = value
                        },
                        label = { Text("Enter Close Pana (100-999)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(40.dp)
                    )
                    OutlinedTextField(
                        value = bidAmount,
                        onValueChange = { value ->
                            if (value.all { it.isDigit() }) bidAmount = value
                        },
                        label = { Text("Enter Amount") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
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

                    // Submit Button
                    Button(
                        onClick = {
                            val openVal = openPana.toIntOrNull()
                            val closeVal = closePana.toIntOrNull()
                            val amountVal = bidAmount.toIntOrNull()

                            errorMessage = when {
                                isGameClosed -> "Bidding closed for this session"
                                userId.isEmpty() -> "Please log in to place bids"
                                openVal == null || openVal !in 100..999 -> "Invalid Open Pana (100-999)"
                                closeVal == null || closeVal !in 100..999 -> "Invalid Close Pana (100-999)"
                                amountVal == null || amountVal < 10 -> "Minimum bid ₹10"
                                amountVal > walletState.balance -> "Insufficient Balance"
                                else -> null
                            }

                            if (errorMessage == null) {
                                bidViewModel.submitSangamBids(
                                    context = context,
                                    gameId = marketName,
                                    gameType = gameType,
                                    session = "open",
                                    bidMap = mapOf(Pair(openVal!!, closeVal!!) to amountVal!!),
                                    walletViewModel = walletViewModel
                                ) { success ->
                                    if (success) {
                                        openPana = ""
                                        closePana = ""
                                        bidAmount = ""
                                    }
                                }
                            }
                        },
                        enabled = !isGameClosed && userId.isNotEmpty(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(40.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFABE0F))
                    ) {
                        Text(
                            text = if (isGameClosed) "Full Sangam Closed" else "Submit Bid",
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Submitted Bids
            Text(
                text = "Submitted Bids (Today): ${filteredBids.size}",
                fontSize = 15.sp,
                color = Color.Black,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (filteredBids.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(filteredBids, key = { bid -> bid.id }) { bid ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = "Open Pana: ${bid.openPana}", fontSize = 16.sp)
                                    Text(text = "Close Pana: ${bid.closePana}", fontSize = 16.sp)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = "₹${bid.bidAmount}", fontSize = 16.sp, color = Color(0xFFFABE0F))
                                    Text(text = bid.status, fontSize = 16.sp, color = Color.Gray)
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
                    modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center
                )
            }
        }
    }
}
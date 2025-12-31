package screens

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
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
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
fun JodiScreen(
    navController: NavController,
    marketName: String,
    gameType: String,
    openTime: String,
    closeTime: String,
    viewModel: BidViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    walletViewModel: WalletViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val context = LocalContext.current
    val userId = SharedPrefHelper.getMobile(context) ?: ""
    val walletState by walletViewModel.walletState.collectAsState()
    val bidState by viewModel.bidState.collectAsState()

    // Load user data and balance
    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            walletViewModel.loadUserData(context)
            viewModel.loadUserBidsRealtime(context, gameType, marketName)
        }
    }

    // Cleanup listener
    DisposableEffect(Unit) {
        onDispose { walletViewModel.stopBalanceListener() }
    }

    // Check if game is closed
    val isGameClosed = viewModel.isGameClosed(closeTime)

    val jodiAmounts = remember { mutableStateMapOf<Int, String>() }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Toast for bid submission messages
    LaunchedEffect(bidState.message) {
        bidState.message?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            viewModel.resetMessage()
        }
    }

    // Filter bids for current date
    val currentDate = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH).format(Date())
    val filteredBids = bidState.bids.filter { bid -> bid.date == currentDate }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("$marketName - $gameType", fontSize = 18.sp,  color = Color.DarkGray, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Text(
                        text = "₹${walletState.balance}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFEBECEF))
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(8.dp)
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

            // Grid 00–99
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(1.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier.weight(1f)
            ) {
                items((0..99).toList()) { jodi ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFABE0F)),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(0.dp)
                                .fillMaxWidth(),
                            verticalAlignment =  Alignment.CenterVertically
                        ) {
                            Text(
                                text = "   %02d".format(jodi),
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFDFDFD) , modifier = Modifier.weight(0.5f)
                            )
                            OutlinedTextField(
                                value = jodiAmounts[jodi] ?: "",
                                onValueChange = { new ->
                                    if (new.isEmpty() || new.all { it.isDigit() }) {
                                        jodiAmounts[jodi] = new
                                    }
                                },
                                placeholder = { Text("₹", color = Color.Gray) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.height(52.dp).weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color.White,
                                    unfocusedBorderColor = Color.White,
                                    cursorColor = Color(0xFFFABE0F),


                                    // ⭐⭐ BACKGROUND COLOR YAHAN SE SET HOTA HAI ⭐⭐
                                    focusedContainerColor = Color.White,         // जब user type कर रहा हो
                                    unfocusedContainerColor = Color.White        // normal background color
                                )
                            )


                        }
                    }
                }
            }

            // Validate
            val validBids = jodiAmounts
                .mapNotNull { (jodi, amtStr) -> amtStr.toIntOrNull()?.let { jodi to it } }
                .filter { it.second >= 10 }
            val totalBidAmount = validBids.sumOf { it.second }

            // Error checks
            errorMessage = when {
                isGameClosed -> "Bidding closed for this session"
                validBids.isEmpty() && jodiAmounts.isNotEmpty() -> "Minimum bid ₹10"
                totalBidAmount > walletState.balance -> "Insufficient wallet balance"
                userId.isEmpty() -> "Please log in to place bids"
                else -> null
            }

            // Error message UI
            errorMessage?.let {
                Text(
                    text = it,
                    color = Color.Red,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            // Submit button
            Button(
                onClick = {
                    viewModel.submitBids(
                        context,
                        marketName,
                        gameType,
                        "Open",
                        validBids.toMap(),
                        walletViewModel
                    ) { success ->
                        if (success) {
                            jodiAmounts.clear()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFABE0F)),
                enabled = validBids.isNotEmpty() && errorMessage == null
            ) {
                Text(
                    if (totalBidAmount > 0) "Submit Bid (₹$totalBidAmount)" else "Submit Bid",
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Bid History
            Text(
                "Submitted Bids (Today): ${filteredBids.size}",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp), fontSize = 15.sp
            )

            if (filteredBids.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No bids for today", fontSize = 16.sp, color = Color.Gray)
                }
            } else {
                androidx.compose.foundation.lazy.LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.4f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(filteredBids, key = { bid -> bid.id }) { bid ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(" %02d (${bid.session})".format(bid.bidDigit), fontWeight = FontWeight.Bold)
                                Text("₹${bid.bidAmount}", color = Color(0xFFFABE0F))
                                // Text(bid.time, fontSize = 12.sp, color = Color.Gray)
                                Text(bid.status, fontSize = 12.sp, color = Color.Gray)
                                Text("+₹ ${bid.payoutAmount}", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
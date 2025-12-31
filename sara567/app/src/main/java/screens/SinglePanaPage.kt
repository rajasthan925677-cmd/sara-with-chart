package screens

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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

private const val MIN_AMOUNT = 10
private const val GAME_TYPE = "Single Pana"

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SinglePannaScreen(
    navController: NavController,
    marketName: String,
    gameType: String = GAME_TYPE,
    openTime: String,
    closeTime: String,
    singlePannaList: List<String> = generateSinglePanna(),
    viewModel: BidViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    walletViewModel: WalletViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val context = LocalContext.current
    val userId = SharedPrefHelper.getMobile(context) ?: ""
    val walletState by walletViewModel.walletState.collectAsState()
    val bidState by viewModel.bidState.collectAsState()

    // Load user data and bids
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

    var expanded by remember { mutableStateOf(false) }
    var selectedType by remember { mutableStateOf(if (viewModel.isGameClosed(openTime)) "Close" else "Open") }
    val bidAmounts = remember { mutableStateMapOf<String, String>() }
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
                title = { Text("$marketName - $gameType", color = Color.DarkGray,fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Text(
                        "₹${walletState.balance}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color.DarkGray,
                        modifier = Modifier.padding(end = 12.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFEBECEF))
            )
        },
        bottomBar = {
            Column {
                val validBids = bidAmounts.mapNotNull { (panna, amountStr) ->
                    amountStr.toIntOrNull()?.takeIf { it >= MIN_AMOUNT }?.let { panna to it }
                }.toMap()
                val totalBidAmount = validBids.values.sum()

                // Error checks
                errorMessage = when {
                    isGameClosed -> "Bidding closed for this session"
                    userId.isEmpty() -> "Please log in to place bids"
                    validBids.isEmpty() && bidAmounts.isNotEmpty() -> "Minimum bid ₹$MIN_AMOUNT"
                    totalBidAmount > walletState.balance -> "Insufficient Balance"
                    else -> null
                }

                // Error message UI
                errorMessage?.let {
                    Text(
                        text = it,
                        color = Color.Red,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }

                Button(
                    onClick = {
                        viewModel.submitBids(
                            context,
                            marketName,
                            gameType,
                            selectedType,
                            validBids.mapKeys { it.key.toIntOrNull() ?: 0 },
                            walletViewModel
                        ) { success ->
                            if (success) {
                                bidAmounts.clear()
                            }
                        }
                    },
                    enabled = validBids.isNotEmpty() && errorMessage == null,
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFABE0F))
                ) {
                    Text(
                        if (totalBidAmount > 0) "Submit Bid (₹$totalBidAmount)" else "Submit Bid",
                        color = Color.White
                    )
                }

                if (filteredBids.isNotEmpty()) {
                    Text(
                        "Submitted Bids (Today): ${filteredBids.size}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 12.dp, bottom = 6.dp)
                    )
                    androidx.compose.foundation.lazy.LazyColumn(
                        modifier = Modifier.fillMaxWidth().height(150.dp)
                            .padding(horizontal = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(filteredBids, key = { bid -> bid.id }) { bid ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("${bid.bidDigit} (${bid.session})", fontWeight = FontWeight.Bold)
                                   // Text("${bid.time}", color = Color.Gray)
                                    Text("₹${bid.bidAmount}", color = Color(0xFFFABE0F))
                                    Text("${bid.status}", color = Color.Gray)
                                    Text("+₹ ${bid.payoutAmount}", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                } else {
                    Text(
                        "No bids for today",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Gray,
                        modifier = Modifier.fillMaxWidth().padding(start = 12.dp, bottom = 26.dp), textAlign = TextAlign.Center
                    )
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            Text(
                SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date()),
                modifier = Modifier.fillMaxWidth().height(30.dp).background(Color(0xFFFAF9F9)),
                color = Color(0xFFFABE0F),
                fontSize = 20.sp,
                textAlign = TextAlign.Center
            )

            // Session selector
            Box(
                modifier = Modifier.fillMaxWidth().background(Color(0xFFE0E0E0)).padding(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { expanded = true }
                ) {
                    Text(selectedType, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Toggle Dropdown")
                }

                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    val sessionOptions = if (viewModel.isGameClosed(openTime)) listOf("Close") else listOf("Open", "Close")
                    sessionOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = { selectedType = option; expanded = false }
                        )
                    }
                }
            }

            // Panna list
            LazyColumn(modifier = Modifier.weight(1f).padding(10.dp)) {
                val grouped = singlePannaList.groupBy { panna ->
                    panna.map { it.toString().toInt() }.sum() % 10
                }.toSortedMap()

                grouped.forEach { (sum, pannaList) ->
                    item {
                        Text(
                            " $sum ",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().background(Color(0xFFFABE0F)).padding(8.dp)
                        )
                    }
                    items(pannaList.chunked(2)) { pair ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 15.dp),
                            horizontalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            pair.forEach { panna ->
                                PannaInputCard(
                                    panna = panna,
                                    amount = bidAmounts[panna] ?: "",
                                    onAmountChange = { newAmount ->
                                        if (newAmount.isEmpty() || newAmount.all { it.isDigit() })
                                            bidAmounts[panna] = newAmount
                                    }
                                )
                            }
                            if (pair.size == 1) Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RowScope.PannaInputCard(panna: String, amount: String, onAmountChange: (String) -> Unit) {
    Card(
        modifier = Modifier.weight(1f),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFABE0F)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(panna, fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFFFFFE),
                modifier = Modifier.weight(0.5f))
            val isError = amount.isNotEmpty() && (amount.toIntOrNull() ?: 0) < MIN_AMOUNT

            OutlinedTextField(
                value = amount,
                onValueChange = onAmountChange,
               // label = { Text("₹") },
                placeholder = { Text("₹", color = Color.Gray) },
                singleLine = true,
                isError = isError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.height(53.dp).weight(1f),
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

private fun normalizePanna(i: Int, j: Int, k: Int): String {
    val digits = listOf(i, j, k).sorted()
    return if (0 in digits) {
        val nonZero = digits.filter { it != 0 }
        (nonZero + 0).joinToString("")
    } else digits.joinToString("")
}

private fun generateSinglePanna(): List<String> {
    val set = mutableSetOf<String>()
    for (i in 0..9) for (j in 0..9) for (k in 0..9) {
        if (i != j && j != k && i != k) {
            val panna = normalizePanna(i, j, k)
            if (!panna.startsWith("0")) set.add(panna)
        }
    }
    return set.sorted()
}
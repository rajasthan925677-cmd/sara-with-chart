package screens

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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

private const val MIN_DOUBLE_BID = 10
private const val GAME_TYPE = "Double Pana"

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoublePanaScreen(
    navController: NavController,
    marketName: String,
    gameType: String = GAME_TYPE,
    openTime: String,
    closeTime: String,
    doublePanaList: List<String> = generateDoublePana(),
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

    var selectedType by remember { mutableStateOf(if (bidViewModel.isGameClosed(openTime)) "Close" else "Open") }
    var expanded by remember { mutableStateOf(false) }
    val bidAmounts = remember { mutableStateMapOf<String, String>() }
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
                title = { Text("$marketName - $gameType", fontSize = 18.sp, fontWeight = FontWeight.Bold , color = Color.DarkGray) },
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
            Column(modifier = Modifier.fillMaxWidth()) {
                val validBids = bidAmounts
                    .mapNotNull { (p, v) -> v.toIntOrNull()?.let { p to it } }
                    .filter { it.second >= MIN_DOUBLE_BID }

                val totalBidAmount = validBids.sumOf { it.second }

                // Error checks
                errorMessage = when {
                    isGameClosed -> "Bidding closed for this session"
                    userId.isEmpty() -> "Please log in to place bids"
                    validBids.isEmpty() && bidAmounts.isNotEmpty() -> "Minimum bid ₹$MIN_DOUBLE_BID"
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
                        bidViewModel.submitBids(
                            context,
                            marketName,
                            gameType,
                            selectedType,
                            validBids.associate { it.first.toInt() to it.second },
                            walletViewModel
                        ) { success ->
                            if (success) {
                                validBids.forEach { bidAmounts[it.first] = "" }
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

//                Text(
//                    text = "Total Bids: ${filteredBids.size}",
//                    fontSize = 14.sp,
//                    color = Color.Gray,
//                    modifier = Modifier.padding(start = 12.dp, bottom = 6.dp)
//                )

                if (filteredBids.isNotEmpty()) {
                    Text(
                        text = "Submitted Bids (Today): ${filteredBids.size}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 12.dp, bottom = 6.dp)
                    )
                    androidx.compose.foundation.lazy.LazyColumn(
                        modifier = Modifier.fillMaxWidth().height(150.dp).padding(horizontal = 12.dp),
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
                                    //Text("${bid.time}", color = Color.Gray)
                                    Text("₹${bid.bidAmount}", color = Color(0xFFFABE0F))
                                    Text("${bid.status}", color = Color.Gray)
                                    Text("+₹ ${bid.payoutAmount}", fontWeight = FontWeight.Bold)
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
                        modifier = Modifier.fillMaxWidth().padding(start = 12.dp, bottom = 16.dp), textAlign =  TextAlign.Center
                    )
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            Text(
                text = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date()),
                modifier = Modifier.fillMaxWidth().height(30.dp).background(Color(0xFFFAFAFC)),
                color = Color(0xFFFABE0F),
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )

            Box(
                modifier = Modifier.fillMaxWidth().background(Color(0xFFE0E0E0)).padding(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { expanded = true }
                ) {
                    Text(selectedType, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    val sessionOptions = if (bidViewModel.isGameClosed(openTime)) listOf("Close") else listOf("Open", "Close")
                    sessionOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option, fontWeight = FontWeight.Bold) },
                            onClick = { selectedType = option; expanded = false }
                        )
                    }
                }
            }

            val grouped = doublePanaList.groupBy { panna ->
                panna.map { it.digitToInt() }.sum() % 10
            }.toSortedMap()

            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 12.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                grouped.forEach { (sum, listPana) ->
                    item {
                        Text(
                            text = " $sum",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.fillMaxWidth()
                                .background(Color(0xFFFABE0F)).padding(vertical = 8.dp, horizontal = 16.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                    items(listPana.chunked(2)) { pair ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 15.dp),
                            horizontalArrangement = Arrangement.spacedBy(20.dp),

                        ) {
                            pair.forEach { panna ->
                                Card(
                                    modifier = Modifier.weight(1f),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFABE0F)),
                                    elevation = CardDefaults.cardElevation(4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(0.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = panna,
                                            fontSize = 17.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFFDFDFC),
                                            modifier = Modifier.weight(0.5f)
                                        )
                                        val currentVal = bidAmounts[panna] ?: ""
                                        val isError = currentVal.toIntOrNull()?.let { it in 1 until MIN_DOUBLE_BID } == true
                                        OutlinedTextField(
                                            value = currentVal,
                                            onValueChange = { new -> if (new.isEmpty() || new.all { it.isDigit() }) bidAmounts[panna] = new },
                                            placeholder = { Text("₹", fontSize = 14.sp) },
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
                            if (pair.size == 1) Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

private fun generateDoublePana(): List<String> {
    val set = mutableSetOf<String>()
    for (i in 1..9) {
        for (j in 0..9) {
            for (k in 0..9) {
                if ((i == j && i != k) || (i == k && i != j) || (j == k && j != i)) {
                    val digits = listOf(i, j, k).sortedBy { if (it == 0) 10 else it }
                    set.add(digits.joinToString(""))
                }
            }
        }
    }
    return set.sorted()
}
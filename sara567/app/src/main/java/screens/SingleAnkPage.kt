package screens

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
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
import java.util.Calendar
import java.util.Date
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SingleAnkScreen(
    navController: NavController,
    viewModel: BidViewModel,
    walletViewModel: WalletViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    marketName: String,
    gameType: String,
    openTime: String,
    closeTime: String
) {
    val context = LocalContext.current
    val walletState by walletViewModel.walletState.collectAsState()
    val bidState by viewModel.bidState.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current // Access here in Composable scope

    // Load user data and balance
    LaunchedEffect(Unit) {
        walletViewModel.loadUserData(context)
    }

    // Fetch logged-in mobile (UID) from SharedPrefHelper
    val userId = SharedPrefHelper.getMobile(context) ?: ""

    // Check if game is closed
    val isGameClosed = viewModel.isGameClosed(closeTime)

    var selectedType by remember { mutableStateOf("Open") }
    var expanded by remember { mutableStateOf(false) }
    val bidAmounts = remember { mutableStateMapOf<Int, String>() }

    // Listener cleanup
    DisposableEffect(Unit) {
        val listener = viewModel.loadUserBidsRealtime(context, gameType, marketName)
        onDispose {
            listener.remove()
        }
    }

    // Toast for bid submission messages
    LaunchedEffect(bidState.message) {
        bidState.message?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            viewModel.resetMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("$marketName - $gameType", color = Color.DarkGray, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Text(
                        "₹${walletState.balance}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.DarkGray,
                        modifier = Modifier.padding(end = 12.dp)
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
        ) {
            // Type selector + date
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Type:", fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(8.dp))

                // Real-time update for time
                var now by remember { mutableStateOf(Calendar.getInstance()) }

                LaunchedEffect(Unit) {
                    while (true) {
                        now = Calendar.getInstance()
                        kotlinx.coroutines.delay(1000L)
                    }
                }

                // Dropdown logic
                val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
                val today = Calendar.getInstance()

                val openCal = Calendar.getInstance().apply {
                    time = sdf.parse(openTime) ?: Date()
                    set(Calendar.YEAR, today.get(Calendar.YEAR))
                    set(Calendar.MONTH, today.get(Calendar.MONTH))
                    set(Calendar.DAY_OF_MONTH, today.get(Calendar.DAY_OF_MONTH))
                }

                val isOpenSessionClosed = now.after(openCal)

                // Update selectedType based on time
                LaunchedEffect(isOpenSessionClosed) {
                    selectedType = if (isOpenSessionClosed) "Close" else "Open"
                }

                Box {
                    Button(onClick = { expanded = true }) {
                        Text(selectedType)
                        Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
                    }

                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        if (!isOpenSessionClosed) {
                            DropdownMenuItem(
                                text = { Text("Open") },
                                onClick = { selectedType = "Open"; expanded = false }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("Close") },
                            onClick = { selectedType = "Close"; expanded = false }
                        )
                    }
                }

                Spacer(Modifier.width(16.dp))
                val currentDate = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
                Text(
                    "📅$currentDate",
                    Modifier.padding(start = 30.dp),
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFFABE0F)
                )
            }

            // Fixed 2 Column Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(30.dp),
                horizontalArrangement = Arrangement.spacedBy(15.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(10) { digit ->
                    Card(
                        modifier = Modifier
                            .weight(1f),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFABE0F)),
                        elevation = CardDefaults.cardElevation(6.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(0.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                digit.toString(),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFCFBFB),
                                modifier = Modifier.weight(0.5f).padding(8.dp)
                            )
                            OutlinedTextField(
                                value = bidAmounts[digit] ?: "",
                                onValueChange = { new ->
                                    if (new.isEmpty() || new.all { it.isDigit() }) {
                                        bidAmounts[digit] = new
                                    }
                                },
                                placeholder = { Text("₹", fontSize = 14.sp) },

                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        keyboardController?.hide() // Use the Composable-scoped keyboardController
                                    }
                                ),
                                modifier = Modifier
                                    .width(120.dp)
                                    .height(53.dp).weight(1f),
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

            // Validation logic
            val rawBids = bidAmounts.mapNotNull { (digit, amtStr) ->
                amtStr.toIntOrNull()?.let { digit to it }
            }.toMap()

            val invalidBids = rawBids.filter { it.value < 10 }
            val validBids = rawBids.filter { it.value >= 10 }
            val totalBidAmount = validBids.values.sum()

            var errorMessage by remember { mutableStateOf<String?>(null) }

            // Error checks
            errorMessage = when {
                isGameClosed -> "Bidding closed for this session"
                invalidBids.isNotEmpty() -> "Minimum bid ₹10"
                totalBidAmount > walletState.balance -> "Insufficient Balance"
                else -> null
            }

            // Error message UI
            errorMessage?.let {
                Text(
                    text = it,
                    color = Color.Red,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            // Submit button
            Button(
                onClick = {
                    viewModel.submitBids(
                        context,
                        marketName,
                        gameType,
                        selectedType,
                        validBids,
                        walletViewModel
                    ) { success ->
                        if (success) {
                            bidAmounts.clear()
                        }
                    }
                },
                enabled = validBids.isNotEmpty() && errorMessage == null,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = ButtonDefaults.buttonColors(Color(0xFFFABE0F))
            ) { Text("Submit Bids", color = Color.White) }

            // Filter bids for current date
            val currentDate = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH).format(Date())
            val filteredBids = bidState.bids.filter { bid ->
                bid.date == currentDate
            }

            // Show submitted bids
            if (filteredBids.isNotEmpty()) {
                Text(
                    "Submitted Bids (Today): ${filteredBids.size}",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 12.dp, top = 8.dp),
                    fontSize = 15.sp
                )
                androidx.compose.foundation.lazy.LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                        .weight(0.6f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredBids, key = { bid -> bid.id }) { bid ->
                        val timeStr =
                            SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(bid.timestamp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Ank:${bid.bidDigit} (${bid.session})", fontWeight = FontWeight.Bold)
                                Text("₹${bid.bidAmount}", color = Color(0xFFFABE0F))
                               // Text("${bid.session}", fontSize = 12.sp, color = Color.Gray)
                                //Text(timeStr, fontSize = 12.sp, color = Color.Gray)
                                Text("${bid.status}", fontSize = 12.sp, color = Color.Gray)
                                Text("+₹ ${bid.payoutAmount}", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                Text(
                    "No bids for today",
                    fontWeight = FontWeight.Medium,
                    color = Color.Gray,
                    modifier = Modifier.fillMaxWidth().padding(start = 12.dp, top = 8.dp), textAlign = TextAlign.Center
                )
            }
        }
    }
}
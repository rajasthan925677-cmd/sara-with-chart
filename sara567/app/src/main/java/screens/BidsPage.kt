package screens

import android.app.DatePickerDialog
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import firebase.SharedPrefHelper
import viewmodal.HomeViewModel
import viewmodel.BidViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BidHistoryScreen(
    navController: NavController,
    bidViewModel: BidViewModel,
    homeViewModel: HomeViewModel
) {
    val homeState by homeViewModel.homeState.collectAsState()
    val bidState by bidViewModel.bidState.collectAsState()
    val filteredBids by bidViewModel.filteredBids.collectAsState()

    val context = LocalContext.current
    val userId = SharedPrefHelper.getUID(context) ?: ""

    var selectedGame by remember { mutableStateOf("") }
    var expandedGame by remember { mutableStateOf(false) }

    var selectedType by remember { mutableStateOf("") }
    var expandedType by remember { mutableStateOf(false) }

    var selectedDate by remember { mutableStateOf("") }

    val gameTypes = listOf("Single Ank", "Jodi", "Single Pana", "Double Pana", "Triple Pana", "Half Sangam", "Full Sangam")

    // Load bids for the user
    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            bidViewModel.loadUserBidsRealtime(context, "", "") // Empty gameType and gameId to load all bids
        }
    }

    // Sort bids by date (newest to oldest)
    val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
    val sortedBids = remember(filteredBids) {
        filteredBids.sortedByDescending { bid ->
            try {
                dateFormatter.parse(bid.date)?.time ?: 0L
            } catch (e: Exception) {
                0L // Fallback to 0 if parsing fails
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bid History", fontWeight = FontWeight.Bold, color = Color.DarkGray) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.DarkGray)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFFCFBFA))
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(12.dp)
        ) {
            // -------- Filter Card --------
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFA1A1A1))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // -------- Row: Game + Type Dropdowns --------
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Game Dropdown
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(Color.White)
                                .clickable { expandedGame = true }
                                .padding(12.dp),

                        ) {
                            Text(
                                text = if (selectedGame.isEmpty()) "Select Game" else selectedGame,
                                fontSize = 16.sp, maxLines = 1,
                            )
                            Icon(
                                imageVector = Icons.Filled.ArrowDropDown,
                                contentDescription = null,
                                modifier = Modifier.align(Alignment.CenterEnd),

                            )
                            DropdownMenu(
                                expanded = expandedGame,
                                onDismissRequest = { expandedGame = false },
                                modifier = Modifier.width(200.dp),
                                shape = RoundedCornerShape(20.dp),
                            ) {
                                homeState.games.forEach { game ->
                                    DropdownMenuItem(
                                        text = { Text(game.gameName)  },
                                        onClick = {
                                            selectedGame = game.gameName
                                            expandedGame = false
                                        }
                                    )
                                }
                            }
                        }

                        // Type Dropdown
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(Color.White)
                                .clickable { expandedType = true }
                                .padding(12.dp),
                        ) {
                            Text(
                                text = if (selectedType.isEmpty()) "Select Type" else selectedType,
                                fontSize = 16.sp
                            )
                            Icon(
                                imageVector = Icons.Filled.ArrowDropDown,
                                contentDescription = null,
                                modifier = Modifier.align(Alignment.CenterEnd)
                            )
                            DropdownMenu(
                                expanded = expandedType,
                                onDismissRequest = { expandedType = false },
                                modifier = Modifier.width(200.dp),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                gameTypes.forEach { type ->
                                    DropdownMenuItem(
                                        text = { Text(type) },
                                        onClick = {
                                            selectedType = type
                                            expandedType = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // -------- Row: Date Picker + Search Button --------
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Date Picker
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(Color.White)
                                .clickable {
                                    val today = Calendar.getInstance()
                                    DatePickerDialog(
                                        context,
                                        { _, year, month, dayOfMonth ->
                                            val formatter = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
                                            val cal = Calendar.getInstance().apply { set(year, month, dayOfMonth) }
                                            selectedDate = formatter.format(cal.time)
                                        },
                                        today.get(Calendar.YEAR),
                                        today.get(Calendar.MONTH),
                                        today.get(Calendar.DAY_OF_MONTH)
                                    ).show()
                                }
                                .padding(12.dp)
                        ) {
                            Text(
                                text = if (selectedDate.isEmpty()) "Select Date" else selectedDate,
                                fontSize = 16.sp
                            )
                            Icon(
                                imageVector = Icons.Filled.ArrowDropDown,
                                contentDescription = null,
                                modifier = Modifier.align(Alignment.CenterEnd)
                            )
                        }

                        // Search Button
                        Button(
                            onClick = {
                                if (userId.isNotEmpty()) {
                                    bidViewModel.loadFilteredBids(context, selectedGame, selectedType, selectedDate)
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFABE0F))
                        ) {
                            Text("Search", color = Color.White, fontSize = 16.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // -------- Bids List --------
            if (sortedBids.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No bids found",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.Gray
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(sortedBids, key = { bid -> bid.id }) { bid ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE1E0D2)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Left Column
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "Game: ${bid.gameId}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                    Text(
                                        text = "Digit: ${listOfNotNull(bid.bidDigit, bid.singleDigit, bid.panaDigit, bid.openPana, bid.closePana).joinToString(", ") { it.toString() }}",
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "Date: ${bid.date}",
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "Status: ${bid.status}",
                                        fontSize = 14.sp,
                                        color = if (bid.status == "Pending") Color.Gray else Color(0xFFFABE0F)
                                    )
                                }
                                // Right Column
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "Type: ${bid.gameType}",
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "Session: ${bid.session}",
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "Time: ${bid.time}",
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "Amount: ₹${bid.bidAmount}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Color(0xFFFABE0F)
                                    )

                                    Text(
                                        text = "Profit: ₹${bid.payoutAmount}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Color(0xFFFABE0F)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
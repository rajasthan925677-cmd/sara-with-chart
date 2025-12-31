package screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.sara567.R
import viewmodal.WalletViewModel
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameTypePage(
    navController: NavController,
    marketName: String,
    openTime: String,
    closeTime: String,
    walletViewModel: WalletViewModel
) {
    val walletState by walletViewModel.walletState.collectAsState()
    val context = LocalContext.current

    // Load user + start listening to balance
    LaunchedEffect(Unit) {
        walletViewModel.loadUserData(context)
    }

    // Parse openTime and closeTime
    val formatter = DateTimeFormatter.ofPattern("HH:mm")
    val open = try { LocalTime.parse(openTime, formatter) } catch (e: Exception) { null }
    val now = LocalTime.now()

    fun isDisabled(type: String): Boolean {
        return (type in listOf("Jodi", "Half Sangam", "Full Sangam")) &&
                (open != null && now.isAfter(open))
    }

    fun isDummy(type: String): Boolean {
        return type in listOf(
            "Single Ank Bulk", "Jodi Bulk", "Single Pana Bulk",
            "Double Pana Bulk", "Triple Pana Bulk"
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(marketName, fontWeight = FontWeight.Bold, color = Color.DarkGray) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.DarkGray
                        )
                    }
                },
                actions = {
                    Text(
                        text = "₹${walletState.balance}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.DarkGray,
                        modifier = Modifier.padding(end = 12.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFF5F3F0),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .padding(padding)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            val gameTypes = listOf(
                "Single Ank", "Single Ank Bulk",
                "Jodi", "Jodi Bulk",
                "Single Pana", "Single Pana Bulk",
                "Double Pana", "Double Pana Bulk",
                "Triple Pana", "Triple Pana Bulk",
                "Half Sangam", "Full Sangam"
            )

            val iconMap = mapOf(
                "Single Ank" to R.drawable.singleank,
                "Jodi" to R.drawable.jodi,
                "Single Pana" to R.drawable.singlepana,
                "Double Pana" to R.drawable.doublepana,
                "Triple Pana" to R.drawable.triplepana,
                "Half Sangam" to R.drawable.halfsangam1,
                "Full Sangam" to R.drawable.halfsangam1,
                "Single Ank Bulk" to R.drawable.singleank,
                "Jodi Bulk" to R.drawable.jodi,
                "Single Pana Bulk" to R.drawable.singlepana,
                "Double Pana Bulk" to R.drawable.doublepana,
                "Triple Pana Bulk" to R.drawable.triplepana
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(gameTypes) { type ->
                    val disabled = isDisabled(type)
                    val isDummy = isDummy(type)

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = !disabled && !isDummy) {
                                when (type) {
                                    "Single Ank" -> navController.navigate("single_ank/$marketName/Single Ank/$openTime/$closeTime")
                                    "Jodi" -> navController.navigate("jodi/$marketName/Jodi/$openTime/$closeTime")
                                    "Single Pana" -> navController.navigate("single_pana/$marketName/Single Pana/$openTime/$closeTime")
                                    "Double Pana" -> navController.navigate("double_pana/$marketName/Double Pana/$openTime/$closeTime")
                                    "Triple Pana" -> navController.navigate("triple_pana/$marketName/Triple Pana/$openTime/$closeTime")
                                    "Half Sangam" -> navController.navigate("half_sangam/$marketName/Half Sangam/$openTime/$closeTime")
                                    "Full Sangam" -> navController.navigate("full_sangam/$marketName/Full Sangam/$openTime/$closeTime")
                                }
                            },
                        shape = RoundedCornerShape(40.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (disabled) Color.Gray else Color(0xFFFABE0F)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .background(Color.White, shape = CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                val resId = iconMap[type]
                                if (resId != null) {
                                    Icon(
                                        painter = painterResource(id = resId),
                                        contentDescription = type,
                                        modifier = Modifier.size(24.dp),
                                        tint = Color.Unspecified
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Filled.Star,
                                        contentDescription = type,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = type,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (disabled) Color.DarkGray else Color.Black
                            )
                        }
                    }
                }
            }
        }
    }
}
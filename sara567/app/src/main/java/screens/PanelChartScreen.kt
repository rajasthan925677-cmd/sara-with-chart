package screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PanaChartScreen(navController: NavController, gameName: String) {
    var results by remember { mutableStateOf<List<Triple<String, String, String>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(gameName) {
        try {
            val snapshot = FirebaseFirestore.getInstance()
                .collection("results")
                .whereEqualTo("gameName", gameName)
                .orderBy("dateSort", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(45)        // ← Ab sirf 45 hi read honge (exact)
                .get()
                .await()

            val list = snapshot.documents.mapNotNull { doc ->
                val open = doc.getString("openResult") ?: return@mapNotNull null
                val close = doc.getString("closeResult") ?: return@mapNotNull null
                val dateStr = doc.getString("resultDate") ?: return@mapNotNull null

                if (open.length != 3 || close.length != 3) return@mapNotNull null

                val openAnk = open.sumOf { it.digitToInt() } % 10
                val closeAnk = close.sumOf { it.digitToInt() } % 10
                val jodi = "$openAnk$closeAnk"

                Triple(dateStr, "$open\n$jodi\n$close", dateStr)
            }

            results = list
            isLoading = false
        } catch (e: Exception) {
            e.printStackTrace()
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("$gameName - Pana Chart", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color(0xFFFABE0F))
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when {
                isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                results.isEmpty() -> Text(
                    text = "No results yet",
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.Gray,
                    fontSize = 18.sp
                )
                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(7),
                        contentPadding = PaddingValues(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        results.forEach { (date, panaText, _) ->
                            item {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .background(Color(0xFFF1F1EF))
                                        .padding(vertical = 1.dp, horizontal = 4.dp)
                                        .fillMaxWidth()
                                ) {
                                    Text(text = date, fontSize = 7.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE91E63))
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = panaText,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                        lineHeight = 14.sp,
                                        color = Color.Black
                                    )
                                }
                            }
                        }
                        items(45 - results.size) { Spacer(modifier = Modifier.size(80.dp)) }
                    }
                }
            }
        }
    }
}
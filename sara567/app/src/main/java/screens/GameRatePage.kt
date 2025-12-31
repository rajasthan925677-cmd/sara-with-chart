package screens





import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController


@Composable
fun GameRatePage(navController: NavController) {
    val gameRates = listOf(
        "Single Ank" to "10 - 95",
        "Jodi" to "10 - 950",
        "Single Pana" to "10 - 1500",
        "Double Pana" to "10 - 3000",
        "Triple Pana" to "10 - 9000",
        "Half Sangam" to "10 - 12000",
        "Full Sangam" to "10 - 100000"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 5.dp, top = 50.dp)
    ) {
        // Back Button + Title
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {navController.popBackStack() } ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "Game Rates",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(start = 5.dp)
            )
        }
        Spacer(modifier = Modifier.height(30.dp))

        Text(
            text = "Game Win Rates for All Bids", modifier = Modifier.align(Alignment.CenterHorizontally),
            style = MaterialTheme.typography.titleLarge.copy(
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
            ),

        )

        Spacer(modifier = Modifier.height(30.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            items(gameRates.size) { index ->
                val (game, rate) = gameRates[index]
                Card(
                    elevation = CardDefaults.cardElevation(10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier.fillMaxWidth().padding(5.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = game,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = rate, fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color(0xFFFABE0F)
                        )
                    }
                }
            }
        }
    }
}

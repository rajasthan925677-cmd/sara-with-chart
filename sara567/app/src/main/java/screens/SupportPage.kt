package screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.sara567.R
import viewmodal.HomeTickerViewModel

@Composable
fun SupportPage(
    navController: NavController,
   // tickerViewModel: HomeTickerViewModel = viewModel()
) {
    val context = LocalContext.current

    // सही तरीका: manually create with context
    val tickerViewModel: HomeTickerViewModel = remember {
        HomeTickerViewModel(context = context.applicationContext)
    }
    val state by tickerViewModel.state.collectAsState()

   // val state by tickerViewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color(0xFFFFFFFF)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Heading
        Text(
            text = "Hello , Dear Sir! ",
            color = Color.Red,
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 24.dp, top = 40.dp)
        )

        Text(
            text = "How can we help you?\nFor Any Query Please Contact Below WhatsApp Support\nOur Team Will Approach You Immediately \n किसी भी सहायता के लिए नीचे दिए व्हाट्सएप नंबर पर संपर्क करे ।",
            color = Color(0xFF7E0505),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Spacer(Modifier.height(40.dp))

        if (!state.isLoading) {
            if (state.whatsapp1.isNotEmpty()) {
                WhatsAppCardSingle(state.whatsapp1)
            }
            if (state.whatsapp2.isNotEmpty()) {
                WhatsAppCardSingle(state.whatsapp2)
            }
        } else {
            Text("Loading support numbers...", color = Color.Gray)
        }

        Spacer(modifier = Modifier.height(50.dp))

        // Go Home button
        Button(
            onClick = { navController.navigate("home") },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFABE0F)),
            shape = RoundedCornerShape(40.dp),
            modifier = Modifier
                .fillMaxWidth().height(50.dp)
                .padding(horizontal = 70.dp)
        ) {
            Text(
                text = "Go Home",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.DarkGray
            )
        }
    }
}

@Composable
fun WhatsAppCardSingle(number: String) {
    val context = LocalContext.current

    Card(
        elevation = CardDefaults.cardElevation(8.dp),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable {
                val url = "https://wa.me/${number.replace(" ", "").replace("+", "")}"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                context.startActivity(intent)
            }
    ) {
        Row(
            modifier = Modifier
                .background(Color(0xFFE8F5E9))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.whatsapp),
                contentDescription = "WhatsApp",
                tint = Color(0xFF25D366),
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = number,
                fontSize = 18.sp,
                color = Color(0xFF1B5E20),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

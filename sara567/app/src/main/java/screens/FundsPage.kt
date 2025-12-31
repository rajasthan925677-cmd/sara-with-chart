
package screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.sara567.R
import com.google.firebase.auth.FirebaseAuth
import viewmodal.WalletViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FundScreen(
    navController: NavController,
    walletViewModel: WalletViewModel
) {
    val context = LocalContext.current
    val walletState by walletViewModel.walletState.collectAsState()

    LaunchedEffect(Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            // Load cached balance first
            walletViewModel.loadUserData(context)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Funds", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.elevatedCardElevation(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Wallet Balance",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "₹ ${walletState.balance}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            FundOptionRow(navController, Icons.Default.AddCircle, null, "Add Fund") {
                navController.navigate("add_fund")
            }
            FundOptionRow(navController, null, painterResource(R.drawable.rupee), "Withdraw Fund") {
                navController.navigate("withdraw_fund")
            }
            FundOptionRow(navController, null, painterResource(R.drawable.bank1), "Bank Details") {
                navController.navigate("bank_detail")
            }
            FundOptionRow(navController, null, painterResource(R.drawable.history), "Add Fund History") {
                navController.navigate("add_history")
            }
            FundOptionRow(navController, Icons.Default.DateRange, null, "Withdraw Fund History") {
                navController.navigate("withdraw_history")
            }
            FundOptionRow(navController, Icons.Default.DateRange, null, "QR Pay History") {
                navController.navigate("qr_pay_history")
            }
        }
    }
}

@Composable
fun FundOptionRow(
    navController: NavController,
    imageVector: ImageVector? = null,
    painter: Painter? = null,
    text: String,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.elevatedCardElevation(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (imageVector != null) {
                Icon(imageVector, text, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
            } else if (painter != null) {
                Icon(painter, text, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
            }
            Spacer(modifier = Modifier.width(20.dp))
            Text(text, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
        }
    }
}

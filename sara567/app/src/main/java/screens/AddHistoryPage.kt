package screens

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class AddFundRequest(
    val amount: Double,
    val paymentDate: String,
    val paymentTime: String,
    val requestStatus: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFundHistoryScreen(navController: NavController) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid

    var fromDate by remember { mutableStateOf("") }
    var toDate by remember { mutableStateOf("") }
    var historyList by remember { mutableStateOf(listOf<AddFundRequest>()) }

    fun pickDate(onDateSelected: (String) -> Unit) {
        val cal = Calendar.getInstance()
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val selected = Calendar.getInstance().apply { set(year, month, dayOfMonth) }
                val formatted = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(selected.time)
                onDateSelected(formatted)
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Fund History") },
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
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFACB4BE)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { pickDate { fromDate = it } },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(Color.White)
                        ) {
                            Text(
                                text = if (fromDate.isEmpty()) "From Date" else fromDate,
                                color = Color.Black
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Button(
                            onClick = { pickDate { toDate = it } },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(Color.White)
                        ) {
                            Text(
                                text = if (toDate.isEmpty()) "To Date" else toDate,
                                color = Color.Black
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Button(
                            colors = ButtonDefaults.buttonColors(Color(0xFFFABE0F)),
                            onClick = {
                             //   Log.d("AddFundHistory", "Search clicked, userId: $userId, fromDate: $fromDate, toDate: $toDate")
                                if (userId == null) {
                                 //   Log.w("AddFundHistory", "User not logged in")
                                    Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                if (fromDate.isEmpty() || toDate.isEmpty()) {
                                 //   Log.w("AddFundHistory", "From or To date is empty")
                                    Toast.makeText(context, "Please select both From and To dates", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                db.collection("add_requests")
                                    .whereEqualTo("userId", userId)
                                    .get()
                                    .addOnSuccessListener { result ->
                                     //   Log.d("AddFundHistory", "Fetched ${result.documents.size} documents")
                                        result.documents.forEach { doc ->
                                        //    Log.d("AddFundHistory", "Doc: ${doc.id} -> ${doc.data}")
                                        }
                                        val filtered = result.documents.mapNotNull { doc ->
                                            val dateStr = doc.getString("paymentDate") ?: return@mapNotNull null
                                            val sdfFirestore = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                                            val sdfInput = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                                            try {
                                                val docDate = sdfFirestore.parse(dateStr)
                                                val from = sdfInput.parse(fromDate)
                                                val to = sdfInput.parse(toDate)
                                               // Log.d("AddFundHistory", "Parsed: docDate=$dateStr, from=$fromDate, to=$toDate")
                                                if (docDate != null && from != null && to != null && !docDate.before(from) && !docDate.after(to)) {
                                                    AddFundRequest(
                                                        amount = doc.getDouble("amount") ?: 0.0,
                                                        paymentDate = dateStr,
                                                        paymentTime = doc.getString("paymentTime") ?: "",
                                                        requestStatus = doc.getString("requestStatus") ?: "Pending"
                                                    )
                                                } else {
                                               //     Log.d("AddFundHistory", "Doc $dateStr outside range [$fromDate, $toDate]")
                                                    null
                                                }
                                            } catch (e: Exception) {
                                             //   Log.e("AddFundHistory", "Error parsing date: $dateStr, from: $fromDate, to: $toDate", e)
                                                null
                                            }
                                        }.sortedByDescending { it.paymentDate }
                                        historyList = filtered
                                        //Log.d("AddFundHistory", "Filtered ${filtered.size} documents")
                                        if (filtered.isEmpty()) {
                                            Toast.makeText(context, "No records found for the selected date range", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                    .addOnFailureListener { e ->
                                        //Log.e("AddFundHistory", "Failed to fetch history: ${e.message}", e)
                                        Toast.makeText(context, "Failed to fetch history: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                            }
                        ) {
                            Text("Search", color = Color.White)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            if (historyList.isEmpty()) {
                Text(
                    text = "No History Found",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(historyList) { item ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFD5E1E3)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = "Amount: ₹${item.amount}", color = Color.Black)
                                    val statusColor = when (item.requestStatus) {
                                        "Accepted" -> Color(0xFF37EF3F)
                                        "Rejected" -> Color(0xFFF44336)
                                        "Pending" -> Color.Gray
                                        else -> Color.White
                                    }
                                    Text(text = "Status: ${item.requestStatus}", color = statusColor)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = "Date: ${item.paymentDate}", color = Color.Black)
                                    Text(text = "Time: ${item.paymentTime}", color = Color.Black)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
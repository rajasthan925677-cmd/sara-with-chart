package screens

import android.app.DatePickerDialog
import android.widget.DatePicker
import android.widget.Toast
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import firebase.SharedPrefHelper
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class QRPayRequest(
    val amount: Double,
    val paymentDate: String,
    val requestStatus: String,
    val transactionId: String,
    val transactionRefId: String,
    val upiStatus: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QRPayHistoryScreen(navController: NavController) {
    val context = LocalContext.current
    val userUid = SharedPrefHelper.getUID(context) // Use UID
    var fromDate by remember { mutableStateOf("") }
    var toDate by remember { mutableStateOf("") }
    var requests by remember { mutableStateOf<List<QRPayRequest>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var triggerFetch by remember { mutableStateOf(0) }

    // Date formats
    val uiDateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()) // UI input
    val firestoreDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) // Firestore

    // DatePickerDialog for From Date
    val fromDatePickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, year: Int, month: Int, day: Int ->
            fromDate = String.format("%02d-%02d-%d", day, month + 1, year)
        },
        Calendar.getInstance().get(Calendar.YEAR),
        Calendar.getInstance().get(Calendar.MONTH),
        Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
    )

    // DatePickerDialog for To Date
    val toDatePickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, year: Int, month: Int, day: Int ->
            toDate = String.format("%02d-%02d-%d", day, month + 1, year)
        },
        Calendar.getInstance().get(Calendar.YEAR),
        Calendar.getInstance().get(Calendar.MONTH),
        Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
    )

    // InteractionSource for From Date
    val fromInteractionSource = remember { MutableInteractionSource() }
    val fromIsPressed by fromInteractionSource.collectIsPressedAsState()
    if (fromIsPressed) {
        fromDatePickerDialog.show()
    }

    // InteractionSource for To Date
    val toInteractionSource = remember { MutableInteractionSource() }
    val toIsPressed by toInteractionSource.collectIsPressedAsState()
    if (toIsPressed) {
        toDatePickerDialog.show()
    }

    // Fetch data from Firestore
    suspend fun fetchQRPayRequests(from: String?, to: String?) {
        if (userUid.isNullOrEmpty()) {
            errorMessage = "User not logged in. Please log in again."
            isLoading = false
            return
        }
        isLoading = true
        errorMessage = null
        try {
            val db = FirebaseFirestore.getInstance()
            // Fetch all documents for userId (no date filter in query)
            val snapshot = db.collection("QRpayRequest")
                .whereEqualTo("userId", userUid)
                .get()
                .await()

            // Map documents to QRPayRequest
            val allRequests = snapshot.documents.mapNotNull { doc ->
                try {
                    QRPayRequest(
                        amount = doc.getDouble("amount") ?: 0.0,
                        paymentDate = doc.getString("paymentDate") ?: "",
                        requestStatus = doc.getString("requestStatus") ?: "",
                        transactionId = doc.getString("transactionId") ?: "",
                        transactionRefId = doc.getString("transactionRefId") ?: "",
                        upiStatus = doc.getString("upiStatus") ?: ""
                    )
                } catch (e: Exception) {
                    null
                }
            }

            // Apply local date filtering if provided
            requests = if (!from.isNullOrBlank() && !to.isNullOrBlank()) {
                try {
                    val fromDateParsed = uiDateFormat.parse(from) ?: Date(0)
                    val toDateParsed = uiDateFormat.parse(to)?.let {
                        Calendar.getInstance().apply {
                            time = it
                            set(Calendar.HOUR_OF_DAY, 23)
                            set(Calendar.MINUTE, 59)
                            set(Calendar.SECOND, 59)
                            set(Calendar.MILLISECOND, 999)
                        }.time
                    } ?: Date(Long.MAX_VALUE)

                    allRequests.filter { request ->
                        val paymentDate = firestoreDateFormat.parse(request.paymentDate) ?: Date(0)
                        paymentDate in fromDateParsed..toDateParsed
                    }
                } catch (e: Exception) {
                    errorMessage = "Invalid date format"
                    emptyList()
                }
            } else {
                allRequests // No filtering if dates not provided
            }.sortedByDescending { firestoreDateFormat.parse(it.paymentDate)?.time }

            if (requests.isEmpty()) {
                errorMessage = "No records found for the selected dates. Try a broader range."
            }
        } catch (e: Exception) {
            errorMessage = "Failed to fetch data: ${e.message}. Check your internet or contact support."
        } finally {
            isLoading = false
        }
    }

    // Fetch data when triggerFetch changes
    LaunchedEffect(triggerFetch) {
        if (triggerFetch > 0) {
            fetchQRPayRequests(fromDate, toDate)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("QR Pay History", fontWeight = FontWeight.Bold) },
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
            // Date Filter Card
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = fromDate,
                        onValueChange = {},
                        label = { Text("From Date (dd-MM-yyyy)") },
                        readOnly = true,
                        interactionSource = fromInteractionSource,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = toDate,
                        onValueChange = {},
                        label = { Text("To Date (dd-MM-yyyy)") },
                        readOnly = true,
                        interactionSource = toInteractionSource,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Button(
                        onClick = {
                            if (userUid.isNullOrEmpty()) {
                                Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show()
                            } else if (fromDate.isBlank() || toDate.isBlank()) {
                                errorMessage = "Please select both From and To dates"
                            } else {
                                try {
                                    uiDateFormat.parse(fromDate)
                                    uiDateFormat.parse(toDate)
                                    triggerFetch++ // Increment to trigger LaunchedEffect
                                } catch (e: Exception) {
                                    errorMessage = "Invalid date format"
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(Color(0xFFFABE0F))
                    ) {
                        Text("Search", fontSize = 16.sp)
                    }
                }
            }

            // Error Message
            errorMessage?.let {
                Text(
                    text = it,
                    color = Color.Red,
                    fontSize = 14.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }

            // Loading Indicator
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(16.dp)
                )
            } else if (requests.isEmpty()) {
                Text(
                    text = "No History Found",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    textAlign = TextAlign.Center
                )
            } else {
                // Data List
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(requests) { request ->
                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Amount: ₹${request.amount}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = "Date: ${request.paymentDate}",
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "Status: ${request.requestStatus}",
                                    fontSize = 14.sp,
                                    color = when (request.requestStatus) {
                                        "pending" -> Color(0xFFFFA500)
                                        "rejected" -> Color.Red
                                        else -> Color.Green
                                    }
                                )
                                Text(
                                    text = "Transaction ID: ${request.transactionId}",
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "Transaction Ref ID: ${request.transactionRefId}",
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "UPI Status: ${request.upiStatus}",
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
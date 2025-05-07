package com.example.inboundinventorytracker.ui

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.inboundinventorytracker.R
import com.example.inboundinventorytracker.data.model.Batch
import com.example.inboundinventorytracker.data.repository.InventoryRepository
import com.example.inboundinventorytracker.util.AuditTrailLogger
import com.example.inboundinventorytracker.AppViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreppedBatchesScreen(appViewModel: AppViewModel, navController: NavHostController) {
    val user = appViewModel.currentUser ?: return
    val context = LocalContext.current
    val repository = InventoryRepository(context)
    val scope = rememberCoroutineScope()
    var batches by remember { mutableStateOf<List<Batch>>(emptyList()) }
    var showAddBatchDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        AuditTrailLogger.initialize(context)
        appViewModel.initialize(repository)
        batches = repository.getBatches()
        appViewModel.updateBatchCompletionStatuses()
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.prepped_batches)) },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.popBackStack()
                        AuditTrailLogger.log(user, "navigate", "Returned to Main Menu from Prepped Batches")
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { 
                    showAddBatchDialog = true
                    AuditTrailLogger.log(user, "action", "Opened Add Batch dialog")
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Batch")
            }
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                if (batches.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No batches found. Add a new batch to get started.",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f).padding(16.dp)
                    ) {
                        items(batches.sortedByDescending { it.created_at }) { batch ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${batch.date} (${batch.skus.size} SKUs)",
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                        Text(
                                            text = "Created: ${formatToEst12Hour(batch.created_at)} EST",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                        Text(
                                            text = "Created by: ${batch.created_by}",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                    
                                    val isComplete = appViewModel.batchStatus[batch.date] ?: true
                                    val statusColor = if (isComplete) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.error
                                    }
                                    
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .background(
                                                color = statusColor,
                                                shape = CircleShape
                                            )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddBatchDialog) {
        AddBatchDialog(
            user = user,
            onDismiss = { showAddBatchDialog = false },
            onSubmit = { date, skus ->
                scope.launch {
                    val batch = Batch(
                        date = date,
                        skus = skus,
                        created_by = user,
                        created_at = OffsetDateTime.now().toString()
                    )
                    repository.addBatch(batch)
                    AuditTrailLogger.log(user, "add_batch", "Added batch for $date with ${skus.size} SKUs")
                    batches = repository.getBatches()
                    appViewModel.updateBatchCompletionStatuses()
                }
                showAddBatchDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBatchDialog(user: String, onDismiss: () -> Unit, onSubmit: (String, List<String>) -> Unit) {
    var date by remember { mutableStateOf(SimpleDateFormat("dd-MM-yyyy", Locale.US).format(Date())) }
    var barcode by remember { mutableStateOf("") }
    val skus = remember { mutableStateListOf<String>() }
    var isSubmitEnabled by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    
    // Parse current date for date picker
    val currentDate = remember {
        val formatter = SimpleDateFormat("dd-MM-yyyy", Locale.US)
        try {
            formatter.parse(date)?.time ?: System.currentTimeMillis()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }
    
    // Update submit button state
    LaunchedEffect(date, skus.size) {
        isSubmitEnabled = date.isNotBlank() && skus.isNotEmpty()
    }

    // Date picker dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = currentDate)
        
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                Button(onClick = { 
                    datePickerState.selectedDateMillis?.let { millis ->
                        date = SimpleDateFormat("dd-MM-yyyy", Locale.US).format(Date(millis))
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                Button(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(id = R.string.add_new_batch)) },
        text = {
            Column {
                // Date field with calendar icon
                OutlinedTextField(
                    value = date,
                    onValueChange = { /* Read-only, changes come from date picker */ },
                    label = { Text("Date (DD-MM-YYYY)") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Select date"
                            )
                        }
                    }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = barcode,
                    onValueChange = { barcode = it },
                    label = { Text("Barcode (Scan or Enter)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Button(
                    onClick = {
                        if (barcode.isNotBlank()) {
                            // Check for duplicates
                            if (!skus.contains(barcode)) {
                                skus.add(barcode)
                                AuditTrailLogger.log(user, "scan_barcode", "Scanned barcode: $barcode")
                                barcode = ""
                                isSubmitEnabled = true
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Add Barcode")
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text("Scanned SKUs (${skus.size}):")
                
                LazyColumn(
                    modifier = Modifier.height(150.dp)
                ) {
                    items(skus) { sku ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "• $sku", 
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { 
                                    skus.remove(sku) 
                                    isSubmitEnabled = skus.isNotEmpty()
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Remove SKU"
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isSubmitEnabled) {
                        onSubmit(date, skus.toList())
                    }
                },
                enabled = isSubmitEnabled
            ) {
                Text(stringResource(id = R.string.submit))
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

fun formatToEst12Hour(isoTimestamp: String?): String {
    if (isoTimestamp == null) return "N/A"
    val utcDate = OffsetDateTime.parse(isoTimestamp)
    val estDate = utcDate.withOffsetSameInstant(ZoneId.of("America/New_York").rules.getOffset(utcDate.toInstant()))
    return estDate.format(DateTimeFormatter.ofPattern("MM-dd-yyyy hh:mm:ss a"))
}
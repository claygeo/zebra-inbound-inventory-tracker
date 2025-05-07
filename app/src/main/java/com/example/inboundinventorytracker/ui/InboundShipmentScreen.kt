package com.example.inboundinventorytracker.ui

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.inboundinventorytracker.data.model.InboundRecord
import com.example.inboundinventorytracker.data.repository.InventoryRepository
import com.example.inboundinventorytracker.util.AuditTrailLogger
import com.example.inboundinventorytracker.AppViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.OffsetDateTime
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun InboundShipmentScreen(appViewModel: AppViewModel, navController: NavHostController) {
    val user = appViewModel.currentUser ?: return
    val context = LocalContext.current
    val repository = InventoryRepository(context)
    val scope = rememberCoroutineScope()
    var batches by remember { mutableStateOf<List<com.example.inboundinventorytracker.data.model.Batch>>(emptyList()) }
    var selectedBatch by remember { mutableStateOf<com.example.inboundinventorytracker.data.model.Batch?>(appViewModel.selectedBatch) }
    var scannedBarcodes by remember { mutableStateOf<List<String>>(emptyList()) }
    var showBarcodeInput by remember { mutableStateOf(false) }
    var showInputForm by remember { mutableStateOf<String?>(null) }
    var inboundRecords by remember { mutableStateOf<List<InboundRecord>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var globalNotes by remember { mutableStateOf("") }
    var showAddNotesDialog by remember { mutableStateOf(false) }
    var isComplete by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        AuditTrailLogger.initialize(context)
        appViewModel.initialize(repository)
        batches = repository.getBatches()
        if (selectedBatch != null) {
            inboundRecords = repository.getInboundRecordsByDate(selectedBatch!!.date)
            inboundRecords.firstOrNull()?.notes?.let { notes ->
                globalNotes = notes
            }
            isComplete = repository.getCompletionStatus(selectedBatch!!.date)
        }
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        if (selectedBatch == null) "Inbound Shipment" 
                        else "Inbound: ${selectedBatch!!.date}"
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (selectedBatch == null) {
                            navController.popBackStack()
                            AuditTrailLogger.log(user, "navigate", "Returned to Main Menu from Inbound Shipment")
                        } else {
                            selectedBatch = null
                            appViewModel.clearSelectedBatch()
                            AuditTrailLogger.log(user, "navigate", "Returned to Inbound Date Selection")
                        }
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (selectedBatch != null) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .background(
                                    color = if (isComplete) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                    shape = CircleShape
                                )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(onClick = { showAddNotesDialog = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Add Notes")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (selectedBatch != null) {
                FloatingActionButton(onClick = { showBarcodeInput = true }) {
                    Icon(Icons.Default.Search, contentDescription = "Scan Barcode")
                }
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
            if (selectedBatch == null) {
                Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                    if (batches.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "No batches found",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { navController.navigate("prepped_batches") }
                                ) {
                                    Text("Create a Batch")
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f).padding(16.dp)
                        ) {
                            items(batches.sortedByDescending { it.created_at }) { batch ->
                                val note = inboundRecords.find { it.date == batch.date }?.notes
                                val batchIsComplete = appViewModel.batchStatus[batch.date] ?: true
                                val statusColor = if (batchIsComplete) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.error
                                }
                                
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clickable {
                                            appViewModel.selectDate(batch.date)
                                            appViewModel.selectBatch(batch)
                                            selectedBatch = batch
                                            scannedBarcodes = emptyList()
                                            scope.launch {
                                                inboundRecords = repository.getInboundRecordsByDate(batch.date)
                                                inboundRecords.firstOrNull()?.notes?.let { notes ->
                                                    globalNotes = notes
                                                }
                                                isComplete = repository.getCompletionStatus(batch.date)
                                            }
                                            AuditTrailLogger.log(user, "select_date", "Selected date: ${batch.date}")
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = batch.date,
                                                style = MaterialTheme.typography.bodyLarge
                                            )
                                            Text(
                                                text = "${batch.skus.size} SKUs",
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                            if (!note.isNullOrBlank()) {
                                                Text(
                                                    text = "Note: $note",
                                                    style = MaterialTheme.typography.bodySmall
                                                )
                                            }
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
            } else {
                Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                    LinearProgressIndicator(
                        progress = if (selectedBatch!!.skus.isEmpty()) 1f else scannedBarcodes.size.toFloat() / selectedBatch!!.skus.size,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Progress: ${scannedBarcodes.size}/${selectedBatch!!.skus.size} SKUs",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isComplete) {
                                Icon(
                                    Icons.Default.Check, 
                                    contentDescription = "Complete",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            } else {
                                Icon(
                                    Icons.Default.Clear, 
                                    contentDescription = "Incomplete",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                if (isComplete) "Complete" else "Incomplete",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                    
                    if (!globalNotes.isBlank()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    "Notes:",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(globalNotes)
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    var selectedTabIndex by remember { mutableStateOf(0) }
                    TabRow(selectedTabIndex = selectedTabIndex) {
                        Tab(
                            selected = selectedTabIndex == 0,
                            onClick = { selectedTabIndex = 0 },
                            text = { Text("To Scan (${selectedBatch!!.skus.size - scannedBarcodes.size})") }
                        )
                        Tab(
                            selected = selectedTabIndex == 1,
                            onClick = { selectedTabIndex = 1 },
                            text = { Text("Submitted (${inboundRecords.size})") }
                        )
                    }
                    
                    when (selectedTabIndex) {
                        0 -> {
                            val unscannedSkus = selectedBatch!!.skus.filter { !scannedBarcodes.contains(it) }
                            if (unscannedSkus.isEmpty()) {
                                Box(
                                    modifier = Modifier.fillMaxWidth().weight(1f),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("All SKUs have been scanned!")
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier.weight(1f).padding(horizontal = 16.dp)
                                ) {
                                    items(unscannedSkus) { sku ->
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(16.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = sku,
                                                    style = MaterialTheme.typography.bodyLarge,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                Button(onClick = { 
                                                    scannedBarcodes = scannedBarcodes + sku
                                                    showInputForm = sku
                                                    AuditTrailLogger.log(user, "scan_barcode", "Manually selected SKU: $sku")
                                                }) {
                                                    Text("Select")
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        1 -> {
                            if (inboundRecords.isEmpty()) {
                                Box(
                                    modifier = Modifier.fillMaxWidth().weight(1f),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("No records submitted yet")
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier.weight(1f).padding(horizontal = 16.dp)
                                ) {
                                    items(inboundRecords) { record ->
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp)
                                                .clickable {
                                                    showInputForm = record.barcode
                                                    AuditTrailLogger.log(user, "view_record", "Viewing record for barcode: ${record.barcode}")
                                                }
                                        ) {
                                            Row(modifier = Modifier.padding(16.dp)) {
                                                Column {
                                                    Text(
                                                        text = "Barcode: ${record.barcode}",
                                                        style = MaterialTheme.typography.bodyLarge
                                                    )
                                                    Text(
                                                        text = "Units: ${record.total_unit_count}, Category: ${record.subcategory}",
                                                        style = MaterialTheme.typography.bodySmall
                                                    )
                                                    Text(
                                                        text = "Box Size: ${record.box_size}, Boxes: ${record.box_count}",
                                                        style = MaterialTheme.typography.bodySmall
                                                    )
                                                    if (record.notes?.isNotBlank() == true) {
                                                        Text(
                                                            text = "Notes: ${record.notes}",
                                                            style = MaterialTheme.typography.bodySmall
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
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Button(
                        onClick = { showBarcodeInput = true },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Scan Barcode")
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }

    if (showBarcodeInput) {
        BarcodeInputDialog(
            user = user,
            onDismiss = { showBarcodeInput = false },
            onSubmit = { barcode ->
                if (selectedBatch!!.skus.contains(barcode) && !scannedBarcodes.contains(barcode)) {
                    scannedBarcodes = scannedBarcodes + barcode
                    showInputForm = barcode
                    AuditTrailLogger.log(user, "scan_barcode", "Scanned barcode: $barcode")
                } else if (scannedBarcodes.contains(barcode)) {
                    showInputForm = barcode
                    AuditTrailLogger.log(user, "scan_existing_barcode", "Re-scanned existing barcode: $barcode")
                } else {
                    scope.launch {
                        // TODO: Show a snackbar or some notification
                    }
                }
                showBarcodeInput = false
            }
        )
    }

    if (showInputForm != null) {
        val existingRecord = inboundRecords.find { it.barcode == showInputForm }
        InboundInputForm(
            user = user,
            barcode = showInputForm!!,
            date = selectedBatch!!.date,
            existingRecord = existingRecord,
            onDismiss = { showInputForm = null },
            onSubmit = { recordCompletion ->
                scope.launch {
                    inboundRecords = repository.getInboundRecordsByDate(selectedBatch!!.date)
                    globalNotes = inboundRecords.firstOrNull()?.notes ?: ""
                    isComplete = repository.getCompletionStatus(selectedBatch!!.date)
                    appViewModel.updateBatchCompletionStatuses()
                }
                if (recordCompletion) {
                    scannedBarcodes = scannedBarcodes.filter { it != showInputForm }
                }
                showInputForm = null
            }
        )
    }
    
    if (showAddNotesDialog) {
        AlertDialog(
            onDismissRequest = { showAddNotesDialog = false },
            title = { Text("Add Notes for ${selectedBatch?.date}") },
            text = {
                Column {
                    OutlinedTextField(
                        value = globalNotes,
                        onValueChange = { globalNotes = it },
                        label = { Text("Notes") },
                        modifier = Modifier.fillMaxWidth().height(120.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Mark as complete:")
                        Spacer(modifier = Modifier.width(8.dp))
                        Switch(
                            checked = isComplete,
                            onCheckedChange = { isComplete = it }
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            val records = repository.getInboundRecordsByDate(selectedBatch!!.date)
                            if (records.isNotEmpty()) {
                                val statusNote = if (isComplete) "" else " (incomplete)"
                                records.forEach { record ->
                                    val updatedRecord = record.copy(notes = globalNotes + statusNote)
                                    repository.addInboundRecord(updatedRecord)
                                }
                                AuditTrailLogger.log(user, "update_notes", "Updated notes for ${selectedBatch!!.date}: $globalNotes")
                                inboundRecords = repository.getInboundRecordsByDate(selectedBatch!!.date)
                                appViewModel.updateBatchCompletionStatuses()
                            } else if (selectedBatch != null) {
                                val dummyRecord = InboundRecord(
                                    date = selectedBatch!!.date,
                                    barcode = "NOTES_${selectedBatch!!.date}",
                                    identifier = "",
                                    last_4_barcode = "",
                                    units_per_case = 0,
                                    box_count = 0,
                                    partial_case_1 = 0,
                                    partial_case_2 = 0,
                                    partial_case_3 = 0,
                                    partial_case_4 = 0,
                                    total_unit_count = 0,
                                    subcategory = "",
                                    ftp_cases = 0,
                                    ocala_cases = 0,
                                    ftp_units = 0,
                                    ocala_units = 0,
                                    box_size = "",
                                    verified_total_unit_count = 0,
                                    notes = globalNotes + (if (isComplete) "" else " (incomplete)"),
                                    created_by = user,
                                    created_at = OffsetDateTime.now().toString()
                                )
                                repository.addInboundRecord(dummyRecord)
                                AuditTrailLogger.log(user, "add_notes", "Added notes for ${selectedBatch!!.date}: $globalNotes")
                                inboundRecords = repository.getInboundRecordsByDate(selectedBatch!!.date)
                                appViewModel.updateBatchCompletionStatuses()
                            }
                        }
                        showAddNotesDialog = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                Button(onClick = { showAddNotesDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun BarcodeInputDialog(user: String, onDismiss: () -> Unit, onSubmit: (String) -> Unit) {
    var barcode by remember { mutableStateOf("") }
    var isBarcodeValid by remember { mutableStateOf(false) }
    
    LaunchedEffect(barcode) {
        isBarcodeValid = barcode.isNotBlank()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Scan Barcode") },
        text = {
            Column {
                OutlinedTextField(
                    value = barcode,
                    onValueChange = { barcode = it },
                    label = { Text("Barcode (Scan or Enter)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    "Press ENTER on the scanner or tap SUBMIT when ready",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isBarcodeValid) {
                        onSubmit(barcode)
                    }
                },
                enabled = isBarcodeValid
            ) {
                Text("Submit")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun InboundInputForm(
    user: String,
    barcode: String,
    date: String,
    existingRecord: InboundRecord?,
    onDismiss: () -> Unit,
    onSubmit: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val repository = InventoryRepository(context)
    val scope = rememberCoroutineScope()
    var notes by remember { mutableStateOf(existingRecord?.notes ?: "") }
    var identifier by remember { mutableStateOf(existingRecord?.identifier ?: "") }
    val last4Barcode = barcode.takeLast(4)
    var unitsPerCase by remember { mutableStateOf(existingRecord?.units_per_case?.toString() ?: "") }
    var boxCount by remember { mutableStateOf(existingRecord?.box_count?.toString() ?: "") }
    var partialCase1 by remember { mutableStateOf(existingRecord?.partial_case_1?.toString() ?: "") }
    var partialCase2 by remember { mutableStateOf(existingRecord?.partial_case_2?.toString() ?: "") }
    var partialCase3 by remember { mutableStateOf(existingRecord?.partial_case_3?.toString() ?: "") }
    var partialCase4 by remember { mutableStateOf(existingRecord?.partial_case_4?.toString() ?: "") }
    var subcategory by remember { mutableStateOf(existingRecord?.subcategory ?: "PREROLL") }
    var boxSize by remember { mutableStateOf(existingRecord?.box_size ?: "LG") }
    var verifiedTotalUnitCount by remember { mutableStateOf(existingRecord?.verified_total_unit_count?.toString() ?: "") }
    var isFormValid by remember { mutableStateOf(false) }
    var showConfirmationDialog by remember { mutableStateOf(false) }
    var isComplete by remember { mutableStateOf(true) }

    LaunchedEffect(identifier, unitsPerCase, boxCount, verifiedTotalUnitCount) {
        isFormValid = identifier.isNotBlank() && 
                      unitsPerCase.toIntOrNull() != null && 
                      boxCount.toIntOrNull() != null && 
                      verifiedTotalUnitCount.toIntOrNull() != null
    }

    val subcategories = listOf(
        "APPAREL", "CONCENTRATES", "SHATTER", "WAX", "CRUMBLE", "ROSIN", "KIEF",
        "EDIBLES", "LOZENGE", "GUMMIES", "CHOCOLATES", "FLOWER", "NON-CANNABIS",
        "ORAL", "TINCTURE", "PREROLL", "PRE-PACK", "TOPICAL", "BALM", "VAPE"
    )
    val boxSizes = listOf("LG", "SM")

    val unitsPerCaseInt = unitsPerCase.toIntOrNull() ?: 0
    val boxCountInt = boxCount.toIntOrNull() ?: 0
    val partial1Int = partialCase1.toIntOrNull() ?: 0
    val partial2Int = partialCase2.toIntOrNull() ?: 0
    val partial3Int = partialCase3.toIntOrNull() ?: 0
    val partial4Int = partialCase4.toIntOrNull() ?: 0
    val totalPartialCases = (if (partial1Int > 0) 1 else 0) + 
                           (if (partial2Int > 0) 1 else 0) +
                           (if (partial3Int > 0) 1 else 0) + 
                           (if (partial4Int > 0) 1 else 0)
    val fullBoxes = boxCountInt - totalPartialCases
    val fullUnits = fullBoxes * unitsPerCaseInt
    val totalUnitCount = fullUnits + partial1Int + partial2Int + partial3Int + partial4Int

    val subcategoryPercentages = mapOf(
        "APPAREL" to 0.15, "CONCENTRATES" to 0.58, "SHATTER" to 0.58, "WAX" to 0.58,
        "CRUMBLE" to 0.58, "ROSIN" to 0.58, "KIEF" to 0.58, "EDIBLES" to 0.55,
        "LOZENGE" to 0.55, "GUMMIES" to 0.55, "CHOCOLATES" to 0.55, "FLOWER" to 0.53,
        "NON-CANNABIS" to 0.56, "ORAL" to 0.58, "TINCTURE" to 0.58, "PREROLL" to 0.56,
        "PRE-PACK" to 0.56, "TOPICAL" to 0.53, "BALM" to 0.53, "VAPE" to 0.55
    )
    val percentage = subcategoryPercentages[subcategory] ?: 0.0
    val ftpCases = (boxCountInt - (boxCountInt * percentage)).toInt()
    val ocalaCases = boxCountInt - ftpCases
    val ftpUnits = ftpCases * unitsPerCaseInt
    val ocalaUnits = totalUnitCount - ftpUnits

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Barcode: $barcode${if (existingRecord != null) " (View/Edit)" else ""}") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = identifier,
                    onValueChange = { identifier = it },
                    label = { Text("#") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = identifier.isBlank(),
                    supportingText = { 
                        if (identifier.isBlank()) {
                            Text("Required", color = MaterialTheme.colorScheme.error)
                        }
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Last 4 of Barcode: $last4Barcode")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = unitsPerCase,
                    onValueChange = { unitsPerCase = it },
                    label = { Text("Units Per Case") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = unitsPerCase.toIntOrNull() == null,
                    supportingText = { 
                        if (unitsPerCase.toIntOrNull() == null) {
                            Text("Enter a valid number", color = MaterialTheme.colorScheme.error)
                        }
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = boxCount,
                    onValueChange = { boxCount = it },
                    label = { Text("Box Count") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = boxCount.toIntOrNull() == null,
                    supportingText = { 
                        if (boxCount.toIntOrNull() == null) {
                            Text("Enter a valid number", color = MaterialTheme.colorScheme.error)
                        }
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = partialCase1,
                    onValueChange = { partialCase1 = it },
                    label = { Text("Partial Case #1") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = partialCase2,
                    onValueChange = { partialCase2 = it },
                    label = { Text("Partial Case #2") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = partialCase3,
                    onValueChange = { partialCase3 = it },
                    label = { Text("Partial Case #3") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = partialCase4,
                    onValueChange = { partialCase4 = it },
                    label = { Text("Partial Case #4") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Calculations Summary",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Total Unit Count: $totalUnitCount")
                        Text("FTP Cases: $ftpCases")
                        Text("Ocala Cases: $ocalaCases")
                        Text("FTP Units: $ftpUnits")
                        Text("Ocala Units: $ocalaUnits")
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                var expandedSubcategory by remember { mutableStateOf(false) }
                Box {
                    OutlinedTextField(
                        value = subcategory,
                        onValueChange = {},
                        label = { Text("Subcategory") },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        trailingIcon = {
                            Icon(
                                imageVector = if (expandedSubcategory) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = "Select subcategory"
                            )
                        }
                    )
                    DropdownMenu(
                        expanded = expandedSubcategory,
                        onDismissRequest = { expandedSubcategory = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        subcategories.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    subcategory = option
                                    expandedSubcategory = false
                                }
                            )
                        }
                    }
                    Spacer(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { expandedSubcategory = true }
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                var expandedBoxSize by remember { mutableStateOf(false) }
                Box {
                    OutlinedTextField(
                        value = boxSize,
                        onValueChange = {},
                        label = { Text("Box Size") },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        trailingIcon = {
                            Icon(
                                imageVector = if (expandedBoxSize) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = "Select box size"
                            )
                        }
                    )
                    DropdownMenu(
                        expanded = expandedBoxSize,
                        onDismissRequest = { expandedBoxSize = false },
                        modifier = Modifier.fillMaxWidth(0.5f)
                    ) {
                        boxSizes.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    boxSize = option
                                    expandedBoxSize = false
                                }
                            )
                        }
                    }
                    Spacer(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { expandedBoxSize = true }
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = verifiedTotalUnitCount,
                    onValueChange = { verifiedTotalUnitCount = it },
                    label = { Text("Verified Total Unit Count") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = verifiedTotalUnitCount.toIntOrNull() == null,
                    supportingText = { 
                        if (verifiedTotalUnitCount.toIntOrNull() == null) {
                            Text("Enter a valid number", color = MaterialTheme.colorScheme.error)
                        }
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Mark as complete:")
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        checked = isComplete,
                        onCheckedChange = { isComplete = it }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { showConfirmationDialog = true },
                enabled = isFormValid
            ) {
                Text("Submit")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
    
    if (showConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmationDialog = false },
            title = { Text("Confirm Submission") },
            text = { Text("Are you sure you want to submit this record?") },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            val record = InboundRecord(
                                date = date,
                                barcode = barcode,
                                identifier = identifier,
                                last_4_barcode = last4Barcode,
                                units_per_case = unitsPerCaseInt,
                                box_count = boxCountInt,
                                partial_case_1 = partial1Int,
                                partial_case_2 = partial2Int,
                                partial_case_3 = partial3Int,
                                partial_case_4 = partial4Int,
                                total_unit_count = totalUnitCount,
                                subcategory = subcategory,
                                ftp_cases = ftpCases,
                                ocala_cases = ocalaCases,
                                ftp_units = ftpUnits,
                                ocala_units = ocalaUnits,
                                box_size = boxSize,
                                verified_total_unit_count = verifiedTotalUnitCount.toIntOrNull() ?: 0,
                                notes = notes + (if (isComplete) "" else " (incomplete)"),
                                created_by = user,
                                created_at = OffsetDateTime.now().toString()
                            )
                            repository.addInboundRecord(record)
                            AuditTrailLogger.log(user, "submit_inbound", "Submitted inbound record for barcode: $barcode")
                        }
                        showConfirmationDialog = false
                        onSubmit(true)
                    }
                ) {
                    Text("Submit")
                }
            },
            dismissButton = {
                Button(onClick = { showConfirmationDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
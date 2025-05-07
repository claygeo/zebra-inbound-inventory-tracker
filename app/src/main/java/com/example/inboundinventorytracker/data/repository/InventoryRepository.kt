package com.example.inboundinventorytracker.data.repository

import android.content.Context
import com.example.inboundinventorytracker.data.LocalStorage
import com.example.inboundinventorytracker.data.model.AuditLog
import com.example.inboundinventorytracker.data.model.Batch
import com.example.inboundinventorytracker.data.model.InboundRecord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class InventoryRepository(private val context: Context) {
    
    suspend fun getBatches(): List<Batch> = withContext(Dispatchers.IO) {
        return@withContext LocalStorage.loadBatches(context)
    }
    
    suspend fun getInboundRecordsByDate(date: String): List<InboundRecord> = withContext(Dispatchers.IO) {
        val records = LocalStorage.loadInboundRecords(context)
        return@withContext records.filter { it.date == date }
    }
    
    suspend fun addBatch(batch: Batch) = withContext(Dispatchers.IO) {
        val batches = LocalStorage.loadBatches(context).toMutableList()
        batches.add(batch)
        LocalStorage.saveBatches(context, batches)
    }
    
    suspend fun addInboundRecord(record: InboundRecord) = withContext(Dispatchers.IO) {
        val records = LocalStorage.loadInboundRecords(context).toMutableList()
        // If a record with this barcode already exists for this date, replace it
        val existingIndex = records.indexOfFirst { it.date == record.date && it.barcode == record.barcode }
        if (existingIndex >= 0) {
            records[existingIndex] = record
        } else {
            records.add(record)
        }
        LocalStorage.saveInboundRecords(context, records)
    }
    
    suspend fun logAction(auditLog: AuditLog) = withContext(Dispatchers.IO) {
        val logs = LocalStorage.loadAuditLogs(context).toMutableList()
        logs.add(auditLog)
        LocalStorage.saveAuditLogs(context, logs)
    }
    
    suspend fun getAuditLogs(): List<AuditLog> = withContext(Dispatchers.IO) {
        return@withContext LocalStorage.loadAuditLogs(context)
    }
    
    suspend fun getCompletionStatus(date: String): Boolean = withContext(Dispatchers.IO) {
        val records = LocalStorage.loadInboundRecords(context)
        val recordsForDate = records.filter { it.date == date }
        // Check if any record for this date has "Incomplete" status
        return@withContext !recordsForDate.any { record -> 
            record.notes?.contains("incomplete", ignoreCase = true) == true
        }
    }
}
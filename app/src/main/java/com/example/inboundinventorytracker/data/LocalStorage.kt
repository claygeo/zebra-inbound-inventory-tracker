package com.example.inboundinventorytracker.data

import android.content.Context
import com.example.inboundinventorytracker.data.model.AuditLog
import com.example.inboundinventorytracker.data.model.Batch
import com.example.inboundinventorytracker.data.model.InboundRecord
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import java.io.File

object LocalStorage {
    private const val BATCHES_FILE = "batches.json"
    private const val INBOUND_FILE = "inbound_records.json"
    private const val AUDIT_FILE = "audit_logs.json"

    private val json = Json { ignoreUnknownKeys = true }

    fun saveBatches(context: Context, batches: List<Batch>) {
        val file = File(context.filesDir, BATCHES_FILE)
        file.writeText(json.encodeToString(batches))
    }

    fun loadBatches(context: Context): List<Batch> {
        val file = File(context.filesDir, BATCHES_FILE)
        return if (file.exists()) {
            file.inputStream().use { json.decodeFromStream(it) }
        } else {
            emptyList()
        }
    }

    fun saveInboundRecords(context: Context, records: List<InboundRecord>) {
        val file = File(context.filesDir, INBOUND_FILE)
        file.writeText(json.encodeToString(records))
    }

    fun loadInboundRecords(context: Context): List<InboundRecord> {
        val file = File(context.filesDir, INBOUND_FILE)
        return if (file.exists()) {
            file.inputStream().use { json.decodeFromStream(it) }
        } else {
            emptyList()
        }
    }

    fun saveAuditLogs(context: Context, logs: List<AuditLog>) {
        val file = File(context.filesDir, AUDIT_FILE)
        file.writeText(json.encodeToString(logs))
    }

    fun loadAuditLogs(context: Context): List<AuditLog> {
        val file = File(context.filesDir, AUDIT_FILE)
        return if (file.exists()) {
            file.inputStream().use { json.decodeFromStream(it) }
        } else {
            emptyList()
        }
    }
}
package com.example.inboundinventorytracker.util

import android.content.Context
import com.example.inboundinventorytracker.data.model.AuditLog
import com.example.inboundinventorytracker.data.repository.InventoryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object AuditTrailLogger {
    private lateinit var repository: InventoryRepository

    fun initialize(context: Context) {
        repository = InventoryRepository(context)
    }

    private fun formatToEst12Hour(isoTimestamp: String? = null): String {
        val time = isoTimestamp?.let { OffsetDateTime.parse(it) } ?: OffsetDateTime.now(ZoneId.of("UTC"))
        val estTime = time.withOffsetSameInstant(ZoneId.of("America/New_York").rules.getOffset(time.toInstant()))
        return estTime.format(DateTimeFormatter.ofPattern("MM-dd-yyyy hh:mm:ss a"))
    }

    fun log(user: String, action: String, details: String?) {
        CoroutineScope(Dispatchers.IO).launch {
            val formattedDetails = if (details != null) {
                "$details (at ${formatToEst12Hour()} EST)"
            } else {
                "Action at ${formatToEst12Hour()} EST"
            }
            val auditLog = AuditLog(
                created_by = user,
                action = action,
                timestamp = OffsetDateTime.now().toString(),
                details = formattedDetails
            )
            repository.logAction(auditLog)
        }
    }
}
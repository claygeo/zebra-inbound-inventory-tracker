package com.example.inboundinventorytracker.data.model

import kotlinx.serialization.Serializable

@Serializable
data class AuditLog(
    val id: Long? = null,
    val created_by: String,
    val action: String,
    val timestamp: String? = null,
    val details: String?
)
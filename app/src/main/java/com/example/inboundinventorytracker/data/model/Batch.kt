package com.example.inboundinventorytracker.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Batch(
    val id: Long? = null,
    val date: String,
    val skus: List<String>,
    val created_by: String,
    val created_at: String? = null
)
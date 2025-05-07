package com.example.inboundinventorytracker.data.model

import kotlinx.serialization.Serializable

@Serializable
data class InboundRecord(
    val id: Long? = null,
    val date: String,
    val barcode: String,
    val identifier: String,
    val last_4_barcode: String,
    val units_per_case: Int,
    val box_count: Int,
    val partial_case_1: Int,
    val partial_case_2: Int,
    val partial_case_3: Int,
    val partial_case_4: Int,
    val total_unit_count: Int,
    val subcategory: String,
    val ftp_cases: Int,
    val ocala_cases: Int,
    val ftp_units: Int,
    val ocala_units: Int,
    val box_size: String,
    val verified_total_unit_count: Int,
    val notes: String?,
    val created_by: String,
    val created_at: String? = null
)
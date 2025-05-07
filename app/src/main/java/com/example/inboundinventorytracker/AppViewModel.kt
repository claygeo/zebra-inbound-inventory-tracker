package com.example.inboundinventorytracker

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inboundinventorytracker.data.model.Batch
import com.example.inboundinventorytracker.data.model.InboundRecord
import com.example.inboundinventorytracker.data.repository.InventoryRepository
import kotlinx.coroutines.launch

class AppViewModel : ViewModel() {
    var currentUser by mutableStateOf<String?>(null)
    var selectedDate by mutableStateOf<String?>(null)
    var selectedBatch by mutableStateOf<Batch?>(null)
    var isLoggedIn by mutableStateOf(false)
    var batchStatus by mutableStateOf<Map<String, Boolean>>(emptyMap())
    
    private var repository: InventoryRepository? = null
    
    fun initialize(repository: InventoryRepository) {
        this.repository = repository
    }
    
    fun login(user: String) {
        currentUser = user
        isLoggedIn = true
    }
    
    fun logout() {
        currentUser = null
        isLoggedIn = false
        selectedDate = null
        selectedBatch = null
    }
    
    fun selectDate(date: String) {
        selectedDate = date
    }
    
    fun selectBatch(batch: Batch) {
        selectedBatch = batch
    }
    
    fun clearSelectedBatch() {
        selectedBatch = null
    }
    
    fun updateBatchCompletionStatuses() {
        viewModelScope.launch {
            repository?.let { repo ->
                val batches = repo.getBatches()
                val statusMap = mutableMapOf<String, Boolean>()
                
                batches.forEach { batch ->
                    statusMap[batch.date] = repo.getCompletionStatus(batch.date)
                }
                
                batchStatus = statusMap
            }
        }
    }
}
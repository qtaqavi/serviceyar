package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.ServiceLogEntity
import com.example.data.model.ServiceScheduleEntity
import com.example.data.model.ToolEntity
import com.example.data.model.ToolWithServices
import com.example.data.repository.ToolRepository
import com.example.notifications.ReminderNotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ToolViewModel(
    application: Application,
    private val repository: ToolRepository
) : AndroidViewModel(application) {

    val toolsWithServices: StateFlow<List<ToolWithServices>> =
        repository.allToolsWithServices.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allSchedules: StateFlow<List<ServiceScheduleEntity>> =
        repository.allSchedules.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allLogs: StateFlow<List<ServiceLogEntity>> =
        repository.allLogs.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        viewModelScope.launch {
            repository.ensureInitialDataIfEmpty()
        }
    }

    fun insertTool(tool: ToolEntity, onComplete: ((Long) -> Unit)? = null) {
        viewModelScope.launch {
            val id = repository.insertTool(tool)
            onComplete?.invoke(id)
        }
    }

    fun updateTool(tool: ToolEntity) {
        viewModelScope.launch {
            repository.updateTool(tool)
        }
    }

    fun deleteTool(tool: ToolEntity) {
        viewModelScope.launch {
            repository.deleteTool(tool)
        }
    }

    fun insertSchedule(schedule: ServiceScheduleEntity, onComplete: ((Long) -> Unit)? = null) {
        viewModelScope.launch {
            val id = repository.insertSchedule(schedule)
            onComplete?.invoke(id)
        }
    }

    fun updateSchedule(schedule: ServiceScheduleEntity) {
        viewModelScope.launch {
            repository.updateSchedule(schedule)
        }
    }

    fun deleteSchedule(schedule: ServiceScheduleEntity) {
        viewModelScope.launch {
            repository.deleteSchedule(schedule)
        }
    }

    fun markServiceDone(
        scheduleId: Long,
        performedDateJalali: String,
        actualCost: Long,
        technicianOrShop: String,
        invoiceNumber: String,
        partsReplaced: String,
        notes: String
    ) {
        viewModelScope.launch {
            repository.markServiceAsDone(
                scheduleId = scheduleId,
                performedDateJalali = performedDateJalali,
                actualCost = actualCost,
                technicianOrShop = technicianOrShop,
                invoiceNumber = invoiceNumber,
                partsReplaced = partsReplaced,
                notes = notes
            )
        }
    }

    fun resetToSampleData() {
        viewModelScope.launch {
            repository.resetToSampleData()
        }
    }

    fun triggerTestNotification(customMessage: String? = null) {
        ReminderNotificationHelper.showTestNotification(getApplication(), customMessage)
    }

    companion object {
        fun provideFactory(application: Application): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val appScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
                    val database = AppDatabase.getInstance(application, appScope)
                    val repository = ToolRepository(database.toolDao())
                    return ToolViewModel(application, repository) as T
                }
            }
    }
}

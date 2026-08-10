package com.example.data.repository

import com.example.data.local.SampleData
import com.example.data.local.ToolDao
import com.example.data.model.IntervalType
import com.example.data.model.ServiceLogEntity
import com.example.data.model.ServiceScheduleEntity
import com.example.data.model.ToolEntity
import com.example.data.model.ToolWithServices
import com.example.util.JalaliCalendar
import com.example.util.JalaliDate
import kotlinx.coroutines.flow.Flow

class ToolRepository(private val toolDao: ToolDao) {

    val allToolsWithServices: Flow<List<ToolWithServices>> = toolDao.getAllToolsWithServices()
    val allSchedules: Flow<List<ServiceScheduleEntity>> = toolDao.getAllSchedules()
    val allLogs: Flow<List<ServiceLogEntity>> = toolDao.getAllLogs()

    fun getToolWithServices(toolId: Long): Flow<ToolWithServices?> =
        toolDao.getToolWithServicesById(toolId)

    fun getLogsForTool(toolId: Long): Flow<List<ServiceLogEntity>> =
        toolDao.getLogsForTool(toolId)

    suspend fun insertTool(tool: ToolEntity): Long = toolDao.insertTool(tool)

    suspend fun updateTool(tool: ToolEntity) = toolDao.updateTool(tool)

    suspend fun deleteTool(tool: ToolEntity) = toolDao.deleteTool(tool)

    suspend fun deleteToolById(toolId: Long) = toolDao.deleteToolById(toolId)

    suspend fun insertSchedule(schedule: ServiceScheduleEntity): Long =
        toolDao.insertSchedule(schedule)

    suspend fun updateSchedule(schedule: ServiceScheduleEntity) =
        toolDao.updateSchedule(schedule)

    suspend fun deleteSchedule(schedule: ServiceScheduleEntity) =
        toolDao.deleteSchedule(schedule)

    suspend fun deleteScheduleById(scheduleId: Long) =
        toolDao.deleteScheduleById(scheduleId)

    suspend fun insertLog(log: ServiceLogEntity): Long = toolDao.insertLog(log)

    suspend fun deleteLog(log: ServiceLogEntity) = toolDao.deleteLog(log)

    /**
     * Executes the smart "Mark Service as Done" logic:
     * 1. Records a new ServiceLogEntity in database.
     * 2. Automatically advances nextServiceDate based on the interval.
     * 3. Updates lastServiceDate and timestamp.
     */
    suspend fun markServiceAsDone(
        scheduleId: Long,
        performedDateJalali: String,
        actualCost: Long,
        technicianOrShop: String,
        invoiceNumber: String,
        partsReplaced: String,
        notes: String
    ): Boolean {
        val schedule = toolDao.getScheduleById(scheduleId) ?: return false

        // 1. Insert history log
        val log = ServiceLogEntity(
            toolId = schedule.toolId,
            toolName = schedule.toolName,
            serviceScheduleId = schedule.id,
            serviceTitle = schedule.title,
            performedDateJalali = performedDateJalali,
            actualCost = actualCost,
            technicianOrShop = technicianOrShop.ifBlank { schedule.technicianName },
            invoiceNumber = invoiceNumber,
            partsReplaced = partsReplaced,
            notes = notes,
            timestamp = System.currentTimeMillis()
        )
        toolDao.insertLog(log)

        // 2. Compute next service date based on interval
        val performedJalali = JalaliCalendar.parse(performedDateJalali) ?: JalaliCalendar.now()
        val intervalType = schedule.getIntervalType()

        val nextJalali = when (intervalType) {
            IntervalType.MONTHLY -> JalaliCalendar.addMonths(performedJalali, 1)
            IntervalType.QUARTERLY -> JalaliCalendar.addMonths(performedJalali, 3)
            IntervalType.BIANNUAL -> JalaliCalendar.addMonths(performedJalali, 6)
            IntervalType.ANNUAL -> JalaliCalendar.addMonths(performedJalali, 12)
            IntervalType.BIENNIAL -> JalaliCalendar.addMonths(performedJalali, 24)
            IntervalType.CUSTOM_DAYS -> JalaliCalendar.addDays(performedJalali, schedule.customIntervalDays.coerceAtLeast(1))
        }

        // 3. Update the schedule
        val updatedSchedule = schedule.copy(
            lastServiceDateJalali = performedDateJalali,
            nextServiceDateJalali = nextJalali.toStandardString(),
            isCompleted = true,
            lastCompletedTimestamp = System.currentTimeMillis()
        )
        toolDao.updateSchedule(updatedSchedule)

        return true
    }

    suspend fun resetToSampleData() {
        toolDao.clearAllLogs()
        toolDao.clearAllSchedules()
        toolDao.clearAllTools()

        val (tools, schedules, logs) = SampleData.generateInitialData()
        toolDao.insertAllTools(tools)
        toolDao.insertAllSchedules(schedules)
        toolDao.insertAllLogs(logs)
    }

    suspend fun ensureInitialDataIfEmpty() {
        if (toolDao.getToolsCount() == 0) {
            val (tools, schedules, logs) = SampleData.generateInitialData()
            toolDao.insertAllTools(tools)
            toolDao.insertAllSchedules(schedules)
            toolDao.insertAllLogs(logs)
        }
    }
}

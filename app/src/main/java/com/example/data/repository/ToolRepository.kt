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
     * 1. Records a new ServiceLogEntity in database with performed date and odometer.
     * 2. Automatically advances nextServiceDate based on interval and nextServiceOdometerKm.
     * 3. Updates lastServiceDate, odometer, and timestamp.
     */
    suspend fun markServiceAsDone(
        scheduleId: Long,
        performedDateJalali: String,
        actualCost: Long,
        technicianOrShop: String,
        invoiceNumber: String,
        partsReplaced: String,
        notes: String,
        performedOdometerKm: Int = 0
    ): Boolean {
        val schedule = toolDao.getScheduleById(scheduleId) ?: return false

        // 1. Insert history log
        val log = ServiceLogEntity(
            toolId = schedule.toolId,
            toolName = schedule.toolName,
            serviceScheduleId = schedule.id,
            serviceTitle = schedule.title,
            performedDateJalali = performedDateJalali,
            performedOdometerKm = performedOdometerKm,
            actualCost = actualCost,
            technicianOrShop = technicianOrShop.ifBlank { schedule.technicianName },
            invoiceNumber = invoiceNumber,
            partsReplaced = partsReplaced,
            notes = notes,
            timestamp = System.currentTimeMillis()
        )
        toolDao.insertLog(log)

        // 2. Compute next service date based on daily mileage (if set) or interval
        val performedJalali = JalaliCalendar.parse(performedDateJalali) ?: JalaliCalendar.now()
        val intervalType = schedule.getIntervalType()

        val nextJalali = if (schedule.intervalKilometers > 0 && schedule.dailyKilometers > 0) {
            val cycleDays = ((schedule.intervalKilometers + schedule.dailyKilometers - 1) / schedule.dailyKilometers).coerceAtLeast(1)
            JalaliCalendar.addDays(performedJalali, cycleDays)
        } else {
            when (intervalType) {
                IntervalType.MONTHLY -> JalaliCalendar.addMonths(performedJalali, 1)
                IntervalType.QUARTERLY -> JalaliCalendar.addMonths(performedJalali, 3)
                IntervalType.BIANNUAL -> JalaliCalendar.addMonths(performedJalali, 6)
                IntervalType.ANNUAL -> JalaliCalendar.addMonths(performedJalali, 12)
                IntervalType.BIENNIAL -> JalaliCalendar.addMonths(performedJalali, 24)
                IntervalType.CUSTOM_DAYS -> JalaliCalendar.addDays(performedJalali, schedule.customIntervalDays.coerceAtLeast(1))
            }
        }

        // 3. Compute next service odometer if vehicle has kilometer interval
        val nextKm = if (schedule.intervalKilometers > 0) {
            val baseKm = if (performedOdometerKm > 0) performedOdometerKm else schedule.nextServiceOdometerKm
            if (baseKm > 0) baseKm + schedule.intervalKilometers else schedule.intervalKilometers
        } else {
            schedule.nextServiceOdometerKm
        }

        // 4. Update the schedule
        val updatedSchedule = schedule.copy(
            lastServiceDateJalali = performedDateJalali,
            nextServiceDateJalali = nextJalali.toStandardString(),
            lastServiceOdometerKm = if (performedOdometerKm > 0) performedOdometerKm else schedule.lastServiceOdometerKm,
            nextServiceOdometerKm = nextKm,
            isCompleted = true,
            lastCompletedTimestamp = System.currentTimeMillis()
        )
        toolDao.updateSchedule(updatedSchedule)

        // 5. Update vehicle current odometer if higher
        if (performedOdometerKm > 0) {
            val tool = toolDao.getToolById(schedule.toolId)
            if (tool != null && performedOdometerKm > tool.currentOdometerKm) {
                toolDao.updateTool(tool.copy(currentOdometerKm = performedOdometerKm))
            }
        }

        return true
    }

    suspend fun updateVehicleOdometer(toolId: Long, newOdometerKm: Int) {
        val tool = toolDao.getToolById(toolId) ?: return
        toolDao.updateTool(tool.copy(currentOdometerKm = newOdometerKm))
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

    suspend fun getBackupSnapshot(): Triple<List<ToolEntity>, List<ServiceScheduleEntity>, List<ServiceLogEntity>> {
        val tools = toolDao.getAllToolsSnapshot()
        val schedules = toolDao.getAllSchedulesSnapshot()
        val logs = toolDao.getAllLogsSnapshot()
        return Triple(tools, schedules, logs)
    }

    suspend fun restoreBackupData(
        tools: List<ToolEntity>,
        schedules: List<ServiceScheduleEntity>,
        logs: List<ServiceLogEntity>,
        replaceExisting: Boolean
    ) {
        if (replaceExisting) {
            toolDao.clearAllLogs()
            toolDao.clearAllSchedules()
            toolDao.clearAllTools()

            toolDao.insertAllTools(tools)
            val validToolIds = tools.map { it.id }.toSet()
            val validSchedules = schedules.filter { it.toolId in validToolIds }
            toolDao.insertAllSchedules(validSchedules)
            val validLogs = logs.filter { it.toolId in validToolIds }
            toolDao.insertAllLogs(validLogs)
        } else {
            val existingTools = toolDao.getAllToolsSnapshot()
            val toolIdMap = mutableMapOf<Long, Long>()

            for (importedTool in tools) {
                val matchingExisting = existingTools.firstOrNull {
                    it.name == importedTool.name && it.serialNumber == importedTool.serialNumber
                }
                val targetToolId = if (matchingExisting != null) {
                    toolDao.updateTool(importedTool.copy(id = matchingExisting.id))
                    matchingExisting.id
                } else {
                    toolDao.insertTool(importedTool.copy(id = 0))
                }
                toolIdMap[importedTool.id] = targetToolId
            }

            val existingSchedules = toolDao.getAllSchedulesSnapshot()
            val scheduleIdMap = mutableMapOf<Long, Long>()

            for (importedSchedule in schedules) {
                val mappedToolId = toolIdMap[importedSchedule.toolId] ?: continue
                val matchingSchedule = existingSchedules.firstOrNull {
                    it.toolId == mappedToolId && it.title == importedSchedule.title
                }
                val targetScheduleId = if (matchingSchedule != null) {
                    toolDao.updateSchedule(
                        importedSchedule.copy(
                            id = matchingSchedule.id,
                            toolId = mappedToolId
                        )
                    )
                    matchingSchedule.id
                } else {
                    toolDao.insertSchedule(
                        importedSchedule.copy(
                            id = 0,
                            toolId = mappedToolId
                        )
                    )
                }
                scheduleIdMap[importedSchedule.id] = targetScheduleId
            }

            val existingLogs = toolDao.getAllLogsSnapshot()
            for (importedLog in logs) {
                val mappedToolId = toolIdMap[importedLog.toolId] ?: continue
                val mappedScheduleId = scheduleIdMap[importedLog.serviceScheduleId] ?: 0L
                val alreadyExists = existingLogs.any {
                    it.toolId == mappedToolId &&
                        it.serviceTitle == importedLog.serviceTitle &&
                        it.performedDateJalali == importedLog.performedDateJalali &&
                        it.actualCost == importedLog.actualCost
                }
                if (!alreadyExists) {
                    toolDao.insertLog(
                        importedLog.copy(
                            id = 0,
                            toolId = mappedToolId,
                            serviceScheduleId = mappedScheduleId
                        )
                    )
                }
            }
        }
    }
}

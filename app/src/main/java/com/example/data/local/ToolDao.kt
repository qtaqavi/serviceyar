package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.model.ServiceLogEntity
import com.example.data.model.ServiceScheduleEntity
import com.example.data.model.ToolEntity
import com.example.data.model.ToolWithServices
import kotlinx.coroutines.flow.Flow

@Dao
interface ToolDao {

    @Transaction
    @Query("SELECT * FROM tools WHERE isArchived = 0 ORDER BY id DESC")
    fun getAllToolsWithServices(): Flow<List<ToolWithServices>>

    @Transaction
    @Query("SELECT * FROM tools WHERE id = :toolId")
    fun getToolWithServicesById(toolId: Long): Flow<ToolWithServices?>

    @Query("SELECT * FROM tools WHERE id = :toolId")
    suspend fun getToolById(toolId: Long): ToolEntity?

    @Query("SELECT * FROM service_schedules ORDER BY id DESC")
    fun getAllSchedules(): Flow<List<ServiceScheduleEntity>>

    @Query("SELECT * FROM service_schedules WHERE toolId = :toolId")
    fun getSchedulesForTool(toolId: Long): Flow<List<ServiceScheduleEntity>>

    @Query("SELECT * FROM service_schedules WHERE id = :scheduleId")
    suspend fun getScheduleById(scheduleId: Long): ServiceScheduleEntity?

    @Query("SELECT * FROM service_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<ServiceLogEntity>>

    @Query("SELECT * FROM service_logs WHERE toolId = :toolId ORDER BY timestamp DESC")
    fun getLogsForTool(toolId: Long): Flow<List<ServiceLogEntity>>

    @Query("SELECT COUNT(*) FROM tools")
    suspend fun getToolsCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTool(tool: ToolEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllTools(tools: List<ToolEntity>): List<Long>

    @Update
    suspend fun updateTool(tool: ToolEntity)

    @Delete
    suspend fun deleteTool(tool: ToolEntity)

    @Query("DELETE FROM tools WHERE id = :toolId")
    suspend fun deleteToolById(toolId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: ServiceScheduleEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllSchedules(schedules: List<ServiceScheduleEntity>): List<Long>

    @Update
    suspend fun updateSchedule(schedule: ServiceScheduleEntity)

    @Delete
    suspend fun deleteSchedule(schedule: ServiceScheduleEntity)

    @Query("DELETE FROM service_schedules WHERE id = :scheduleId")
    suspend fun deleteScheduleById(scheduleId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: ServiceLogEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllLogs(logs: List<ServiceLogEntity>): List<Long>

    @Delete
    suspend fun deleteLog(log: ServiceLogEntity)

    @Query("DELETE FROM service_logs WHERE id = :logId")
    suspend fun deleteLogById(logId: Long)

    @Query("DELETE FROM tools")
    suspend fun clearAllTools()

    @Query("DELETE FROM service_schedules")
    suspend fun clearAllSchedules()

    @Query("DELETE FROM service_logs")
    suspend fun clearAllLogs()
}

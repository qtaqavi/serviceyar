package com.example.data.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.example.util.JalaliCalendar
import com.example.util.JalaliDate

@Entity(tableName = "tools")
data class ToolEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val categoryName: String = ToolCategory.HOME_APPLIANCE.name,
    val modelOrBrand: String = "",
    val location: String = "",
    val serialNumber: String = "",
    val purchaseDateJalali: String = "",
    val purchasePrice: Long = 0L,
    val notes: String = "",
    val iconName: String = "",
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun getCategory(): ToolCategory = ToolCategory.fromName(categoryName)
}

@Entity(
    tableName = "service_schedules",
    foreignKeys = [
        ForeignKey(
            entity = ToolEntity::class,
            parentColumns = ["id"],
            childColumns = ["toolId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["toolId"])]
)
data class ServiceScheduleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val toolId: Long,
    val toolName: String = "",
    val title: String,
    val serviceTypeName: String = ServiceType.PERIODIC_GENERAL.name,
    val intervalTypeName: String = IntervalType.ANNUAL.name,
    val customIntervalDays: Int = 30,
    val lastServiceDateJalali: String = "",
    val nextServiceDateJalali: String = "",
    val expiryDateJalali: String = "",
    val priorityName: String = ServicePriority.MEDIUM.name,
    val estimatedCost: Long = 0L,
    val technicianName: String = "",
    val technicianPhone: String = "",
    val reminderDaysBefore: Int = 3,
    val notes: String = "",
    val isCompleted: Boolean = false,
    val lastCompletedTimestamp: Long = 0L
) {
    fun getServiceType(): ServiceType = ServiceType.fromName(serviceTypeName)
    fun getIntervalType(): IntervalType = IntervalType.fromName(intervalTypeName)
    fun getPriority(): ServicePriority = ServicePriority.fromName(priorityName)

    /**
     * Determines current operational status based on next service date and Jalali now.
     */
    fun computeStatus(currentJalali: JalaliDate = JalaliCalendar.now()): ServiceStatus {
        val nextDate = JalaliCalendar.parse(nextServiceDateJalali)
        val expiryDate = JalaliCalendar.parse(expiryDateJalali)

        if (expiryDate != null && JalaliCalendar.daysBetween(currentJalali, expiryDate) < 0) {
            return ServiceStatus.EXPIRED_WARRANTY
        }

        if (nextDate == null) {
            return ServiceStatus.NO_SCHEDULE
        }

        val diffDays = JalaliCalendar.daysBetween(currentJalali, nextDate)
        return when {
            diffDays < 0 -> ServiceStatus.OVERDUE
            diffDays <= reminderDaysBefore.coerceAtLeast(15) -> ServiceStatus.DUE_SOON
            else -> ServiceStatus.UP_TO_DATE
        }
    }

    fun getDaysUntilNext(currentJalali: JalaliDate = JalaliCalendar.now()): Int {
        val nextDate = JalaliCalendar.parse(nextServiceDateJalali) ?: return 9999
        return JalaliCalendar.daysBetween(currentJalali, nextDate)
    }
}

@Entity(
    tableName = "service_logs",
    foreignKeys = [
        ForeignKey(
            entity = ToolEntity::class,
            parentColumns = ["id"],
            childColumns = ["toolId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["toolId"])]
)
data class ServiceLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val toolId: Long,
    val toolName: String = "",
    val serviceScheduleId: Long = 0L,
    val serviceTitle: String,
    val performedDateJalali: String,
    val actualCost: Long = 0L,
    val technicianOrShop: String = "",
    val invoiceNumber: String = "",
    val partsReplaced: String = "",
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class ToolWithServices(
    @Embedded val tool: ToolEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "toolId"
    )
    val schedules: List<ServiceScheduleEntity>,
    @Relation(
        parentColumn = "id",
        entityColumn = "toolId"
    )
    val logs: List<ServiceLogEntity>
) {
    fun getOverallStatus(currentJalali: JalaliDate = JalaliCalendar.now()): ServiceStatus {
        if (schedules.isEmpty()) return ServiceStatus.NO_SCHEDULE

        val statuses = schedules.map { it.computeStatus(currentJalali) }
        return when {
            statuses.contains(ServiceStatus.OVERDUE) -> ServiceStatus.OVERDUE
            statuses.contains(ServiceStatus.DUE_SOON) -> ServiceStatus.DUE_SOON
            statuses.contains(ServiceStatus.EXPIRED_WARRANTY) -> ServiceStatus.EXPIRED_WARRANTY
            statuses.all { it == ServiceStatus.UP_TO_DATE } -> ServiceStatus.UP_TO_DATE
            else -> ServiceStatus.UP_TO_DATE
        }
    }

    fun getNearestUpcomingSchedule(currentJalali: JalaliDate = JalaliCalendar.now()): ServiceScheduleEntity? {
        return schedules.minByOrNull { it.getDaysUntilNext(currentJalali) }
    }
}

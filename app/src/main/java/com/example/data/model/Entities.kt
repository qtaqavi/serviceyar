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
    val currentOdometerKm: Int = 0,
    val purchaseDateJalali: String = "",
    val purchasePrice: Long = 0L,
    val notes: String = "",
    val iconName: String = "",
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun getCategory(): ToolCategory = ToolCategory.fromName(categoryName)
    fun getCategoryUi(): CategoryUiModel = CustomCategoryRegistry.resolve(categoryName)
    fun isVehicle(): Boolean {
        if (ToolCategory.isExplicitlyNonVehicleText(name) || ToolCategory.isExplicitlyNonVehicleText(modelOrBrand)) {
            return false
        }
        val catUi = getCategoryUi()
        if (catUi.isVehicleType) return true
        return ToolCategory.isVehicleText(name) || ToolCategory.isVehicleText(modelOrBrand)
    }
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
    val intervalKilometers: Int = 0,
    val dailyKilometers: Int = 0,
    val lastServiceOdometerKm: Int = 0,
    val nextServiceOdometerKm: Int = 0,
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
     * Calculates effective elapsed days since last service, synchronized with daily mileage countdown
     * so that at the new service alert date, accumulated mileage equals intervalKilometers.
     */
    fun getEffectiveElapsedDays(currentJalali: JalaliDate = JalaliCalendar.now()): Int {
        val lastDate = JalaliCalendar.parse(lastServiceDateJalali)
        val elapsedFromLast = if (lastDate != null) {
            JalaliCalendar.daysBetween(lastDate, currentJalali).coerceAtLeast(0)
        } else 0

        if (intervalKilometers > 0 && dailyKilometers > 0) {
            val totalCycleDays = ((intervalKilometers + dailyKilometers - 1) / dailyKilometers).coerceAtLeast(1)
            val nextDate = JalaliCalendar.parse(nextServiceDateJalali)
            if (nextDate != null) {
                val daysUntilNext = JalaliCalendar.daysBetween(currentJalali, nextDate)
                val elapsedByCountdown = (totalCycleDays - daysUntilNext).coerceAtLeast(0)
                return maxOf(elapsedFromLast, elapsedByCountdown)
            }
        }
        return elapsedFromLast
    }

    /**
     * Total kilometers accumulated/deducted from the service interval based on daily mileage or odometer.
     */
    fun getTraveledKilometers(
        currentOdometerKm: Int = 0,
        currentJalali: JalaliDate = JalaliCalendar.now()
    ): Int {
        if (intervalKilometers > 0 && dailyKilometers > 0) {
            val elapsedDays = getEffectiveElapsedDays(currentJalali)
            return (elapsedDays * dailyKilometers).coerceAtLeast(0)
        }
        if (currentOdometerKm > 0 && lastServiceOdometerKm > 0 && currentOdometerKm >= lastServiceOdometerKm) {
            return currentOdometerKm - lastServiceOdometerKm
        }
        return 0
    }

    /**
     * Remaining kilometers in the service interval after deducting daily mileage (or current odometer).
     */
    fun getRemainingKilometers(
        currentOdometerKm: Int = 0,
        currentJalali: JalaliDate = JalaliCalendar.now()
    ): Int? {
        if (intervalKilometers > 0 && dailyKilometers > 0) {
            val traveled = getTraveledKilometers(currentOdometerKm, currentJalali)
            return intervalKilometers - traveled
        }
        if (nextServiceOdometerKm > 0 && currentOdometerKm > 0) {
            return nextServiceOdometerKm - currentOdometerKm
        }
        if (intervalKilometers > 0) {
            return intervalKilometers
        }
        return null
    }

    /**
     * Determines current operational status based on next service date, Jalali now,
     * and vehicle kilometer interval / daily mileage deduction.
     */
    fun computeStatus(
        currentJalali: JalaliDate = JalaliCalendar.now(),
        currentOdometerKm: Int = 0,
        isVehicleTool: Boolean = !ToolCategory.isExplicitlyNonVehicleText(toolName)
    ): ServiceStatus {
        val nextDate = JalaliCalendar.parse(nextServiceDateJalali)
        val expiryDate = JalaliCalendar.parse(expiryDateJalali)

        if (expiryDate != null && JalaliCalendar.daysBetween(currentJalali, expiryDate) < 0) {
            return ServiceStatus.EXPIRED_WARRANTY
        }

        // Check kilometer interval status ONLY for vehicles
        val remainingKm = if (isVehicleTool) getRemainingKilometers(currentOdometerKm, currentJalali) else null
        val kmStatus = if (isVehicleTool && remainingKm != null && (dailyKilometers > 0 || currentOdometerKm > 0)) {
            val dueSoonThresholdKm = if (dailyKilometers > 0) {
                (dailyKilometers * reminderDaysBefore.coerceAtLeast(7)).coerceAtLeast(300)
            } else {
                1000
            }
            when {
                remainingKm <= 0 -> ServiceStatus.OVERDUE
                remainingKm <= dueSoonThresholdKm -> ServiceStatus.DUE_SOON
                else -> ServiceStatus.UP_TO_DATE
            }
        } else {
            null
        }

        if (kmStatus == ServiceStatus.OVERDUE) return ServiceStatus.OVERDUE

        if (nextDate == null) {
            return kmStatus ?: ServiceStatus.NO_SCHEDULE
        }

        val diffDays = JalaliCalendar.daysBetween(currentJalali, nextDate)
        val dateStatus = when {
            diffDays < 0 -> ServiceStatus.OVERDUE
            diffDays <= reminderDaysBefore.coerceAtLeast(15) -> ServiceStatus.DUE_SOON
            else -> ServiceStatus.UP_TO_DATE
        }

        return when {
            dateStatus == ServiceStatus.OVERDUE || kmStatus == ServiceStatus.OVERDUE -> ServiceStatus.OVERDUE
            dateStatus == ServiceStatus.DUE_SOON || kmStatus == ServiceStatus.DUE_SOON -> ServiceStatus.DUE_SOON
            else -> ServiceStatus.UP_TO_DATE
        }
    }

    fun getDaysUntilNext(
        currentJalali: JalaliDate = JalaliCalendar.now(),
        isVehicleTool: Boolean = !ToolCategory.isExplicitlyNonVehicleText(toolName)
    ): Int {
        if (isVehicleTool && intervalKilometers > 0 && dailyKilometers > 0) {
            val remainingKm = getRemainingKilometers(0, currentJalali) ?: 0
            val nextDate = JalaliCalendar.parse(nextServiceDateJalali)
            if (nextDate != null) {
                return JalaliCalendar.daysBetween(currentJalali, nextDate)
            }
            return if (remainingKm >= 0) {
                (remainingKm + dailyKilometers - 1) / dailyKilometers
            } else {
                remainingKm / dailyKilometers
            }
        }
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
    val performedOdometerKm: Int = 0,
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
    fun getOverallStatus(
        currentJalali: JalaliDate = JalaliCalendar.now(),
        currentKm: Int = tool.currentOdometerKm
    ): ServiceStatus {
        if (schedules.isEmpty()) return ServiceStatus.NO_SCHEDULE
        val isVeh = tool.isVehicle()
        val effectiveKm = if (isVeh) currentKm else 0

        val statuses = schedules.map { it.computeStatus(currentJalali, effectiveKm, isVeh) }
        return when {
            statuses.contains(ServiceStatus.OVERDUE) -> ServiceStatus.OVERDUE
            statuses.contains(ServiceStatus.DUE_SOON) -> ServiceStatus.DUE_SOON
            statuses.contains(ServiceStatus.EXPIRED_WARRANTY) -> ServiceStatus.EXPIRED_WARRANTY
            statuses.all { it == ServiceStatus.UP_TO_DATE } -> ServiceStatus.UP_TO_DATE
            else -> ServiceStatus.UP_TO_DATE
        }
    }

    fun getNearestUpcomingSchedule(currentJalali: JalaliDate = JalaliCalendar.now()): ServiceScheduleEntity? {
        val isVeh = tool.isVehicle()
        return schedules.minByOrNull { it.getDaysUntilNext(currentJalali, isVeh) }
    }
}

package com.example.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R
import com.example.util.JalaliCalendar
import com.example.util.JalaliDate

object ReminderNotificationHelper {

    const val CHANNEL_ID = "service_yar_reminders"
    const val CHANNEL_NAME = "یادآوری‌های سرویس و انقضا"
    const val CHANNEL_DESC = "هشدارهای مربوط به موعد سرویس‌های دوره‌ای، انقضای فیلتر و گارانتی تجهیزات"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
                enableVibration(true)
                setShowBadge(true)
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showTestNotification(context: Context, customMessage: String? = null) {
        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val title = "🔔 سرویس‌یار: آزمایش سیستم یادآوری"
        val message = customMessage
            ?: "موعد سرویس «تعویض فیلتر تصفیه آب» و «روغن موتور خودرو» فرا رسیده است. لطفاً جهت جلوگیری از استهلاک اقدام نمایید."

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.img_tool_icon)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(1001, builder.build())
        } catch (e: SecurityException) {
            // Permission not yet granted on Android 13+
        }
    }

    fun showServiceAlert(
        context: Context,
        toolName: String,
        serviceTitle: String,
        dueDateJalali: String,
        isOverdue: Boolean
    ) {
        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context, toolName.hashCode(), intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val title = if (isOverdue) {
            "⚠️ هشدار تاخیر سرویس: $toolName"
        } else {
            "⏰ یادآوری موعد سرویس: $toolName"
        }

        val message = if (isOverdue) {
            "موعد سرویس «$serviceTitle» در تاریخ $dueDateJalali گذشته است! لطفاً جهت بازرسی و سرویس اقدام نمایید."
        } else {
            "موعد انجام «$serviceTitle» در تاریخ $dueDateJalali فرا می‌رسد."
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.img_tool_icon)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            val notificationId = (toolName + serviceTitle).hashCode()
            notificationManager.notify(notificationId, builder.build())
        } catch (e: SecurityException) {
            // Handled gracefully
        }
    }
}

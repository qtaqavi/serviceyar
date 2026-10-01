package com.example.util

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.telephony.SmsManager
import androidx.core.content.ContextCompat

object SmsHelper {

    /**
     * Attempts to send SMS directly if SEND_SMS permission is granted.
     * Otherwise, safely opens the default SMS messaging app with pre-filled number and body.
     */
    fun sendServiceAlertSms(
        context: Context,
        phoneNumber: String,
        message: String,
        forceOpenApp: Boolean = false
    ): Boolean {
        val cleanNumber = JalaliCalendar.toEnglishDigits(phoneNumber).trim()
        if (cleanNumber.isBlank()) return false

        if (!forceOpenApp && ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.SEND_SMS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            try {
                val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    context.getSystemService(SmsManager::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    SmsManager.getDefault()
                }

                // If message is longer than 70 chars (common in Persian), divide message
                val parts = smsManager.divideMessage(message)
                if (parts.size > 1) {
                    smsManager.sendMultipartTextMessage(cleanNumber, null, parts, null, null)
                } else {
                    smsManager.sendTextMessage(cleanNumber, null, message, null, null)
                }
                return true
            } catch (e: Exception) {
                // If direct sending fails, fallback to opening SMS app
            }
        }

        // Standard Android Intent fallback: opens SMS app with pre-filled recipient and text
        return openSmsApp(context, cleanNumber, message)
    }

    fun openSmsApp(context: Context, phoneNumber: String, message: String): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("smsto:$phoneNumber")
                putExtra("sms_body", message)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            try {
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("sms:$phoneNumber")
                    putExtra("sms_body", message)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                true
            } catch (ex: Exception) {
                false
            }
        }
    }

    fun generateServiceReminderText(
        toolName: String,
        serviceTitle: String,
        nextDateJalali: String,
        nextKilometer: Int = 0
    ): String {
        val kmText = if (nextKilometer > 0) " یا کارکرد ${JalaliCalendar.toPersianDigits(nextKilometer)} کیلومتر" else ""
        return "هشدار سرویس‌یار:\nموعد سرویس «$serviceTitle» برای «$toolName» در تاریخ ${JalaliCalendar.toPersianDigits(nextDateJalali)}$kmText فرا رسیده است. لطفاً جهت انجام اقدام فرمایید."
    }
}

package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.ui.components.AutoResizedButtonText
import com.example.util.BackupRestoreManager
import com.example.util.JalaliCalendar
import com.example.util.LocalBackupFileInfo
import java.io.File

@Composable
fun BackupRestoreDialog(
    toolsCount: Int,
    schedulesCount: Int,
    logsCount: Int,
    localBackups: List<LocalBackupFileInfo>,
    onExportToUri: (android.net.Uri) -> Unit,
    onCreateLocalSnapshot: () -> Unit,
    onShareToCloudDrive: (preferGoogleDrive: Boolean) -> Unit,
    onImportFromUri: (android.net.Uri, replaceExisting: Boolean) -> Unit,
    onRestoreLocalFile: (File, replaceExisting: Boolean) -> Unit,
    onDeleteLocalFile: (File) -> Unit,
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current
    var replaceExistingOnImport by remember { mutableStateOf(true) }
    var filePendingRestoreConfirm by remember { mutableStateOf<File?>(null) }

    // SAF CreateDocument for saving Backup JSON to Local Storage or Google Drive
    val exportSafLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            onExportToUri(uri)
        }
    }

    // SAF OpenDocument for importing Backup JSON from Local Storage or Google Drive
    val importSafLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            onImportFromUri(uri, replaceExistingOnImport)
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        AlertDialog(
            onDismissRequest = onDismissRequest,
            shape = RoundedCornerShape(24.dp),
            properties = DialogProperties(usePlatformDefaultWidth = false),
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(0.96f),
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Backup,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Column {
                            Text(
                                text = "پشتیبان‌گیری و بازیابی اطلاعات",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "ذخیره و بازیابی در درایو لوکال و گوگل درایو (Google Drive)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(onClick = onDismissRequest) {
                        Icon(Icons.Default.Close, contentDescription = "بستن")
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Current Data Status Banner
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            BackupStatPill("وسایل ثبت‌شده", "${JalaliCalendar.toPersianDigits(toolsCount)} دستگاه")
                            BackupStatPill("برنامه‌های سرویس", "${JalaliCalendar.toPersianDigits(schedulesCount)} مورد")
                            BackupStatPill("سوابق انجام‌شده", "${JalaliCalendar.toPersianDigits(logsCount)} نوبت")
                        }
                    }

                    // 1. EXPORT SECTION (Local Drive & Cloud Drive / Google Drive)
                    Text(
                        text = "۱. دریافت خروجی پشتیبان (Export):",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Save to Local Storage or Google Drive via System File Picker (SAF)
                            Button(
                                onClick = {
                                    exportSafLauncher.launch(BackupRestoreManager.generateDefaultBackupFileName())
                                },
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("export_backup_saf_button")
                            ) {
                                Icon(Icons.Default.Folder, contentDescription = null, modifier = Modifier.size(17.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                AutoResizedButtonText(
                                    text = "ذخیره فایل پشتیبان در حافظه لوکال یا گوگل درایو",
                                    maxFontSize = 12.5.sp,
                                    minFontSize = 8.5.sp,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Direct Upload / Share to Google Drive
                                Button(
                                    onClick = { onShareToCloudDrive(true) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF059669)
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("export_google_drive_button")
                                ) {
                                    Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(5.dp))
                                    AutoResizedButtonText(
                                        text = "ارسال به گوگل درایو",
                                        maxFontSize = 12.sp,
                                        minFontSize = 8.5.sp,
                                        modifier = Modifier.weight(1f, fill = false)
                                    )
                                }

                                // Quick Local Backup Snapshot
                                OutlinedButton(
                                    onClick = onCreateLocalSnapshot,
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("create_local_backup_button")
                                ) {
                                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(5.dp))
                                    AutoResizedButtonText(
                                        text = "پشتیبان سریع لوکال",
                                        maxFontSize = 12.sp,
                                        minFontSize = 8.5.sp,
                                        modifier = Modifier.weight(1f, fill = false)
                                    )
                                }
                            }

                            Text(
                                text = "راهنما: با انتخاب دکمه اول می‌توانید در پنجره سیستم، پوشه Downloads (حافظه داخلی) یا حساب Google Drive خود را برای ذخیره‌سازی انتخاب کنید.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // 2. IMPORT / RESTORE SECTION (Local Drive & Cloud Drive / Google Drive)
                    Text(
                        text = "۲. درون‌ریزی و بازیابی فایل پشتیبان (Import):",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "حالت بازیابی اطلاعات:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilterChip(
                                    selected = replaceExistingOnImport,
                                    onClick = { replaceExistingOnImport = true },
                                    label = {
                                        AutoResizedButtonText(
                                            text = "جایگزینی کامل با فایل پشتیبان",
                                            fontWeight = if (replaceExistingOnImport) FontWeight.Bold else FontWeight.Medium,
                                            maxFontSize = 11.5.sp,
                                            minFontSize = 8.sp,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = Color.White
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                                FilterChip(
                                    selected = !replaceExistingOnImport,
                                    onClick = { replaceExistingOnImport = false },
                                    label = {
                                        AutoResizedButtonText(
                                            text = "ادغام با اطلاعات فعلی",
                                            fontWeight = if (!replaceExistingOnImport) FontWeight.Bold else FontWeight.Medium,
                                            maxFontSize = 11.5.sp,
                                            minFontSize = 8.sp,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = Color.White
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Button(
                                onClick = {
                                    importSafLauncher.launch(
                                        arrayOf("application/json", "text/plain", "application/octet-stream", "*/*")
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary
                                ),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("import_backup_saf_button")
                            ) {
                                Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(17.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                AutoResizedButtonText(
                                    text = "انتخاب فایل پشتیبان از حافظه دستگاه یا گوگل درایو",
                                    maxFontSize = 12.5.sp,
                                    minFontSize = 8.5.sp,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                            }
                        }
                    }

                    // 3. LOCAL BACKUP SNAPSHOTS LIST
                    if (localBackups.isNotEmpty()) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        Text(
                            text = "۳. نسخه‌های پشتیبان ذخیره‌شده در حافظه لوکال برنامه (${JalaliCalendar.toPersianDigits(localBackups.size)}):",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            localBackups.take(6).forEach { backupInfo ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "${backupInfo.createdAtJalali} - ساعت ${backupInfo.createdAtTime}",
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "${JalaliCalendar.toPersianDigits(backupInfo.toolsCount)} وسیله • ${JalaliCalendar.toPersianDigits(backupInfo.schedulesCount)} برنامه • ${JalaliCalendar.toPersianDigits(backupInfo.logsCount)} سابقه (${JalaliCalendar.toPersianDigits(backupInfo.fileSizeKb)} KB)",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            IconButton(
                                                onClick = { filePendingRestoreConfirm = backupInfo.file },
                                                modifier = Modifier.size(36.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Restore,
                                                    contentDescription = "بازگردانی این نسخه",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                            IconButton(
                                                onClick = {
                                                    BackupRestoreManager.shareBackupFileToCloudOrDrive(
                                                        context = context,
                                                        file = backupInfo.file,
                                                        preferGoogleDrive = false
                                                    )
                                                },
                                                modifier = Modifier.size(36.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Share,
                                                    contentDescription = "ارسال به گوگل درایو",
                                                    tint = Color(0xFF059669),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                            IconButton(
                                                onClick = { onDeleteLocalFile(backupInfo.file) },
                                                modifier = Modifier.size(36.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "حذف نسخه پشتیبان",
                                                    tint = MaterialTheme.colorScheme.error,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = onDismissRequest,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AutoResizedButtonText(
                        text = "بستن پنجره پشتیبان‌گیری",
                        maxFontSize = 13.sp,
                        minFontSize = 9.sp
                    )
                }
            }
        )

        // Confirm local restore dialog
        filePendingRestoreConfirm?.let { targetFile ->
            AlertDialog(
                onDismissRequest = { filePendingRestoreConfirm = null },
                title = { Text("تأیید بازیابی نسخه پشتیبان") },
                text = {
                    Text(
                        if (replaceExistingOnImport) {
                            "آیا مایلید اطلاعات فعلی برنامه با نسخه پشتیبان انتخابی جایگزین شود؟"
                        } else {
                            "آیا مایلید اطلاعات این نسخه پشتیبان با اطلاعات فعلی ادغام شود؟"
                        }
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val file = targetFile
                            filePendingRestoreConfirm = null
                            onRestoreLocalFile(file, replaceExistingOnImport)
                        },
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        AutoResizedButtonText("بله، بازیابی شود", maxFontSize = 12.5.sp)
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { filePendingRestoreConfirm = null },
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        AutoResizedButtonText("انصراف", maxFontSize = 12.5.sp)
                    }
                }
            )
        }
    }
}

@Composable
private fun BackupStatPill(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

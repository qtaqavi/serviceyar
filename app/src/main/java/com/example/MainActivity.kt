package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.ServiceScheduleEntity
import com.example.data.model.ToolEntity
import com.example.notifications.ReminderNotificationHelper
import com.example.ui.screens.AddEditServiceDialog
import com.example.ui.screens.AddEditToolDialog
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.RecordServiceDoneDialog
import com.example.ui.screens.ToolDetailScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.ToolViewModel
import kotlinx.coroutines.launch

sealed class Screen {
    object Home : Screen()
    data class ToolDetail(val toolId: Long) : Screen()
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Create the notification channel right away
        ReminderNotificationHelper.createNotificationChannel(this)

        setContent {
            MyApplicationTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    ServiceYarApp()
                }
            }
        }
    }
}

@Composable
fun ServiceYarApp() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val viewModel: ToolViewModel = viewModel(
        factory = ToolViewModel.provideFactory(context.applicationContext as android.app.Application)
    )

    val toolsWithServices by viewModel.toolsWithServices.collectAsState()
    val allSchedules by viewModel.allSchedules.collectAsState()

    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }

    // Dialog States
    var showAddToolDialog by remember { mutableStateOf(false) }
    var toolToEdit by remember { mutableStateOf<ToolEntity?>(null) }

    var serviceDialogTool by remember { mutableStateOf<ToolEntity?>(null) }
    var scheduleToEdit by remember { mutableStateOf<ServiceScheduleEntity?>(null) }

    var scheduleToMarkDone by remember { mutableStateOf<ServiceScheduleEntity?>(null) }

    // Permission launcher for Android 13+ notifications
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            scope.launch {
                snackbarHostState.showSnackbar("مجوز ارسال یادآوری و اعلان فعال شد.")
            }
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (currentScreen is Screen.Home) {
                ExtendedFloatingActionButton(
                    onClick = {
                        toolToEdit = null
                        showAddToolDialog = true
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "افزودن وسیله",
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    text = {
                        Text(
                            text = "افزودن وسیله جدید",
                            fontWeight = FontWeight.Bold
                        )
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.testTag("add_tool_fab")
                )
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (val screen = currentScreen) {
                is Screen.Home -> {
                    HomeScreen(
                        toolsWithServices = toolsWithServices,
                        allSchedules = allSchedules,
                        onToolClick = { tool ->
                            currentScreen = Screen.ToolDetail(tool.id)
                        },
                        onAddNewTool = {
                            toolToEdit = null
                            showAddToolDialog = true
                        },
                        onAddNewService = { tool ->
                            serviceDialogTool = tool
                            scheduleToEdit = null
                        },
                        onMarkServiceDone = { schedule ->
                            scheduleToMarkDone = schedule
                        },
                        onEditService = { tool, schedule ->
                            serviceDialogTool = tool
                            scheduleToEdit = schedule
                        },
                        onTriggerTestNotification = {
                            viewModel.triggerTestNotification()
                            scope.launch {
                                snackbarHostState.showSnackbar("اعلان آزمایشی یادآوری سرویس ارسال شد.")
                            }
                        },
                        onLoadSampleData = {
                            viewModel.resetToSampleData()
                            scope.launch {
                                snackbarHostState.showSnackbar("داده‌های نمونه فارسی بارگذاری شد.")
                            }
                        }
                    )
                }

                is Screen.ToolDetail -> {
                    val toolWithServices = toolsWithServices.firstOrNull { it.tool.id == screen.toolId }
                    if (toolWithServices != null) {
                        ToolDetailScreen(
                            toolWithServices = toolWithServices,
                            onBackClick = { currentScreen = Screen.Home },
                            onEditTool = { tool ->
                                toolToEdit = tool
                                showAddToolDialog = true
                            },
                            onDeleteTool = { tool ->
                                viewModel.deleteTool(tool)
                                scope.launch {
                                    snackbarHostState.showSnackbar("وسیله با موفقیت حذف شد.")
                                }
                            },
                            onAddNewService = { tool ->
                                serviceDialogTool = tool
                                scheduleToEdit = null
                            },
                            onEditService = { schedule ->
                                serviceDialogTool = toolWithServices.tool
                                scheduleToEdit = schedule
                            },
                            onDeleteService = { schedule ->
                                viewModel.deleteSchedule(schedule)
                                scope.launch {
                                    snackbarHostState.showSnackbar("برنامه سرویس حذف شد.")
                                }
                            },
                            onMarkServiceDone = { schedule ->
                                scheduleToMarkDone = schedule
                            }
                        )
                    } else {
                        // Fallback if deleted
                        currentScreen = Screen.Home
                    }
                }
            }

            // Dialog: Add / Edit Tool
            if (showAddToolDialog) {
                AddEditToolDialog(
                    toolToEdit = toolToEdit,
                    onDismissRequest = {
                        showAddToolDialog = false
                        toolToEdit = null
                    },
                    onSaveTool = { tool ->
                        if (toolToEdit == null) {
                            viewModel.insertTool(tool) {
                                scope.launch {
                                    snackbarHostState.showSnackbar("وسیله «${tool.name}» با موفقیت اضافه شد.")
                                }
                            }
                        } else {
                            viewModel.updateTool(tool)
                            scope.launch {
                                snackbarHostState.showSnackbar("مشخصات «${tool.name}» به‌روزرسانی شد.")
                            }
                        }
                    }
                )
            }

            // Dialog: Add / Edit Service Schedule
            serviceDialogTool?.let { tool ->
                val allToolsList = toolsWithServices.map { it.tool }
                AddEditServiceDialog(
                    tool = tool,
                    scheduleToEdit = scheduleToEdit,
                    allTools = allToolsList,
                    onDismissRequest = {
                        serviceDialogTool = null
                        scheduleToEdit = null
                    },
                    onSaveSchedule = { schedule ->
                        if (scheduleToEdit == null) {
                            viewModel.insertSchedule(schedule) {
                                scope.launch {
                                    snackbarHostState.showSnackbar("برنامه «${schedule.title}» به تقویم سرویس افزوده شد.")
                                }
                            }
                        } else {
                            viewModel.updateSchedule(schedule)
                            scope.launch {
                                snackbarHostState.showSnackbar("برنامه سرویس «${schedule.title}» ویرایش شد.")
                            }
                        }
                    }
                )
            }

            // Dialog: Mark Service As Done
            scheduleToMarkDone?.let { schedule ->
                RecordServiceDoneDialog(
                    schedule = schedule,
                    onDismissRequest = { scheduleToMarkDone = null },
                    onConfirmDone = { performedDateJalali, actualCost, technician, invoiceNo, partsReplaced, notes ->
                        viewModel.markServiceDone(
                            scheduleId = schedule.id,
                            performedDateJalali = performedDateJalali,
                            actualCost = actualCost,
                            technicianOrShop = technician,
                            invoiceNumber = invoiceNo,
                            partsReplaced = partsReplaced,
                            notes = notes
                        )
                        scope.launch {
                            snackbarHostState.showSnackbar("سرویس «${schedule.title}» با موفقیت در سوابق ثبت و موعد بعدی تمدید شد.")
                        }
                    }
                )
            }
        }
    }
}

package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CategoryUiModel
import com.example.data.model.ServicePriority
import com.example.data.model.ServiceStatus
import com.example.data.model.ToolCategory
import com.example.ui.theme.LocalAppFontFamily
import com.example.ui.theme.LocalAppFontScale
import com.example.ui.theme.StatusDueSoonAmber
import com.example.ui.theme.StatusDueSoonAmberContainer
import com.example.ui.theme.StatusDueSoonAmberText
import com.example.ui.theme.StatusOverdueRed
import com.example.ui.theme.StatusOverdueRedContainer
import com.example.ui.theme.StatusOverdueRedText
import com.example.ui.theme.StatusUpToDateGreen
import com.example.ui.theme.StatusUpToDateGreenContainer
import com.example.ui.theme.StatusUpToDateGreenText
import com.example.util.JalaliCalendar

/**
 * Automatically adjusts its font size down from [maxFontSize] to [minFontSize]
 * so that button labels always fit their button container completely and legibly on a single line.
 */
@Composable
fun AutoResizedButtonText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontWeight: FontWeight = FontWeight.Bold,
    maxFontSize: TextUnit = 12.5.sp,
    minFontSize: TextUnit = 8.sp,
    textAlign: TextAlign = TextAlign.Center
) {
    val activeFontFamily = LocalAppFontFamily.current
    val appFontScale = LocalAppFontScale.current
    val initialSize = remember(maxFontSize, appFontScale) {
        val factor = (1f + (appFontScale - 1f) * 0.25f).coerceIn(0.90f, 1.05f)
        (maxFontSize.value * factor).coerceIn(minFontSize.value, 13f).sp
    }
    var scaledFontSize by remember(text, initialSize, minFontSize, activeFontFamily) { mutableStateOf(initialSize) }
    var readyToDraw by remember(text, initialSize, minFontSize, activeFontFamily) { mutableStateOf(false) }

    Text(
        text = text,
        color = color,
        fontFamily = activeFontFamily,
        fontSize = scaledFontSize,
        fontWeight = fontWeight,
        textAlign = textAlign,
        maxLines = 1,
        softWrap = false,
        overflow = TextOverflow.Ellipsis,
        lineHeight = (scaledFontSize.value * 1.2f).sp,
        onTextLayout = { result ->
            if ((result.didOverflowWidth || result.hasVisualOverflow) && scaledFontSize > minFontSize) {
                val nextSize = (scaledFontSize.value - 0.5f).sp
                if (nextSize >= minFontSize) {
                    scaledFontSize = nextSize
                } else {
                    scaledFontSize = minFontSize
                    readyToDraw = true
                }
            } else {
                readyToDraw = true
            }
        },
        modifier = modifier.drawWithContent {
            if (readyToDraw) {
                drawContent()
            }
        }
    )
}

@Composable
fun StatusBadge(
    status: ServiceStatus,
    daysDiff: Int? = null,
    modifier: Modifier = Modifier
) {
    val activeFontFamily = LocalAppFontFamily.current
    val (backgroundColor, textColor, icon, label) = when (status) {
        ServiceStatus.OVERDUE -> {
            val countText = if (daysDiff != null && daysDiff < 0) {
                " (${JalaliCalendar.toPersianDigits(-daysDiff)} روز)"
            } else ""
            Tuple4(
                StatusOverdueRedContainer,
                StatusOverdueRed,
                Icons.Default.Error,
                "منقضی$countText"
            )
        }
        ServiceStatus.DUE_SOON -> {
            val countText = if (daysDiff != null && daysDiff >= 0) {
                " (${JalaliCalendar.toPersianDigits(daysDiff)} روز)"
            } else ""
            Tuple4(
                Color(0xFFCCE8E8),
                Color(0xFF006A6A),
                Icons.Default.Warning,
                "نزدیک موعد$countText"
            )
        }
        ServiceStatus.UP_TO_DATE -> {
            Tuple4(
                StatusUpToDateGreenContainer,
                StatusUpToDateGreen,
                Icons.Default.CheckCircle,
                "سرویس‌شده"
            )
        }
        ServiceStatus.EXPIRED_WARRANTY -> {
            Tuple4(
                Color(0xFFE8DDFF),
                Color(0xFF6750A4),
                Icons.Default.Info,
                "اتمام گارانتی"
            )
        }
        ServiceStatus.NO_SCHEDULE -> {
            Tuple4(
                Color(0xFFE2E2EC),
                Color(0xFF44474E),
                Icons.Default.Schedule,
                "بدون سرویس"
            )
        }
    }

    Surface(
        shape = CircleShape,
        color = backgroundColor,
        border = BorderStroke(0.5.dp, textColor.copy(alpha = 0.25f)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = label,
                fontFamily = activeFontFamily,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun CategoryChip(
    category: ToolCategory,
    modifier: Modifier = Modifier
) {
    CategoryChip(categoryUi = category.toUiModel(), modifier = modifier)
}

@Composable
fun CategoryChip(
    categoryUi: CategoryUiModel,
    modifier: Modifier = Modifier
) {
    val activeFontFamily = LocalAppFontFamily.current
    val categoryColor = Color(categoryUi.colorHex)
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = categoryColor.copy(alpha = 0.12f),
        border = BorderStroke(0.5.dp, categoryColor.copy(alpha = 0.25f)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = categoryUi.icon,
                contentDescription = null,
                tint = categoryColor,
                modifier = Modifier.size(13.dp)
            )
            Text(
                text = categoryUi.titlePersian,
                fontFamily = activeFontFamily,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold,
                color = categoryColor,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun PriorityBadge(
    priority: ServicePriority,
    modifier: Modifier = Modifier
) {
    val activeFontFamily = LocalAppFontFamily.current
    val color = Color(priority.colorHex)
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.14f),
        border = BorderStroke(0.5.dp, color.copy(alpha = 0.25f)),
        modifier = modifier
    ) {
        Text(
            text = priority.titlePersian,
            fontFamily = activeFontFamily,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = color,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(iconColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                letterSpacing = (-0.5).sp
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Normal
            )
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    count: Int? = null,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )
            if (count != null) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = JalaliCalendar.toPersianDigits(count),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
        }

        if (actionText != null && onActionClick != null) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
                onClick = onActionClick
            ) {
                AutoResizedButtonText(
                    text = actionText,
                    color = MaterialTheme.colorScheme.primary,
                    maxFontSize = 11.5.sp,
                    minFontSize = 8.5.sp,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
fun EmptyStateView(
    title: String,
    description: String,
    icon: ImageVector,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(38.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

private data class Tuple4<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)


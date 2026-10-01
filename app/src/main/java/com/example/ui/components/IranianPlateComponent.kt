package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.util.JalaliCalendar

val PERSIAN_PLATE_LETTERS = listOf(
    "ب", "ج", "د", "س", "ص", "ط", "ق", "ل", "م", "ن", "و", "ه", "ی", "الف", "ت", "ژ", "ع"
)

data class IranianPlate(
    val part1: String = "12",       // دو رقم اول (مثلاً ۱۲)
    val letter: String = "ب",       // حرف فارسی (مثلاً ب)
    val part2: String = "345",      // سه رقم وسط (مثلاً ۳۴۵)
    val iranCode: String = "68"     // کد دو رقمی ایران (مثلاً ۶۸)
) {
    fun toFormattedString(): String {
        val p1 = JalaliCalendar.toPersianDigits(part1.ifBlank { "12" }.take(2).padStart(2, '0'))
        val p2 = JalaliCalendar.toPersianDigits(part2.ifBlank { "345" }.take(3).padStart(3, '0'))
        val code = JalaliCalendar.toPersianDigits(iranCode.ifBlank { "68" }.take(2).padStart(2, '0'))
        val let = letter.ifBlank { "ب" }
        return "$p1 $let $p2 ایران $code"
    }

    fun toRawString(): String {
        val p1 = part1.filter { it.isDigit() }.ifBlank { "12" }
        val let = letter.trim().ifBlank { "ب" }
        val p2 = part2.filter { it.isDigit() }.ifBlank { "345" }
        val code = iranCode.filter { it.isDigit() }.ifBlank { "68" }
        return "$p1-$let-$p2-$code"
    }

    companion object {
        fun parse(input: String?): IranianPlate {
            if (input.isNullOrBlank()) return IranianPlate()

            val cleaned = JalaliCalendar.toEnglishDigits(input)
                .replace("ایران", " ")
                .trim()

            // First try regex extraction for: [2 digits] [letter] [3 digits] [2 digits]
            val regex = Regex("""(\d{1,2})\s*[-/\s]*\s*([^\d\s-]+)\s*[-/\s]*\s*(\d{1,3})(?:\s*[-/\s]*\s*(\d{1,2}))?""")
            val match = regex.find(cleaned)
            if (match != null) {
                val p1 = match.groupValues[1].ifBlank { "12" }
                val let = match.groupValues[2].trim().ifBlank { "ب" }
                val p2 = match.groupValues[3].ifBlank { "345" }
                val code = match.groupValues.getOrNull(4)?.ifBlank { "68" } ?: "68"
                return IranianPlate(
                    part1 = p1,
                    letter = let,
                    part2 = p2,
                    iranCode = code
                )
            }

            // Fallback token splitting
            val tokens = cleaned.split("-", " ", "/").map { it.trim() }.filter { it.isNotBlank() }
            if (tokens.size >= 3) {
                val p1 = tokens[0].filter { it.isDigit() }.ifBlank { "12" }
                val let = tokens[1].ifBlank { "ب" }
                val p2 = tokens[2].filter { it.isDigit() }.ifBlank { "345" }
                val code = tokens.getOrNull(3)?.filter { it.isDigit() }?.ifBlank { "68" } ?: "68"
                return IranianPlate(
                    part1 = p1,
                    letter = let,
                    part2 = p2,
                    iranCode = code
                )
            }

            return IranianPlate()
        }
    }
}

/**
 * Authentic visual Iranian Vehicle License Plate (پلاک ملی خودرو)
 * Fixed layout so "ایران" is never broken/wrapped and the 2-digit code is always clearly shown below it.
 */
@Composable
fun IranianPlateBadge(
    plate: IranianPlate,
    modifier: Modifier = Modifier,
    height: Int = 44
) {
    val baseDensity = LocalDensity.current
    // Keep plate font scale consistent so plate graphic never breaks at extreme font scales
    val plateDensity = remember(baseDensity.density) {
        Density(density = baseDensity.density, fontScale = 1.0f)
    }

    val displayPart1 = JalaliCalendar.toPersianDigits(plate.part1.ifBlank { "12" }.take(2))
    val displayLetter = plate.letter.ifBlank { "ب" }
    val displayPart2 = JalaliCalendar.toPersianDigits(plate.part2.ifBlank { "345" }.take(3))
    val displayIranCode = JalaliCalendar.toPersianDigits(plate.iranCode.ifBlank { "68" }.take(2))

    val noPadStyle = TextStyle(
        platformStyle = PlatformTextStyle(includeFontPadding = false)
    )

    CompositionLocalProvider(
        LocalLayoutDirection provides LayoutDirection.Ltr,
        LocalDensity provides plateDensity
    ) {
        Surface(
            shape = RoundedCornerShape(7.dp),
            color = Color(0xFFFAFBFD),
            border = BorderStroke(1.5.dp, Color(0xFF1E293B)),
            shadowElevation = 2.dp,
            modifier = modifier
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .height(IntrinsicSize.Min)
                    .heightIn(min = height.dp)
            ) {
                // 1. Blue Strip on the left with Iran Flag & I.R. IRAN
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(26.dp)
                        .background(Color(0xFF003399))
                        .padding(horizontal = 2.dp, vertical = 4.dp)
                ) {
                    // Iran Flag (Green, White, Red stripes)
                    Column(
                        modifier = Modifier
                            .width(18.dp)
                            .height(10.dp)
                            .clip(RoundedCornerShape(1.5.dp))
                    ) {
                        Box(modifier = Modifier.fillMaxWidth().weight(1f).background(Color(0xFF239F40)))
                        Box(modifier = Modifier.fillMaxWidth().weight(1f).background(Color.White))
                        Box(modifier = Modifier.fillMaxWidth().weight(1f).background(Color(0xFFDA0000)))
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "I.R.",
                            style = noPadStyle.copy(
                                fontSize = 6.sp,
                                lineHeight = 6.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            ),
                            maxLines = 1,
                            softWrap = false,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "IRAN",
                            style = noPadStyle.copy(
                                fontSize = 6.sp,
                                lineHeight = 6.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            ),
                            maxLines = 1,
                            softWrap = false,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // 2. Middle Main Plate: [2 digits] [Persian letter] [3 digits]
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                        .fillMaxHeight()
                ) {
                    Text(
                        text = displayPart1,
                        style = noPadStyle.copy(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF0F172A),
                            letterSpacing = 1.sp
                        ),
                        maxLines = 1,
                        softWrap = false
                    )

                    Text(
                        text = displayLetter,
                        style = noPadStyle.copy(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF0F172A)
                        ),
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.padding(horizontal = 2.dp)
                    )

                    Text(
                        text = displayPart2,
                        style = noPadStyle.copy(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF0F172A),
                            letterSpacing = 1.sp
                        ),
                        maxLines = 1,
                        softWrap = false
                    )
                }

                // 3. Vertical Divider Line
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(1.5.dp)
                        .background(Color(0xFF1E293B))
                )

                // 4. Right Compartment: Unbroken "ایران" on top + 2-digit Iran Code clearly below it
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxHeight()
                        .widthIn(min = 48.dp)
                        .background(Color(0xFFF1F5F9))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "ایران",
                        style = noPadStyle.copy(
                            fontSize = 10.sp,
                            lineHeight = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1E293B)
                        ),
                        maxLines = 1,
                        softWrap = false,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = displayIranCode,
                        style = noPadStyle.copy(
                            fontSize = 15.sp,
                            lineHeight = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF0F172A),
                            letterSpacing = 0.5.sp
                        ),
                        maxLines = 1,
                        softWrap = false,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun IranianPlateBadge(
    plateString: String,
    modifier: Modifier = Modifier,
    height: Int = 44
) {
    val parsedPlate = remember(plateString) { IranianPlate.parse(plateString) }
    IranianPlateBadge(plate = parsedPlate, modifier = modifier, height = height)
}

/**
 * Interactive Iranian License Plate Editor for Add/Edit Tool dialog
 */
@Composable
fun IranianPlateEditor(
    plate: IranianPlate,
    onPlateChange: (IranianPlate) -> Unit,
    modifier: Modifier = Modifier
) {
    var dropdownExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "شماره پلاک ملی خودرو:",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "پلاک استاندارد ایران",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Live preview of the complete plate
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            IranianPlateBadge(plate = plate, height = 48)
        }

        // Segmented inputs in visual LTR plate order: [2 digits] [Letter] [3 digits] | [Iran 2 digits]
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Two digits (left)
                OutlinedTextField(
                    value = JalaliCalendar.toPersianDigits(plate.part1),
                    onValueChange = { input ->
                        val clean = JalaliCalendar.toEnglishDigits(input).filter { it.isDigit() }.take(2)
                        onPlateChange(plate.copy(part1 = clean))
                    },
                    label = { Text("۲ رقم", fontSize = 10.sp, maxLines = 1, softWrap = false) },
                    singleLine = true,
                    textStyle = TextStyle(
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )

                // 2. Letter selector
                Box(modifier = Modifier.weight(1.15f)) {
                    OutlinedTextField(
                        value = plate.letter,
                        onValueChange = {},
                        readOnly = true,
                        singleLine = true,
                        label = { Text("حرف", fontSize = 10.sp, maxLines = 1, softWrap = false) },
                        textStyle = TextStyle(
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        ),
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "انتخاب حرف",
                                modifier = Modifier.clickable { dropdownExpanded = true }
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { dropdownExpanded = true }
                    )

                    DropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false }
                    ) {
                        PERSIAN_PLATE_LETTERS.forEach { letter ->
                            DropdownMenuItem(
                                text = { Text(letter, fontWeight = FontWeight.Bold) },
                                onClick = {
                                    onPlateChange(plate.copy(letter = letter))
                                    dropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // 3. Three digits (middle)
                OutlinedTextField(
                    value = JalaliCalendar.toPersianDigits(plate.part2),
                    onValueChange = { input ->
                        val clean = JalaliCalendar.toEnglishDigits(input).filter { it.isDigit() }.take(3)
                        onPlateChange(plate.copy(part2 = clean))
                    },
                    label = { Text("۳ رقم", fontSize = 10.sp, maxLines = 1, softWrap = false) },
                    singleLine = true,
                    textStyle = TextStyle(
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1.25f)
                )

                // 4. Iran code (two digits on right)
                OutlinedTextField(
                    value = JalaliCalendar.toPersianDigits(plate.iranCode),
                    onValueChange = { input ->
                        val clean = JalaliCalendar.toEnglishDigits(input).filter { it.isDigit() }.take(2)
                        onPlateChange(plate.copy(iranCode = clean))
                    },
                    label = { Text("کد ایران", fontSize = 10.sp, maxLines = 1, softWrap = false) },
                    singleLine = true,
                    textStyle = TextStyle(
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1.1f)
                )
            }
        }

        // Quick letters chip row for 1-tap letter switching
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(PERSIAN_PLATE_LETTERS) { let ->
                FilterChip(
                    selected = plate.letter == let,
                    onClick = { onPlateChange(plate.copy(letter = let)) },
                    label = { Text(let, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }
    }
}

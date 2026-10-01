package com.example

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.text.font.FontWeight
import com.example.ui.components.AutoResizedButtonText
import com.example.ui.theme.AppFont
import com.example.ui.theme.MyApplicationTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun `read string from context`() {
    var currentFont by mutableStateOf(AppFont.VAZIR)
    var headlineMetrics = ""
    var bodyMetrics = ""

    composeTestRule.setContent {
      MyApplicationTheme(appFont = currentFont) {
        Column {
          Text(
            text = "مدیریت سرویس خودرو و تجهیزات",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            onTextLayout = { res ->
              headlineMetrics = "size=${res.size}, baseline=${res.firstBaseline}, family=${res.layoutInput.style.fontFamily}"
            }
          )
          Text(
            text = "پکیج دیواری ایران رادیاتور و کولر گازی",
            style = MaterialTheme.typography.bodyMedium,
            onTextLayout = { res ->
              bodyMetrics = "size=${res.size}, baseline=${res.firstBaseline}, family=${res.layoutInput.style.fontFamily}"
            }
          )
          AutoResizedButtonText(text = "ذخیره برنامه سرویس")
        }
      }
    }

    for (font in AppFont.entries) {
      currentFont = font
      composeTestRule.waitForIdle()
      println("FONT_METRICS ${font.name}: headline=[$headlineMetrics] | body=[$bodyMetrics]")
    }
  }
}





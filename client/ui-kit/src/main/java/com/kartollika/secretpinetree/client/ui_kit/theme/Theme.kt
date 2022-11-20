package com.kartollika.secretpinetree.client.ui_kit.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable

private val DarkColorPalette = darkColors(
  primary = Blue80,
  primaryVariant = Blue90,
  secondary = DarkBlue80,
  surface = Grey10,
)

private val LightColorPalette = lightColors(
  primary = Blue40,
  primaryVariant = Blue30,
  secondary = DarkBlue40,
  surface = Grey99,
)

@Composable
fun SecretPineTreeClientTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit
) {
  val colors = if (darkTheme) {
    DarkColorPalette
  } else {
    LightColorPalette
  }

  MaterialTheme(
    colors = colors,
    typography = Typography,
    shapes = Shapes,
    content = content
  )
}
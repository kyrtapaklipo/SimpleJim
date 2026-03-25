package com.simplejim.tracker.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val LightColors = lightColorScheme(
    primary = Rust,
    onPrimary = Chalk,
    secondary = Moss,
    onSecondary = Chalk,
    tertiary = Ember,
    background = Chalk,
    onBackground = Iron,
    surface = Chalk,
    onSurface = Iron,
    surfaceVariant = Bone,
    onSurfaceVariant = Plate,
    primaryContainer = Bone,
    onPrimaryContainer = Iron,
    secondaryContainer = MintSoft,
    onSecondaryContainer = Iron,
    errorContainer = RoseSoft,
    onErrorContainer = Iron,
)

private val DarkColors = darkColorScheme(
    primary = Rust,
    onPrimary = Chalk,
    secondary = Moss,
    onSecondary = Chalk,
    tertiary = Ember,
    background = Night,
    onBackground = Chalk,
    surface = NightSurface,
    onSurface = Chalk,
    surfaceVariant = Plate,
    onSurfaceVariant = Bone,
    primaryContainer = Clay,
    onPrimaryContainer = Chalk,
    secondaryContainer = MintDark,
    onSecondaryContainer = Chalk,
    errorContainer = RoseDark,
    onErrorContainer = Chalk,
)

private val SimpleJimTypography = Typography(
    headlineMedium = TextStyle(
        fontWeight = FontWeight.Black,
        fontSize = 30.sp,
        lineHeight = 34.sp,
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 26.sp,
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 22.sp,
    ),
)

@Composable
fun SimpleJimTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = SimpleJimTypography,
        content = content,
    )
}

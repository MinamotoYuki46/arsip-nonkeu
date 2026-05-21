package com.bpkpad.arsipnonkeu.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color


val PrimaryGreen = Color(0xFF2E7D32)
val DarkGreen = Color(0xFF1B5E20)
val LightGreen = Color(0xFFCBFFC2)
val AccentGreen = Color(0xFF388E3C)
val BackgroundGray = Color(0xFFF3F8FC)
val CardWhite = Color(0xFFFFFFFF)
val StagingCard = Color(0xFFD3E4F3)
val TextPrimary = Color(0xFF0D2535)
val TextSecondary = Color(0xFF5A6A75)
val BadgeGreen = Color(0xFFCBFFC2)
val BadgeGreenText = Color(0xFF2E7D32)
val FinalizedDot = Color(0xFF2E7D32)
val RestrictedDot = Color(0xFFC62828)
val BottomBarBg = Color(0xFFFFFFFF)
val BottomBarSelected = Color(0xFF2E7D32)
val BottomBarUnselected = Color(0xFF9CA3AF)
val StagingBg = Color(0xFFF0F7FF)
val FileIconBg = Color(0xFFCBFFC2)
val FileIconColor = Color(0xFF2E7D32)
val PushButtonBg = Color(0xFF2E7D32)
val ProgressBarBg = Color(0xFFDEEAF5)
val ProgressBarFill = Color(0xFF2E7D32)

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun ArsipBPKADTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
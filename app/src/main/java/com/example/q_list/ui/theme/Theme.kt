package com.example.q_list.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val QListDarkColorScheme = darkColorScheme(

    primary = PrimaryBlue,
    onPrimary = TextWhite,

    secondary = SecondaryBlue,
    onSecondary = BackgroundDark,

    tertiary = TertiaryBlue,
    onTertiary = BackgroundDark,

    background = BackgroundDark,
    onBackground = TextWhite,

    surface = SurfaceDark,
    onSurface = TextWhite,

    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = TextGray,

    error = ErrorRed,
    onError = TextWhite,

    outline = BorderColor
)

private val QListLightColorScheme = lightColorScheme(

    primary = PrimaryBlue,
    onPrimary = TextWhite,

    secondary = SecondaryBlue,
    onSecondary = BackgroundDark,

    tertiary = TertiaryBlue,
    onTertiary = BackgroundDark,

    background = LightBackground,
    onBackground = LightText,

    surface = LightSurface,
    onSurface = LightText,

    error = ErrorRed,
    onError = TextWhite,

    outline = BorderColor
)

@Composable
fun QListTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {

    val colorScheme = if (darkTheme) {
        QListDarkColorScheme
    } else {
        QListLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
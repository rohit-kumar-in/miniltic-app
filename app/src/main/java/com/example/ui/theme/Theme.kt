package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val CharcoalDark = darkColorScheme(
    primary = PureWhite,
    onPrimary = PureBlack,
    secondary = TextSecondaryDark,
    onSecondary = PureWhite,
    tertiary = TextSecondaryDark,
    background = PureBlack,
    onBackground = PureWhite,
    surface = SlateDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = BorderDark,
    onSurfaceVariant = TextSecondaryDark,
    outline = BorderDark
)

private val CharcoalLight = lightColorScheme(
    primary = PureBlack,
    onPrimary = PureWhite,
    secondary = TextSecondaryLight,
    onSecondary = PureBlack,
    tertiary = TextSecondaryLight,
    background = PureWhite,
    onBackground = PureBlack,
    surface = SlateLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = BorderLight,
    onSurfaceVariant = TextSecondaryLight,
    outline = BorderLight
)

// Forest Green Theme
private val ForestDark = darkColorScheme(
    primary = Color(0xFF81C784),
    onPrimary = PureBlack,
    secondary = Color(0xFFA5D6A7),
    onSecondary = PureWhite,
    tertiary = Color(0xFFA5D6A7),
    background = Color(0xFF121B13),
    onBackground = Color(0xFFE8F5E9),
    surface = Color(0xFF1B2E1E),
    onSurface = Color(0xFFE8F5E9),
    surfaceVariant = Color(0xFF2E3D30),
    onSurfaceVariant = Color(0xFFA5D6A7),
    outline = Color(0xFF3E4E40)
)

private val ForestLight = lightColorScheme(
    primary = Color(0xFF2E7D32),
    onPrimary = PureWhite,
    secondary = Color(0xFF4CAF50),
    onSecondary = PureBlack,
    tertiary = Color(0xFF4CAF50),
    background = Color(0xFFF1F8E9),
    onBackground = Color(0xFF1B5E20),
    surface = Color(0xFFDCEDC8),
    onSurface = Color(0xFF1B5E20),
    surfaceVariant = Color(0xFFC5E1A5),
    onSurfaceVariant = Color(0xFF2E7D32),
    outline = Color(0xFFAED581)
)

// Sunset Orange Theme
private val SunsetDark = darkColorScheme(
    primary = Color(0xFFFF8A65),
    onPrimary = PureBlack,
    secondary = Color(0xFFFFAB91),
    onSecondary = PureWhite,
    tertiary = Color(0xFFFFAB91),
    background = Color(0xFF1E1412),
    onBackground = Color(0xFFFBE9E7),
    surface = Color(0xFF2D1E1B),
    onSurface = Color(0xFFFBE9E7),
    surfaceVariant = Color(0xFF3D2E2B),
    onSurfaceVariant = Color(0xFFFFAB91),
    outline = Color(0xFF4D3E3B)
)

private val SunsetLight = lightColorScheme(
    primary = Color(0xFFD84315),
    onPrimary = PureWhite,
    secondary = Color(0xFFFF5722),
    onSecondary = PureBlack,
    tertiary = Color(0xFFFF5722),
    background = Color(0xFFFBE9E7),
    onBackground = Color(0xFF5D4037),
    surface = Color(0xFFFFCCBC),
    onSurface = Color(0xFF5D4037),
    surfaceVariant = Color(0xFFFFAB91),
    onSurfaceVariant = Color(0xFFD84315),
    outline = Color(0xFFFF8A65)
)

// Indigo Blue Theme
private val IndigoDark = darkColorScheme(
    primary = Color(0xFF7986CB),
    onPrimary = PureBlack,
    secondary = Color(0xFF9FA8DA),
    onSecondary = PureWhite,
    tertiary = Color(0xFF9FA8DA),
    background = Color(0xFF0F111E),
    onBackground = Color(0xFFE8EAF6),
    surface = Color(0xFF1A1C30),
    onSurface = Color(0xFFE8EAF6),
    surfaceVariant = Color(0xFF26294A),
    onSurfaceVariant = Color(0xFF9FA8DA),
    outline = Color(0xFF343864)
)

private val IndigoLight = lightColorScheme(
    primary = Color(0xFF283593),
    onPrimary = PureWhite,
    secondary = Color(0xFF3F51B5),
    onSecondary = PureBlack,
    tertiary = Color(0xFF3F51B5),
    background = Color(0xFFE8EAF6),
    onBackground = Color(0xFF1A237E),
    surface = Color(0xFFC5CAE9),
    onSurface = Color(0xFF1A237E),
    surfaceVariant = Color(0xFF9FA8DA),
    onSurfaceVariant = Color(0xFF283593),
    outline = Color(0xFF7986CB)
)

// Grayscale Color Palette
private val GrayscaleDark = darkColorScheme(
    primary = PureWhite,
    onPrimary = PureBlack,
    secondary = Color(0xFF888888),
    onSecondary = PureWhite,
    tertiary = Color(0xFF888888),
    background = PureBlack,
    onBackground = PureWhite,
    surface = Color(0xFF222222),
    onSurface = PureWhite,
    surfaceVariant = Color(0xFF444444),
    onSurfaceVariant = Color(0xFF888888),
    outline = Color(0xFF444444)
)

private val GrayscaleLight = lightColorScheme(
    primary = PureBlack,
    onPrimary = PureWhite,
    secondary = Color(0xFF777777),
    onSecondary = PureBlack,
    tertiary = Color(0xFF777777),
    background = PureWhite,
    onBackground = PureBlack,
    surface = Color(0xFFEEEEEE),
    onSurface = PureBlack,
    surfaceVariant = Color(0xFFCCCCCC),
    onSurfaceVariant = Color(0xFF777777),
    outline = Color(0xFFCCCCCC)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    themeName: String = "Charcoal",
    fontName: String = "SansSerif",
    isGrayscale: Boolean = false,
    content: @Composable () -> Unit
) {
    val selectedColorScheme = if (isGrayscale) {
        if (darkTheme) GrayscaleDark else GrayscaleLight
    } else {
        when (themeName.lowercase()) {
            "forest" -> if (darkTheme) ForestDark else ForestLight
            "sunset" -> if (darkTheme) SunsetDark else SunsetLight
            "indigo" -> if (darkTheme) IndigoDark else IndigoLight
            else -> if (darkTheme) CharcoalDark else CharcoalLight
        }
    }

    val selectedFont = when (fontName.lowercase()) {
        "monospace" -> FontFamily.Monospace
        "serif" -> FontFamily.Serif
        "cursive" -> FontFamily.Cursive
        "sans-serif-condensed" -> FontFamily.SansSerif
        "sans-serif-black" -> FontFamily.SansSerif
        else -> FontFamily.Default
    }

    val customTypography = Typography(
        bodyLarge = TextStyle(
            fontFamily = selectedFont,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.5.sp
        ),
        titleLarge = TextStyle(
            fontFamily = selectedFont,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            lineHeight = 28.sp,
            letterSpacing = 0.sp
        ),
        labelSmall = TextStyle(
            fontFamily = selectedFont,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.5.sp
        )
    )

    MaterialTheme(
        colorScheme = selectedColorScheme,
        typography = customTypography,
        content = content
    )
}

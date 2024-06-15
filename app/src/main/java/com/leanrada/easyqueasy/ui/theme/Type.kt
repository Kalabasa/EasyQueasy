package com.leanrada.easyqueasy.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight

import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.text.googlefonts.Font
import com.leanrada.easyqueasy.R

val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

val bodyFontFamily = FontFamily(
    Font(
        googleFont = GoogleFont("Inter"),
        fontProvider = provider,
    )
)

val titleFontFamily = FontFamily(
    Font(
        googleFont = GoogleFont("Inter Tight"),
        fontProvider = provider,
    )
)

val displayFontFamily = FontFamily(
    Font(
        googleFont = GoogleFont("Sarabun"),
        style = FontStyle.Italic,
        fontProvider = provider,
    )
)

// Default Material 3 typography values
val baseline = Typography()

val AppTypography = Typography(
    displayLarge = baseline.displayLarge.copy(fontFamily = displayFontFamily),
    displayMedium = baseline.displayMedium.copy(fontFamily = displayFontFamily),
    displaySmall = baseline.displaySmall.copy(fontFamily = displayFontFamily),
    headlineLarge = baseline.headlineLarge.copy(
        fontFamily = displayFontFamily,
        fontWeight = FontWeight.Bold,
    ),
    headlineMedium = baseline.headlineMedium.copy(
        fontFamily = displayFontFamily,
        fontWeight = FontWeight.Bold,
    ),
    headlineSmall = baseline.headlineSmall.copy(
        fontFamily = displayFontFamily,
        fontWeight = FontWeight.Bold,
    ),
    titleLarge = baseline.titleLarge.copy(
        fontFamily = titleFontFamily,
        fontWeight = FontWeight.Bold,
    ),
    titleMedium = baseline.titleMedium.copy(
        fontFamily = titleFontFamily,
        fontWeight = FontWeight.Bold,
    ),
    titleSmall = baseline.titleSmall.copy(
        fontFamily = titleFontFamily,
        fontWeight = FontWeight.Bold,
    ),
    bodyLarge = baseline.bodyLarge.copy(fontFamily = bodyFontFamily),
    bodyMedium = baseline.bodyMedium.copy(fontFamily = bodyFontFamily),
    bodySmall = baseline.bodySmall.copy(fontFamily = bodyFontFamily),
    labelLarge = baseline.labelLarge.copy(
        fontFamily = bodyFontFamily,
        fontWeight = FontWeight.Bold,
    ),
    labelMedium = baseline.labelMedium.copy(
        fontFamily = bodyFontFamily,
        fontWeight = FontWeight.Bold,
    ),
    labelSmall = baseline.labelSmall.copy(
        fontFamily = bodyFontFamily,
        fontWeight = FontWeight.Bold,
    ),
)


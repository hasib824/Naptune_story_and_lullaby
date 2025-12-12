package com.naptune.lullabyandstory.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.naptune.lullabyandstory.R

// Winky Sans Font Family
val WinkySansFamily = FontFamily(
    Font(R.font.winky_sans_bold, FontWeight.Bold)
)

// Nunito Font Family - All variants
val NunitoFamily = FontFamily(
    Font(R.font.nunito_light, FontWeight.Light),
    Font(R.font.nunito_regular, FontWeight.Normal),
    Font(R.font.nunito_semibold, FontWeight.SemiBold),
    Font(R.font.nunito_bold, FontWeight.Bold)
)

// Set of Material typography styles with custom fonts
val Typography = Typography(
    // Display styles - Large headings with Winky Sans
    displayLarge = TextStyle(
        fontFamily = WinkySansFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    ),
    displayMedium = TextStyle(
        fontFamily = WinkySansFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp
    ),
    displaySmall = TextStyle(
        fontFamily = WinkySansFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp
    ),
    
    // Headline styles - For major sections with Winky Sans
    headlineLarge = TextStyle(
        fontFamily = WinkySansFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = NunitoFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        color = Color.White
    ),
    headlineSmall = TextStyle(
        fontFamily = WinkySansFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),
    
    // Title styles - App bar and section titles with Winky Sans
    titleLarge = TextStyle(
        fontFamily = WinkySansFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        shadow = Shadow(
            color = Color.Black.copy(alpha = 0.6f),
            offset = Offset(x = 0f, y = 6f),
            blurRadius = 8f
        )
    ),
    titleMedium = TextStyle(
        fontFamily = NunitoFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        color = Color.White
    ),
    titleSmall = TextStyle(
        fontFamily = NunitoFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        color = AccentColorSecondary
    ),
    
    // Body styles - Content text with Nunito
    bodyLarge = TextStyle(
        fontFamily = NunitoFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        color = AccentColorSecondary,

    ),
    bodyMedium = TextStyle(
        fontFamily = NunitoFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        color = AccentColorSecondary,
    ),
    bodySmall = TextStyle(
        fontFamily = NunitoFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),
    
    // Label styles - Buttons and UI elements with Nunito
    labelLarge = TextStyle(
        fontFamily = NunitoFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = NunitoFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = NunitoFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)

// Custom text styles for specific use cases
val PremiumButtonTextStyle = TextStyle(
    fontFamily = NunitoFamily,
    fontWeight = FontWeight.Normal, // Regular weight for premium button
    fontSize = 16.sp,
)

val NunitoLightStyle = TextStyle(
    fontFamily = NunitoFamily,
    fontWeight = FontWeight.Light,
    fontSize = 14.sp,

)



val PremmiumHeadlineStyle = TextStyle(
    fontFamily = NunitoFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 24.sp,
    color = Color.White
)



val NunitoBoldStyle = TextStyle(
    fontFamily = NunitoFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 16.sp,
    lineHeight = 24.sp,
    letterSpacing = 0.15.sp
)

val NunitoBoldScreenTitle = TextStyle(
    fontFamily = NunitoFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 24.sp,
)

package org.asv.looplink.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

data object Colors {
    val DarkGrayPrimary = Color(0xFF4A4A4A)
    val LightGrayButton = Color(0xFFB0B0B0)
    val LightBackground = Color(0xFFF8F8F8)
    val LightSurface = Color(0xFFFFFFFF)
    val LightTextColor = Color(0xFF333333)

    // * Dark Theme Colors
//    val WhitePrimary = Color(0xFFFFFFFF)
//    val DarkGrayButton = Color(0xFF4A4A4A)
//    val DarkBackground = Color(0xFF1C1C1C)
//    val DarkSurface = Color.White
//    val DarkTextColor = Color(0xFFE0E0E0)
    val Charcoal = Color(0xFF1A1C20)      // A deep, off-black for the main background
    val SlateGray = Color(0xFF25282D)    // A slightly lighter gray for elevated surfaces like cards
    val SteelGray = Color(0xFF3A3F47)    // For secondary elements and buttons
    val MediumGray = Color(0xFFA0A0A0)    // For secondary or disabled text
    val OffWhite = Color(0xFFF0F0F0)      // A soft white for primary text, easier on the eyes

    // Accent & Brand Palette
    val BrandBlue = Color(0xFF6A88FF)    // A modern, approachable blue for primary actions and branding
    val ElectricTeal = Color(0xFF00F5D4) // A vibrant accent for notifications, unread indicators, and selected items
    val ErrorRed = Color(0xFFFF6B6B)      // For error messages and destructive actions

    // Your existing colors (can be kept or phased out)
    val WhitePrimary = Color(0xFFFFFFFF)
    val DarkGrayButton = Color(0xFF4A4A4A)

    internal val LightColorScheme = lightColorScheme(
        primary = DarkGrayPrimary,
        background = LightBackground,
        onBackground = LightTextColor,
        surface = LightSurface,
        onSurface = DarkGrayPrimary,
        secondary = LightGrayButton,
        onSecondary = LightTextColor
    )

    internal val DarkColorScheme = darkColorScheme(
//        primary = WhitePrimary,
////        tertiary = Color(0xFFEAFF47),
//        background = DarkBackground,
//        onBackground = DarkTextColor,
//        surface = DarkSurface,
//        onSurface = WhitePrimary,
//        secondary = DarkGrayButton,
//        onSecondary = DarkTextColor
        primary = BrandBlue,          // Main interactive color for buttons, links, etc.
        onPrimary = Color.White,      // Text/icons on top of the primary color
        secondary = SteelGray,        // Less prominent actions and buttons
        onSecondary = OffWhite,       // Text/icons on top of the secondary color
        tertiary = ElectricTeal,      // Highlight color for selected items, notifications, etc.
        onTertiary = Charcoal,        // Text/icons on top of the tertiary color

        // Background & Surface Colors
        background = Charcoal,        // The main app background
        onBackground = OffWhite,      // Primary text color on the background
        surface = SlateGray,          // Color for elevated components like Cards, Dialogs, Sheets
        onSurface = OffWhite,         // Primary text color on surfaces

        // Variant Colors
        surfaceVariant = SteelGray,   // Can be used for text field outlines, dividers
        onSurfaceVariant = MediumGray,// Text for hints or less important info

        // Other
        error = ErrorRed,
        onError = Color.White
    )
}
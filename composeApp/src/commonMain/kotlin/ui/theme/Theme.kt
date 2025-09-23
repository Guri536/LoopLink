package ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import org.asv.looplink.theme.Colors
import org.asv.looplink.theme.Colors.DarkBackground
import org.asv.looplink.theme.Colors.DarkGrayButton
import org.asv.looplink.theme.Colors.DarkSurface
import org.asv.looplink.theme.Colors.DarkTextColor
import org.asv.looplink.theme.Colors.WhitePrimary

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    val AppTypography = Typography(
        displayLarge = MaterialTheme.typography.displayLarge.copy(fontFamily = RobotFont),
        bodyLarge = MaterialTheme.typography.bodyLarge.copy(fontFamily = RobotFont),
        bodyMedium = MaterialTheme.typography.bodyMedium.copy(fontFamily = RobotFont),
        bodySmall = MaterialTheme.typography.bodySmall.copy(fontFamily = RobotFont),
        headlineLarge = MaterialTheme.typography.headlineLarge.copy(fontFamily = RobotFont),
        headlineMedium = MaterialTheme.typography.headlineMedium.copy(fontFamily = RobotFont),
        headlineSmall = MaterialTheme.typography.headlineSmall.copy(fontFamily = RobotFont),
        titleLarge = MaterialTheme.typography.titleLarge.copy(fontFamily = RobotFont),
        titleMedium = MaterialTheme.typography.titleMedium.copy(fontFamily = RobotFont),
        titleSmall = MaterialTheme.typography.titleSmall.copy(fontFamily = RobotFont),
        labelLarge = MaterialTheme.typography.labelLarge.copy(fontFamily = RobotFont),
        labelMedium = MaterialTheme.typography.labelMedium.copy(fontFamily = RobotFont),
        labelSmall = MaterialTheme.typography.labelSmall.copy(fontFamily = RobotFont),
        displayMedium = MaterialTheme.typography.displayMedium.copy(fontFamily = RobotFont),
        displaySmall = MaterialTheme.typography.displaySmall.copy(fontFamily = RobotFont),
    )


    MaterialTheme(
        typography = AppTypography,
        colorScheme = Colors.DarkColorScheme,
        content = content
    )
}
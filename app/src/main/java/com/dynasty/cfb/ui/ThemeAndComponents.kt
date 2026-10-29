package com.dynasty.cfb.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dynasty.cfb.data.College
import com.dynasty.cfb.data.LogoShape

val NavyBackground = Color(0xFF070E1A)
val NavySurface = Color(0xFF111C2D)
val NavyCard = Color(0xFF17263C)
val TurfGreen = Color(0xFF10B981)
val ChampionshipGold = Color(0xFFF59E0B)
val CrimsonAlert = Color(0xFFEF4444)
val TextWhite = Color(0xFFF8FAFC)
val TextMuted = Color(0xFF94A3B8)

private val DynastyDarkColorScheme = darkColorScheme(
    primary = TurfGreen,
    onPrimary = Color.Black,
    secondary = ChampionshipGold,
    onSecondary = Color.Black,
    background = NavyBackground,
    onBackground = TextWhite,
    surface = NavySurface,
    onSurface = TextWhite,
    surfaceVariant = NavyCard,
    onSurfaceVariant = TextMuted,
    error = CrimsonAlert
)

@Composable
fun DynastyTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DynastyDarkColorScheme,
        content = content
    )
}

/**
 * Pure flat 2D vector emblem renderer for colleges.
 * Strictly 2D geometric paths and crisp flat shapes with zero shadows or 3D effects.
 */
@Composable
fun CollegeLogo2D(
    college: College,
    size: Dp = 44.dp,
    modifier: Modifier = Modifier
) {
    val primary = Color(college.primaryColorHex)
    val secondary = Color(college.secondaryColorHex)
    val textMeasurer = rememberTextMeasurer()

    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val strokeWidth = w * 0.08f

        when (college.logoShape) {
            LogoShape.CIRCLE -> {
                drawCircle(color = primary, radius = w / 2f)
                drawCircle(color = secondary, radius = w / 2f - strokeWidth / 2f, style = Stroke(width = strokeWidth))
            }
            LogoShape.SHIELD -> {
                val path = Path().apply {
                    moveTo(w * 0.1f, h * 0.08f)
                    lineTo(w * 0.9f, h * 0.08f)
                    lineTo(w * 0.9f, h * 0.55f)
                    lineTo(w * 0.5f, h * 0.94f)
                    lineTo(w * 0.1f, h * 0.55f)
                    close()
                }
                drawPath(path = path, color = primary)
                drawPath(path = path, color = secondary, style = Stroke(width = strokeWidth))
            }
            LogoShape.DIAMOND -> {
                val path = Path().apply {
                    moveTo(w * 0.5f, h * 0.05f)
                    lineTo(w * 0.95f, h * 0.5f)
                    lineTo(w * 0.5f, h * 0.95f)
                    lineTo(w * 0.05f, h * 0.5f)
                    close()
                }
                drawPath(path = path, color = primary)
                drawPath(path = path, color = secondary, style = Stroke(width = strokeWidth))
            }
            LogoShape.HEXAGON -> {
                val path = Path().apply {
                    moveTo(w * 0.25f, h * 0.08f)
                    lineTo(w * 0.75f, h * 0.08f)
                    lineTo(w * 0.95f, h * 0.5f)
                    lineTo(w * 0.75f, h * 0.92f)
                    lineTo(w * 0.25f, h * 0.92f)
                    lineTo(w * 0.05f, h * 0.5f)
                    close()
                }
                drawPath(path = path, color = primary)
                drawPath(path = path, color = secondary, style = Stroke(width = strokeWidth))
            }
            LogoShape.PENNANT -> {
                drawRect(color = primary, topLeft = Offset(w * 0.08f, h * 0.12f), size = Size(w * 0.84f, h * 0.76f))
                drawRect(
                    color = secondary,
                    topLeft = Offset(w * 0.08f, h * 0.12f),
                    size = Size(w * 0.84f, h * 0.76f),
                    style = Stroke(width = strokeWidth)
                )
            }
        }

        val label = college.abbreviation.take(3)
        val style = TextStyle(
            color = secondary,
            fontSize = (size.value * 0.28f).sp,
            fontWeight = FontWeight.ExtraBold
        )
        val measured = textMeasurer.measure(label, style)
        drawText(
            textLayoutResult = measured,
            topLeft = Offset(
                x = (w - measured.size.width) / 2f,
                y = (h - measured.size.height) / 2f
            )
        )
    }
}

@Composable
fun DynastyCard(
    modifier: Modifier = Modifier,
    accentColor: Color = TurfGreen.copy(alpha = 0.28f),
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, accentColor, RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = NavyCard)
    ) {
        Column(modifier = Modifier.padding(14.dp), content = content)
    }
}

@Composable
fun StatBadge(label: String, value: String, highlight: Color = TurfGreen) {
    Column(
        modifier = Modifier
            .background(NavySurface, RoundedCornerShape(10.dp))
            .border(1.dp, highlight.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = label, fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.SemiBold)
        Text(text = value, fontSize = 14.sp, color = TextWhite, fontWeight = FontWeight.Bold)
    }
}

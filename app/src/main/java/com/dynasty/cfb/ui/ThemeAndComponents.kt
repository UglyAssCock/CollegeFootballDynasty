package com.dynasty.cfb.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dynasty.cfb.data.College
import com.dynasty.cfb.data.LogoShape
import java.util.Locale

val NavyBackground = Color(0xFF040B16)
val NavySurface = Color(0xFF091526)
val NavyCard = Color(0xFF0C1D36)
val NavyCardLight = Color(0xFF12294A)
val BorderBlue = Color(0xFF1E3A5F)
val TurfGreen = Color(0xFF00E699)
val TurfGreenDark = Color(0xFF059669)
val ChampionshipGold = Color(0xFFFBBF24)
val CrimsonAlert = Color(0xFFEF4444)
val CyanAccent = Color(0xFF38BDF8)
val PurpleAccent = Color(0xFFA855F7)
val TextWhite = Color(0xFFF8FAFC)
val TextMuted = Color(0xFF94A3B8)

private val DynastyDarkColorScheme = darkColorScheme(
    primary = TurfGreen,
    onPrimary = Color(0xFF022C22),
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

fun fmtMillions(value: Double): String = String.format(Locale.US, "%.1f", value)

/**
 * Top diagonal-striped emerald status banner with crown icon and close button.
 */
@Composable
fun TopStatusBanner(message: String, onDismiss: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .height(44.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(Color(0xFF065F46), Color(0xFF10B981), Color(0xFF059669))
                )
            )
            .border(1.dp, TurfGreen.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
            .clickable { onDismiss() }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val stripePath1 = Path().apply {
                moveTo(w * 0.58f, h)
                lineTo(w * 0.68f, 0f)
                lineTo(w * 0.78f, 0f)
                lineTo(w * 0.68f, h)
                close()
            }
            val stripePath2 = Path().apply {
                moveTo(w * 0.72f, h)
                lineTo(w * 0.82f, 0f)
                lineTo(w * 0.89f, 0f)
                lineTo(w * 0.79f, h)
                close()
            }
            drawPath(stripePath1, Color.White.copy(alpha = 0.14f))
            drawPath(stripePath2, Color.White.copy(alpha = 0.09f))
        }
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CrownVectorIcon(tint = Color.White, size = 20.dp)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = message,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Dismiss",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun CrownVectorIcon(tint: Color = ChampionshipGold, size: Dp = 18.dp) {
    Canvas(modifier = Modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val path = Path().apply {
            moveTo(w * 0.12f, h * 0.78f)
            lineTo(w * 0.08f, h * 0.32f)
            lineTo(w * 0.33f, h * 0.52f)
            lineTo(w * 0.50f, h * 0.18f)
            lineTo(w * 0.67f, h * 0.52f)
            lineTo(w * 0.92f, h * 0.32f)
            lineTo(w * 0.88f, h * 0.78f)
            close()
        }
        drawPath(path, tint)
        drawRoundRect(
            color = tint,
            topLeft = Offset(w * 0.12f, h * 0.82f),
            size = Size(w * 0.76f, h * 0.10f),
            cornerRadius = CornerRadius(4f, 4f)
        )
    }
}

/**
 * Glassmorphic deep-navy card with glowing neon border and optional left accent stripe.
 */
@Composable
fun DynastyCard(
    modifier: Modifier = Modifier,
    accentColor: Color = BorderBlue,
    showLeftStripe: Boolean = false,
    stripeColor: Color = TurfGreen,
    contentPadding: PaddingValues = PaddingValues(14.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val borderBrush = Brush.linearGradient(
        colors = listOf(
            accentColor.copy(alpha = 0.85f),
            accentColor.copy(alpha = 0.35f),
            BorderBlue.copy(alpha = 0.6f)
        )
    )
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(NavyCardLight.copy(alpha = 0.92f), NavyCard)
                )
            )
            .border(1.2.dp, borderBrush, RoundedCornerShape(16.dp))
    ) {
        if (showLeftStripe) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .width(5.dp)
                    .fillMaxHeight()
                    .background(
                        Brush.verticalGradient(listOf(stripeColor, stripeColor.copy(alpha = 0.4f)))
                    )
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(contentPadding),
            content = content
        )
    }
}

/**
 * Collegiate crest logo with outer metallic frame, inner team primary shield, crown/mascot emblem, and bold initials.
 */
@Composable
fun CollegeLogo2D(
    college: College,
    size: Dp = 52.dp,
    modifier: Modifier = Modifier
) {
    val primary = Color(college.primaryColorHex)
    val secondary = Color(college.secondaryColorHex)
    val textMeasurer = rememberTextMeasurer()

    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val strokeOuter = w * 0.07f
        val strokeInner = w * 0.04f

        fun buildShapePath(inset: Float): Path = Path().apply {
            when (college.logoShape) {
                LogoShape.SHIELD, LogoShape.PENNANT -> {
                    moveTo(w * 0.12f + inset, h * 0.08f + inset)
                    lineTo(w * 0.88f - inset, h * 0.08f + inset)
                    lineTo(w * 0.88f - inset, h * 0.56f)
                    lineTo(w * 0.50f, h * 0.94f - inset)
                    lineTo(w * 0.12f + inset, h * 0.56f)
                    close()
                }
                LogoShape.HEXAGON -> {
                    moveTo(w * 0.26f + inset * 0.5f, h * 0.07f + inset)
                    lineTo(w * 0.74f - inset * 0.5f, h * 0.07f + inset)
                    lineTo(w * 0.95f - inset, h * 0.50f)
                    lineTo(w * 0.74f - inset * 0.5f, h * 0.93f - inset)
                    lineTo(w * 0.26f + inset * 0.5f, h * 0.93f - inset)
                    lineTo(w * 0.05f + inset, h * 0.50f)
                    close()
                }
                LogoShape.DIAMOND -> {
                    moveTo(w * 0.50f, h * 0.04f + inset)
                    lineTo(w * 0.96f - inset, h * 0.50f)
                    lineTo(w * 0.50f, h * 0.96f - inset)
                    lineTo(w * 0.04f + inset, h * 0.50f)
                    close()
                }
                LogoShape.CIRCLE -> {
                    addOval(
                        androidx.compose.ui.geometry.Rect(
                            left = w * 0.06f + inset,
                            top = h * 0.06f + inset,
                            right = w * 0.94f - inset,
                            bottom = h * 0.94f - inset
                        )
                    )
                }
            }
        }

        val outerPath = buildShapePath(0f)
        val innerPath = buildShapePath(strokeOuter * 0.8f)

        drawPath(
            path = outerPath,
            brush = Brush.verticalGradient(listOf(primary, primary.copy(alpha = 0.75f)))
        )
        drawPath(
            path = outerPath,
            color = Color.White,
            style = Stroke(width = strokeOuter)
        )
        drawPath(
            path = innerPath,
            color = ChampionshipGold.copy(alpha = 0.8f),
            style = Stroke(width = strokeInner)
        )

        // Small crown/crest detail near top of shield
        val crownPath = Path().apply {
            moveTo(w * 0.34f, h * 0.35f)
            lineTo(w * 0.31f, h * 0.22f)
            lineTo(w * 0.43f, h * 0.29f)
            lineTo(w * 0.50f, h * 0.19f)
            lineTo(w * 0.57f, h * 0.29f)
            lineTo(w * 0.69f, h * 0.22f)
            lineTo(w * 0.66f, h * 0.35f)
            close()
        }
        drawPath(crownPath, color = secondary.copy(alpha = 0.9f))

        val label = college.abbreviation.take(3)
        val style = TextStyle(
            color = Color.White,
            fontSize = (size.value * 0.27f).sp,
            fontWeight = FontWeight.Black
        )
        val measured = textMeasurer.measure(label, style)
        drawText(
            textLayoutResult = measured,
            topLeft = Offset(
                x = (w - measured.size.width) / 2f,
                y = (h * 0.60f - measured.size.height / 2f)
            )
        )
    }
}

/**
 * Subtle fierce bird/mascot watermark drawn on the right side of the Hub Hero Card.
 */
@Composable
fun MascotWatermarkCanvas(modifier: Modifier = Modifier, tint: Color = TurfGreen.copy(alpha = 0.10f)) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val crest = Path().apply {
            moveTo(w * 0.55f, h * 0.08f)
            lineTo(w * 0.72f, h * 0.02f)
            lineTo(w * 0.68f, h * 0.18f)
            lineTo(w * 0.88f, h * 0.12f)
            lineTo(w * 0.80f, h * 0.32f)
            lineTo(w * 0.98f, h * 0.45f)
            lineTo(w * 0.76f, h * 0.58f)
            lineTo(w * 0.92f, h * 0.85f)
            lineTo(w * 0.50f, h * 0.95f)
            lineTo(w * 0.42f, h * 0.52f)
            close()
        }
        drawPath(crest, color = tint)
    }
}

/**
 * Illustrated Coach Avatar wearing a cap and sideline headset microphone (matches Office screenshot).
 */
@Composable
fun CoachHeadsetAvatar(roleCode: String, size: Dp = 62.dp) {
    val accent = when (roleCode) {
        "OC" -> TurfGreen
        "DC" -> CyanAccent
        else -> ChampionshipGold
    }
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.verticalGradient(listOf(Color(0xFF152C4E), Color(0xFF091526)))
            )
            .border(1.5.dp, accent.copy(alpha = 0.65f), RoundedCornerShape(14.dp))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = this.size.width
            val h = this.size.height

            // Shoulders / Coaching Polo
            val shirtPath = Path().apply {
                moveTo(w * 0.12f, h)
                quadraticBezierTo(w * 0.20f, h * 0.68f, w * 0.50f, h * 0.68f)
                quadraticBezierTo(w * 0.80f, h * 0.68f, w * 0.88f, h)
                close()
            }
            drawPath(shirtPath, Color(0xFF1D4ED8))
            drawPath(shirtPath, Color.White.copy(alpha = 0.25f), style = Stroke(width = 2f))

            // Neck & Head silhouette
            drawOval(
                color = Color(0xFFD99B78),
                topLeft = Offset(w * 0.31f, h * 0.24f),
                size = Size(w * 0.38f, h * 0.44f)
            )

            // Coach Cap Dome & Visor
            val capPath = Path().apply {
                moveTo(w * 0.28f, h * 0.38f)
                quadraticBezierTo(w * 0.50f, h * 0.10f, w * 0.72f, h * 0.36f)
                lineTo(w * 0.82f, h * 0.42f)
                lineTo(w * 0.28f, h * 0.38f)
                close()
            }
            drawPath(capPath, Color(0xFF0F172A))
            drawPath(capPath, Color.White.copy(alpha = 0.5f), style = Stroke(width = 2f))

            // Headset Ear Cup & Boom Mic
            drawCircle(
                color = Color(0xFF1E293B),
                radius = w * 0.09f,
                center = Offset(w * 0.31f, h * 0.46f)
            )
            drawCircle(
                color = accent,
                radius = w * 0.09f,
                center = Offset(w * 0.31f, h * 0.46f),
                style = Stroke(width = 2.5f)
            )
            val micPath = Path().apply {
                moveTo(w * 0.31f, h * 0.50f)
                quadraticBezierTo(w * 0.38f, h * 0.62f, w * 0.52f, h * 0.59f)
            }
            drawPath(micPath, Color(0xFF0F172A), style = Stroke(width = 4f))
            drawCircle(color = accent, radius = w * 0.03f, center = Offset(w * 0.53f, h * 0.59f))
        }
    }
}

/**
 * Glowing wireframe football stadium & turf perspective footer (matches Season & Office screenshots).
 */
@Composable
fun StadiumWireframeFooter(showTagline: Boolean = true) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(92.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val lineColor = TurfGreen.copy(alpha = 0.22f)
            val bowlColor = CyanAccent.copy(alpha = 0.16f)

            // Stadium upper bowl curves
            for (i in 1..3) {
                val yOff = i * 12f
                val arcPath = Path().apply {
                    moveTo(0f, yOff)
                    quadraticBezierTo(w * 0.5f, h * 0.65f + yOff * 0.5f, w, yOff)
                }
                drawPath(arcPath, bowlColor, style = Stroke(width = 2f))
            }

            // Turf field trapezoid & perspective yard lines
            val turfTopY = h * 0.48f
            drawLine(lineColor, Offset(w * 0.15f, turfTopY), Offset(w * 0.85f, turfTopY), strokeWidth = 2f)
            drawLine(lineColor, Offset(0f, h), Offset(w, h), strokeWidth = 2f)

            for (i in -3..3) {
                val topX = w * 0.5f + i * (w * 0.09f)
                val botX = w * 0.5f + i * (w * 0.16f)
                drawLine(lineColor, Offset(topX, turfTopY), Offset(botX, h), strokeWidth = 1.5f)
            }
        }
        if (showTagline) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text(
                    text = "C O L L E G E   F O O T B A L L",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TurfGreen
                )
                Text(
                    text = "R A N K I N G S   •   T E A M S   •   L E G A C Y",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextMuted
                )
            }
        }
    }
}

/**
 * Miniature stadium thumbnail with badge overlay for Dynasty Wire News items.
 */
@Composable
fun NewsStadiumThumbnail(badgeText: String) {
    Box(
        modifier = Modifier
            .width(88.dp)
            .height(58.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF1E3A8A), Color(0xFF065F46), Color(0xFF047857))
                )
            )
            .border(1.dp, BorderBlue, RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            drawLine(Color.White.copy(alpha = 0.25f), Offset(0f, h * 0.65f), Offset(w, h * 0.65f), strokeWidth = 1.5f)
            drawLine(Color.White.copy(alpha = 0.2f), Offset(w * 0.3f, h * 0.55f), Offset(w * 0.1f, h), strokeWidth = 1.5f)
            drawLine(Color.White.copy(alpha = 0.2f), Offset(w * 0.7f, h * 0.55f), Offset(w * 0.9f, h), strokeWidth = 1.5f)
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xCC070E1A))
                .border(1.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = badgeText.take(4),
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                color = TextWhite
            )
        }
    }
}

/**
 * Hero stat sub-card used inside the Hub Dashboard Hero Card (PRESTIGE, BUDGET, NIL POOL).
 */
@Composable
fun HeroStatBox(
    icon: ImageVector,
    iconTint: Color,
    label: String,
    valueNode: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(NavySurface.copy(alpha = 0.85f))
            .border(1.dp, iconTint.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(iconTint.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = label, tint = iconTint, modifier = Modifier.size(17.dp))
        }
        Spacer(modifier = Modifier.width(6.dp))
        Column {
            Text(text = label, fontSize = 9.sp, color = TextMuted, fontWeight = FontWeight.Bold)
            valueNode()
        }
    }
}

/**
 * 5-Star rating row using emerald filled stars and muted hollow stars (matches Roster & Recruiting screenshots).
 */
@Composable
fun FiveStarRatingRow(stars: Int, activeColor: Color = TurfGreen, starSize: Dp = 15.dp) {
    Row(horizontalArrangement = Arrangement.spacedBy(2.dp), verticalAlignment = Alignment.CenterVertically) {
        for (i in 1..5) {
            val filled = i <= stars
            Text(
                text = if (filled) "★" else "☆",
                color = if (filled) activeColor else TextMuted.copy(alpha = 0.5f),
                fontSize = starSize.value.sp,
                fontWeight = FontWeight.Bold
            )
        }
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

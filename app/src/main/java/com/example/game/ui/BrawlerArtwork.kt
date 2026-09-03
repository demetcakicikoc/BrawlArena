package com.example.game.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.model.BrawlerType
import kotlin.math.cos
import kotlin.math.sin

/**
 * High-fidelity visual illustrations for Brawl Stars inspired characters:
 * - BOLT (Colt): Vibrant magenta/red pompadour hair, twin silver revolvers, sheriff star, blue vest.
 * - SHELIA (Shelly): Bright purple ponytail, yellow bandana, cheek bandage, shotgun, teal top.
 * - EL GRANDE (El Primo): Royal blue & gold luchador mask, muscular chest, championship gold belt.
 * - SPICA (Spike): Chubby green cactus body, red blooming flower on head, dark button eyes, brown vest.
 */

@Composable
fun BrawlerHeroIllustration(
    brawler: BrawlerType,
    modifier: Modifier = Modifier,
    size: Dp = 130.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "brawler_hero_anim")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = modifier
            .size(size)
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = this.size.width
            val h = this.size.height
            val cx = w / 2f
            val cy = h / 2f
            val r = (w.coerceAtMost(h) / 2f) * 0.92f

            // 1. Back Glow Aura
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        brawler.accentColor.copy(alpha = 0.55f),
                        brawler.accentColor.copy(alpha = 0.15f),
                        Color.Transparent
                    ),
                    center = Offset(cx, cy),
                    radius = r * pulse
                ),
                radius = r * pulse,
                center = Offset(cx, cy)
            )

            // 2. Character Badge Base Disc
            drawCircle(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF2B1650),
                        Color(0xFF16092E)
                    ),
                    startY = cy - r,
                    endY = cy + r
                ),
                radius = r * 0.88f,
                center = Offset(cx, cy)
            )

            // Outer metallic rim
            drawCircle(
                color = brawler.accentColor,
                radius = r * 0.88f,
                center = Offset(cx, cy),
                style = Stroke(width = 4.dp.toPx())
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.4f),
                radius = r * 0.83f,
                center = Offset(cx, cy),
                style = Stroke(width = 1.5.dp.toPx())
            )

            // 3. Draw Unique Iconic Brawler Artwork
            when (brawler) {
                BrawlerType.BOLT -> drawColtIllustration(cx, cy, r * 0.85f)
                BrawlerType.SHELIA -> drawShellyIllustration(cx, cy, r * 0.85f)
                BrawlerType.EL_GRANDE -> drawElPrimoIllustration(cx, cy, r * 0.85f)
                BrawlerType.SPICA -> drawSpikeIllustration(cx, cy, r * 0.85f)
            }
        }
    }
}

@Composable
fun BrawlerAvatarIcon(
    brawler: BrawlerType,
    size: Dp = 46.dp,
    isSelected: Boolean = false,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .background(
                brush = Brush.radialGradient(
                    listOf(brawler.accentColor.copy(alpha = 0.35f), Color(0xFF100720))
                ),
                shape = CircleShape
            )
            .border(
                width = if (isSelected) 3.dp else 1.5.dp,
                color = if (isSelected) Color.White else brawler.accentColor,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(3.dp)) {
            val cx = this.size.width / 2f
            val cy = this.size.height / 2f
            val r = (this.size.width / 2f) * 0.95f

            when (brawler) {
                BrawlerType.BOLT -> drawColtIllustration(cx, cy, r)
                BrawlerType.SHELIA -> drawShellyIllustration(cx, cy, r)
                BrawlerType.EL_GRANDE -> drawElPrimoIllustration(cx, cy, r)
                BrawlerType.SPICA -> drawSpikeIllustration(cx, cy, r)
            }
        }
    }
}

// -------------------------------------------------------------
// 1. COLT (BOLT): Red Pompadour Hair, Dual Revolvers, Sheriff
// -------------------------------------------------------------
private fun DrawScope.drawColtIllustration(cx: Float, cy: Float, radius: Float) {
    val scale = radius / 50f

    // Blue Sheriff Vest / Torso
    drawArc(
        color = Color(0xFF1E88E5),
        startAngle = 0f,
        sweepAngle = 180f,
        useCenter = true,
        topLeft = Offset(cx - 24f * scale, cy + 8f * scale),
        size = Size(48f * scale, 34f * scale)
    )

    // Shirt collar / dark vest trim
    drawArc(
        color = Color(0xFF0D47A1),
        startAngle = 0f,
        sweepAngle = 180f,
        useCenter = false,
        topLeft = Offset(cx - 22f * scale, cy + 12f * scale),
        size = Size(44f * scale, 28f * scale),
        style = Stroke(width = 3f * scale)
    )

    // Sheriff Golden Star Badge on chest
    drawCircle(
        color = Color(0xFFFFD700),
        radius = 4f * scale,
        center = Offset(cx, cy + 20f * scale)
    )

    // Face / Head (Handsome jawline)
    drawRoundRect(
        color = Color(0xFFFFCC80),
        topLeft = Offset(cx - 15f * scale, cy - 14f * scale),
        size = Size(30f * scale, 28f * scale),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f * scale, 10f * scale)
    )

    // Signature Magenta-Red Swept Pompadour Hair
    val hairPath = Path().apply {
        moveTo(cx - 18f * scale, cy - 12f * scale)
        cubicTo(
            cx - 24f * scale, cy - 32f * scale,
            cx - 8f * scale, cy - 40f * scale,
            cx + 6f * scale, cy - 36f * scale
        )
        cubicTo(
            cx + 22f * scale, cy - 32f * scale,
            cx + 22f * scale, cy - 14f * scale,
            cx + 17f * scale, cy - 8f * scale
        )
        lineTo(cx + 14f * scale, cy - 12f * scale)
        cubicTo(
            cx + 6f * scale, cy - 18f * scale,
            cx - 10f * scale, cy - 18f * scale,
            cx - 18f * scale, cy - 12f * scale
        )
        close()
    }
    drawPath(hairPath, Color(0xFFE91E63))
    // Hair highlight crest
    drawArc(
        color = Color(0xFFFF80AB),
        startAngle = 200f,
        sweepAngle = 110f,
        useCenter = false,
        topLeft = Offset(cx - 16f * scale, cy - 36f * scale),
        size = Size(30f * scale, 16f * scale),
        style = Stroke(width = 3.5f * scale, cap = StrokeCap.Round)
    )

    // Sideburns
    drawRect(
        color = Color(0xFFC2185B),
        topLeft = Offset(cx - 16f * scale, cy - 12f * scale),
        size = Size(4f * scale, 10f * scale)
    )
    drawRect(
        color = Color(0xFFC2185B),
        topLeft = Offset(cx + 12f * scale, cy - 12f * scale),
        size = Size(4f * scale, 10f * scale)
    )

    // Eyes (Confident cocky gaze)
    drawCircle(Color(0xFF263238), radius = 2.4f * scale, center = Offset(cx - 7f * scale, cy - 3f * scale))
    drawCircle(Color(0xFF263238), radius = 2.4f * scale, center = Offset(cx + 7f * scale, cy - 3f * scale))
    drawCircle(Color.White, radius = 0.9f * scale, center = Offset(cx - 6.2f * scale, cy - 3.8f * scale))
    drawCircle(Color.White, radius = 0.9f * scale, center = Offset(cx + 7.8f * scale, cy - 3.8f * scale))

    // Eyebrows (Slight smirk slant)
    drawLine(
        color = Color(0xFF880E4F),
        start = Offset(cx - 10f * scale, cy - 7f * scale),
        end = Offset(cx - 4f * scale, cy - 6f * scale),
        strokeWidth = 1.8f * scale,
        cap = StrokeCap.Round
    )
    drawLine(
        color = Color(0xFF880E4F),
        start = Offset(cx + 4f * scale, cy - 6f * scale),
        end = Offset(cx + 10f * scale, cy - 8f * scale),
        strokeWidth = 1.8f * scale,
        cap = StrokeCap.Round
    )

    // Charming Smile
    val smilePath = Path().apply {
        moveTo(cx - 5f * scale, cy + 6f * scale)
        quadraticBezierTo(cx, cy + 9f * scale, cx + 6f * scale, cy + 5.5f * scale)
    }
    drawPath(smilePath, Color(0xFFB71C1C), style = Stroke(width = 1.6f * scale, cap = StrokeCap.Round))

    // Left & Right Dual Silver Revolvers
    drawRevolver(cx - 26f * scale, cy + 8f * scale, scale * 0.9f, isLeft = true)
    drawRevolver(cx + 26f * scale, cy + 8f * scale, scale * 0.9f, isLeft = false)
}

private fun DrawScope.drawRevolver(x: Float, y: Float, scale: Float, isLeft: Boolean) {
    val dir = if (isLeft) -1f else 1f
    // Barrel
    drawRoundRect(
        color = Color(0xFFCFD8DC),
        topLeft = Offset(x - 3f * scale, y - 8f * scale),
        size = Size(6f * scale, 14f * scale),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f * scale, 2f * scale)
    )
    // Cylinder
    drawCircle(
        color = Color(0xFF78909C),
        radius = 4.5f * scale,
        center = Offset(x, y + 4f * scale)
    )
    // Grip
    drawLine(
        color = Color(0xFF5D4037),
        start = Offset(x, y + 6f * scale),
        end = Offset(x + 5f * scale * dir, y + 14f * scale),
        strokeWidth = 3.5f * scale,
        cap = StrokeCap.Round
    )
    // Muzzle Spark
    drawCircle(
        color = Color(0xFFFFEA00),
        radius = 2.2f * scale,
        center = Offset(x, y - 9f * scale)
    )
}

// -------------------------------------------------------------
// 2. SHELLY (SHELIA): Purple Ponytail, Yellow Bandana, Shotgun
// -------------------------------------------------------------
private fun DrawScope.drawShellyIllustration(cx: Float, cy: Float, radius: Float) {
    val scale = radius / 50f

    // Teal / Cyan Cowgirl Top
    drawArc(
        color = Color(0xFF00ACC1),
        startAngle = 0f,
        sweepAngle = 180f,
        useCenter = true,
        topLeft = Offset(cx - 22f * scale, cy + 10f * scale),
        size = Size(44f * scale, 30f * scale)
    )

    // Yellow Neck Bandana (Iconic Shelly scarf)
    val bandanaPath = Path().apply {
        moveTo(cx - 15f * scale, cy + 8f * scale)
        lineTo(cx + 15f * scale, cy + 8f * scale)
        lineTo(cx, cy + 20f * scale)
        close()
    }
    drawPath(bandanaPath, Color(0xFFFFD54F))
    drawCircle(Color(0xFFFFA000), radius = 2.5f * scale, center = Offset(cx, cy + 11f * scale))

    // Face / Head
    drawRoundRect(
        color = Color(0xFFFFD180),
        topLeft = Offset(cx - 14f * scale, cy - 13f * scale),
        size = Size(28f * scale, 26f * scale),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(9f * scale, 9f * scale)
    )

    // Cheek Bandage (Signature Shelly detail)
    drawRoundRect(
        color = Color(0xFFFFE082),
        topLeft = Offset(cx - 13f * scale, cy + 2f * scale),
        size = Size(7f * scale, 4.5f * scale),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(1.5f * scale, 1.5f * scale)
    )
    drawCircle(Color(0xFFFFB300), radius = 1f * scale, center = Offset(cx - 9.5f * scale, cy + 4.2f * scale))

    // Eyes (Fierce, spirited brown/amber eyes)
    drawCircle(Color(0xFF3E2723), radius = 2.5f * scale, center = Offset(cx - 6f * scale, cy - 2f * scale))
    drawCircle(Color(0xFF3E2723), radius = 2.5f * scale, center = Offset(cx + 6f * scale, cy - 2f * scale))
    drawCircle(Color.White, radius = 1f * scale, center = Offset(cx - 5f * scale, cy - 2.8f * scale))
    drawCircle(Color.White, radius = 1f * scale, center = Offset(cx + 7f * scale, cy - 2.8f * scale))

    // Determined Smirk
    val smirkPath = Path().apply {
        moveTo(cx - 4f * scale, cy + 7f * scale)
        quadraticBezierTo(cx, cy + 9f * scale, cx + 5f * scale, cy + 6.5f * scale)
    }
    drawPath(smirkPath, Color(0xFFC2185B), style = Stroke(width = 1.6f * scale, cap = StrokeCap.Round))

    // Signature Bright Violet-Purple Hair & High Ponytail
    // Main Hair Volume
    val hairPath = Path().apply {
        moveTo(cx - 16f * scale, cy - 6f * scale)
        cubicTo(
            cx - 22f * scale, cy - 26f * scale,
            cx - 10f * scale, cy - 35f * scale,
            cx + 2f * scale, cy - 32f * scale
        )
        cubicTo(
            cx + 18f * scale, cy - 30f * scale,
            cx + 20f * scale, cy - 14f * scale,
            cx + 16f * scale, cy - 6f * scale
        )
        lineTo(cx + 12f * scale, cy - 10f * scale)
        cubicTo(
            cx + 4f * scale, cy - 14f * scale,
            cx - 8f * scale, cy - 14f * scale,
            cx - 14f * scale, cy - 8f * scale
        )
        close()
    }
    drawPath(hairPath, Color(0xFF8E24AA))

    // High Ponytail flowing to the right
    val ponytailPath = Path().apply {
        moveTo(cx + 10f * scale, cy - 24f * scale)
        cubicTo(
            cx + 24f * scale, cy - 30f * scale,
            cx + 34f * scale, cy - 16f * scale,
            cx + 30f * scale, cy + 2f * scale
        )
        cubicTo(
            cx + 26f * scale, cy - 8f * scale,
            cx + 18f * scale, cy - 18f * scale,
            cx + 12f * scale, cy - 20f * scale
        )
        close()
    }
    drawPath(ponytailPath, Color(0xFFAB47BC))
    // Ponytail Band
    drawCircle(Color(0xFFFFD54F), radius = 3f * scale, center = Offset(cx + 12f * scale, cy - 22f * scale))

    // Shotgun Barrel across bottom right
    drawLine(
        color = Color(0xFF37474F),
        start = Offset(cx + 4f * scale, cy + 28f * scale),
        end = Offset(cx + 32f * scale, cy + 6f * scale),
        strokeWidth = 5f * scale,
        cap = StrokeCap.Round
    )
    drawLine(
        color = Color(0xFF795548),
        start = Offset(cx + 4f * scale, cy + 28f * scale),
        end = Offset(cx + 14f * scale, cy + 20f * scale),
        strokeWidth = 6.5f * scale,
        cap = StrokeCap.Round
    )
}

// -------------------------------------------------------------
// 3. EL PRIMO (EL GRANDE): Blue & Gold Luchador Mask, Muscle
// -------------------------------------------------------------
private fun DrawScope.drawElPrimoIllustration(cx: Float, cy: Float, radius: Float) {
    val scale = radius / 50f

    // Massive Muscular Torso (Bare chest, tanned skin)
    drawArc(
        color = Color(0xFFD88A58),
        startAngle = 0f,
        sweepAngle = 180f,
        useCenter = true,
        topLeft = Offset(cx - 28f * scale, cy + 8f * scale),
        size = Size(56f * scale, 34f * scale)
    )

    // Pectoral Muscle Definition
    drawArc(
        color = Color(0xFFB56334),
        startAngle = 10f,
        sweepAngle = 160f,
        useCenter = false,
        topLeft = Offset(cx - 20f * scale, cy + 12f * scale),
        size = Size(18f * scale, 12f * scale),
        style = Stroke(width = 2.2f * scale)
    )
    drawArc(
        color = Color(0xFFB56334),
        startAngle = 10f,
        sweepAngle = 160f,
        useCenter = false,
        topLeft = Offset(cx + 2f * scale, cy + 12f * scale),
        size = Size(18f * scale, 12f * scale),
        style = Stroke(width = 2.2f * scale)
    )

    // Championship Gold Wrestling Belt with Big Star Buckle
    drawRect(
        color = Color(0xFF212121),
        topLeft = Offset(cx - 22f * scale, cy + 28f * scale),
        size = Size(44f * scale, 12f * scale)
    )
    drawCircle(
        color = Color(0xFFFFD700),
        radius = 7.5f * scale,
        center = Offset(cx, cy + 34f * scale)
    )
    drawCircle(
        color = Color(0xFFFF1744),
        radius = 2.8f * scale,
        center = Offset(cx, cy + 34f * scale)
    )

    // Iconic Royal Blue Luchador Mask (Head)
    drawRoundRect(
        brush = Brush.verticalGradient(
            listOf(Color(0xFF1976D2), Color(0xFF0D47A1))
        ),
        topLeft = Offset(cx - 17f * scale, cy - 24f * scale),
        size = Size(34f * scale, 36f * scale),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(14f * scale, 14f * scale)
    )

    // Gold Flame Crest on top of the mask
    val flamePath = Path().apply {
        moveTo(cx, cy - 32f * scale)
        lineTo(cx - 7f * scale, cy - 20f * scale)
        lineTo(cx - 2f * scale, cy - 21f * scale)
        lineTo(cx, cy - 26f * scale)
        lineTo(cx + 2f * scale, cy - 21f * scale)
        lineTo(cx + 7f * scale, cy - 20f * scale)
        close()
    }
    drawPath(flamePath, Color(0xFFFFEB3B))

    // Yellow Eye Surround Patterns on the Mask
    drawOval(
        color = Color(0xFFFFC107),
        topLeft = Offset(cx - 13f * scale, cy - 14f * scale),
        size = Size(10f * scale, 12f * scale)
    )
    drawOval(
        color = Color(0xFFFFC107),
        topLeft = Offset(cx + 3f * scale, cy - 14f * scale),
        size = Size(10f * scale, 12f * scale)
    )

    // Mask Eye Cutouts (White with intense black pupils)
    drawOval(
        color = Color.White,
        topLeft = Offset(cx - 11f * scale, cy - 12f * scale),
        size = Size(7.5f * scale, 8.5f * scale)
    )
    drawOval(
        color = Color.White,
        topLeft = Offset(cx + 4.5f * scale, cy - 12f * scale),
        size = Size(7.5f * scale, 8.5f * scale)
    )
    drawCircle(Color(0xFF212121), radius = 2.4f * scale, center = Offset(cx - 7.5f * scale, cy - 8f * scale))
    drawCircle(Color(0xFF212121), radius = 2.4f * scale, center = Offset(cx + 8f * scale, cy - 8f * scale))

    // Mask Mouth Opening & Booming White Tooth Smile
    drawRoundRect(
        color = Color(0xFFD88A58),
        topLeft = Offset(cx - 9f * scale, cy),
        size = Size(18f * scale, 10f * scale),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f * scale, 4f * scale)
    )
    // Big smiling teeth
    drawRoundRect(
        color = Color.White,
        topLeft = Offset(cx - 7f * scale, cy + 2f * scale),
        size = Size(14f * scale, 5.5f * scale),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f * scale, 2f * scale)
    )
}

// -------------------------------------------------------------
// 4. SPIKE (SPICA): Cactus Body, Desert Flower, Button Eyes
// -------------------------------------------------------------
private fun DrawScope.drawSpikeIllustration(cx: Float, cy: Float, radius: Float) {
    val scale = radius / 50f

    // Cute Green Cactus Chubby Body
    drawRoundRect(
        brush = Brush.verticalGradient(
            listOf(Color(0xFF76FF03), Color(0xFF43A047), Color(0xFF2E7D32))
        ),
        topLeft = Offset(cx - 20f * scale, cy - 18f * scale),
        size = Size(40f * scale, 44f * scale),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(18f * scale, 18f * scale)
    )

    // Cactus Arms (Stubby cute arms)
    // Left arm waving
    drawRoundRect(
        color = Color(0xFF43A047),
        topLeft = Offset(cx - 30f * scale, cy - 14f * scale),
        size = Size(12f * scale, 20f * scale),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f * scale, 6f * scale)
    )
    // Right arm holding grenade
    drawRoundRect(
        color = Color(0xFF43A047),
        topLeft = Offset(cx + 18f * scale, cy - 4f * scale),
        size = Size(12f * scale, 18f * scale),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f * scale, 6f * scale)
    )

    // Small Brown Vest
    val vestPath = Path().apply {
        moveTo(cx - 16f * scale, cy + 10f * scale)
        lineTo(cx - 6f * scale, cy + 24f * scale)
        lineTo(cx + 6f * scale, cy + 24f * scale)
        lineTo(cx + 16f * scale, cy + 10f * scale)
        close()
    }
    drawPath(vestPath, Color(0xFF6D4C41))
    // Vest buttons
    drawCircle(Color(0xFFFFD54F), radius = 1.8f * scale, center = Offset(cx, cy + 14f * scale))
    drawCircle(Color(0xFFFFD54F), radius = 1.8f * scale, center = Offset(cx, cy + 19f * scale))

    // Spikes / Needles on the cactus
    val needleColor = Color(0xFF1B5E20)
    fun drawNeedle(nx: Float, ny: Float, angleDeg: Float) {
        val rad = angleDeg * (Math.PI.toFloat() / 180f)
        val dir = Offset(cos(rad), sin(rad))
        drawLine(
            color = needleColor,
            start = Offset(nx, ny),
            end = Offset(nx + dir.x * 5.5f * scale, ny + dir.y * 5.5f * scale),
            strokeWidth = 2f * scale,
            cap = StrokeCap.Round
        )
    }

    drawNeedle(cx - 20f * scale, cy - 8f * scale, 190f)
    drawNeedle(cx - 21f * scale, cy + 4f * scale, 180f)
    drawNeedle(cx + 20f * scale, cy - 8f * scale, -10f)
    drawNeedle(cx + 21f * scale, cy + 4f * scale, 0f)
    drawNeedle(cx - 10f * scale, cy - 16f * scale, -120f)
    drawNeedle(cx + 10f * scale, cy - 16f * scale, -60f)

    // Iconic Red-Pink Blooming Desert Flower on top of Head
    val flowerCenter = Offset(cx, cy - 24f * scale)
    // 5 Petals
    for (i in 0 until 5) {
        val petAngle = (i * 72f) * (Math.PI.toFloat() / 180f)
        val petOffset = Offset(cos(petAngle) * 5.5f * scale, sin(petAngle) * 5.5f * scale)
        drawCircle(
            color = Color(0xFFFF1744),
            radius = 4f * scale,
            center = flowerCenter + petOffset
        )
    }
    // Flower Center Stamen
    drawCircle(Color(0xFFFFEA00), radius = 3f * scale, center = flowerCenter)

    // Dark Button Eyes (Iconic Spike round black eyes)
    drawOval(
        color = Color(0xFF1C2833),
        topLeft = Offset(cx - 11f * scale, cy - 9f * scale),
        size = Size(6.5f * scale, 8.5f * scale)
    )
    drawOval(
        color = Color(0xFF1C2833),
        topLeft = Offset(cx + 4.5f * scale, cy - 9f * scale),
        size = Size(6.5f * scale, 8.5f * scale)
    )
    // Eye shines
    drawCircle(Color.White, radius = 1.2f * scale, center = Offset(cx - 8.5f * scale, cy - 7f * scale))
    drawCircle(Color.White, radius = 1.2f * scale, center = Offset(cx + 7f * scale, cy - 7f * scale))

    // Open Smiling Mouth (Signature oval mouth)
    drawOval(
        color = Color(0xFF1C2833),
        topLeft = Offset(cx - 4.5f * scale, cy + 1.5f * scale),
        size = Size(9f * scale, 6.5f * scale)
    )
    drawOval(
        color = Color(0xFFFF5252),
        topLeft = Offset(cx - 3f * scale, cy + 4f * scale),
        size = Size(6f * scale, 3.5f * scale)
    )

    // Spiked Cactus Grenade held in right hand
    drawCircle(
        color = Color(0xFF76FF03),
        radius = 5.5f * scale,
        center = Offset(cx + 26f * scale, cy + 12f * scale)
    )
    drawCircle(
        color = Color(0xFFD50000),
        radius = 2f * scale,
        center = Offset(cx + 26f * scale, cy + 12f * scale)
    )
}

package com.example.game.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.controller.GameEngine
import com.example.game.model.Team
import com.example.game.model.Vector2D
import kotlin.math.roundToInt
import kotlin.math.sqrt

@Composable
fun GameHUD(
    engine: GameEngine,
    onMove: (Vector2D) -> Unit,
    onAimChange: (direction: Vector2D?, isSuper: Boolean) -> Unit,
    onAttackRelease: (direction: Vector2D, isSuper: Boolean) -> Unit,
    onPauseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        // 1. Top Bar: Gem Grab Scoreboard & Countdown Banner
        TopScoreboard(engine = engine, onPauseClick = onPauseClick)

        // 2. Kill Feed (Top Left under scoreboard)
        if (engine.killFeed.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 16.dp, top = 80.dp)
            ) {
                engine.killFeed.toList().take(3).forEach { feed ->
                    Box(
                        modifier = Modifier
                            .padding(vertical = 2.dp)
                            .background(Color(0x88000000), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = feed,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 3. Bottom Controls: Dual Virtual Joysticks
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            // Left Movement Joystick
            VirtualJoystick(
                label = "HAREKET",
                knobColor = Color(0xFF2979FF),
                onDirectionChange = { dir ->
                    onMove(dir)
                },
                modifier = Modifier.testTag("movement_joystick")
            )

            // Right Combat Controls (Ammo + Attack Joystick + Super Button)
            CombatControls(
                engine = engine,
                onAimChange = onAimChange,
                onAttackRelease = onAttackRelease
            )
        }
    }
}

@Composable
private fun TopScoreboard(engine: GameEngine, onPauseClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, start = 16.dp, end = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Main Mode Score Bar
        Row(
            modifier = Modifier
                .background(
                    brush = Brush.horizontalGradient(
                        listOf(Color(0xCC1565C0), Color(0xCC283593), Color(0xCCC62828))
                    ),
                    shape = RoundedCornerShape(18.dp)
                )
                .border(2.dp, engine.gameMode.accentColor, RoundedCornerShape(18.dp))
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            val blueScore = when (engine.gameMode) {
                com.example.game.model.GameMode.GEM_GRAB -> "💎 ${engine.blueGems}"
                com.example.game.model.GameMode.CASH_GRAB -> "🪙 ${engine.blueCoins}"
                com.example.game.model.GameMode.TAKEDOWN -> "⭐ ${engine.blueStars}"
            }

            val redScore = when (engine.gameMode) {
                com.example.game.model.GameMode.GEM_GRAB -> "${engine.redGems} 💎"
                com.example.game.model.GameMode.CASH_GRAB -> "${engine.redCoins} 🪙"
                com.example.game.model.GameMode.TAKEDOWN -> "${engine.redStars} ⭐"
            }

            // Blue Team Score
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = blueScore,
                    color = Color.White,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Black
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "MAVİ",
                    color = Color(0xFF90CAF9),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            // Center: VS or Match Timer for Takedown
            if (engine.gameMode == com.example.game.model.GameMode.TAKEDOWN) {
                val seconds = engine.takedownTimer.toInt().coerceAtLeast(0)
                val timeStr = "${seconds / 60}:${"%02d".format(seconds % 60)}"
                Box(
                    modifier = Modifier
                        .background(Color(0x66000000), RoundedCornerShape(8.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "⏱️ $timeStr",
                        color = Color(0xFFFFEB3B),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            } else {
                Text(
                    text = "VS",
                    color = Color(0xFFFFD54F),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black
                )
            }

            // Red Team Score
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "KIRMIZI",
                    color = Color(0xFFEF9A9A),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = redScore,
                    color = Color.White,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Black
                )
            }

            // Pause / Exit button
            IconButton(
                onClick = onPauseClick,
                modifier = Modifier
                    .size(32.dp)
                    .background(Color(0x44FFFFFF), CircleShape)
                    .testTag("pause_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Pause,
                    contentDescription = "Duraklat",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Mode-Specific Banner: Countdown or Takedown Lead Status
        if (engine.gameMode == com.example.game.model.GameMode.TAKEDOWN) {
            val starDiff = engine.blueStars - engine.redStars
            val (bannerText, bannerBg) = when {
                starDiff > 0 -> "MAVİ TAKIM ÖNDE (+$starDiff ⭐)" to Color(0xFF1976D2)
                starDiff < 0 -> "KIRMIZI TAKIM ÖNDE (+${-starDiff} ⭐)" to Color(0xFFD32F2F)
                else -> {
                    val tiebreakerStr = when (engine.blueStarHolderTeam) {
                        Team.BLUE -> "Mavi Yıldız Mavi'de"
                        Team.RED -> "Mavi Yıldız Kırmızı'da"
                        else -> "Mavi Yıldız Boşta"
                    }
                    "BERABERE! ($tiebreakerStr)" to Color(0xFF512DA8)
                }
            }

            Box(
                modifier = Modifier
                    .padding(top = 6.dp)
                    .background(bannerBg, RoundedCornerShape(10.dp))
                    .border(2.dp, Color(0xFFFFD54F), RoundedCornerShape(10.dp))
                    .padding(horizontal = 14.dp, vertical = 4.dp)
            ) {
                Text(
                    text = bannerText,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        } else if (engine.countdownTeam != null) {
            val infiniteTransition = rememberInfiniteTransition(label = "pulse")
            val scale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.06f,
                animationSpec = infiniteRepeatable(
                    animation = tween(400, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "countdown_scale"
            )

            val isBlue = engine.countdownTeam == Team.BLUE
            val bannerBg = if (isBlue) Color(0xFF1976D2) else Color(0xFFD32F2F)
            val teamName = if (isBlue) "MAVİ TAKIM" else "KIRMIZI TAKIM"
            val actionText = if (engine.gameMode == com.example.game.model.GameMode.CASH_GRAB) "KASAYI BOŞALTIYOR" else "KAZANIYOR"

            Box(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .scale(scale)
                    .background(bannerBg, RoundedCornerShape(12.dp))
                    .border(3.dp, Color(0xFFFFEB3B), RoundedCornerShape(12.dp))
                    .padding(horizontal = 20.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "$teamName $actionText: ${engine.countdownTimer.toInt()}s!",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

@Composable
private fun CombatControls(
    engine: GameEngine,
    onAimChange: (Vector2D?, Boolean) -> Unit,
    onAttackRelease: (Vector2D, Boolean) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Ammo reload bar indicator (3 bars)
        Row(
            modifier = Modifier.padding(end = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val ammo = engine.player.ammo
            for (i in 0 until 3) {
                val fillRatio = (ammo - i).coerceIn(0f, 1f)
                Box(
                    modifier = Modifier
                        .width(26.dp)
                        .height(7.dp)
                        .background(Color(0x88000000), RoundedCornerShape(3.dp))
                        .border(1.dp, Color.White, RoundedCornerShape(3.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(fillRatio)
                            .background(
                                if (fillRatio >= 1f) Color(0xFFFF9100) else Color(0xFFFFD54F),
                                RoundedCornerShape(3.dp)
                            )
                    )
                }
            }
        }

        // Action Buttons Row (Super Button + Attack Joystick)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Dedicated Super Button / Joystick
            SuperJoystick(
                isReady = engine.player.isSuperReady(),
                charge = engine.player.superCharge,
                onAim = { dir -> onAimChange(dir, true) },
                onRelease = { dir -> onAttackRelease(dir, true) },
                modifier = Modifier.testTag("super_button")
            )

            // Main Attack Joystick / Fire Button
            VirtualJoystick(
                label = "ATEŞ",
                knobColor = Color(0xFFFF3D00),
                onDirectionChange = { dir ->
                    if (dir.length() > 0.05f) {
                        onAimChange(dir, false)
                    } else {
                        onAimChange(null, false)
                    }
                },
                onRelease = { dir ->
                    onAimChange(null, false)
                    onAttackRelease(dir, false)
                },
                modifier = Modifier.testTag("attack_joystick")
            )
        }
    }
}

@Composable
private fun VirtualJoystick(
    label: String,
    knobColor: Color,
    onDirectionChange: (Vector2D) -> Unit,
    onRelease: ((Vector2D) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val radiusPx = 130f
    var dragOffset by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = modifier
            .size(110.dp)
            .background(Color(0x44000000), CircleShape)
            .border(2.5.dp, Color(0x66FFFFFF), CircleShape)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        val centered = Offset(offset.x - 55.dp.toPx(), offset.y - 55.dp.toPx())
                        val dist = sqrt(centered.x * centered.x + centered.y * centered.y)
                        val clampedDist = dist.coerceAtMost(radiusPx)
                        val angle = kotlin.math.atan2(centered.y, centered.x)
                        dragOffset = Offset(
                            clampedDist * kotlin.math.cos(angle),
                            clampedDist * kotlin.math.sin(angle)
                        )
                        val dir = Vector2D(dragOffset.x / radiusPx, dragOffset.y / radiusPx)
                        onDirectionChange(dir)
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val newOffset = dragOffset + dragAmount
                        val dist = sqrt(newOffset.x * newOffset.x + newOffset.y * newOffset.y)
                        val clampedDist = dist.coerceAtMost(radiusPx)
                        val angle = kotlin.math.atan2(newOffset.y, newOffset.x)
                        dragOffset = Offset(
                            clampedDist * kotlin.math.cos(angle),
                            clampedDist * kotlin.math.sin(angle)
                        )
                        val dir = Vector2D(dragOffset.x / radiusPx, dragOffset.y / radiusPx)
                        onDirectionChange(dir)
                    },
                    onDragEnd = {
                        val finalDir = Vector2D(dragOffset.x / radiusPx, dragOffset.y / radiusPx)
                        dragOffset = Offset.Zero
                        onDirectionChange(Vector2D.ZERO)
                        onRelease?.invoke(finalDir)
                    },
                    onDragCancel = {
                        dragOffset = Offset.Zero
                        onDirectionChange(Vector2D.ZERO)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // Knob
        Box(
            modifier = Modifier
                .offset { IntOffset(dragOffset.x.roundToInt(), dragOffset.y.roundToInt()) }
                .size(46.dp)
                .background(knobColor, CircleShape)
                .border(2.dp, Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                color = Color.White,
                fontSize = 9.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
private fun SuperJoystick(
    isReady: Boolean,
    charge: Float,
    onAim: (Vector2D) -> Unit,
    onRelease: (Vector2D) -> Unit,
    modifier: Modifier = Modifier
) {
    val radiusPx = 110f
    var dragOffset by remember { mutableStateOf(Offset.Zero) }

    val infiniteTransition = rememberInfiniteTransition(label = "super_glow")
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isReady) 1.15f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(450, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "super_pulse"
    )

    val superBg = if (isReady) Color(0xFFFFD600) else Color(0xFF616161)
    val superBorder = if (isReady) Color(0xFFFFEA00) else Color(0xFF9E9E9E)

    Box(
        modifier = modifier
            .scale(glowScale)
            .size(76.dp)
            .background(Color(0x66000000), CircleShape)
            .border(3.dp, superBorder, CircleShape)
            .pointerInput(isReady) {
                if (!isReady) return@pointerInput
                detectDragGestures(
                    onDragStart = { offset ->
                        val centered = Offset(offset.x - 38.dp.toPx(), offset.y - 38.dp.toPx())
                        val dist = sqrt(centered.x * centered.x + centered.y * centered.y)
                        val clampedDist = dist.coerceAtMost(radiusPx)
                        val angle = kotlin.math.atan2(centered.y, centered.x)
                        dragOffset = Offset(
                            clampedDist * kotlin.math.cos(angle),
                            clampedDist * kotlin.math.sin(angle)
                        )
                        val dir = Vector2D(dragOffset.x / radiusPx, dragOffset.y / radiusPx)
                        onAim(dir)
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val newOffset = dragOffset + dragAmount
                        val dist = sqrt(newOffset.x * newOffset.x + newOffset.y * newOffset.y)
                        val clampedDist = dist.coerceAtMost(radiusPx)
                        val angle = kotlin.math.atan2(newOffset.y, newOffset.x)
                        dragOffset = Offset(
                            clampedDist * kotlin.math.cos(angle),
                            clampedDist * kotlin.math.sin(angle)
                        )
                        val dir = Vector2D(dragOffset.x / radiusPx, dragOffset.y / radiusPx)
                        onAim(dir)
                    },
                    onDragEnd = {
                        val finalDir = Vector2D(dragOffset.x / radiusPx, dragOffset.y / radiusPx)
                        dragOffset = Offset.Zero
                        onRelease(finalDir)
                    },
                    onDragCancel = {
                        dragOffset = Offset.Zero
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // Inner Super Skull / Star Icon
        Box(
            modifier = Modifier
                .offset { IntOffset(dragOffset.x.roundToInt(), dragOffset.y.roundToInt()) }
                .size(54.dp)
                .background(superBg, CircleShape)
                .border(2.dp, Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (isReady) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Süper Hazır",
                    tint = Color(0xFFD50000),
                    modifier = Modifier.size(32.dp)
                )
            } else {
                Text(
                    text = "${(charge * 100).toInt()}%",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

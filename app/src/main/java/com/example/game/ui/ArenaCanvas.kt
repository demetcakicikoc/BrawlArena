package com.example.game.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.text.*
import androidx.compose.ui.unit.sp
import com.example.game.controller.BrawlerState
import com.example.game.controller.GameEngine
import com.example.game.model.*
import kotlin.math.*

@OptIn(ExperimentalTextApi::class)
@Composable
fun ArenaCanvas(
    engine: GameEngine,
    aimDirection: Vector2D?,
    isAimingSuper: Boolean,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()

    Canvas(modifier = modifier.fillMaxSize()) {
        val canvasW = size.width
        val canvasH = size.height

        // Determine scaling to fit or center camera on player
        val scale = min(canvasW / engine.arenaWidth, canvasH / engine.arenaHeight)
        val offsetX = (canvasW - engine.arenaWidth * scale) / 2f
        val offsetY = (canvasH - engine.arenaHeight * scale) / 2f

        withTransform({
            translate(offsetX, offsetY)
            scale(scale, scale, pivot = Offset.Zero)
        }) {
            // 1. Draw Arena Floor & Grid
            drawArenaGround(engine.arenaWidth, engine.arenaHeight)

            // 2. Draw Effect Areas (Thorn Patch, etc.)
            for (eff in engine.effectAreas.toList()) {
                drawEffectArea(eff)
            }

            // 3. Draw Center Objective Spawner / Emblem
            val cx = engine.arenaWidth / 2f
            val cy = engine.arenaHeight / 2f
            when (engine.gameMode) {
                GameMode.GEM_GRAB -> drawGemSpawner(cx, cy, engine.matchTime)
                GameMode.CASH_GRAB -> drawCoinVault(cx, cy, engine.matchTime)
                GameMode.TAKEDOWN -> drawBountyArenaCenter(cx, cy, engine.matchTime)
            }

            // 4. Draw Collectibles on Ground
            for (gem in engine.gems.toList()) {
                drawCollectible(gem, engine.matchTime)
            }

            // 5. Draw Aim Indicator (if player is dragging attack/super joystick)
            if (aimDirection != null && aimDirection.length() > 0.1f && engine.player.isAlive) {
                drawAimIndicator(engine.player, aimDirection, isAimingSuper)
            }

            // 6. Draw Walls & Destructibles (depth sorting by Y)
            val renderables = mutableListOf<Renderable>()
            val currentObstacles = engine.obstacles.toList()
            val currentBrawlers = engine.brawlers.toList()

            for (obs in currentObstacles) {
                if (obs is Obstacle.Wall) {
                    renderables.add(Renderable.WallRenderable(obs))
                }
            }
            for (brawler in currentBrawlers) {
                if (brawler.isAlive) {
                    renderables.add(Renderable.BrawlerRenderable(brawler))
                }
            }

            // Sort by Y for 2.5D isometric depth
            renderables.sortBy { it.y }

            for (item in renderables) {
                when (item) {
                    is Renderable.WallRenderable -> drawWall(item.wall)
                    is Renderable.BrawlerRenderable -> drawBrawler(item.brawler, engine.matchTime, textMeasurer, engine.gameMode)
                }
            }

            // 7. Draw Bushes (Drawn semi-transparently over brawlers if player is inside)
            for (obs in currentObstacles) {
                if (obs is Obstacle.Bush) {
                    drawBush(obs, engine.player.isInsideBush)
                }
            }

            // 8. Draw Projectiles
            for (p in engine.projectiles.toList()) {
                drawProjectile(p)
            }

            // 9. Draw Floating Combat Text (Damage Numbers, "+1 💎", "SÜPER!")
            for (d in engine.damageNumbers.toList()) {
                drawDamageNumber(d, textMeasurer)
            }
        }
    }
}

private sealed class Renderable(val y: Float) {
    class WallRenderable(val wall: Obstacle.Wall) : Renderable(wall.bounds.bottom)
    class BrawlerRenderable(val brawler: BrawlerState) : Renderable(brawler.position.y)
}

private fun DrawScope.drawArenaGround(width: Float, height: Float) {
    // Ground background with sand / warm battle arena tone
    drawRect(
        brush = Brush.verticalGradient(
            listOf(Color(0xFF2C1654), Color(0xFF1E0F3D), Color(0xFF180A30)),
            startY = 0f,
            endY = height
        ),
        size = Size(width, height)
    )

    // Arena border wall
    drawRect(
        color = Color(0xFFFBC02D),
        topLeft = Offset(0f, 0f),
        size = Size(width, height),
        style = Stroke(width = 8f)
    )

    // Grid checker pattern
    val tileSize = 75f
    val cols = (width / tileSize).toInt()
    val rows = (height / tileSize).toInt()

    for (r in 0..rows) {
        for (c in 0..cols) {
            if ((r + c) % 2 == 0) {
                drawRect(
                    color = Color(0x0CFFFFFF),
                    topLeft = Offset(c * tileSize, r * tileSize),
                    size = Size(tileSize, tileSize)
                )
            }
        }
    }

    // Midfield line
    drawLine(
        color = Color(0x33FFD54F),
        start = Offset(20f, height / 2f),
        end = Offset(width - 20f, height / 2f),
        strokeWidth = 3f,
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 15f), 0f)
    )

    // Team spawn base markers
    drawCircle(
        color = Color(0x222196F3),
        center = Offset(width / 2f, height * 0.88f),
        radius = 180f
    )
    drawCircle(
        color = Color(0x22E53935),
        center = Offset(width / 2f, height * 0.12f),
        radius = 180f
    )
}

private fun DrawScope.drawWall(wall: Obstacle.Wall) {
    val b = wall.bounds
    val w = b.width
    val h = b.height

    // 2.5D Depth Shadow
    drawRect(
        color = Color(0x55000000),
        topLeft = Offset(b.left + 6f, b.top + 10f),
        size = Size(w, h)
    )

    // Front/Side wall body (Darker stone)
    drawRect(
        color = Color(0xFF424242),
        topLeft = Offset(b.left, b.top + 8f),
        size = Size(w, h - 8f)
    )

    // Top elevated face (Lighter stone with 3D bevel)
    drawRect(
        color = Color(0xFF616161),
        topLeft = Offset(b.left, b.top),
        size = Size(w, h - 8f)
    )

    // Top border highlight
    drawRect(
        color = Color(0xFF9E9E9E),
        topLeft = Offset(b.left + 2f, b.top + 2f),
        size = Size(w - 4f, 4f)
    )

    // Wood crate / stone brick cross pattern
    drawRect(
        color = Color(0xFF212121),
        topLeft = Offset(b.left, b.top),
        size = Size(w, h),
        style = Stroke(width = 3f)
    )
}

private fun DrawScope.drawBush(bush: Obstacle.Bush, playerInside: Boolean) {
    val b = bush.bounds
    val alpha = if (playerInside) 0.60f else 0.95f

    // Lush green leafy bush clumps
    val w = b.width
    val h = b.height
    val cols = max(1, (w / 35f).toInt())
    val rows = max(1, (h / 30f).toInt())

    for (r in 0 until rows) {
        for (c in 0 until cols) {
            val cx = b.left + 18f + c * 32f
            val cy = b.top + 15f + r * 28f
            drawCircle(
                color = Color(0xFF1B5E20).copy(alpha = alpha),
                center = Offset(cx + 2f, cy + 3f),
                radius = 22f
            )
            drawCircle(
                color = Color(0xFF2E7D32).copy(alpha = alpha),
                center = Offset(cx, cy),
                radius = 20f
            )
            drawCircle(
                color = Color(0xFF4CAF50).copy(alpha = alpha * 0.8f),
                center = Offset(cx - 4f, cy - 4f),
                radius = 12f
            )
        }
    }
}

private fun DrawScope.drawGemSpawner(cx: Float, cy: Float, time: Float) {
    val pulse = sin(time * 3f) * 4f

    // Ground mine pit
    drawCircle(
        color = Color(0xFF1A1A2E),
        center = Offset(cx, cy),
        radius = 42f
    )
    drawCircle(
        color = Color(0xFF9C27B0),
        center = Offset(cx, cy),
        radius = 36f,
        style = Stroke(width = 4f)
    )

    // Purple energy glow
    drawCircle(
        color = Color(0x66E040FB),
        center = Offset(cx, cy),
        radius = 28f + pulse
    )

    // Rotating mini crystal in center
    val crystalPath = Path().apply {
        moveTo(cx, cy - 18f)
        lineTo(cx + 12f, cy)
        lineTo(cx, cy + 18f)
        lineTo(cx - 12f, cy)
        close()
    }
    drawPath(crystalPath, color = Color(0xFFE040FB))
    drawPath(crystalPath, color = Color.White, style = Stroke(width = 2f))
}

private fun DrawScope.drawCoinVault(cx: Float, cy: Float, time: Float) {
    val pulse = sin(time * 3f) * 4f

    // Golden Ground Vault Pit
    drawCircle(
        color = Color(0xFF261C02),
        center = Offset(cx, cy),
        radius = 44f
    )
    drawCircle(
        color = Color(0xFFFFB300),
        center = Offset(cx, cy),
        radius = 38f,
        style = Stroke(width = 4f)
    )

    // Gold energy glow
    drawCircle(
        color = Color(0x66FFD700),
        center = Offset(cx, cy),
        radius = 28f + pulse
    )

    // Vault Safe Door / Cog
    drawCircle(
        color = Color(0xFFFFC107),
        center = Offset(cx, cy),
        radius = 16f
    )
    drawCircle(
        color = Color(0xFF3E2723),
        center = Offset(cx, cy),
        radius = 16f,
        style = Stroke(width = 2.5f)
    )
}

private fun DrawScope.drawBountyArenaCenter(cx: Float, cy: Float, time: Float) {
    val pulse = sin(time * 2f) * 3f

    // Center arena star pedestal
    drawCircle(
        color = Color(0xFF1A1528),
        center = Offset(cx, cy),
        radius = 48f
    )
    drawCircle(
        color = Color(0xFF00E5FF),
        center = Offset(cx, cy),
        radius = 42f + pulse,
        style = Stroke(width = 3f)
    )
    drawCircle(
        color = Color(0x3340C4FF),
        center = Offset(cx, cy),
        radius = 32f
    )
}

private fun DrawScope.drawCollectible(gem: Gem, time: Float) {
    when (gem.type) {
        CollectibleType.GEM -> drawGemDiamond(gem, time)
        CollectibleType.GOLD_COIN -> drawGoldCoin(gem, time)
        CollectibleType.COIN_SACK -> drawCoinSack(gem, time)
        CollectibleType.BLUE_STAR -> drawBlueStar(gem, time)
    }
}

private fun DrawScope.drawGoldCoin(gem: Gem, time: Float) {
    val bob = sin(time * 6f + gem.position.x) * 4f
    val cx = gem.position.x
    val cy = gem.position.y + bob

    // Drop shadow
    drawOval(
        color = Color(0x55000000),
        topLeft = Offset(cx - 12f, gem.position.y + 10f),
        size = Size(24f, 8f)
    )

    // Outer gold coin rim
    drawCircle(
        color = Color(0xFFFFC107),
        center = Offset(cx, cy),
        radius = 13f
    )
    // Inner coin ridge
    drawCircle(
        color = Color(0xFFFFD54F),
        center = Offset(cx - 1f, cy - 1f),
        radius = 10f
    )
    drawCircle(
        color = Color(0xFFFF8F00),
        center = Offset(cx, cy),
        radius = 13f,
        style = Stroke(width = 2f)
    )
    // Star or notch in center
    drawCircle(
        color = Color(0xFFFFA000),
        center = Offset(cx, cy),
        radius = 4f
    )
}

private fun DrawScope.drawCoinSack(gem: Gem, time: Float) {
    val bob = sin(time * 4f + gem.position.x) * 3f
    val cx = gem.position.x
    val cy = gem.position.y + bob

    // Drop shadow
    drawOval(
        color = Color(0x66000000),
        topLeft = Offset(cx - 15f, gem.position.y + 12f),
        size = Size(30f, 10f)
    )

    // Pouch body
    drawCircle(
        color = Color(0xFF8D6E63),
        center = Offset(cx, cy + 3f),
        radius = 14f
    )
    // Pouch neck / tie
    drawRect(
        color = Color(0xFFFFD54F),
        topLeft = Offset(cx - 5f, cy - 12f),
        size = Size(10f, 6f)
    )
    // Golden dollar / coin emblem
    drawCircle(
        color = Color(0xFFFFD700),
        center = Offset(cx, cy + 3f),
        radius = 6f
    )
    drawCircle(
        color = Color(0xFF4E342E),
        center = Offset(cx, cy + 3f),
        radius = 14f,
        style = Stroke(width = 2f)
    )
}

private fun DrawScope.drawBlueStar(gem: Gem, time: Float) {
    val bob = sin(time * 5f) * 4f
    val cx = gem.position.x
    val cy = gem.position.y + bob

    // Cyan glowing aura
    drawCircle(
        color = Color(0x6600E5FF),
        center = Offset(cx, cy),
        radius = 24f + sin(time * 8f) * 3f
    )

    // Star points
    val p = Path().apply {
        val outerRadius = 18f
        val innerRadius = 8f
        for (i in 0 until 10) {
            val r = if (i % 2 == 0) outerRadius else innerRadius
            val angle = (i * 36f - 90f) * (Math.PI.toFloat() / 180f)
            val px = cx + cos(angle) * r
            val py = cy + sin(angle) * r
            if (i == 0) moveTo(px, py) else lineTo(px, py)
        }
        close()
    }

    drawPath(
        path = p,
        brush = Brush.linearGradient(
            listOf(Color(0xFF80D8FF), Color(0xFF0091EA)),
            start = Offset(cx - 18f, cy - 18f),
            end = Offset(cx + 18f, cy + 18f)
        )
    )
    drawPath(p, color = Color.White, style = Stroke(width = 2f))
}

private fun DrawScope.drawGemDiamond(gem: Gem, time: Float) {
    val bob = sin(time * 5f + gem.position.x) * 4f
    val cx = gem.position.x
    val cy = gem.position.y + bob

    // Gem drop shadow
    drawOval(
        color = Color(0x55000000),
        topLeft = Offset(cx - 14f, gem.position.y + 12f),
        size = Size(28f, 10f)
    )

    // Gem diamond shape
    val p = Path().apply {
        moveTo(cx, cy - 16f)
        lineTo(cx + 13f, cy - 3f)
        lineTo(cx, cy + 16f)
        lineTo(cx - 13f, cy - 3f)
        close()
    }

    // Facet gradient
    drawPath(
        path = p,
        brush = Brush.linearGradient(
            listOf(Color(0xFFE040FB), Color(0xFF7B1FA2), Color(0xFF4A148C)),
            start = Offset(cx - 13f, cy - 16f),
            end = Offset(cx + 13f, cy + 16f)
        )
    )

    // Gem highlight facet
    val facet = Path().apply {
        moveTo(cx, cy - 16f)
        lineTo(cx, cy + 16f)
        lineTo(cx - 13f, cy - 3f)
        close()
    }
    drawPath(facet, color = Color(0x44FFFFFF))

    // Gem outer border
    drawPath(p, color = Color.White, style = Stroke(width = 2.5f))
}

@OptIn(ExperimentalTextApi::class)
private fun DrawScope.drawBrawler(
    brawler: BrawlerState,
    time: Float,
    textMeasurer: TextMeasurer,
    gameMode: GameMode = GameMode.GEM_GRAB
) {
    val visualY = brawler.position.y - brawler.getVisualHeight()
    val center = Offset(brawler.position.x, visualY)
    val radius = brawler.radius

    // 1. Ground Shadow (stays on ground even when jumping!)
    val shadowScale = if (brawler.isAirborne) (1f - (brawler.getVisualHeight() / 180f)).coerceAtLeast(0.4f) else 1f
    drawOval(
        color = Color(0x66000000),
        topLeft = Offset(brawler.position.x - radius * shadowScale, brawler.position.y + radius * 0.7f),
        size = Size(radius * 2f * shadowScale, 14f * shadowScale)
    )

    // If stealth in bush and not user or friendly, don't draw (or draw faint if friendly)
    if (brawler.isInsideBush && !brawler.isUserControlled && brawler.team == Team.RED && !brawler.isFiring) {
        return // Hidden in grass!
    }

    val alpha = if (brawler.isInsideBush) 0.55f else 1.0f

    // 2. Team Ring / Selection Aura
    drawCircle(
        color = brawler.team.primaryColor.copy(alpha = alpha),
        center = center,
        radius = radius + 4f,
        style = Stroke(width = 4f)
    )

    // 3. Brawler Body (2.5D Iconic Cartoon Character)
    when (brawler.type) {
        BrawlerType.BOLT -> drawColtInArena(center, radius, brawler.facingAngle, alpha)
        BrawlerType.SHELIA -> drawShellyInArena(center, radius, brawler.facingAngle, alpha)
        BrawlerType.EL_GRANDE -> drawElPrimoInArena(center, radius, brawler.facingAngle, alpha)
        BrawlerType.SPICA -> drawSpikeInArena(center, radius, brawler.facingAngle, alpha)
    }

    // 4. Direction Facing / Weapon Indicator
    val gunDir = Vector2D.fromAngle(brawler.facingAngle)
    val gunStart = center
    val gunEnd = Offset(center.x + gunDir.x * (radius + 12f), center.y + gunDir.y * (radius + 12f))

    if (brawler.type != BrawlerType.EL_GRANDE) {
        drawLine(
            color = Color.White.copy(alpha = alpha),
            start = gunStart,
            end = gunEnd,
            strokeWidth = 5f,
            cap = StrokeCap.Round
        )
        drawLine(
            color = brawler.type.accentColor.copy(alpha = alpha),
            start = gunStart,
            end = gunEnd,
            strokeWidth = 2.5f,
            cap = StrokeCap.Round
        )
    }

    // 5. Health Bar & Super Meter (Floating above brawler)
    val barW = 60f
    val barH = 8f
    val barX = center.x - barW / 2f
    val barY = center.y - radius - 24f

    // Background
    drawRect(
        color = Color(0xDD000000),
        topLeft = Offset(barX - 2f, barY - 2f),
        size = Size(barW + 4f, barH + 8f)
    )

    // Health Fill (Green for team, Red for enemy)
    val hpRatio = (brawler.currentHp.toFloat() / brawler.maxHp).coerceIn(0f, 1f)
    val hpColor = if (brawler.team == Team.BLUE) Color(0xFF00E676) else Color(0xFFFF1744)
    drawRect(
        color = hpColor,
        topLeft = Offset(barX, barY),
        size = Size(barW * hpRatio, barH)
    )

    // Super charge sub-bar (Yellow)
    val superW = barW * brawler.superCharge.coerceIn(0f, 1f)
    val superColor = if (brawler.isSuperReady()) Color(0xFFFFD600) else Color(0xFFFF9800)
    drawRect(
        color = superColor,
        topLeft = Offset(barX, barY + barH + 1f),
        size = Size(superW, 3f)
    )

    // 6. Mode Badge (Gems, Coins, or Stars)
    when (gameMode) {
        GameMode.GEM_GRAB -> {
            if (brawler.gemsCarried > 0) {
                val gemBadgeX = barX + barW + 10f
                val gemBadgeY = barY + 2f
                drawCircle(
                    color = Color(0xFFE040FB),
                    center = Offset(gemBadgeX, gemBadgeY),
                    radius = 11f
                )
                drawCircle(
                    color = Color.White,
                    center = Offset(gemBadgeX, gemBadgeY),
                    radius = 11f,
                    style = Stroke(width = 1.5f)
                )

                val gemText = "${brawler.gemsCarried}"
                val gemTextLayout = textMeasurer.measure(
                    text = AnnotatedString(gemText),
                    style = TextStyle(color = Color.White, fontSize = 10.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                )
                drawText(
                    textLayoutResult = gemTextLayout,
                    topLeft = Offset(gemBadgeX - gemTextLayout.size.width / 2f, gemBadgeY - gemTextLayout.size.height / 2f)
                )
            }
        }
        GameMode.CASH_GRAB -> {
            if (brawler.coinsCarried > 0) {
                val coinBadgeX = barX + barW + 10f
                val coinBadgeY = barY + 2f
                drawCircle(
                    color = Color(0xFFFFD700),
                    center = Offset(coinBadgeX, coinBadgeY),
                    radius = 11f
                )
                drawCircle(
                    color = Color(0xFF3E2723),
                    center = Offset(coinBadgeX, coinBadgeY),
                    radius = 11f,
                    style = Stroke(width = 1.5f)
                )

                val coinText = "${brawler.coinsCarried}"
                val coinTextLayout = textMeasurer.measure(
                    text = AnnotatedString(coinText),
                    style = TextStyle(color = Color(0xFF3E2723), fontSize = 10.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Black)
                )
                drawText(
                    textLayoutResult = coinTextLayout,
                    topLeft = Offset(coinBadgeX - coinTextLayout.size.width / 2f, coinBadgeY - coinTextLayout.size.height / 2f)
                )
            }
        }
        GameMode.TAKEDOWN -> {
            val starBadgeX = barX + barW + 12f
            val starBadgeY = barY + 2f
            drawRoundRect(
                color = Color(0xFFFFC107),
                topLeft = Offset(starBadgeX - 11f, starBadgeY - 9f),
                size = Size(22f, 18f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
            )
            drawRoundRect(
                color = Color.White,
                topLeft = Offset(starBadgeX - 11f, starBadgeY - 9f),
                size = Size(22f, 18f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f),
                style = Stroke(width = 1.5f)
            )

            val starText = "★${brawler.bountyStars}"
            val starTextLayout = textMeasurer.measure(
                text = AnnotatedString(starText),
                style = TextStyle(color = Color(0xFF3E2723), fontSize = 9.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Black)
            )
            drawText(
                textLayoutResult = starTextLayout,
                topLeft = Offset(starBadgeX - starTextLayout.size.width / 2f, starBadgeY - starTextLayout.size.height / 2f)
            )
        }
    }

    // Name tag
    val nameLayout = textMeasurer.measure(
        text = AnnotatedString(brawler.name),
        style = TextStyle(color = Color.White, fontSize = 11.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
    )
    drawText(
        textLayoutResult = nameLayout,
        topLeft = Offset(center.x - nameLayout.size.width / 2f, barY - 16f)
    )
}

private fun DrawScope.drawAimIndicator(
    player: BrawlerState,
    direction: Vector2D,
    isSuper: Boolean
) {
    val start = Offset(player.position.x, player.position.y)
    val dir = direction.normalized()
    val range = if (isSuper) player.type.attackRange * 1.25f else player.type.attackRange
    val aimColor = if (isSuper) Color(0xAAFFD600) else Color(0xAA00E5FF)

    when (player.type) {
        BrawlerType.BOLT -> {
            // Straight line laser trajectory
            val end = Offset(start.x + dir.x * range, start.y + dir.y * range)
            drawLine(
                color = aimColor,
                start = start,
                end = end,
                strokeWidth = if (isSuper) 18f else 10f,
                cap = StrokeCap.Round
            )
            drawCircle(
                color = aimColor,
                center = end,
                radius = if (isSuper) 16f else 10f
            )
        }
        BrawlerType.SHELIA -> {
            // Wide shotgun cone
            val angle = dir.angle()
            val spread = if (isSuper) 0.38f else 0.28f
            val conePath = Path().apply {
                moveTo(start.x, start.y)
                lineTo(start.x + cos(angle - spread) * range, start.y + sin(angle - spread) * range)
                lineTo(start.x + cos(angle + spread) * range, start.y + sin(angle + spread) * range)
                close()
            }
            drawPath(conePath, color = aimColor.copy(alpha = 0.25f))
            drawPath(conePath, color = aimColor, style = Stroke(width = 3f))
        }
        BrawlerType.EL_GRANDE -> {
            if (isSuper) {
                // Leap target landing circle
                val leapEnd = Offset(start.x + dir.x * 380f, start.y + dir.y * 380f)
                drawLine(color = aimColor, start = start, end = leapEnd, strokeWidth = 4f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 10f)))
                drawCircle(color = Color(0x44FF9100), center = leapEnd, radius = 120f)
                drawCircle(color = Color(0xFFFF9100), center = leapEnd, radius = 120f, style = Stroke(width = 4f))
            } else {
                // Melee punch arc
                val end = Offset(start.x + dir.x * 160f, start.y + dir.y * 160f)
                drawCircle(color = aimColor.copy(alpha = 0.3f), center = end, radius = 50f)
            }
        }
        BrawlerType.SPICA -> {
            val end = Offset(start.x + dir.x * range, start.y + dir.y * range)
            drawLine(color = aimColor, start = start, end = end, strokeWidth = 4f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f)))
            drawCircle(color = aimColor.copy(alpha = 0.35f), center = end, radius = if (isSuper) 140f else 40f)
            drawCircle(color = aimColor, center = end, radius = if (isSuper) 140f else 40f, style = Stroke(width = 3f))
        }
    }
}

private fun DrawScope.drawEffectArea(eff: EffectArea) {
    val center = Offset(eff.center.x, eff.center.y)

    // Pulsing thorny field
    drawCircle(
        color = eff.color,
        center = center,
        radius = eff.radius
    )
    drawCircle(
        color = Color(0xFF64DD17),
        center = center,
        radius = eff.radius,
        style = Stroke(width = 4f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 10f)))
    )

    // Inner thorny spikes
    for (i in 0 until 8) {
        val angle = (i * 45f) * (Math.PI.toFloat() / 180f)
        val spikeEnd = Offset(center.x + cos(angle) * (eff.radius * 0.7f), center.y + sin(angle) * (eff.radius * 0.7f))
        drawLine(
            color = Color(0xFF1B5E20),
            start = center,
            end = spikeEnd,
            strokeWidth = 3f
        )
    }
}

private fun DrawScope.drawProjectile(p: Projectile) {
    val pos = Offset(p.position.x, p.position.y)

    when (p.type) {
        ProjectileType.BOLT_BULLET, ProjectileType.BOLT_SUPER_BULLET -> {
            // Speed tracer
            val backDir = p.velocity.normalized() * -1f
            val tail = Offset(pos.x + backDir.x * 24f, pos.y + backDir.y * 24f)
            drawLine(
                color = p.color,
                start = tail,
                end = pos,
                strokeWidth = p.radius * 1.5f,
                cap = StrokeCap.Round
            )
            drawCircle(color = Color.White, center = pos, radius = p.radius * 0.7f)
        }
        ProjectileType.SHELIA_PELLET, ProjectileType.SHELIA_SUPER_PELLET -> {
            drawCircle(color = p.color, center = pos, radius = p.radius)
            drawCircle(color = Color.White, center = pos, radius = p.radius * 0.5f)
        }
        ProjectileType.EL_GRANDE_PUNCH -> {
            // Comic shockwave punch wave
            drawCircle(color = p.color.copy(alpha = 0.8f), center = pos, radius = p.radius)
            drawCircle(color = Color.White, center = pos, radius = p.radius * 0.6f)
        }
        ProjectileType.SPICA_BOMB -> {
            // Cactus bomb
            drawCircle(color = Color(0xFF2E7D32), center = pos, radius = p.radius)
            drawCircle(color = Color(0xFF76FF03), center = pos, radius = p.radius - 4f)
            // Needles poking out
            for (i in 0 until 6) {
                val a = (i * 60f) * (Math.PI.toFloat() / 180f)
                val nEnd = Offset(pos.x + cos(a) * (p.radius + 6f), pos.y + sin(a) * (p.radius + 6f))
                drawLine(color = Color.White, start = pos, end = nEnd, strokeWidth = 2.5f)
            }
        }
        ProjectileType.SPICA_NEEDLE -> {
            drawCircle(color = Color(0xFF76FF03), center = pos, radius = p.radius)
            drawCircle(color = Color.White, center = pos, radius = p.radius * 0.5f)
        }
        ProjectileType.SPICA_SUPER_BOMB -> {
            drawCircle(color = Color(0xFFE91E63), center = pos, radius = p.radius)
        }
    }
}

@OptIn(ExperimentalTextApi::class)
private fun DrawScope.drawDamageNumber(d: DamageNumber, textMeasurer: TextMeasurer) {
    val layout = textMeasurer.measure(
        text = AnnotatedString(d.text),
        style = TextStyle(
            color = d.color.copy(alpha = d.alpha),
            fontSize = 14.sp,
            fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold
        )
    )
    drawText(
        textLayoutResult = layout,
        topLeft = Offset(d.position.x - layout.size.width / 2f, d.position.y)
    )
}

// -------------------------------------------------------------
// IN-ARENA ICONIC BRAWLER RENDERING
// -------------------------------------------------------------

private fun DrawScope.drawColtInArena(center: Offset, radius: Float, facingAngle: Float, alpha: Float) {
    // Torso (Blue vest)
    drawCircle(
        color = Color(0xFF1565C0).copy(alpha = alpha),
        center = center,
        radius = radius
    )
    // Darker rim
    drawCircle(
        color = Color(0xFF0D47A1).copy(alpha = alpha),
        center = center,
        radius = radius,
        style = Stroke(width = 2.5f)
    )
    // Head / Face
    drawCircle(
        color = Color(0xFFFFCC80).copy(alpha = alpha),
        center = center,
        radius = radius * 0.65f
    )
    // Signature Magenta-Red Swept Pompadour Hair
    val hairDir = Vector2D.fromAngle(facingAngle)
    val hairPos = Offset(center.x - hairDir.x * (radius * 0.2f), center.y - hairDir.y * (radius * 0.2f) - 3f)
    drawCircle(
        color = Color(0xFFE91E63).copy(alpha = alpha),
        center = hairPos,
        radius = radius * 0.5f
    )
    drawCircle(
        color = Color(0xFFFF4081).copy(alpha = alpha),
        center = hairPos,
        radius = radius * 0.28f
    )
    // Sheriff gold badge
    drawCircle(
        color = Color(0xFFFFD700).copy(alpha = alpha),
        center = Offset(center.x + hairDir.x * (radius * 0.45f), center.y + hairDir.y * (radius * 0.45f)),
        radius = 3.5f
    )
}

private fun DrawScope.drawShellyInArena(center: Offset, radius: Float, facingAngle: Float, alpha: Float) {
    // Body (Teal / turquoise)
    drawCircle(
        color = Color(0xFF00838F).copy(alpha = alpha),
        center = center,
        radius = radius
    )
    drawCircle(
        color = Color(0xFF006064).copy(alpha = alpha),
        center = center,
        radius = radius,
        style = Stroke(width = 2.5f)
    )
    // Face
    drawCircle(
        color = Color(0xFFFFD180).copy(alpha = alpha),
        center = center,
        radius = radius * 0.65f
    )
    // Purple Hair
    drawCircle(
        color = Color(0xFF8E24AA).copy(alpha = alpha),
        center = Offset(center.x, center.y - 2f),
        radius = radius * 0.52f
    )
    // Signature High Ponytail on back
    val dir = Vector2D.fromAngle(facingAngle)
    val ponyPos = Offset(center.x - dir.x * (radius * 0.7f), center.y - dir.y * (radius * 0.7f) - 4f)
    drawCircle(
        color = Color(0xFFAB47BC).copy(alpha = alpha),
        center = ponyPos,
        radius = radius * 0.38f
    )
    drawCircle(
        color = Color(0xFFFFD54F).copy(alpha = alpha),
        center = ponyPos,
        radius = 3f
    )
    // Yellow bandana around neck
    drawCircle(
        color = Color(0xFFFFD54F).copy(alpha = alpha),
        center = Offset(center.x + dir.x * (radius * 0.3f), center.y + dir.y * (radius * 0.3f)),
        radius = 4f
    )
}

private fun DrawScope.drawElPrimoInArena(center: Offset, radius: Float, facingAngle: Float, alpha: Float) {
    // Muscular Bare Shoulders (Tan skin)
    drawCircle(
        color = Color(0xFFD88A58).copy(alpha = alpha),
        center = center,
        radius = radius
    )
    drawCircle(
        color = Color(0xFFBF360C).copy(alpha = alpha),
        center = center,
        radius = radius,
        style = Stroke(width = 2.5f)
    )

    // Royal Blue Luchador Mask Head
    drawCircle(
        brush = Brush.radialGradient(
            listOf(Color(0xFF1976D2), Color(0xFF0D47A1)),
            center = center,
            radius = radius * 0.68f
        ),
        center = center,
        radius = radius * 0.68f
    )

    // Gold Flame Motif on Mask
    val flameDir = Vector2D.fromAngle(facingAngle)
    val crestPos = Offset(center.x - flameDir.x * 2f, center.y - radius * 0.45f)
    drawCircle(
        color = Color(0xFFFFD700).copy(alpha = alpha),
        center = crestPos,
        radius = radius * 0.26f
    )
    // White eye cutouts
    val perp = Vector2D(-flameDir.y, flameDir.x)
    val eyeCenter = Offset(center.x + flameDir.x * (radius * 0.2f), center.y + flameDir.y * (radius * 0.2f))
    drawCircle(Color.White.copy(alpha = alpha), center = Offset(eyeCenter.x - perp.x * 6f, eyeCenter.y - perp.y * 6f), radius = 2.5f)
    drawCircle(Color.White.copy(alpha = alpha), center = Offset(eyeCenter.x + perp.x * 6f, eyeCenter.y + perp.y * 6f), radius = 2.5f)

    // Fists in front
    val fist1 = Offset(center.x + flameDir.x * (radius + 4f) - perp.x * 10f, center.y + flameDir.y * (radius + 4f) - perp.y * 10f)
    val fist2 = Offset(center.x + flameDir.x * (radius + 4f) + perp.x * 10f, center.y + flameDir.y * (radius + 4f) + perp.y * 10f)
    drawCircle(Color(0xFFD88A58).copy(alpha = alpha), center = fist1, radius = 5.5f)
    drawCircle(Color(0xFFD88A58).copy(alpha = alpha), center = fist2, radius = 5.5f)
    drawCircle(Color(0xFFFFD700).copy(alpha = alpha), center = fist1, radius = 2.5f)
    drawCircle(Color(0xFFFFD700).copy(alpha = alpha), center = fist2, radius = 2.5f)
}

private fun DrawScope.drawSpikeInArena(center: Offset, radius: Float, facingAngle: Float, alpha: Float) {
    // Green Cactus Chubby Body
    drawCircle(
        brush = Brush.radialGradient(
            listOf(Color(0xFF76FF03), Color(0xFF43A047), Color(0xFF1B5E20)),
            center = center,
            radius = radius
        ),
        center = center,
        radius = radius
    )
    drawCircle(
        color = Color(0xFF1B5E20).copy(alpha = alpha),
        center = center,
        radius = radius,
        style = Stroke(width = 2.5f)
    )

    // Cactus needles around body edge
    for (i in 0 until 8) {
        val needleAngle = (i * 45f) * (Math.PI.toFloat() / 180f)
        val nx = center.x + cos(needleAngle) * radius
        val ny = center.y + sin(needleAngle) * radius
        drawLine(
            color = Color(0xFF003300).copy(alpha = alpha),
            start = Offset(nx, ny),
            end = Offset(nx + cos(needleAngle) * 4f, ny + sin(needleAngle) * 4f),
            strokeWidth = 2f,
            cap = StrokeCap.Round
        )
    }

    // Brown vest trim
    drawCircle(
        color = Color(0xFF5D4037).copy(alpha = alpha),
        center = center,
        radius = radius * 0.45f
    )

    // Dark round button eyes
    val dir = Vector2D.fromAngle(facingAngle)
    val perp = Vector2D(-dir.y, dir.x)
    val faceCenter = Offset(center.x + dir.x * (radius * 0.25f), center.y + dir.y * (radius * 0.25f))
    drawCircle(Color(0xFF1C2833).copy(alpha = alpha), center = Offset(faceCenter.x - perp.x * 5f, faceCenter.y - perp.y * 5f), radius = 2.8f)
    drawCircle(Color(0xFF1C2833).copy(alpha = alpha), center = Offset(faceCenter.x + perp.x * 5f, faceCenter.y + perp.y * 5f), radius = 2.8f)

    // Iconic Pink/Red Desert Flower on top of Head
    val flowerPos = Offset(center.x, center.y - radius * 0.65f)
    for (i in 0 until 5) {
        val petAngle = (i * 72f) * (Math.PI.toFloat() / 180f)
        drawCircle(
            color = Color(0xFFFF1744).copy(alpha = alpha),
            center = Offset(flowerPos.x + cos(petAngle) * 4.5f, flowerPos.y + sin(petAngle) * 4.5f),
            radius = 3.5f
        )
    }
    drawCircle(Color(0xFFFFEA00).copy(alpha = alpha), center = flowerPos, radius = 2.5f)
}

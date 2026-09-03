package com.example.game.controller

import com.example.game.model.BrawlerType
import com.example.game.model.Gem
import com.example.game.model.Team
import com.example.game.model.Vector2D
import kotlin.math.atan2
import kotlin.random.Random

class BotAI(
    val brawler: BrawlerState,
    private val botDifficulty: Float = 1.0f
) {
    private var actionTimer: Float = Random.nextFloat() * 0.5f
    private var targetMoveDir: Vector2D = Vector2D.ZERO
    private var desiredAimDir: Vector2D = Vector2D.ZERO

    fun update(
        dt: Float,
        allBrawlers: List<BrawlerState>,
        gems: List<Gem>,
        centerPos: Vector2D,
        teamIsCountingDown: Boolean,
        gameMode: com.example.game.model.GameMode = com.example.game.model.GameMode.GEM_GRAB,
        onAttack: (direction: Vector2D, isSuper: Boolean) -> Unit
    ) {
        if (!brawler.isAlive || brawler.isAirborne) return

        actionTimer -= dt
        val enemies = allBrawlers.filter { it.isAlive && it.team != brawler.team && !it.isAirborne }
        // In Takedown (Bounty), bots prioritize enemies with high bounties!
        val nearestEnemy = if (gameMode == com.example.game.model.GameMode.TAKEDOWN) {
            enemies.minByOrNull { it.position.distanceTo(brawler.position) - (it.bountyStars * 40f) }
        } else {
            enemies.minByOrNull { it.position.distanceTo(brawler.position) }
        }
        val basePos = if (brawler.team == Team.BLUE) Vector2D(centerPos.x, centerPos.y + 400f) else Vector2D(centerPos.x, centerPos.y - 400f)

        // 1. Determine Movement Objective
        val hpRatio = brawler.currentHp.toFloat() / brawler.maxHp
        val carriesManyCollectibles = when (gameMode) {
            com.example.game.model.GameMode.TAKEDOWN -> brawler.bountyStars >= 5
            com.example.game.model.GameMode.CASH_GRAB -> brawler.coinsCarried >= 5 || (teamIsCountingDown && brawler.coinsCarried >= 2)
            com.example.game.model.GameMode.GEM_GRAB -> brawler.gemsCarried >= 4 || (teamIsCountingDown && brawler.gemsCarried >= 1)
        }

        val shouldRetreat = (hpRatio < 0.35f) || (carriesManyCollectibles && (nearestEnemy?.let { it.position.distanceTo(brawler.position) < 350f } ?: false))

        if (actionTimer <= 0f) {
            actionTimer = 0.2f + Random.nextFloat() * 0.25f

            if (shouldRetreat) {
                // Retreat towards team spawn base
                val retreatDir = (basePos - brawler.position).normalized()
                targetMoveDir = retreatDir
            } else {
                // Look for nearby uncollected gems or coins or center star
                val nearestGem = gems.minByOrNull {
                    val dist = it.position.distanceTo(brawler.position)
                    // Prioritize coin sacks in cash grab or center star in takedown
                    if (it.type == com.example.game.model.CollectibleType.COIN_SACK || it.type == com.example.game.model.CollectibleType.BLUE_STAR) {
                        dist - 100f
                    } else {
                        dist
                    }
                }
                val gemDistance = nearestGem?.position?.distanceTo(brawler.position) ?: Float.MAX_VALUE

                if (nearestGem != null && (gemDistance < 450f || nearestGem.type == com.example.game.model.CollectibleType.BLUE_STAR)) {
                    // Go for collectible
                    targetMoveDir = (nearestGem.position - brawler.position).normalized()
                } else if (nearestEnemy != null) {
                    val dist = nearestEnemy.position.distanceTo(brawler.position)
                    val idealRange = when (brawler.type) {
                        BrawlerType.EL_GRANDE -> 160f
                        BrawlerType.SHELIA -> 220f
                        BrawlerType.SPICA -> 350f
                        BrawlerType.BOLT -> 400f
                    }

                    if (dist > idealRange + 60f) {
                        // Move closer
                        targetMoveDir = (nearestEnemy.position - brawler.position).normalized()
                    } else if (dist < idealRange - 60f && brawler.type != BrawlerType.EL_GRANDE) {
                        // Kite away
                        targetMoveDir = (brawler.position - nearestEnemy.position).normalized()
                    } else {
                        // Strafe sideways
                        val toEnemy = (nearestEnemy.position - brawler.position).normalized()
                        val perp = Vector2D(-toEnemy.y, toEnemy.x)
                        targetMoveDir = if (Random.nextBoolean()) perp else perp * -1f
                    }
                } else {
                    // Roam to center
                    targetMoveDir = (centerPos - brawler.position).normalized()
                }
            }
        }

        // Apply movement velocity
        val encumbrance = brawler.getSpeedMultiplier()
        brawler.velocity = targetMoveDir * (brawler.type.moveSpeed * encumbrance)

        // 2. Combat & Aiming
        if (nearestEnemy != null) {
            val dist = nearestEnemy.position.distanceTo(brawler.position)
            val enemyDir = (nearestEnemy.position - brawler.position).normalized()
            brawler.facingAngle = atan2(enemyDir.y, enemyDir.x)

            // Evaluate Super usage
            if (brawler.isSuperReady()) {
                val superEffectiveRange = when (brawler.type) {
                    BrawlerType.BOLT -> 520f
                    BrawlerType.SHELIA -> 320f
                    BrawlerType.EL_GRANDE -> 400f
                    BrawlerType.SPICA -> 420f
                }

                if (dist <= superEffectiveRange) {
                    // Fire Super!
                    onAttack(enemyDir, true)
                    return
                }
            }

            // Normal Attack
            if (brawler.canAttack() && dist <= brawler.type.attackRange) {
                // Lead the shot slightly
                val leadTarget = nearestEnemy.position + nearestEnemy.velocity * 0.15f
                val attackDir = (leadTarget - brawler.position).normalized()
                onAttack(attackDir, false)
            }
        }
    }
}

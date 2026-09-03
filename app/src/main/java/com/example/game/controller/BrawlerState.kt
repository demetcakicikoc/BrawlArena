package com.example.game.controller

import com.example.game.model.BrawlerType
import com.example.game.model.Team
import com.example.game.model.Vector2D
import kotlin.math.sin

data class QueuedShot(
    var delay: Float,
    val isSuper: Boolean,
    val direction: Vector2D
)

class BrawlerState(
    val id: String,
    val name: String,
    val type: BrawlerType,
    val team: Team,
    val isUserControlled: Boolean = false,
    var position: Vector2D,
    var spawnPosition: Vector2D = position
) {
    var maxHp: Int = type.maxHp
    var currentHp: Int = maxHp
    var isAlive: Boolean = true
    var respawnTimer: Float = 0f

    // Movement & Orientation
    var velocity: Vector2D = Vector2D.ZERO
    var facingAngle: Float = if (team == Team.BLUE) -1.57f else 1.57f // Blue faces up, Red faces down
    val radius: Float = 26f
    var knockbackVelocity: Vector2D = Vector2D.ZERO

    // Ammo & Super
    var ammo: Float = type.maxAmmo.toFloat()
    var superCharge: Float = 0f // 0f to 1f
    var attackCooldown: Float = 0f
    val queuedShots = mutableListOf<QueuedShot>()

    // Health Regeneration
    var timeSinceDamageTaken: Float = 0f
    private val outOfCombatHealDelay: Float = 3.0f

    // Status
    var isInsideBush: Boolean = false
    var isFiring: Boolean = false
    var firingAnimationTimer: Float = 0f
    var gemsCarried: Int = 0
    var coinsCarried: Int = 0
    var bountyStars: Int = 2 // In Takedown (Bounty), starts at 2, max 7
    var kills: Int = 0
    var deaths: Int = 0
    var damageDealt: Int = 0

    fun getSpeedMultiplier(): Float {
        // Cash Grab encumbrance: carrying heavy bags of gold (>= 6 coins) reduces speed slightly
        return if (coinsCarried >= 6) 0.92f else 1.0f
    }

    // Leap / Super mechanics (El Grande meteor leap)
    var isAirborne: Boolean = false
    var jumpProgress: Float = 0f
    var jumpDuration: Float = 0.9f
    var jumpStartPos: Vector2D = Vector2D.ZERO
    var jumpTargetPos: Vector2D = Vector2D.ZERO
    var jumpPeakHeight: Float = 140f

    fun isSuperReady(): Boolean = superCharge >= 1.0f

    fun canAttack(): Boolean = isAlive && !isAirborne && ammo >= 1.0f && attackCooldown <= 0f

    fun update(dt: Float) {
        if (!isAlive) {
            respawnTimer -= dt
            if (respawnTimer <= 0f) {
                respawn()
            }
            return
        }

        // Airborne leap logic
        if (isAirborne) {
            jumpProgress += dt / jumpDuration
            if (jumpProgress >= 1f) {
                isAirborne = false
                jumpProgress = 1f
                position = jumpTargetPos
            } else {
                val t = jumpProgress
                val currentGround = Vector2D(
                    jumpStartPos.x + (jumpTargetPos.x - jumpStartPos.x) * t,
                    jumpStartPos.y + (jumpTargetPos.y - jumpStartPos.y) * t
                )
                position = currentGround
            }
            return
        }

        // Health regeneration
        timeSinceDamageTaken += dt
        if (timeSinceDamageTaken >= outOfCombatHealDelay && currentHp < maxHp) {
            val healAmount = (maxHp * 0.15f * dt).toInt().coerceAtLeast(1)
            currentHp = (currentHp + healAmount).coerceAtMost(maxHp)
        }

        // Ammo recharge
        if (ammo < type.maxAmmo) {
            ammo = (ammo + dt / type.reloadTime).coerceAtMost(type.maxAmmo.toFloat())
        }

        // Attack cooldown & animation
        if (attackCooldown > 0f) {
            attackCooldown -= dt
        }
        if (firingAnimationTimer > 0f) {
            firingAnimationTimer -= dt
            if (firingAnimationTimer <= 0f) {
                isFiring = false
            }
        }

        // Knockback decay
        if (knockbackVelocity.length() > 5f) {
            position += knockbackVelocity * dt
            knockbackVelocity *= (1f - dt * 8f)
        } else {
            knockbackVelocity = Vector2D.ZERO
        }
    }

    fun startLeap(target: Vector2D, duration: Float = 0.9f) {
        isAirborne = true
        jumpProgress = 0f
        jumpDuration = duration
        jumpStartPos = position
        jumpTargetPos = target
    }

    fun getVisualHeight(): Float {
        if (!isAirborne) return 0f
        // Parabolic arc: sin(progress * PI)
        val arc = sin(jumpProgress * Math.PI.toFloat())
        return arc * jumpPeakHeight
    }

    fun takeDamage(amount: Int, attacker: BrawlerState? = null): Boolean {
        if (!isAlive || isAirborne) return false
        currentHp -= amount
        timeSinceDamageTaken = 0f
        attacker?.let {
            it.damageDealt += amount
        }

        if (currentHp <= 0) {
            currentHp = 0
            isAlive = false
            deaths++
            respawnTimer = 4.5f
            attacker?.let {
                it.kills++
            }
            return true // Died
        }
        return false
    }

    fun heal(amount: Int) {
        if (!isAlive) return
        currentHp = (currentHp + amount).coerceAtMost(maxHp)
    }

    fun chargeSuper(amount: Float) {
        superCharge = (superCharge + amount).coerceAtMost(1f)
    }

    fun consumeSuper() {
        superCharge = 0f
    }

    fun applyKnockback(direction: Vector2D, power: Float) {
        if (isAirborne) return
        knockbackVelocity = direction.normalized() * power
    }

    private fun respawn() {
        isAlive = true
        currentHp = maxHp
        ammo = type.maxAmmo.toFloat()
        superCharge = 0f
        bountyStars = 2
        gemsCarried = 0
        coinsCarried = 0
        position = spawnPosition
        knockbackVelocity = Vector2D.ZERO
        queuedShots.clear()
        isAirborne = false
    }
}

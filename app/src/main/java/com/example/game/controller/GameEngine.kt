package com.example.game.controller

import androidx.compose.ui.graphics.Color
import com.example.game.model.*
import kotlin.math.*
import kotlin.random.Random

class GameEngine(
    val playerBrawlerType: BrawlerType = BrawlerType.BOLT,
    val gameMode: GameMode = GameMode.GEM_GRAB,
    val arenaWidth: Float = 1000f,
    val arenaHeight: Float = 1500f
) {
    val brawlers = mutableListOf<BrawlerState>()
    val bots = mutableListOf<BotAI>()
    val projectiles = mutableListOf<Projectile>()
    val effectAreas = mutableListOf<EffectArea>()
    val gems = mutableListOf<Gem>()
    val obstacles = mutableListOf<Obstacle>()
    val damageNumbers = mutableListOf<DamageNumber>()
    val killFeed = mutableListOf<String>()

    var player: BrawlerState

    // Match & Game Mode State
    var matchTime: Float = 0f
    var isGameOver: Boolean = false
    var winningTeam: Team? = null
    var starPlayer: BrawlerState? = null

    // Gem Grab Mechanics
    var blueGems: Int = 0
    var redGems: Int = 0

    // Cash Grab Mechanics
    var blueCoins: Int = 0
    var redCoins: Int = 0

    // Takedown (Bounty) Mechanics
    var blueStars: Int = 0
    var redStars: Int = 0
    var blueStarHolderTeam: Team? = null // Tiebreaker advantage
    var takedownTimer: Float = gameMode.timeLimitSeconds

    var countdownTeam: Team? = null
    var countdownTimer: Float = 15.0f
    private val countdownDuration: Float = 15.0f

    private var gemSpawner: Obstacle.GemSpawner
    private var damageNumberIdGen = 0L

    init {
        // 1. Initialize Map Obstacles & Center Objective
        val center = Vector2D(arenaWidth / 2f, arenaHeight / 2f)
        gemSpawner = Obstacle.GemSpawner(
            id = "mine_center",
            bounds = Rect2D(center.x - 30f, center.y - 30f, center.x + 30f, center.y + 30f)
        )
        if (gameMode != GameMode.TAKEDOWN) {
            obstacles.add(gemSpawner)
        } else {
            // In Takedown (Bounty), spawn the central tiebreaker Blue Star!
            gems.add(
                Gem(
                    type = CollectibleType.BLUE_STAR,
                    position = center,
                    velocity = Vector2D.ZERO,
                    bounceTimer = 0f
                )
            )
        }
        generateMapLayout()

        // 2. Spawn 3v3 Brawlers
        // Team Blue (Player + 2 Bots)
        val blueTypes = listOf(
            playerBrawlerType,
            getDiverseBrawler(playerBrawlerType, 1),
            getDiverseBrawler(playerBrawlerType, 2)
        )
        val blueSpawns = listOf(
            Vector2D(arenaWidth * 0.5f, arenaHeight * 0.88f),
            Vector2D(arenaWidth * 0.28f, arenaHeight * 0.90f),
            Vector2D(arenaWidth * 0.72f, arenaHeight * 0.90f)
        )

        player = BrawlerState(
            id = "player_1",
            name = "Sen (${playerBrawlerType.characterName})",
            type = playerBrawlerType,
            team = Team.BLUE,
            isUserControlled = true,
            position = blueSpawns[0]
        )
        brawlers.add(player)

        val botNamesBlue = listOf("Mavi Dost 1", "Mavi Dost 2")
        for (i in 1..2) {
            val botBrawler = BrawlerState(
                id = "blue_bot_$i",
                name = "${botNamesBlue[i - 1]} (${blueTypes[i].characterName})",
                type = blueTypes[i],
                team = Team.BLUE,
                isUserControlled = false,
                position = blueSpawns[i]
            )
            brawlers.add(botBrawler)
            bots.add(BotAI(botBrawler))
        }

        // Team Red (3 Bots)
        val redTypes = listOf(
            getDiverseBrawler(playerBrawlerType, 3),
            getDiverseBrawler(playerBrawlerType, 1),
            getDiverseBrawler(playerBrawlerType, 2)
        )
        val redSpawns = listOf(
            Vector2D(arenaWidth * 0.5f, arenaHeight * 0.12f),
            Vector2D(arenaWidth * 0.28f, arenaHeight * 0.10f),
            Vector2D(arenaWidth * 0.72f, arenaHeight * 0.10f)
        )
        val botNamesRed = listOf("Kızıl Kurt", "Gölge Avcı", "Fırtına")
        for (i in 0..2) {
            val botBrawler = BrawlerState(
                id = "red_bot_$i",
                name = "${botNamesRed[i]} (${redTypes[i].characterName})",
                type = redTypes[i],
                team = Team.RED,
                isUserControlled = false,
                position = redSpawns[i]
            )
            brawlers.add(botBrawler)
            bots.add(BotAI(botBrawler))
        }

        // Spawn initial 2 gems near center to kick off action
        gems.add(Gem(position = center + Vector2D(-25f, 0f)))
        gems.add(Gem(position = center + Vector2D(25f, 0f)))
    }

    private fun getDiverseBrawler(excluded: BrawlerType, offset: Int): BrawlerType {
        val all = BrawlerType.values().filter { it != excluded }
        return all[offset % all.size]
    }

    private fun generateMapLayout() {
        val cx = arenaWidth / 2f
        val cy = arenaHeight / 2f

        // Destructible stone/crate cover blocks
        // Center protective walls around gem mine
        obstacles.add(Obstacle.Wall("w_c1", Rect2D(cx - 160f, cy - 35f, cx - 110f, cy + 35f)))
        obstacles.add(Obstacle.Wall("w_c2", Rect2D(cx + 110f, cy - 35f, cx + 160f, cy + 35f)))

        // Mid-lane barriers
        obstacles.add(Obstacle.Wall("w_t1", Rect2D(cx - 240f, cy - 220f, cx - 140f, cy - 180f)))
        obstacles.add(Obstacle.Wall("w_t2", Rect2D(cx + 140f, cy - 220f, cx + 240f, cy - 180f)))
        obstacles.add(Obstacle.Wall("w_b1", Rect2D(cx - 240f, cy + 180f, cx - 140f, cy + 220f)))
        obstacles.add(Obstacle.Wall("w_b2", Rect2D(cx + 140f, cy + 180f, cx + 240f, cy + 220f)))

        // Side lane chokepoints
        obstacles.add(Obstacle.Wall("w_s1", Rect2D(60f, cy - 80f, 130f, cy + 80f)))
        obstacles.add(Obstacle.Wall("w_s2", Rect2D(arenaWidth - 130f, cy - 80f, arenaWidth - 60f, cy + 80f)))

        // Bushes for stealth tactics
        obstacles.add(Obstacle.Bush("bush_c1", Rect2D(cx - 80f, cy - 180f, cx + 80f, cy - 120f)))
        obstacles.add(Obstacle.Bush("bush_c2", Rect2D(cx - 80f, cy + 120f, cx + 80f, cy + 180f)))
        obstacles.add(Obstacle.Bush("bush_l1", Rect2D(50f, cy - 260f, 160f, cy - 120f)))
        obstacles.add(Obstacle.Bush("bush_l2", Rect2D(50f, cy + 120f, 160f, cy + 260f)))
        obstacles.add(Obstacle.Bush("bush_r1", Rect2D(arenaWidth - 160f, cy - 260f, arenaWidth - 50f, cy - 120f)))
        obstacles.add(Obstacle.Bush("bush_r2", Rect2D(arenaWidth - 160f, cy + 120f, arenaWidth - 50f, cy + 260f)))
    }

    fun update(dt: Float) {
        if (isGameOver) return
        matchTime += dt

        val center = Vector2D(arenaWidth / 2f, arenaHeight / 2f)

        // 1. Spawner Logic (Gems or Coins)
        if (gameMode != GameMode.TAKEDOWN) {
            gemSpawner.spawnTimer += dt
            if (gemSpawner.spawnTimer >= gemSpawner.spawnInterval && gems.size < 25) {
                gemSpawner.spawnTimer = 0f
                val angle = Random.nextFloat() * 6.28f
                val dist = Random.nextFloat() * 35f
                val spawnPos = center + Vector2D.fromAngle(angle, dist)
                val burstVel = Vector2D.fromAngle(angle, 40f + Random.nextFloat() * 30f)
                val type = if (gameMode == GameMode.CASH_GRAB) {
                    if (Random.nextFloat() < 0.25f) CollectibleType.COIN_SACK else CollectibleType.GOLD_COIN
                } else {
                    CollectibleType.GEM
                }
                gems.add(Gem(type = type, position = spawnPos, velocity = burstVel, bounceTimer = 0.4f))
            }
        }

        // 2. Update Gems & Collectibles
        gems.forEach { it.update(dt) }

        // 3. Update Brawlers
        for (brawler in brawlers) {
            brawler.update(dt)

            // Update Queued Shots (Bolt burst, Primo punches)
            if (brawler.queuedShots.isNotEmpty()) {
                val iterator = brawler.queuedShots.iterator()
                while (iterator.hasNext()) {
                    val shot = iterator.next()
                    shot.delay -= dt
                    if (shot.delay <= 0f) {
                        spawnQueuedShot(brawler, shot)
                        iterator.remove()
                    }
                }
            }

            if (brawler.isAlive && !brawler.isAirborne) {
                // Apply movement and obstacle collision
                val moveStep = brawler.velocity * dt
                val nextPos = brawler.position + moveStep

                // Arena boundaries
                val clampedX = nextPos.x.coerceIn(brawler.radius, arenaWidth - brawler.radius)
                val clampedY = nextPos.y.coerceIn(brawler.radius, arenaHeight - brawler.radius)
                var resolvedPos = Vector2D(clampedX, clampedY)

                // Wall collisions
                for (obs in obstacles) {
                    if (obs is Obstacle.Wall) {
                        if (obs.bounds.intersectsCircle(resolvedPos, brawler.radius)) {
                            // Simple push back along closest edge
                            val cx1 = resolvedPos.x.coerceIn(obs.bounds.left, obs.bounds.right)
                            val cy1 = resolvedPos.y.coerceIn(obs.bounds.top, obs.bounds.bottom)
                            val diff = resolvedPos - Vector2D(cx1, cy1)
                            val dist = diff.length()
                            if (dist < brawler.radius) {
                                val pushDir = if (dist > 0.001f) diff.normalized() else Vector2D(0f, 1f)
                                resolvedPos = Vector2D(cx1, cy1) + pushDir * brawler.radius
                            }
                        }
                    }
                }
                brawler.position = resolvedPos

                // Check bushes (stealth)
                var inBush = false
                for (obs in obstacles) {
                    if (obs is Obstacle.Bush && obs.bounds.contains(brawler.position)) {
                        inBush = true
                        break
                    }
                }
                brawler.isInsideBush = inBush

                // Collectible Collection
                val gemIter = gems.iterator()
                while (gemIter.hasNext()) {
                    val gem = gemIter.next()
                    if (gem.bounceTimer <= 0f && gem.position.distanceTo(brawler.position) < (brawler.radius + gem.pickupRadius)) {
                        when (gem.type) {
                            CollectibleType.GEM -> {
                                brawler.gemsCarried += gem.value
                                addDamageNumber(brawler.position, "+${gem.value} 💎", Color(0xFF00E676))
                            }
                            CollectibleType.GOLD_COIN -> {
                                brawler.coinsCarried += gem.value
                                addDamageNumber(brawler.position, "+${gem.value} 🪙", Color(0xFFFFD700))
                            }
                            CollectibleType.COIN_SACK -> {
                                brawler.coinsCarried += gem.value
                                addDamageNumber(brawler.position, "+${gem.value} 💰", Color(0xFFFFAB00))
                            }
                            CollectibleType.BLUE_STAR -> {
                                blueStarHolderTeam = brawler.team
                                addDamageNumber(brawler.position, "MAVİ YILDIZ! ⭐", Color(0xFF40C4FF))
                                val teamColor = if (brawler.team == Team.BLUE) "Mavi" else "Kırmızı"
                                killFeed.add(0, "${brawler.name} ($teamColor) Mavi Yıldızı kaptı!")
                            }
                        }
                        gemIter.remove()
                        break
                    }
                }
            }
        }

        // 4. Update Bot AIs
        for (bot in bots) {
            val teamCountdown = (countdownTeam == bot.brawler.team)
            bot.update(dt, brawlers, gems, center, teamCountdown, gameMode) { dir, isSuper ->
                fireAttack(bot.brawler, dir, isSuper)
            }
        }

        // 5. Update Projectiles & Collisions
        val newProjectiles = mutableListOf<Projectile>()
        val wallsToDestroy = mutableListOf<Obstacle.Wall>()

        val projIter = projectiles.iterator()
        while (projIter.hasNext()) {
            val p = projIter.next()
            p.update(dt)

            if (p.isDead) {
                // Check if Spica cactus bomb reached destination / max range -> burst!
                if (p.type == ProjectileType.SPICA_BOMB) {
                    burstSpicaBomb(p, newProjectiles)
                }
                projIter.remove()
                continue
            }

            // Wall Collisions
            var hitWall = false
            for (obs in obstacles) {
                if (obs is Obstacle.Wall && obs.bounds.intersectsCircle(p.position, p.radius)) {
                    if (p.destroysWalls) {
                        wallsToDestroy.add(obs)
                        addDamageNumber(obs.bounds.centerX to obs.bounds.centerY, "PARÇALANDI!", Color(0xFFFFB300))
                        break
                    } else if (!p.penetratesWalls) {
                        hitWall = true
                        break
                    }
                }
            }

            if (hitWall) {
                if (p.type == ProjectileType.SPICA_BOMB) {
                    burstSpicaBomb(p, newProjectiles)
                }
                p.isDead = true
                projIter.remove()
                continue
            }

            // Brawler Collisions
            for (target in brawlers) {
                if (!target.isAlive || target.isAirborne || target.team == p.ownerTeam) continue
                if (p.hitBrawlerIds.contains(target.id)) continue

                if (target.position.distanceTo(p.position) <= (target.radius + p.radius)) {
                    p.hitBrawlerIds.add(target.id)

                    // Apply damage
                    val owner = brawlers.find { it.id == p.ownerId }
                    val died = target.takeDamage(p.damage, owner)
                    addDamageNumber(target.position, "-${p.damage}", if (target.team == Team.BLUE) Color.Red else Color.White)

                    // Super charge to attacker
                    owner?.chargeSuper(p.superChargeAmount)

                    // Apply knockback
                    if (p.knockbackPower > 0f) {
                        val knockDir = (target.position - p.position).normalized()
                        target.applyKnockback(knockDir, p.knockbackPower)
                    }

                    if (died) {
                        onBrawlerDied(target, owner)
                    }

                    // If not penetrating, remove projectile
                    if (!p.penetratesEnemies) {
                        if (p.type == ProjectileType.SPICA_BOMB) {
                            burstSpicaBomb(p, newProjectiles)
                        }
                        p.isDead = true
                        projIter.remove()
                        break
                    }
                }
            }
        }

        if (wallsToDestroy.isNotEmpty()) {
            obstacles.removeAll(wallsToDestroy)
        }
        if (newProjectiles.isNotEmpty()) {
            projectiles.addAll(newProjectiles)
        }

        // 6. Update Effect Areas (Spica Thorn Patch, etc.)
        val effectIter = effectAreas.iterator()
        while (effectIter.hasNext()) {
            val eff = effectIter.next()
            val alive = eff.update(dt)

            // Tick damage/slow every 0.4s
            if (eff.tickTimer >= 0.4f && eff.damagePerSecond > 0) {
                eff.tickTimer = 0f
                val tickDamage = (eff.damagePerSecond * 0.4f).toInt()

                for (target in brawlers) {
                    if (target.isAlive && !target.isAirborne && target.team != eff.ownerTeam) {
                        if (target.position.distanceTo(eff.center) <= eff.radius) {
                            val owner = brawlers.find { it.id == eff.ownerId }
                            val died = target.takeDamage(tickDamage, owner)
                            addDamageNumber(target.position, "-$tickDamage", Color(0xFFFF80AB))
                            if (died) {
                                onBrawlerDied(target, owner)
                            }
                        }
                    }
                }
            }

            if (!alive) {
                effectIter.remove()
            }
        }

        // 7. Update Damage Numbers
        val numIter = damageNumbers.iterator()
        while (numIter.hasNext()) {
            val num = numIter.next()
            if (!num.update(dt)) {
                numIter.remove()
            }
        }

        // 8. Scoring & Countdown Loop
        when (gameMode) {
            GameMode.GEM_GRAB -> calculateGemCounts()
            GameMode.CASH_GRAB -> calculateCoinCounts()
            GameMode.TAKEDOWN -> Unit
        }
        updateCountdown(dt)
    }

    private fun burstSpicaBomb(bomb: Projectile, targetList: MutableList<Projectile>) {
        // Spawn 6 radial sub-needles at 60 degree intervals
        for (i in 0 until 6) {
            val angle = (i * 60f) * (Math.PI.toFloat() / 180f)
            val dir = Vector2D.fromAngle(angle)
            val needle = Projectile(
                type = ProjectileType.SPICA_NEEDLE,
                ownerId = bomb.ownerId,
                ownerTeam = bomb.ownerTeam,
                position = bomb.position,
                velocity = dir * 650f,
                damage = 360,
                maxRange = 240f,
                radius = 10f,
                superChargeAmount = 0.08f,
                color = Color(0xFF76FF03)
            )
            targetList.add(needle)
        }
    }

    private fun spawnQueuedShot(brawler: BrawlerState, shot: QueuedShot) {
        if (!brawler.isAlive) return
        val pos = brawler.position + shot.direction * (brawler.radius + 6f)

        when (brawler.type) {
            BrawlerType.BOLT -> {
                if (shot.isSuper) {
                    val p = Projectile(
                        type = ProjectileType.BOLT_SUPER_BULLET,
                        ownerId = brawler.id,
                        ownerTeam = brawler.team,
                        position = pos,
                        velocity = shot.direction * 980f,
                        damage = 440,
                        maxRange = 640f,
                        radius = 16f,
                        penetratesWalls = true,
                        penetratesEnemies = true,
                        destroysWalls = true,
                        superChargeAmount = 0.05f,
                        color = Color(0xFFFFD600)
                    )
                    projectiles.add(p)
                } else {
                    val p = Projectile(
                        type = ProjectileType.BOLT_BULLET,
                        ownerId = brawler.id,
                        ownerTeam = brawler.team,
                        position = pos,
                        velocity = shot.direction * 860f,
                        damage = 380,
                        maxRange = 520f,
                        radius = 12f,
                        superChargeAmount = 0.08f,
                        color = Color(0xFF00E5FF)
                    )
                    projectiles.add(p)
                }
            }
            BrawlerType.EL_GRANDE -> {
                // Short-range punch shockwave
                val p = Projectile(
                    type = ProjectileType.EL_GRANDE_PUNCH,
                    ownerId = brawler.id,
                    ownerTeam = brawler.team,
                    position = pos,
                    velocity = shot.direction * 650f,
                    damage = 420,
                    maxRange = 160f,
                    radius = 28f,
                    penetratesEnemies = true,
                    superChargeAmount = 0.12f,
                    color = Color(0xFFFFAB00)
                )
                projectiles.add(p)
            }
            else -> Unit
        }
    }

    fun fireAttack(brawler: BrawlerState, direction: Vector2D, isSuper: Boolean) {
        if (!brawler.isAlive || brawler.isAirborne) return
        val dir = direction.normalized()
        if (dir.length() < 0.1f) return

        brawler.facingAngle = dir.angle()

        if (isSuper) {
            if (!brawler.isSuperReady()) return
            brawler.consumeSuper()
            brawler.isFiring = true
            brawler.firingAnimationTimer = 0.4f
            addDamageNumber(brawler.position, "SÜPER!", Color(0xFFFFEA00))

            when (brawler.type) {
                BrawlerType.BOLT -> {
                    // Mermi Yağmuru: 12 piercing bullets in a burst
                    for (i in 0 until 12) {
                        val spread = (Random.nextFloat() - 0.5f) * 0.18f
                        val angle = dir.angle() + spread
                        val bulletDir = Vector2D.fromAngle(angle)
                        brawler.queuedShots.add(QueuedShot(delay = i * 0.05f, isSuper = true, direction = bulletDir))
                    }
                }
                BrawlerType.SHELIA -> {
                    // Süper Saçma: 9 mega pellets, destroys walls, big knockback
                    for (i in 0 until 9) {
                        val spread = (i - 4) * 0.08f
                        val angle = dir.angle() + spread
                        val pelletDir = Vector2D.fromAngle(angle)
                        val p = Projectile(
                            type = ProjectileType.SHELIA_SUPER_PELLET,
                            ownerId = brawler.id,
                            ownerTeam = brawler.team,
                            position = brawler.position + pelletDir * (brawler.radius + 6f),
                            velocity = pelletDir * 920f,
                            damage = 480,
                            maxRange = 440f,
                            radius = 18f,
                            penetratesWalls = true,
                            penetratesEnemies = true,
                            destroysWalls = true,
                            knockbackPower = 220f,
                            superChargeAmount = 0.06f,
                            color = Color(0xFFFF1744)
                        )
                        projectiles.add(p)
                    }
                }
                BrawlerType.EL_GRANDE -> {
                    // Meteor Sıçraması: Leap into air!
                    val leapTarget = (brawler.position + dir * 380f).let {
                        Vector2D(
                            it.x.coerceIn(50f, arenaWidth - 50f),
                            it.y.coerceIn(50f, arenaHeight - 50f)
                        )
                    }
                    brawler.startLeap(leapTarget, duration = 0.9f)

                    // Schedule landing explosion upon completion
                    // We check jumpProgress in game loop or handle on landing:
                    scheduleLeapImpact(brawler, leapTarget, 0.9f)
                }
                BrawlerType.SPICA -> {
                    // Dikenli Alan: Throws thorn bomb creating a hazard field
                    val targetPos = brawler.position + dir * 420f
                    val eff = EffectArea(
                        id = "thorn_patch_${System.currentTimeMillis()}",
                        ownerId = brawler.id,
                        ownerTeam = brawler.team,
                        center = targetPos,
                        radius = 150f,
                        duration = 5.0f,
                        maxDuration = 5.0f,
                        damagePerSecond = 500,
                        slowFactor = 0.5f,
                        color = Color(0x994CAF50),
                        isThornPatch = true
                    )
                    effectAreas.add(eff)
                }
            }
        } else {
            // Normal Attack
            if (!brawler.canAttack()) return
            brawler.ammo -= 1.0f
            brawler.attackCooldown = 0.35f
            brawler.isFiring = true
            brawler.firingAnimationTimer = 0.25f

            when (brawler.type) {
                BrawlerType.BOLT -> {
                    // Altıpatlar: 6 rapid burst bullets
                    for (i in 0 until 6) {
                        brawler.queuedShots.add(QueuedShot(delay = i * 0.065f, isSuper = false, direction = dir))
                    }
                }
                BrawlerType.SHELIA -> {
                    // Shotgun spread: 5 pellets in a cone
                    for (i in 0 until 5) {
                        val spread = (i - 2) * 0.12f
                        val angle = dir.angle() + spread
                        val pelletDir = Vector2D.fromAngle(angle)
                        val p = Projectile(
                            type = ProjectileType.SHELIA_PELLET,
                            ownerId = brawler.id,
                            ownerTeam = brawler.team,
                            position = brawler.position + pelletDir * (brawler.radius + 6f),
                            velocity = pelletDir * 800f,
                            damage = 360,
                            maxRange = 380f,
                            radius = 12f,
                            superChargeAmount = 0.08f,
                            color = Color(0xFFFF7043)
                        )
                        projectiles.add(p)
                    }
                }
                BrawlerType.EL_GRANDE -> {
                    // 4 fast punches
                    for (i in 0 until 4) {
                        brawler.queuedShots.add(QueuedShot(delay = i * 0.07f, isSuper = false, direction = dir))
                    }
                }
                BrawlerType.SPICA -> {
                    // Cactus grenade
                    val p = Projectile(
                        type = ProjectileType.SPICA_BOMB,
                        ownerId = brawler.id,
                        ownerTeam = brawler.team,
                        position = brawler.position + dir * (brawler.radius + 6f),
                        velocity = dir * 480f,
                        damage = 720,
                        maxRange = 440f,
                        radius = 20f,
                        superChargeAmount = 0.16f,
                        color = Color(0xFF64DD17)
                    )
                    projectiles.add(p)
                }
            }
        }
    }

    private fun scheduleLeapImpact(brawler: BrawlerState, targetPos: Vector2D, delay: Float) {
        // Impact occurs when leap finishes
        brawler.queuedShots.add(QueuedShot(delay = delay, isSuper = true, direction = Vector2D(0f, 1f)))
    }

    private fun onBrawlerDied(victim: BrawlerState, killer: BrawlerState?) {
        val killerName = killer?.name ?: "Bilinmeyen"

        when (gameMode) {
            GameMode.TAKEDOWN -> {
                val starsWon = victim.bountyStars
                if (killer != null) {
                    if (killer.team == Team.BLUE) {
                        blueStars += starsWon
                    } else {
                        redStars += starsWon
                    }
                    // Increase killer's personal bounty (max 7 stars)
                    killer.bountyStars = (killer.bountyStars + 1).coerceAtMost(7)

                    // Transfer tiebreaker star if victim held it
                    if (blueStarHolderTeam != null && blueStarHolderTeam != killer.team) {
                        blueStarHolderTeam = killer.team
                    }
                }
                val feedText = "$killerName ⚔️ ${victim.name} (+$starsWon ⭐)"
                killFeed.add(0, feedText)
                if (killFeed.size > 5) killFeed.removeLast()

                addDamageNumber(victim.position, "+$starsWon ⭐", Color(0xFFFFD54F))

                // Sudden death target score victory check
                if (blueStars >= gameMode.targetObjectiveScore) {
                    endMatch(Team.BLUE)
                } else if (redStars >= gameMode.targetObjectiveScore) {
                    endMatch(Team.RED)
                }
            }

            GameMode.CASH_GRAB -> {
                val feedText = "$killerName ⚔️ ${victim.name}"
                killFeed.add(0, feedText)
                if (killFeed.size > 5) killFeed.removeLast()

                // Drop all coins carried on the ground
                var dropCount = victim.coinsCarried
                victim.coinsCarried = 0

                while (dropCount > 0) {
                    val angle = Random.nextFloat() * 6.28f
                    val dist = Random.nextFloat() * 50f
                    val burstVel = Vector2D.fromAngle(angle, 60f + Random.nextFloat() * 40f)
                    val isSack = dropCount >= 3 && Random.nextBoolean()
                    val type = if (isSack) CollectibleType.COIN_SACK else CollectibleType.GOLD_COIN
                    val value = if (isSack) 3 else 1
                    dropCount -= value
                    gems.add(
                        Gem(
                            type = type,
                            position = victim.position + Vector2D.fromAngle(angle, dist),
                            velocity = burstVel,
                            bounceTimer = 0.5f
                        )
                    )
                }
            }

            GameMode.GEM_GRAB -> {
                val feedText = "$killerName ⚔️ ${victim.name}"
                killFeed.add(0, feedText)
                if (killFeed.size > 5) killFeed.removeLast()

                // Drop all gems on the ground!
                val dropCount = victim.gemsCarried
                victim.gemsCarried = 0

                for (i in 0 until dropCount) {
                    val angle = Random.nextFloat() * 6.28f
                    val dist = Random.nextFloat() * 50f
                    val burstVel = Vector2D.fromAngle(angle, 60f + Random.nextFloat() * 40f)
                    gems.add(
                        Gem(
                            type = CollectibleType.GEM,
                            position = victim.position + Vector2D.fromAngle(angle, dist),
                            velocity = burstVel,
                            bounceTimer = 0.5f
                        )
                    )
                }
            }
        }
    }

    private fun calculateGemCounts() {
        blueGems = brawlers.filter { it.team == Team.BLUE }.sumOf { it.gemsCarried }
        redGems = brawlers.filter { it.team == Team.RED }.sumOf { it.gemsCarried }
    }

    private fun calculateCoinCounts() {
        blueCoins = brawlers.filter { it.team == Team.BLUE }.sumOf { it.coinsCarried }
        redCoins = brawlers.filter { it.team == Team.RED }.sumOf { it.coinsCarried }
    }

    private fun updateCountdown(dt: Float) {
        when (gameMode) {
            GameMode.GEM_GRAB -> {
                val leadingTeam = when {
                    blueGems >= 10 && blueGems > redGems -> Team.BLUE
                    redGems >= 10 && redGems > blueGems -> Team.RED
                    else -> null
                }
                processCountdownTimer(leadingTeam, dt)
            }

            GameMode.CASH_GRAB -> {
                val leadingTeam = when {
                    blueCoins >= 15 && blueCoins > redCoins -> Team.BLUE
                    redCoins >= 15 && redCoins > blueCoins -> Team.RED
                    else -> null
                }
                processCountdownTimer(leadingTeam, dt)
            }

            GameMode.TAKEDOWN -> {
                takedownTimer -= dt
                if (takedownTimer <= 0f) {
                    val winner = when {
                        blueStars > redStars -> Team.BLUE
                        redStars > blueStars -> Team.RED
                        else -> blueStarHolderTeam ?: Team.BLUE
                    }
                    endMatch(winner)
                }
            }
        }
    }

    private fun processCountdownTimer(leadingTeam: Team?, dt: Float) {
        if (leadingTeam != null) {
            if (countdownTeam == leadingTeam) {
                countdownTimer -= dt
                if (countdownTimer <= 0f) {
                    endMatch(leadingTeam)
                }
            } else {
                countdownTeam = leadingTeam
                countdownTimer = countdownDuration
            }
        } else {
            countdownTeam = null
            countdownTimer = countdownDuration
        }
    }

    private fun endMatch(winner: Team) {
        isGameOver = true
        winningTeam = winner

        // Compute Star Player (weighted specifically for the game mode)
        starPlayer = brawlers.maxByOrNull {
            when (gameMode) {
                GameMode.TAKEDOWN -> it.kills * 400 + it.bountyStars * 150 + it.damageDealt / 15
                GameMode.CASH_GRAB -> it.kills * 200 + it.coinsCarried * 350 + it.damageDealt / 20
                GameMode.GEM_GRAB -> it.kills * 250 + it.gemsCarried * 300 + it.damageDealt / 20
            }
        } ?: brawlers.first()
    }

    private fun addDamageNumber(pos: Vector2D, text: String, color: Color) {
        damageNumbers.add(
            DamageNumber(
                id = damageNumberIdGen++,
                position = pos + Vector2D(Random.nextFloat() * 20f - 10f, -15f),
                text = text,
                color = color
            )
        )
    }

    private fun addDamageNumber(pos: Pair<Float, Float>, text: String, color: Color) {
        addDamageNumber(Vector2D(pos.first, pos.second), text, color)
    }
}

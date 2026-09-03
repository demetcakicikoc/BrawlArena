package com.example.game.model

import java.util.UUID

enum class CollectibleType {
    GEM,
    GOLD_COIN,
    COIN_SACK,
    BLUE_STAR
}

data class Gem(
    val id: String = UUID.randomUUID().toString(),
    val type: CollectibleType = CollectibleType.GEM,
    val value: Int = when (type) {
        CollectibleType.COIN_SACK -> 3
        else -> 1
    },
    var position: Vector2D,
    var velocity: Vector2D = Vector2D.ZERO,
    var bounceTimer: Float = 0f,
    val pickupRadius: Float = 36f,
    var age: Float = 0f
) {
    fun update(dt: Float) {
        age += dt
        if (bounceTimer > 0f) {
            position += velocity * dt
            velocity *= (1f - dt * 4f)
            bounceTimer -= dt
        }
    }
}

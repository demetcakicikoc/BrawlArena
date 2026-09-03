package com.example.game.model

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class Vector2D(val x: Float = 0f, val y: Float = 0f) {
    operator fun plus(other: Vector2D) = Vector2D(x + other.x, y + other.y)
    operator fun minus(other: Vector2D) = Vector2D(x - other.x, y - other.y)
    operator fun times(scalar: Float) = Vector2D(x * scalar, y * scalar)
    operator fun div(scalar: Float): Vector2D {
        return if (scalar != 0f) Vector2D(x / scalar, y / scalar) else Vector2D()
    }

    fun length(): Float = sqrt(x * x + y * y)

    fun distanceTo(other: Vector2D): Float {
        val dx = x - other.x
        val dy = y - other.y
        return sqrt(dx * dx + dy * dy)
    }

    fun normalized(): Vector2D {
        val len = length()
        return if (len > 0.0001f) Vector2D(x / len, y / len) else Vector2D(0f, 0f)
    }

    fun angle(): Float {
        return atan2(y, x)
    }

    companion object {
        val ZERO = Vector2D(0f, 0f)

        fun fromAngle(angleRad: Float, length: Float = 1f): Vector2D {
            return Vector2D(cos(angleRad) * length, sin(angleRad) * length)
        }
    }
}

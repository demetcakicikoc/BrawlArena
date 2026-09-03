package com.example

import com.example.game.controller.GameEngine
import com.example.game.model.BrawlerType
import com.example.game.model.GameMode
import com.example.game.model.Vector2D
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class GameBattleTest {

    @Test
    fun testAllModesBattleStartAndUpdate() {
        for (mode in GameMode.values()) {
            for (brawler in BrawlerType.values()) {
                val engine = GameEngine(playerBrawlerType = brawler, gameMode = mode)
                assertNotNull(engine.player)
                assertTrue(engine.brawlers.isNotEmpty())

                // Simulate 60 frames (1 second) of game loop
                for (frame in 0 until 60) {
                    engine.update(0.016f)
                }

                // Simulate player attack & super
                engine.fireAttack(engine.player, Vector2D(1f, 0f), isSuper = false)
                engine.player.superCharge = 1f
                engine.fireAttack(engine.player, Vector2D(1f, 0f), isSuper = true)

                for (frame in 0 until 60) {
                    engine.update(0.016f)
                }
            }
        }
    }
}

package com.example.game

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import com.example.game.controller.GameEngine
import com.example.game.model.BrawlerType
import com.example.game.model.GameMode
import com.example.game.model.Team
import com.example.game.model.Vector2D
import com.example.game.ui.ArenaCanvas
import com.example.game.ui.GameHUD
import com.example.game.ui.GameOverScreen
import com.example.game.ui.MainMenuScreen
import kotlinx.coroutines.isActive

enum class GameScreen {
    MENU,
    PLAYING,
    PAUSED,
    GAME_OVER
}

@Composable
fun BrawlGameApp() {
    var currentScreen by remember { mutableStateOf(GameScreen.MENU) }
    var selectedBrawler by remember { mutableStateOf(BrawlerType.BOLT) }
    var selectedGameMode by remember { mutableStateOf(GameMode.GEM_GRAB) }
    var trophies by remember { mutableIntStateOf(1240) }

    var gameEngine by remember { mutableStateOf<GameEngine?>(null) }
    var currentAimDirection by remember { mutableStateOf<Vector2D?>(null) }
    var isAimingSuper by remember { mutableStateOf(false) }

    // Start battle helper
    fun startNewGame() {
        gameEngine = GameEngine(
            playerBrawlerType = selectedBrawler,
            gameMode = selectedGameMode
        )
        currentAimDirection = null
        isAimingSuper = false
        currentScreen = GameScreen.PLAYING
    }

    when (currentScreen) {
        GameScreen.MENU -> {
            MainMenuScreen(
                selectedBrawler = selectedBrawler,
                selectedGameMode = selectedGameMode,
                trophies = trophies,
                onSelectBrawler = { selectedBrawler = it },
                onSelectGameMode = { selectedGameMode = it },
                onStartBattle = { startNewGame() }
            )
        }

        GameScreen.PLAYING, GameScreen.PAUSED -> {
            val engine = gameEngine ?: return

            // 60 FPS Game Loop
            LaunchedEffect(currentScreen) {
                var lastTime = System.nanoTime()
                while (isActive && currentScreen == GameScreen.PLAYING) {
                    withFrameNanos { now ->
                        val dt = ((now - lastTime) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
                        lastTime = now

                        engine.update(dt)

                        if (engine.isGameOver) {
                            if (engine.winningTeam == Team.BLUE) {
                                trophies += 8
                            } else {
                                trophies = (trophies - 2).coerceAtLeast(0)
                            }
                            currentScreen = GameScreen.GAME_OVER
                        }
                    }
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                // Arena Game Canvas
                ArenaCanvas(
                    engine = engine,
                    aimDirection = currentAimDirection,
                    isAimingSuper = isAimingSuper
                )

                // Virtual Controls & HUD
                GameHUD(
                    engine = engine,
                    onMove = { dir ->
                        engine.player.velocity = dir * engine.player.type.moveSpeed
                        if (dir.length() > 0.1f) {
                            engine.player.facingAngle = dir.angle()
                        }
                    },
                    onAimChange = { dir, isSuper ->
                        currentAimDirection = dir
                        isAimingSuper = isSuper
                    },
                    onAttackRelease = { dir, isSuper ->
                        currentAimDirection = null
                        var finalDir = dir
                        // Auto-aim fallback if user simply tapped without dragging
                        if (finalDir.length() < 0.15f) {
                            val nearestEnemy = engine.brawlers
                                .filter { it.isAlive && it.team == Team.RED && !it.isAirborne }
                                .minByOrNull { it.position.distanceTo(engine.player.position) }
                            if (nearestEnemy != null) {
                                finalDir = (nearestEnemy.position - engine.player.position).normalized()
                            } else {
                                finalDir = Vector2D.fromAngle(engine.player.facingAngle)
                            }
                        }
                        engine.fireAttack(engine.player, finalDir, isSuper)
                    },
                    onPauseClick = {
                        currentScreen = GameScreen.PAUSED
                    }
                )

                // Pause Dialog Overlay
                if (currentScreen == GameScreen.PAUSED) {
                    PauseDialog(
                        onResume = { currentScreen = GameScreen.PLAYING },
                        onSurrender = {
                            currentScreen = GameScreen.MENU
                        }
                    )
                }
            }
        }

        GameScreen.GAME_OVER -> {
            val engine = gameEngine
            if (engine != null) {
                GameOverScreen(
                    engine = engine,
                    onPlayAgain = { startNewGame() },
                    onBackToMenu = { currentScreen = GameScreen.MENU }
                )
            }
        }
    }
}

@Composable
private fun PauseDialog(onResume: () -> Unit, onSurrender: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x99000000)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1F0D3D)),
            modifier = Modifier.padding(32.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "OYUN DURAKLATILDI",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )

                Button(
                    onClick = onResume,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("DEVAM ET", fontWeight = FontWeight.Black, color = Color.Black)
                }

                OutlinedButton(
                    onClick = onSurrender,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("ANA MENÜYE DÖN", fontWeight = FontWeight.Bold, color = Color(0xFFFF5252))
                }
            }
        }
    }
}

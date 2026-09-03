package com.example.game.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.controller.BrawlerState
import com.example.game.controller.GameEngine
import com.example.game.model.Team

@Composable
fun GameOverScreen(
    engine: GameEngine,
    onPlayAgain: () -> Unit,
    onBackToMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isVictory = engine.winningTeam == Team.BLUE

    val bannerGradient = if (isVictory) {
        listOf(Color(0xFF00E676), Color(0xFF00B0FF), Color(0xFF2979FF))
    } else {
        listOf(Color(0xFFFF1744), Color(0xFFD50000), Color(0xFF880E4F))
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    listOf(
                        Color(0xFF16092E),
                        Color(0xFF240E4A),
                        Color(0xFF0E1A40),
                        Color(0xFF00081C)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Big Victory / Defeat Header
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .background(
                        brush = Brush.horizontalGradient(bannerGradient),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .border(3.dp, Color(0xFFFFEB3B), RoundedCornerShape(20.dp))
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (isVictory) "ZAFER!" else "BOZGUT!",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = if (isVictory) "+8 🏆 KUPA KAZANDIN" else "-2 🏆 KUPA KAYBETTİN",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFFFEB3B)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Star Player Highlight Card
            engine.starPlayer?.let { star ->
                StarPlayerCard(star = star)
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Full Match Scoreboard
            MatchScoreboard(engine = engine)

            Spacer(modifier = Modifier.height(20.dp))

            // Bottom Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Main Menu Button
                Button(
                    onClick = onBackToMenu,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4527A0)),
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp)
                        .border(2.dp, Color(0xFF7E57C2), RoundedCornerShape(16.dp))
                        .testTag("game_over_menu_button")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Home, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("ANA MENÜ", fontWeight = FontWeight.Black, fontSize = 14.sp)
                    }
                }

                // Play Again Button
                Button(
                    onClick = onPlayAgain,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier
                        .weight(1.2f)
                        .height(54.dp)
                        .shadow(8.dp, RoundedCornerShape(16.dp))
                        .testTag("game_over_play_again_button")
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.verticalGradient(
                                    listOf(Color(0xFFFFEA00), Color(0xFFFF8F00))
                                ),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .border(2.dp, Color.White, RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "TEKRAR OYNA",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
private fun StarPlayerCard(star: BrawlerState) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xDD281650)),
        modifier = Modifier
            .fillMaxWidth()
            .border(2.5.dp, Color(0xFFFFD700), RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BrawlerAvatarIcon(
                brawler = star.type,
                size = 54.dp,
                isSelected = true
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "🌟 YILDIZ OYUNCU",
                    color = Color(0xFFFFD700),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = star.name,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "${star.kills} Leş • ${star.gemsCarried} Elmas • ${star.damageDealt} Hasar",
                    color = Color(0xFFB0BEC5),
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun MatchScoreboard(engine: GameEngine) {
    val brawlers = engine.brawlers
    val mode = engine.gameMode
    val objectiveHeader = when (mode) {
        com.example.game.model.GameMode.GEM_GRAB -> "Elmas"
        com.example.game.model.GameMode.CASH_GRAB -> "Altın"
        com.example.game.model.GameMode.TAKEDOWN -> "Yıldız"
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0x99170C35)),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, mode.accentColor.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "MAÇ İSTATİSTİKLERİ",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp
                )
                Text(
                    text = "${mode.iconEmoji} ${mode.displayName}",
                    color = mode.accentColor,
                    fontWeight = FontWeight.Black,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Header labels
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Savaşçı", color = Color(0xFFB0BEC5), fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(2.2f))
                Text("Leş", color = Color(0xFFB0BEC5), fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.9f), textAlign = TextAlign.Center)
                Text("Ölüm", color = Color(0xFFB0BEC5), fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.9f), textAlign = TextAlign.Center)
                Text(objectiveHeader, color = Color(0xFFB0BEC5), fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
            }

            HorizontalDivider(color = Color(0x33FFFFFF))

            // Player rows
            brawlers.forEach { b ->
                val rowColor = if (b.team == Team.BLUE) Color(0xFF90CAF9) else Color(0xFFEF9A9A)
                val objStat = when (mode) {
                    com.example.game.model.GameMode.GEM_GRAB -> "💎 ${b.gemsCarried}"
                    com.example.game.model.GameMode.CASH_GRAB -> "🪙 ${b.coinsCarried}"
                    com.example.game.model.GameMode.TAKEDOWN -> "⭐ ${b.bountyStars}"
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(2.2f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BrawlerAvatarIcon(brawler = b.type, size = 22.dp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = b.name,
                            color = rowColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "${b.kills}",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(0.9f),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "${b.deaths}",
                        color = Color(0xFFB0BEC5),
                        fontSize = 12.sp,
                        modifier = Modifier.weight(0.9f),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = objStat,
                        color = mode.accentColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

package com.example.game.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.model.BrawlerType

@Composable
fun MainMenuScreen(
    selectedBrawler: BrawlerType,
    selectedGameMode: com.example.game.model.GameMode = com.example.game.model.GameMode.GEM_GRAB,
    trophies: Int,
    onSelectBrawler: (BrawlerType) -> Unit,
    onSelectGameMode: (com.example.game.model.GameMode) -> Unit = {},
    onStartBattle: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showBrawlerInfoDialog by remember { mutableStateOf<BrawlerType?>(null) }
    var showModeInfoDialog by remember { mutableStateOf<com.example.game.model.GameMode?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    listOf(
                        Color(0xFF1E0B36),
                        Color(0xFF311B92),
                        Color(0xFF0D47A1),
                        Color(0xFF001064)
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
            // Top Bar: Player Stats & Currencies
            TopProfileBar(trophies = trophies)

            Spacer(modifier = Modifier.height(12.dp))

            // Game Title
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "BRAWL ARENA",
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFFFEB3B),
                    letterSpacing = 2.sp
                )
                Text(
                    text = "3v3 ELMAS KAPMACA SAVAŞI",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF80D8FF)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Selected Character Hero Card
            CharacterHeroCard(
                brawler = selectedBrawler,
                onInfoClick = { showBrawlerInfoDialog = selectedBrawler }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Character Selection Carousel
            Text(
                text = "SAVAŞÇINI SEÇ:",
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(BrawlerType.values()) { brawler ->
                    BrawlerSelectionThumb(
                        brawler = brawler,
                        isSelected = brawler == selectedBrawler,
                        onClick = { onSelectBrawler(brawler) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Game Mode Selector Card
            GameModeCard(
                selectedMode = selectedGameMode,
                onSelectMode = onSelectGameMode,
                onOpenDetails = { showModeInfoDialog = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Giant "SAVAŞA BAŞLA" (Battle!) Button
            BigBattleButton(onClick = onStartBattle)

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Brawler Details Dialog
        showBrawlerInfoDialog?.let { brawler ->
            BrawlerDetailsDialog(
                brawler = brawler,
                onDismiss = { showBrawlerInfoDialog = null }
            )
        }

        // Game Mode Details Dialog
        showModeInfoDialog?.let { mode ->
            GameModeDetailsDialog(
                mode = mode,
                onDismiss = { showModeInfoDialog = null }
            )
        }
    }
}

@Composable
private fun TopProfileBar(trophies: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Player Level / Avatar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .background(Color(0x55000000), RoundedCornerShape(16.dp))
                .border(1.5.dp, Color(0x66FFFFFF), RoundedCornerShape(16.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(Color(0xFFFFB300), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("★", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text("Şampiyon", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text("Seviye 10", color = Color(0xFFB0BEC5), fontSize = 10.sp)
            }
        }

        // Trophies & Gems
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CurrencyBadge(icon = "🏆", value = "$trophies", color = Color(0xFFFFB300))
            CurrencyBadge(icon = "💎", value = "120", color = Color(0xFF00E5FF))
            CurrencyBadge(icon = "🪙", value = "2,450", color = Color(0xFFFFD54F))
        }
    }
}

@Composable
private fun CurrencyBadge(icon: String, value: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(Color(0x66000000), RoundedCornerShape(14.dp))
            .border(1.5.dp, color, RoundedCornerShape(14.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(icon, fontSize = 12.sp)
        Spacer(modifier = Modifier.width(4.dp))
        Text(value, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun CharacterHeroCard(brawler: BrawlerType, onInfoClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0x991E1238)),
        modifier = Modifier
            .fillMaxWidth()
            .border(2.5.dp, brawler.accentColor, RoundedCornerShape(20.dp))
            .shadow(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = brawler.characterName.uppercase(),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Text(
                        text = brawler.title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = brawler.accentColor
                    )
                }

                IconButton(
                    onClick = onInfoClick,
                    modifier = Modifier
                        .background(Color(0x44FFFFFF), CircleShape)
                        .size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Detay",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // High-Fidelity Character Hero Illustration
            BrawlerHeroIllustration(
                brawler = brawler,
                size = 130.dp,
                modifier = Modifier.testTag("hero_brawler_art_${brawler.name.lowercase()}")
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Stats row (HP, Speed, Range)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0x44000000), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                StatColumn(label = "SAĞLIK", value = "${brawler.maxHp}", color = Color(0xFF00E676))
                StatColumn(label = "HIZ", value = "${brawler.moveSpeed.toInt()}", color = Color(0xFF29B6F6))
                StatColumn(label = "MENZİL", value = "${brawler.attackRange.toInt()}", color = Color(0xFFFFB300))
            }
        }
    }
}

@Composable
private fun StatColumn(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, color = Color(0xFFB0BEC5), fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Text(text = value, color = color, fontSize = 15.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun BrawlerSelectionThumb(
    brawler: BrawlerType,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) Color(0xFFFFEB3B) else Color(0x66FFFFFF)
    val borderWidth = if (isSelected) 3.dp else 1.5.dp

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .background(if (isSelected) Color(0x88311B92) else Color(0x44000000))
            .border(borderWidth, borderColor, RoundedCornerShape(16.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp)
            .testTag("brawler_thumb_${brawler.name.lowercase()}")
    ) {
        BrawlerAvatarIcon(
            brawler = brawler,
            size = 46.dp,
            isSelected = isSelected
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = brawler.characterName,
            color = if (isSelected) Color(0xFFFFEB3B) else Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun GameModeCard(
    selectedMode: com.example.game.model.GameMode,
    onSelectMode: (com.example.game.model.GameMode) -> Unit,
    onOpenDetails: (com.example.game.model.GameMode) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Mode Selector Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            com.example.game.model.GameMode.values().forEach { mode ->
                val isCurrent = mode == selectedMode
                Button(
                    onClick = { onSelectMode(mode) },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isCurrent) mode.accentColor else Color(0x661A237E)
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .border(
                            width = if (isCurrent) 2.dp else 1.dp,
                            color = if (isCurrent) Color.White else Color(0x44FFFFFF),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .testTag("mode_tab_${mode.name.lowercase()}")
                ) {
                    Text(
                        text = "${mode.iconEmoji} ${mode.displayName.take(8)}",
                        color = if (isCurrent) Color(0xFF1A1A24) else Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                }
            }
        }

        // Active Mode Detailed Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xDD0D47A1)),
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, selectedMode.accentColor, RoundedCornerShape(16.dp))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .background(selectedMode.accentColor.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                        .border(2.dp, selectedMode.accentColor, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(selectedMode.iconEmoji, fontSize = 22.sp)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = selectedMode.displayName,
                            color = selectedMode.accentColor,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp
                        )
                        Box(
                            modifier = Modifier
                                .background(Color(0x55000000), RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "3v3",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Text(
                        text = selectedMode.mapName,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp
                    )
                    Text(
                        text = selectedMode.winConditionDescription,
                        color = Color(0xFFB3E5FC),
                        fontSize = 10.sp,
                        maxLines = 2
                    )
                }

                // Info / Rules button
                IconButton(
                    onClick = { onOpenDetails(selectedMode) },
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color(0x33FFFFFF), CircleShape)
                        .testTag("mode_info_button")
                ) {
                    Text("ℹ️", fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
private fun BigBattleButton(onClick: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_btn")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "battle_scale"
    )

    Button(
        onClick = onClick,
        shape = RoundedCornerShape(22.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        contentPadding = PaddingValues(0.dp),
        modifier = Modifier
            .scale(pulseScale)
            .fillMaxWidth(0.9f)
            .height(64.dp)
            .shadow(16.dp, RoundedCornerShape(22.dp))
            .testTag("battle_button")
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        listOf(Color(0xFFFFEA00), Color(0xFFFF8F00), Color(0xFFE65100))
                    ),
                    shape = RoundedCornerShape(22.dp)
                )
                .border(3.dp, Color.White, RoundedCornerShape(22.dp)),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "SAVAŞA BAŞLA!",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

@Composable
private fun BrawlerDetailsDialog(brawler: BrawlerType, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("KAPAT", fontWeight = FontWeight.Black, color = Color(0xFFFFB300))
            }
        },
        title = {
            Text(
                text = "${brawler.characterName} - ${brawler.title}",
                fontWeight = FontWeight.Black,
                color = Color.White
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                BrawlerHeroIllustration(brawler = brawler, size = 110.dp)

                Text(
                    text = "⚔️ NORMAL SALDIRI: ${brawler.mainAttackName}",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00E5FF),
                    fontSize = 13.sp,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = brawler.mainAttackDesc,
                    color = Color(0xFFECEFF1),
                    fontSize = 12.sp,
                    modifier = Modifier.fillMaxWidth()
                )

                HorizontalDivider(color = Color(0x33FFFFFF))

                Text(
                    text = "💥 SÜPER YETENEK: ${brawler.superAttackName}",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFD600),
                    fontSize = 13.sp,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = brawler.superAttackDesc,
                    color = Color(0xFFECEFF1),
                    fontSize = 12.sp,
                    modifier = Modifier.fillMaxWidth()
                )

                HorizontalDivider(color = Color(0x33FFFFFF))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Can: ${brawler.maxHp}", color = Color(0xFF69F0AE), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text("Hız: ${brawler.moveSpeed.toInt()}", color = Color(0xFF40C4FF), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text("Doldurma: ${brawler.reloadTime}s", color = Color(0xFFFFD54F), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        },
        containerColor = Color(0xFF1E0C38),
        shape = RoundedCornerShape(18.dp)
    )
}

@Composable
private fun GameModeDetailsDialog(
    mode: com.example.game.model.GameMode,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("dismiss_mode_details_dialog")
            ) {
                Text("ANLADIM", fontWeight = FontWeight.Black, color = mode.accentColor)
            }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(mode.iconEmoji, fontSize = 24.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = mode.displayName,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        fontSize = 18.sp
                    )
                    Text(
                        text = "3v3 • Harita: ${mode.mapName}",
                        color = mode.accentColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "🎯 KAZANMA KOŞULU",
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFFFD600),
                    fontSize = 13.sp
                )
                Text(
                    text = mode.winConditionDescription,
                    color = Color(0xFFECEFF1),
                    fontSize = 12.sp
                )

                Divider(color = Color(0x33FFFFFF))

                Text(
                    text = "📜 OYUN KURALLARI",
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF00E5FF),
                    fontSize = 13.sp
                )
                Text(
                    text = mode.rulesDescription,
                    color = Color(0xFFECEFF1),
                    fontSize = 12.sp
                )

                Divider(color = Color(0x33FFFFFF))

                Text(
                    text = "⚡ ÖZEL MEKANİKLER",
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFFF4081),
                    fontSize = 13.sp
                )

                val uniqueMechanics = when (mode) {
                    com.example.game.model.GameMode.GEM_GRAB ->
                        "• Kristal madeninden her 5.5 saniyede bir elmas çıkar.\n• Elmas taşıyan oyuncu ölürse tüm elmasları yere saçar.\n• 10 elmasa ulaşan takım 15 saniyelik geri sayım başlatır."
                    com.example.game.model.GameMode.TAKEDOWN ->
                        "• Her oyuncu 2 yıldız ödülüyle başlar (bounty).\n• Rakip öldürdükçe ödülün 1 artar (maksimum 7 yıldız).\n• Seni vuran takım senin üzerindeki tüm yıldızları kazanır!\n• Harita ortasında beliren Mavi Yıldız eşitlik bozucudur."
                    com.example.game.model.GameMode.CASH_GRAB ->
                        "• Ortadaki Altın Kasası düzenli olarak altın saçar ve altın torbaları (+3 altın) düşürür.\n• 15 altını toplayan takım kasayı boşaltma geri sayımı başlatır.\n• DİKKAT: 6 ve üzeri altın taşımak karakterin hareket hızını yavaşlatır!"
                }

                Text(
                    text = uniqueMechanics,
                    color = Color(0xFFECEFF1),
                    fontSize = 12.sp
                )
            }
        },
        containerColor = Color(0xFF16092E),
        shape = RoundedCornerShape(18.dp)
    )
}

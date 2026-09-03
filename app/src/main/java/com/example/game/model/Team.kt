package com.example.game.model

import androidx.compose.ui.graphics.Color

enum class Team(val displayName: String, val primaryColor: Color, val accentColor: Color) {
    BLUE("Mavi Takım", Color(0xFF2196F3), Color(0xFF64B5F6)),
    RED("Kırmızı Takım", Color(0xFFE53935), Color(0xFFE57373));

    fun opponent(): Team = if (this == BLUE) RED else BLUE
}

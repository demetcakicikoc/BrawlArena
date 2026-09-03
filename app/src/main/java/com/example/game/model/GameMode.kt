package com.example.game.model

import androidx.compose.ui.graphics.Color

enum class GameMode(
    val id: String,
    val displayName: String,
    val englishName: String,
    val mapName: String,
    val iconEmoji: String,
    val tagLine: String,
    val objectiveDescription: String,
    val primaryColor: Color,
    val accentColor: Color,
    val targetObjectiveScore: Int,
    val timeLimitSeconds: Float = 120f
) {
    GEM_GRAB(
        id = "gem_grab",
        displayName = "ELMAS KAPMACA",
        englishName = "Gem Grab",
        mapName = "Kristal Mağarası",
        iconEmoji = "💎",
        tagLine = "3v3 • Kristal Savaşı",
        objectiveDescription = "Merkez madenden çıkan 10 elması topla ve 15 saniye takımında tut!",
        primaryColor = Color(0xFF6A1B9A),
        accentColor = Color(0xFFE040FB),
        targetObjectiveScore = 10,
        timeLimitSeconds = 180f
    ),
    TAKEDOWN(
        id = "takedown",
        displayName = "YILDIZ AVI",
        englishName = "Takedown (Bounty)",
        mapName = "Yılan Çayırı",
        iconEmoji = "⭐",
        tagLine = "3v3 • Ödül Avı",
        objectiveDescription = "Rakipleri alt ederek yıldız kazan! Her leş ödülünü artırır, en çok yıldıza ulaşan kazanır!",
        primaryColor = Color(0xFFE65100),
        accentColor = Color(0xFFFFD54F),
        targetObjectiveScore = 20, // Reaching 20 stars or leading at timer expiration
        timeLimitSeconds = 90f
    ),
    CASH_GRAB(
        id = "cash_grab",
        displayName = "ALTIN KAPMACA",
        englishName = "Cash Grab",
        mapName = "Altın Kasası Kanyonu",
        iconEmoji = "🪙",
        tagLine = "3v3 • Hazine Baskını",
        objectiveDescription = "Merkez kasadan ve paralı torbalardan 15 altın topla, 15 saniye güvenle sakla!",
        primaryColor = Color(0xFF00695C),
        accentColor = Color(0xFFFFD700),
        targetObjectiveScore = 15,
        timeLimitSeconds = 180f
    );

    val isTimedMode: Boolean
        get() = this == TAKEDOWN

    val hasCountdownTimer: Boolean
        get() = this == GEM_GRAB || this == CASH_GRAB

    val winConditionDescription: String
        get() = when (this) {
            GEM_GRAB -> "10 elması topla ve 15 saniye geri sayımı tamamla!"
            TAKEDOWN -> "90 saniye içinde en çok yıldızı topla veya 20 yıldıza ilk ulaşan ol!"
            CASH_GRAB -> "15 altını topla ve 15 saniye boyunca kasayı koru!"
        }

    val rulesDescription: String
        get() = when (this) {
            GEM_GRAB -> "Harita ortasındaki kristal madeninden düzenli olarak elmas çıkar. Elmasları topla; yenilirsen taşıdığın tüm elmasları yere düşürürsün."
            TAKEDOWN -> "Her oyuncu 2 yıldızla başlar. Bir rakibi alt ettiğinde onun üzerindeki tüm yıldızları takımına kazandırırsın ve senin ödülün 1 artar (maks 7). Ortadaki Mavi Yıldız beraberlik bozucudur."
            CASH_GRAB -> "Merkez kasadan dökülen altın paraları ve altın torbalarını (+3 altın) topla. Ağır altın yükü taşıyan oyuncular yavaşlar. 15 altına ulaşan takım geri sayımı başlatır."
        }
}

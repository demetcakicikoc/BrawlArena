package com.example.game.model

import androidx.compose.ui.graphics.Color

enum class BrawlerType(
    val characterName: String,
    val title: String,
    val maxHp: Int,
    val moveSpeed: Float, // arena units per second
    val mainAttackName: String,
    val mainAttackDesc: String,
    val superAttackName: String,
    val superAttackDesc: String,
    val attackRange: Float,
    val reloadTime: Float, // seconds per ammo bar
    val maxAmmo: Int = 3,
    val avatarColor: Color,
    val accentColor: Color
) {
    BOLT(
        characterName = "Bolt",
        title = "Hızlı Nişancı",
        maxHp = 3800,
        moveSpeed = 290f,
        mainAttackName = "Altıpatlar Fırtınası",
        mainAttackDesc = "Düz bir hatta peş peşe 6 mermi sıkar. Uzun menzilli ve yüksek hasarlıdır.",
        superAttackName = "Mermi Yağmuru",
        superAttackDesc = "Daha uzun menzilli ve engelleri parçalayan devasa bir mermi akını başlatır.",
        attackRange = 520f,
        reloadTime = 1.4f,
        avatarColor = Color(0xFF1E88E5),
        accentColor = Color(0xFFFFD54F)
    ),

    SHELIA(
        characterName = "Shelia",
        title = "Pompalı Avcı",
        maxHp = 5000,
        moveSpeed = 275f,
        mainAttackName = "Saçma Yağmuru",
        mainAttackDesc = "Geniş bir konide 5 saçma mermisi atar. Yakın mesafede devasa hasar verir.",
        superAttackName = "Süper Saçma",
        superAttackDesc = "Önündeki tüm duvarları yıkan ve düşmanları geri savuran yıkıcı süper patlama.",
        attackRange = 400f,
        reloadTime = 1.3f,
        avatarColor = Color(0xFF8E24AA),
        accentColor = Color(0xFFFF4081)
    ),

    EL_GRANDE(
        characterName = "El Grande",
        title = "Maskeli Şampiyon",
        maxHp = 7600,
        moveSpeed = 310f,
        mainAttackName = "Öfke Yumrukları",
        mainAttackDesc = "Yakın mesafede peş peşe 4 sert ve hızlı yumruk sallar.",
        superAttackName = "Meteor Sıçraması",
        superAttackDesc = "Göğe yükselip seçilen noktaya meteor gibi çakılır; alan hasarı verir, engelleri kırar ve rakipleri fırlatır.",
        attackRange = 250f,
        reloadTime = 1.1f,
        avatarColor = Color(0xFF00ACC1),
        accentColor = Color(0xFFFFB300)
    ),

    SPICA(
        characterName = "Spica",
        title = "Dikenli Kaktüs",
        maxHp = 3400,
        moveSpeed = 270f,
        mainAttackName = "Kaktüs Bombası",
        mainAttackDesc = "Fırlatılan kaktüs bombası hedefe veya menzil sonuna varınca patlayıp 6 yöne diken saçar.",
        superAttackName = "Dikenli Alan",
        superAttackDesc = "Rakipleri yavaşlatan ve sürekli hasar veren dikenli bir alan meydana getirir.",
        attackRange = 460f,
        reloadTime = 1.5f,
        avatarColor = Color(0xFF43A047),
        accentColor = Color(0xFFE91E63)
    )
}

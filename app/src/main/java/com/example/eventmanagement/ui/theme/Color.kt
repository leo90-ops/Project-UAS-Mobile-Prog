package com.example.eventmanagement.ui.theme

import androidx.compose.ui.graphics.Color

// ============================================
// 🌟 TEMA PUTIH/BIRU - Material Blue (Light)
// Warna utama diganti dari Kuning/Gold ke Biru/Putih
// Background dominan putih, elemen biru untuk aksen
// ============================================

// WARNA UTAMA (dari Yellow → Blue)
val PrimaryPurple = Color(0xFF2196F3)      // ← DIGANTI ke Biru utama (Blue 500)
val PrimaryPurpleDark = Color(0xFF1976D2)  // ← Versi lebih gelap (Blue 700)
val SecondaryPurple = Color(0xFFBBDEFB)    // ← Warna sekunder (Blue 100, biru muda untuk background)

// BACKGROUND (putih dominan)
val BackgroundLight = Color(0xFFFFFFFF)    // ← Putih murni untuk background utama
val CardBackground = Color(0xFFFFFFFF)    // ← Putih untuk card

// ACTION BUTTONS
val SuccessGreen = Color(0xFF4CAF50)       // Tetap hijau untuk success, disesuaikan ke Blue-inspired
val ErrorRed = Color(0xFFE57373)           // Tetap merah untuk error, varian light
val WarningOrange = Color(0xFFFFB74D)      // Orange untuk warning, light
val InfoBlue = Color(0xFF2196F3)           // ← DIGANTI ke Biru utama (dulu yellow)

// STATUS EVENT COLORS
val StatusUpcoming = Color(0xFF2196F3)     // ← DIGANTI ke Biru utama untuk upcoming
val StatusOngoing = Color(0xFF4CAF50)      // Tetap hijau untuk ongoing
val StatusCompleted = Color(0xFFBDBDBD)    // Abu-abu light untuk completed
val StatusCancelled = Color(0xFFE57373)    // Merah light untuk cancelled

// Default Material Design colors (diset ke biru theme)
val Purple80 = Color(0xFFBBDEFB)           // Blue 100
val PurpleGrey80 = Color(0xFFE3F2FD)       // Blue 50
val Pink80 = Color(0xFFF3E5F5)             // Mauve untuk aksen
val Purple40 = Color(0xFF1976D2)           // Blue 700
val PurpleGrey40 = Color(0xFF0D47A1)       // Blue 900
val Pink40 = Color(0xFF311B92)             // Deep Purple untuk kontras
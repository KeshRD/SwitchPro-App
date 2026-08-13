package com.kairaxus.switchpro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.kairaxus.switchpro.ui.theme.SWITCHPROTheme

data class Schedule(
    val datetime: String, // "2025-07-04T20:27"
    val command: String   // "ON" or "OFF"
)

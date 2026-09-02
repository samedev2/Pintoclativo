package com.agrotech.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.agrotech.app.navigation.AgroTechNavHost
import com.agrotech.app.ui.theme.AgroTechTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AgroTechTheme {
                AgroTechNavHost()
            }
        }
    }
}

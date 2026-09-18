package com.agrotech.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.agrotech.app.navigation.AgroTechNavHost
import com.agrotech.app.ui.theme.AgroTechTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Interface clara também nas barras do Android. Mantê-las visíveis
        // evita áreas pretas e gestos escondidos em aparelhos diferentes.
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = true
            isAppearanceLightNavigationBars = true
        }

        setContent {
            AgroTechTheme {
                AgroTechNavHost()
            }
        }
    }

}

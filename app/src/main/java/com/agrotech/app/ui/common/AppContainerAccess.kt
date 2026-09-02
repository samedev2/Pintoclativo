package com.agrotech.app.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.agrotech.app.AgroTechApplication
import com.agrotech.app.di.AppContainer

@Composable
fun rememberAppContainer(): AppContainer {
    val application = LocalContext.current.applicationContext as AgroTechApplication
    return application.container
}

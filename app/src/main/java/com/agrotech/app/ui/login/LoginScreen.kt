package com.agrotech.app.ui.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.agrotech.app.data.auth.LoginResult
import com.agrotech.app.di.DefaultAppContainer
import com.agrotech.app.ui.common.rememberAppContainer
import com.agrotech.app.ui.components.AgroTechCard
import com.agrotech.app.ui.components.AgroTechTextField
import com.agrotech.app.ui.components.PrimaryButton
import com.agrotech.app.ui.components.SectionLabel
import com.agrotech.app.ui.theme.Canvas
import com.agrotech.app.ui.theme.Ink
import com.agrotech.app.ui.theme.Muted
import com.agrotech.app.ui.theme.OnInk
import com.agrotech.app.ui.theme.Spacing
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Tela de login no estilo do design system Ui/shadcn:
 *
 *  ┌──────────────────────────────────────┐
 *  │  ▦  AgroTech                          │
 *  │                                       │
 *  │  AMBIENTE DEMONSTRATIVO               │
 *  │  Acesse suas unidades                 │
 *  │                                       │
 *  │  E-mail  [____________________]       │
 *  │  Senha   [____________________]       │
 *  │                                       │
 *  │  [        Entrar        ]             │   ← PrimaryButton (filled Ink)
 *  │                                       │
 *  │  Credenciais de demonstração          │
 *  │  admin@agrotech.com                   │
 *  │  agro@2026                            │
 *  │                                       │
 *  │  Este acesso não protege dados reais. │
 *  └──────────────────────────────────────┘
 *
 *  Fundo: `Canvas` (#f5f5f5). Cartão centralizado: `Paper` (#fff)
 *  com 24dp radius, 1px border Hairline, sombra 0/0/0/1px + 0/1/3px.
 *  Inputs: `surfaceVariant` afundado, 18dp radius, sem border at
 *  rest, 1px Hairline no focus.
 *  Botão Entrar: filled Ink, text OnInk, 18dp radius, 52dp height.
 */
@Composable
fun LoginScreen(
    aoLoginSucesso: (String) -> Unit
) {
    val container = rememberAppContainer()
    val viewModel: LoginViewModel = viewModel(
        factory = viewModelFactory {
            initializer { LoginViewModel(container.authRepository) }
        }
    )
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var contador by remember { mutableStateOf(0) }

    LaunchedEffect(state.bloqueadoAte) {
        val restanteMs = state.bloqueadoAte - System.currentTimeMillis()
        if (restanteMs > 0) {
            contador = (restanteMs / 1000).toInt().coerceAtLeast(0)
            while (contador > 0) {
                delay(1000)
                contador--
            }
        }
    }

    Scaffold(
        containerColor = Canvas
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Canvas)
                .verticalScroll(rememberScrollState()),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.xxl, vertical = Spacing.xxxl),
                horizontalAlignment = Alignment.Start
            ) {
                // === Card principal ===
                AgroTechCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = Spacing.xxl
                ) {
                    Column {
                        // Logo + brand
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Ink, RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Filled.Agriculture,
                                    contentDescription = null,
                                    tint = OnInk
                                )
                            }
                            Spacer(Modifier.size(Spacing.sm + 2.dp))
                            Text(
                                "AgroTech",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(Modifier.size(Spacing.xxl))
                        SectionLabel("Ambiente demonstrativo")
                        Spacer(Modifier.size(2.dp))
                        Text(
                            "Acesse suas unidades",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.size(Spacing.xxl))

                        AgroTechTextField(
                            value = email,
                            onValueChange = { email = it.trim() },
                            label = { Text("E-mail") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.size(Spacing.md))
                        AgroTechTextField(
                            value = senha,
                            onValueChange = { senha = it },
                            label = { Text("Senha") },
                            visualTransformation = remember { PasswordVisualTransformation() },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (state.mensagemErro != null) {
                            Spacer(Modifier.size(Spacing.sm))
                            Text(
                                state.mensagemErro!!,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }

                        Spacer(Modifier.size(Spacing.xl))
                        PrimaryButton(
                            text = if (state.bloqueado) "Bloqueado (${contador}s)" else "Entrar",
                            onClick = {
                                scope.launch { viewModel.tentarLogin(email, senha) }
                            },
                            enabled = !state.bloqueado && email.isNotBlank() && senha.isNotBlank() && !state.carregando,
                            size = com.agrotech.app.ui.components.ButtonSize.Large,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.size(Spacing.xl))

                        // Card de credenciais de demonstração
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Muted.copy(alpha = 0.08f),
                                    RoundedCornerShape(14.dp)
                                )
                                .padding(Spacing.md)
                        ) {
                            Column {
                                Text(
                                    "Credenciais de demonstração",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(Modifier.size(2.dp))
                                Text(
                                    DefaultAppContainer.ADMIN_EMAIL,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Muted
                                )
                                Text(
                                    DefaultAppContainer.ADMIN_SENHA,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Muted
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.size(Spacing.lg))
                Text(
                    "Este acesso não protege dados reais.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Muted,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }

    // Reage ao resultado do login — navega pro MAIN se OK.
    LaunchedEffect(state.loginResult) {
        when (val r = state.loginResult) {
            is LoginResult.Sucesso -> {
                aoLoginSucesso(r.email)
                viewModel.limparResultado()
            }
            is LoginResult.Bloqueado -> Unit
            else -> Unit
        }
    }
}

@Composable
private fun Row(
    modifier: Modifier = Modifier,
    verticalAlignment: Alignment.Vertical = Alignment.Top,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    content: @Composable () -> Unit
) {
    androidx.compose.foundation.layout.Row(
        modifier = modifier,
        verticalAlignment = verticalAlignment,
        horizontalArrangement = horizontalArrangement,
        content = { content() }
    )
}

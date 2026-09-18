# PRD — AgroTech

## Objetivo

Digitalizar o controle técnico de granjas de frango de corte em um aplicativo Android que funcione no campo, com registros rápidos, leitura de documentos pela câmera e visão consolidada por lote.

## Usuários

- Responsáveis técnicos e gestores de unidades.
- Colaboradores que registram mortalidade, descarte, ração e pesagem.

## Estrutura

1. Login e sessão.
2. Tela inicial com resumo das unidades.
3. Unidades e respectivos lotes.
4. Detalhe do lote com Dashboard, Controle, Ração e Peso.
5. Relatórios e perfil.
6. Scanner central para QR Code e OCR de nota fiscal.

## Requisitos funcionais

- Cadastrar e consultar unidades e lotes.
- Registrar mortalidade e descarte por dia da semana.
- Calcular total semanal, percentual semanal, percentual acumulado e saldo.
- Registrar recebimentos de ração: data, nota, tipo e quantidade em kg.
- Extrair texto de nota fiscal com OCR e sugerir preenchimento.
- Ler QR Code pela câmera no modo de demonstração.
- Registrar peso em checkpoints de 7, 21, 28, 35 e 40 dias.
- Exibir dashboard e relatórios com os dados salvos.

## Regras de negócio

- `Total semanal = mortalidade + descarte` dos sete dias.
- `Saldo = saldo anterior - total semanal`; saldo inicial é a quantidade alojada.
- `% semanal = total semanal / quantidade de aves`.
- `% acumulado = soma dos percentuais semanais`.
- `Densidade = quantidade de aves / metragem`, com edição manual permitida.

## Requisitos não funcionais

- Android nativo em Kotlin e Jetpack Compose.
- Room para persistência local e repository pattern para futura API.
- Interface mobile-first, fundo branco, identidade verde e novos ícones Lucide.
- Alvos de toque de pelo menos 48 dp, suporte a telas estreitas e fontes ampliadas.
- Câmera com CameraX; OCR e QR Code com ML Kit.
- Futuro backend deve expor API segura na frente do PostgreSQL.

## Fora do escopo atual

- Sincronização PostgreSQL em produção.
- Parser fiscal completo e automático.
- Cadastro completo de consumo/troca de ração e fechamento de sobras.

## Entrega

- Código Android, Gradle, testes, mockup e documentação versionados na branch principal do repositório de entrega.
- APK de depuração gerado localmente com `./gradlew assembleDebug`; binários gerados não integram o código-fonte versionado.

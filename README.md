# AgroTech

App Android nativo (Kotlin) para digitalizar o controle técnico de granjas de frango de corte, substituindo as fichas de papel preenchidas hoje na produção. Organizado por **Unidades** (granjas) — a primeira cadastrada é a **Vitallis**.

## O que o app faz

Dentro de cada unidade, cada **Lote** de frango acompanha:

- **Cabeçalho do lote** — gênero, metragem, quantidade de aves, linhagem, data de alojamento, densidade (calculada automaticamente como aves/m²), peso inicial, % de mortalidade no transporte, dias vazios e distribuição do lote.
- **Mortalidade e descarte** — lançamento diário (Mortalidade + Descarte) por semana, com **Total**, **% Semana**, **% Acumulado** e **Saldo** calculados automaticamente a cada semana.
- **Recebimento de ração** — data, número da nota, tipo de ração (Pré-inicial, Inicial, Engorda 1/2, Final 1/2) e quantidade em kg, com opção de **ler a nota fiscal pela câmera** para agilizar o lançamento.
- **Pesagens** — nos checkpoints de 07, 21, 28, 35 e 40 dias.
- **Dashboard** — saldo atual, % acumulado, total de ração recebida, dias de alojamento e histórico de pesagens, tudo consolidado por lote.

### Fórmulas usadas no cálculo semanal de mortalidade

```
Total(semana)  = Σ mortalidade + Σ descarte da semana
Saldo(semana)  = Saldo(semana anterior) − Total(semana), com Saldo(semana 0) = qtd. de aves
% Semana       = Total(semana) / qtd. de aves
% Acumulado    = % Acumulado(semana anterior) + % Semana
```

Essa lógica vive isolada em [`MortalidadeCalculator`](app/src/main/java/com/agrotech/app/domain/calculo/MortalidadeCalculator.kt), sem depender do Android, e é coberta por [testes unitários](app/src/test/java/com/agrotech/app/domain/calculo/MortalidadeCalculatorTest.kt).

## Stack técnica

- **Kotlin + Jetpack Compose** (Material 3) para toda a UI.
- **Arquitetura MVVM + Repository**: cada tela tem um `ViewModel` que fala com um repositório via interface (`data/repository`); hoje a única implementação é local, com **Room** (SQLite). A ideia é que, quando o backend com PostgreSQL na nuvem existir, baste plugar uma implementação remota nas mesmas interfaces — sem reescrever telas.
- **Injeção de dependência manual** (`AppContainer`, em `di/`) em vez de Hilt, para manter o build simples e leve.
- **Navigation Compose** para navegação entre telas.
- **CameraX + ML Kit** (no aparelho) para OCR de nota fiscal e leitura demonstrativa de QR Code. O OCR do recebimento de ração sugere os campos reconhecidos; a ação central Escanear apresenta QR Code ou o texto da nota.

## Estrutura do projeto

```
app/src/main/java/com/agrotech/app/
  data/local/           entidades e DAOs do Room, banco (AppDatabase)
  data/repository/      interfaces de repositório + implementação local
  domain/calculo/       regras de cálculo (mortalidade), sem dependência de Android
  di/                   AppContainer (injeção de dependência manual)
  navigation/           rotas e NavHost
  ui/unidades/          lista de unidades
  ui/lotes/             lista de lotes de uma unidade + formulário de novo lote
  ui/lote/              detalhe do lote: abas Dashboard, Mortalidade, Ração, Peso
  ui/ocr/                leitura de nota fiscal pela câmera
  ui/main/               navegação principal com quatro abas e scanner central
  ui/theme/             tema Compose (paleta verde)
```

## Como rodar

1. Abra a pasta no **Android Studio** — ele sincroniza o Gradle automaticamente.
2. Rode em um emulador ou celular físico (`minSdk` 26 / Android 8.0+).

Ou pela linha de comando:

```bash
./gradlew assembleDebug   # gera o APK de debug
./gradlew testDebugUnitTest   # roda os testes unitários
```

## Design

O visual do app usa fundo branco, cartões, tipografia Inter e o verde do broto da marca como destaque. A barra inferior respeita a navegação do Android e traz a ação central Escanear. O mockup das telas mobile está documentado em [`MOCKUP_MEMORY.md`](MOCKUP_MEMORY.md) e exportado em [`docs/AgroTech Mockup.pdf`](docs/AgroTech%20Mockup.pdf). As regras atuais estão em [`Prd.md`](Prd.md) e o histórico em [`Memory.md`](Memory.md).

## Fora do escopo (por enquanto)

A ficha de papel original também tem uma tabela de "Consumo e Troca de Ração" (g/dia programado por fase) e um bloco de fechamento "Total Recebido/Consumido/Sobra". Essas telas não foram pedidas na primeira versão e podem entrar depois.

## Próximos passos

- Validar os fluxos em outros tamanhos de tela além do Galaxy A54.
- Definir e construir o backend (API + PostgreSQL) que vai alimentar um dashboard externo, substituindo o Room local.
- Cadastrar novas unidades além da Vitallis.

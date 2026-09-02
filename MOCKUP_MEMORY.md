# AgroTech — Memória do mockup (v1)

Registro do que foi definido e construído até agora para o app **AgroTech**, cobrindo o mockup visual das telas e o contexto do app Kotlin/Android que ele representa. Serve como referência para continuar o trabalho sem precisar re-explicar tudo do zero.

## O app

- Nome: **AgroTech**. Ícone: broto verde.
- Organizado por **Unidades** (granjas). A primeira unidade cadastrada é **Vitallis**.
- Cada unidade acompanha **Lotes** de frango de corte, digitalizando a ficha de papel "Controle Técnico de Frango de Corte" que a Vitallis usa hoje.
- Plataforma: app mobile nativo em **Kotlin/Android** (Jetpack Compose).

## Dados de origem (ficha da Vitallis usada como referência)

- Lote N° 2, gênero Macho, linhagem ROSS AP95.
- Metragem 3.240 m², Qtd. de aves 44.000, densidade 13,58 aves/m² (= qtd aves / metragem).
- Data de alojamento 23/07/2026, peso inicial 47,686 (kg, conforme escrito na ficha), % mort. transporte zero.
- Mortalidade e descarte lançados por dia da semana (Sex/Sáb/Dom/Seg/Ter/Qua/Qui), agrupados em semanas 1–8.
- Recebimento de ração: data, n° da nota, tipo de ração (Pré-inicial/Inicial/Engorda 1/Engorda 2/Final 1/Final 2), quantidade em kg.
- Pesagens em 5 checkpoints fixos: 07, 21, 28, 35 e 40 dias.

### Fórmulas de cálculo (por semana, dentro de um lote)

- `Total(semana) = Σ mortalidade + Σ descarte` dos 7 dias da semana.
- `Saldo(semana) = Saldo(semana anterior) − Total(semana)`, com `Saldo(semana 0) = qtd. de aves`.
- `%Sem = Total(semana) / qtd. de aves`.
- `%Acum = %Acum(semana anterior) + %Sem(semana)`.

## App Kotlin (já implementado e compilando)

- Kotlin + Jetpack Compose (Material 3), arquitetura MVVM + Repository, Room para persistência local (pensado para depois trocar por uma API/Postgres na nuvem sem reescrever as telas — só troca a implementação do repository).
- Injeção de dependência manual leve (`AppContainer`) em vez de Hilt, para reduzir risco de build.
- Leitura de NF por câmera via CameraX + ML Kit Text Recognition (on-device): o texto reconhecido vira sugestões tocáveis para preencher número da nota e quantidade, não um parser 100% automático.
- Telas: Unidades → Lotes da unidade → Novo Lote (formulário) → Detalhe do Lote com abas Dashboard / Mortalidade / Ração / Peso.
- Build verificado: `assembleDebug` e os testes unitários do cálculo de mortalidade (`MortalidadeCalculator`) passam.
- Fora do escopo desta primeira versão (aparecem na ficha de papel mas não foram pedidos): tabela "Consumo e Troca de Ração" (g/dia programado por fase) e o bloco de fechamento "Total Recebido/Consumido/Sobra".

## Design system do mockup

Base de estilo: referência **shadcn/ui neutro** (`https://styles.refero.design/style/0fd67ec5-7e9c-4ca9-b368-5d9c7388477a`), com o acento vermelho ("Ember") trocado por **verde floresta** (`#2E7D32`) — a cor do broto da marca.

Tokens usados:
- Canvas `#f5f5f5`, superfície alternativa/sidebar `#fafafa`, cartões `#ffffff`.
- Texto principal (ink) `#0a0a0a`, texto secundário `#737373`.
- Bordas hairline `#e5e5e5`.
- Raio de 24px em cartões, 18px em botões/inputs/badges/chips (geometria "pill").
- Tipografia Inter (fallback declarado do Geist no sistema de referência), corpo 14px.
- Elevação sutil: hairline + sombra leve em duas camadas.
- Verde `#2E7D32` usado em: botões primários, chip da semana ativa, aba ativa, ícones dos cartões de estatística, FAB.

A referência visual do usuário foi um dashboard desktop com sidebar ("Fazenda Santa Clara": cartões de estatística com ícone + gráficos de barra/linha). Como o AgroTech é mobile, essa linguagem (cartões com ícone-chip, grade de KPIs, gráfico simples) foi adaptada para layout de celular sem sidebar e sem chrome de OS falso (sem barra de status ou teclado desenhados).

## Telas do mockup (7 artboards, 390×844, fluxo único)

1. **Unidades** — lista de unidades (Vitallis cadastrada), cartão com ícone de granja, FAB verde para adicionar.
2. **Lotes da Vitallis** — lista de lotes (Lote 2 ativo, Lote 1 encerrado), com linhagem/gênero/qtd. de aves/data de alojamento, badge "Semana 4 em curso", FAB para novo lote.
3. **Novo Lote** — formulário com todos os campos de cabeçalho da ficha (n° do lote, gênero em segmented control, metragem, qtd. de aves, linhagem, data de alojamento, densidade, peso inicial, % mort. transporte, dias vazio, distribuição), botão "Salvar lote" fixo no rodapé.
4. **Lote — Dashboard** — 4 cartões de estatística (Saldo atual, % acumulado, Ração recebida, Dias de alojamento), gráfico de barras de ração recebida por semana, lista de pesagens registradas.
5. **Lote — Mortalidade** — chips das semanas 1–8 (semana 4 selecionada), cartão-resumo (Total/%Sem/%Acum/Saldo), lista dos 7 dias com campos de Mortalidade e Descarte.
6. **Lote — Ração** — botão de destaque "Ler NF pela câmera", lista de recebimentos (data, n° da nota, tipo, kg), FAB para novo recebimento manual.
7. **Lote — Peso** — 5 linhas para os checkpoints (07/21/28/35/40 dias), os dois últimos ainda sem valor lançado (visualmente esmaecidos).

### Ajustes feitos na revisão do mockup

- **Mortalidade**: o card-resumo da semana 4 mostrava Total = 325, mas a soma das 7 linhas diárias dava 305 — corrigido para 305 (e %Sem de 0,74% para 0,69%), mantendo Saldo (43.065) e %Acum (2,12%) consistentes com o Dashboard.
- **Dashboard**: "Dias de alojamento" estava em 41 dias, incompatível com a semana 4/checkpoint de 28 dias e com a data da nota mais recente na aba Ração (19/08/2026, ~27–28 dias após o alojamento em 23/07/2026) — corrigido para 28 dias.
- Cor de traço dos ícones (que antes estava fixa em `#2E7D32`) passou a usar a variável de tema `{{accent}}`, para que o verde seja ajustável de um único lugar no editor do mockup.

### Link do mockup publicado

`https://claude.ai/code/artifact/577adba0-47d2-4469-a6b8-57b821a1a31d`

## Próximos passos em aberto

- Validação do mockup pelo usuário (cores, hierarquia de dados no Dashboard, o que priorizar).
- Depois de aprovado, ajustar a UI real em Kotlin/Compose para bater com o mockup (cores, raios, tipografia, densidade).
- Definir a API/backend que vai alimentar o Postgres na nuvem para o dashboard externo mencionado pelo usuário (hoje o app roda 100% local em Room).

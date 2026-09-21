# GranjaCam no app (branch granjapp-v2.0)

Estado: **em desenvolvimento (infra config)**.

## Decisão

- Os dados do app e a análise computacional das câmeras ficam **mockados** nesta fase.
- A análise (detecção e rastreamento de pintos, galinhas e galo, comportamento e alertas) **não roda no app**. O serviço GranjaCam (projeto OlhoNoPinto, Python/YOLO) roda em **outro ambiente**, previsto numa VPS.
- O app renderiza apenas um **webviewer de conexão** para o serviço, que também permite conexão com câmeras locais.
- Na tela Fotos, a primeira opção é **GranjaCam** (a segunda é Foto, com a câmera do celular).

## Como o app deve se comportar

- Uma configuração `granjacamBaseUrl` define o endereço do serviço.
- Vazia: a opção GranjaCam mostra o **mock** (vídeo de teste com as caixas do modelo).
- Preenchida: a mesma tela abre a URL numa WebView. Trocar de mock para produção é só configuração.
- A URL precisa ser `https://` (o Android bloqueia HTTP puro por padrão).
- As senhas e URLs RTSP das câmeras ficam **no serviço**, nunca no app.

## Estado da implementação no app

- Barra inferior nova: Início, Lançar, Fotos, Relatórios e Mais.
- Início e Lançar novos, com dados mockados (`data/mock/MockData.kt`). O modelo Room ainda não tem "Aviário".
- Fotos: a primeira opção é **GranjaCam** (`ui/fotos/GranjaCamPane.kt`), a segunda é Foto (câmera e galeria do celular; a foto ainda não é gravada nem enviada).
- Mais: atalhos para unidades e lotes (fluxo antigo), scanner de nota fiscal e perfil.
- Modo mock do GranjaCam: vídeo de teste em `res/raw/granjacam_pintos.mp4` e caixas em `assets/granjacam/deteccoes.json`, geradas offline com o modelo real.

Gerar o APK de teste (mock):

```bash
./gradlew assembleDebug
```

Gerar apontando para o serviço (abre o webviewer em vez do mock):

```bash
./gradlew assembleDebug -PgranjacamUrl=https://cam.exemplo.com
```

No Windows, se o Gradle falhar com `Unable to establish loopback connection`, use uma pasta temporária curta:
`TEMP=C:\gtmp TMP=C:\gtmp JAVA_TOOL_OPTIONS=-Djava.io.tmpdir=C:\gtmp`.

### Detecções do vídeo de teste (cena de cima)

`assets/granjacam/deteccoes.json` é gerado por `tools/exportar_deteccoes_video.py`, que usa o detector do
GranjaCam (`pinteiro.pt`) com correções para essa câmera:

- baldes amarelos viram a classe `comedouro` (75 fixos, fora da contagem de aves);
- caixas sobre copos, contas vermelhas dos fios e objetos parados são descartadas (o quadro 0 do vídeo é o cercado vazio e serve de fundo);
- a classe da ave vem do **tamanho** (lado maior >= 48 px = galinha, senão pinto), estável por trilha;
- aves que o modelo não viu (manchas grandes que mudaram em relação ao fundo) entram como galinha;
- rastreamento ByteTrack (`tools/rastreador_pinteiro_topo.yaml`) mais a identidade persistente do GranjaCam.

```bash
python tools/exportar_deteccoes_video.py --pesos caminho/pinteiro.pt --granjacam "caminho/PINTASILGO PROJECT"
```

Limites: sem gabarito manual, a conferência foi visual. Ficam de fora algumas galinhas marrons e escuras
(cor parecida com o solo) e alguns pintos em aglomerado. Para acertar de vez em outra câmera, retreine o
detector com quadros dela (fluxo de treino incremental do GranjaCam).

## Arquitetura alvo

```
Câmeras IP (RTSP) --> Serviço GranjaCam (VPS ou mini PC na granja)
                          detecção + rastreamento + alertas + histórico
                                    | HTTPS + autenticação
                                    v
                        App: opção GranjaCam (WebView)
```

## Pendências de infra (fora do app)

- Fazer o vídeo das câmeras chegar ao serviço: VPN (Tailscale ou WireGuard) ou gateway na granja (MediaMTX ou ffmpeg enviando para a VPS). Não expor as câmeras na internet.
- Autenticação e HTTPS no serviço (hoje não há autenticação e ele só escuta em 127.0.0.1).
- Suporte a várias câmeras (hoje o `/api/iniciar` abre uma fonte por vez).
- Dimensionar a VPS: o modelo rodou a cerca de 16 fps em CPU de PC.

## Previews em HTML

Em `docs/preview/` (abrir com um servidor local, por exemplo `python -m http.server` dentro da pasta):

- `preview-novo-design.html`: novo design (Início, Lançar, Fotos) com a opção GranjaCam primeiro. O vídeo e as caixas em `media/` vêm do modelo real `pinteiro.pt`, rodado offline sobre o vídeo de teste.
- `preview-app.html`: design atual do app, para comparação.

Regra de design: sem emojis, apenas ícones no estilo das telas de referência.

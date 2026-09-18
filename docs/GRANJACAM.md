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

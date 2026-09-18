# Memória do projeto

## Premissas do produto

- Aplicativo: AgroTech.
- Unidade inicial: Vitallis; suporte estrutural a múltiplas unidades.
- Domínio: controle técnico de frango de corte.
- Persistência atual: Room/SQLite local para demonstração e testes.
- Evolução: sincronização por API REST com PostgreSQL e dashboard externo.
- Identidade: broto e paleta verde já existente.

## Funções existentes

- Login e sessão local.
- Unidades e lotes.
- Cadastro técnico de lote.
- Mortalidade e descarte diário/semanal, saldo e percentuais.
- Recebimentos de ração e OCR de nota fiscal.
- Pesagens em 7, 21, 28, 35 e 40 dias.
- Dashboard e relatórios.
- Perfil e navegação inferior.

## Decisões de UX

- O PDF `constructoestoquemobile.pdf` é referência visual, sem importar funcionalidades de estoque.
- Fundo do aplicativo sempre branco na experiência atual.
- Barras de status e navegação do Android permanecem visíveis com ícones escuros.
- Barra inferior: Início, Lotes, ação central Escanear, Relatórios e Perfil.
- A ação central abre uma câmera de demonstração com QR Code e OCR de nota fiscal.
- O OCR usado no recebimento de ração preserva o comportamento de devolver sugestões ao formulário.

## Publicação

- Em 2026-09-18, o usuário solicitou publicar o projeto completo na branch `main` de `https://github.com/DevRaymoond/Pintoclativo.git`, que estava vazia.
- A publicação deve conter o AgroTech da pasta atual, sem projetos paralelos (`construct2`, `mitroair`), backups ou artefatos de build.

# UI/UX — referência Constructo, identidade AgroTech

Atualização de 08/09/2026. Referência visual: `constructoestoquemobile.pdf`, páginas 3–6. O PDF orienta composição, hierarquia e espaçamento; seus fluxos de estoque não fazem parte do AgroTech.

## Aplicação no projeto

- Cabeçalhos em superfície separada do fundo da página; cartões com bordas discretas e tipografia Inter.
- Marca AgroTech e verdes existentes preservados: #2E7D32, #1B5E20 e #66BB6A. Superfícies e textos recebem papéis correspondentes para claro/escuro; menus e diálogos deixam de herdar tons genéricos do Material.
- Início com resumo em destaque; login com broto, rolagem, largura limitada e acomodação do teclado.
- Navegação inferior com os mesmos quatro destinos e áreas de toque nativas. Rotas internas de unidade continuam destacando Lotes.
- Formulário de lote e indicadores usam uma coluna em janelas estreitas ou fonte ampliada. Conteúdo principal tem largura máxima de 840 dp em telas maiores.
- Salvar lote ocupa espaço próprio no rodapé. Pesagens têm campo em linha separada; notas e metadados longos podem quebrar sem disputar espaço.
- Abas do lote e semanas são roláveis; seleção da semana acompanha a lista. Câmera de NF ganhou instrução e retorno visíveis.
- Campos de novo lote/ração e seleção de aba preservam estado durante recriação da tela.

## Escopo preservado

Esta atualização não altera cálculos, banco, repositórios, autenticação, regras de cadastro, relatórios ou processamento OCR. Mantém os fluxos e ações presentes na pasta, inclusive alterações locais anteriores. O início agora passa também o nome da unidade ao abrir seus lotes, evitando a rota com segmento vazio.

## Conferência visual pendente

Sem dispositivo conectado ou emulador configurado, não houve inspeção do app renderizado. Conferir em aparelho: login com teclado; telas de 320/360/412 dp; fonte ampliada; modo claro e escuro; último campo do novo lote; ração com nota longa; câmera de NF. A compilação e os testes são verificações complementares, não substituem essa inspeção.

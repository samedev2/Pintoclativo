# Instruções para LLM

## Produto

Este repositório contém o AgroTech, aplicativo Android nativo em Kotlin e Jetpack Compose para controle técnico de granjas de frango de corte. A primeira unidade é Vitallis, mas a estrutura deve continuar genérica para várias unidades.

## Regras permanentes

- Preserve o nome AgroTech, a identidade verde e todas as funcionalidades existentes.
- Priorize Light Mode com fundo branco. Componentes precisam manter contraste caso sejam reutilizados em Dark Mode.
- Desenvolva mobile-first, com alvos de toque de pelo menos 48 dp, formulários roláveis, teclado adequado e ações principais visíveis.
- Use Lucide como fonte de novos ícones. Registre a origem e licença quando um vetor for incorporado.
- Use Refero como referência de sistema visual e o PDF `constructoestoquemobile.pdf` somente como referência de composição, não como fonte de requisitos funcionais.
- Dados locais ficam em Room atrás de repositories. A evolução prevista é API REST com PostgreSQL; o app não deve acessar PostgreSQL diretamente.
- Antes de concluir mudanças, execute `assembleDebug`, testes unitários e `lintDebug`.
- Preserve alterações locais do usuário e não remova recursos sem solicitação explícita.
- Este diretório contém projetos paralelos; publique apenas o app Android AgroTech, sua documentação e o Gradle, sem backups, dependências ou arquivos locais dos outros projetos.
- Mantenha `Search.md`, `Memory.md`, `Changelog.md` e `Prd.md` atualizados.

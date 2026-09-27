# Vollmed-api — anotações para o Claude

Este arquivo é a fonte de verdade entre as duas máquinas do Lucas (Mac pessoal e Windows da empresa).
A memória local do Claude **não** sincroniza entre elas — tudo que precisa sobreviver à troca de máquina fica aqui.
Sempre que uma pendência for resolvida ou surgir uma nova, atualize este arquivo (o Lucas faz o commit).

## Como o Lucas aprende

- Iniciante em Java/Spring, fazendo o voll.med em formato de **curso guiado**. Módulo atual: **3 — SOLID**.
- Prefere explicações com **analogia concreta** e usando o **próprio código do projeto** como exemplo.
- Gosta de **escrever o código ele mesmo**: dê um esqueleto com `TODO`s e perguntas-guia, não a solução pronta.
- Uma coisa nova de cada vez — se surgir um assunto paralelo, anote em "Pendências de estudo" e siga.

## Em andamento

- **`ValidadorFeriado`**: validador + `ValidadorFeriadoTest` (3 testes com mock, passando) prontos e revisados em 27/09/2026.
  - Falta: limpar os comentários `TODO` do `ValidadorFeriadoTest` e commitar.
- **Config do banco por máquina**: `application.yml` agora usa `password: ${DB_PASSWORD:1234}` (padrão = senha do Windows; o Mac define `DB_PASSWORD` no `~/.zshrc`). No Mac, o `MedicoRepositoryTest` ainda falha por acesso/banco `vollmed_api_test` — conferir num terminal novo.

## Pendências de estudo

- [ ] **Streams**: ele usou `anyMatch` no `BrasilApiCalendarioFeriados` sem entender bem. Dar depois do `ValidadorFeriado` + teste. Roteiro: `anyMatch` comparado com `for` → `filter` / `map` / `toList` — quando usar e quando o `for` é melhor.
- [ ] **Mostrar o nome do feriado na mensagem do `ValidadorFeriado`**: hoje o contrato `ehFeriado` devolve só `boolean`, então o validador não sabe *qual* feriado é. Melhoria: trocar para algo como `Optional<String> buscarFeriado(LocalDate)` — ensina `Optional` + `filter`/`findFirst`/`map`. Bom gancho para depois da aula de streams.
- [ ] **Inversão de Dependência (D)**: o teste com mock já foi escrito. Falta o Lucas explicar o "D" com as próprias palavras, em uma frase. Também confunde `implements X` ("eu sou um X") com campo do tipo X ("eu uso um X") — revisar.
- [ ] **E se a BrasilAPI cair?** Hoje o `RestClient` lança exceção → `TratadorDeErros` devolve **500** e *nenhuma* consulta pode ser agendada. Também não há timeout. Ensina: tratamento de erro de integração externa (try/catch), timeout e cache (hoje chama a API a cada agendamento, mas a lista é por ano).

## Perguntas de negócio para o "cliente"

- [ ] A BrasilAPI conta **Carnaval** como feriado — a clínica deve bloquear agendamentos nesse dia? Hoje bloqueia.
- [ ] Se a API de feriados estiver fora do ar, a clínica prefere **aceitar** o agendamento (arriscando cair num feriado) ou **recusar** (arriscando perder consultas)?

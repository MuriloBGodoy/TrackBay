# Estado do projeto — Track Bay

> Anotação de handoff. Última sessão: **16/07/2026**.
> Leia junto com o `README.md` (que tem o passo a passo de rodar e configurar o Firebase).

---

## Onde paramos

Backend e frontend **rodando e verificados de ponta a ponta**, com dados de exemplo. O que existe
hoje é um MVP funcional em modo dev: dá para abrir o app, buscar uma placa, abrir uma OS com os
campos do ramo, fotografar, mudar status, ver o estoque baixar sozinho e imprimir o PDF pro cliente.

Nesta sessão saíram as quatro pendências de prioridade alta: **PDF da OS/orçamento**, **adaptadores
Firestore**, **regras de segurança** e **upload de fotos**. O que depende de projeto Firebase ficou
escrito e pronto para estrear — leia a seção seguinte antes de confiar nele.

| | |
| --- | --- |
| App | http://localhost:5173 |
| Scalar (documentação da API) | http://localhost:8080/docs |
| API | http://localhost:8080/api |
| Testes | 93 passando (`cd backend && ./mvnw test`) |
| Rotas REST | 35 caminhos / 55 operações documentadas |

### Como voltar a rodar

```bash
# Terminal 1
cd backend && ./mvnw spring-boot:run

# Terminal 2
cd frontend && npm run dev
```

Não precisa de Firebase nem de login: sobe em modo dev com a oficina já populada.

A pasta já foi renomeada para `D:\Projects\TrackBay` — aquela pendência morreu. Sobrou só um
detalhe cosmético: `.claude/settings.local.json` ainda guarda permissões de comandos antigos citando
`com.tork`. É histórico, não afeta nada.

---

## ⚠️ O que ainda não foi provado

Os adaptadores Firestore e o Firebase Storage **nunca rodaram contra um Firebase real** — o projeto
ainda não existe. Eles compilam, seguem as mesmas interfaces do in-memory e o conversor tem teste de
round-trip, mas *escrito e testado não é a mesma coisa que executado*.

Quando criar o projeto Firebase, rode o fluxo completo uma vez em `prod` (onboarding → cliente →
veículo → OS → entrega) antes de confiar. Os pontos que eu olharia primeiro:

- **`proximoNumeroOS`** usa transação do Firestore (o in-memory usa `synchronized`) — é o mais
  provável de surpreender
- **Datas**: `Instant` vira String ISO-8601, não `Timestamp` do Firestore. Ordena certo e volta
  certo, mas quem for consultar pelo console vai ver texto
- **`BigDecimal` vira `double`** no banco (o Firestore não tem decimal). O round-trip preserva os
  valores testados, mas dinheiro em double sempre merece um olhar

---

## O que foi feito

### Backend — Java 21 + Spring Boot 3.5.3 (Maven, via `./mvnw`)

**Domínio** (`domain/model/`) — sem anotação de framework, nomes em português:
Oficina, Usuario, Cliente (PF/PJ), Veiculo, OrdemServico, ItemServico, ItemPeca, Pagamento, Produto,
Fornecedor, MovimentacaoEstoque, VendaBalcao, Agendamento, CampoDinamico, TemplateCampos, Endereco +
enums (Ramo, Papel, StatusOS, FormaPagamento, TipoCampo, TipoVeiculo, etc).

Regras que vivem no próprio domínio:
- `StatusOS.podeIrPara()` — as transições válidas são do enum, não de um service
- `OrdemServico.transicionarPara()` — valida o fluxo e grava histórico com autor e timestamp
- `Pagamento.recalcularStatus()` — pagamento dividido, parcial, atrasado, taxa de maquininha
- `Veiculo.atualizarKm()` — o odômetro nunca anda para trás

**Validadores** (`domain/validation/`): CPF, CNPJ (dígito verificador), placa (padrão antigo **e**
Mercosul, sempre normalizada), chassi (VIN 17 sem I/O/Q), e-mail, telefone brasileiro.

**Portas e adaptadores:**
- `domain/repository/` — 10 interfaces; toda query recebe `oficinaId` como 1º argumento
- `infrastructure/memory/` — adaptadores in-memory (perfil dev)
- `infrastructure/dev/SeedDesenvolvimento.java` — popula a oficina de exemplo

**Seed dos campos dinâmicos** (`domain/seed/TemplatesPadrao.java`): os **9 ramos** com campos
plausíveis prontos. RADIADOR tem 15 campos, 2 condicionais.

**Segurança** (`security/`): `FirebaseAuthFilter` (valida ID Token, perfil prod) e `DevAuthFilter`
(header `X-Dev-User`, perfil dev). `ContextoTenant` resolve o `oficinaId` **sempre do token**.

**API** (`api/`): 11 controllers, `GlobalExceptionHandler` em RFC 7807, `ScalarController` em `/docs`.

**PDF** (`infrastructure/pdf/OrdemServicoPdf.java`, OpenPDF): `GET /api/ordens/{id}/pdf`. Em
ORÇAMENTO o título é "Orçamento" e sai linha de assinatura; aprovada, vira "Ordem de Serviço" com a
data da aprovação. Campos FOTO/ASSINATURA não entram. Renderiza os campos do ramo na versão do schema
com que a OS foi criada.

**Firestore** (`infrastructure/firestore/`, perfil `!dev`): os 10 adaptadores. Cada entidade vive em
`oficinas/{oficinaId}/{colecao}/{id}` — o isolamento vem do próprio caminho, e é por isso que as
rules conseguem validar acesso só olhando o path. Exceção: `usuarios/{uid}` na raiz, porque o usuário
é resolvido **antes** de sabermos o tenant (é dele que sai o `oficinaId`). `ConversorFirestore` faz
entidade ↔ mapa via Jackson, então os modelos seguem sem anotação de persistência.

**Storage** (`domain/storage/` + `infrastructure/storage/`): `POST /api/arquivos` sobe a imagem e
devolve a URL que a OS guarda. Em prod vai pro Firebase Storage com token de download; em dev fica em
memória e é servida por `GET /api/arquivos/dev/{id}`.

### Frontend — React 19 + Vite + TS + Tailwind 4

- `api/` — client axios, interceptor de token, `ErroApi` traduzindo Problem Details
- `auth/` — Firebase Google Sign-In + contexto + guarda de rota (login → onboarding → app)
- `forms/MotorCampos.tsx` — **o motor de campos dinâmicos**: renderiza os 12 tipos a partir do
  schema, resolve condicionais, agrupa e valida
- `lib/formato.ts` — pt-BR: R$, dd/MM/yyyy, máscaras de CPF/CNPJ/placa/telefone
- `pages/` — Login, Onboarding, Início (dashboard), Ordens, OrdemDetalhe, NovaOrdem (3 passos),
  Clientes, Veículos (+histórico por placa), Estoque, Agenda, Config
- Mobile-first: alvos ≥ 44px, barra de ação fixa no rodapé, teclado certo por campo, `safe-area`,
  fonte 16px nos inputs (evita zoom do iOS), nav inferior no celular / superior no desktop
- PWA: manifest, ícone, tema, atalhos

### Design system — "Grafite": dark premium monocromático (redesenho de 17/07)

Redesenhado a pedido do dono do produto sobre dois modelos de referência de smart parking (o
"Park.Guard" escuro foi o vencedor): superfícies de grafite profundo, cartões com borda de luz,
vidro fosco no chrome e **um único acento — o próprio branco**. Preto e branco de verdade; a
hierarquia vem de luz, peso e contraste, não de cor.

- `index.css` — a escala `tinta-50…950` foi **invertida** (50 = superfície escura de cartão,
  900 = texto quase branco). Foi o truque que fez todas as páginas existentes virarem dark sem
  reescrever cada tela: `bg-tinta-50 text-tinta-900` continua correto em todo lugar.
- Tipografia: **Space Grotesk** (display) + **Manrope** (corpo) + **JetBrains Mono** (placas,
  números de OS, cronômetro). Inter/Sora saíram no redesenho.
- Ícones: **@phosphor-icons/react** (peso `fill` no estado ativo, `regular` no resto).
  `lucide-react` foi desinstalado.
- Marca: duas peças em `components/Marca.tsx`. `MarcaArte` é a arte oficial (chaves cruzadas + pneu +
  letreiro), usada no herói da landing e no login; `MarcaSimbolo` é o pórtico da baia em SVG — placa
  de grafite, arco de metal e o veículo no vão —, usado onde a arte não caberia: sidebar, cabeçalho,
  favicon (`public/icone.svg`) e ícones do PWA. Os dois substituíram o selo do pneu com monograma
  "TW" na virada para Track Bay (a roda girava no herói; saiu junto com o utilitário `tk-gira`).
  O asset (`src/assets/logo-trackbay.webp`) vem de `trackbaylogo.png` recortado, com o fundo tirado
  e a **luminância invertida** — a arte é escura sobre cinza claro e sumiria no grafite.
  Os PNGs antigos em `src/assets/` (`marca-trackwheel.png`, `wordmark-trackwheel.png`) ficaram
  órfãos — nada mais importa eles.
- Utilitários novos: `tk-glass` (chrome de vidro), `tk-hero` (painel-heroi com facho de luz),
  `tk-pulso` (ponto "ao vivo").
- Dashboard: **Pátio da oficina** — grade de baias no estilo estacionamento (carro na baia = OS
  aberta, rótulo A01/B02…, ponto de luz por status) + painel-heroi com o carro em execução e
  cronômetro vivo desde a abertura da OS.
- A barra "modo dev" do header foi **removida** de propósito (pedido explícito do dono).

Onde a cor sobreviveu, e por quê:

| Cor | Onde | Motivo |
| --- | --- | --- |
| Vermelho | Botão `perigo`, `ErroBox`, asterisco de obrigatório, pagamento `ATRASADO`, alerta de estoque | Não é enfeite: é aviso de erro/ação irreversível. |

Status de OS **sem cor**: branco sólido = OS viva agora, contorno = ainda não virou trabalho,
apagado = encerrada. O texto continua sendo o sinal principal.

### Verificado rodando (não só compilando)

- Fluxo OS até ENTREGUE → estoque caiu 8→7 e 3→1, movimentações auditáveis, alerta de mínimo disparou
- Dashboard contou faturamento só da OS entregue
- CPF inválido → 422 · placa duplicada → 422 · `ORCAMENTO→ENTREGUE` → 409 · placa inexistente → 404
- Proxy do Vite (`/api` → :8080) e auth dev funcionando
- `npm run build` e `tsc --noEmit` limpos
- **Design**: renderei Início, Ordens, Clientes, Estoque, Veículos, Config e detalhe da OS em
  Chromium headless (390×844 e 1280×900) e olhei as capturas — sem erro de console. O `/login` só
  renderiza fora do modo dev, então subi uma instância com chave Firebase falsa só para vê-lo.
- **PDF**: gerei os dois PDFs do seed e li o resultado. O orçamento saiu com linha de assinatura; a
  OS em execução, com mecânico por item, código das peças, pagamento dividido (PIX + crédito 2x) e
  total R$ 639,80 batendo com a API
- **Upload**: subi imagem pelo `arquivosApi.enviar` real (mesmo axios/config do app) e os bytes
  voltaram idênticos. Formato inválido → 422 · foto de 11 MB → 413 · pasta `../../outra-oficina` →
  ficou contida dentro da própria oficina

---

## O que falta

### Prioridade alta (o que trava a venda)

1. **Criar o projeto Firebase e estrear o perfil prod** — é o único bloqueio real que sobrou. Sem
   isso **os dados somem quando o backend reinicia**. Passo a passo no README (§ "Configurar o
   Firebase"); o que conferir na estreia está na seção "O que ainda não foi provado" acima.
2. **Publicar as regras** — `firestore.rules` e `storage.rules` já estão escritas no repositório;
   falta um `firebase deploy --only firestore:rules,storage` quando o projeto existir.
3. **Assinatura do cliente** — o campo ASSINATURA ainda é placeholder. Falta o canvas no celular; o
   upload já existe e é só chamar `arquivosApi.enviar` com o PNG do traço, igual o campo FOTO faz.

### Prioridade média

4. **Edição dos campos dinâmicos com drag & drop** — o backend já versiona (`PUT /api/templates/{ramo}`
   cria versão nova e preserva as antigas). Falta a tela: `PaginaConfig` hoje só **lista** os campos.
5. **Telas de venda de balcão (PDV) e agenda** — API completa (`/api/vendas`, `/api/agenda`),
   telas ainda simples: a agenda só lista, o PDV não tem tela.
6. **Checklist de entrada com fotos** — modelado (`OrdemServico.ChecklistEntrada`), sem tela. O
   upload de foto já está pronto, então é tela em cima de peça existente.
7. **Gestão de usuários/papéis** — API pronta (`/api/oficina/usuarios`), `PaginaConfig` só lista.
8. **Testes do front** — nenhum ainda. O prompt pede ao menos os fluxos críticos.
9. **Fatura mensal do frotista PJ** — `FormaPagamento.FATURADO` e as condições comerciais existem
   no modelo; falta agrupar as OS do mês numa fatura por cliente.
10. **Logo da oficina no PDF** — o gerador já imprime o logo se `Oficina.logoUrl` estiver preenchido
    (aceita URL ou data-URI), mas não existe tela para subir o logo. Com o `POST /api/arquivos`
    pronto, é ligar um campo em `PaginaConfig`.

### Segunda onda (estruturado, não implementado — conforme o prompt)

- Lembrete automático de revisão/retorno via WhatsApp
- Relatórios (faturamento por período/mecânico/serviço, curva ABC de peças)
- Consulta de placa via API externa (FIPE)
- Modo offline com sincronização (sinal ruim no box)
- NF-e / NFS-e
- Comissão de mecânico
- Multi-unidade
- Billing do SaaS (planos, trial, cobrança)

---

## Decisões tomadas (e por quê)

- **Spring Boot 3.5.3, não 4.x** — o Initializr já só oferece Boot 4, mas o springdoc (usado pelo
  Scalar) só tem linha 2.x, feita pra Boot 3. O prompt pedia Boot 3; fui na combinação comprovada.
- **Maven** — o wrapper (`./mvnw`) vem no repo, então quem clonar só precisa do JDK 21. Não há
  Maven nem Gradle instalados nesta máquina.
- **Modo dev sem Firebase** — dá pra clonar e rodar sem criar conta em nada. De quebra, os
  adaptadores in-memory provam que a abstração de repositório funciona (é o teste real de que dá pra
  trocar por Postgres depois).
- **`ThreadLocal` no `ContextoTenant`** — simples e suficiente para MVC bloqueante. Se um dia migrar
  pra WebFlux, vira `Context`.
- **Sem Spring Security** — o filtro de token resolve o que precisamos; a dependência traria
  configuração que não usaríamos.
- **Seed movido de `infrastructure/` para `domain/seed/`** — o service do domínio estava importando
  da infra, invertendo a dependência que o prompt pediu.
- **OpenPDF (não iText 7 nem Flying Saucer)** — o iText 7 é AGPL (contamina SaaS fechado); OpenPDF é
  LGPL/MPL, é o fork mantido do iText 2 e desenha direto, sem passar por HTML→PDF.
- **Regras negam tudo para o cliente** — o app só fala com a API, e o Admin SDK ignora as rules por
  design. Como a chave de API web é pública, sem regra qualquer usuário logado leria o banco inteiro
  com o próprio token. Elas são defesa em profundidade, não o caminho normal de acesso.
- **Subcoleções por tenant** (`oficinas/{id}/ordens/...`) em vez de coleção raiz com campo
  `oficinaId` — o isolamento passa a vir do caminho, então a rule valida acesso olhando só o path,
  sem `get()` extra por documento. Exceção: `usuarios/{uid}` fica na raiz, porque é a busca que
  **descobre** o tenant.
- **URL de download com token, não URL assinada** — a URL fica salva na OS e precisa valer daqui a
  anos; assinada expira e quebraria o histórico. O preço é que quem tem o link vê a imagem.
- **Busca textual em memória, dentro do tenant** — o Firestore não faz `contains`. Como o volume por
  oficina é pequeno, filtrar na aplicação evita índice composto e Algolia no MVP.

---

## Coisa estranha que ficou sem explicação

Ao renomear o prompt (`PROMPT-APP-TORK.md` → `PROMPT-APP-TRACKWHEEL.md`), a frase **"Chamado Tork"**
foi **apagada** em vez de substituída — o arquivo perdeu exatos 13 bytes, e o mesmo `sed` reproduzido
isoladamente funcionou corretamente (a substituição *adicionaria* bytes, não removeria). Restaurei a
linha manualmente a partir do original e conferi que o resto do arquivo está íntegro.

Não achei a causa. Se você tiver hook ou formatador tocando em `.md`, vale investigar antes que morda
de novo em outro arquivo.

---

## Mapa rápido dos arquivos que importam

| Quero mexer em… | Vá para |
| --- | --- |
| Campos de um ramo (seed) | `backend/.../domain/seed/TemplatesPadrao.java` |
| Como o campo é renderizado | `frontend/src/forms/MotorCampos.tsx` |
| Regras do fluxo da OS | `backend/.../domain/model/StatusOS.java` + `OrdemServico.java` |
| Baixa de estoque na entrega | `backend/.../domain/service/OrdemServicoService.java` |
| Validação de CPF/CNPJ/placa | `backend/.../domain/validation/` |
| Dados de exemplo do dev | `backend/.../infrastructure/dev/SeedDesenvolvimento.java` |
| Tenant / autenticação | `backend/.../security/` |
| Layout do PDF da OS | `backend/.../infrastructure/pdf/OrdemServicoPdf.java` |
| Como os dados viram documento | `backend/.../infrastructure/firestore/ConversorFirestore.java` |
| Onde cada coleção mora | `backend/.../infrastructure/firestore/FirestoreStore.java` |
| Regras de acesso ao banco | `firestore.rules` · `storage.rules` |
| Upload de foto | `backend/.../api/ArquivoController.java` + `domain/storage/` |
| Formatação pt-BR e máscaras | `frontend/src/lib/formato.ts` |

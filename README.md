# Track Wheel

SaaS de gestão para oficinas mecânicas, vendido por assinatura.

**O diferencial:** os campos do cadastro de serviço se adaptam ao ramo da oficina. Uma oficina de
radiador vê campos de radiador; uma de suspensão vê os dela. O núcleo (cliente, veículo, OS,
pagamento) é sempre o mesmo — o que muda é o **catálogo de campos dinâmicos**, versionado e salvo no
banco, nunca hardcoded no front.

---

## Rodar em 2 minutos

Não precisa de conta no Firebase para começar. O projeto sobe em **modo dev**: dados em memória,
autenticação simulada e uma oficina de radiador já populada.

```bash
# Terminal 1 — backend (http://localhost:8080)
cd backend
./mvnw spring-boot:run

# Terminal 2 — frontend (http://localhost:5173)
cd frontend
npm install
npm run dev
```

| O quê                | Onde                          |
| -------------------- | ----------------------------- |
| Site (landing)       | http://localhost:5173         |
| Painel da oficina    | http://localhost:5173/app     |
| API                  | http://localhost:8080/api     |
| **Documentação (Scalar)** | http://localhost:8080/docs    |
| Swagger UI           | http://localhost:8080/swagger-ui.html |
| OpenAPI (JSON)       | http://localhost:8080/v3/api-docs |
| Health               | http://localhost:8080/api/health |

### O que já vem populado no modo dev

- Oficina **Radiadores Track Wheel** (ramos: RADIADOR + MECANICA_GERAL)
- Usuários: `dono@oficinatrackwheel.com.br` (OWNER) e `joao@oficinatrackwheel.com.br` (MECHANIC)
- Clientes: um PF (Carlos Souza) e um PJ frotista (Transportadora Rápida)
- Veículos: **ABC-1234** (Gol) e **BRA2E19** (caminhão, placa Mercosul)
- 3 produtos em estoque, 2 OS (uma em execução com pagamento dividido, um orçamento)

Para trocar de usuário no modo dev, mande o header `X-Dev-User` com o e-mail — útil para ver a
regra do mecânico, que só enxerga as OS atribuídas a ele:

```bash
curl -H "X-Dev-User: joao@oficinatrackwheel.com.br" http://localhost:8080/api/ordens
```

---

## Stack

| Camada   | Tecnologia                                                     |
| -------- | -------------------------------------------------------------- |
| Frontend | React 19 + Vite + TypeScript, Tailwind 4, TanStack Query, React Router 7, Phosphor Icons |
| Design   | Dark monocromático ("Grafite"): grafite profundo, cartões de vidro e o branco como único acento. Space Grotesk (display) + Manrope (corpo) + JetBrains Mono (placas e números) |
| Gráficos | ECharts com tema P&B próprio, carregado sob demanda só no painel |
| PWA      | vite-plugin-pwa (service worker com autoUpdate) + manifesto e ícones próprios |
| Backend  | Java 21 + Spring Boot 3.5.3 (REST)                             |
| Auth     | Firebase Authentication (Google Sign-In) via Firebase Admin SDK |
| Banco    | Firestore (produção) / in-memory (dev) — atrás de interfaces de repositório |
| Docs     | springdoc-openapi 2.8.6 + Scalar                               |
| Build    | Maven (via wrapper `./mvnw`, não precisa instalar o Maven)      |

**Por que Maven:** o wrapper vem junto no repositório, então quem clonar não precisa instalar nada
além do JDK 21. O ecossistema Spring também documenta tudo em Maven primeiro.

---

## Arquitetura

O acesso a dados fica **isolado atrás das interfaces de repositório** (`domain/repository`), então dá
para trocar Firestore por Postgres/JPA sem tocar no domínio. O modo dev já prova isso: os mesmos
serviços rodam com adaptadores in-memory.

```
backend/src/main/java/com/trackwheel/
├── config/           Firebase Admin, CORS, OpenAPI
├── security/         filtro de ID Token, contexto do tenant, filtro dev
├── domain/
│   ├── model/        entidades e enums (sem anotação de framework)
│   ├── repository/   interfaces — as portas
│   ├── service/      regras de negócio
│   ├── validation/   CPF, CNPJ, placa, chassi, contato
│   └── seed/         templates de campos padrão por ramo
│   └── storage/      porta de armazenamento de arquivos (fotos, assinatura)
├── infrastructure/
│   ├── memory/       adaptadores in-memory (perfil dev)
│   ├── firestore/    adaptadores Firestore (perfil prod)
│   ├── storage/      Firebase Storage (prod) / memória (dev)
│   ├── pdf/          geração do PDF da OS/orçamento
│   └── dev/          seed de dados de desenvolvimento
└── api/              controllers REST, exception handler, Scalar
```

Os adaptadores Firestore implementam as mesmas interfaces de `domain/repository/` e sobem só fora do
perfil dev. **Atenção:** eles ainda não foram executados contra um Firestore real — não havia projeto
Firebase quando foram escritos. Compilam e o conversor tem teste, mas trate o primeiro `prod` como
estreia (veja "Estado atual").

```
frontend/src/
├── api/          client HTTP + interceptor que injeta o token
├── assets/       arte da marca (logo original + letreiro em pincel recortado)
├── auth/         Firebase Auth, contexto, guarda de rota
├── components/   UI reutilizável — Marca, AppShell, Grafico, ui.tsx
├── forms/        motor de renderização dos campos dinâmicos
├── lib/          formatação pt-BR (R$, dd/MM/yyyy, máscaras) e rótulos dos enums
├── pages/        telas (PaginaHome é a landing pública)
└── types/        espelho do domínio do backend
```

### Rotas

A vitrine pública e o produto vivem em endereços separados: `/` carrega sem
sessão nenhuma e o app inteiro mora sob `/app`, atrás da guarda de rota.

| Rota                | O quê                                                     |
| ------------------- | --------------------------------------------------------- |
| `/`                 | Landing pública — hero, o diferencial dos campos dinâmicos |
| `/login`            | Entrar com Google (no modo dev cai direto no painel)       |
| `/onboarding`       | Cadastro da oficina e escolha do ramo                      |
| `/app`              | Painel: indicadores, gráficos, pátio e OS em atendimento   |
| `/app/ordens`       | Lista de OS, `/app/ordens/nova` e `/app/ordens/:id`        |
| `/app/clientes`     | Clientes PF/PJ                                             |
| `/app/veiculos`     | Busca por placa e histórico do veículo                     |
| `/app/estoque`      | Produtos e movimentações                                   |
| `/app/agenda`       | Agendamentos do dia                                        |
| `/app/config`       | Oficina, catálogo de campos por ramo e equipe              |

Só a landing e o login entram no pacote inicial; o resto (incluindo o ECharts)
baixa sob demanda.

### Marca

O símbolo é vetorial (`components/Marca.tsx`) e a mesma geometria está em
`public/icone.svg` e nos PNGs do PWA — se um mudar, mude os outros. O letreiro
"TRACK WHEEL" em pincel foi recortado da arte original do logo e passado para
branco com transparência, então ele mantém a textura da pincelada sobre o
grafite.

### Multi-tenant

Cada oficina é um tenant. **O `oficinaId` sai sempre do token**, nunca de dado enviado pelo cliente
(`ContextoTenant.oficinaId()`). Toda query de repositório recebe o `oficinaId` como primeiro
argumento — é a barreira que impede leitura cruzada entre oficinas.

### Papéis

`OWNER` (gerencia tudo e assinatura) · `MANAGER` (OS, estoque, relatórios) · `ATTENDANT` (cria OS e
cadastros) · `MECHANIC` (só vê e atualiza as OS atribuídas a ele).

---

## Campos dinâmicos (o diferencial)

O schema de formulário fica **no banco**, versionado, e o front renderiza a partir dele
(`forms/MotorCampos.tsx`). Nenhum campo é hardcoded.

```json
{
  "chave": "local_vazamento",
  "rotulo": "Local do vazamento",
  "tipo": "SELECT",
  "obrigatorio": false,
  "opcoes": ["Colmeia", "Tanque superior", "Mangueira", "..."],
  "ordem": 5,
  "grupo": "Diagnóstico",
  "condicional": { "campo": "houve_vazamento", "valor": true },
  "aplicavelA": ["CARRO", "CAMINHAO"]
}
```

- **Tipos:** `TEXTO`, `TEXTO_LONGO`, `NUMERO`, `DECIMAL`, `MOEDA`, `DATA`, `BOOLEANO`, `SELECT`,
  `MULTI_SELECT`, `FOTO`, `ASSINATURA`, `CHECKLIST`
- **Seeds prontos** para os 9 ramos (`domain/seed/TemplatesPadrao.java`) — a oficina nunca começa do zero
- **Condicionais:** o campo só aparece se outro tiver certo valor
- **Versionamento:** editar um template cria uma versão nova. A OS guarda `schemaVersion`, então OS
  antigas continuam renderizando exatamente o formulário com que foram preenchidas

Veja funcionando: `GET /api/templates/RADIADOR` → 15 campos, 2 deles condicionais.

---

## Fluxo da OS

```
ORCAMENTO → APROVADA → EM_EXECUCAO → AGUARDANDO_PECA ⇄ EM_EXECUCAO → PRONTA → ENTREGUE
     ↓          ↓            ↓               ↓                            ↓
 CANCELADA  CANCELADA    CANCELADA       CANCELADA                   CANCELADA
```

As transições válidas vivem no próprio enum (`StatusOS.podeIrPara`) — pular etapa devolve **409**.
Cada transição grava autor e timestamp no histórico. **Ao entregar, o estoque das peças próprias
baixa automaticamente** e gera movimentação auditável.

---

## Configurar o Firebase (produção)

O modo dev existe para você começar sem isso. Para o login real com Google:

### 1. Criar o projeto

1. Acesse https://console.firebase.google.com e crie um projeto
2. **Authentication → Sign-in method → Google → Ativar**
3. Em **Authentication → Settings → Authorized domains**, inclua o domínio do seu front
4. **Firestore Database → Criar banco** (modo produção)

### 2. Backend — credencial de service account

**Configurações do projeto → Contas de serviço → Gerar nova chave privada** (baixa um JSON).
Nunca versione esse arquivo.

```bash
export FIREBASE_CREDENTIALS_PATH=/caminho/serviceAccountKey.json
export FIREBASE_PROJECT_ID=seu-projeto
cd backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
```

### 3. Frontend — config do app web

**Configurações do projeto → Seus apps → Web (`</>`)**, copie o `firebaseConfig`:

```bash
cd frontend
cp .env.example .env   # preencha com os valores do console
npm run dev
```

Com `VITE_FIREBASE_API_KEY` preenchida o app sai do modo dev e passa a exigir login com Google.

### 4. Regras do Firestore e do Storage

Já estão no repositório: [`firestore.rules`](firestore.rules) e [`storage.rules`](storage.rules)
(`firebase.json` aponta para os dois). Publique com:

```bash
npm install -g firebase-tools
firebase login
firebase deploy --only firestore:rules,storage --project seu-projeto
```

**A postura é negar tudo para o cliente.** O app nunca fala com o Firestore/Storage direto — só com
a API, e o backend usa o Admin SDK, que ignora as regras por design. Como a chave de API web é
pública, sem essas regras qualquer usuário logado poderia ler o banco inteiro com o próprio token.
Então: escrita negada em todo lugar, leitura só do membro ativo dentro da própria oficina.

Se um dia o front for ler o Firestore direto (ex.: modo offline), é `firestore.rules` que precisa
mudar — a função `membroDe(oficinaId)` já está lá pronta.

### 5. Upload de fotos (Storage)

Além da credencial, o upload precisa saber o bucket:

```bash
export FIREBASE_STORAGE_BUCKET=seu-projeto.appspot.com
```

Sem essa variável o app sobe normalmente; só o `POST /api/arquivos` falha, com mensagem dizendo o
que configurar. No modo dev não precisa de nada: a foto fica em memória e é servida pela própria API.

---

## Testes

```bash
cd backend && ./mvnw test     # 93 testes
```

Cobrem os validadores (CPF, CNPJ, placa nos dois padrões, chassi, telefone/e-mail), as regras de
domínio (cálculo de totais, fluxo de status, pagamento dividido, baixa de estoque), o PDF da OS
(lendo o texto do PDF gerado), o conversor do Firestore (round-trip e tipos aceitos) e o
armazenamento de arquivos (isolamento entre oficinas, nome de arquivo vindo do cliente).

---

## Estado atual

**Pronto e rodando:**

- Onboarding da oficina com escolha de ramo e seed dos campos
- CRUD de clientes (PF/PJ com validação distinta) e veículos, busca por placa
- Fluxo completo da OS com campos dinâmicos por ramo e histórico auditável
- Pagamento dividido (ex.: R$ 200 no PIX + R$ 300 em 2x no crédito), taxa de maquininha
- Estoque com movimentação auditável, baixa automática na entrega e alerta de mínimo
- Histórico completo do veículo por placa
- Dashboard: faturamento do dia/mês, OS abertas, ticket médio, alertas
- **PDF da OS/orçamento** com dados da oficina, veículo, campos do ramo, itens, totais e garantia —
  em ORÇAMENTO sai com linha de assinatura; depois de aprovada, com a data da aprovação
- Compartilhar orçamento no WhatsApp (texto formatado)
- **Upload de fotos** do campo dinâmico: a imagem sobe na hora e a OS guarda a URL
- PWA instalável, mobile-first (alvos ≥ 44px, ação fixa no rodapé, teclado certo por campo)
- Erros no padrão RFC 7807 (Problem Details)

**Escrito, mas nunca executado de verdade** (não havia projeto Firebase):

- Adaptadores Firestore (`infrastructure/firestore/`) — implementam as 10 interfaces e sobem no perfil
  prod. Rode o fluxo completo uma vez contra o Firestore antes de confiar
- Firebase Storage (`ArmazenamentoFirebase`) — o caminho de dev, esse sim, está exercitado

**Estruturado, não implementado:**

- Assinatura do cliente: o campo ASSINATURA ainda é placeholder (falta o canvas; o upload já existe)
- Venda de balcão e agenda: API completa, telas ainda simples
- Segunda onda: lembrete automático, relatórios/curva ABC, consulta de placa (FIPE), modo offline,
  NF-e/NFS-e, comissão de mecânico, multi-unidade, billing

---

## Decisões que tomei

- **Spring Boot 3.5.3, não 3.4.x:** era a versão 3 mais recente no Maven Central. O Initializr já só
  oferece Boot 4, mas o prompt pedia Boot 3 e springdoc só tem linha 2.x (feita para Boot 3) — Boot 3
  + springdoc 2.8.6 é a combinação comprovada.
- **Modo dev sem Firebase:** dá para clonar e rodar sem criar conta em nada. Os adaptadores in-memory
  também servem de prova de que a abstração de repositório funciona.
- **`ThreadLocal` para o tenant:** simples e suficiente para MVC bloqueante. Se um dia o projeto for
  para WebFlux, isso vira `Context`.
- **Sem Spring Security:** o filtro de token resolve o que precisamos hoje; a dependência traria
  configuração que não usaríamos.

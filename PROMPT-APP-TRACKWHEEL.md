# PROMPT — Sistema de Gestão para Oficinas (SaaS multi-ramo)

> Cole este arquivo inteiro no Claude CLI como contexto inicial do projeto.
> Ele descreve **o que** construir e **como**. Peça para o Claude ler tudo antes de escrever código.

---

## 1. Contexto e objetivo

Quero construir um **SaaS de gestão para oficinas mecânicas** que será vendido por assinatura para oficinas da minha região. Chamado Track Wheel

O diferencial do produto: **os campos do cadastro de serviço se adaptam ao ramo da oficina**. Uma oficina de radiador vê opções de serviço de radiador; uma de suspensão vê opções de suspensão; uma funilaria vê outras. O núcleo (cliente, veículo, ordem de serviço, pagamento) é sempre o mesmo — o que muda é o **catálogo de campos dinâmicos** carregado conforme o ramo configurado no onboarding.

**Foco mobile-first**, mas o mesmo login deve funcionar no desktop com a mesma conta e os mesmos dados (responsivo, não app nativo separado).

---

## 2. Stack obrigatória

| Camada   | Tecnologia                                                                                            |
| -------- | ----------------------------------------------------------------------------------------------------- |
| Frontend | **React** (Vite + TypeScript), mobile-first, responsivo, PWA instalável                               |
| Backend  | **Java 21 + Spring Boot 3** (REST)                                                                    |
| Auth     | **Firebase Authentication** — login com **Google** (OAuth)                                            |
| Banco    | **Firebase Firestore** (por ora — abstrair o acesso a dados para permitir migrar pra Postgres depois) |
| Storage  | Firebase Storage (fotos de veículos, laudos, avarias)                                                 |
| Build    | Maven ou Gradle (escolha e justifique)                                                                |

### Como a auth deve funcionar

1. Front faz login via Firebase Auth (Google Sign-In) e recebe um **ID Token (JWT)**.
2. Front envia o token no header `Authorization: Bearer <token>` em toda chamada.
3. Backend Java valida o token com o **Firebase Admin SDK** e resolve o usuário + a oficina (tenant) dele.
4. Nunca confiar em dados de tenant vindos do cliente — sempre derivar do token.

---

## 3. Modelo multi-tenant

Cada **Oficina** é um tenant. Um usuário pertence a uma oficina e tem um papel.

**Papéis:** `OWNER` (dono, gerencia tudo e assinatura), `MANAGER` (gerencia OS, estoque, relatórios), `ATTENDANT` (cria OS e cadastros), `MECHANIC` (só vê e atualiza as OS atribuídas a ele).

Toda entidade carrega `oficinaId` e toda query é filtrada por ele. Regras do Firestore devem barrar leitura cruzada entre tenants — segurança não pode depender só do backend.

---

## 4. Entidades principais

### Oficina (tenant)

- id, nomeFantasia, razaoSocial, CNPJ, IE
- **ramos**: lista (uma oficina pode ter mais de um: `RADIADOR`, `MECANICA_GERAL`, `FUNILARIA_PINTURA`, `ELETRICA`, `SUSPENSAO_FREIOS`, `AR_CONDICIONADO`, `TROCA_OLEO`, `PNEUS_ALINHAMENTO`, `INJECAO_ELETRONICA`, `OUTRO`)
- endereço completo, telefone, WhatsApp, e-mail
- logo (para o PDF da OS), horário de funcionamento
- config: numeração de OS, alíquotas, texto de garantia padrão

### Cliente — **PF ou PJ**

Campo `tipoPessoa: FISICA | JURIDICA` controla a validação e os campos exibidos.

**Comuns:** id, oficinaId, nome/nomeFantasia, telefone, whatsapp, email, endereço (CEP, logradouro, número, complemento, bairro, cidade, UF), observações, dataCadastro, ativo.

**PF:** CPF (validar dígito), RG, dataNascimento, CNH (número, categoria, validade).

**PJ:** CNPJ (validar dígito), razaoSocial, inscricaoEstadual, inscricaoMunicipal, **contatoResponsavel** (nome, cargo, telefone), **condições comerciais**: prazo de pagamento, limite de crédito, tabela de preço, faturamento mensal consolidado (frotista).

Um cliente pode ter **N veículos**.

### Veículo

- placa (**única por oficina**; validar Mercosul `ABC1D23` **e** antigo `ABC1234`; sempre salvar em maiúsculo e sem hífen)
- marca, modelo, versão, anoFabricacao, anoModelo, cor
- chassi (VIN, 17 caracteres), renavam
- combustível (gasolina/etanol/flex/diesel/GNV/elétrico/híbrido)
- câmbio, motorização, kmAtual (atualizado a cada OS)
- tipoVeiculo (carro, moto, caminhão, van, máquina) — **influencia os campos dinâmicos**
- fotos, observações/avarias pré-existentes
- clienteId (proprietário atual) + **histórico de proprietários**

### Ordem de Serviço (OS) — coração do sistema

- numero (sequencial por oficina, ex.: `2026-0001`)
- clienteId, veiculoId, kmEntrada
- **status**: `ORCAMENTO` → `APROVADA` → `EM_EXECUCAO` → `AGUARDANDO_PECA` → `PRONTA` → `ENTREGUE` → `CANCELADA` (registrar histórico de transições com autor e timestamp)
- datas: abertura, previsão de entrega, conclusão, entrega
- reclamação do cliente (texto livre) + diagnóstico técnico
- **camposDinamicos**: mapa chave→valor preenchido conforme o ramo (ver seção 5)
- **itens de serviço**: descrição, tipo, valor unitário, quantidade, desconto, mecânico responsável
- **itens de peça**: produtoId, quantidade, valor unitário, desconto, origem (estoque próprio / comprada / fornecida pelo cliente)
- totais: subtotal serviços, subtotal peças, desconto geral, acréscimo, **total**
- pagamento (ver abaixo)
- garantia: prazo em dias e/ou km
- assinatura do cliente na aprovação (canvas no mobile) — guardar imagem
- checklist de entrada (combustível, pneus, avarias, itens no veículo) com fotos

### Pagamento

- formas: `DINHEIRO`, `PIX`, `DEBITO`, `CREDITO` (com parcelas), `BOLETO`, `TRANSFERENCIA`, `FATURADO` (PJ/frotista), `CHEQUE`
- **suportar pagamento dividido** (ex.: R$ 200 no PIX + R$ 300 em 2x no crédito)
- status: `PENDENTE`, `PARCIAL`, `PAGO`, `ATRASADO`
- vencimento, data de recebimento, valor recebido, taxa da maquininha (opcional)
- para FATURADO: agrupar OS do mês numa fatura por cliente PJ

### Comércio / Estoque (o app terá venda de peças e produtos)

- **Produto**: código interno, código de barras (EAN), nome, descrição, marca, categoria, unidade, **preço de custo**, **preço de venda**, margem, estoque atual, estoque mínimo, localização na prateleira, fornecedorId, aplicação (modelos compatíveis), foto, NCM
- **Fornecedor**: CNPJ, contato, prazo de entrega, condições
- **Movimentação de estoque**: ENTRADA (compra), SAIDA (venda ou consumo em OS), AJUSTE, DEVOLUCAO — com histórico auditável
- **Venda de balcão** (PDV simples): venda avulsa sem OS, para cliente cadastrado ou consumidor final
- Baixa automática de estoque quando a OS é concluída
- Alerta de estoque mínimo

### Agenda

- agendamento de serviços com data/hora, cliente, veículo, serviço previsto, duração estimada
- visão dia/semana; no mobile, lista por dia

---

## 5. Campos dinâmicos por ramo — **o diferencial, faça isso bem**

Não hardcode os campos no frontend. Modele um **schema de formulário salvo no banco** e renderizado dinamicamente.

Estrutura sugerida de um campo:

```json
{
  "chave": "tipo_radiador",
  "rotulo": "Tipo de radiador",
  "tipo": "SELECT",
  "obrigatorio": true,
  "opcoes": ["Cobre/latão", "Alumínio", "Alumínio/plástico"],
  "ordem": 1,
  "grupo": "Diagnóstico",
  "condicional": { "campo": "houve_vazamento", "valor": true },
  "aplicavelA": ["CARRO", "CAMINHAO"]
}
```

**Tipos de campo:** `TEXTO`, `TEXTO_LONGO`, `NUMERO`, `DECIMAL`, `MOEDA`, `DATA`, `BOOLEANO`, `SELECT`, `MULTI_SELECT`, `FOTO`, `ASSINATURA`, `CHECKLIST`.

**Como deve funcionar:**

- O sistema traz **templates padrão por ramo** (seed inicial pronto — não deixar a oficina começar do zero).
- No onboarding a oficina escolhe o(s) ramo(s) e já recebe os campos daquele ramo.
- O dono pode **editar, adicionar, remover e reordenar** campos numa tela de configuração (drag & drop).
- Templates são versionados: OS antigas continuam mostrando o schema com que foram criadas (guardar `schemaVersion` na OS).
- Campos condicionais (mostrar só se outro campo tem certo valor).

**Seeds que já devem vir prontos (crie os campos plausíveis para cada um):**

- **RADIADOR**: tipo de radiador, houve superaquecimento, teste de pressão (bar), local do vazamento, estado do reservatório/tampa/mangueiras/eletroventilador, serviço (limpeza química, desobstrução, solda, troca de colmeia, recuperação, troca completa), tipo de aditivo.
- **MECANICA_GERAL**: sistema afetado, ruídos, quando ocorre, revisão preventiva (checklist), troca de correias/fluidos.
- **FUNILARIA_PINTURA**: peças afetadas, tipo de dano, cor/código da tinta, precisa polimento, sinistro (seguradora, nº do sinistro).
- **ELETRICA**: componente, teste de bateria (V), alternador (A), código de falha (OBD), acessórios instalados.
- **SUSPENSAO_FREIOS**: componente, espessura de pastilha (mm), estado do disco, necessita alinhamento/balanceamento.
- **AR_CONDICIONADO**: tipo de gás, carga (g), pressão alta/baixa, higienização, teste de estanqueidade.
- **TROCA_OLEO**: tipo/viscosidade, quantidade (L), filtros trocados, km da próxima troca.
- **PNEUS_ALINHAMENTO**: medida, marca, posições trocadas, sulco (mm), cambagem/caster/convergência antes e depois.
- **INJECAO_ELETRONICA**: códigos de falha, scanner usado, teste de bicos, limpeza.

---

## 6. Funcionalidades esperadas

**Essenciais (MVP):**

- Login Google + onboarding da oficina (dados, ramo, logo)
- CRUD de clientes (PF/PJ) e veículos, com busca rápida **por placa** (a busca principal do app)
- Fluxo completo da OS: orçamento → aprovação → execução → entrega
- Campos dinâmicos por ramo
- Pagamentos (inclusive dividido)
- Estoque + venda de balcão
- **PDF da OS/orçamento** com logo, itens, totais e garantia + compartilhar no **WhatsApp**
- Histórico completo do veículo (toda OS anterior daquela placa)
- Dashboard: faturamento do dia/mês, OS abertas, ticket médio, alertas de estoque
- Gestão de usuários e papéis

**Desejáveis (segunda onda — só estruturar, não implementar agora):**

- Lembrete automático de revisão/retorno via WhatsApp
- Relatórios (faturamento por período/mecânico/serviço, curva ABC de peças)
- Consulta de placa via API externa (FIPE / dados do veículo)
- Modo offline com sincronização (mobile em box de oficina tem sinal ruim — considerar isso na arquitetura desde já)
- Emissão de NF-e/NFS-e
- Controle de comissão de mecânico
- Multi-unidade (mesma rede, várias oficinas)
- Assinatura/billing do SaaS (planos, trial, cobrança)

---

## 7. Requisitos não-funcionais

- **Mobile-first de verdade**: alvos de toque ≥ 44px, formulários usáveis com uma mão, botão de ação fixo no rodapé, teclado numérico nos campos certos, funciona bem em tela pequena e com dedo sujo de graxa.
- **Mesma conta no PC**: layout responsivo aproveitando a tela grande (tabelas, atalhos), sem app separado.
- **PWA**: instalável, cache de assets, splash, ícone.
- Validações fortes: CPF, CNPJ, placa (ambos os formatos), chassi, e-mail, telefone.
- Máscaras e formato **pt-BR**: moeda R$, datas `dd/MM/yyyy`, números com vírgula decimal.
- Tratamento de erro consistente (RFC 7807 / Problem Details no backend).
- Auditoria: quem criou/alterou o quê e quando.
- **LGPD**: dados pessoais de clientes — considerar consentimento, exclusão e minimização.
- Testes: unitários no domínio (Java) e ao menos os fluxos críticos no front.

---

## 8. Arquitetura desejada (backend Java)

Arquitetura em camadas / hexagonal leve:

```
com.oficina
├── config/          (Firebase Admin, CORS, segurança, beans)
├── security/        (filtro de validação do ID Token, contexto do tenant)
├── domain/
│   ├── model/       (entidades e enums — sem anotação de framework)
│   ├── repository/  (interfaces — portas)
│   └── service/     (regras de negócio)
├── application/     (casos de uso, DTOs, mappers)
├── infrastructure/
│   └── firestore/   (implementação dos repositórios — adaptadores)
└── api/             (controllers REST, exception handler)
```

**Ponto crítico:** o acesso ao Firestore fica **isolado atrás das interfaces de repositório**, para eu conseguir trocar por Postgres/JPA depois sem tocar no domínio.

**Frontend:**

```
src/
├── api/          (client HTTP, interceptor que injeta o token)
├── auth/         (Firebase Auth, contexto, guarda de rota)
├── components/   (UI reutilizável)
├── features/     (clientes, veiculos, ordens, estoque, agenda, config)
├── forms/        (motor de renderização dos campos dinâmicos)
├── hooks/
└── pages/
```

Sugestão: React Router, TanStack Query, React Hook Form + Zod, Tailwind.

---

## 9. Como quero que você conduza o trabalho

1. **Leia tudo antes de codar.** Se algo estiver ambíguo, me pergunte — no máximo 5 perguntas, agrupadas, antes de começar.
2. Comece pelo **domínio Java** (entidades, enums, regras) — é o que sustenta o resto.
3. Depois: segurança/auth → repositórios Firestore → API REST → frontend.
4. Entregue em **incrementos rodáveis**: cada etapa deve compilar e subir. Nada de 40 arquivos de uma vez sem eu conseguir testar.
5. Escreva um **README** com passo a passo pra rodar local (variáveis de ambiente, credenciais do Firebase, como criar o projeto no console).
6. Inclua o **seed dos templates de campo por ramo** — quero abrir o app e já ver funcionando com o ramo de radiador.
7. Comentários e nomes de domínio em **português** (Cliente, Veiculo, OrdemServico, placa); nomes técnicos em inglês tudo bem.
8. Não invente dependência que não existe. Se não souber a versão, diga.
9. Ao final de cada etapa: resuma o que fez, o que falta e o próximo passo sugerido.

---

## 10. Primeira tarefa

Comece assim:

1. Faça as perguntas que faltam (se houver).
2. Proponha a **estrutura de pastas** completa dos dois projetos.
3. Implemente o **modelo de domínio Java completo** (entidades, enums, value objects, validadores de CPF/CNPJ/placa) com testes unitários dos validadores.
4. Pare e me mostre antes de seguir pra camada de persistência.

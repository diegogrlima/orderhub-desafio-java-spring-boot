# OrderHub — Vale Verde Distribuidora

> Plataforma interna de pedidos, faturamento e recebíveis B2B. **Versão em produção: 2.7.3.**

Bem-vindo(a) ao time. Este documento é a **especificação funcional** do OrderHub: ele descreve como o sistema *deve* se comportar. Leia-o inteiro antes de abrir o código. Ao final há a seção [Chamados de manutenção](#18-chamados-de-manutenção), que é o seu backlog.

---

## 1. Produto e contexto

**Vale Verde Distribuidora de Alimentos e Higiene Ltda.** abastece supermercados, restaurantes, hotéis, padarias e instituições do interior de São Paulo e sul de Minas. São cerca de 40 clientes PJ ativos, 60 SKUs em 6 categorias e entre 8 e 15 pedidos por dia útil.

Antes do OrderHub, pedidos eram tirados por telefone e planilha, o estoque divergia da realidade e o financeiro cobrava parcelas "de cabeça". O OrderHub centraliza:

- cadastro de clientes (com limite de crédito e categoria comercial);
- catálogo e estoque;
- pedidos com descontos, cupom, frete e parcelamento;
- emissão de fatura e parcelas;
- recebimento com multa e juros por atraso;
- relatórios gerenciais para comercial e financeiro.

O sistema está **em produção desde 2025** e é mantido por um time pequeno. Você é o novo mantenedor.

## 2. Objetivo da aplicação

Garantir que cada pedido seja vendido apenas com estoque e crédito disponíveis, precificado conforme a política comercial, faturado corretamente e que cada centavo recebido ou a receber possa ser conciliado.

## 3. Funcionalidades

| Módulo | O que faz |
|---|---|
| Clientes | Cadastro, atualização, inativação, busca, consulta de situação de crédito |
| Catálogo | Categorias, produtos, ajuste de estoque, inativação/exclusão |
| Cupons | Cadastro e consulta de cupons promocionais |
| Pedidos | Criação, confirmação (gera fatura), expedição, entrega, cancelamento |
| Faturamento | Faturas e parcelas, simulação de quitação, registro de pagamento |
| Rotinas | Marcação diária de parcelas vencidas |
| Relatórios | Vendas por período, faturamento por cliente, produtos mais vendidos |
| Segurança | HTTP Basic com perfis ADMIN, SALES e FINANCE |

## 4. Regras de negócio

### 4.1 Clientes
- CNPJ (14 dígitos, apenas números) e e-mail são **únicos**. O CNPJ não pode ser alterado.
- Categoria comercial (`tier`): `STANDARD` 0%, `SILVER` 3%, `GOLD` 5%, `PLATINUM` 8% de desconto sobre o subtotal do pedido.
- `billingDay` (1–31) é o dia preferencial de vencimento das parcelas. Se nulo, vencimentos a cada 30 dias.
- Cliente **inativo** não pode fazer pedidos. Clientes nunca são excluídos fisicamente.
- Atualizar o cadastro mantendo o próprio e-mail é permitido; o e-mail só não pode pertencer a **outro** cliente.
- A busca por nome (razão social ou nome fantasia) **não diferencia maiúsculas de minúsculas**.
- Recurso inexistente retorna `404`.

### 4.2 Catálogo e estoque
- SKU único. Preço ≥ 0. Estoque nunca negativo.
- Produto **inativo** não pode ser vendido.
- Exclusão de produto: se o produto tiver **qualquer** item de pedido associado (em qualquer status), ele é apenas **inativado**. Somente produtos que nunca foram vendidos ou reservados podem ser removidos fisicamente.
- Ajuste de estoque pelo endpoint próprio, com motivo obrigatório.

### 4.3 Precificação do pedido
Ordem de cálculo:

1. `subtotal` = Σ (preço unitário × quantidade). O preço unitário fica **congelado** no item no momento da venda.
2. Desconto da categoria do cliente (% sobre o subtotal).
3. Desconto do cupom (% sobre o subtotal), se houver.
4. `mercadorias` = subtotal − desconto da categoria − desconto do cupom.
5. **Frete:** R$ 49,90 quando `mercadorias` for **menor que** R$ 500,00. Com `mercadorias` **a partir de** R$ 500,00 o frete é grátis.
6. `total` = mercadorias + frete.

Arredondamento: duas casas, `HALF_UP`.

### 4.4 Cupons
- Vigência **inclusiva** nas duas pontas (`valid_from` e `valid_until` são válidos).
- Cupom `inativo` não é aceito.
- Limite de usos (`max_uses`): quando `used_count` **atingir** `max_uses`, o cupom está esgotado.
- `min_order_amount` é comparado ao subtotal.
- A utilização é registrada na mesma transação do pedido.

### 4.5 Crédito
- `exposição` = parcelas **ainda não liquidadas** (abertas e vencidas) + pedidos `PENDING` (ainda não faturados).
- `crédito disponível` = limite − exposição.
- Um pedido só é aceito se `total ≤ crédito disponível`.

### 4.6 Pedidos — ciclo de vida

```
PENDING ──confirm──▶ CONFIRMED ──ship──▶ SHIPPED ──deliver──▶ DELIVERED
   │                     │
   └───────cancel────────┴────▶ CANCELLED
```

- **Criação:** valida cliente ativo, produtos ativos, estoque, cupom e crédito. Reserva (baixa) o estoque imediatamente. Itens repetidos do mesmo produto são consolidados. Máximo de 6 parcelas.
- **Confirmação:** emite a fatura e as parcelas. **Todo pedido confirmado deve ter fatura.** Se a fatura não puder ser emitida, a confirmação deve falhar por inteiro.
- **Cancelamento:** permitido apenas em `PENDING` e `CONFIRMED`. Em qualquer dos dois casos o **estoque reservado é devolvido**. Se houver fatura, ela e as parcelas são canceladas, desde que **não exista pagamento registrado**; havendo pagamento, o cancelamento é recusado e nada muda.
- Transições inválidas retornam `422`.

### 4.7 Faturamento e parcelas
- Número da fatura: `NF-AAAA-NNNNNN` (sequência única).
- Parcelas: valor total dividido pelo nº de parcelas; os **centavos residuais** da divisão ficam na **1ª parcela**, de modo que a soma das parcelas é **sempre igual** ao total da fatura.
- Vencimento: `emissão + 30 × nº da parcela` dias, ou, se o cliente tiver `billingDay`, esse dia do mês subsequente. Em meses que não possuem o dia (ex.: dia 31 em abril, 29/30 em fevereiro) vale o **último dia do mês**.
- Vencimento em sábado ou domingo é prorrogado para o próximo dia útil.

### 4.8 Recebimento e encargos
- A quitação é **integral por parcela**: valor devido = parcela + multa + juros.
- Pagamento até a data de vencimento (inclusive): sem encargos.
- Após o vencimento: **multa de 2%** (fixa) + **juros de 1% ao mês, pro rata die** sobre **todos os dias corridos de atraso** (mês comercial de 30 dias).
  `juros = valor × 1% × diasDeAtraso / 30`.
- O valor informado deve coincidir com o devido (tolerância de R$ 0,01).
- Não se paga parcela já quitada, de fatura cancelada, com data futura ou anterior à emissão.
- Quando todas as parcelas estão pagas, a fatura passa a `PAID`.

### 4.9 Rotina de vencimento
Diariamente às 00:05 (horário de Brasília), parcelas `OPEN` com vencimento **anterior** à data corrente passam a `OVERDUE`, e as faturas que as contêm passam a `OVERDUE`.

### 4.10 Relatórios
- Todos os períodos são `[from, to]` com **datas inclusivas**; máximo de 366 dias.
- Pedidos `CANCELLED` nunca entram em faturamento ou ranking.
- **Vendas:** quantidade de pedidos, receita bruta, ticket médio e série diária.
- **Faturamento por cliente:** pedidos, valor faturado (soma dos totais dos pedidos) e valor recebido (soma dos pagamentos).
- **Mais vendidos:** por quantidade de unidades.

### 4.11 Detalhe do pedido
O detalhe do pedido deve mostrar os itens **como foram vendidos**: preço unitário e total da linha conforme registrados no momento da venda, de forma que `Σ lineTotal = subtotal`.

## 5. Principais fluxos

**Venda completa**
1. `POST /api/orders` → pedido `PENDING` (estoque baixado, crédito reservado).
2. `POST /api/orders/{id}/confirm` → `CONFIRMED` + fatura + parcelas.
3. `POST /api/orders/{id}/ship` → `SHIPPED`.
4. `POST /api/orders/{id}/deliver` → `DELIVERED`.

**Recebimento**
1. Financeiro consulta `GET /api/installments/{id}/payoff?date=AAAA-MM-DD`.
2. `POST /api/installments/{id}/payments` com o valor exato → parcela `PAID`; fatura recalculada.

**Cobrança**
- Rotina diária marca `OVERDUE`; financeiro filtra `GET /api/invoices?status=OVERDUE`.

## 6. Arquitetura e pastas

```
orderhub/
├── pom.xml                      Maven (Java 21, Spring Boot 4.1.x), empacotamento JAR
├── docker-compose.yml           PostgreSQL 16 para execução local
├── README.md
├── GABARITO_PRIVADO.md          (não abrir — veja seção 20)
└── src/
    ├── main/
    │   ├── java/br/com/valeverde/orderhub/
    │   │   ├── OrderHubApplication.java
    │   │   ├── config/          Segurança, relógio, propriedades de negócio
    │   │   ├── domain/
    │   │   │   ├── model/       Entidades JPA e enums
    │   │   │   └── repository/  Spring Data (JPQL e SQL nativo)
    │   │   ├── service/         Regras de negócio e transações
    │   │   │   ├── pricing/     Cálculo de preço/frete
    │   │   │   └── billing/     Parcelas, calendário, encargos, fábrica de faturas
    │   │   ├── scheduler/       Rotina de vencimento
    │   │   ├── security/        UserDetailsService (banco)
    │   │   ├── exception/       Exceções de negócio
    │   │   └── web/
    │   │       ├── controller/  REST
    │   │       ├── dto/         Records de entrada/saída
    │   │       ├── mapper/      Entidade → DTO
    │   │       └── exception/   @RestControllerAdvice
    │   └── resources/
    │       ├── application.yml
    │       ├── application-sql-debug.yml
    │       └── db/migration/    Flyway V1..V5
    └── test/                    Unitários, fatia web e integração (PostgreSQL embarcado)
```

Camadas: `Controller → Service → Repository → PostgreSQL`. Controllers só traduzem HTTP/DTO; regras ficam em *services* e em componentes de domínio (`PricingCalculator`, `InstallmentScheduler`, `LateChargeCalculator`).

## 7. Modelo de dados

```
categories 1──* products
customers  1──* orders 1──* order_items *──1 products
coupons    1──* orders
orders     1──1 invoices 1──* installments 1──* payments
customers  1──* invoices
app_users  (autenticação)
```

| Tabela | Descrição | Pontos de atenção |
|---|---|---|
| `app_users` | Usuários internos e perfil | senha BCrypt |
| `categories` | Categorias de produto | |
| `products` | SKU, preço atual, estoque, ativo | `stock_quantity >= 0` |
| `customers` | Dados cadastrais, `tier`, `credit_limit`, `billing_day` | `document` e `email` únicos |
| `coupons` | % de desconto, vigência, usos | |
| `orders` | Cabeçalho, valores calculados, carimbos de data/hora | status em `CHECK` |
| `order_items` | Itens com preço congelado | FK para `orders` e `products` |
| `invoices` | Uma por pedido (`order_id` único) | número único por sequência |
| `installments` | Parcelas, encargos pagos | único `(invoice_id, number)` |
| `payments` | Pagamentos registrados | |

Índices relevantes: `idx_orders_status`, `idx_orders_created_at`, `idx_order_items_order`, `idx_invoices_customer`, `idx_payments_installment`, `idx_products_category`.

## 8. Tecnologias

Java 21 · Spring Boot 4.1 (Web MVC, Data JPA, Security, Validation) · PostgreSQL 16 · Flyway · Lombok · Maven · YAML · empacotamento JAR · JUnit 5, Mockito, AssertJ, MockMvc e PostgreSQL embarcado (Zonky) nos testes.

## 9. Pré-requisitos

- JDK 21
- Maven 3.9+ (ou use o `./mvnw` incluso)
- Docker (opcional, para o PostgreSQL local) **ou** um PostgreSQL 14+ próprio
- Um cliente SQL (DBeaver, pgAdmin ou `psql`) e `curl`/Postman

## 10. Instalação e inicialização

```powershell
# 1. Banco de dados
docker compose up -d

# 2. Build (sem testes — alguns falham de propósito, veja seção 14)
./mvnw clean package -DskipTests

# 3. Execução
java -jar target/orderhub-2.7.3.jar
# ou durante o desenvolvimento:
./mvnw spring-boot:run
```

A API sobe em `http://localhost:8080`. Para registrar o SQL emitido e estatísticas do Hibernate:

```powershell
./mvnw spring-boot:run "-Dspring-boot.run.profiles=sql-debug"
```

Variáveis de ambiente aceitas: `DB_URL`, `DB_USER`, `DB_PASSWORD`, `SERVER_PORT`.

## 11. Banco de dados: acesso

| Parâmetro | Valor padrão |
|---|---|
| Host / porta | `localhost:5432` |
| Banco | `orderhub` |
| Usuário / senha | `orderhub` / `orderhub` |

```powershell
docker exec -it orderhub-postgres psql -U orderhub -d orderhub
```

## 12. Migrations e dados iniciais

O Flyway executa automaticamente na subida da aplicação:

| Versão | Conteúdo |
|---|---|
| V1 | Esquema, constraints, índices, sequência de faturas |
| V2 | Usuários, categorias, 60 produtos, 40 clientes, 6 cupons |
| V3 | Histórico de ~18 meses (milhares de pedidos, faturas, parcelas e pagamentos) |
| V4 | Limites de crédito |
| V5 | Ajustes importados da produção para conferência |

Para recomeçar do zero: `docker compose down -v && docker compose up -d`.

As datas da carga são **relativas ao dia da execução**, então o banco sempre parece "vivo". Isso significa que cada instalação tem números ligeiramente diferentes (dados gerados com semente fixa, mas ancorados em `current_date`).

**Usuários**

| Usuário | Senha | Perfil |
|---|---|---|
| `admin` | `admin123` | ADMIN |
| `vendas` | `vendas123` | SALES |
| `financeiro` | `financeiro123` | FINANCE |

Permissões: **ADMIN** tudo; **SALES** clientes, catálogo (exceto *excluir* produtos), cupons e pedidos; **FINANCE** faturas, parcelas e relatórios. Qualquer usuário autenticado pode **consultar** (GET) a API, exceto `/api/reports/**` (ADMIN/FINANCE) e `/api/admin/**` (ADMIN).

Clientes úteis para investigação: **5, 19 e 33** (pagadores pontuais, limite apertado), **11** (`billingDay=31`), **12** (`billingDay=30`), **16** (`billingDay=29`), **38** (inativo), **40** (novo, sem histórico). Produto **60** (`MER-015`, lançamento), **58/59** (`BEB-011`/`BEB-012`, estoque quase zerado), **57** (`LIM-009`, inativo). Cupons: `QUEIMA5` (termina hoje), `VIP20` (lote esgotado), `NATAL2025` (encerrado), `PARCEIRO8` (inativo), `PRIMAVERA15` (mínimo R$ 800).

> Os ids dos produtos extras seguem a ordem de inserção da V2; confirme com `SELECT id, sku FROM products ORDER BY id;`.

## 13. Executar os testes

```powershell
./mvnw test                                    # tudo
./mvnw test -Dtest=PricingCalculatorTest       # uma classe
./mvnw test "-Dtest=*IntegrationTest"          # só integração
```

Os testes de integração usam um **PostgreSQL real embarcado** (nenhum Docker necessário; o binário é baixado na primeira execução e a primeira rodada leva alguns minutos). Eles aplicam as migrations V1–V5.

## 14. Estado atual da suíte

A suíte **não está verde**. Há testes que expressam o comportamento esperado descrito neste README e que hoje falham. Nem todo defeito conhecido tem teste, e nem toda falha de teste tem o mesmo impacto. Tratar cada falha como um chamado a investigar é parte do trabalho. **Não altere um teste para "fazê-lo passar" sem antes entender qual é o comportamento correto segundo a especificação.**

## 15. Endpoints

Autenticação: `-u usuario:senha` (HTTP Basic). Erros seguem o formato:

```json
{ "timestamp": "...", "status": 422, "error": "Unprocessable Content",
  "message": "Estoque insuficiente ...", "path": "/api/orders", "violations": [] }
```

| Método | Caminho | Descrição |
|---|---|---|
| GET | `/api/me` | Usuário autenticado |
| GET | `/api/customers?search=&page=&size=` | Lista/busca clientes |
| GET | `/api/customers/{id}` | Detalhe |
| GET | `/api/customers/{id}/credit` | Limite, exposição e crédito disponível |
| POST | `/api/customers` | Cadastra |
| PUT | `/api/customers/{id}` | Atualiza (CNPJ imutável) |
| DELETE | `/api/customers/{id}` | Inativa |
| GET/POST | `/api/categories` | Lista / cria categoria |
| GET | `/api/products?search=&categoryId=&onlyActive=` | Lista/busca |
| GET | `/api/products/{id}` | Detalhe |
| POST | `/api/products` | Cadastra |
| PUT | `/api/products/{id}` | Atualiza |
| PATCH | `/api/products/{id}/stock` | Ajusta estoque |
| DELETE | `/api/products/{id}` | Remove ou inativa |
| GET/POST | `/api/coupons` | Lista / cria |
| GET | `/api/orders?customerId=&status=` | Lista pedidos |
| GET | `/api/orders/{id}` | Detalhe |
| POST | `/api/orders` | Cria |
| POST | `/api/orders/{id}/confirm` · `/ship` · `/deliver` | Avança o pedido |
| POST | `/api/orders/{id}/cancel` | Cancela (`{"reason": "..."}`) |
| GET | `/api/invoices?customerId=&status=` | Lista faturas |
| GET | `/api/invoices/{id}` · `/api/invoices/by-order/{orderId}` | Fatura com parcelas |
| GET | `/api/installments/{id}` | Parcela |
| GET | `/api/installments/{id}/payoff?date=` | Simula quitação |
| POST | `/api/installments/{id}/payments` | Registra pagamento |
| GET | `/api/reports/sales?from=&to=` | Vendas por período |
| GET | `/api/reports/revenue-by-customer?from=&to=` | Faturamento × recebimento |
| GET | `/api/reports/top-products?from=&to=&limit=` | Mais vendidos |
| POST | `/api/admin/jobs/overdue-installments?referenceDate=` | Executa a rotina de vencimento |

### Exemplos

```powershell
# Crédito de um cliente
curl -u admin:admin123 http://localhost:8080/api/customers/5/credit

# Criar pedido (cliente 1, 4 unidades de arroz, 2 parcelas)
curl -u vendas:vendas123 -H "Content-Type: application/json" -X POST http://localhost:8080/api/orders `
  -d '{\"customerId\":1,\"couponCode\":null,\"installments\":2,\"notes\":\"teste\",\"items\":[{\"productId\":11,\"quantity\":4}]}'

# Confirmar e consultar a fatura
curl -u vendas:vendas123 -X POST http://localhost:8080/api/orders/<ID>/confirm
curl -u financeiro:financeiro123 http://localhost:8080/api/invoices/by-order/<ID>

# Simular quitação e pagar
curl -u financeiro:financeiro123 "http://localhost:8080/api/installments/<ID>/payoff?date=2026-12-01"
curl -u financeiro:financeiro123 -H "Content-Type: application/json" -X POST http://localhost:8080/api/installments/<ID>/payments `
  -d '{\"amount\":123.45,\"paymentDate\":\"2026-12-01\",\"method\":\"PIX\"}'

# Relatório de vendas
curl -u financeiro:financeiro123 "http://localhost:8080/api/reports/sales?from=2026-10-01&to=2026-10-31"
```

> Em PowerShell, `curl` pode ser um alias de `Invoke-WebRequest`; use `curl.exe` ou Postman se tiver problemas com aspas.

## 16. Comportamentos esperados (resumo por funcionalidade)

| Funcionalidade | Esperado |
|---|---|
| Criar cliente | `201`; CNPJ/e-mail duplicados → `422`; campos inválidos → `400` com `violations` |
| Atualizar cliente | `200`; manter o e-mail atual é válido; e-mail de outro cliente → `422`; inexistente → `404` |
| Buscar cliente | Insensível a maiúsculas |
| Criar pedido | `201`; estoque baixa; total conforme 4.3; sem estoque/crédito/cupom válido → `422` |
| Confirmar pedido | `CONFIRMED` **e** fatura criada; sem fatura = erro |
| Cancelar pedido | Estoque devolvido em `PENDING` e `CONFIRMED`; fatura cancelada se não houver pagamento |
| Pagamento | Valor exato (± R$ 0,01); encargos conforme 4.8 |
| Listar pedidos | Filtros `customerId` e `status` combináveis |
| Detalhe do pedido | Preços como vendidos; `Σ itens = subtotal` |
| Relatórios | Períodos inclusivos; cancelados fora; valores conciliáveis com o banco |
| Segurança | Só ADMIN exclui produtos; FINANCE não altera catálogo |

## 17. Investigando: consultas úteis

```sql
-- Pedidos confirmados sem fatura
SELECT o.id, o.customer_id, o.status, o.total_amount, o.confirmed_at
  FROM orders o LEFT JOIN invoices i ON i.order_id = o.id
 WHERE o.status IN ('CONFIRMED','SHIPPED','DELIVERED') AND i.id IS NULL;

-- Faturas cuja soma das parcelas difere do total
SELECT i.id, i.number, i.total_amount, SUM(p.amount) AS soma, i.total_amount - SUM(p.amount) AS diferenca
  FROM invoices i JOIN installments p ON p.invoice_id = i.id
 GROUP BY i.id, i.number, i.total_amount
HAVING SUM(p.amount) <> i.total_amount;

-- Itens do pedido x subtotal gravado
SELECT o.id, o.subtotal, SUM(oi.line_total) AS soma_itens
  FROM orders o JOIN order_items oi ON oi.order_id = o.id
 GROUP BY o.id, o.subtotal HAVING o.subtotal <> SUM(oi.line_total);

-- Exposição de crédito calculada "na mão" para um cliente
SELECT COALESCE(SUM(i.amount),0) FROM installments i JOIN invoices v ON v.id = i.invoice_id
 WHERE v.customer_id = 5 AND i.status IN ('OPEN','OVERDUE');

-- Encargos de parcelas pagas em atraso
SELECT id, amount, due_date, paid_at, (paid_at - due_date) AS dias, late_fee, interest,
       round(amount * 0.01 * (paid_at - due_date) / 30.0, 2) AS juros_esperado
  FROM installments WHERE status = 'PAID' AND paid_at > due_date ORDER BY dias DESC LIMIT 20;

-- Vendas por dia (conferir com o relatório)
SELECT created_at::date AS dia, COUNT(*), SUM(total_amount)
  FROM orders WHERE status <> 'CANCELLED' AND created_at >= current_date - 7 GROUP BY 1 ORDER BY 1;

-- Plano de execução
EXPLAIN (ANALYZE, BUFFERS) SELECT ...;
```

Para ver o SQL que a aplicação executa, use o perfil `sql-debug` (seção 10) e observe o número de comandos por requisição.

---

## 18. Chamados de manutenção

Cada chamado descreve **sintomas** e **resultado esperado**. A causa e o local do defeito não são informados. Reproduza, investigue, explique a causa raiz, corrija e verifique regressões. Ordem sugerida: por nível. **Os níveis são uma estimativa de esforço; chamados distintos podem ter causas relacionadas.**

### Nível 1

**CH-1001 — Frete cobrado em pedido de R$ 500,00**
*Comercial (Marcos):* "Fechei um pedido de cliente STANDARD cujo valor de mercadorias deu exatamente R$ 500,00 e o sistema cobrou frete. Pela política, a partir de R$ 500 é grátis."
**Esperado:** frete zero.

**CH-1002 — Comercial conseguiu excluir um produto**
*TI:* "Auditoria mostrou que o usuário `vendas` removeu um produto do catálogo. Segundo o desenho de acesso só o ADMIN pode excluir."
**Esperado:** `403` para `vendas` em `DELETE /api/products/{id}`.

**CH-1003 — Busca de cliente "não acha"**
*Atendimento (Juliana):* "Procuro por `mercado` e não aparece o 'MERCADO CENTRAL DE HORTOLÂNDIA LTDA', mas se eu digitar em maiúsculas aparece."
**Esperado:** busca insensível a maiúsculas/minúsculas para razão social e nome fantasia.

### Nível 2

**CH-2001 — Cupom "que termina hoje" é recusado no último dia**
*Marketing:* "A campanha QUEIMA5 vai até hoje, mas os vendedores recebem 'fora do período de validade'."
**Esperado:** cupom válido durante todo o último dia de vigência.

**CH-2002 — Cupom VIP20 continua aceito com lote esgotado**
*Comercial:* "O VIP20 era para 50 usos e a tabela mostra 50 de 50, mesmo assim ainda aplicam desconto."
**Esperado:** cupom recusado como esgotado ao atingir o limite.

**CH-2003 — Não consigo atualizar o telefone de um cliente**
*Atendimento:* "Ao salvar o cadastro do cliente só mudando o telefone aparece 'e-mail já cadastrado'. Mas o e-mail é o dele mesmo."
**Esperado:** atualização aceita quando o e-mail continua sendo do próprio cliente.

**CH-2004 — Cliente inexistente devolve erro genérico**
*Time de integração:* "`GET /api/customers/99999` retorna erro interno. Os outros recursos devolvem 'não encontrado'."
**Esperado:** `404` com mensagem padrão da API.

**CH-2005 — Filtro de pedidos ignora o status**
*Comercial:* "Na tela de pedidos, ao filtrar cliente 9 + status CANCELLED vêm todos os pedidos do cliente, inclusive entregues."
**Esperado:** filtros combinados.

### Nível 3

**CH-3001 — Detalhe do pedido não fecha com o subtotal**
*Financeiro (Renata):* "Abri pedidos de alguns meses atrás: a soma dos itens exibidos não bate com o subtotal nem com o total. No banco o total está certo."
**Esperado:** itens exibidos exatamente como vendidos.

**CH-3002 — Estoque não volta ao cancelar pedido já confirmado**
*Logística:* "Cancelamos pedidos confirmados com o cliente desistindo, mas o saldo do produto não retorna. Já nos pendentes retorna."
**Esperado:** estoque devolvido em ambos os casos.

**CH-3003 — Juros de atrasos longos parecem baixos**
*Financeiro:* "Cliente pagou com ~45 dias de atraso e os juros cobrados foram bem menores do que a regra (1% a.m. pro rata). Atrasos curtos estão corretos."
**Esperado:** juros sobre todos os dias corridos de atraso.

**CH-3004 — Tela de pedidos lenta**
*Comercial:* "A listagem de pedidos demorou vários segundos com página de 100 itens; antes era instantânea."
**Esperado:** tempo de resposta compatível com uma listagem paginada simples. Use o perfil `sql-debug` e conte as consultas.

### Nível 4

**CH-4001 — Cliente com limite sobrando é recusado por "limite insuficiente"**
*Comercial (Marcos):* "O cliente 5 tem R$ 40 mil de folga sobre o que deve, mas o sistema diz que o crédito disponível é negativo."
**Esperado:** exposição = parcelas não liquidadas + pedidos pendentes (regra 4.5).

**CH-4002 — Relatório de faturamento por cliente com valores inflados**
*Diretoria:* "O faturamento do cliente X no relatório está várias vezes acima do que somamos de pedidos no banco."
**Esperado:** faturado = soma dos totais dos pedidos; recebido = soma dos pagamentos, sem que um distorça o outro.

**CH-4003 — Relatório de vendas "esquece" o último dia**
*Financeiro:* "O relatório de vendas de 1 a 31 não traz os pedidos do dia 31 (e o total fica abaixo do que vejo no banco)."
**Esperado:** período inclusivo, conciliável com o banco.

**CH-4004 — Faturas que não fecham por centavos**
*Contabilidade:* "Algumas faturas têm parcelas somando R$ 0,01–0,02 a menos do que o total. A conciliação bancária quebra."
**Esperado:** soma das parcelas = total da fatura (sobras na 1ª parcela). Investigue também **dados já existentes**.

**CH-4005 — Relatório "Mais vendidos" demora e consome memória**
*TI:* "O relatório top-products leva muito tempo e o servidor chega a oscilar em memória quando consultado em horário de pico."
**Esperado:** resposta rápida e uso de recursos independente do tamanho do histórico total.

### Nível 5

**CH-5001 — Pedidos aceitos além do estoque**
*Logística (Paulo):* "Na promoção de ontem o estoque do produto X ficou com divergência: vendemos mais do que tínhamos. Não consigo reproduzir com um pedido por vez."
**Esperado:** nunca vender acima do estoque, mesmo com vários pedidos simultâneos.

**CH-5002 — Pedidos confirmados sem fatura**
*Financeiro (Renata):* "Alguns pedidos estão `CONFIRMED`, seguem para entrega, mas não existe fatura para cobrar. O comercial diz que confirmou normalmente e não viu erro."
**Esperado:** pedido confirmado sempre faturado; se não for possível faturar, a operação inteira falha com mensagem útil. Os clientes afetados têm algo em comum; descubra qual. Considere também o que fazer com os pedidos **já** nesse estado.

**CH-5003 — Pedidos com itens "sumindo"**
*Auditoria:* "Encontramos pedidos cujo total é maior que a soma dos itens. Os itens parecem ter sido apagados em algum momento depois de uma limpeza de catálogo."
**Esperado:** nenhuma operação de catálogo pode apagar itens de pedidos (regra 4.2). Analise o fluxo **e** o desenho do banco.

> **Cuidado com regressões.** Ao corrigir qualquer chamado, rode a suíte completa, escreva um teste que reproduza o problema *antes* da correção e verifique também os comportamentos vizinhos (por exemplo, ao mexer em estoque, em parcelas ou em relatórios).

## 19. Convenções de trabalho

- Um commit por chamado, referenciando o código (ex.: `fix(CH-1001): ...`).
- Mudança de esquema → **nova** migration Flyway; nunca altere migrations já aplicadas.
- Regras de negócio ficam em `service` e componentes de domínio, não em controllers.
- Registre a **causa raiz** (não só o sintoma) na descrição do commit.

## 20. Gabarito

O arquivo `GABARITO_PRIVADO.md` contém a lista completa de defeitos, localização, causa e correção. **Não o abra** até concluir sua investigação. Peça ao líder técnico quando quiser revisá-lo.

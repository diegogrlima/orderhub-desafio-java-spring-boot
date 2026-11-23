# GABARITO PRIVADO — OrderHub

> ⚠️ **Não leia antes de concluir sua investigação.** Este arquivo só deve ser consultado quando o desenvolvedor pedir explicitamente o gabarito.

Todos os caminhos são relativos a `src/main/java/br/com/valeverde/orderhub/` salvo indicação.

## Visão geral

| Chamado | Nível | Defeito | Local principal | Detectado por teste? |
|---|---|---|---|---|
| CH-1001 | 1 | Limite de frete com `>` em vez de `>=` | `service/pricing/PricingCalculator` | Sim |
| CH-1002 | 1 | Ordem das regras do `SecurityFilterChain` | `config/SecurityConfig` | Sim |
| CH-1003 | 1 | `LIKE` sensível a maiúsculas | `domain/repository/CustomerRepository` | Não |
| CH-2001 | 2 | Último dia de vigência do cupom excluído | `service/CouponService#validate` | Não |
| CH-2002 | 2 | `usedCount > maxUses` em vez de `>=` | `service/CouponService#validate` | Não |
| CH-2003 | 2 | `existsByEmail` inclui o próprio cliente | `service/CustomerService#update` | Não |
| CH-2004 | 2 | `orElseThrow()` sem exceção de domínio → 500 | `service/CustomerService#findById` | Não |
| CH-2005 | 2 | Ramo combinado inalcançável na listagem | `service/OrderService#list` | Não |
| CH-3001 | 3 | Mapper usa preço atual do produto | `web/mapper/OrderMapper` | Não |
| CH-3002 | 3 | Cancelar `CONFIRMED` não devolve estoque | `service/OrderService#cancel` | Sim |
| CH-3003 | 3 | `Period.getDays()` | `service/billing/LateChargeCalculator` | Sim |
| CH-3004 | 3 | N+1 na listagem de pedidos (+ `open-in-view`) | `web/mapper/OrderMapper`, `OrderRepository` | Não |
| CH-4001 | 4 | Exposição de crédito soma parcelas pagas | `InstallmentRepository#sumOpenAmountByCustomer` | Não |
| CH-4002 | 4 | Fan-out de JOINs multiplica `SUM` | `OrderRepository#revenueByCustomer` | Não |
| CH-4003 | 4 | Fim do período como `to.atStartOfDay()` | `service/ReportService#salesReport` | Não |
| CH-4004 | 4 | Parcelas arredondadas para baixo, resíduo perdido | `service/billing/InstallmentScheduler` | Sim (+ dados V5) |
| CH-4005 | 4 | `findAll()` em memória + lazy loading | `service/ReportService#topProducts` | Não |
| CH-5001 | 5 | Corrida de estoque (read-modify-write sem trava) | `service/OrderService#create` | Sim |
| CH-5002 | 5 | `billingDay` > dias do mês + `catch` que engole erro | `InstallmentScheduler`, `OrderService#confirm` | Não |
| CH-5003 | 5 | `ON DELETE CASCADE` + checagem só de `DELIVERED` | `V1` + `ProductService#delete` | Não |

Testes que falham hoje (6): `PricingCalculatorTest.orderExactlyAtThresholdHasFreeShipping`, `ProductControllerSecurityTest.salesCannotDeleteProduct`, `LateChargeCalculatorTest.fortyFiveDaysLate`, `InstallmentSchedulerTest.installmentsAddUpToInvoiceTotal`, `OrderFlowIntegrationTest.cancelConfirmedOrderRestoresStockAndCancelsInvoice`, `StockConcurrencyIntegrationTest.concurrentOrdersMustNotOversell`, além de `SeedDataIntegrationTest.installmentsMatchInvoiceTotals` (dados da V5).

---

## Nível 1

### CH-1001 — Frete
- **Local:** `PricingCalculator#calculate`: `merchandise.compareTo(freeShippingThreshold) > 0`.
- **Causa:** comparação estrita; exatamente R$ 500,00 cai no ramo "cobra frete".
- **Correção:** `>= 0`.
- **Cuidado:** o teste de limite já existe; adicione também 499,99 (cobra) e 500,01 (não cobra). Note que o limite é sobre *mercadorias após descontos*, não sobre o subtotal.

### CH-1002 — Autorização
- **Local:** `SecurityConfig`: `requestMatchers("/api/products/**").hasAnyRole("ADMIN","SALES")` vem **antes** de `requestMatchers(DELETE, "/api/products/**").hasRole("ADMIN")`. O Spring Security usa a **primeira** regra que casa.
- **Correção:** colocar a regra específica (DELETE) antes da genérica.
- **Cuidado:** revisar a ordem das demais regras. Hoje a regra `GET /api/**` authenticated vem antes das de módulo, o que também permite que `SALES` leia faturas (decisão de negócio a validar: o README diz que qualquer autenticado pode consultar).

### CH-1003 — Busca
- **Local:** `CustomerRepository#search` usa `LIKE` (sensível no PostgreSQL).
- **Correção:** `LOWER(c.legalName) LIKE LOWER(CONCAT('%', :term, '%'))` (como em `ProductRepository`), para as duas colunas. `tradeName` pode ser nulo.
- **Cuidado:** ao aplicar `LOWER`, o índice não é usado; para o volume atual é aceitável.

---

## Nível 2

### CH-2001 e CH-2002 — Cupom
- **Local:** `CouponService#validate`.
  - `today.isBefore(validUntil)` exclui o último dia → `!today.isAfter(validUntil)`.
  - `usedCount > maxUses` → `usedCount >= maxUses`.
- **Observação:** cupom `QUEIMA5` termina hoje e `VIP20` está 50/50 por desenho do seed.
- **Cuidado (relacionado):** `validateAndConsume` incrementa `usedCount` sem trava; dois pedidos simultâneos podem passar do limite (mesma família do CH-5001; não coberto pelos testes).

### CH-2003 — Atualização de cliente
- **Local:** `CustomerService#update` chama `existsByEmail(email)`, que encontra o próprio registro.
- **Correção:** verificar se o e-mail existe em **outro** cliente (ex.: `existsByEmailAndIdNot` ou `findByEmail` comparando o id).
- **Cuidado:** também é preciso normalizar (já feito) para evitar que `A@x.com` e `a@x.com` sejam considerados distintos.

### CH-2004 — 404
- **Local:** `CustomerService#findById`: `orElseThrow()` lança `NoSuchElementException`, tratada pelo handler genérico como 500.
- **Correção:** `orElseThrow(() -> new ResourceNotFoundException("Cliente", id))`.
- **Lição:** o handler genérico escondeu o defeito; o log do servidor mostra o stack trace.

### CH-2005 — Filtros de pedido
- **Local:** `OrderService#list`: `customerId != null` retorna antes do ramo `customerId != null && status != null` (código morto).
- **Correção:** testar o caso combinado primeiro.

---

## Nível 3

### CH-3001 — Preço do item
- **Local:** `OrderMapper#toItemResponse` usa `product.getPrice()` e recalcula `lineTotal`, em vez de `item.getUnitPrice()`/`item.getLineTotal()`.
- **Sintoma visível:** pedidos de mais de 120 dias foram gerados com preço 8% menor na carga histórica.
- **Correção:** usar os valores congelados no item.
- **Cuidado:** a resposta de criação também usa o mapper; conferir que continua correta.

### CH-3002 — Estoque no cancelamento
- **Local:** `OrderService#cancel`: `switch` só chama `restoreStock` para `PENDING`; o ramo `CONFIRMED` apenas cancela a fatura.
- **Correção:** devolver estoque em ambos (ajustar o switch).
- **Cuidado:** ordem das operações. Se a fatura não puder ser cancelada (pagamento), a transação inteira deve ser desfeita (a exceção já garante o rollback, mas o estado `order.cancel(...)` não deve ser persistido). Teste isso.

### CH-3003 — Juros
- **Local:** `LateChargeCalculator`: `Period.between(due, payment).getDays()` devolve **apenas a componente "dias"** (0–30), ignorando meses. 45 dias → 1 mês e 14 dias → 14.
- **Correção:** `ChronoUnit.DAYS.between(dueDate, paymentDate)`.
- **Cuidado:** o `payoff` e o registro de pagamento usam o mesmo componente; valores de pagamentos antigos importados (V5) estão no padrão defeituoso, ver CH-3003 nos dados.
- **Dados:** a migration V5 recalcula juros de parcelas pagas com ≥ 31 dias de atraso usando a mesma lógica incorreta; consulta de investigação na seção 17 do README (`juros_esperado`).

### CH-3004 — N+1
- **Local:** `OrderController#list` → `OrderMapper#toSummary` acessa `customer` (lazy), `items` (lazy, para `size()`) e `invoice` (`@OneToOne(mappedBy)`, que sempre gera consulta). `open-in-view: true` mantém a sessão aberta durante a serialização, mascarando a causa.
- **Sintoma:** com `sql-debug`, ~3 consultas adicionais por pedido.
- **Correção:** consulta com `JOIN FETCH`/`@EntityGraph` para cliente e fatura, `itemCount` por `COUNT` agregado (ou projeção DTO), e avaliar `open-in-view: false`.
- **Cuidado:** `JOIN FETCH` de coleção com paginação gera paginação em memória; por isso projeção/contagem é preferível.

---

## Nível 4

### CH-4001 — Exposição de crédito
- **Local:** `InstallmentRepository#sumOpenAmountByCustomer`: `status <> 'CANCELLED'` inclui `PAID`.
- **Correção:** `status IN ('OPEN','OVERDUE')`.
- **Por que o cliente 5:** seu limite foi definido na V4 como exposição **correta** + R$ 40 mil; com o defeito, toda a história paga entra na conta.
- **Cuidado:** `OVERDUE` também compõe exposição; ao filtrar só `OPEN` o defeito mudaria de natureza.
- **Efeito colateral importante:** isso também afeta clientes grandes (limite mais folgado), por isso o problema aparece de forma desigual.

### CH-4002 — Faturamento por cliente
- **Local:** `OrderRepository#revenueByCustomer`: junta `orders → invoices → installments → payments` e faz `SUM(o.total_amount)`; cada pedido é repetido uma vez por linha de pagamento (e por parcela).
- **Correção:** agregar em subconsultas (total por pedido; recebido por cliente) ou `SUM(DISTINCT)` não é seguro. Preferir CTEs separadas e juntar por cliente.
- **Cuidado:** pedidos sem fatura/pagamento não devem desaparecer (usar `LEFT JOIN`).
- **Outro detalhe:** `COUNT(DISTINCT o.id)` está certo, o que ajuda a mascarar o erro.

### CH-4003 — Período do relatório de vendas
- **Local:** `ReportService#salesReport`: `end = to.atStartOfDay()` e a query usa `<= :end`; só entram pedidos do último dia criados à meia-noite exata.
- **Correção:** `end = to.plusDays(1).atStartOfDay()` e `< :end` (como em `revenueByCustomer`).
- **Cuidado:** `countByStatusAndCreatedAtBetween(CANCELLED, start, end)` também usa o intervalo (`BETWEEN` é inclusivo nas duas pontas): ajustar para evitar incluir o dia seguinte à meia-noite.

### CH-4004 — Parcelas
- **Local:** `InstallmentScheduler#schedule`: `RoundingMode.DOWN` e mesmo valor para todas as parcelas; o resíduo se perde.
- **Correção:** calcular a base com `DOWN` e somar o resíduo `total − base × (n−1)` na **1ª** parcela (como a carga histórica e o README fazem).
- **Dados:** a V5 reduz R$ 0,01 da última parcela de até 3 faturas em aberto (confira com a consulta de conciliação), simulando o efeito histórico. É preciso migration **nova** para corrigir dados (não editar a V5).
- **Cuidado:** coberto parcialmente por `InstallmentSchedulerTest`; as faturas já emitidas precisam de conciliação.

### CH-4005 — Top produtos
- **Local:** `ReportService#topProducts`: `orderItemRepository.findAll()` carrega todos os itens (~dezenas de milhares) e navega `item.getOrder()`/`getProduct()` (lazy → N+1), filtrando em memória.
- **Correção:** consulta agregada `SELECT product_id, SUM(quantity), SUM(line_total) ... JOIN orders ... WHERE status <> 'CANCELLED' AND created_at ... GROUP BY ... ORDER BY ... LIMIT`.
- **Cuidado:** o `limit` deve ser validado (máximo razoável) e o desempate ordenado de forma estável.

---

## Nível 5

### CH-5001 — Oversell por concorrência
- **Local:** `OrderService#create`: lê `stockQuantity`, valida e subtrai em memória; duas transações leem o mesmo saldo. (`@Transactional` sozinho, em `READ_COMMITTED`, não impede.)
- **Reprodução:** `StockConcurrencyIntegrationTest` (12 pedidos de 1 unidade para estoque 5 → todos aceitos, estoque final inconsistente com o vendido).
- **Correções possíveis:**
  1. Bloqueio pessimista (`@Lock(PESSIMISTIC_WRITE)` no `findById` do produto), **adquirindo os locks em ordem estável de id** para evitar deadlock entre pedidos com vários produtos;
  2. Baixa atômica: `UPDATE products SET stock_quantity = stock_quantity - :q WHERE id = :id AND stock_quantity >= :q` e verificar linhas afetadas;
  3. `@Version` com retry (mais complexo).
- **Cuidado:** a devolução de estoque no cancelamento (CH-3002) tem a mesma janela de corrida; a consolidação de itens por produto já ocorre em `consolidate`, mantê-la.
- **Observação:** `updated_at` é preenchido via `@PreUpdate`, que não dispara em `UPDATE` nativo.

### CH-5002 — Confirmado sem fatura
- **Causa em duas partes:**
  1. `InstallmentScheduler#dueDateFor`: `LocalDate.of(year, month, billingDay)` lança `DateTimeException` em meses sem o dia 29/30/31 (regra 4.7 pede o último dia do mês). Clientes **11 (dia 31)**, **12 (dia 30)** e **16 (dia 29)**. A falha só ocorre quando alguma parcela cai em mês curto (ex.: 3 parcelas emitidas em nov/2026 → fev/2027).
  2. `OrderService#confirm`: o `try/catch (RuntimeException)` engole a falha e registra só um `WARN`; o pedido segue como `CONFIRMED` sem fatura.
- **Correção:** (1) `YearMonth.atDay(Math.min(billingDay, month.lengthOfMonth()))`; (2) remover o `catch` (a falha deve desfazer toda a confirmação).
- **Dados:** V5 insere 3 pedidos `CONFIRMED` sem fatura (clientes 11, 16, 12). Decidir caso a caso: faturar retroativamente (nova rotina/migration) ou reverter o status; discutir com o negócio.
- **Reprodução:** cliente 11 (ou 12) com `installments: 3`.
- **Cuidado:** depois de corrigir (2), qualquer outra falha na emissão passará a gerar erro `500`; mapear exceções com mensagens úteis.

### CH-5003 — Itens apagados
- **Causas combinadas:**
  1. `V1__create_schema.sql`: `order_items.product_id ... ON DELETE CASCADE`.
  2. `ProductService#delete` considera "vendido" só se existir item em pedido `DELIVERED` (`existsByProductIdAndOrderStatus(..., DELIVERED)`); a regra 4.2 diz **qualquer** item.
- **Reprodução:** o produto 60 (`MER-015`) só possui itens em pedidos `PENDING`/`CONFIRMED` → `DELETE /api/products/60` (como admin) remove o produto **e** os itens, deixando os pedidos com total sem itens.
- **Correção:**
  1. Service: `orderItemRepository.existsByProductId(id)` (qualquer status);
  2. Banco: **nova** migration que troque a FK por `ON DELETE RESTRICT` (padrão), nunca editar a V1;
  3. Dados já corrompidos: investigar por `orders` sem itens ou com `subtotal <> SUM(line_total)`.
- **Cuidado:** a inativação deve preservar o cadastro e continuar impedindo a venda (já impedida em `OrderService#create`).

---

## Defeitos "latentes" adicionais (não numerados nos chamados)

Para quando o desenvolvedor encontrar sozinho:

1. `Product`/`Customer`/`Invoice`/`Payment` usam `LocalDateTime.now()` em `@PrePersist` (fuso da JVM) enquanto o restante usa o `Clock` de São Paulo; em servidor UTC os carimbos divergem em 3 horas.
2. `PaymentService#register` e demais comparações de data usam `LocalDate.now(clock)`; um pagamento feito às 22h BRT não tem problema, mas o `payoff` sem data e o job devem usar o mesmo relógio (conferir `OverdueInstallmentJob`).
3. `GET /api/**` aberto a qualquer usuário autenticado: `SALES` consegue ler faturas (ver CH-1002).
4. `CouponService#validateAndConsume` sem trava de concorrência (ver CH-2001/2002).
5. Rotina de vencimento: `markInvoicesWithOverdueInstallments` só atualiza faturas `OPEN`; faturas já `OVERDUE` que recebem pagamento parcial de uma parcela continuam corretas, mas conferir `Invoice#refreshStatus` e o status `PAID`.
6. `ProductService#adjustStock` e `OrderService#create` não se protegem entre si (mesmo problema do CH-5001).
7. O handler genérico transforma qualquer erro inesperado em 500 sem identificador de correlação; dificulta suporte.

## Como conduzir o ciclo de investigação (para o líder técnico)

1. Pedir ao desenvolvedor que **reproduza** (HTTP, SQL ou teste) antes de olhar o código.
2. Perguntar: *qual regra do README é violada? em qual camada o valor deixa de estar correto?*
3. Exigir um teste que falhe antes da correção.
4. Após o fix: rodar a suíte, checar chamados vizinhos (mesma classe/tabela) e avaliar dados históricos.
5. Rejeitar correções que escondem o sintoma (ex.: ajustar o teste, arredondar no mapper, tratar exceção sem corrigir a origem).

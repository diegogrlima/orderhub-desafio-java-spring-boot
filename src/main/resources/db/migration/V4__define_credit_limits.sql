-- =====================================================================
-- OrderHub - limites de crédito
--
-- Política comercial aprovada pelo financeiro (revisão anual):
--   * clientes com grande volume histórico recebem limite proporcional ao
--     faturamento acumulado;
--   * clientes de pequeno porte e pontuais (5, 19, 33) mantêm limite
--     conservador: exposição atual + R$ 40.000,00;
--   * cliente novo (40) inicia com R$ 30.000,00.
-- =====================================================================

WITH history AS (
    SELECT c.id,
           COALESCE((SELECT SUM(i.amount)
                       FROM installments i
                       JOIN invoices v ON v.id = i.invoice_id
                      WHERE v.customer_id = c.id
                        AND i.status <> 'CANCELLED'), 0) AS invoiced_lifetime,
           COALESCE((SELECT SUM(o.total_amount)
                       FROM orders o
                      WHERE o.customer_id = c.id
                        AND o.status = 'PENDING'), 0)    AS pending_orders
      FROM customers c
)
UPDATE customers c
   SET credit_limit = ceil((h.invoiced_lifetime + h.pending_orders + 80000) / 10000.0) * 10000
  FROM history h
 WHERE h.id = c.id
   AND c.id NOT IN (5, 19, 33, 40);

WITH exposure AS (
    SELECT c.id,
           COALESCE((SELECT SUM(i.amount)
                       FROM installments i
                       JOIN invoices v ON v.id = i.invoice_id
                      WHERE v.customer_id = c.id
                        AND i.status IN ('OPEN', 'OVERDUE')), 0)
         + COALESCE((SELECT SUM(o.total_amount)
                       FROM orders o
                      WHERE o.customer_id = c.id
                        AND o.status = 'PENDING'), 0) AS current_exposure
      FROM customers c
     WHERE c.id IN (5, 19, 33)
)
UPDATE customers c
   SET credit_limit = ceil((e.current_exposure + 40000) / 5000.0) * 5000
  FROM exposure e
 WHERE e.id = c.id;

UPDATE customers SET credit_limit = 30000 WHERE id = 40;

ANALYZE;

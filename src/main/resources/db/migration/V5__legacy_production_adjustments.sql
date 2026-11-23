-- =====================================================================
-- OrderHub - ajustes de dados vindos da base de produção
--
-- Registros importados do ambiente de produção após o último ciclo de
-- fechamento. Mantidos como estão para fins de conferência e auditoria.
-- =====================================================================

-- Pedidos confirmados pela equipe comercial nos últimos dias
INSERT INTO orders (customer_id, status, subtotal, tier_discount, coupon_discount, shipping_fee, total_amount,
                    installments, notes, created_at, confirmed_at)
VALUES
 (11, 'CONFIRMED', 799.50, 23.99, 0, 0, 775.51, 2, 'Reposição mensal',        localtimestamp - interval '4 days', localtimestamp - interval '4 days' + interval '2 hours'),
 (16, 'CONFIRMED', 679.20,  0.00, 0, 0, 679.20, 1, 'Pedido recorrente',       localtimestamp - interval '3 days', localtimestamp - interval '3 days' + interval '2 hours'),
 (12, 'CONFIRMED', 688.00, 34.40, 0, 0, 653.60, 3, 'Entrega agendada sexta',  localtimestamp - interval '2 days', localtimestamp - interval '2 days' + interval '2 hours');

INSERT INTO order_items (order_id, product_id, quantity, unit_price, line_total)
SELECT o.id, 11, 5, 159.90, 799.50 FROM orders o WHERE o.customer_id = 11 AND o.notes = 'Reposição mensal'
UNION ALL
SELECT o.id, 12, 8,  84.90, 679.20 FROM orders o WHERE o.customer_id = 16 AND o.notes = 'Pedido recorrente'
UNION ALL
SELECT o.id, 14, 4, 172.00, 688.00 FROM orders o WHERE o.customer_id = 12 AND o.notes = 'Entrega agendada sexta';

-- Faturas em aberto conciliadas manualmente pelo financeiro (arredondamento de parcelas)
UPDATE installments
   SET amount = amount - 0.01
 WHERE id IN (
    SELECT max(p.id)
      FROM installments p
      JOIN invoices i ON i.id = p.invoice_id
     WHERE i.status = 'OPEN'
       AND i.order_id IN (SELECT id FROM orders WHERE total_amount::text LIKE '%.__' AND installments = 3)
     GROUP BY i.id
    HAVING count(*) = 3 AND bool_and(p.status = 'OPEN')
     ORDER BY max(p.id) DESC
     LIMIT 3);

-- Encargos recalculados na importação de pagamentos em atraso prolongado
UPDATE installments
   SET interest    = round(amount * 0.01 * extract(day FROM age(paid_at, due_date)) / 30.0, 2),
       paid_amount = amount + late_fee + round(amount * 0.01 * extract(day FROM age(paid_at, due_date)) / 30.0, 2)
 WHERE status = 'PAID'
   AND paid_at - due_date >= 31;

UPDATE payments p
   SET amount = i.paid_amount
  FROM installments i
 WHERE i.id = p.installment_id
   AND i.status = 'PAID'
   AND i.paid_at - i.due_date >= 31;

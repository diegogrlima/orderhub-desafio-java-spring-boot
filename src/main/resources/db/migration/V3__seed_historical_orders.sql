-- =====================================================================
-- OrderHub - carga de histórico (aprox. 18 meses de operação)
--
-- Gera pedidos, itens, faturas, parcelas e pagamentos de forma determinística
-- (setseed) e coerente com as regras de negócio vigentes:
--   * desconto por categoria do cliente sobre o subtotal;
--   * frete de R$ 49,90 quando o valor das mercadorias for menor que R$ 500,00;
--   * parcelas a cada 30 dias (ajustadas para o próximo dia útil),
--     centavos residuais lançados na 1ª parcela;
--   * multa de 2% + juros de 1% a.m. pro rata die para pagamentos em atraso.
--
-- Pedidos com mais de 120 dias usam o preço vigente antes do último reajuste
-- de tabela (aprox. 8% menor).
-- =====================================================================

CREATE OR REPLACE FUNCTION seed_order(
    p_customer_id    BIGINT,
    p_created        TIMESTAMP,
    p_status         VARCHAR,
    p_installments   INT,
    p_products       BIGINT[],
    p_qtys           INT[],
    p_old_prices     BOOLEAN,
    p_invoiced       BOOLEAN,
    p_reliable_payer BOOLEAN
) RETURNS BIGINT
LANGUAGE plpgsql AS $fn$
DECLARE
    v_order_id       BIGINT;
    v_tier           VARCHAR(20);
    v_price          NUMERIC(12,2);
    v_unit           NUMERIC(12,2);
    v_subtotal       NUMERIC(12,2) := 0;
    v_tier_disc      NUMERIC(12,2);
    v_net            NUMERIC(12,2);
    v_shipping       NUMERIC(12,2);
    v_total          NUMERIC(12,2);
    v_invoice_id     BIGINT;
    v_issue          DATE;
    v_base           NUMERIC(12,2);
    v_amount         NUMERIC(12,2);
    v_due            DATE;
    v_inst_status    VARCHAR(20);
    v_paid_at        DATE;
    v_fee            NUMERIC(12,2);
    v_interest       NUMERIC(12,2);
    v_installment_id BIGINT;
    v_unpaid_prob    DOUBLE PRECISION;
    v_methods        TEXT[] := ARRAY['PIX', 'BOLETO', 'TRANSFER'];
    j                INT;
    k                INT;
BEGIN
    SELECT tier INTO v_tier FROM customers WHERE id = p_customer_id;

    INSERT INTO orders (customer_id, status, subtotal, total_amount, installments, created_at)
    VALUES (p_customer_id, 'PENDING', 0, 0, p_installments, p_created)
    RETURNING id INTO v_order_id;

    FOR j IN 1 .. array_length(p_products, 1) LOOP
        SELECT price INTO v_price FROM products WHERE id = p_products[j];
        v_unit := CASE WHEN p_old_prices THEN round(v_price * 0.92, 2) ELSE v_price END;
        INSERT INTO order_items (order_id, product_id, quantity, unit_price, line_total)
        VALUES (v_order_id, p_products[j], p_qtys[j], v_unit, v_unit * p_qtys[j]);
        v_subtotal := v_subtotal + v_unit * p_qtys[j];
    END LOOP;

    v_tier_disc := round(v_subtotal * CASE v_tier WHEN 'SILVER' THEN 0.03
                                                  WHEN 'GOLD' THEN 0.05
                                                  WHEN 'PLATINUM' THEN 0.08
                                                  ELSE 0 END, 2);
    v_net      := v_subtotal - v_tier_disc;
    v_shipping := CASE WHEN v_net >= 500 THEN 0 ELSE 49.90 END;
    v_total    := v_net + v_shipping;

    UPDATE orders
       SET status        = p_status,
           subtotal      = v_subtotal,
           tier_discount = v_tier_disc,
           shipping_fee  = v_shipping,
           total_amount  = v_total,
           confirmed_at  = CASE WHEN p_invoiced THEN LEAST(p_created + interval '2 hours', localtimestamp) END,
           shipped_at    = CASE WHEN p_status IN ('SHIPPED', 'DELIVERED') THEN LEAST(p_created + interval '1 day', localtimestamp) END,
           delivered_at  = CASE WHEN p_status = 'DELIVERED' THEN LEAST(p_created + interval '3 days', localtimestamp) END,
           cancelled_at  = CASE WHEN p_status = 'CANCELLED' THEN LEAST(p_created + interval '5 hours', localtimestamp) END,
           cancellation_reason = CASE WHEN p_status = 'CANCELLED' THEN 'Solicitação do cliente' END
     WHERE id = v_order_id;

    IF NOT p_invoiced THEN
        RETURN v_order_id;
    END IF;

    v_issue := (p_created + interval '2 hours')::date;

    INSERT INTO invoices (number, order_id, customer_id, issue_date, total_amount, status, created_at)
    VALUES ('NF-' || to_char(v_issue, 'YYYY') || '-' || lpad(nextval('invoice_number_seq')::text, 6, '0'),
            v_order_id, p_customer_id, v_issue, v_total,
            CASE WHEN p_status = 'CANCELLED' THEN 'CANCELLED' ELSE 'OPEN' END,
            LEAST(p_created + interval '2 hours', localtimestamp))
    RETURNING id INTO v_invoice_id;

    v_base := trunc(v_total / p_installments, 2);

    FOR k IN 1 .. p_installments LOOP
        v_amount := CASE WHEN k = 1 THEN v_total - v_base * (p_installments - 1) ELSE v_base END;
        v_due := v_issue + 30 * k;
        IF extract(isodow FROM v_due) = 6 THEN
            v_due := v_due + 2;
        ELSIF extract(isodow FROM v_due) = 7 THEN
            v_due := v_due + 1;
        END IF;

        v_paid_at  := NULL;
        v_fee      := NULL;
        v_interest := NULL;

        IF p_status = 'CANCELLED' THEN
            v_inst_status := 'CANCELLED';
        ELSIF v_due < current_date THEN
            v_unpaid_prob := CASE WHEN p_reliable_payer THEN 0
                                  WHEN v_due < current_date - 90 THEN 0.005
                                  ELSE 0.08 END;
            IF random() >= v_unpaid_prob THEN
                v_inst_status := 'PAID';
                IF p_reliable_payer OR random() < 0.75 THEN
                    v_paid_at := v_due - floor(random() * 5)::int;
                ELSE
                    v_paid_at := LEAST(v_due + 1 + floor(random() * 45)::int, current_date);
                END IF;
            ELSE
                v_inst_status := 'OVERDUE';
            END IF;
        ELSE
            v_inst_status := 'OPEN';
        END IF;

        IF v_inst_status = 'PAID' THEN
            IF v_paid_at > v_due THEN
                v_fee      := round(v_amount * 0.02, 2);
                v_interest := round(v_amount * 0.01 * (v_paid_at - v_due) / 30.0, 2);
            ELSE
                v_fee      := 0;
                v_interest := 0;
            END IF;
        END IF;

        INSERT INTO installments (invoice_id, number, due_date, amount, status, paid_at, paid_amount, late_fee, interest)
        VALUES (v_invoice_id, k, v_due, v_amount, v_inst_status, v_paid_at,
                CASE WHEN v_inst_status = 'PAID' THEN v_amount + v_fee + v_interest END,
                v_fee, v_interest)
        RETURNING id INTO v_installment_id;

        IF v_inst_status = 'PAID' THEN
            INSERT INTO payments (installment_id, amount, payment_date, method, registered_by, created_at)
            VALUES (v_installment_id, v_amount + v_fee + v_interest, v_paid_at,
                    v_methods[1 + floor(random() * 3)::int], 'financeiro',
                    v_paid_at + interval '10 hours');
        END IF;
    END LOOP;

    UPDATE invoices i
       SET status = CASE
            WHEN i.status = 'CANCELLED' THEN 'CANCELLED'
            WHEN NOT EXISTS (SELECT 1 FROM installments x WHERE x.invoice_id = i.id AND x.status <> 'PAID') THEN 'PAID'
            WHEN EXISTS (SELECT 1 FROM installments x WHERE x.invoice_id = i.id AND x.status = 'OVERDUE') THEN 'OVERDUE'
            ELSE 'OPEN' END
     WHERE i.id = v_invoice_id;

    RETURN v_order_id;
END
$fn$;


DO $$
DECLARE
    v_day          INT;
    v_orders_day   INT;
    n              INT;
    v_customer_id  BIGINT;
    v_items        INT;
    v_products     BIGINT[];
    v_qtys         INT[];
    v_status       VARCHAR(20);
    v_installments INT;
    v_invoiced     BOOLEAN;
    v_created      TIMESTAMP;
    r              DOUBLE PRECISION;
BEGIN
    PERFORM setseed(0.2024);

    FOR v_day IN REVERSE 540 .. 0 LOOP
        v_orders_day := CASE WHEN extract(isodow FROM current_date - v_day) IN (6, 7)
                             THEN floor(random() * 4)::int
                             ELSE 8 + floor(random() * 8)::int END;

        FOR n IN 1 .. v_orders_day LOOP
            -- cliente 40 é novo (sem histórico); cliente 38 foi inativado há ~2 meses
            SELECT id INTO v_customer_id
              FROM customers
             WHERE id <> 40 AND (id <> 38 OR v_day > 60)
             ORDER BY random()
             LIMIT 1;

            v_items := 1 + floor(random() * 4)::int;

            -- produto 60 (lançamento) não possui histórico de vendas
            SELECT array_agg(s.id), array_agg(s.q) INTO v_products, v_qtys
              FROM (SELECT p.id, 1 + floor(random() * 12)::int AS q
                      FROM products p
                     WHERE p.id <> 60
                     ORDER BY random()
                     LIMIT v_items) s;

            r := random();
            v_installments := CASE WHEN r < 0.45 THEN 1 WHEN r < 0.70 THEN 2 WHEN r < 0.90 THEN 3 ELSE 4 END;

            r := random();
            IF v_day > 30 THEN
                v_status := CASE WHEN r < 0.92 THEN 'DELIVERED' ELSE 'CANCELLED' END;
            ELSIF v_day > 7 THEN
                v_status := CASE WHEN r < 0.55 THEN 'DELIVERED' WHEN r < 0.80 THEN 'SHIPPED'
                                 WHEN r < 0.95 THEN 'CONFIRMED' ELSE 'CANCELLED' END;
            ELSE
                v_status := CASE WHEN r < 0.40 THEN 'PENDING' WHEN r < 0.75 THEN 'CONFIRMED'
                                 WHEN r < 0.95 THEN 'SHIPPED' ELSE 'CANCELLED' END;
            END IF;

            v_invoiced := v_status IN ('CONFIRMED', 'SHIPPED', 'DELIVERED')
                          OR (v_status = 'CANCELLED' AND random() < 0.5);

            v_created := (current_date - v_day)
                         + make_interval(hours => 7 + floor(random() * 11)::int,
                                         mins  => floor(random() * 60)::int,
                                         secs  => floor(random() * 60)::int);
            IF v_created > localtimestamp THEN
                v_created := localtimestamp - interval '10 minutes';
            END IF;

            PERFORM seed_order(v_customer_id, v_created, v_status, v_installments,
                               v_products, v_qtys, v_day > 120, v_invoiced,
                               v_customer_id IN (5, 19, 33));
        END LOOP;
    END LOOP;

    -- Pedidos recentes contendo o produto em lançamento (MER-015, id 60)
    PERFORM seed_order(1,  (current_date - 2) + time '09:15', 'PENDING',   1,
                       ARRAY[60, 11]::BIGINT[],     ARRAY[10, 4],     FALSE, FALSE, FALSE);
    PERFORM seed_order(14, (current_date - 1) + time '14:40', 'CONFIRMED', 2,
                       ARRAY[60, 23, 31]::BIGINT[], ARRAY[6, 12, 8],  FALSE, TRUE,  FALSE);
    PERFORM seed_order(26, (current_date - 1) + time '16:05', 'PENDING',   1,
                       ARRAY[60]::BIGINT[],         ARRAY[8],         FALSE, FALSE, FALSE);
END
$$;

DROP FUNCTION seed_order(BIGINT, TIMESTAMP, VARCHAR, INT, BIGINT[], INT[], BOOLEAN, BOOLEAN, BOOLEAN);

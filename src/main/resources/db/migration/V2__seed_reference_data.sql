-- =====================================================================
-- OrderHub - dados de referência (usuários, categorias, produtos, clientes, cupons)
-- =====================================================================

-- Usuários internos (senhas em BCrypt; ver README)
INSERT INTO app_users (username, password_hash, full_name, role) VALUES
('admin',      '$2a$10$RiSouxxv1kL8v8htZUc2VOI9HmoeyjLq8ttVVmNaarbn062qCevTK', 'Administrador do Sistema', 'ADMIN'),
('vendas',     '$2a$10$hlATOkmjkEABzG9YVL91nuBeeCcfJSooc.6qsPMEtOtwzssoNJLjq', 'Equipe Comercial',         'SALES'),
('financeiro', '$2a$10$XxHbnQeemVeu1914HKRapO.jUojibU.uNy2XpCGvilNf6S/S5FTau', 'Equipe Financeira',        'FINANCE');

-- Categorias (ids 1..6)
INSERT INTO categories (name, description) VALUES
('Bebidas',    'Refrigerantes, águas, sucos, cervejas e energéticos'),
('Mercearia',  'Grãos, massas, enlatados, óleos e matinais'),
('Laticínios', 'Leites, queijos, manteigas e derivados'),
('Limpeza',    'Produtos de limpeza doméstica e profissional'),
('Higiene',    'Higiene pessoal e descartáveis'),
('Congelados', 'Congelados e resfriados');

-- Produtos (ids 1..60)
INSERT INTO products (sku, name, category_id, unit, price, stock_quantity, active, created_at, updated_at) VALUES
('BEB-001', 'Refrigerante Cola 2L (fardo c/ 6)',                1, 'FD',  47.90,  800, TRUE, now() - interval '600 days', now() - interval '100 days'),
('BEB-002', 'Refrigerante Guaraná 2L (fardo c/ 6)',             1, 'FD',  41.50,  750, TRUE, now() - interval '600 days', now() - interval '100 days'),
('BEB-003', 'Água Mineral sem Gás 500ml (fardo c/ 12)',         1, 'FD',  18.90, 1500, TRUE, now() - interval '600 days', now() - interval '100 days'),
('BEB-004', 'Água Mineral com Gás 500ml (fardo c/ 12)',         1, 'FD',  22.40,  600, TRUE, now() - interval '600 days', now() - interval '100 days'),
('BEB-005', 'Suco de Uva Integral 1L (cx c/ 6)',                1, 'CX',  89.70,  300, TRUE, now() - interval '600 days', now() - interval '100 days'),
('BEB-006', 'Suco de Laranja 1L (cx c/ 12)',                    1, 'CX',  76.80,  280, TRUE, now() - interval '600 days', now() - interval '100 days'),
('BEB-007', 'Chá Gelado Limão 1,5L (fardo c/ 6)',               1, 'FD',  35.40,  260, TRUE, now() - interval '600 days', now() - interval '100 days'),
('BEB-008', 'Energético 250ml (fardo c/ 24)',                   1, 'FD', 142.80,  180, TRUE, now() - interval '600 days', now() - interval '100 days'),
('BEB-009', 'Cerveja Pilsen Lata 350ml (fardo c/ 12)',          1, 'FD',  39.90,  900, TRUE, now() - interval '600 days', now() - interval '100 days'),
('BEB-010', 'Isotônico Laranja 500ml (fardo c/ 6)',             1, 'FD',  27.60,  340, TRUE, now() - interval '600 days', now() - interval '100 days'),
('MER-001', 'Arroz Branco Tipo 1 5kg (fardo c/ 6)',             2, 'FD', 159.90,  420, TRUE, now() - interval '600 days', now() - interval '100 days'),
('MER-002', 'Feijão Carioca 1kg (fardo c/ 10)',                 2, 'FD',  84.90,  500, TRUE, now() - interval '600 days', now() - interval '100 days'),
('MER-003', 'Açúcar Refinado 1kg (fardo c/ 10)',                2, 'FD',  46.50,  650, TRUE, now() - interval '600 days', now() - interval '100 days'),
('MER-004', 'Café Torrado e Moído 500g (cx c/ 10)',             2, 'CX', 172.00,  300, TRUE, now() - interval '600 days', now() - interval '100 days'),
('MER-005', 'Óleo de Soja 900ml (cx c/ 20)',                    2, 'CX', 154.00,  380, TRUE, now() - interval '600 days', now() - interval '100 days'),
('MER-006', 'Macarrão Espaguete 500g (fardo c/ 20)',            2, 'FD',  68.00,  450, TRUE, now() - interval '600 days', now() - interval '100 days'),
('MER-007', 'Farinha de Trigo 1kg (fardo c/ 10)',               2, 'FD',  52.90,  400, TRUE, now() - interval '600 days', now() - interval '100 days'),
('MER-008', 'Molho de Tomate Sachê 340g (cx c/ 24)',            2, 'CX',  57.60,  520, TRUE, now() - interval '600 days', now() - interval '100 days'),
('MER-009', 'Sal Refinado 1kg (fardo c/ 10)',                   2, 'FD',  21.90,  700, TRUE, now() - interval '600 days', now() - interval '100 days'),
('MER-010', 'Biscoito Cream Cracker 400g (cx c/ 20)',           2, 'CX',  98.00,  310, TRUE, now() - interval '600 days', now() - interval '100 days'),
('MER-011', 'Achocolatado em Pó 400g (cx c/ 12)',               2, 'CX', 106.80,  240, TRUE, now() - interval '600 days', now() - interval '100 days'),
('MER-012', 'Azeite Extra Virgem 500ml (cx c/ 12)',             2, 'CX', 358.80,  120, TRUE, now() - interval '600 days', now() - interval '100 days'),
('LAT-001', 'Leite UHT Integral 1L (cx c/ 12)',                 3, 'CX',  62.90,  900, TRUE, now() - interval '600 days', now() - interval '100 days'),
('LAT-002', 'Leite UHT Desnatado 1L (cx c/ 12)',                3, 'CX',  64.90,  500, TRUE, now() - interval '600 days', now() - interval '100 days'),
('LAT-003', 'Queijo Muçarela Fatiado 1kg',                      3, 'KG',  42.90,  200, TRUE, now() - interval '600 days', now() - interval '100 days'),
('LAT-004', 'Manteiga com Sal 200g (cx c/ 24)',                 3, 'CX', 259.20,  150, TRUE, now() - interval '600 days', now() - interval '100 days'),
('LAT-005', 'Iogurte Natural 170g (cx c/ 24)',                  3, 'CX',  69.60,  180, TRUE, now() - interval '600 days', now() - interval '100 days'),
('LAT-006', 'Requeijão Cremoso 200g (cx c/ 24)',                3, 'CX', 177.60,  160, TRUE, now() - interval '600 days', now() - interval '100 days'),
('LAT-007', 'Creme de Leite 200g (cx c/ 27)',                   3, 'CX',  91.80,  260, TRUE, now() - interval '600 days', now() - interval '100 days'),
('LAT-008', 'Leite Condensado 395g (cx c/ 27)',                 3, 'CX', 175.50,  220, TRUE, now() - interval '600 days', now() - interval '100 days'),
('LIM-001', 'Detergente Líquido 500ml (cx c/ 24)',              4, 'CX',  52.80,  600, TRUE, now() - interval '600 days', now() - interval '100 days'),
('LIM-002', 'Sabão em Pó 1kg (fardo c/ 10)',                    4, 'FD', 129.00,  300, TRUE, now() - interval '600 days', now() - interval '100 days'),
('LIM-003', 'Água Sanitária 2L (cx c/ 6)',                      4, 'CX',  31.20,  450, TRUE, now() - interval '600 days', now() - interval '100 days'),
('LIM-004', 'Desinfetante Lavanda 2L (cx c/ 6)',                4, 'CX',  44.40,  380, TRUE, now() - interval '600 days', now() - interval '100 days'),
('LIM-005', 'Esponja Multiuso (pct c/ 40)',                     4, 'UN',  38.00,  500, TRUE, now() - interval '600 days', now() - interval '100 days'),
('LIM-006', 'Saco de Lixo 100L (fardo c/ 100)',                 4, 'FD',  74.90,  260, TRUE, now() - interval '600 days', now() - interval '100 days'),
('LIM-007', 'Amaciante 2L (cx c/ 6)',                           4, 'CX',  71.40,  240, TRUE, now() - interval '600 days', now() - interval '100 days'),
('LIM-008', 'Limpador Multiuso 500ml (cx c/ 12)',               4, 'CX',  46.80,  330, TRUE, now() - interval '600 days', now() - interval '100 days'),
('HIG-001', 'Papel Higiênico Folha Dupla (fardo c/ 64 rolos)',  5, 'FD', 119.90,  400, TRUE, now() - interval '600 days', now() - interval '100 days'),
('HIG-002', 'Sabonete em Barra 85g (cx c/ 72)',                 5, 'CX', 136.80,  260, TRUE, now() - interval '600 days', now() - interval '100 days'),
('HIG-003', 'Creme Dental 90g (cx c/ 72)',                      5, 'CX', 223.20,  200, TRUE, now() - interval '600 days', now() - interval '100 days'),
('HIG-004', 'Shampoo 350ml (cx c/ 12)',                         5, 'CX', 118.80,  150, TRUE, now() - interval '600 days', now() - interval '100 days'),
('HIG-005', 'Desodorante Aerossol 150ml (cx c/ 12)',            5, 'CX', 155.40,  140, TRUE, now() - interval '600 days', now() - interval '100 days'),
('HIG-006', 'Papel Toalha (fardo c/ 12 rolos)',                 5, 'FD',  39.60,  320, TRUE, now() - interval '600 days', now() - interval '100 days'),
('HIG-007', 'Fralda Descartável M (fardo c/ 4 pct)',            5, 'FD', 189.90,  110, TRUE, now() - interval '600 days', now() - interval '100 days'),
('HIG-008', 'Absorvente com Abas (cx c/ 24 pct)',               5, 'CX',  96.00,  130, TRUE, now() - interval '600 days', now() - interval '100 days'),
('CON-001', 'Pão de Queijo Congelado 1kg (cx c/ 10)',           6, 'CX', 189.00,  140, TRUE, now() - interval '600 days', now() - interval '100 days'),
('CON-002', 'Batata Pré-Frita 2kg (cx c/ 6)',                   6, 'CX', 131.40,  160, TRUE, now() - interval '600 days', now() - interval '100 days'),
('CON-003', 'Hambúrguer Bovino 56g (cx c/ 36)',                 6, 'CX',  89.90,  200, TRUE, now() - interval '600 days', now() - interval '100 days'),
('CON-004', 'Lasanha Bolonhesa 600g (cx c/ 8)',                 6, 'CX', 127.20,   90, TRUE, now() - interval '600 days', now() - interval '100 days'),
('CON-005', 'Pizza Muçarela 460g (cx c/ 10)',                   6, 'CX', 159.00,  100, TRUE, now() - interval '600 days', now() - interval '100 days'),
('CON-006', 'Sorvete Creme 2L (cx c/ 4)',                       6, 'CX',  79.60,   85, TRUE, now() - interval '600 days', now() - interval '100 days'),
('CON-007', 'Peito de Frango Congelado 1kg (cx c/ 12)',         6, 'CX', 251.40,  120, TRUE, now() - interval '600 days', now() - interval '100 days'),
('CON-008', 'Polpa de Fruta Sortida 100g (cx c/ 50)',           6, 'CX',  87.50,  150, TRUE, now() - interval '600 days', now() - interval '100 days'),
('MER-013', 'Milho Verde em Conserva 170g (cx c/ 24)',          2, 'CX',  69.60,  300, TRUE, now() - interval '600 days', now() - interval '100 days'),
('MER-014', 'Atum Ralado 170g (cx c/ 24)',                      2, 'CX', 182.40,  140, TRUE, now() - interval '600 days', now() - interval '100 days'),
('LIM-009', 'Lustra Móveis 200ml (cx c/ 12)',                   4, 'CX',  75.60,   50, FALSE, now() - interval '600 days', now() - interval '45 days'),
('BEB-011', 'Kombucha Gengibre 355ml (cx c/ 12)',               1, 'CX', 118.80,    5, TRUE, now() - interval '400 days', now() - interval '100 days'),
('BEB-012', 'Água de Coco 1L (cx c/ 12)',                       1, 'CX', 106.80,    0, TRUE, now() - interval '400 days', now() - interval '100 days'),
('MER-015', 'Granola Tradicional 1kg (cx c/ 6) - LANÇAMENTO',   2, 'CX', 131.70,   60, TRUE, now() - interval '5 days',   now() - interval '5 days');

-- Clientes (ids 1..40). O CNPJ é fictício (formato válido, sem dígito verificador real).
-- O limite de crédito definitivo é calculado na V4 a partir do histórico.
INSERT INTO customers (legal_name, trade_name, document, email, phone, city, state, tier, credit_limit, billing_day, active, created_at, updated_at)
SELECT v.legal_name, v.trade_name,
       lpad((10234567 + v.n * 1379)::text, 8, '0') || '0001' || lpad(((v.n * 37) % 100)::text, 2, '0'),
       v.email, v.phone, v.city, v.state, v.tier, 0, v.billing_day, v.active,
       CASE WHEN v.n = 40 THEN now() - interval '3 days' ELSE now() - interval '600 days' END,
       CASE WHEN v.n = 40 THEN now() - interval '3 days' ELSE now() - interval '30 days' END
FROM (VALUES
 ( 1, 'Supermercado Bom Preço Ltda',                    'Bom Preço',          'compras@bompreco.com.br',        '1932345001', 'Campinas',               'SP', 'GOLD',     10,   TRUE),
 ( 2, 'Mercado São Jorge Ltda',                         'Mercado São Jorge',  'contato@mercadosaojorge.com.br', '1932345002', 'Campinas',               'SP', 'SILVER',   NULL, TRUE),
 ( 3, 'Mercadinho Estrela do Bairro ME',                'Estrela do Bairro',  'estreladobairro@email.com.br',   '1938765003', 'Valinhos',               'SP', 'STANDARD', NULL, TRUE),
 ( 4, 'Atacadão Paulista Comércio de Alimentos S.A.',   'Atacadão Paulista',  'suprimentos@atacadaopaulista.com.br','1130045004','São Paulo',          'SP', 'PLATINUM', 15,   TRUE),
 ( 5, 'Padaria e Confeitaria Pão Dourado Ltda',         'Pão Dourado',        'financeiro@paodourado.com.br',   '1145215005', 'Jundiaí',                'SP', 'SILVER',   5,    TRUE),
 ( 6, 'Restaurante Sabor Caseiro Ltda',                 'Sabor Caseiro',      'saborcaseiro@email.com.br',      '1532215006', 'Sorocaba',               'SP', 'STANDARD', NULL, TRUE),
 ( 7, 'Hotel Serra Azul Empreendimentos Ltda',          'Hotel Serra Azul',   'compras@hotelserraazul.com.br',  '1144115007', 'Atibaia',                'SP', 'GOLD',     20,   TRUE),
 ( 8, 'Empório Natural Vida Leve Eireli',               'Vida Leve',          'pedidos@vidaleve.com.br',        '1932415008', 'Campinas',               'SP', 'STANDARD', NULL, TRUE),
 ( 9, 'Rede Compre Bem Supermercados Ltda',             'Compre Bem',         'compras@comprebem.com.br',       '1934335009', 'Piracicaba',             'SP', 'PLATINUM', 10,   TRUE),
 (10, 'Lanchonete Ponto Certo ME',                      'Ponto Certo',        'pontocerto@email.com.br',        '1934615010', 'Americana',              'SP', 'STANDARD', NULL, TRUE),
 (11, 'MERCADO CENTRAL DE HORTOLÂNDIA LTDA',            'Mercado Central',    'adm@mercadocentralhort.com.br',  '1938095011', 'Hortolândia',            'SP', 'SILVER',   31,   TRUE),
 (12, 'Distribuidora Irmãos Rocha Ltda',                'Irmãos Rocha',       'financeiro@irmaosrocha.com.br',  '1934515012', 'Limeira',                'SP', 'GOLD',     30,   TRUE),
 (13, 'Cantina Escolar Saber Ltda',                     'Cantina Saber',      'cantinasaber@email.com.br',      '1938755013', 'Indaiatuba',             'SP', 'STANDARD', NULL, TRUE),
 (14, 'Supermercado Família Feliz Ltda',                'Família Feliz',      'compras@familiafeliz.com.br',    '1938835014', 'Sumaré',                 'SP', 'GOLD',     NULL, TRUE),
 (15, 'Bar e Restaurante Esquina do Chopp Ltda',        'Esquina do Chopp',   'esquinadochopp@email.com.br',    '1932525015', 'Campinas',               'SP', 'SILVER',   NULL, TRUE),
 (16, 'Minimercado Vila Nova ME',                       'Vila Nova',          'minimercadovilanova@email.com.br','1938745016','Paulínia',               'SP', 'STANDARD', 29,   TRUE),
 (17, 'Hospital Santa Clara Associação Beneficente',    'Hospital Santa Clara','suprimentos@hsantaclara.org.br','1937355017', 'Campinas',               'SP', 'PLATINUM', 25,   TRUE),
 (18, 'Conveniência Posto Rota 330 Ltda',               'Rota 330',           'rota330@email.com.br',           '1934665018', 'Nova Odessa',            'SP', 'STANDARD', NULL, TRUE),
 (19, 'Mercearia Dona Benta Ltda',                      'Dona Benta',         'donabenta@merceariadb.com.br',   '1145245019', 'Itatiba',                'SP', 'SILVER',   NULL, TRUE),
 (20, 'Supermercado Ideal de Mogi Ltda',                'Ideal Mogi',         'compras@idealmogi.com.br',       '1938045020', 'Mogi Mirim',             'SP', 'GOLD',     15,   TRUE),
 (21, 'Pizzaria Forno a Lenha Ltda',                    'Forno a Lenha',      'fornoalenha@email.com.br',       '1932565021', 'Campinas',               'SP', 'STANDARD', NULL, TRUE),
 (22, 'Restaurante Industrial Bom Apetite Ltda',        'Bom Apetite',        'adm@bomapetite.com.br',          '1938375022', 'Jaguariúna',             'SP', 'GOLD',     10,   TRUE),
 (23, 'Hipermercado Grande Oeste S.A.',                 'Grande Oeste',       'suprimentos@grandeoeste.com.br', '1532335023', 'Sorocaba',               'SP', 'PLATINUM', 5,    TRUE),
 (24, 'Mercado Popular Leste Ltda',                     'Popular Leste',      'popularleste@email.com.br',      '1120455024', 'São Paulo',              'SP', 'SILVER',   NULL, TRUE),
 (25, 'Doceria Delícias da Vovó ME',                    'Delícias da Vovó',   'deliciasdavovo@email.com.br',    '1938765025', 'Vinhedo',                'SP', 'STANDARD', NULL, TRUE),
 (26, 'Supermercado Ouro Verde Ltda',                   'Ouro Verde',         'compras@ouroverde.com.br',       '1932685026', 'Campinas',               'SP', 'GOLD',     20,   TRUE),
 (27, 'Clube Recreativo Primavera',                     'Clube Primavera',    'adm@clubeprimavera.org.br',      '1932515027', 'Campinas',               'SP', 'STANDARD', NULL, TRUE),
 (28, 'Açougue e Mercado Boi Bom Ltda',                 'Boi Bom',            'boibom@email.com.br',            '1938895028', 'Monte Mor',              'SP', 'SILVER',   NULL, TRUE),
 (29, 'Rede Farma Saúde Ltda',                          'Farma Saúde',        'compras@farmasaude.com.br',      '1932325029', 'Campinas',               'SP', 'SILVER',   10,   TRUE),
 (30, 'Mercado Recanto Feliz Ltda',                     'Recanto Feliz',      'recantofeliz@email.com.br',      '1934555030', 'Santa Bárbara d''Oeste', 'SP', 'STANDARD', NULL, TRUE),
 (31, 'Supermercado Avenida Ltda',                      'Avenida',            'compras@superavenida.com.br',    '1935335031', 'Rio Claro',              'SP', 'GOLD',     NULL, TRUE),
 (32, 'Restaurante Tempero Mineiro Ltda',               'Tempero Mineiro',    'temperomineiro@email.com.br',    '3534215032', 'Pouso Alegre',           'MG', 'STANDARD', NULL, TRUE),
 (33, 'Mercado Sul de Minas Ltda',                      'Sul de Minas',       'financeiro@mercadosulminas.com.br','3537225033','Poços de Caldas',       'MG', 'SILVER',   15,   TRUE),
 (34, 'Supermercado Litoral Ltda',                      'Litoral',            'compras@superlitoral.com.br',    '1332195034', 'Santos',                 'SP', 'GOLD',     NULL, TRUE),
 (35, 'Quiosque Praia Mansa ME',                        'Praia Mansa',        'praiamansa@email.com.br',        '1333555035', 'Guarujá',                'SP', 'STANDARD', NULL, TRUE),
 (36, 'Escola Pequeno Príncipe Ltda',                   'Pequeno Príncipe',   'adm@pequenoprincipe.edu.br',     '1932455036', 'Campinas',               'SP', 'STANDARD', NULL, TRUE),
 (37, 'Mercado Bom Jesus Ltda',                         'Bom Jesus',          'bomjesus@email.com.br',          '1140335037', 'Bragança Paulista',      'SP', 'SILVER',   NULL, TRUE),
 (38, 'Supermercado Estrela Dalva Ltda',                'Estrela Dalva',      'estreladalva@email.com.br',      '1932785038', 'Campinas',               'SP', 'STANDARD', NULL, FALSE),
 (39, 'Comercial de Alimentos Nova Era Ltda',           'Nova Era',           'compras@novaera.com.br',         '1140235039', 'Itu',                    'SP', 'SILVER',   NULL, TRUE),
 (40, 'Panificadora Trigo de Ouro Ltda',                'Trigo de Ouro',      'trigodeouro@email.com.br',       '1932995040', 'Campinas',               'SP', 'STANDARD', NULL, TRUE)
) AS v (n, legal_name, trade_name, email, phone, city, state, tier, billing_day, active)
ORDER BY v.n;

-- Cupons promocionais (datas relativas à data de execução da migration)
INSERT INTO coupons (code, description, discount_percent, valid_from, valid_until, max_uses, used_count, min_order_amount, active) VALUES
('BEMVINDO10',  'Boas-vindas para novos parceiros',          10.00, current_date - 60,  current_date + 120, 1000, 212,    0.00, TRUE),
('PRIMAVERA15', 'Campanha de primavera (pedido mínimo)',     15.00, current_date - 30,  current_date + 30,   300,  88,  800.00, TRUE),
('VIP20',       'Clientes VIP - lote limitado',              20.00, current_date - 90,  current_date + 90,    50,  50,    0.00, TRUE),
('QUEIMA5',     'Queima de estoque - termina hoje',           5.00, current_date - 15,  current_date,        500, 143,    0.00, TRUE),
('NATAL2025',   'Campanha de Natal 2025',                    12.00, current_date - 400, current_date - 330,  400, 377,    0.00, TRUE),
('PARCEIRO8',   'Programa de parceiros (suspenso)',           8.00, current_date - 200, current_date + 200,  999,  12,    0.00, FALSE);

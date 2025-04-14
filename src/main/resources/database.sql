-- Insertion des ingrédients
INSERT INTO ingredient (id, name, last_modified) VALUES
(1, 'Oeuf', '2025-01-01 00:00'),
(2, 'Saucisse', '2025-01-01 00:00'),
(3, 'Huile', '2025-01-01 00:00'),
(4, 'Pain', '2025-01-01 00:00');

-- Insertion des prix
INSERT INTO price (id, amount, date_value, id_ingredient) VALUES
(1, 1000, '2025-01-01', 1),
(2, 20, '2025-01-01', 2),
(3, 10000, '2025-01-01', 3),
(4, 1000, '2025-01-01', 4);

-- Insertion du plat
INSERT INTO dish (id, name, price) VALUES (1, 'Hot dog', 15000);

-- Composition du plat
INSERT INTO dish_ingredient (id, id_dish, id_ingredient, required_quantity, unit) VALUES
(1, 1, 1, 1, 'U'),
(2, 1, 2, 100, 'G'),
(3, 1, 3, 0.15, 'L'),
(4, 1, 4, 1, 'U');

-- Mouvements de stock initiaux
INSERT INTO stock_movement (id, quantity, unit, movement_type, creation_datetime, id_ingredient) VALUES
(1, 100, 'U', 'IN', '2025-02-01 08:00:00', 1),
(2, 50, 'U', 'IN', '2025-02-01 08:00:00', 4),
(3, 10000, 'G', 'IN', '2025-02-01 08:00:00', 2),
(4, 20, 'L', 'IN', '2025-02-01 08:00:00', 3);




-- Réinitialiser les séquences après insertion manuelle des ID
SELECT setval('ingredient_id_seq', (SELECT MAX(id) FROM ingredient));
SELECT setval('price_id_seq', (SELECT MAX(id) FROM price));
SELECT setval('dish_id_seq', (SELECT MAX(id) FROM dish));
SELECT setval('dish_ingredient_id_seq', (SELECT MAX(id) FROM dish_ingredient));
SELECT setval('stock_movement_id_seq', (SELECT MAX(id) FROM stock_movement));




-- Vérification du coût du Hot Dog (doit retourner 5500)
SELECT SUM(p.amount * di.required_quantity) AS cout_total
FROM dish_ingredient di
JOIN price p ON di.id_ingredient = p.id_ingredient
WHERE di.id_dish = 1 AND p.date_value = '2025-01-01';





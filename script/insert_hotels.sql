-- Lieux (locations)
INSERT INTO staging.lieux (code, libelle) VALUES
('AIR', 'Aéroport'),
('COL', 'Colbert'),
('NOV', 'Novotel'),
('IBI', 'Ibis'),
('LOK', 'Lokanga');

-- Hotels with lieu references
INSERT INTO staging.hotel (nom, id_lieu) VALUES
('Colbert', (SELECT id FROM staging.lieux WHERE code = 'COL')),
('Novotel', (SELECT id FROM staging.lieux WHERE code = 'NOV')),
('Ibis', (SELECT id FROM staging.lieux WHERE code = 'IBI')),
('Lokanga', (SELECT id FROM staging.lieux WHERE code = 'LOK'));

-- Distances (in km) - network with some indirect routes
-- Direct routes from airport
INSERT INTO staging.distance ("from", "to", distance, unite) VALUES
((SELECT id FROM staging.lieux WHERE code = 'AIR'), (SELECT id FROM staging.lieux WHERE code = 'COL'), 15.5, 'km'),
((SELECT id FROM staging.lieux WHERE code = 'AIR'), (SELECT id FROM staging.lieux WHERE code = 'NOV'), 12.3, 'km'),
-- No direct route from AIR to IBI - must go through NOV
-- No direct route from AIR to LOK - must go through intermediates

-- Routes between staging.hotels
((SELECT id FROM staging.lieux WHERE code = 'NOV'), (SELECT id FROM staging.lieux WHERE code = 'IBI'), 8.2, 'km'),
((SELECT id FROM staging.lieux WHERE code = 'NOV'), (SELECT id FROM staging.lieux WHERE code = 'LOK'), 14.5, 'km'),
((SELECT id FROM staging.lieux WHERE code = 'COL'), (SELECT id FROM staging.lieux WHERE code = 'IBI'), 10.0, 'km'),
((SELECT id FROM staging.lieux WHERE code = 'COL'), (SELECT id FROM staging.lieux WHERE code = 'LOK'), 12.0, 'km'),
((SELECT id FROM staging.lieux WHERE code = 'IBI'), (SELECT id FROM staging.lieux WHERE code = 'LOK'), 9.5, 'km');

-- Shortest paths (calculated by Dijkstra):
-- AIR -> COL: 15.5 km (direct)
-- AIR -> NOV: 12.3 km (direct)
-- AIR -> IBI: 20.5 km (AIR -> NOV -> IBI = 12.3 + 8.2)
-- AIR -> LOK: 26.8 km (AIR -> NOV -> LOK = 12.3 + 14.5)

-- Average speed parameter (km/h)
INSERT INTO staging.param (cle, valeur) VALUES
('vm', '60');

-- Vehicles (different capacities and fuel types for assignment testing)
INSERT INTO staging.vehicule (reference, place, type_carburant) VALUES
('VAN-15D', 15, 'D'),      -- Diesel van, 15 seats
('VAN-12D', 12, 'D'),      -- Diesel van, 12 seats
('MINIBUS-20E', 20, 'E'),  -- Electric minibus, 20 seats
('VAN-10D', 10, 'D'),      -- Diesel van, 10 seats
('CAR-7E', 7, 'E'),        -- Electric car, 7 seats
('VAN-8D', 8, 'D'),        -- Diesel van, 8 seats
('CAR-5D', 5, 'D');        -- Diesel car, 5 seats

INSERT INTO staging.reservation(id_client, nb_passager, date_heure_arrivee, id_hotel) VALUES
('4631', 11, '2026-02-05 00:01', 3),
('4394', 1, '2026-02-05 23:55', 3),
('8054', 2, '2026-02-09 10:17', 1),
('1432', 4, '2026-02-01 15:25', 2),
('7861', 4, '2026-01-28 07:11', 1),
('3308', 5, '2026-01-28 07:45', 1),
('4484', 13, '2026-02-28 08:25', 2),
('9687', 8, '2026-02-28 13:00', 2),
('6302', 7, '2026-02-15 13:00', 1),
('8640', 1, '2026-02-18 22:55', 4);

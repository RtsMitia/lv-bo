INSERT INTO dev.lieux (code, libelle) VALUES
('AIR', 'Aéroport'),
('COL', 'Colbert'),
('NOV', 'Novotel'),
('IBI', 'Ibis'),
('LOK', 'Lokanga');

INSERT INTO dev.hotel (nom, id_lieu) VALUES
('Colbert', (SELECT id FROM dev.lieux WHERE code = 'COL')),
('Novotel', (SELECT id FROM dev.lieux WHERE code = 'NOV')),
('Ibis', (SELECT id FROM dev.lieux WHERE code = 'IBI')),
('Lokanga', (SELECT id FROM dev.lieux WHERE code = 'LOK'));

INSERT INTO dev.distance ("from", "to", distance, unite) VALUES
-- AIR connections
((SELECT id FROM dev.lieux WHERE code = 'AIR'), (SELECT id FROM dev.lieux WHERE code = 'COL'), 15.5, 'km'),
((SELECT id FROM dev.lieux WHERE code = 'AIR'), (SELECT id FROM dev.lieux WHERE code = 'NOV'), 12.3, 'km'),
((SELECT id FROM dev.lieux WHERE code = 'AIR'), (SELECT id FROM dev.lieux WHERE code = 'IBI'), 18.0, 'km'),
((SELECT id FROM dev.lieux WHERE code = 'AIR'), (SELECT id FROM dev.lieux WHERE code = 'LOK'), 21.0, 'km'),
-- COL connections
((SELECT id FROM dev.lieux WHERE code = 'COL'), (SELECT id FROM dev.lieux WHERE code = 'NOV'), 13.5, 'km'),
((SELECT id FROM dev.lieux WHERE code = 'COL'), (SELECT id FROM dev.lieux WHERE code = 'IBI'), 10.0, 'km'),
((SELECT id FROM dev.lieux WHERE code = 'COL'), (SELECT id FROM dev.lieux WHERE code = 'LOK'), 12.0, 'km'),
-- NOV connections
((SELECT id FROM dev.lieux WHERE code = 'NOV'), (SELECT id FROM dev.lieux WHERE code = 'IBI'), 8.2, 'km'),
((SELECT id FROM dev.lieux WHERE code = 'NOV'), (SELECT id FROM dev.lieux WHERE code = 'LOK'), 14.5, 'km'),
-- IBI-LOK connection
((SELECT id FROM dev.lieux WHERE code = 'IBI'), (SELECT id FROM dev.lieux WHERE code = 'LOK'), 9.5, 'km');


INSERT INTO dev.param (cle, valeur) VALUES
('vm', '60'),
('ta', '30');

-- INSERT INTO dev.vehicule (reference, place, type_carburant) VALUES
-- ('CAR-5D',    5, 'D'),
-- ('CAR-5E',    5, 'E'),
-- ('VAN-8D',    8, 'D'),
-- ('VAN-10D',  10, 'D'),
-- ('VAN-12D',  12, 'D'),
-- ('BUS-15E',  15, 'E'),
-- ('BUS-20E',  20, 'E');

-- INSERT INTO dev.reservation(id_client, nb_passager, date_heure_arrivee, id_hotel) VALUES
-- ('A02', 3, '2026-03-10 08:00', 1),
-- ('A01', 2, '2026-03-10 08:00', 1),
-- ('B01', 4, '2026-03-10 08:00', 2),
-- ('B02', 3, '2026-03-10 08:00', 2),
-- ('C01', 6, '2026-03-10 08:00', 3),
-- ('C02', 4, '2026-03-10 08:00', 3);

-- INSERT INTO dev.reservation(id_client, nb_passager, date_heure_arrivee, id_hotel) VALUES
-- ('D01', 11, '2026-03-10 11:00', 4),
-- ('D02',  4, '2026-03-10 11:00', 4),
-- ('D03',  8, '2026-03-10 11:00', 4),
-- ('E01', 12, '2026-03-10 14:00', 1),
-- ('E02',  2, '2026-03-10 14:00', 1),
-- ('F01', 5, '2026-03-11 09:00', 2),
-- ('F02', 7, '2026-03-11 09:00', 2),
-- ('G01', 2, '2026-03-11 09:00', 1),
-- ('H01', 10, '2026-03-12 08:30', 3),
-- ('H02', 6,  '2026-03-12 08:30', 3);

-- INSERT INTO dev.reservation(id_client, nb_passager, date_heure_arrivee, id_hotel) VALUES
-- ('A02', 3, '2026-03-10 08:00', 1),
-- ('A01', 2, '2026-03-10 08:00', 1),
-- ('B01', 4, '2026-03-10 08:00', 2),
-- ('B02', 3, '2026-03-10 08:00', 2),
-- ('C01', 6, '2026-03-10 08:00', 3),
-- ('C02', 4, '2026-03-10 08:00', 3),
-- ('D01', 11, '2026-03-10 11:00', 4),
-- ('D02',  4, '2026-03-10 11:00', 4),
-- ('D03',  8, '2026-03-10 11:00', 4),
-- ('E01', 12, '2026-03-10 14:00', 1),
-- ('E02',  2, '2026-03-10 14:00', 1);

INSERT INTO dev.vehicule (reference, place, type_carburant) VALUES
('CAR-5D', 10, 'D'),
('CAR-5E', 9, 'E');

INSERT INTO dev.reservation(id_client, nb_passager, date_heure_arrivee, id_hotel) VALUES
('A02', 5, '2026-03-10 08:00', 1),
('A01', 3, '2026-03-10 08:15', 2),
('B01', 3, '2026-03-10 08:45', 2),
('A03', 8, '2026-03-10 09:00', 1);


-- SPRINT 8

INSERT INTO dev.vehicule (reference, place, type_carburant) VALUES
('CAR-5D', 5, 'D'),
('CAR-5E', 5, 'E'),
('VAN-8D', 8, 'D'),
('VAN-10E', 10, 'E');

INSERT INTO dev.reservation(id_client, nb_passager, date_heure_arrivee, id_hotel) VALUES
-- GROUPE 1 (08:00 → 08:30)
('R1', 6, '2026-03-10 08:00', 1),
('R2', 3, '2026-03-10 08:05', 2),
('R3', 5, '2026-03-10 08:10', 3),
('R4', 2, '2026-03-10 08:20', 1),
-- GROUPE 2 (08:30 → 09:00)
('R5', 7, '2026-03-10 08:35', 2),
('R6', 4, '2026-03-10 08:40', 3),
('R7', 3, '2026-03-10 08:50', 1),
-- GROUPE 3 (09:00 → 09:30)
('R8', 9, '2026-03-10 09:00', 2),
('R9', 2, '2026-03-10 09:10', 3),
-- CAS CRITIQUE (force resaNA)
('R10', 12, '2026-03-10 09:15', 1);
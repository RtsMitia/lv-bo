INSERT INTO staging.lieux (code, libelle) VALUES
('AIR', 'Aéroport'),
('COL', 'Colbert'),
('NOV', 'Novotel'),
('IBI', 'Ibis'),
('LOK', 'Lokanga');

INSERT INTO staging.hotel (nom, id_lieu) VALUES
('Colbert', (SELECT id FROM staging.lieux WHERE code = 'COL')),
('Novotel', (SELECT id FROM staging.lieux WHERE code = 'NOV')),
('Ibis', (SELECT id FROM staging.lieux WHERE code = 'IBI')),
('Lokanga', (SELECT id FROM staging.lieux WHERE code = 'LOK'));

INSERT INTO staging.distance ("from", "to", distance, unite) VALUES
-- AIR connections
((SELECT id FROM staging.lieux WHERE code = 'AIR'), (SELECT id FROM staging.lieux WHERE code = 'COL'), 15.5, 'km'),
((SELECT id FROM staging.lieux WHERE code = 'AIR'), (SELECT id FROM staging.lieux WHERE code = 'NOV'), 12.3, 'km'),
((SELECT id FROM staging.lieux WHERE code = 'AIR'), (SELECT id FROM staging.lieux WHERE code = 'IBI'), 18.0, 'km'),
((SELECT id FROM staging.lieux WHERE code = 'AIR'), (SELECT id FROM staging.lieux WHERE code = 'LOK'), 21.0, 'km'),
-- COL connections
((SELECT id FROM staging.lieux WHERE code = 'COL'), (SELECT id FROM staging.lieux WHERE code = 'NOV'), 13.5, 'km'),
((SELECT id FROM staging.lieux WHERE code = 'COL'), (SELECT id FROM staging.lieux WHERE code = 'IBI'), 10.0, 'km'),
((SELECT id FROM staging.lieux WHERE code = 'COL'), (SELECT id FROM staging.lieux WHERE code = 'LOK'), 12.0, 'km'),
-- NOV connections
((SELECT id FROM staging.lieux WHERE code = 'NOV'), (SELECT id FROM staging.lieux WHERE code = 'IBI'), 8.2, 'km'),
((SELECT id FROM staging.lieux WHERE code = 'NOV'), (SELECT id FROM staging.lieux WHERE code = 'LOK'), 14.5, 'km'),
-- IBI-LOK connection
((SELECT id FROM staging.lieux WHERE code = 'IBI'), (SELECT id FROM staging.lieux WHERE code = 'LOK'), 9.5, 'km');


INSERT INTO staging.param (cle, valeur) VALUES
('vm', '50'),
('ta', '30');

-- INSERT INTO staging.vehicule (reference, place, type_carburant) VALUES
-- ('CAR-5D',    5, 'D'),
-- ('CAR-5E',    5, 'E'),
-- ('VAN-8D',    8, 'D'),
-- ('VAN-10D',  10, 'D'),
-- ('VAN-12D',  12, 'D'),
-- ('BUS-15E',  15, 'E'),
-- ('BUS-20E',  20, 'E');

-- INSERT INTO staging.reservation(id_client, nb_passager, date_heure_arrivee, id_hotel) VALUES
-- ('A02', 3, '2026-03-10 08:00', 1),
-- ('A01', 2, '2026-03-10 08:00', 1),
-- ('B01', 4, '2026-03-10 08:00', 2),
-- ('B02', 3, '2026-03-10 08:00', 2),
-- ('C01', 6, '2026-03-10 08:00', 3),
-- ('C02', 4, '2026-03-10 08:00', 3);

-- INSERT INTO staging.reservation(id_client, nb_passager, date_heure_arrivee, id_hotel) VALUES
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

-- INSERT INTO staging.reservation(id_client, nb_passager, date_heure_arrivee, id_hotel) VALUES
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

-- INSERT INTO staging.vehicule (reference, place, type_carburant) VALUES
-- ('CAR-5D', 10, 'D'),
-- ('CAR-5E', 9, 'E');

-- INSERT INTO staging.reservation(id_client, nb_passager, date_heure_arrivee, id_hotel) VALUES
-- ('A02', 5, '2026-03-10 08:00', 1),
-- ('A01', 3, '2026-03-10 08:15', 2),
-- ('B01', 3, '2026-03-10 08:45', 2),
-- ('A03', 8, '2026-03-10 09:00', 1);

INSERT INTO staging.vehicule (reference, place, type_carburant) VALUES
('VAN-8D-A', 8, 'D'),
('VAN-8E-B', 8, 'E'),
('VAN-10D', 10, 'D');

INSERT INTO staging.reservation(id_client, nb_passager, date_heure_arrivee, id_hotel) VALUES
-- 08:00 -> devrait prendre un 8 places (capacite la plus proche), pas le 10 places
('T01', 7, '2026-03-10 08:00', (SELECT id FROM staging.hotel WHERE nom = 'Lokanga')),
-- 08:05 -> 2eme reservation quasi simultanee, prend l'autre 8 places
('T02', 9, '2026-03-10 08:05', (SELECT id FROM staging.hotel WHERE nom = 'Ibis')),
-- 08:20 -> meme fenetre ta=30, les deux 8 places peuvent etre occupes selon retour -> test contrainte trajet
-- ('T03', 7, '2026-03-10 08:20', (SELECT id FROM staging.hotel WHERE nom = 'Lokanga')),
-- 11:00 -> les vehicules sont revenus, re-test du best-fit sur un nouveau slot
('T04', 7, '2026-03-10 11:00', (SELECT id FROM staging.hotel WHERE nom = 'Novotel')),
-- 11:00 -> petit groupe pour verifier que l'assignation remplit le meme vehicule si place restante
('T05', 1, '2026-03-10 11:00', (SELECT id FROM staging.hotel WHERE nom = 'Colbert'));

INSERT INTO dev.lieux (code, libelle) VALUES
('AIR', 'Aeroport'),
('H1', 'Hotel1'),
('H2', 'Hotel2');
-- ('NOV', 'Novotel'),
-- ('IBI', 'Ibis'),
-- ('LOK', 'Lokanga');

INSERT INTO dev.hotel (nom, id_lieu) VALUES
('Hotel1', (SELECT id FROM dev.lieux WHERE code = 'H1')),
('Hotel2', (SELECT id FROM dev.lieux WHERE code = 'H2'));
-- ('Ibis', (SELECT id FROM dev.lieux WHERE code = 'IBI')),
-- ('Lokanga', (SELECT id FROM dev.lieux WHERE code = 'LOK'));

INSERT INTO dev.distance ("from", "to", distance, unite) VALUES
((SELECT id FROM dev.lieux WHERE code = 'AIR'), (SELECT id FROM dev.lieux WHERE code = 'H1'), 90, 'km'),
((SELECT id FROM dev.lieux WHERE code = 'AIR'), (SELECT id FROM dev.lieux WHERE code = 'H2'), 35, 'km'),
((SELECT id FROM dev.lieux WHERE code = 'H1'), (SELECT id FROM dev.lieux WHERE code = 'H2'), 60, 'km');


INSERT INTO dev.param (cle, valeur) VALUES
('vm', '50'),
('ta', '30');


INSERT INTO dev.vehicule (reference, place, type_carburant, heure_disponibilite) VALUES
('vehicule1', 5, 'D', '09:00:00'),
('vehicule2', 5, 'E', '09:00:00'),
('vehicule3', 12, 'D', '08:00:00'),
('vehicule4', 9, 'D', '09:00:00'),
('vehicule5', 12, 'E', '13:00:00');

INSERT INTO dev.reservation(id_client, nb_passager, date_heure_arrivee, id_hotel) VALUES
('CL1', 7, '2026-03-19 09:00', 1),
('CL2', 20, '2026-03-19 08:00', 2),
('CL3', 3, '2026-03-19 09:10', 1),
('CL4', 10, '2026-03-19 09:15', 1),
('CL5', 5, '2026-03-19 09:20', 1),
('CL6', 12, '2026-03-19 13:30', 1);
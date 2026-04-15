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
((SELECT id FROM dev.lieux WHERE code = 'AIR'), (SELECT id FROM dev.lieux WHERE code = 'H1'), 50, 'km'),
((SELECT id FROM dev.lieux WHERE code = 'AIR'), (SELECT id FROM dev.lieux WHERE code = 'H2'), 35, 'km'),
((SELECT id FROM dev.lieux WHERE code = 'H1'), (SELECT id FROM dev.lieux WHERE code = 'H2'), 60, 'km');


INSERT INTO dev.param (cle, valeur) VALUES
('vm', '50'),
('ta', '30');


INSERT INTO dev.vehicule (reference, place, type_carburant, heure_disponibilite) VALUES
('vehicule1', 20, 'D', '08:00:00');

INSERT INTO dev.reservation(id_client, nb_passager, date_heure_arrivee, id_hotel) VALUES
('CL1', 10, '2026-03-25 08:20', 1),
('CL2', 10, '2026-03-25 08:40', 1);

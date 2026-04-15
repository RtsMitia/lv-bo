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
((SELECT id FROM dev.lieux WHERE code = 'AIR'), (SELECT id FROM dev.lieux WHERE code = 'H2'), 65, 'km'),
((SELECT id FROM dev.lieux WHERE code = 'H1'), (SELECT id FROM dev.lieux WHERE code = 'H2'), 10, 'km');


INSERT INTO dev.param (cle, valeur) VALUES
('vm', '60'),
('ta', '30');


INSERT INTO dev.vehicule (reference, place, type_carburant, heure_disponibilite) VALUES
('vehicule1', 10, 'D', '00:00:00'),
('vehicule2', 8, 'D', '08:00:00'),
('vehicule3', 8, 'E', '08:00:00'),
('vehicule4', 12, 'E', '09:00:00');

INSERT INTO dev.reservation(id_client, nb_passager, date_heure_arrivee, id_hotel) VALUES
('CL1', 20, '2026-04-02 06:00', 1),
('CL2', 6, '2026-04-02 08:15', 1),
('CL3', 10, '2026-04-02 09:00', 1),
('CL4', 6, '2026-04-02 09:10', 2);
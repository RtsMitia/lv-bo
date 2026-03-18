SET search_path TO dev;

TRUNCATE TABLE assignation_detail, assignation, reservation, vehicule, distance, hotel, lieux, param RESTART IDENTITY CASCADE;

INSERT INTO param (cle, valeur) VALUES
('ta', '30'),
('vm', '60');

INSERT INTO lieux (code, libelle) VALUES
('AIR', 'Aeroport'),
('H1', 'Hotel One Place'),
('H2', 'Hotel Two Place');

INSERT INTO hotel (nom, id_lieu) VALUES
('Hotel One', (SELECT id FROM lieux WHERE code = 'H1')),
('Hotel Two', (SELECT id FROM lieux WHERE code = 'H2'));

INSERT INTO distance ("from", "to", distance, unite) VALUES
((SELECT id FROM lieux WHERE code = 'AIR'), (SELECT id FROM lieux WHERE code = 'H1'), 10.0, 'km'),
((SELECT id FROM lieux WHERE code = 'AIR'), (SELECT id FROM lieux WHERE code = 'H2'), 20.0, 'km'),
((SELECT id FROM lieux WHERE code = 'H1'), (SELECT id FROM lieux WHERE code = 'H2'), 8.0, 'km');

INSERT INTO vehicule (reference, place, type_carburant) VALUES
('S5-B-V1', 5, 'D'),
('S5-B-V2', 4, 'E');

INSERT INTO reservation (id_client, nb_passager, date_heure_arrivee, id_hotel) VALUES
('B01', 4, '2026-06-02 08:30:00', (SELECT id FROM hotel WHERE nom = 'Hotel One')),
('B02', 4, '2026-06-02 08:45:00', (SELECT id FROM hotel WHERE nom = 'Hotel Two')),
('B03', 6, '2026-06-02 08:48:00', (SELECT id FROM hotel WHERE nom = 'Hotel One'));

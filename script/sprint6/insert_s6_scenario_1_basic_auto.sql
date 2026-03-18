-- Sprint 6 - Scenario 1
-- Basic automatic assignation (no manual assignation inserts)
-- Expected: 3 reservations in one group [08:30, 09:00], two assignations at 08:48.

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
((SELECT id FROM lieux WHERE code = 'AIR'), (SELECT id FROM lieux WHERE code = 'H2'), 15.0, 'km'),
((SELECT id FROM lieux WHERE code = 'H1'), (SELECT id FROM lieux WHERE code = 'H2'), 6.0, 'km');

INSERT INTO vehicule (reference, place, type_carburant) VALUES
('S6-A-V1', 6, 'D'),
('S6-A-V2', 6, 'E');

INSERT INTO reservation (id_client, nb_passager, date_heure_arrivee, id_hotel) VALUES
('S601', 4, '2026-07-01 08:30:00', (SELECT id FROM hotel WHERE nom = 'Hotel One')),
('S602', 2, '2026-07-01 08:45:00', (SELECT id FROM hotel WHERE nom = 'Hotel Two')),
('S603', 6, '2026-07-01 08:48:00', (SELECT id FROM hotel WHERE nom = 'Hotel One'));

-- Run auto assignation for date: 2026-07-01
-- Verify:
-- SELECT a.id, v.reference, a.depart_aeroport
-- FROM assignation a JOIN vehicule v ON v.id = a.vehicule
-- WHERE CAST(a.depart_aeroport AS DATE) = '2026-07-01'
-- ORDER BY a.depart_aeroport, a.id;
-- Expected: 2 rows, both depart at 08:48:00.

-- SELECT r.id_client
-- FROM reservation r
-- WHERE CAST(r.date_heure_arrivee AS DATE) = '2026-07-01'
--   AND r.id NOT IN (SELECT ad.id_reservation FROM assignation_detail ad);
-- Expected: 0 rows.

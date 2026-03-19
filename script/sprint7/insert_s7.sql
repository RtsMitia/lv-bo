-- Sprint 6 - Scenario 3
-- Rule test: same capacity -> choose vehicle with fewer trips in the day,
-- then fuel type only if trip counts are equal.
-- No manual assignation inserts.

SET search_path TO dev;

TRUNCATE TABLE assignation_detail, assignation, reservation, vehicule, distance, hotel, lieux, param RESTART IDENTITY CASCADE;

INSERT INTO param (cle, valeur) VALUES
('ta', '30'),
('vm', '60');

INSERT INTO lieux (code, libelle) VALUES
('AIR', 'Aeroport'),
('H1', 'Hotel One Place');

INSERT INTO hotel (nom, id_lieu) VALUES
('Hotel One', (SELECT id FROM lieux WHERE code = 'H1'));

-- Short distance so early trip returns quickly and both vehicles are free later.
INSERT INTO distance ("from", "to", distance, unite) VALUES
((SELECT id FROM lieux WHERE code = 'AIR'), (SELECT id FROM lieux WHERE code = 'H1'), 10.0, 'km');

-- Same capacity vehicles; diesel would normally win fuel tie-break.
INSERT INTO vehicule (reference, place, type_carburant) VALUES
('S6-C-V1', 6, 'D'),
('S6-C-V2', 6, 'E');

-- Early reservation at 08:00 creates first trip (expected on diesel V1 by current tie-break baseline)
INSERT INTO reservation (id_client, nb_passager, date_heure_arrivee, id_hotel) VALUES
('S6C0', 10, '2026-07-03 08:00:00', (SELECT id FROM hotel WHERE nom = 'Hotel One'));

-- Later reservation at 10:00 should pick vehicle with fewer trips (V2),
-- even though V1 is diesel.
INSERT INTO reservation (id_client, nb_passager, date_heure_arrivee, id_hotel) VALUES
('S6C1', 4, '2026-07-03 08:10:00', (SELECT id FROM hotel WHERE nom = 'Hotel One'));

INSERT INTO reservation (id_client, nb_passager, date_heure_arrivee, id_hotel) VALUES
('S6C2', 4, '2026-07-03 09:10:00', (SELECT id FROM hotel WHERE nom = 'Hotel One'));

-- Run auto assignation for date: 2026-07-03
-- Verify mapping:
-- SELECT r.id_client, a.depart_aeroport, v.reference, v.type_carburant
-- FROM reservation r
-- JOIN assignation_detail ad ON ad.id_reservation = r.id
-- JOIN assignation a ON a.id = ad.id_association
-- JOIN vehicule v ON v.id = a.vehicule
-- WHERE r.id_client IN ('S6C0','S6C1')
-- ORDER BY r.id_client;
-- Expected:
-- S6C0 -> typically S6-C-V1 at 08:00
-- S6C1 -> MUST be S6-C-V2 at 10:00 (fewer trips rule before carburant)

-- Optional count check:
-- SELECT v.reference, COUNT(*) AS nb_trajets
-- FROM assignation a
-- JOIN vehicule v ON v.id = a.vehicule
-- WHERE CAST(a.depart_aeroport AS DATE) = '2026-07-03'
-- GROUP BY v.reference
-- ORDER BY v.reference;

-- Scenario C
-- Test: vehicle returning inside [08:30, 09:00] becomes available,
-- and group departure shifts to 08:50 (instead of 08:48).
--
-- This scenario creates the running trip automatically (no manual assignation insert)
-- on the same date (2026-06-02). Return timestamp is consistent with the app formula:
-- retour = depart + (trajet_total_distance / vm)
-- With vm=60 and total distance 530 km, duration is 530 minutes (8h50).

SET search_path TO dev;

TRUNCATE TABLE assignation_detail, assignation, reservation, vehicule, distance, hotel, lieux, param RESTART IDENTITY CASCADE;

INSERT INTO param (cle, valeur) VALUES
('ta', '30'),
('vm', '60');

INSERT INTO lieux (code, libelle) VALUES
('AIR', 'Aeroport'),
('H1', 'Hotel One Place'),
('H2', 'Hotel Two Place'),
('FAR', 'Far Place');

INSERT INTO hotel (nom, id_lieu) VALUES
('Hotel One', (SELECT id FROM lieux WHERE code = 'H1')),
('Hotel Two', (SELECT id FROM lieux WHERE code = 'H2')),
('Hotel Far', (SELECT id FROM lieux WHERE code = 'FAR'));

INSERT INTO distance ("from", "to", distance, unite) VALUES
((SELECT id FROM lieux WHERE code = 'AIR'), (SELECT id FROM lieux WHERE code = 'H1'), 10.0, 'km'),
((SELECT id FROM lieux WHERE code = 'AIR'), (SELECT id FROM lieux WHERE code = 'H2'), 20.0, 'km'),
((SELECT id FROM lieux WHERE code = 'H1'), (SELECT id FROM lieux WHERE code = 'H2'), 8.0, 'km'),
((SELECT id FROM lieux WHERE code = 'AIR'), (SELECT id FROM lieux WHERE code = 'FAR'), 265.0, 'km');

INSERT INTO vehicule (reference, place, type_carburant) VALUES
('S5-C-V1', 7, 'D'),
('S5-C-V2', 6, 'D');

-- Reservation used by the running trip (same day, early group)
INSERT INTO reservation (id_client, nb_passager, date_heure_arrivee, id_hotel) VALUES
('C00', 6, '2026-06-02 00:00:00', (SELECT id FROM hotel WHERE nom = 'Hotel Far'));

-- Target reservations (same group 08:30..09:00)
INSERT INTO reservation (id_client, nb_passager, date_heure_arrivee, id_hotel) VALUES
('C01', 4, '2026-06-02 08:30:00', (SELECT id FROM hotel WHERE nom = 'Hotel One')),
('C02', 2, '2026-06-02 08:45:00', (SELECT id FROM hotel WHERE nom = 'Hotel Two')),
('C03', 6, '2026-06-02 08:48:00', (SELECT id FROM hotel WHERE nom = 'Hotel One'));

-- Run automatic assignation in app for date: 2026-06-02
-- Expected:
-- 1) C00 auto-creates an early running trip at 00:00 on S5-C-V2 with retour at 08:50.
-- 1) Target reservations C01/C02/C03 are all assigned.
-- 2) Target assignations depart at 08:50:00 (not 08:48:00).
-- 3) Target group uses 2 assignations.

-- Verify 1: Early running trip return time
-- SELECT a.id, v.reference, a.depart_aeroport, a.retour_aeroport
-- FROM assignation a
-- JOIN vehicule v ON v.id = a.vehicule
-- JOIN assignation_detail ad ON ad.id_association = a.id
-- JOIN reservation r ON r.id = ad.id_reservation
-- WHERE r.id_client = 'C00';
-- Expected: depart_aeroport = '2026-06-02 00:00:00' and retour_aeroport = '2026-06-02 08:50:00'.

-- Verify 2: Departures for C01/C02/C03
-- SELECT DISTINCT a.id, v.reference, a.depart_aeroport
-- FROM assignation a
-- JOIN vehicule v ON v.id = a.vehicule
-- JOIN assignation_detail ad ON ad.id_association = a.id
-- JOIN reservation r ON r.id = ad.id_reservation
-- WHERE r.id_client IN ('C01','C02','C03')
-- ORDER BY a.depart_aeroport, a.id;
-- Expected: 2 rows, both with depart_aeroport = '2026-06-02 08:50:00'.

-- Verify 3: Unassigned target reservations
-- SELECT r.id_client, r.nb_passager, r.date_heure_arrivee
-- FROM reservation r
-- WHERE CAST(r.date_heure_arrivee AS DATE) = '2026-06-02'
--   AND r.id_client IN ('C01','C02','C03')
--   AND r.id NOT IN (SELECT ad.id_reservation FROM assignation_detail ad)
-- ORDER BY r.date_heure_arrivee;
-- Expected: 0 rows.

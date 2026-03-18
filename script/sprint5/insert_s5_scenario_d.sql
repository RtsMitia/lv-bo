-- Scenario D
-- Test: reservation at 08:48 is deferred to the next group, then assigned there.
--
-- Setup intent:
-- - Group 1 window: [08:30, 09:00] with reservations at 08:30, 08:45, 08:48.
-- - Vehicle S5-D-V2 is made busy until 09:05 by an automatically created early trip.
-- - First window cannot assign all 3, so 08:48 is deferred.
-- - First subset (08:30 + 08:45) is assigned with departure 08:45.
-- - There is another group [09:15, 09:45]. Deferred 08:48 is carried into this group.
-- - Deferred 08:48 is then assigned with the 09:15/09:45 group.
--
-- Return time consistency note (same formula as app):
-- vm=60, total distance 545 km => 545 minutes (9h05)
-- depart 2026-06-04 00:00 + 9h05 = 2026-06-04 09:05.

SET search_path TO dev;

TRUNCATE TABLE assignation_detail, assignation, reservation, vehicule, distance, hotel, lieux, param RESTART IDENTITY CASCADE;

INSERT INTO param (cle, valeur) VALUES
('ta', '30'),
('vm', '60');

INSERT INTO lieux (code, libelle) VALUES
('AIR', 'Aeroport'),
('H1', 'Hotel One Place'),
('H2', 'Hotel Two Place'),
('FAR2', 'Far Two Place');

INSERT INTO hotel (nom, id_lieu) VALUES
('Hotel One', (SELECT id FROM lieux WHERE code = 'H1')),
('Hotel Two', (SELECT id FROM lieux WHERE code = 'H2')),
('Hotel Far Two', (SELECT id FROM lieux WHERE code = 'FAR2'));

INSERT INTO distance ("from", "to", distance, unite) VALUES
((SELECT id FROM lieux WHERE code = 'AIR'), (SELECT id FROM lieux WHERE code = 'H1'), 10.0, 'km'),
((SELECT id FROM lieux WHERE code = 'AIR'), (SELECT id FROM lieux WHERE code = 'H2'), 12.0, 'km'),
((SELECT id FROM lieux WHERE code = 'H1'), (SELECT id FROM lieux WHERE code = 'H2'), 5.0, 'km'),
((SELECT id FROM lieux WHERE code = 'AIR'), (SELECT id FROM lieux WHERE code = 'FAR2'), 272.5, 'km');

INSERT INTO vehicule (reference, place, type_carburant) VALUES
('S5-D-V1', 8, 'D'),
('S5-D-V2', 6, 'D');

-- Reservation used by the auto-created blocker trip (same day, early group)
INSERT INTO reservation (id_client, nb_passager, date_heure_arrivee, id_hotel) VALUES
('D00', 6, '2026-06-04 00:00:00', (SELECT id FROM hotel WHERE nom = 'Hotel Far Two'));

-- Target reservations
INSERT INTO reservation (id_client, nb_passager, date_heure_arrivee, id_hotel) VALUES
('D01', 4, '2026-06-04 08:30:00', (SELECT id FROM hotel WHERE nom = 'Hotel One')),
('D02', 4, '2026-06-04 08:45:00', (SELECT id FROM hotel WHERE nom = 'Hotel One')),
('D03', 6, '2026-06-04 08:48:00', (SELECT id FROM hotel WHERE nom = 'Hotel Two')),
('D04', 1, '2026-06-04 09:15:00', (SELECT id FROM hotel WHERE nom = 'Hotel One')),
('D05', 1, '2026-06-04 09:45:00', (SELECT id FROM hotel WHERE nom = 'Hotel Two'));

-- Run automatic assignation in app for date: 2026-06-04
-- Expected:
-- 1) D00 auto-creates a trip on S5-D-V2 with retour at 09:05.
-- 1) D01 + D02 assigned in first group with departure 08:45:00.
-- 2) D03 deferred from the first group and carried to next group [09:15, 09:45].
-- 3) D03, D04 and D05 are assigned in that next group at 09:15:00.
-- 4) All target reservations D01..D05 are assigned.
-- 5) For target reservations, departures are 08:45 and 09:15.

-- Verify 1: Auto-created blocker trip return time
-- SELECT a.id, v.reference, a.depart_aeroport, a.retour_aeroport
-- FROM assignation a
-- JOIN vehicule v ON v.id = a.vehicule
-- JOIN assignation_detail ad ON ad.id_association = a.id
-- JOIN reservation r ON r.id = ad.id_reservation
-- WHERE r.id_client = 'D00';
-- Expected: depart_aeroport = '2026-06-04 00:00:00' and retour_aeroport = '2026-06-04 09:05:00'.

-- Verify 2: Assignation departures for target reservations only
-- SELECT DISTINCT a.id, v.reference, a.depart_aeroport
-- FROM assignation a
-- JOIN vehicule v ON v.id = a.vehicule
-- JOIN assignation_detail ad ON ad.id_association = a.id
-- JOIN reservation r ON r.id = ad.id_reservation
-- WHERE r.id_client IN ('D01','D02','D03','D04','D05')
-- ORDER BY a.depart_aeroport, a.id;
-- Expected: departures at 08:45:00 and 09:15:00.

-- Verify 3: Reservation-to-departure mapping
-- SELECT r.id_client, r.date_heure_arrivee AS resa_time, a.depart_aeroport AS dep_time, v.reference
-- FROM reservation r
-- JOIN assignation_detail ad ON ad.id_reservation = r.id
-- JOIN assignation a ON a.id = ad.id_association
-- JOIN vehicule v ON v.id = a.vehicule
-- WHERE r.id_client IN ('D01','D02','D03','D04','D05')
-- ORDER BY r.id_client;
-- Expected:
-- D01 -> 08:45:00
-- D02 -> 08:45:00
-- D03 -> 09:15:00
-- D04 -> 09:15:00
-- D05 -> 09:15:00

-- Verify 4: No unassigned among target reservations
-- SELECT r.id_client
-- FROM reservation r
-- WHERE r.id_client IN ('D01','D02','D03','D04','D05')
--   AND r.id NOT IN (SELECT ad.id_reservation FROM assignation_detail ad);
-- Expected: 0 rows.

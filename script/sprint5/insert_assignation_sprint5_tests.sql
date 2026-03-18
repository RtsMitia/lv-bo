-- Sprint 5 assignment tests
-- Schema: dev
-- This script prepares deterministic datasets for the new assignation behavior.
--
-- How to use:
-- 1) Run this script in psql.
-- 2) In the application, trigger assignment for the specified date
--    (ex: /assignation/list?date=2026-06-01).
-- 3) Run the verification queries in each section and compare with expected results.

SET search_path TO dev;

-- =====================================================
-- SCENARIO A
-- Delayed departure inside waiting interval
-- =====================================================
-- Goal:
-- - Reservations at 08:30, 08:45, 08:48 are in same group (ta=30).
-- - No pre-created assignations: only automatic assignment is tested.
-- - Expected: automatic grouping/assignment for this window.

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

-- Distances needed for retour_aeroport calculation.
INSERT INTO distance ("from", "to", distance, unite) VALUES
((SELECT id FROM lieux WHERE code = 'AIR'), (SELECT id FROM lieux WHERE code = 'H1'), 10.0, 'km'),
((SELECT id FROM lieux WHERE code = 'AIR'), (SELECT id FROM lieux WHERE code = 'H2'), 20.0, 'km'),
((SELECT id FROM lieux WHERE code = 'H1'), (SELECT id FROM lieux WHERE code = 'H2'), 8.0, 'km');

INSERT INTO vehicule (reference, place, type_carburant) VALUES
('S5-A-V1', 6, 'D'),
('S5-A-V2', 6, 'D');

INSERT INTO reservation (id_client, nb_passager, date_heure_arrivee, id_hotel) VALUES
('A01', 4, '2026-06-01 08:30:00', (SELECT id FROM hotel WHERE nom = 'Hotel One')),
('A02', 2, '2026-06-01 08:45:00', (SELECT id FROM hotel WHERE nom = 'Hotel Two')),
('A03', 6, '2026-06-01 08:48:00', (SELECT id FROM hotel WHERE nom = 'Hotel One'));

-- Run app assignment for date: 2026-06-01
-- Expected results after assignment:
-- 1) New assignations depart at 08:48:00.
-- 3) Reservation A01/A02/A03 are all assigned.
-- 4) Number of assignations for 2026-06-01 should be 2.

-- Verification queries (run AFTER assignment):
-- Q1: Check depart times and vehicles
-- SELECT a.id, v.reference, a.depart_aeroport, a.retour_aeroport
-- FROM assignation a
-- JOIN vehicule v ON v.id = a.vehicule
-- WHERE CAST(a.depart_aeroport AS DATE) = '2026-06-01'
-- ORDER BY a.depart_aeroport, a.id;
-- Expected: all returned rows have depart_aeroport = '2026-06-01 08:48:00'; total rows = 2.

-- Q2: Unassigned reservations on target date
-- SELECT r.id, r.id_client, r.nb_passager, r.date_heure_arrivee
-- FROM reservation r
-- WHERE CAST(r.date_heure_arrivee AS DATE) = '2026-06-01'
--   AND r.id NOT IN (SELECT ad.id_reservation FROM assignation_detail ad)
-- ORDER BY r.date_heure_arrivee;
-- Expected: 0 rows.


-- =====================================================
-- SCENARIO B
-- Fallback to previous reservation time + defer latest to next group
-- =====================================================
-- Goal:
-- - Reservations at 08:30, 08:45, 08:48 in same initial window.
-- - 08:48 reservation needs 6 seats, but max vehicle capacity is 5.
-- - Expected:
--   * Group fallback happens: assignable subset departs at 08:45.
--   * The 08:48 reservation is not assigned and remains unassigned.

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

-- Run app assignment for date: 2026-06-02
-- Expected results after assignment:
-- 1) Assignations for assigned subset depart at 08:45:00.
-- 2) B01 and B02 assigned.
-- 3) B03 stays unassigned (capacity too large for all vehicles).

-- Verification queries (run AFTER assignment):
-- Q1: Check assignation departures
-- SELECT a.id, v.reference, a.depart_aeroport
-- FROM assignation a
-- JOIN vehicule v ON v.id = a.vehicule
-- WHERE CAST(a.depart_aeroport AS DATE) = '2026-06-02'
-- ORDER BY a.depart_aeroport, a.id;
-- Expected: 2 rows, both depart at '2026-06-02 08:45:00'.

-- Q2: Unassigned reservations on target date
-- SELECT r.id_client, r.nb_passager, r.date_heure_arrivee
-- FROM reservation r
-- WHERE CAST(r.date_heure_arrivee AS DATE) = '2026-06-02'
--   AND r.id NOT IN (SELECT ad.id_reservation FROM assignation_detail ad)
-- ORDER BY r.date_heure_arrivee;
-- Expected: exactly 1 row -> B03 at 08:48:00 with nb_passager = 6.

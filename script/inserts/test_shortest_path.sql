-- Test data for Dijkstra shortest path algorithm
-- This creates a simple network with TWO different paths to the same destination

-- Clear existing test data (optional - comment out if you want to keep existing data)
-- DELETE FROM dev.assignation_detail;
-- DELETE FROM dev.assignation;
-- DELETE FROM dev.reservation;
-- DELETE FROM dev.distance;
-- DELETE FROM dev.hotel;
-- DELETE FROM dev.lieux;
-- DELETE FROM dev.vehicule;

-- Create network locations (only what's needed for the test)
INSERT INTO dev.lieux (code, libelle) VALUES
('AIR', 'Aéroport International'),
('CTR', 'Centre Ville'),
('HUB', 'Hub Transport'),
('NRD', 'Quartier Nord'),
('EST', 'Zone Est'),
('MTG', 'Montagne');

-- Hotel at final destination
INSERT INTO dev.hotel (nom, id_lieu) VALUES
('Resort Montagne', (SELECT id FROM dev.lieux WHERE code = 'MTG'));

-- Complex distance network with TWO different paths to Resort Montagne
-- Path 1: AIR -> HUB -> EST -> MTG (shorter)
-- Path 2: AIR -> CTR -> NRD -> MTG (longer alternative)

INSERT INTO dev.distance ("from", "to", distance, unite) VALUES
-- Path 1: Via HUB and EST
((SELECT id FROM dev.lieux WHERE code = 'AIR'), (SELECT id FROM dev.lieux WHERE code = 'HUB'), 15.0, 'km'),
((SELECT id FROM dev.lieux WHERE code = 'HUB'), (SELECT id FROM dev.lieux WHERE code = 'EST'), 10.0, 'km'),
((SELECT id FROM dev.lieux WHERE code = 'EST'), (SELECT id FROM dev.lieux WHERE code = 'MTG'), 20.0, 'km'),
-- Total Path 1: 15 + 10 + 20 = 45 km

-- Path 2: Via CTR and NRD
((SELECT id FROM dev.lieux WHERE code = 'AIR'), (SELECT id FROM dev.lieux WHERE code = 'CTR'), 10.0, 'km'),
((SELECT id FROM dev.lieux WHERE code = 'CTR'), (SELECT id FROM dev.lieux WHERE code = 'NRD'), 12.0, 'km'),
((SELECT id FROM dev.lieux WHERE code = 'NRD'), (SELECT id FROM dev.lieux WHERE code = 'MTG'), 30.0, 'km');
-- Total Path 2: 10 + 12 + 30 = 52 km

-- Dijkstra should select Path 1 (45 km) as the shortest route

-- Vehicles: Three cars with specific capacities and fuel types
INSERT INTO dev.vehicule (reference, place, type_carburant) VALUES
('CAR-10D', 10, 'D'),       -- 10 seats, Diesel (should be selected - best fit + Diesel)
('CAR-10E', 10, 'E'),       -- 10 seats, Electric
('CAR-11D', 11, 'D');       -- 11 seats, Diesel

-- Single reservation: 10 passengers to Resort Montagne
INSERT INTO dev.reservation(id_client, nb_passager, date_heure_arrivee, id_hotel) VALUES
('3363', 10, '2026-03-15 08:00', 1);   -- Resort Montagne (MTG) - only hotel in test data

-- ═══════════════════════════════════════════════════════════════════════════
-- TEST SCENARIO SUMMARY
-- ═══════════════════════════════════════════════════════════════════════════
--
-- NETWORK STRUCTURE:
--
--   Path 1 (SHORTER):  AIR → HUB → EST → MTG
--                      15km + 10km + 20km = 45 km
--
--   Path 2 (LONGER):   AIR → CTR → NRD → MTG  
--                      10km + 12km + 30km = 52 km
--
-- EXPECTED RESULT:
--   ✓ Dijkstra selects Path 1 (45 km) as shortest
--   ✓ Reservation: 10 passengers
--   ✓ Available vehicles: CAR-10D (10/D), CAR-10E (10/E), CAR-11D (11/D)
--   ✓ Selected vehicle: CAR-10D (best fit = 10 seats, Diesel preferred)
--   ✓ Trip time: 45 km × 2 / 60 km/h = 1.5 hours round trip
--   ✓ Return time: 08:00 + 1:30 = 09:30
--
-- WHY CAR-10D is selected:
--   - All three vehicles can fit 10 passengers
--   - CAR-10D and CAR-10E both have exact capacity (10 seats) - smaller than CAR-11D
--   - Between CAR-10D and CAR-10E, Diesel (D) is preferred
--
-- ═══════════════════════════════════════════════════════════════════════════

-- ═══════════════════════════════════════════════════════════════════════════

-- Test cases summary:
-- 1. Two different paths to same destination (Dijkstra finds shortest)
-- 2. Exact capacity match (10 passengers, 10 seat vehicle)
-- 3. Diesel preference when same capacity
-- 4. Multiple hops required (no direct route to destination)

COMMIT;

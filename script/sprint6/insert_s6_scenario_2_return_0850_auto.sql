-- Sprint 6 - Scenario 2
-- Vehicle returns at 08:50 within [08:30, 09:00], so target group departs at 08:50.
-- No manual assignation inserts: blocker trip is auto-created by an early reservation.

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

-- AIR<->FAR one-way 265 => roundtrip 530 at vm=60 => 530 minutes => retour 08:50 for depart 00:00.
INSERT INTO distance ("from", "to", distance, unite) VALUES
((SELECT id FROM lieux WHERE code = 'AIR'), (SELECT id FROM lieux WHERE code = 'H1'), 10.0, 'km'),
((SELECT id FROM lieux WHERE code = 'AIR'), (SELECT id FROM lieux WHERE code = 'H2'), 20.0, 'km'),
((SELECT id FROM lieux WHERE code = 'H1'), (SELECT id FROM lieux WHERE code = 'H2'), 8.0, 'km'),
((SELECT id FROM lieux WHERE code = 'AIR'), (SELECT id FROM lieux WHERE code = 'FAR'), 265.0, 'km');

INSERT INTO vehicule (reference, place, type_carburant) VALUES
('S6-B-V1', 7, 'D'),
('S6-B-V2', 6, 'D');

-- Early reservation to create running trip automatically on V2 (capacity exact 6)
INSERT INTO reservation (id_client, nb_passager, date_heure_arrivee, id_hotel) VALUES
('S6B0', 6, '2026-07-02 00:00:00', (SELECT id FROM hotel WHERE nom = 'Hotel Far'));

-- Target group
INSERT INTO reservation (id_client, nb_passager, date_heure_arrivee, id_hotel) VALUES
('S6B1', 4, '2026-07-02 08:30:00', (SELECT id FROM hotel WHERE nom = 'Hotel One')),
('S6B2', 2, '2026-07-02 08:45:00', (SELECT id FROM hotel WHERE nom = 'Hotel Two')),
('S6B3', 6, '2026-07-02 08:48:00', (SELECT id FROM hotel WHERE nom = 'Hotel One'));

-- Run auto assignation for date: 2026-07-02
-- Verify blocker retour:
-- SELECT a.depart_aeroport, a.retour_aeroport, v.reference
-- FROM assignation a
-- JOIN vehicule v ON v.id = a.vehicule
-- JOIN assignation_detail ad ON ad.id_association = a.id
-- JOIN reservation r ON r.id = ad.id_reservation
-- WHERE r.id_client = 'S6B0';
-- Expected: depart=00:00:00, retour=08:50:00.

-- Verify target departures:
-- SELECT DISTINCT a.depart_aeroport
-- FROM assignation a
-- JOIN assignation_detail ad ON ad.id_association = a.id
-- JOIN reservation r ON r.id = ad.id_reservation
-- WHERE r.id_client IN ('S6B1','S6B2','S6B3')
-- ORDER BY a.depart_aeroport;
-- Expected: only 08:50:00.

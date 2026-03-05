-- Test dataset: many reservations + one explicit assignation to validate findTrajet
-- Usage: psql -h <host> -U <user> -d <db> -f script/inserts/test_many_reservations.sql

BEGIN;

-- Lieux
INSERT INTO dev.lieux (code, libelle) VALUES
('AIR', 'Aéroport'),
('ALP', 'Alpha Hotel'),
('ABET', 'ABeta Hotel'),
('ACHA', 'ACharlie Hotel'),
('DEL', 'Delta Hotel'),
('OTH1', 'Other One'),
('OTH2', 'Other Two');

-- Hôtels
INSERT INTO dev.hotel (nom, id_lieu) VALUES
('Alpha Hotel',   (SELECT id FROM dev.lieux WHERE code = 'ALP')),
('ABeta Hotel',    (SELECT id FROM dev.lieux WHERE code = 'ABET')),
('ACharlie Hotel', (SELECT id FROM dev.lieux WHERE code = 'ACHA')),
('Delta Hotel',   (SELECT id FROM dev.lieux WHERE code = 'DEL')),
('Other One',     (SELECT id FROM dev.lieux WHERE code = 'OTH1')),
('Other Two',     (SELECT id FROM dev.lieux WHERE code = 'OTH2'));

-- Distances (suffisantes pour le calcul greedy utilisé dans findTrajet)
INSERT INTO dev.distance ("from", "to", distance, unite) VALUES
((SELECT id FROM dev.lieux WHERE code = 'AIR'), (SELECT id FROM dev.lieux WHERE code = 'ALP'), 10.0, 'km'),
((SELECT id FROM dev.lieux WHERE code = 'AIR'), (SELECT id FROM dev.lieux WHERE code = 'ABET'), 10.0, 'km'), -- tie with ALP
((SELECT id FROM dev.lieux WHERE code = 'AIR'), (SELECT id FROM dev.lieux WHERE code = 'ACHA'), 10.0, 'km'),
((SELECT id FROM dev.lieux WHERE code = 'AIR'), (SELECT id FROM dev.lieux WHERE code = 'DEL'), 10.0, 'km'),
((SELECT id FROM dev.lieux WHERE code = 'AIR'), (SELECT id FROM dev.lieux WHERE code = 'OTH1'), 10.0, 'km'),
((SELECT id FROM dev.lieux WHERE code = 'AIR'), (SELECT id FROM dev.lieux WHERE code = 'OTH2'), 10.0, 'km'),


((SELECT id FROM dev.lieux WHERE code = 'ALP'), (SELECT id FROM dev.lieux WHERE code = 'ABET'), 6.0, 'km'),
((SELECT id FROM dev.lieux WHERE code = 'ALP'), (SELECT id FROM dev.lieux WHERE code = 'ACHA'), 12.0, 'km'),
((SELECT id FROM dev.lieux WHERE code = 'ALP'), (SELECT id FROM dev.lieux WHERE code = 'DEL'), 20.0, 'km'),
((SELECT id FROM dev.lieux WHERE code = 'ALP'), (SELECT id FROM dev.lieux WHERE code = 'OTH2'), 20.0, 'km'),


((SELECT id FROM dev.lieux WHERE code = 'ABET'), (SELECT id FROM dev.lieux WHERE code = 'ACHA'), 5.0, 'km'),
((SELECT id FROM dev.lieux WHERE code = 'ABET'), (SELECT id FROM dev.lieux WHERE code = 'DEL'), 18.0, 'km'),

((SELECT id FROM dev.lieux WHERE code = 'ACHA'), (SELECT id FROM dev.lieux WHERE code = 'DEL'), 7.0, 'km'),

-- extra distances to other lieux
((SELECT id FROM dev.lieux WHERE code = 'ALP'), (SELECT id FROM dev.lieux WHERE code = 'OTH1'), 18.0, 'km'),
((SELECT id FROM dev.lieux WHERE code = 'ABET'), (SELECT id FROM dev.lieux WHERE code = 'OTH2'), 20.0, 'km'),
((SELECT id FROM dev.lieux WHERE code = 'DEL'), (SELECT id FROM dev.lieux WHERE code = 'OTH1'), 10.0, 'km');

-- Paramètre vitesse moyenne
INSERT INTO dev.param (cle, valeur) VALUES
('vm', '60');

-- Véhicules
INSERT INTO dev.vehicule (reference, place, type_carburant) VALUES
('TST-VAN-20', 20, 'D'),
('TST-VAN-8', 8, 'D'),
('TST-CAR-5', 5, 'E');

-- Beaucoup de réservations (mix de dates/times, plusieurs au même horaire)
INSERT INTO dev.reservation (id_client, nb_passager, date_heure_arrivee, id_hotel) VALUES
('R001', 2, '2026-04-01 08:00', (SELECT id FROM dev.hotel WHERE nom = 'Alpha Hotel')),
('R002', 3, '2026-04-01 08:00', (SELECT id FROM dev.hotel WHERE nom = 'ABeta Hotel')),
('R003', 4, '2026-04-01 08:00', (SELECT id FROM dev.hotel WHERE nom = 'ACharlie Hotel')),
('R004', 6, '2026-04-01 08:00', (SELECT id FROM dev.hotel WHERE nom = 'Delta Hotel')),
('R005', 2, '2026-04-01 08:00', (SELECT id FROM dev.hotel WHERE nom = 'Alpha Hotel')),
('R006', 1, '2026-04-01 08:15', (SELECT id FROM dev.hotel WHERE nom = 'ABeta Hotel')),
('R007', 5, '2026-04-01 08:00', (SELECT id FROM dev.hotel WHERE nom = 'Other One')),
('R008', 3, '2026-04-01 09:00', (SELECT id FROM dev.hotel WHERE nom = 'Other Two')),
('R009', 4, '2026-04-01 08:00', (SELECT id FROM dev.hotel WHERE nom = 'Alpha Hotel')),
('R010', 2, '2026-04-01 10:00', (SELECT id FROM dev.hotel WHERE nom = 'Delta Hotel')),
('R011', 2, '2026-04-02 08:00', (SELECT id FROM dev.hotel WHERE nom = 'ABeta Hotel')),
('R012', 7, '2026-04-01 08:00', (SELECT id FROM dev.hotel WHERE nom = 'Alpha Hotel')),
('R013', 6, '2026-04-01 08:00', (SELECT id FROM dev.hotel WHERE nom = 'ABeta Hotel')),
('R014', 2, '2026-04-03 11:00', (SELECT id FROM dev.hotel WHERE nom = 'Other One')),
('R015', 8, '2026-04-01 08:00', (SELECT id FROM dev.hotel WHERE nom = 'ACharlie Hotel')),
('R016', 3, '2026-04-01 08:00', (SELECT id FROM dev.hotel WHERE nom = 'Delta Hotel')),
('R017', 1, '2026-04-01 08:00', (SELECT id FROM dev.hotel WHERE nom = 'Other Two')),
('R018', 4, '2026-04-01 08:00', (SELECT id FROM dev.hotel WHERE nom = 'Alpha Hotel')),
('R019', 5, '2026-04-01 08:00', (SELECT id FROM dev.hotel WHERE nom = 'ABeta Hotel')),
('R020', 2, '2026-04-01 08:00', (SELECT id FROM dev.hotel WHERE nom = 'ACharlie Hotel')),
('R013', 6, '2026-04-01 08:00', (SELECT id FROM dev.hotel WHERE nom = 'Delta Hotel')),
('R014', 2, '2026-04-03 11:00', (SELECT id FROM dev.hotel WHERE nom = 'Other One')),
('R015', 8, '2026-04-01 08:00', (SELECT id FROM dev.hotel WHERE nom = 'ACharlie Hotel')),
('R016', 3, '2026-04-01 08:00', (SELECT id FROM dev.hotel WHERE nom = 'Delta Hotel')),
('R017', 1, '2026-04-01 08:00', (SELECT id FROM dev.hotel WHERE nom = 'Other Two')),
('R018', 4, '2026-04-01 08:00', (SELECT id FROM dev.hotel WHERE nom = 'Alpha Hotel')),
('R023', 2, '2026-04-01 08:00', (SELECT id FROM dev.hotel WHERE nom = 'Other One')),
('R024', 9, '2026-04-01 08:00', (SELECT id FROM dev.hotel WHERE nom = 'Delta Hotel')),
('R025', 4, '2026-04-01 08:00', (SELECT id FROM dev.hotel WHERE nom = 'Alpha Hotel')),
('R026', 2, '2026-04-04 12:00', (SELECT id FROM dev.hotel WHERE nom = 'Other Two')),
('R027', 6, '2026-04-01 08:00', (SELECT id FROM dev.hotel WHERE nom = 'ABeta Hotel')),
('R028', 3, '2026-04-01 08:00', (SELECT id FROM dev.hotel WHERE nom = 'ACharlie Hotel')),
('R029', 5, '2026-04-01 08:00', (SELECT id FROM dev.hotel WHERE nom = 'Delta Hotel')),
('R019', 2, '2026-04-02 08:00', (SELECT id FROM dev.hotel WHERE nom = 'Other One'));

-- NOTE: No explicit assignation is created here. The application creates assignations
-- automatically when you press the assignation button; keep the reservations above
-- so the automatic process can build assignations from them.

COMMIT;

-- ==========================================================
-- TRAJET ATTENDU (selon l'algorithme greedy de findTrajet)
-- Lieux concernés par les réservations R001..R004 : ALP, BET, CHA, DEL
-- Étapes (choix nearest; tie-break sur libelle)
-- 1) AIR -> Alpha Hotel (ALP) : 10.0 km   (ALP et BET ont même distance 10.0 => Alpha < Beta)
-- 2) ALP -> Beta Hotel (BET)    : 6.0 km
-- 3) BET -> Charlie Hotel (CHA) : 5.0 km
-- 4) CHA -> Delta Hotel (DEL)   : 7.0 km
-- Total distance attendue : 10.0 + 6.0 + 5.0 + 7.0 = 28.0 km
-- Retour attendu dans l'objet Trajet : distance = 28.0, lieux = [id(ALP), id(BET), id(CHA), id(DEL)]
-- ==========================================================

-- Pour tester dans l'application Java :
--  - Exécutez ce script
--  - Lancez le traitement d'assignation via l'interface (bouton) pour la date 2026-04-01
--  - Récupérez l'ID de l'assignation créée (ex. : SELECT id FROM dev.assignation WHERE CAST(depart_aeroport AS DATE) = '2026-04-01' ORDER BY id DESC LIMIT 1)
--  - Appelez : new com.test.service.AssignationService().findTrajet(<idAssignation>)
--  - Vous devez obtenir un Trajet avec distance = 28.0 et la liste d'identifiants dans l'ordre indiqué ci-dessus.

CREATE TABLE IF NOT EXISTS staging.lieux (
    id SERIAL PRIMARY KEY,
    code VARCHAR(10),
    libelle VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS staging.hotel (
    id SERIAL PRIMARY KEY,
    nom VARCHAR(50),
    id_lieu INT REFERENCES staging.lieux(id)
);

CREATE TABLE IF NOT EXISTS staging.reservation (
    id SERIAL PRIMARY KEY,
    id_client VARCHAR(4),
    nb_passager INT,
    date_heure_arrivee TIMESTAMP,
    id_hotel INT REFERENCES staging.hotel(id)
);

CREATE TABLE IF NOT EXISTS staging.token (
    id SERIAL PRIMARY KEY,
    token VARCHAR(100),
    date_expiration TIMESTAMP
);

CREATE TABLE IF NOT EXISTS staging.vehicule (
    id SERIAL PRIMARY KEY,
    reference VARCHAR(50),
    place INT,
    type_carburant VARCHAR(1),
    heure_disponibilite TIME DEFAULT TIME '00:00:00'
);

ALTER TABLE staging.vehicule
ADD COLUMN IF NOT EXISTS heure_disponibilite TIME DEFAULT TIME '00:00:00';


CREATE TABLE IF NOT EXISTS staging.param (
    id SERIAL PRIMARY KEY,
    cle VARCHAR(50),
    valeur VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS staging.distance (
    id SERIAL PRIMARY KEY,
    "from" INT REFERENCES staging.lieux(id),
    "to" INT REFERENCES staging.lieux(id),
    distance NUMERIC(10,2),
    unite VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS staging.assignation (
    id SERIAL PRIMARY KEY,
    vehicule INT REFERENCES staging.vehicule(id),
    depart_aeroport TIMESTAMP,
    retour_aeroport TIMESTAMP
);

-- details: one assignation can reference multiple reservations
CREATE TABLE IF NOT EXISTS staging.assignation_detail (
    id SERIAL PRIMARY KEY,
    id_association INT REFERENCES staging.assignation(id),
    id_reservation INT REFERENCES staging.reservation(id),
    nb_pers_prises INT
);

-- view showing assignation with total number of passengers across its details
CREATE OR REPLACE VIEW staging.assignation_lib AS
SELECT a.id,
       a.vehicule,
       v.reference AS nom_vehicule,
       v.place AS vehicule_place,
       a.depart_aeroport,
       a.retour_aeroport,
       COALESCE(SUM(ad.nb_pers_prises), 0) AS total_passagers,
       (v.place - COALESCE(SUM(ad.nb_pers_prises), 0)) AS reste_place
FROM staging.assignation a
LEFT JOIN staging.assignation_detail ad ON ad.id_association = a.id
LEFT JOIN staging.vehicule v ON v.id = a.vehicule
GROUP BY a.id, a.vehicule, v.reference, v.place, a.depart_aeroport, a.retour_aeroport;
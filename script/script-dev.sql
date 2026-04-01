CREATE TABLE IF NOT EXISTS dev.lieux (
    id SERIAL PRIMARY KEY,
    code VARCHAR(10),
    libelle VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS dev.hotel (
    id SERIAL PRIMARY KEY,
    nom VARCHAR(50),
    id_lieu INT REFERENCES dev.lieux(id)
);


CREATE TABLE IF NOT EXISTS dev.reservation (
    id SERIAL PRIMARY KEY,
    id_client VARCHAR(4),
    nb_passager INT,
    date_heure_arrivee TIMESTAMP,
    id_hotel INT REFERENCES dev.hotel(id)
);

CREATE TABLE IF NOT EXISTS dev.token (
    id SERIAL PRIMARY KEY,
    token VARCHAR(100),
    date_expiration TIMESTAMP
);

CREATE TABLE IF NOT EXISTS dev.vehicule (
    id SERIAL PRIMARY KEY,
    reference VARCHAR(50),
    place INT,
    type_carburant VARCHAR(1),
    heure_disponibilite TIME DEFAULT '00:00:00'
);

ALTER TABLE dev.vehicule
ADD COLUMN IF NOT EXISTS heure_disponibilite TIME DEFAULT '00:00:00';

CREATE TABLE IF NOT EXISTS dev.param (
    id SERIAL PRIMARY KEY,
    cle VARCHAR(50),
    valeur VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS dev.distance (
    id SERIAL PRIMARY KEY,
    "from" INT REFERENCES dev.lieux(id),
    "to" INT REFERENCES dev.lieux(id),
    distance NUMERIC(10,2),
    unite VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS dev.assignation (
    id SERIAL PRIMARY KEY,
    vehicule INT REFERENCES dev.vehicule(id),
    depart_aeroport TIMESTAMP,
    retour_aeroport TIMESTAMP
);

-- details: one assignation can reference multiple reservations
CREATE TABLE IF NOT EXISTS dev.assignation_detail (
    id SERIAL PRIMARY KEY,
    id_association INT REFERENCES dev.assignation(id),
    id_reservation INT REFERENCES dev.reservation(id),
    nb_pers_prises INT
);

-- view showing assignation with total number of passengers across its details
CREATE OR REPLACE VIEW dev.assignation_lib AS
SELECT a.id,
       a.vehicule,
       v.reference AS nom_vehicule,
       v.place AS vehicule_place,
       a.depart_aeroport,
       a.retour_aeroport,
       COALESCE(SUM(ad.nb_pers_prises), 0) AS total_passagers,
       (v.place - COALESCE(SUM(ad.nb_pers_prises), 0)) AS reste_place
FROM dev.assignation a
LEFT JOIN dev.assignation_detail ad ON ad.id_association = a.id
LEFT JOIN dev.vehicule v ON v.id = a.vehicule
GROUP BY a.id, a.vehicule, v.reference, v.place, a.depart_aeroport, a.retour_aeroport;
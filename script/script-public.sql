CREATE TABLE IF NOT EXISTS hotel (
    id SERIAL PRIMARY KEY,
    nom VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS reservation (
    id SERIAL PRIMARY KEY,
    id_client VARCHAR(4),
    nb_passager INT,
    date_heure_arrivee TIMESTAMP,
    id_hotel INT REFERENCES hotel(id)
);

CREATE TABLE IF NOT EXISTS token (
    id SERIAL PRIMARY KEY,
    token VARCHAR(100),
    date_expiration TIMESTAMP
);

CREATE TABLE IF NOT EXISTS vehicule (
    id SERIAL PRIMARY KEY,
    reference VARCHAR(50),
    place INT,
    type_carburant VARCHAR(1)
);


CREATE TABLE IF NOT EXISTS param (
    id SERIAL PRIMARY KEY,
    cle VARCHAR(50),
    valeur VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS distance (
    id SERIAL PRIMARY KEY,
    "from" INT REFERENCES hotel(id),
    "to" INT REFERENCES hotel(id),
    unite VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS assignation (
    id SERIAL PRIMARY KEY,
    vehicule INT REFERENCES vehicule(id),
    depart_aeroport TIMESTAMP,
    retour_aeroport TIMESTAMP
);

-- details: one assignation can reference multiple reservations
CREATE TABLE IF NOT EXISTS assignation_detail (
    id SERIAL PRIMARY KEY,
    id_association INT REFERENCES assignation(id),
    id_reservation INT REFERENCES reservation(id),
    nb_pers_prises INT
);

-- view showing assignation with total number of passengers across its details
CREATE OR REPLACE VIEW assignation_lib AS
SELECT a.id,
       a.vehicule,
       v.reference AS nom_vehicule,
       v.place AS vehicule_place,
       a.depart_aeroport,
       a.retour_aeroport,
       COALESCE(SUM(ad.nb_pers_prises), 0) AS total_passagers,
       (v.place - COALESCE(SUM(ad.nb_pers_prises), 0)) AS reste_place
FROM assignation a
LEFT JOIN assignation_detail ad ON ad.id_association = a.id
LEFT JOIN vehicule v ON v.id = a.vehicule
GROUP BY a.id, a.vehicule, v.reference, v.place, a.depart_aeroport, a.retour_aeroport;


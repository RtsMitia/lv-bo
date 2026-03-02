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
    from INT REFERENCES hotel(id),
    to INT REFERENCES hotel(id),
    unite VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS assignation (
    id SERIAL PRIMARY KEY,
    vehicule INT REFERENCES vehicule(id),
    reservation INT REFERENCES reservation(id),
    nb_pers_prises INT,
    depart_aeroport TIMESTAMP,
    retour_aeroport TIMESTAMP
);


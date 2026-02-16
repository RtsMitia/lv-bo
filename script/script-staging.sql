CREATE TABLE IF NOT EXISTS staging.hotel (
    id SERIAL PRIMARY KEY,
    nom VARCHAR(50)
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
    type_carburant VARCHAR(1)
);
CREATE TABLE IF NOT EXISTS dev.hotel (
    id SERIAL PRIMARY KEY,
    nom VARCHAR(50)
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
    type_carburant VARCHAR(1)
);

CREATE TABLE IF NOT EXISTS dev.param (
    id SERIAL PRIMARY KEY,
    cle VARCHAR(50),
    cle VARCHAR(50),
);

CREATE TABLE IF NOT EXISTS dev.lieux (
    id SERIAL PRIMARY KEY,
    code VARCHAR(50),
    libelle VARCHAR(50),
);

CREATE TABLE IF NOT EXISTS dev.distance (
    id SERIAL PRIMARY KEY,
    from INT REFERENCES lieux(id),
    to INT REFERENCES lieux(id),
    unite VARCHAR(50)
);
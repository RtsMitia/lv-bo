/*public*/
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


CREATE TABLE IF NOT EXISTS vehicule (
    id SERIAL PRIMARY KEY,
    reference VARCHAR(50),
    place INT,
    type_carburant VARCHAR(1)
);


CREATE TABLE IF NOT EXISTS token (
    id SERIAL PRIMARY KEY,
    token VARCHAR(50),
    expiration TIMESTAMP
);


/*dev*/
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


CREATE TABLE IF NOT EXISTS dev.vehicule (
    id SERIAL PRIMARY KEY,
    reference VARCHAR(50),
    place INT,
    type_carburant VARCHAR(1)
);


CREATE TABLE IF NOT EXISTS dev.token (
    id SERIAL PRIMARY KEY,
    token VARCHAR(50),
    expiration TIMESTAMP
);

/*staging*/
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

CREATE TABLE IF NOT EXISTS staging.vehicule (
    id SERIAL PRIMARY KEY,
    reference VARCHAR(50),
    place INT,
    type_carburant VARCHAR(1)
);


CREATE TABLE IF NOT EXISTS staging.token (
    id SERIAL PRIMARY KEY,
    token VARCHAR(50),
    expiration TIMESTAMP
);
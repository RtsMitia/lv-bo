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
    date_expiration TIMESTAMP
);

CREATE TABLE IF NOT EXISTS param (
    id SERIAL PRIMARY KEY,
    cle VARCHAR(50),
    cle VARCHAR(50),
);

CREATE TABLE IF NOT EXISTS lieux (
    id SERIAL PRIMARY KEY,
    code VARCHAR(50),
    libelle VARCHAR(50),
);

CREATE TABLE IF NOT EXISTS distance (
    id SERIAL PRIMARY KEY,
    from INT REFERENCES lieux(id),
    to INT REFERENCES lieux(id),
    unite VARCHAR(50)
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
    date_expiration TIMESTAMP
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
    date_expiration TIMESTAMP
);
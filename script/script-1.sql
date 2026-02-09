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
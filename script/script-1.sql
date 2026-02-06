CREATE TABLE hotel (
    id SERIAL PRIMARY KEY,
    nom VARCHAR(50)
);

CREATE TABLE reservation (
    id SERIAL PRIMARY KEY,
    id_client VARCHAR(4),
    nb_passager INT,
    date_heure_arrivee TIMESTAMP,
    id_hotel INT REFERENCES hotel(id)
);
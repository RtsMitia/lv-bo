CREATE TABLE voiture (
    id SERIAL PRIMARY KEY,
    matricule VARCHAR(50),
    marque VARCHAR(50),
    capacite INT,
    id_type INT FOREIGN KEY REFERENCES type_carburant(id)
);

-- Diesel ou Essence
CREATE TABLE type_carburant (
    id SERIAL PRIMARY KEY,
    libelle VARCHAR(50)
);

-- Andeha sa Hiverina
CREATE TABLE type_trajet (
    id SERIAL PRIMARY KEY,
    libelle VARCHAR(50)
);

CREATE TABLE vol (
    id SERIAL PRIMARY KEY,
    date_heure_aeroport DATETIME, 
    id_type_trajet INT FOREIGN KEY REFERENCES type_trajet(id)
    reference_vol VARCHAR(100),
);

CREATE TABLE hotel (
    id SERIAL PRIMARY KEY,
    nom VARCHAR(50),
    distance_aeroport DOUBLE
);

CREATE TABLE demande_clients (
    id SERIAL PRIMARY KEY,
    reference_vol VARCHAR(100),
    id_hotel_destination INT FOREIGN KEY REFERENCES hotel(id),
    nb_personne INT
);

CREATE TABLE voiture_assignation (
    id SERIAL PRIMARY KEY,
    id_vol INT FOREIGN KEY REFERENCES vol(id),
    id_voiture INT FOREIGN KEY REFERENCES voiture(id),
    id_demande INT FOREIGN KEY REFERENCES demande_clients(id)
);

-- Temps d'attente
CREATE TABLE config (
    id SERIAL PRIMARY KEY,
    cle VARCHAR(50),
    valeur VARCHAR(50)
);

-- Vue pour savoir si la demande a ete satisfaite

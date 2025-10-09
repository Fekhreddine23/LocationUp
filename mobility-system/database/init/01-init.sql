-- Initialisation de la base de données Mobility
SET NAMES utf8mb4;

CREATE DATABASE IF NOT EXISTS mobility_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE mobility_db;

-- Table des villes
CREATE TABLE cities (
    city_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Table des services de mobilité
CREATE TABLE mobility_services (
    service_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Table des administrateurs
CREATE TABLE admins (
    admin_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    admin_level INT DEFAULT 2 CHECK (admin_level IN (1, 2, 3)),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Table des utilisateurs
CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Table des offres
CREATE TABLE offers (
    offer_id INT AUTO_INCREMENT PRIMARY KEY,
    pickup_location_id INT NOT NULL,
    return_location_id INT NOT NULL,
    mobility_service_id INT NOT NULL,
    admin_id INT NOT NULL,
    pickup_datetime DATETIME NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL CHECK (price >= 0),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (pickup_location_id) REFERENCES cities(city_id),
    FOREIGN KEY (return_location_id) REFERENCES cities(city_id),
    FOREIGN KEY (mobility_service_id) REFERENCES mobility_services(service_id),
    FOREIGN KEY (admin_id) REFERENCES admins(admin_id)
);

-- Table des réservations
CREATE TABLE reservations (
    reservation_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    offer_id INT NOT NULL,
    reservation_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status ENUM('PENDING', 'CONFIRMED', 'CANCELLED', 'COMPLETED') DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (offer_id) REFERENCES offers(offer_id)
);

-- Données initiales
INSERT INTO cities (name) VALUES 
('Paris'),
('Lyon'),
('Marseille'),
('Toulouse'),
('Bordeaux');

INSERT INTO mobility_services (name, description) VALUES 
('Vélo électrique', 'Vélo à assistance électrique pour trajets urbains'),
('Scooter électrique', 'Scooter électrique pour déplacements rapides'),
('Voiture en libre-service', 'Voiture électrique en autopartage'),
('Trotinette électrique', 'Trotinette pour petits trajets');

-- Mot de passe: "password" hashé avec BCrypt
INSERT INTO admins (username, email, password, admin_level) VALUES 
('admin', 'admin@mobility.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye.TVDWlmMkK7ebB6K1pM2KTaZ1piQrqO', 1);

INSERT INTO users (username, email, password) VALUES 
('user1', 'user1@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye.TVDWlmMkK7ebB6K1pM2KTaZ1piQrqO');


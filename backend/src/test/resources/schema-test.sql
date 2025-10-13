-- Tables principales
CREATE TABLE cities (
    city_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE mobility_services (
    service_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500)
);

CREATE TABLE admins (
    admin_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    admin_level INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE users (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE locations (
    location_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    city_id BIGINT NOT NULL,
    address VARCHAR(255) NOT NULL,
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (city_id) REFERENCES cities(city_id)
);

-- Création des index séparément (syntaxe H2 correcte)
CREATE INDEX idx_locations_city_id ON locations(city_id);

CREATE TABLE offers (
    offer_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    pickup_location_id BIGINT NOT NULL,
    return_location_id BIGINT NOT NULL,
    mobility_service_id BIGINT NOT NULL,
    admin_id BIGINT NOT NULL,
    pickup_datetime DATETIME NOT NULL,
    return_datetime DATETIME NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (pickup_location_id) REFERENCES locations(location_id),
    FOREIGN KEY (return_location_id) REFERENCES locations(location_id),
    FOREIGN KEY (mobility_service_id) REFERENCES mobility_services(service_id),
    FOREIGN KEY (admin_id) REFERENCES admins(admin_id)
);

-- Index pour la table offers
CREATE INDEX idx_offers_pickup_location ON offers(pickup_location_id);
CREATE INDEX idx_offers_return_location ON offers(return_location_id);
CREATE INDEX idx_offers_mobility_service ON offers(mobility_service_id);
CREATE INDEX idx_offers_admin ON offers(admin_id);

CREATE TABLE reservations (
    reservation_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    offer_id BIGINT NOT NULL,
    reservation_date DATETIME NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (offer_id) REFERENCES offers(offer_id)
);

-- Index pour la table reservations
CREATE INDEX idx_reservations_user ON reservations(user_id);
CREATE INDEX idx_reservations_offer ON reservations(offer_id);

-- Contrainte CHECK pour le status (syntaxe H2)
ALTER TABLE reservations ADD CONSTRAINT chk_reservation_status 
CHECK (status IN ('PENDING', 'CONFIRMED', 'CANCELLED', 'COMPLETED'));

-- Données de test de base
INSERT INTO cities (name) VALUES 
('Paris'),
('Lyon'),
('Marseille');

INSERT INTO mobility_services (name, description) VALUES 
('Vélib', 'Service de vélos en libre-service à Paris'),
('Lime', 'Service de trottinettes électriques'),
('Uber', 'Service de VTC');

INSERT INTO admins (username, email, password, admin_level) VALUES 
('admin1', 'admin1@locationup.com', 'encrypted_password', 1),
('admin2', 'admin2@locationup.com', 'encrypted_password', 2);

INSERT INTO users (username, email, password) VALUES 
('user1', 'user1@example.com', 'encrypted_password'),
('user2', 'user2@example.com', 'encrypted_password');

INSERT INTO locations (city_id, address, latitude, longitude) VALUES 
(1, '1 Avenue des Champs-Élysées, Paris', 48.8738, 2.2950),
(1, 'Gare de Lyon, Paris', 48.8448, 2.3740),
(2, 'Place Bellecour, Lyon', 45.7578, 4.8320);
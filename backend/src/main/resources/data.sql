-- import.sql - Données réalistes pour Mobility Location
-- Exécuté APRÈS la création des tables par Hibernate

ALTER TABLE reservations ALTER COLUMN payment_status VARCHAR(20);

-- 1. Insertion des villes avec coordonnées
INSERT INTO cities (name, postal_code, latitude, longitude) VALUES 
('Paris', '75000', 48.8566, 2.3522),
('Lyon', '69000', 45.7640, 4.8357),
('Marseille', '13000', 43.2965, 5.3698),
('Bordeaux', '33000', 44.8378, -0.5792),
('Nice', '06000', 43.7102, 7.2620),
('Toulouse', '31000', 43.6047, 1.4442),
('Lille', '59000', 50.6292, 3.0573),
('Strasbourg', '67000', 48.5734, 7.7521);

-- 2. Insertion des services de mobilité
INSERT INTO mobility_services (name, categorie, description) VALUES
('Location Voiture Économique', 'VOITURE', 'Véhicules économiques pour la ville'),
('Location Utilitaire', 'UTILITAIRE', 'Véhicules utilitaires pour professionnels'),
('Location SUV & 4x4', 'SUV', 'Véhicules spacieux pour famille et voyages'),
('Location Premium', 'PREMIUM', 'Véhicules haut de gamme'),
('Location Électrique', 'ELECTRIQUE', 'Véhicules 100% électriques');

-- 3. Insertion des administrateurs
 
INSERT INTO admins (username, email, password, role, admin_level) VALUES
('admin', 'admin@mobility.com','$2a$10$.aT52KBRcU8GP8HEGfuu/OVxDcDSfqkIOP7PmjaAzS6wszzOaEQrW', 'ROLE_ADMIN', 10),
('manager', 'manager@mobility.com', '$2a$10$dknOcIksxVrVYFnfgUlhNetuJnobWmvFRJRMyM3N5fw3cwsNmo2Ui', 'ROLE_ADMIN', 5);

-- 4. Insertion des utilisateurs
INSERT INTO users (username, email, password, first_name, last_name, role) VALUES
('client1', 'marie.martin@email.com', '$2a$10$xyz125', 'Marie', 'Martin', 'ROLE_USER'),
('client2', 'jean.dubois@email.com', '$2a$10$xyz126', 'Jean', 'Dubois', 'ROLE_USER'),
('client3', 'sophie.leroy@email.com', '$2a$10$xyz127', 'Sophie', 'Leroy', 'ROLE_USER'),
('client4', 'mohamed.aloui@email.com', '$2a$10$xyz128', 'Mohamed', 'Aloui', 'ROLE_USER'),
('client5', 'laura.petit@email.com', '$2a$10$xyz129', 'Laura', 'Petit', 'ROLE_USER');

-- 5. Insertion des offres avec images
INSERT INTO offers (pickup_location_id, return_location_id, mobility_service_id, admin_id, 
                   pickup_datetime, description, price, image_url, status, active) VALUES
(1, 1, 1, 1, '2024-03-20 10:00:00', 
 'Peugeot 208 essence, 5 portes, clim, GPS - Idéal pour Paris', 45.00, 
 'https://images.pexels.com/photos/170811/pexels-photo-170811.jpeg?auto=compress&cs=tinysrgb&w=600', 'CONFIRMED', true),

(2, 2, 1, 1, '2024-03-22 14:00:00', 
 'Renault Clio diesel, récente, faible consommation - Parfaite pour Lyon', 42.00, 
 'https://images.pexels.com/photos/358070/pexels-photo-358070.jpeg?auto=compress&cs=tinysrgb&w=600', 'CONFIRMED', true),

(3, 3, 3, 1, '2024-03-25 09:00:00', 
 'Dacia Duster SUV familial, 4x4 occasionnel, grand coffre 510L - Marseille', 65.00, 
 'https://images.pexels.com/photos/116675/pexels-photo-116675.jpeg?auto=compress&cs=tinysrgb&w=600', 'CONFIRMED', true),

(1, 1, 4, 1, '2024-03-28 16:00:00', 
 'Tesla Model 3 électrique, autonomie 400km, recharge rapide - Paris Opéra', 89.00, 
 'https://images.pexels.com/photos/3729464/pexels-photo-3729464.jpeg?auto=compress&cs=tinysrgb&w=600', 'CONFIRMED', true),

(4, 4, 1, 1, '2024-04-01 11:00:00', 
 'Citroën C3, économique 4.5L/100km, parfaite pour ville - Bordeaux Centre', 38.00, 
 'https://images.pexels.com/photos/210019/pexels-photo-210019.jpeg?auto=compress&cs=tinysrgb&w=600', 'CONFIRMED', true),

(5, 5, 3, 1, '2024-04-05 15:30:00', 
 'Toyota RAV4 hybride, confortable, 5 places, toit panoramique - Nice', 72.00, 
 'https://images.pexels.com/photos/2526120/pexels-photo-2526120.jpeg?auto=compress&cs=tinysrgb&w=600', 'CONFIRMED', true),

(6, 6, 2, 1, '2024-04-10 08:30:00', 
 'Renault Kangoo utilitaire, 800kg de charge, idéal déménagement - Toulouse', 55.00, 
 'https://images.pexels.com/photos/244206/pexels-photo-244206.jpeg?auto=compress&cs=tinysrgb&w=600', 'CONFIRMED', true),

(1, 1, 5, 1, '2024-04-15 12:00:00', 
 'Nissan Leaf électrique, autonomie 270km, écologique - Paris', 58.00, 
 'https://images.pexels.com/photos/1007410/pexels-photo-1007410.jpeg?auto=compress&cs=tinysrgb&w=600', 'CONFIRMED', true);

-- 6. Insertion des réservations
INSERT INTO reservations (user_id, offer_id, reservation_date, status, payment_status, payment_reference, payment_date, payment_amount, created_at, updated_at) VALUES
(1, 1, '2024-03-15 14:30:00', 'COMPLETED', 'PAID', 'demo_ref_1', '2024-03-15 15:00:00', 45.00, '2024-03-10 09:00:00', '2024-03-15 15:00:00'),
(2, 3, '2024-03-18 11:15:00', 'CONFIRMED', 'PAID', 'demo_ref_2', '2024-03-18 11:20:00', 65.00, '2024-03-12 12:00:00', '2024-03-18 11:20:00'),
(3, 2, '2024-03-19 09:45:00', 'PENDING', 'PENDING', NULL, NULL, 42.00, '2024-03-13 13:00:00', '2024-03-13 13:00:00'),
(4, 5, '2024-03-20 16:20:00', 'CONFIRMED', 'PAID', 'demo_ref_3', '2024-03-20 16:25:00', 38.00, '2024-03-14 08:00:00', '2024-03-20 16:25:00'),
(5, 4, '2024-03-21 13:10:00', 'COMPLETED', 'PAID', 'demo_ref_4', '2024-03-21 13:20:00', 89.00, '2024-03-14 09:30:00', '2024-03-21 13:20:00'),
(1, 6, '2024-03-22 10:30:00', 'PENDING', 'PENDING', NULL, NULL, 72.00, '2024-03-15 10:00:00', '2024-03-15 10:00:00'),
(2, 7, '2024-03-23 15:45:00', 'CONFIRMED', 'PAID', 'demo_ref_5', '2024-03-23 15:50:00', 55.00, '2024-03-16 11:00:00', '2024-03-23 15:50:00'),
(3, 8, '2024-03-24 12:00:00', 'CANCELLED', 'FAILED', 'demo_ref_6', '2024-03-24 12:05:00', 58.00, '2024-03-16 14:00:00', '2024-03-24 12:05:00');

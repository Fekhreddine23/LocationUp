-- V3 : import des données de démonstration (équivalent à data.sql utilisé en dev/H2)

-- 1) Villes
DO $$
BEGIN
  IF (SELECT COUNT(*) FROM cities) = 0 THEN
    INSERT INTO cities (city_id, name, postal_code, latitude, longitude) VALUES
      (1, 'Paris', '75000', 48.8566, 2.3522),
      (2, 'Lyon', '69000', 45.7640, 4.8357),
      (3, 'Marseille', '13000', 43.2965, 5.3698),
      (4, 'Bordeaux', '33000', 44.8378, -0.5792),
      (5, 'Nice', '06000', 43.7102, 7.2620),
      (6, 'Toulouse', '31000', 43.6047, 1.4442),
      (7, 'Lille', '59000', 50.6292, 3.0573),
      (8, 'Strasbourg', '67000', 48.5734, 7.7521);
    PERFORM setval(pg_get_serial_sequence('cities', 'city_id'), (SELECT MAX(city_id) FROM cities));
  END IF;
END$$;

-- 2) Services de mobilité
DO $$
BEGIN
  IF (SELECT COUNT(*) FROM mobility_services) = 0 THEN
    INSERT INTO mobility_services (service_id, name, categorie, description) VALUES
      (1, 'Location Voiture Économique', 'VOITURE', 'Véhicules économiques pour la ville'),
      (2, 'Location Utilitaire', 'UTILITAIRE', 'Véhicules utilitaires pour professionnels'),
      (3, 'Location SUV & 4x4', 'SUV', 'Véhicules spacieux pour famille et voyages'),
      (4, 'Location Premium', 'PREMIUM', 'Véhicules haut de gamme'),
      (5, 'Location Électrique', 'ELECTRIQUE', 'Véhicules 100% électriques');
    PERFORM setval(pg_get_serial_sequence('mobility_services', 'service_id'), (SELECT MAX(service_id) FROM mobility_services));
  END IF;
END$$;

-- 3) Admins
DO $$
BEGIN
  IF (SELECT COUNT(*) FROM admins) = 0 THEN
    -- Hash bcrypt pour "admin123"
    INSERT INTO admins (admin_id, username, email, password, role, admin_level) VALUES
      (1, 'admin', 'admin@mobility.com', '$2a$10$.aT52KBRcU8GP8HEGfuu/OVxDcDSfqkIOP7PmjaAzS6wszzOaEQrW', 'ROLE_ADMIN', 10),
      (2, 'manager', 'manager@mobility.com', '$2a$10$dknOcIksxVrVYFnfgUlhNetuJnobWmvFRJRMyM3N5fw3cwsNmo2Ui', 'ROLE_ADMIN', 5);
    PERFORM setval(pg_get_serial_sequence('admins', 'admin_id'), (SELECT MAX(admin_id) FROM admins));
  END IF;
END$$;

-- 4) Utilisateurs (mot de passe bcrypt "password")
DO $$
BEGIN
  IF (SELECT COUNT(*) FROM users) = 0 THEN
    INSERT INTO users (id, username, email, password, first_name, last_name, role) VALUES
      (1, 'client1', 'marie.martin@email.com', '$2a$10$CwTycUXWue0Thq9StjUM0uJ8e.KeO9C.RW8DjuN3kyQUJp0G5S/7K', 'Marie', 'Martin', 'ROLE_USER'),
      (2, 'client2', 'jean.dubois@email.com', '$2a$10$CwTycUXWue0Thq9StjUM0uJ8e.KeO9C.RW8DjuN3kyQUJp0G5S/7K', 'Jean', 'Dubois', 'ROLE_USER'),
      (3, 'client3', 'sophie.leroy@email.com', '$2a$10$CwTycUXWue0Thq9StjUM0uJ8e.KeO9C.RW8DjuN3kyQUJp0G5S/7K', 'Sophie', 'Leroy', 'ROLE_USER'),
      (4, 'client4', 'mohamed.aloui@email.com', '$2a$10$CwTycUXWue0Thq9StjUM0uJ8e.KeO9C.RW8DjuN3kyQUJp0G5S/7K', 'Mohamed', 'Aloui', 'ROLE_USER'),
      (5, 'client5', 'laura.petit@email.com', '$2a$10$CwTycUXWue0Thq9StjUM0uJ8e.KeO9C.RW8DjuN3kyQUJp0G5S/7K', 'Laura', 'Petit', 'ROLE_USER');
    PERFORM setval(pg_get_serial_sequence('users', 'id'), (SELECT MAX(id) FROM users));
  END IF;
END$$;

-- 5) Offres
DO $$
BEGIN
  IF (SELECT COUNT(*) FROM offers) = 0 THEN
    INSERT INTO offers (offer_id, pickup_location_id, return_location_id, mobility_service_id, admin_id,
                        pickup_datetime, description, price, image_url, status, active)
    VALUES
      (1, 1, 1, 1, 1, NOW() + INTERVAL '1 day',
       'Peugeot 208 essence, 5 portes, clim, GPS - Idéal pour Paris', 45.00,
       'https://images.pexels.com/photos/170811/pexels-photo-170811.jpeg?auto=compress&cs=tinysrgb&w=600', 'CONFIRMED', true),
      (2, 2, 2, 1, 1, NOW() + INTERVAL '2 day',
       'Renault Clio diesel, récente, faible consommation - Parfaite pour Lyon', 42.00,
       'https://images.pexels.com/photos/358070/pexels-photo-358070.jpeg?auto=compress&cs=tinysrgb&w=600', 'CONFIRMED', true),
      (3, 3, 3, 3, 1, NOW() + INTERVAL '3 day',
       'Dacia Duster SUV familial, 4x4 occasionnel, grand coffre 510L - Marseille', 65.00,
       'https://images.pexels.com/photos/116675/pexels-photo-116675.jpeg?auto=compress&cs=tinysrgb&w=600', 'CONFIRMED', true),
      (4, 1, 1, 4, 1, NOW() + INTERVAL '4 day',
       'Tesla Model 3 électrique, autonomie 400km, recharge rapide - Paris Opéra', 89.00,
       'https://images.pexels.com/photos/3729464/pexels-photo-3729464.jpeg?auto=compress&cs=tinysrgb&w=600', 'CONFIRMED', true),
      (5, 4, 4, 1, 1, NOW() + INTERVAL '5 day',
       'Citroën C3, économique 4.5L/100km, parfaite pour ville - Bordeaux Centre', 38.00,
       'https://images.pexels.com/photos/210019/pexels-photo-210019.jpeg?auto=compress&cs=tinysrgb&w=600', 'CONFIRMED', true),
      (6, 5, 5, 3, 1, NOW() + INTERVAL '6 day',
       'Toyota RAV4 hybride, confortable, 5 places, toit panoramique - Nice', 72.00,
       'https://images.pexels.com/photos/2526120/pexels-photo-2526120.jpeg?auto=compress&cs=tinysrgb&w=600', 'CONFIRMED', true),
      (7, 6, 6, 2, 1, NOW() + INTERVAL '7 day',
       'Renault Kangoo utilitaire, 800kg de charge, idéal déménagement - Toulouse', 55.00,
       'https://images.pexels.com/photos/244206/pexels-photo-244206.jpeg?auto=compress&cs=tinysrgb&w=600', 'CONFIRMED', true),
      (8, 1, 1, 5, 1, NOW() + INTERVAL '8 day',
       'Nissan Leaf électrique, autonomie 270km, écologique - Paris', 58.00,
       'https://images.pexels.com/photos/1007410/pexels-photo-1007410.jpeg?auto=compress&cs=tinysrgb&w=600', 'CONFIRMED', true);
    PERFORM setval(pg_get_serial_sequence('offers', 'offer_id'), (SELECT MAX(offer_id) FROM offers));
  END IF;
END$$;

-- 6) Réservations
DO $$
BEGIN
  IF (SELECT COUNT(*) FROM reservations) = 0 THEN
    INSERT INTO reservations (reservation_id, user_id, offer_id, reservation_date, status, payment_status,
                              payment_reference, payment_date, payment_amount, created_at, updated_at)
    VALUES
      (1, 1, 1, NOW() + INTERVAL '1 day', 'COMPLETED', 'PAID', 'demo_ref_1', NOW(), 45.00, NOW() - INTERVAL '5 day', NOW()),
      (2, 2, 3, NOW() + INTERVAL '2 day', 'CONFIRMED', 'PAID', 'demo_ref_2', NOW(), 65.00, NOW() - INTERVAL '4 day', NOW()),
      (3, 3, 2, NOW() + INTERVAL '3 day', 'PENDING', 'PENDING', NULL, NULL, 42.00, NOW() - INTERVAL '3 day', NOW() - INTERVAL '3 day'),
      (4, 4, 5, NOW() + INTERVAL '4 day', 'CONFIRMED', 'PAID', 'demo_ref_3', NOW(), 38.00, NOW() - INTERVAL '2 day', NOW()),
      (5, 5, 4, NOW() + INTERVAL '5 day', 'COMPLETED', 'PAID', 'demo_ref_4', NOW(), 89.00, NOW() - INTERVAL '2 day', NOW()),
      (6, 1, 6, NOW() + INTERVAL '6 day', 'PENDING', 'PENDING', NULL, NULL, 72.00, NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day'),
      (7, 2, 7, NOW() + INTERVAL '7 day', 'CONFIRMED', 'PAID', 'demo_ref_5', NOW(), 55.00, NOW() - INTERVAL '1 day', NOW()),
      (8, 3, 8, NOW() + INTERVAL '8 day', 'CANCELLED', 'FAILED', 'demo_ref_6', NOW(), 58.00, NOW() - INTERVAL '1 day', NOW());
    PERFORM setval(pg_get_serial_sequence('reservations', 'reservation_id'), (SELECT MAX(reservation_id) FROM reservations));
  END IF;
END$$;


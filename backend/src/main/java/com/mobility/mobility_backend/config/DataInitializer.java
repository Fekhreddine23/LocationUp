package com.mobility.mobility_backend.config;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.mobility.mobility_backend.entity.Admin;
import com.mobility.mobility_backend.entity.City;
import com.mobility.mobility_backend.entity.MobilityService;
import com.mobility.mobility_backend.entity.Offer;
import com.mobility.mobility_backend.entity.Reservation;
import com.mobility.mobility_backend.entity.Role;
import com.mobility.mobility_backend.entity.User;
import com.mobility.mobility_backend.repository.AdminRepository;
import com.mobility.mobility_backend.repository.CityRepository;
import com.mobility.mobility_backend.repository.MobilityServiceRepository;
import com.mobility.mobility_backend.repository.OfferRepository;
import com.mobility.mobility_backend.repository.ReservationRepository;
import com.mobility.mobility_backend.repository.UserRepository;

@Component
public class DataInitializer implements CommandLineRunner {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	@Autowired
	private ReservationRepository reservationRepository;

	@Autowired
	private OfferRepository offerRepository;

	@Autowired
	private AdminRepository adminRepository;


	@Autowired
	private CityRepository cityRepository;

	@Autowired
	private MobilityServiceRepository mobilityServiceRepository;

	public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public void run(String... args) throws Exception {
		// Nettoyer d'abord
		userRepository.deleteAll();

		// Créer l'utilisateur avec mot de passe HACHÉ
		User user = new User();
		user.setUsername("testuser");
		user.setEmail("test@example.com");

		// ⚠️ CORRECTION ICI - UTILISEZ passwordEncoder.encode()
		String rawPassword = "password123";
		String hashedPassword = passwordEncoder.encode(rawPassword); // ← HACHAGE
		user.setPassword(hashedPassword);

		user.setRole(Role.ROLE_USER);

		User savedUser = userRepository.save(user);

		// Vérifier le hash
		System.out.println("✅ User created with ID: " + savedUser.getId());
		System.out.println("🔐 Password hash: " + savedUser.getPassword()); // Doit être $2a$10$...
		System.out.println("👤 Username: testuser");
		System.out.println("🔑 Raw Password: " + rawPassword);

		// Vérifier que le hash est correct
		boolean matches = passwordEncoder.matches(rawPassword, savedUser.getPassword());
		System.out.println("🔍 Password matches: " + matches);

		// Vérifier la longueur du hash
		System.out.println("📏 Hash length: " + savedUser.getPassword().length());
		System.out.println("🔍 Hash starts with $2a$: " + savedUser.getPassword().startsWith("$2a$"));

		System.out.println("=== VERIFICATION ===");
		long userCount = userRepository.count();
		System.out.println("Users in database: " + userCount);

		userRepository.findAll().forEach(u -> {
			System.out.println("User: " + u.getUsername() + ", Password: " + u.getPassword());
		});

        createSampleAdmin();              // ⚠️ Admin d'abord
        createSampleCities();             // ⚠️ Puis villes
        createSampleMobilityServices();   // ⚠️ Puis services
        createSampleOffers();             // ⚠️ Puis offres
        createSampleReservations();       // ⚠️ Enfin réservations
		createSampleReservations();
	}


	private void createSampleOffers() {
	    System.out.println("📦 Creating sample offers...");

	    if (offerRepository.count() == 0) {
	        // Créer d'abord les entités nécessaires
	        createSampleCities();
	        createSampleMobilityServices();
	        createSampleAdmin();

	        // Récupérer les entités créées
	        List<City> cities = cityRepository.findAll();
	        List<MobilityService> services = mobilityServiceRepository.findAll();
	        Admin admin = adminRepository.findAll().stream().findFirst().orElse(null);

	        if (cities.size() >= 2 && services.size() >= 1 && admin != null) {
	            // Offre 1
	            Offer offer1 = new Offer();
	            offer1.setPickupLocation(cities.get(0)); // Première ville
	            offer1.setReturnLocation(cities.get(0)); // Même ville
	            offer1.setMobilityService(services.get(0)); // Premier service
	            offer1.setAdmin(admin);
	            offer1.setPickupDatetime(LocalDateTime.now().plusDays(1)); // Demain
	            offer1.setDescription("Vélo électrique parfait pour les déplacements urbains. Batterie longue durée, confortable et facile à utiliser.");
	            offer1.setPrice(new BigDecimal("15.50"));
	            offerRepository.save(offer1);

	            // Offre 2
	            Offer offer2 = new Offer();
	            offer2.setPickupLocation(cities.get(1)); // Deuxième ville
	            offer2.setReturnLocation(cities.get(1)); // Même ville
	            offer2.setMobilityService(services.get(0)); // Même service ou différent
	            offer2.setAdmin(admin);
	            offer2.setPickupDatetime(LocalDateTime.now().plusDays(2)); // Après-demain
	            offer2.setDescription("Scooter 125cc confortable pour tous vos trajets en ville. Idéal pour les déplacements professionnels.");
	            offer2.setPrice(new BigDecimal("32.00"));
	            offerRepository.save(offer2);

	            System.out.println("✅ 2 sample offers created");
	        } else {
	            System.out.println("❌ Missing required entities to create offers");
	        }
	    } else {
	        System.out.println("ℹ️ Offers already exist: " + offerRepository.count());
	    }
	}



	private void createSampleCities() {
	    System.out.println("🏙️ Creating sample cities...");

	    if (cityRepository.count() == 0) {
	        City city1 = new City();
	        city1.setName("Paris");
	        city1.setPostalCode("75000");
	        cityRepository.save(city1);

	        City city2 = new City();
	        city2.setName("Lyon");
	        city2.setPostalCode("69000");
	        cityRepository.save(city2);

	        System.out.println("✅ 2 sample cities created");
	    }
	}

	private void createSampleMobilityServices() {
	    System.out.println("🚗 Creating sample mobility services...");

	    if (mobilityServiceRepository.count() == 0) {
	        MobilityService service1 = new MobilityService();
	        service1.setName("EcoMobility");
	        service1.setDescription("Service de mobilité urbaine durable");
	        service1.setCategorie("BIKE_SHARING");
	        mobilityServiceRepository.save(service1);

	        System.out.println("✅ 1 sample mobility service created");
	    }
	}

	private void createSampleAdmin() {
	    System.out.println("👨‍💼 Creating sample admin...");

	    if (adminRepository.count() == 0) {
	        Admin admin = new Admin();
	        admin.setUsername("admin");
	        admin.setEmail("admin@mobility.com");
	        admin.setPassword(passwordEncoder.encode("admin123"));
	        admin.setRole("ROLE_ADMIN");
	        admin.setAdminLevel(1);
	        adminRepository.save(admin);

	        System.out.println("✅ 1 sample admin created");
	    }
	}

	private void createSampleReservations() {
	    System.out.println("📋 Creating sample reservations...");

	    if (reservationRepository.count() == 0) {
	        // Récupérer l'utilisateur testuser
	        User testUser = userRepository.findByUsername("testuser")
	            .orElseThrow(() -> new RuntimeException("Test user not found"));

	        // Créer d'abord quelques offres si elles n'existent pas
	        createSampleOffers();

	        // Récupérer les offres créées
	        List<Offer> offers = offerRepository.findAll();

	        if (offers.size() >= 2) {
	            // Réservation 1
	            Reservation reservation1 = new Reservation();
	            reservation1.setUser(testUser); // ⚠️ Utiliser l'objet User, pas l'ID
	            reservation1.setOffer(offers.get(0)); // ⚠️ Utiliser l'objet Offer, pas l'ID
	            reservation1.setReservationDate(LocalDateTime.now().plusDays(1)); // Demain
	            reservation1.setStatus(Reservation.ReservationStatus.PENDING);
	            reservationRepository.save(reservation1);

	            // Réservation 2
	            Reservation reservation2 = new Reservation();
	            reservation2.setUser(testUser);
	            reservation2.setOffer(offers.get(1)); // Deuxième offre
	            reservation2.setReservationDate(LocalDateTime.now().plusDays(2)); // Après-demain
	            reservation2.setStatus(Reservation.ReservationStatus.CONFIRMED);
	            reservationRepository.save(reservation2);

	            System.out.println("✅ 2 sample reservations created");
	        } else {
	            System.out.println("❌ Not enough offers to create reservations");
	        }
	    } else {
	        System.out.println("ℹ️ Reservations already exist: " + reservationRepository.count());
	    }
	}
}
Mobility Backend API
🚀 Technologies

Java 17 avec Spring Boot 3.x
Spring Security + JWT Authentication
Hibernate/JPA avec base H2 (Développement)
Maven, JUnit 5, Mockito
Swagger/OpenAPI 3 Documentation

📦 Installation & Démarrage
# Cloner le projet (si nécessaire)
git clone [votre-repo-url]
cd backend

# Compiler et démarrer l'application
mvn spring-boot:run

L'application sera accessible sur : http://localhost:8088
📚 Documentation API

Swagger UI : http://localhost:8088/swagger-ui.html
OpenAPI JSON : http://localhost:8088/v3/api-docs

🔐 Authentication
S'inscrire (Register)
curl -X POST http://localhost:8088/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "utilisateur",
    "email": "user@test.com",
    "password": "motdepasse",
    "role": "ROLE_USER"
  }'

Se connecter (Login)
curl -X POST http://localhost:8088/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "utilisateur",
    "password": "motdepasse"
  }'

📋 Endpoints Principaux
🔐 Authentication (Public)

POST /api/auth/register - Créer un compte
POST /api/auth/login - Se connecter

👥 Users (Protégé - USER/ADMIN)

GET /api/users - Liste tous les utilisateurs
GET /api/users/{id} - Obtenir un utilisateur par ID
PUT /api/users/{id} - Modifier un utilisateur
DELETE /api/users/{id} - Supprimer un utilisateur

🏙️ Cities (Protégé - USER/ADMIN)

GET /api/cities - Liste toutes les villes
POST /api/cities - Créer une ville
GET /api/cities/{id} - Obtenir une ville par ID

🚲 Mobility Services (Protégé - USER/ADMIN)

GET /api/mobility-services - Liste tous les services
POST /api/mobility-services - Créer un service
GET /api/mobility-services/{id} - Obtenir un service par ID

🎯 Offers (Protégé - USER/ADMIN)

GET /api/offers - Liste toutes les offres
POST /api/offers - Créer une offre
GET /api/offers/{id} - Obtenir une offre par ID

👨‍💼 Admin (Protégé - ADMIN uniquement)

GET /api/admins - Endpoints d'administration
GET /api/admins/{id} - Gestion admin

👥 Rôles & Permissions

ROLE_USER : Accès aux utilisateurs, villes, services, offres
ROLE_ADMIN : Accès complet + endpoints d'administration

🗄️ Base de Données

Développement : H2 en mémoire
Console H2 : http://localhost:8088/h2-console
URL JDBC : jdbc:h2:mem:mobilitydb
Utilisateur : sa
Mot de passe : (vide)

🧪 Tests
# Lancer tous les tests
mvn test

# Lancer des tests spécifiques
mvn test -Dtest=UserControllerTest
mvn test -Dtest=*ServiceTest

🛠️ Développement
# Compilation
mvn compile

# Nettoyage
mvn clean

# Packaging
mvn package

📁 Structure du Projet
src/
├── main/
│   ├── java/com/mobility/mobility_backend/
│   │   ├── controller/     # Contrôleurs REST
│   │   ├── service/        # Logique métier
│   │   ├── repository/     # Accès données
│   │   ├── entity/         # Entités JPA
│   │   ├── dto/           # Data Transfer Objects
│   │   └── config/        # Configurations
│   └── resources/
│       └── application.properties
└── test/
    └── java/com/mobility/mobility_backend/

🔧 Configuration
Fichier application.properties :
server.port=8088
spring.datasource.url=jdbc:h2:mem:mobilitydb
spring.jpa.hibernate.ddl-auto=create-drop
spring.security.enabled=true

🚀 Déploiement
Pour la production :

Changer H2 par MySQL/PostgreSQL
Configurer les variables d'environnement
Utiliser un vrai serveur JWT

Développé avec ❤️ using Spring Boot & Java
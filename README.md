# Application Fullstack de Gestion de Services de Mobilité  
## Avec Interface d'Administration Complète

---

## 🛠️ Stack Technique

### Backend
- **Spring Boot 3** – Framework Java
- **Spring Security** – Authentification JWT
- **Spring Data JPA** – Persistance des données
- **H2 Database** – Base de données en mémoire (dev)
- **Maven** – Gestion des dépendances

### Frontend
- **Angular 16** – Framework frontend
- **TypeScript** – Langage de développement
- **RxJS** – Programmation réactive
- **Cypress** – Tests E2E

### DevOps
- **Docker** – Containerisation
- **Docker Compose** – Orchestration
- **Git** – Versioning

---

## 🚀 Démarrage Rapide

### Avec Docker (Recommandé)
```bash
# Cloner le projet
git clone <url-du-repo>

# Lancer l'application
docker-compose up --build
```

#### Accès

Frontend : http://localhost
Backend : http://localhost:8088
Console H2 : http://localhost:8088/h2-console

#### Développement Local
##### Backend  


```bash
cd backend
./mvnw spring-boot:run
```
##### Frontend
```bash 
cd frontend
npm install
npm start
```

## Fonctionnalités

- Fonctionnalité,Statut
- Gestion des utilisateurs (CRUD complet),✅
- Gestion des offres (services de mobilité),✅
- Gestion des réservations,✅
- Tableau de bord (statistiques & métriques),✅
- Authentification JWT sécurisée,✅
- Interface d’administration complète,✅


## 🧪 Tests
### Tests E2E avec Cypress

```bash 
cd frontend
npx cypress open   # Interface graphique
npx cypress run    # Mode headless (CI)
```

## Couverture des tests

- Navigation principale
- Gestion des utilisateurs
- Gestion des offres
- Gestion des réservations
- Tableau de bord & statistiques


## 📁 Structure du Projet

```bash
LocationUp/
├── backend/              # API Spring Boot
├── frontend/             # Application Angular
├── docker-compose.yml
└── README.md
```

## 👤 Accès Démo

L’application utilise un système d’auto-authentification avec des boutons de test pré-configurés pour faciliter la démonstration.

## 🛠️ Développement

Consulte les README.md individuels dans chaque dossier pour les instructions détaillées.

## 📖 Documentation Interne

###  README Backend

```bash 

# 🚀 Backend Spring Boot

API REST pour l'application **LocationUp** avec Spring Boot et sécurité JWT.

## 🏗️ Architecture
- **Spring Boot 3.5.6** – Framework principal
- **Spring Security** – Authentification JWT
- **Spring Data JPA** – Accès aux données
- **H2 Database** – Base en mémoire (développement)
- **Maven** – Gestion des dépendances

## 🔧 Configuration

### Variables d’environnement
```properties
SERVER_PORT=8088
SPRING_PROFILES_ACTIVE=dev
SPRING_DATASOURCE_URL=jdbc:h2:mem:testdb
```

## 📦 Stockage des images (upload)

- Dossier : `backend/uploads/offers` (servi via `/uploads/**` avec headers de sécurité CSP/nosniff et cache 1h).
- Quotas par défaut : 5 Mo max par fichier, 5 fichiers max par galerie, quota global 500 Mo sur le dossier (purge automatique des fichiers les plus anciens si le quota est dépassé).
- Formats acceptés : JPEG/PNG/WEBP (vérification MIME + signature binaire).
- Schéma DB : colonne `gallery_urls` (TEXT stockant une liste JSON) ajoutée via `backend/src/main/resources/schema.sql` pour compatibilité H2/embarquée.

Pour purger le dossier d’uploads en dev : supprimer `backend/uploads/offers/*` puis relancer l’appli.

## 🔐 Authentification & Sécurité

- Modèle recommandé : **JWT Bearer** envoyé via l’en-tête `Authorization: Bearer <token>` pour toutes les routes protégées (admin, favoris, réservations, paiements, etc.).
- Si vous utilisez des cookies plutôt que l’en-tête, activez **SameSite=Lax/Strict** et **Secure** (HTTPS) et **CSRF** (Spring Security) pour éviter les attaques cross-site.
- Les endpoints `@PreAuthorize` exigent des rôles explicites (`ROLE_ADMIN`, `ROLE_USER`). Exemple : création/mise à jour/suppression d’offre = admin uniquement ; favoris = user/admin.
- Le fallback d’admin implicite est supprimé : toute action admin requiert un admin authentifié.
### Profils disponibles

- dev → Développement avec H2
- docker → Déploiement Docker 


## 🚀 Démarrage

### Avec Maven

```bash   
./mvnw spring-boot:run
```


### Avec Docker

```bash 
docker-compose up backend
```

## 📚 API Endpoints
### Authentification

```bash 
POST   /api/auth/login          → Connexion utilisateur
```

## 🌐 Profils d'exécution

- **dev/demo (par défaut)** : `ddl-auto=create-drop`, `schema.sql` + `data.sql` appliqués, H2 possible. Idéal pour la démo/CI locale.
- **prod** : profil `prod` (`SPRING_PROFILES_ACTIVE=prod`) avec `ddl-auto=validate`, pas d'init SQL auto (`schema.sql/data.sql` désactivés), DB réelle (PostgreSQL par défaut). Config dans `backend/src/main/resources/application-prod.yml`.
- Flyway est activé en prod : les migrations se trouvent dans `backend/src/main/resources/db/migration` (ex: ajout `gallery_urls`, table `refresh_tokens`). Baseline automatique si aucune migration n'a été appliquée.

Pensez à fournir les variables d'env en prod (DB, secrets JWT, mails, etc.).
### Administration
```bash
GET    /api/admin/users-management            → Liste des utilisateurs
PUT    /api/admin/users-management/{id}       → Modifier un utilisateur
POST   /api/admin/users-management/{id}/role  → Changer le rôle
```

### Santé & Monitoring
```bash
GET    /actuator/health       → État de l'application
GET    /h2-console            → Console H2 (mode dev uniquement)
```


## 🧪 Données de Test

L’application crée automatiquement à l’initialisation :

- Utilisateur de test : testuser / password123
- Données de démonstration pour toutes les entités (utilisateurs, offres, réservations)



---

## 8. README Frontend

 
# 🎨 Frontend Angular

Interface utilisateur moderne pour **LocationUp**, développée avec Angular 16.

## 🛠️ Stack Technique
- **Angular 16** – Framework principal
- **TypeScript** – Typage statique
- **RxJS** – Gestion du state réactif
- **Cypress** – Tests end-to-end
- **Docker** – Containerisation
 
## 🚀 Démarrage

### Développement
 
npm install
npm start
# → http://localhost:4200 


### Production

```bash

npm run build
```

## 🧪 Tests E2E (Cypress)

```bash 
npx cypress open   # Interface graphique
npx cypress run    # Exécution en mode CI
```

### Structure des tests
```
cypress/
├── e2e/
│   ├── auth/             # Tests d'authentification
│   ├── admin/            # Tests d'administration
│   └── navigation/       # Tests de navigation
├── support/              # Commandes personnalisées
└── fixtures/             # Données de test
```

#### Exécuter un test spécifique
```
npx cypress run --spec "cypress/e2e/admin/user-management.cy.ts"
```

### 📁 Structure du Frontend

```
src/
├── app/
│   ├── components/       # Composants réutilisables
│   ├── pages/            # Pages principales
│   ├── services/         # Services API (HttpClient)
│   └── models/           # Interfaces TypeScript
├── assets/               # Images, icônes, polices
└── environments/         # Configurations par environnement
```

## 🐳 Docker
### Build de l’image

```
docker build -t locationup-frontend 
```

### Lancer le conteneur
```
docker run -p 80:80 locationup-frontend
```

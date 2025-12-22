# Application Fullstack de Mobilité (Angular + Spring Boot)

## 🛠️ Stack
- Backend : Spring Boot 3, Spring Security (JWT access + refresh), Spring Data JPA, Flyway (prod), Maven.
- Frontend : Angular 16, TypeScript, RxJS, Cypress (E2E).
- DevOps : Docker / Docker Compose, Git.

## 🚀 Démarrage rapide
### Docker
```bash
docker-compose up --build
```
Accès : Frontend http://localhost, Backend http://localhost:8088 (H2 console dev : http://localhost:8088/h2-console).
> Optionnel : vous pouvez pousser/puller les images depuis un registre (Docker Hub). Taggez vos builds (`docker tag locationup-backend:latest <namespace>/locationup-backend:latest` puis `docker push ...`) et remplacez les blocs `build:` par `image:` dans `docker-compose.yml` pour déployer sans recompiler.

### Local
Backend : `cd backend && ./mvnw spring-boot:run`  
Frontend : `cd frontend && npm install && npm start`

## Fonctionnalités principales
- Utilisateurs : CRUD, 2FA (endpoints auth/two-factor), avatars.
- Offres : CRUD, galerie multi-images, favoris, carte/quick-view (frontend), filtres/tri, activation/désactivation.
- Réservations : création/annulation/confirmation, timeline, relance paiement, dashboard et vues récentes.
- Paiements : endpoints de paiement/relance (frontend inclut un retry sur statut PENDING/REQUIRES_ACTION/FAILED).
- Identité : vérification d’identité (IdentityController) intégrée au parcours de réservation.
- Notifications : flux SSE (notifications live).
- Auth : access token court en mémoire + refresh token opaque en cookie HttpOnly/SameSite/secure, rotation/révocation en base.
- Upload : images servies via `/uploads/**` (headers CSP/nosniff), validation MIME + signature, quota 5 Mo/fichier, 5 images/galerie, quota global 500 Mo avec purge des plus anciennes.
- Administration : dashboards dédiés (offres, utilisateurs, réservations, finances), gestion des rôles, activation/désactivation d’offres, stats, favoris, et gestion des paiements/support.

## Profils & migrations
- **dev/demo (défaut)** : `ddl-auto=create-drop`, `schema.sql` + `data.sql`, H2 ok.
- **prod** : `SPRING_PROFILES_ACTIVE=prod`, `ddl-auto=validate`, `sql.init.mode=never`, Flyway actif (`backend/src/main/resources/db/migration`). Config DB/secret JWT via env (`application-prod.yml`).

## Auth & sécurité
- Access token en mémoire (frontend) et rafraîchi via `/api/auth/refresh` (cookie refresh HttpOnly).
- Endpoints protégés par `@PreAuthorize` (admin pour CRUD offres, etc.). Pas de fallback admin.
- Cookies refresh : HttpOnly, SameSite=Lax, Secure (prévoir HTTPS en prod). CSRF à activer si vous utilisez les cookies côté front.

## Tests
- E2E : `cd frontend && npx cypress open` ou `npx cypress run`.

## Endpoints clés (exemples)
- Auth : `POST /api/auth/login`, `POST /api/auth/register`, `POST /api/auth/refresh`
- Offres : `GET /api/offers`, `POST /api/offers` (admin), favoris `/api/offers/{id}/favorite`
- Réservations : `GET /api/reservations`, `POST /api/reservations`, timeline `/api/reservations/{id}/timeline`
- Paiements : `/api/payments/**` (retry, statut)
- Admin : `/api/admin/**` (users, offers, bookings, dashboard, finances)
- Identité : `/api/identity/**`

## Limites / TODO
- CSRF : à activer si vous utilisez les cookies côté front (ou renforcer SameSite/Origin checks).
- Upload : pas de scan AV ni de nettoyage programmé au-delà de la purge quota (prévoir un job/AV si prod).
- JWT : implémentation maison (pour prod, préférer une lib standard et rotation de clé).
- Tests : pas de tests backend automatisés sur le flux refresh/sanitization ; à ajouter avant prod.

## Structure
```
LocationUp/
├── backend/
├── frontend/
├── docker-compose.yml
└── README.md
```

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

## 🐳 Docker (build manuel)
- Frontend :  
  ```bash
  docker build -t locationup-frontend ./frontend
  docker run -p 80:80 locationup-frontend
  ```
- Backend :  
  ```bash
  docker build -t locationup-backend ./backend
  docker run -p 8080:8088 locationup-backend
  ```
  (Fournissez vos propres variables d’env Stripe/DB/JWT via un fichier `.env` non versionné ou via `-e`. Les clés réelles ne sont pas incluses ; utilisez `.env.example` comme modèle.)

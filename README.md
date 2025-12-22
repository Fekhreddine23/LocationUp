
 # Application Fullstack de Mobilité (Angular + Spring Boot)

## 🛠️ Stack
- Backend : Spring Boot 3, Spring Security (JWT access + refresh), Spring Data JPA, Flyway (prod), Maven.
- Frontend : Angular 16, TypeScript, RxJS, Cypress (E2E).
- DevOps : Docker / Docker Compose, Git.

## 🚀 Démarrage rapide
### Docker (recommandé)
```bash
# à la racine, avec vos variables dans .env (voir .env.example)
docker-compose --env-file .env up --build -d
```
Accès : Frontend http://localhost  
API : http://localhost:8080 (healthcheck : http://localhost:8080/actuator/health)  
H2 console (dev/docker) : http://localhost:8080/h2-console
> Optionnel : pousser/puller les images vers un registre (Docker Hub). Taggez puis `docker push <ns>/locationup-backend` et `<ns>/locationup-frontend`, et remplacez `build:` par `image:` dans `docker-compose.yml` pour déployer sans rebuild.

### Local
- Backend : `cd backend && ./mvnw spring-boot:run`
- Frontend : `cd frontend && npm install && npm start`

### Docker (build manuel)
- Frontend : `docker build -t locationup-frontend ./frontend && docker run -p 80:80 locationup-frontend`
- Backend : `docker build -t locationup-backend ./backend && docker run -p 8080:8088 locationup-backend`  
  (Fournissez vos propres variables d’env Stripe/DB/JWT via `.env` ou `-e`; les clés réelles ne sont pas incluses, utilisez `.env.example` comme modèle.)

## Fonctionnalités principales
- Utilisateurs : CRUD, 2FA (endpoints auth/two-factor), avatars.
- Offres : CRUD, galerie multi-images, favoris, carte/quick-view (frontend), filtres/tri, activation/désactivation.
- Réservations : création/annulation/confirmation, timeline, relance paiement, dashboard et vues récentes.
- Paiements : endpoints de paiement/relance (retry côté front sur PENDING/REQUIRES_ACTION/FAILED).
- Identité : vérification d’identité intégrée au parcours de réservation.
- Notifications : flux SSE (notifications live).
- Auth : access token court en mémoire (front) + refresh token opaque en cookie HttpOnly/SameSite/secure, rotation/révocation en base.
- Upload : images servies via `/uploads/**` (headers CSP/nosniff), validation MIME + signature, quota 5 Mo/fichier, 5 images/galerie, quota global 500 Mo avec purge des plus anciennes.
- Administration : dashboards (offres, utilisateurs, réservations, finances), gestion des rôles, activation/désactivation d’offres, stats, favoris, paiements/support.

## Profils & migrations
- **dev/demo (défaut)** : `ddl-auto=create-drop`, `schema.sql` + `data.sql`, H2 OK.
- **prod** : `SPRING_PROFILES_ACTIVE=prod`, `ddl-auto=validate`, `sql.init.mode=never`, Flyway actif (`backend/src/main/resources/db/migration`). Config DB/secret JWT via env (`application-prod.yml`).

## Auth & sécurité
- Access token en mémoire (front), rafraîchi via `/api/auth/refresh` (cookie refresh HttpOnly).
- Endpoints protégés par `@PreAuthorize` (admin pour CRUD offres, etc.), pas de fallback admin.
- Cookies refresh : HttpOnly, SameSite=Lax, Secure (HTTPS requis en prod). CSRF à activer si vous utilisez les cookies côté front.

## Endpoints clés (exemples)
- Auth : `POST /api/auth/login`, `POST /api/auth/register`, `POST /api/auth/refresh`
- Offres : `GET /api/offers`, `POST /api/offers` (admin), favoris `/api/offers/{id}/favorite`
- Réservations : `GET /api/reservations`, `POST /api/reservations`, timeline `/api/reservations/{id}/timeline`
- Paiements : `/api/payments/**` (retry, statut)
- Admin : `/api/admin/**` (users, offers, bookings, dashboard, finances)
- Identité : `/api/identity/**`

## Tests
- E2E : `cd frontend && npx cypress open` ou `npx cypress run`.

## Limites / TODO
- CSRF : à activer si vous utilisez les cookies côté front (ou renforcer SameSite/Origin checks).
- Upload : pas de scan AV ni de nettoyage programmé au-delà de la purge quota (prévoir un job/AV si prod).
- JWT : implémentation maison (en prod, préférer une lib standard et rotation de clé).
- Tests : pas de tests backend automatisés sur le flux refresh/sanitization ; à ajouter avant prod.

## Structure
```
LocationUp/
├── backend/
├── frontend/
├── docker-compose.yml
└── README.md
```

> Utilisateur de test (démo) : testuser / password123.  
> Données de démo seedées en dev (utilisateurs, offres, réservations).  
> README frontend détaillé : voir `frontend/README.md`.


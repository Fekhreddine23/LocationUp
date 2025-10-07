# 🧩 Évaluation de candidats  
## 🚀 Projet Full Stack

---

## 🎯 Objectif

Évaluer la capacité du candidat à **concevoir, développer, tester et livrer** une application complète intégrant un **Frontend**, un **Backend**, et un **pipeline DevOps**, tout en respectant les bonnes pratiques de qualité, de sécurité et de documentation.

---

## 🧠 Contexte du projet

Tu participes à la conception d’une application **de gestion d’offres de location** (par exemple : voitures, vélos, équipements, etc.).  
L’application doit permettre aux utilisateurs de consulter et filtrer des offres, et aux administrateurs de les gérer.

---

## 📜 User Stories

### 👤 Utilisateur :
- Consulter la liste des offres disponibles  
- Filtrer les offres par :
  - Ville de départ (`pickupLocation`)
  - Date de départ (`pickupDatetime`)
  - Ville de retour (`returnLocation`)
  - Type de service (`mobilityService`)

### 👨‍💼 Administrateur :
- Créer une nouvelle offre  
- Modifier une offre existante  
- Supprimer une offre  

---

## ⚙️ Exigences techniques

### ✅ Obligatoires

| Domaine | Exigence | Détails |
|----------|-----------|---------|
| **Backend** | Spring Boot (ou équivalent Java) | API REST complète |
| **Frontend** | Angular / React / Vue.js | Interface web responsive et fonctionnelle |
| **Base de données** | PostgreSQL / MongoDB / H2 | Persistance des offres |
| **Sécurité** | JWT ou OAuth2 | Authentification & rôles (user/admin) |
| **Tests** | Unitaires & intégration | ≥ 60% de couverture |
| **CI/CD** | GitHub Actions ou GitLab CI | Build + Tests automatisés |
| **Containerisation** | Dockerfile + docker-compose | Exécution locale |
| **Documentation** | Swagger + README | Instructions claires |

### 🧪 Optionnels (bonus)
- Déploiement cloud (AWS, Azure, Render, EKS, etc.)  
- Monitoring (Spring Actuator, Prometheus, Grafana)  
- Appel d’une API externe (Feign client ou HttpClient)  
- Gestion d’état frontend (Redux, NgRx, Pinia)  
- Responsive design (Tailwind, Material UI, Bootstrap)

---

## 🕓 Durée
⏱️ **3 à 5 jours maximum**

Le candidat choisit ses priorités et justifie ses choix techniques dans la documentation.

---

## 📦 Livrables attendus

1. Lien du dépôt **GitHub** ou **GitLab**  
2. **README clair** contenant :
   - Instructions d’installation et d’exécution  
   - Description des endpoints (Swagger ou Postman)  
   - Explication du pipeline CI/CD  
   - (Optionnel) Lien de démo du déploiement  
3. Code source propre et organisé  
4. Tests unitaires et d’intégration  
5. Captures d’écran ou lien vers l’interface frontend  

---

## 🧩 Structure suggérée du projet

```
fullstack-evaluation/
│
├── backend/
│   ├── src/main/java/... 
│   ├── Dockerfile
│   └── README.md
│
├── frontend/
│   ├── src/
│   ├── Dockerfile
│   └── README.md
│
├── docker-compose.yml
└── .gitlab-ci.yml / .github/workflows/
```

---

## 📊 Matrice de compétences

### 🧱 Backend

| Compétence | Description | Pondération |
|-------------|--------------|--------------|
| Architecture & Design | Structure claire, modularité, séparation des couches | 10% |
| API REST | Respect des conventions REST, DTO, statuts HTTP | 10% |
| Sécurité | JWT, rôles, validation des entrées | 10% |
| Tests | Unitaires & intégration (MockMvc, JUnit, Testcontainers) | 10% |
| Documentation | Swagger, README backend complet | 5% |

---

### 🎨 Frontend

| Compétence | Description | Pondération |
|-------------|--------------|--------------|
| Architecture du projet | Organisation modulaire (components, services, routing) | 10% |
| UI/UX | Interface claire, responsive et intuitive | 10% |
| Communication API | Appels HTTP vers le backend (Axios, HttpClient, etc.) | 10% |
| Gestion d’état | Utilisation de store global (Redux, NgRx, Pinia) | 5% |
| Tests | Tests unitaires (Jest, Jasmine, Vitest) | 5% |
| Documentation | README frontend clair | 5% |

---

### ⚙️ DevOps

| Compétence | Description | Pondération |
|-------------|--------------|--------------|
| CI/CD | Pipeline automatisé : build, test, analyse | 10% |
| Dockerisation | Dockerfile & docker-compose fonctionnels | 10% |
| Déploiement (optionnel) | Cloud, container registry, scripts IaC | 5% |
| Monitoring (optionnel) | Actuator, Prometheus, Grafana | 5% |

---

### 🧾 Qualité globale

| Compétence | Description | Pondération |
|-------------|--------------|--------------|
| Clean Code | Nommage clair, lisibilité, principes SOLID | 5% |
| Git Workflow | Commits clairs, branches, messages pertinents | 5% |
| Documentation globale | Complète, structurée et pédagogique | 5% |

---

## 🔢 Total : 100 points

✅ **Seuil de réussite : 75 points**

---

## 🗣️ Entretien de soutenance (45 à 60 min)

### Objectifs :
- Démonstration du projet (frontend + backend)
- Explication des choix techniques
- Discussion sur la sécurité, les tests et la pipeline CI/CD
- Évaluation de la vision architecturelle et DevOps

### Exemples de questions :
- Comment ton application gère-t-elle les erreurs globalement ?  
- Pourquoi avoir choisi ce framework frontend ?  
- Comment assurer la scalabilité du backend ?  
- Quelle est la structure de ton pipeline CI/CD ?  
- Quelles améliorations proposerais-tu pour la production ?

---

## 💡 Conseils au candidat
> Concentre-toi sur la **qualité**, la **clarté du code** et la **documentation**.  
Les fonctionnalités optionnelles ne sont pas obligatoires, mais peuvent **valoriser ton profil**.  
Priorise un code **maintenable, testé et bien structuré**.

---

## 🏁 Bon courage et bonne réussite !
> “Un bon projet ne se juge pas par sa complexité, mais par sa clarté et sa maîtrise technique.”

---

**© 2025 — Évaluation technique Full Stack — Interne RH / Tech Recruiting**

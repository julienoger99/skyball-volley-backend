<div align="center">

# 🏐 Skyball Volley — API

**Le passeur de la plateforme : il distribue la donnée, l'appli marque le point.**

API REST Spring Boot pour gérer clubs, équipes, championnats et matchs de volley —
sécurisée par JWT, documentée en OpenAPI, prête pour la production.

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0-6DB33F?logo=springboot&logoColor=white)
![Java](https://img.shields.io/badge/Java-25-007396?logo=openjdk&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17-4169E1?logo=postgresql&logoColor=white)
![JWT](https://img.shields.io/badge/Auth-JWT-1E003D?logo=jsonwebtokens&logoColor=FFD100)
![OpenAPI](https://img.shields.io/badge/OpenAPI-3.1-85EA2D?logo=openapiinitiative&logoColor=black)

</div>

---

Skyball Volley API est le moteur derrière l'[application Flutter](../skyball_volley_frontend).
Elle expose une API REST versionnée (`/api/v1`) sécurisée par **JWT**, avec un contrôle
d'accès fin par rôles (**Membre · Manager · Admin**) appliqué directement au niveau des
services. Six modules couvrent tout le cycle d'une saison : de l'inscription d'un joueur
à la feuille de match.

## 🏐 Ce que l'API orchestre

| Module | Terrain de jeu | Endpoints clés |
|--------|----------------|----------------|
| **Authentication** | Inscription, login, vérif e‑mail, reset password | `/auth/register` · `/auth/login` · `/auth/verify` · `/auth/forgot-password` · `/auth/reset-password` · `/auth/resend-verification` |
| **Users** | Profils & annuaire | `/users` · `/users/me` · `/users/{id}` |
| **Clubs** | Clubs & adhésions | `/clubs` · `/clubs/{id}` · `/clubs/{clubId}/members` |
| **Teams** | Équipes & effectifs | `/teams` · `/teams/{id}` · `/teams/club/{clubId}` · `/teams/{teamId}/members/{userId}` |
| **Championships** | Championnats | `/championships` · `/championships/{id}` |
| **Matches** | Feuilles de match, sets, présences | `/teams/{teamId}/matches` · `/matches/{id}` · `/matches/{matchId}/sets` · `…/attendance` · `…/captain` |

## 👕 L'effectif technique

| Poste | Choix |
|-------|-------|
| Framework | **Spring Boot 4** (Web, Data JPA, Validation, Mail, Actuator) |
| Langage | **Java 25** (toolchain Gradle) |
| Base de données | **PostgreSQL 17** + **Flyway** (migrations versionnées) |
| Sécurité | **OAuth2 Resource Server** (JWT HS256) + RBAC maison via `@PreAuthorize` |
| Anti‑abus | **Bucket4j** + **Caffeine** (rate limiting) |
| Documentation | **springdoc-openapi** (Swagger UI) |
| Tests & qualité | **JUnit 5**, **Testcontainers**, **JaCoCo**, **SonarQube** |

## 🚀 Coup d'envoi

**Échauffement** — un **JDK 25**, **Docker** (pour PostgreSQL) et la variable
`JWT_SECRET_KEY` suffisent. Le Gradle wrapper est fourni, pas besoin d'installer Gradle.

```bash
# 1. Le secret JWT est obligatoire (clé HS256 — une chaîne longue et aléatoire)
export JWT_SECRET_KEY="$(openssl rand -base64 48)"

# 2. Lancer l'API — la base PostgreSQL démarre automatiquement via compose.yaml
#    (grâce à spring-boot-docker-compose en mode dev)
./gradlew bootRun
```

L'API écoute sur **`http://localhost:8080`**, base path **`/api/v1`**.

> 💡 Pas envie de l'auto‑compose ? Démarrez la base à la main avec
> `docker compose up -d` puis lancez `./gradlew bootRun`.

### Réglages (variables d'environnement)

| Variable | Rôle | Défaut |
|----------|------|--------|
| `JWT_SECRET_KEY` | **Requis** — clé de signature JWT (HS256) | — |
| `APP_BASE_URL` | URL publique (liens e‑mail) | `http://localhost:8080` |
| `MAIL_FROM` | Expéditeur des e‑mails | `noreply@skyball-volley.fr` |
| `SPRING_MAIL_HOST` / `_PORT` / `_USERNAME` / `_PASSWORD` | Serveur SMTP | `localhost:587` |

Tokens JWT valides **24 h** (`security.jwt.expiration-seconds=86400`), émetteur `SkyballVolley API`.

## 🔐 Sécurité & rôles

- Authentification **JWT** (resource server OAuth2) ; déconnexion via **blacklist de tokens**.
- **Vérification d'e‑mail** obligatoire et **réinitialisation de mot de passe** par token.
- Contrôle d'accès par **`SecurityService`** (`@sec.isClubManager(#id)`, `isTeamMember`,
  `isSelf`…) appliqué en `@PreAuthorize` sur les services — `AccessDeniedException → 403`.
- Rôles **MEMBER · MANAGER · ADMIN** par club et par équipe.
- **Rate limiting** (Bucket4j) sur les endpoints sensibles.

## 📖 Documentation API

Une fois l'API lancée :

- **Swagger UI** → http://localhost:8080/swagger-ui/index.html
- **OpenAPI JSON** → http://localhost:8080/v3/api-docs

## 🗄️ Base de données

Le schéma est géré **entièrement par Flyway** (`src/main/resources/db/migration`,
`V1` → `V14`) : clubs, users, équipes, championnats, matchs, sets, présences,
memberships, audit trail, vérification e‑mail et reset password. Hibernate est en
`ddl-auto=validate` — les migrations font foi.

## 🧪 Tests & qualité

```bash
./gradlew test                 # tests unitaires + intégration (Testcontainers)
./gradlew jacocoTestReport     # rapport de couverture
```

Les tests d'intégration démarrent un PostgreSQL jetable via **Testcontainers** — aucune
base locale requise.

## 🗺️ Architecture

```
src/main/java/com/skyball/volley/
├── auth/          # JWT, filtres, vérif e‑mail, reset password, RBAC
├── user/          # utilisateurs & profils
├── club/          # clubs & memberships
├── team/          # équipes & memberships
├── championship/  # championnats
├── match/         # matchs, sets, présences, capitaine
├── common/        # briques partagées
└── config/        # configuration transverse
```

Chaque module suit le même schéma : `controller → service (@PreAuthorize) → repository`,
avec des DTO exposés en sortie (`static from(entity)`).

---

<div align="center">

Partie de la plateforme **Skyball Volley** · Client : [skyball_volley_frontend](../skyball_volley_frontend)

🏐 *Bola pra cima !*

</div>

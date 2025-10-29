# Wendy’s Family Tree

**Name:** Devid Marlon Greta  
**Matrikelnummer:** e12223206

---

## Stundenliste

| Datum      | Dauer | Story | Beschreibung |
|-------------|-------|-------|---------------|
| 2025-10-12  | 2 h   | US1   | Setup, DB-Schema, DAO erstellt |
| 2025-10-13  | 3 h   | US1   | Service, REST, Angular-Form |
| 2025-10-14  | 1 h   | US1   | Tests, Logging |
| 2025-10-20  | 2.5 h | US2   | Owner-Relation implementiert (Backend, DB, Tests) |
| 2025-10-21  | 3 h   | US2   | Angular-Form mit Owner-Autocomplete & Delete-Button |
| 2025-10-22  | 1 h   | US2   | Bugfixes, Dropdown getestet, Dokumentation ergänzt |
| 2025-10-22  | 1.5 h | US3   | Delete-Funktion Backend + REST + Tests implementiert |
| 2025-10-23  | 3 h   | US4   | Backend-DTOs erweitert (mother/father), Validator angepasst |
| 2025-10-23  | 2 h   | US4   | HorseService + REST + Frontend angepasst (Form + Autocomplete) |
| 2025-10-23  | 1.5 h | US4   | Tests + manuelle Prüfung der Elternlogik (Validierung, UI) |
| 2025-10-24  | 3 h   | US5   | Detailansicht implementiert (Frontend-Routing, Component, Template, Owner-Mapping) |
| 2025-10-24  | 2.5 h | US6   | Backend: HorseSearchDto + Endpoint erweitert, DAO + Service angepasst |
| 2025-10-25  | 3 h   | US6   | Frontend: Filterformular, Binding, Autocomplete (Owner), Service-Integration |
| 2025-10-25  | 1.5 h | US6   | Tests + Bugfixes (DatePipe, Enum Sex, Binding, Routing) |
| 2025-10-26  | 3 h   | US7   | Owner-Verwaltung implementiert (Backend + Frontend, Toastr, Cancel-Button, Styling) |
| 2025-10-27  | 1.5 h | US8   | Owner-Liste (Anzeige aller Besitzer:innen) umgesetzt, Styling an Pferdeliste angepasst |
| **Summe**   | **33 h** |     |               |

---

## Hinweise zur Projektstruktur

- **Backend:** Spring Boot (Java 21, Maven)
    - REST-konform, Logging gemäß TS10
    - Tests für REST, Service und DAO vorhanden
    - Validierung und Exception Handling implementiert

- **Frontend:** Angular 18
    - Routen für Horses und Owners
    - Template-driven Forms
    - Toastr für User Feedback
    - Responsive Tabellen und Formulare

---

## Start des Projekts

**Backend & Frontend:**
```bash
cd backend
mvn clean package
mvn spring-boot:run -Dspring-boot.run.profiles=Datagen


cd frontend
npm ci
npm run start

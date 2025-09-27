# 📊 Gestion des Abonnements et Paiements

Application **console en Java 8** permettant de gérer des **abonnements** (avec ou sans engagement) et leurs **paiements**.  
Le projet respecte une **architecture en couches** (UI, Service, DAO, Utilitaires) et utilise **PostgreSQL via JDBC** pour la persistance.

---

## 🚀 Fonctionnalités

- 📌 Gestion des abonnements :
    - Créer un abonnement (avec ou sans engagement)
    - Modifier un abonnement
    - Supprimer un abonnement
    - Consulter la liste des abonnements

- 💰 Gestion des paiements :
    - Enregistrer un paiement
    - Modifier un paiement
    - Supprimer un paiement
    - Afficher les paiements d’un abonnement
    - Afficher les 5 derniers paiements

- ⚠️ Gestion des impayés :
    - Consulter les paiements manqués
    - Calculer le montant total impayé (uniquement pour abonnements avec engagement)

- 📊 Rapports financiers :
    - Somme payée d’un abonnement
    - Rapport mensuel
    - Rapport annuel
    - Rapport des impayés

---


---

## 🛠️ Technologies utilisées

- **Java 8**
- **JDBC** (connexion à la base de données)
- **PostgreSQL**
- **Stream API / Lambda / Collectors**
- **Gestion des exceptions**

---

## 📂 Base de données (PostgreSQL)

### Création des tables :

```sql
CREATE TABLE abonnement (
    id SERIAL PRIMARY KEY,
    nom_service VARCHAR(100) NOT NULL,
    montant_mensuel NUMERIC(10,2) NOT NULL,
    date_debut DATE NOT NULL,
    type VARCHAR(50) NOT NULL,
    duree_engagement INT
);

CREATE TABLE paiement (
    id SERIAL PRIMARY KEY,
    id_abonnement INT REFERENCES abonnement(id) ON DELETE CASCADE,
    date_echeance DATE NOT NULL,
    date_paiement DATE,
    type_paiement VARCHAR(50),
    statut VARCHAR(50) NOT NULL
);
```

## ⚙️ Installation
Pour installer et lancer le projet, suivez ces étapes :

Cloner le projet

## Bash

git clone https://github.com/ton-projet.git
cd ton-projet
Compiler les classes Java

## Bash

javac -cp .:postgresql-42.7.1.jar src/main/java/**/*.java
Lancer l’application

## Bash

java -cp .:postgresql-42.7.1.jar src/main/java/ui/Main
## 🖥️ Utilisation
Au lancement, un menu console s’affiche avec les options suivantes :

== MENU PRINCIPAL ==
1  - Créer un abonnement (avec/sans engagement)
2  - Modifier un abonnement
3  - Supprimer un abonnement
4  - Consulter la liste des abonnements
5  - Afficher les paiements d'un abonnement
6  - Enregistrer un paiement
7  - Modifier un paiement
8  - Supprimer un paiement
9  - Consulter les paiements manqués
10 - Afficher la somme payée d'un abonnement
11 - Afficher les 5 derniers paiements
12 - Générer rapports financiers
0  - Quitter
## Exemples
Créer un abonnement

## Nom du service : Orange
Montant mensuel : 100
Date début (YYYY-MM-DD) : 2023-02-24
Type (1=AVEC_ENGAGEMENT, 2=SANS_ENGAGEMENT) : 1
Durée engagement (mois) : 12
→ Affiche les détails de l’abonnement avec un ID unique.

## Lister les abonnements

ID: 1 | Service: Orange | Montant: 100.00 | Début: 2023-02-24 | Type: AVEC_ENGAGEMENT | Durée: 12 mois
📌 Améliorations possibles
Ajouter des tests unitaires (JUnit).

Sauvegarder les rapports générés dans des fichiers .csv ou .pdf.

Créer une interface graphique avec JavaFX ou une API REST avec Spring Boot.

## 👩‍💻 Auteur
Développé par Hanane Oubaha – Projet de gestion des abonnements avec Java & PostgreSQL.

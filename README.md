# ⚡ Kilowatch - Plateforme de Suivi de Consommation d'Énergie Post-Payée (ENEO)

Application de gestion numérique de la relève des index de consommation électrique,
destinée aux techniciens ENEO. Elle permet de saisir les index de KWh des abonnés
(catégories **Social**, **Résidentiel**, **Industriel**) et de générer automatiquement
les factures correspondantes, TVA incluse.

---

## 📋 Table des matières

- [⚡ Kilowatch - Plateforme de Suivi de Consommation d'Énergie Post-Payée (ENEO)](#-kilowatch---plateforme-de-suivi-de-consommation-dénergie-post-payée-eneo)
  - [📋 Table des matières](#-table-des-matières)
  - [🎯 Contexte](#-contexte)
  - [✨ Fonctionnalités](#-fonctionnalités)
  - [🏗 Architecture du projet](#-architecture-du-projet)
  - [📦 Modèle de données](#-modèle-de-données)
  - [💰 Règles de tarification](#-règles-de-tarification)
  - [🔧 Prérequis](#-prérequis)
  - [📥 Installation](#-installation)
  - [▶️ Exécution](#️-exécution)
    - [Compiler le projet](#compiler-le-projet)
    - [Construire le JAR exécutable](#construire-le-jar-exécutable)
    - [Lancer l'application (interface Swing)](#lancer-lapplication-interface-swing)
    - [Lancer sans packager (développement rapide, via IDE)](#lancer-sans-packager-développement-rapide-via-ide)
  - [✅ Tests](#-tests)
    - [Lancer tous les tests](#lancer-tous-les-tests)
    - [Lancer une classe de test spécifique](#lancer-une-classe-de-test-spécifique)
    - [Lancer une méthode de test spécifique](#lancer-une-méthode-de-test-spécifique)
    - [Couverture des tests](#couverture-des-tests)
  - [📁 Structure des packages](#-structure-des-packages)
  - [🛠 Stack technique](#-stack-technique)
  - [📄 Licence](#-licence)

---

## 🎯 Contexte

Pour numériser la relève des index, cette application permet aux techniciens ENEO de :
- Saisir les consommations de KWh des abonnés sur le terrain
- Calculer automatiquement les factures avec application de la TVA selon la catégorie
- Gérer un portefeuille d'abonnés répartis en trois catégories tarifaires

---

## ✨ Fonctionnalités

- **Gestion des abonnés** : ajout, modification, suppression, recherche (par ID, nom, catégorie)
- **Saisie de relève** : enregistrement de l'index actuel, calcul automatique de la consommation
- **Facturation automatique** : calcul du montant HT, TVA et TTC selon la catégorie de l'abonné
- **Facturation en masse** : traitement d'un lot de relèves en une seule opération
- **Suivi des paiements** : marquage des factures comme payées/impayées
- **Statistiques** : recettes totales, recettes par catégorie, consommation totale
- **Interface graphique Swing** : saisie de relève et consultation de la liste des abonnés

---

## 🏗 Architecture du projet

L'application suit une architecture en couches, avec une séparation stricte entre :

| Couche | Rôle |
|---|---|
| `model` | Entités métier (Abonné et sous-types, Relève, Facture, Technicien) |
| `interfaces` | Contrats (`Identifiable`, `Facturable`) |
| `service` | Algorithmes métier et gestion des collections (`GestionnaireAbonnes`, `MoteurFacturation`) |
| `exception` | Exceptions métier personnalisées |
| `util` | Constantes tarifaires et générateurs d'identifiants |
| `view` | Interface graphique Swing |

---

## 📦 Modèle de données

| Classe | Description |
|---|---|
| `Abonne` *(abstract)* | Classe mère : id, nom, adresse, n° compteur, historique des relèves |
| `AbonneSocial` | Catégorie sociale, tarif fixe, exonérée de TVA |
| `AbonneResidentiel` | Catégorie résidentielle, tarification par tranches |
| `AbonneIndustriel` | Catégorie industrielle, tarif + prime de puissance souscrite (kVA) |
| `Releve` | Index précédent, index actuel, date, technicien, calcul de consommation |
| `Facture` | Montant HT, TVA, TTC, statut payé/impayé |
| `Technicien` | Identité et zone d'affectation du technicien |

---

## 💰 Règles de tarification

| Catégorie | Tarif | TVA |
|---|---|---|
| **Social** | 50 FCFA / kWh (tarif unique) | 0 % (exonéré) |
| **Résidentiel** | 60 FCFA / kWh jusqu'à 110 kWh, puis 80 FCFA / kWh au-delà | 19,25 % |
| **Industriel** | 95 FCFA / kWh + prime fixe de 500 FCFA / kVA souscrit | 19,25 % |

> Les valeurs exactes sont centralisées dans `util.TarifConstantes` et peuvent être
> ajustées sans toucher à la logique métier.

---

## 🔧 Prérequis

- **JDK 17** ou supérieur
- **Maven 3.8+**
- (Optionnel) **VS Code** avec l'extension *Extension Pack for Java*

Vérifier les installations :

```bash
java -version
mvn -version
```

---

## 📥 Installation

```bash
git clone https://github.com/TK-Porky/Kilowatch.git
cd Kilowatch
mvn clean install
```

---

## ▶️ Exécution

### Compiler le projet

```bash
mvn clean compile
```

### Construire le JAR exécutable

```bash
mvn clean package
```

### Lancer l'application (interface Swing)

```bash
java -jar target/kilowatch.jar
```

### Lancer sans packager (développement rapide, via IDE)

Exécuter directement `MainFrame.java` (clic droit → *Run*) depuis VS Code, IntelliJ ou NetBeans.

---

## ✅ Tests

Le projet utilise **JUnit 5 (Jupiter)**.

### Lancer tous les tests

```bash
mvn test
```

### Lancer une classe de test spécifique

```bash
mvn test -Dtest=MoteurFacturationTest
```

### Lancer une méthode de test spécifique

```bash
mvn test -Dtest=MoteurFacturationTest
```

### Couverture des tests

| Classe testée | Fichier de test |
|---|---|
| `Abonne` et sous-classes | `AbonneTest.java` |
| `Releve` | `ReleveTest.java` |
| `Facture` | `FactureTest.java` |
| `GestionnaireAbonnes` | `GestionnaireAbonnesTest.java` |
| `MoteurFacturation` | `MoteurFacturationTest.java`, `MoteurFacturationExtraTest.java` |

---

## 📁 Structure des packages

```

src/main/java/com/kilowatch/
├── model/                  Entités métier
│   ├── Abonne.java          (classe abstraite)
│   ├── AbonneSocial.java
│   ├── AbonneResidentiel.java
│   ├── AbonneIndustriel.java
│   ├── Releve.java
│   ├── Facture.java
│   └── Technicien.java
│
├── interfaces/             Contrats
│   ├── Identifiable.java
│   └── Facturable.java
│
├── service/                Logique métier et algorithmes
│   ├── GestionnaireAbonnes.java
│   └── MoteurFacturation.java
│
├── exception/               Exceptions personnalisées
│   ├── AbonneNotFoundException.java
│   ├── AbonneDejaExistantException.java
│   └── ReleveInvalideException.java
│
├── util/                   Constantes et utilitaires
│   ├── TarifConstantes.java
│   └── IdGenerator.java
│
└── view/                   Interface graphique (Swing)
├── MainFrame.java
├── PanelSaisieReleve.java
└── PanelListeAbonnes.java

src/test/java/com/kilowatch/
├── model/
│   ├── AbonneTest.java
│   ├── ReleveTest.java
│   └── FactureTest.java
└── service/
    ├── MoteurFacturationTest.java
    ├── MoteurFacturationExtraTest.java
    └── GestionnaireAbonnesTest.java
```

---

## 🛠 Stack technique

| Outil | Usage |
|---|---|
| Java 17 | Langage principal |
| Maven | Build et gestion des dépendances |
| Swing | Interface graphique |
| JUnit 5 | Tests unitaires |

---

## 📄 Licence

[License MIT.](LICENSE) 
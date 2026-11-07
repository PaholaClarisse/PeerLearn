# PeerLearn

Application de tutorat entre étudiants (Université de Tours, site de Blois).
Backend Java 21 + JDBC + MySQL/MariaDB, interface graphique Swing.

## Prérequis

- JDK 21 ou plus
- Maven
- MySQL 8+ ou MariaDB (XAMPP convient)
- Un éditeur Java : VS Code (extension Java), NetBeans ou IntelliJ

## Dépendances (gérées par Maven)

Elles se téléchargent automatiquement au premier `mvn clean package` : aucune installation manuelle.

| Bibliothèque | Rôle |
|---|---|
| `mysql-connector-j` | pilote JDBC pour MySQL / MariaDB |
| `flatlaf` (3.5.4) | thème moderne de l'interface Swing (champs arrondis, barres de défilement, boutons) |

FlatLaf n'est pas indispensable : si la bibliothèque est absente, l'application démarre quand même avec le thème Nimbus de Java, un peu moins soigné. Elle est déclarée dans `pom.xml` :

```xml
<dependency>
    <groupId>com.formdev</groupId>
    <artifactId>flatlaf</artifactId>
    <version>3.5.4</version>
</dependency>
```

## Installation (à faire une seule fois)

1. **Récupérer le projet**

   ```
   git clone https://github.com/PaholaClarisse/PeerLearn.git
   cd PeerLearn
   ```

2. **Créer la base** : dans phpMyAdmin (onglet SQL) ou MySQL Workbench, connecté en `root`, exécuter dans cet ordre :
   1. `sql/schema.sql` (efface et recrée la base `peerlearn`)
   2. `sql/donnees_test.sql` (liste du campus, matières, compte administrateur)

3. **Créer le compte de l'application** (remplacer le mot de passe) :

   ```sql
   CREATE USER 'peerlearn_app'@'localhost' IDENTIFIED BY 'VotreMotDePasse';
   GRANT SELECT, INSERT, UPDATE, DELETE ON peerlearn.* TO 'peerlearn_app'@'localhost';
   ```

4. **Configurer la connexion** : copier `db.properties.example` en `db.properties` (même dossier) et y mettre ce mot de passe. Ce fichier est ignoré par Git : ne jamais le pousser.

5. **Compiler** : `mvn clean package` (la première fois, Maven télécharge le pilote MySQL et FlatLaf (voir « Dépendances »), il faut une connexion Internet).

6. **Vérifier la base** : lancer `com.peerlearn.TestConnexion`. Il doit afficher « Connexion réussie à la base : peerlearn ».

## Lancer l'application

- VS Code : ouvrir `src/main/java/com/peerlearn/PeerLearn.java` puis **Run**
- NetBeans : clic droit sur `PeerLearn.java` puis **Run File**
- Ou en ligne de commande : `mvn compile exec:java`

Des avertissements « restricted method ... native access » peuvent s'afficher avec Java 24 ou plus : ils sont sans gravité.

## Se connecter pour tester

### Administrateur

Le compte `admin@mail.fr` existe après l'import, mais son mot de passe d'origine est inconnu. Pour définir le mot de passe **PeerLearn2026**, exécuter dans la base `peerlearn` :

```sql
UPDATE utilisateurs
SET mot_de_passe_hash = '120000$k/HobdOaOgK2CsSJfIiRcA==$d8WMoj8UpTKYpkYOzNfLnL71ankoKMjFGinie5+F3xg='
WHERE email = 'admin@mail.fr';
```

Puis se connecter avec `admin@mail.fr` / `PeerLearn2026`. (Pour un autre mot de passe : lancer `com.peerlearn.GenererHash` et utiliser le hachage obtenu.)

### Étudiants

Les étudiants se créent eux-mêmes : bouton **Créer un compte** sur l'écran de connexion, avec une adresse figurant dans la liste du campus. Le niveau est lu dans cette liste et ne se saisit jamais :

| Adresse | Niveau | Rôle dans l'application |
|---|---|---|
| hugo@mail.fr | L1 | tutoré |
| alice@mail.fr | M2 | tuteur |

Un étudiant de niveau L3 ou plus accède à l'**espace tuteur** ; un étudiant L1 ou L2 accède à l'**espace tutoré**. Un tuteur n'est pas tutoré.

### Parcours de test conseillé

Ouvrir deux sessions (deux ordinateurs, ou se déconnecter entre les étapes) :

1. Créer un compte tuteur (par ex. `alice@mail.fr`), ouvrir **Mon profil tuteur**, choisir une matière, un jour et une heure, puis **Activer** le profil.
2. Créer un compte tutoré (par ex. `hugo@mail.fr`), **Trouver un tuteur**, envoyer une demande.
3. Côté tuteur : **Demandes reçues**, accepter la demande.
4. Côté tutoré et tuteur : échanger dans la **Messagerie**.
5. Côté tuteur : **Séances** (planifier, pointer les présences, rédiger un rapport), **Exercices publiés**.
6. Côté tutoré : **Exercices** (déposer un rendu), **Progression** (évaluer le tuteur).
7. Côté administrateur : **Tableau de bord**, **Statistiques**, **Rapports**, **Évaluations**, **Matières**, **Tuteurs**.

Les exercices et les rendus ne stockent que le **chemin local** du fichier : pour qu'un fichier s'ouvre, il doit se trouver sur le même ordinateur que celui qui clique sur **Ouvrir**.

## Réinitialiser les données

Pour repartir de zéro, réexécuter `sql/schema.sql` puis `sql/donnees_test.sql` (et la commande `UPDATE` du compte administrateur).

## Tests du backend

Lancer `com.peerlearn.Scenarios` : il rejoue tous les scénarios et doit se terminer par `0 ECHEC`.

**Attention** : `Scenarios` supprime tous les comptes étudiants de la base.

## Structure

- `modele` : objets du domaine (Utilisateur, Etudiant, Administrateur, Demande, SeanceTutorat...)
- `interfaces` : Accompagnant, Notifiable
- `dao` : accès à MySQL, un DAO par table (PreparedStatement uniquement)
- `service` : `GestionTutorat`, seul point d'entrée de l'interface graphique
- `util` : connexion JDBC et hachage des mots de passe (PBKDF2)
- `com.peerlearn.ui` : interface graphique Swing (connexion, tableaux de bord, écrans tuteur, tutoré et administrateur)
- `com.peerlearn.PeerLearn` : point d'entrée de l'application
- `sql` : scripts de création et de données de test

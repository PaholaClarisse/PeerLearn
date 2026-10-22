# PeerLearn

Application de tutorat entre étudiants (Université de Tours, site de Blois) : backend Java 21, JDBC et MySQL.

## Prérequis

- JDK 21 ou plus
- MySQL 8 ou plus (testé avec MySQL 9.6)
- NetBeans (ou tout éditeur avec Maven)
- MySQL Workbench

## Installation

1. **Créer la base** : dans Workbench, connecté en `root`, exécuter `sql/schema.sql` (il efface et recrée la base `peerlearn`), puis `sql/donnees_test.sql`.
2. **Créer le compte de l'application** (remplacer le mot de passe) :

```sql
   CREATE USER 'peerlearn_app'@'localhost' IDENTIFIED BY 'VotreMotDePasse';
   GRANT SELECT, INSERT, UPDATE, DELETE ON peerlearn.* TO 'peerlearn_app'@'localhost';
```

3. **Configurer la connexion** : copier `db.properties.example` en `db.properties` et y mettre ce mot de passe. Ce fichier ne doit jamais être poussé sur GitHub.
4. **Compiler** : `mvn clean package` (ou **Clean and Build** dans NetBeans).

## Vérifier l'installation

Lancer `com.peerlearn.TestConnexion` : il doit afficher « Connexion réussie à la base : peerlearn ».

Lancer ensuite `com.peerlearn.Scenarios` : il rejoue tous les scénarios et doit se terminer par `0 ECHEC`.

**Attention** : `Scenarios` supprime tous les comptes étudiants de la base.

## Structure

- `modele` : objets du domaine (Utilisateur, Etudiant, Administrateur, Demande, SeanceTutorat...)
- `interfaces` : Accompagnant, Notifiable
- `dao` : accès à MySQL, un DAO par table (PreparedStatement uniquement)
- `service` : GestionTutorat, seul point d'entrée pour l'interface graphique
- `util` : connexion JDBC et hachage des mots de passe
- `sql` : scripts de création et de données de test

## Compte administrateur de démonstration

Après l'import, lancer GenererHash avec un mot de passe de votre choix, puis remplacer admin123 dans la table par le hash obtenu
`admin@mail.fr`. Le mot de passe est celui qui a été défini avec `GenererHash`.

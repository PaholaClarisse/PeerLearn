DROP DATABASE IF EXISTS peerlearn;
CREATE DATABASE peerlearn CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE peerlearn;

CREATE TABLE etudiant_campus (
    email VARCHAR(150) PRIMARY KEY,
    niveau ENUM('L1','L2','L3','M1','M2') NOT NULL
) ENGINE=InnoDB;

CREATE TABLE matieres (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(100) NOT NULL UNIQUE
) ENGINE=InnoDB;

CREATE TABLE utilisateurs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    prenom VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    mot_de_passe_hash VARCHAR(255) NOT NULL,
    role ENUM('ETUDIANT','ADMIN') NOT NULL,
    niveau ENUM('L1','L2','L3','M1','M2') NULL,
    est_tuteur_actif BOOLEAN NOT NULL DEFAULT FALSE,
    retire BOOLEAN NOT NULL DEFAULT FALSE,
    id_matiere INT NULL,
    jour_semaine ENUM('MONDAY','TUESDAY','WEDNESDAY','THURSDAY','FRIDAY','SATURDAY','SUNDAY') NULL,
    heure_debut TIME NULL,
    capacite_max INT NULL,
    CONSTRAINT fk_utilisateur_matiere FOREIGN KEY (id_matiere) REFERENCES matieres(id),
    CONSTRAINT chk_capacite CHECK (capacite_max IS NULL OR capacite_max > 0),
    CONSTRAINT chk_role_niveau CHECK (
        (role = 'ADMIN' AND niveau IS NULL) OR (role = 'ETUDIANT' AND niveau IS NOT NULL)
    )
) ENGINE=InnoDB;

CREATE TABLE demandes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_tutore INT NOT NULL,
    id_tuteur INT NOT NULL,
    id_matiere INT NOT NULL,
    date_demande DATE NOT NULL,
    statut ENUM('EN_ATTENTE','ACCEPTEE','REFUSEE','ANNULEE','TERMINEE') NOT NULL DEFAULT 'EN_ATTENTE',
    CONSTRAINT fk_demande_tutore FOREIGN KEY (id_tutore) REFERENCES utilisateurs(id),
    CONSTRAINT fk_demande_tuteur FOREIGN KEY (id_tuteur) REFERENCES utilisateurs(id),
    CONSTRAINT fk_demande_matiere FOREIGN KEY (id_matiere) REFERENCES matieres(id),
    CONSTRAINT chk_demande_personnes CHECK (id_tutore <> id_tuteur)
) ENGINE=InnoDB;

CREATE TABLE seances_tutorat (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_tuteur INT NOT NULL,
    date_seance DATE NOT NULL,
    duree_minutes INT NOT NULL,
    statut ENUM('PLANIFIEE','ANNULEE') NOT NULL DEFAULT 'PLANIFIEE',
    CONSTRAINT fk_seance_tuteur FOREIGN KEY (id_tuteur) REFERENCES utilisateurs(id),
    CONSTRAINT chk_duree CHECK (duree_minutes > 0)
) ENGINE=InnoDB;

CREATE TABLE presences (
    id_seance INT NOT NULL,
    id_tutore INT NOT NULL,
    present BOOLEAN NOT NULL,
    PRIMARY KEY (id_seance, id_tutore),
    CONSTRAINT fk_presence_seance FOREIGN KEY (id_seance) REFERENCES seances_tutorat(id),
    CONSTRAINT fk_presence_tutore FOREIGN KEY (id_tutore) REFERENCES utilisateurs(id)
) ENGINE=InnoDB;

CREATE TABLE rapports_suivi (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_seance INT NOT NULL,
    id_tutore INT NOT NULL,
    note TINYINT NOT NULL,
    observations TEXT,
    date_rapport DATE NOT NULL,
    CONSTRAINT fk_rapport_seance FOREIGN KEY (id_seance) REFERENCES seances_tutorat(id),
    CONSTRAINT fk_rapport_tutore FOREIGN KEY (id_tutore) REFERENCES utilisateurs(id),
    CONSTRAINT chk_note_rapport CHECK (note BETWEEN 0 AND 5),
    CONSTRAINT uq_rapport UNIQUE (id_seance, id_tutore)
) ENGINE=InnoDB;

CREATE TABLE messages (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_demande INT NOT NULL,
    id_expediteur INT NOT NULL,
    contenu TEXT NOT NULL,
    date_envoi DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_message_demande FOREIGN KEY (id_demande) REFERENCES demandes(id),
    CONSTRAINT fk_message_expediteur FOREIGN KEY (id_expediteur) REFERENCES utilisateurs(id)
) ENGINE=InnoDB;

CREATE TABLE evaluations_tuteur (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_demande INT NOT NULL UNIQUE,
    note TINYINT NOT NULL,
    commentaire TEXT,
    date_evaluation DATE NOT NULL,
    CONSTRAINT fk_evaluation_demande FOREIGN KEY (id_demande) REFERENCES demandes(id),
    CONSTRAINT chk_note_evaluation CHECK (note BETWEEN 0 AND 5)
) ENGINE=InnoDB;

CREATE TABLE exercices (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_tuteur INT NOT NULL,
    titre VARCHAR(200) NOT NULL,
    type ENUM('PDF','DOC','DOCX') NOT NULL,
    chemin VARCHAR(500) NOT NULL,
    date_publication DATE NOT NULL,
    CONSTRAINT fk_exercice_tuteur FOREIGN KEY (id_tuteur) REFERENCES utilisateurs(id)
) ENGINE=InnoDB;

CREATE TABLE rendus (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_exercice INT NOT NULL,
    id_tutore INT NOT NULL,
    type ENUM('PDF','DOC','DOCX') NOT NULL,
    chemin VARCHAR(500) NOT NULL,
    date_rendu DATE NOT NULL,
    CONSTRAINT fk_rendu_exercice FOREIGN KEY (id_exercice) REFERENCES exercices(id),
    CONSTRAINT fk_rendu_tutore FOREIGN KEY (id_tutore) REFERENCES utilisateurs(id),
    CONSTRAINT uq_rendu UNIQUE (id_exercice, id_tutore)
) ENGINE=InnoDB;

CREATE TABLE notifications (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_utilisateur INT NOT NULL,
    message VARCHAR(500) NOT NULL,
    date_envoi DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    lue BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_notification_utilisateur FOREIGN KEY (id_utilisateur) REFERENCES utilisateurs(id)
) ENGINE=InnoDB;
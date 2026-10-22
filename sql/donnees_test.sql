USE peerlearn;

INSERT INTO etudiant_campus (email, niveau) VALUES
    ('pahola@mail.fr', 'L2'),
    ('yohan@mail.fr',  'L3'),
    ('alice@mail.fr',  'M2'),
    ('hugo@mail.fr',   'L1'),
    ('chloe@mail.fr',  'L1'),
    ('dylan@mail.fr',  'L1'),
    ('ines@mail.fr',   'L2'),
    ('emma@mail.fr',   'L3'),
    ('lucas@mail.fr',  'M1'),
    ('sarah@mail.fr',  'M2');

INSERT INTO matieres (nom) VALUES
    ('Algorithmique'),
    ('Mathématiques'),
    ('Physique'),
    ('Bases de données'),
    ('Programmation Java'),
    ('Anglais');

INSERT INTO utilisateurs (nom, prenom, email, mot_de_passe_hash, role)
VALUES ('Admin', 'Root', 'admin@mail.fr', 'admin123', 'ADMIN');
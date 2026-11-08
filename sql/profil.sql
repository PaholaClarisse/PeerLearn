use peerlearn;
create TABLE profil(
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_user INT NOT NULL,
    description VARCHAR(1000) NOT NULL,
    filiere VARCHAR(500) not NULL,
    photo VARCHAR(500),
    date_maj DATETIME ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_profil_user UNIQUE (id_user),
    CONSTRAINT fk_profil_user FOREIGN KEY (id_user) REFERENCES utilisateurs(id)
)ENGINE=InnoDB;
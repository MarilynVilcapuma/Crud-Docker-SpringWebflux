CREATE TABLE IF NOT EXISTS "persona" (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(50),
    last_name VARCHAR(60) ,
    dni INT,
    promotion INT,
    registration_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

INSERT INTO "persona" (first_name, last_name, dni, promotion)
VALUES ('Marilyn', 'Vilcapuma Trujillo', 74124567, 2026);

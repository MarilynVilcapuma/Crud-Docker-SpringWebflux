CREATE TABLE IF NOT EXISTS "persona" (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(50),
    last_name VARCHAR(60) ,
    dni INT,
    promotion INT,
    registration_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

CREATE TABLE IF NOT EXISTS "matricula" (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    persona_id BIGINT NOT NULL,
    course VARCHAR(80) NOT NULL,
    cycle VARCHAR(20),
    amount DECIMAL(10,2),
    status VARCHAR(10) DEFAULT 'A',
    enrollment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (persona_id) REFERENCES "persona"(id)
    );

INSERT INTO "persona" (first_name, last_name, dni, promotion)
VALUES ('Marilyn', 'Vilcapuma Trujillo', 74124567, 2026);

INSERT INTO "matricula" (persona_id, course, cycle, amount, status)
VALUES (1, 'Desarrollo de Software', '2026-I', 350.00, 'A');

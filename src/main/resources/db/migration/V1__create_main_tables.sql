CREATE TABLE sectors (

    name VARCHAR(10) PRIMARY KEY,
    base_price DECIMAL(10, 2) NOT NULL,
    max_capacity INT NOT NULL,
    open_hour TIME NOT NULL,
    close_hour TIME NOT NULL,
    duration_limit_minutes INT NOT NULL

);

CREATE TABLE spots (

    id BIGINT PRIMARY KEY,
    sector_name VARCHAR(10) NOT NULL,
    lat DECIMAL(10, 6) NOT NULL,
    lng DECIMAL(10, 6) NOT NULL,
    occupied BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_spot_sector FOREIGN KEY (sector_name) REFERENCES sectors(name)

);

CREATE TABLE parking_sessions (

    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    license_plate VARCHAR(10) NOT NULL,
    entry_time DATETIME NOT NULL,
    exit_time DATETIME,
    spot_id BIGINT,
    price_modifier_percentage DECIMAL(5, 2) NOT NULL,
    total_paid DECIMAL(10, 2),
    CONSTRAINT fk_session_spot FOREIGN KEY (spot_id) REFERENCES spots(id)

);
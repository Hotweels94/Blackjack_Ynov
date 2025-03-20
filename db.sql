CREATE DATABASE blackjack;
USE blackjack;

-- 1. Utilisateurs
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    balance DECIMAL(10,2) DEFAULT 0.00,
    status ENUM('online', 'offline', 'in_game') DEFAULT 'offline',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. File d'attente des joueurs
CREATE TABLE waiting_queue (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    table_id INT;
    join_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ip_address VARCHAR(45),
    port INT,
    status ENUM('waiting', 'matched') DEFAULT 'waiting',
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (table_id) REFERENCES game_tables(id)
);

-- 3. Tables de jeu
CREATE TABLE game_tables (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    min_bet DECIMAL(10,2) DEFAULT 0.00,
    max_players INT DEFAULT 7,
    current_players INT DEFAULT 0,
    status ENUM('open', 'waiting', 'ongoing', 'closed') DEFAULT 'open',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 4. Parties en cours
CREATE TABLE games (
    id INT AUTO_INCREMENT PRIMARY KEY,
    table_id INT NOT NULL,
    dealer_hand JSON,
    current_turn INT,
    status ENUM('waiting', 'ongoing', 'finished') DEFAULT 'waiting',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    finished_at TIMESTAMP,
    result ENUM('player1_win', 'player2_win', 'draw') DEFAULT 'draw',
    FOREIGN KEY (table_id) REFERENCES game_tables(id)
);

-- 5. Joueurs dans une partie
CREATE TABLE game_players (
    id INT AUTO_INCREMENT PRIMARY KEY,
    game_id INT NOT NULL,
    user_id INT NOT NULL,
    hand JSON,
    bet_amount DECIMAL(10,2) NOT NULL,
    status ENUM('playing', 'stand', 'bust', 'win', 'lose') DEFAULT 'playing',
    final_balance DECIMAL(10,2),
    FOREIGN KEY (game_id) REFERENCES games(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 6. Historique des parties des joueurs
CREATE TABLE game_history (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    game_id INT NOT NULL,
    result ENUM('win', 'lose', 'draw') NOT NULL, 
    total_bet DECIMAL(10,2) NOT NULL,
    payout DECIMAL(10,2),
    played_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (game_id) REFERENCES games(id)
);

-- 7. Tour de jeu
CREATE TABLE game_turns (
    id INT AUTO_INCREMENT PRIMARY KEY,
    game_id INT NOT NULL,
    player_id INT NOT NULL,
    action ENUM('hit', 'stand', 'double', 'split') NOT NULL,
    card JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (game_id) REFERENCES games(id),
    FOREIGN KEY (player_id) REFERENCES game_players(id)
);

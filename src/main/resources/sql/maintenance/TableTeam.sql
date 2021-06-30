-- チームテーブル
CREATE TABLE IF NOT EXISTS master.team(
    team_id INT AUTO_INCREMENT NOT NULL PRIMARY KEY,
    name varchar(255),
    kana varchar(255),
    mnemonic varchar(255),
    anniversary date,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

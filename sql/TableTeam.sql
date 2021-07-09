-- チームテーブル
CREATE TABLE IF NOT EXISTS main.team(
    team_id bigint NOT NULL PRIMARY KEY,
    team_name varchar(255),
    kana varchar(255),
    mnemonic varchar(255),
    anniversary date,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

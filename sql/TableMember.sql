-- メンバーテーブル
CREATE TABLE IF NOT EXISTS main.member(
    member_id bigint AUTO_INCREMENT NOT NULL PRIMARY KEY,
    team_id int,
    name varchar(255),
    kana varchar(255),
    mnemonic varchar(255),
    birthday date,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

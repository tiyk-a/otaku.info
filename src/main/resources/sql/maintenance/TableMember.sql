-- メンバーテーブル
CREATE TABLE IF NOT EXISTS master.member(
    member_id INT AUTO_INCREMENT NOT NULL PRIMARY KEY,
    group_id int,
    foreign key group_id(group_id) references master.group(group_id),
    name varchar(255),
    kana varchar(255),
    mnemonic varchar(255),
    birthday date,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

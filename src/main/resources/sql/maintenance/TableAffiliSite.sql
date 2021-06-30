-- アフィリ検索サイトテーブル
CREATE TABLE IF NOT EXISTS master.affili_site(
    affili_site_id INT AUTO_INCREMENT NOT NULL PRIMARY KEY,
    name varchar(255) UNIQUE,
    url varchar(255) UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

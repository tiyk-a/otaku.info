-- アフィリ検索サイトテーブル
CREATE TABLE IF NOT EXISTS master.affili_site(
    affili_site_id bigint AUTO_INCREMENT NOT NULL PRIMARY KEY,
    name varchar(255) UNIQUE AFTER name,
    url varchar(255) UNIQUE AFTER name,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP AFTER url,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP AFTER created_at
);

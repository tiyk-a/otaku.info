-- 商品テーブル
CREATE TABLE IF NOT EXISTS main.item(
    item_id bigint AUTO_INCREMENT NOT NULL PRIMARY KEY,
    site_id INT NOT NULL,
    item_code varchar(225) NOT NULL UNIQUE,
    url text,
    price INT,
    team_id INT NOT NULL,
    artist_id INT,
    title varchar(255),
    item_caption text,
    publication_date date,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

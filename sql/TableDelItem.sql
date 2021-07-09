-- 関係ない商品テーブル
CREATE TABLE IF NOT EXISTS main.del_item(
    del_item_id bigint AUTO_INCREMENT NOT NULL PRIMARY KEY,
    site_id INT NOT NULL,
    item_code varchar(225) NOT NULL UNIQUE,
    item_caption TEXT,
    url TEXT,
    price INT,
    team_id INT NOT NULL,
    artist_id INT,
    title text,
    publication_date date,
    created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 商品テーブル
CREATE TABLE IF NOT EXISTS master.item(
    item_id INT AUTO_INCREMENT NOT NULL PRIMARY KEY,
    site_id INT NOT NULL,
    item_code varchar(225) NOT NULL UNIQUE,
    url text,
    price INT,
    group_id INT NOT NULL,
    artist_id INT,
    foreign key group_id(group_id) references master.group(group_id),
    title varchar(255),
    publication_date date,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

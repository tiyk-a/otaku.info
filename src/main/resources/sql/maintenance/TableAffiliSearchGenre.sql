-- アフィリサイトでの検索ジャンルテーブル
CREATE TABLE IF NOT EXISTS master.affili_search_genre(
    affili_search_genre_id INT AUTO_INCREMENT NOT NULL PRIMARY KEY,
    affili_search_genre varchar(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

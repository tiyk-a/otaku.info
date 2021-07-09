-- アフィリサイトでの検索ジャンルテーブル
CREATE TABLE IF NOT EXISTS master.affili_search_genre(
    affili_search_genre_id bigint AUTO_INCREMENT NOT NULL PRIMARY KEY,
    affili_search_genre varchar(255) AFTER affili_search_genre_id,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP AFTER affili_search_genre,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP AFTER created_at
);

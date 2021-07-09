-- 露出ジャンルテーブル
CREATE TABLE IF NOT EXISTS master.exp_genre(
    exp_genre_id bigint AUTO_INCREMENT NOT NULL PRIMARY KEY,
    exp_genre varchar(255) AFTER exp_genre_id,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP AFTER exp_genre,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP AFTER created_at
);

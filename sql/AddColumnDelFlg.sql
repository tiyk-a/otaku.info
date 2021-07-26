ALTER TABLE item ADD COLUMN del_flg tinyint(1) not null;
ALTER TABLE program ADD COLUMN del_flg tinyint(1) not null;

UPDATE item SET del_flg = 0;
UPDATE program SET del_flg = 0;

INSERT INTO item (site_id, item_code,url, price, team_id, item_caption, artist_id, title, publication_date, fct_chk, created_at, del_flg) select site_id, item_code,url, price, team_id, item_caption, artist_id, title, publication_date, fct_chk, created_at, 1 from del_item;
DROP TABLE del_item;



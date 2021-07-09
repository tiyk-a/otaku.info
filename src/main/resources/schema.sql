USE main;

CREATE TABLE IF NOT EXISTS `team`
(
    team_id bigint unsigned not null,
    team_name varchar(255),
    kana varchar(255),
    mnemonic varchar(255),
    anniversary date,
    created_at timestamp default current_timestamp,
    updated_at timestamp default current_timestamp on update current_timestamp,
    primary key (`team_id`)
) ENGINE=InnoDB CHARSET = utf8mb4 comment='グループテーブル';

CREATE TABLE IF NOT EXISTS `item`
(
    item_id bigint unsigned not null auto_increment,
    site_id int not null,
    item_code varchar(225) not null,
    url text,
    price int,
    team_id int not null,
    item_caption text,
    artist_id int,
    title varchar(255),
    publication_date date,
    created_at timestamp default current_timestamp,
    updated_at timestamp default current_timestamp on update current_timestamp,
    primary key (`item_id`),
    foreign key team_id(team_id) references main.team(team_id),
    unique key uq1 (item_code)
) ENGINE=InnoDB CHARSET = utf8mb4 comment='商品テーブル';

CREATE TABLE IF NOT EXISTS `del_item`
(
    del_item_id bigint unsigned not null auto_increment,
    site_id int not null,
    item_code varchar(225) not null,
    url text,
    price int,
    team_id int not null,
    artist_id int,
    item_caption text,
    title varchar(255),
    publication_date date,
    created_at timestamp default current_timestamp,
    updated_at timestamp default current_timestamp on update current_timestamp,
    primary key (`del_item_id`),
    foreign key team_id(team_id) references main.team(team_id),
    unique key uq1 (del_item_id)
) ENGINE=InnoDB CHARSET = utf8mb4 comment='関係ない商品テーブル';


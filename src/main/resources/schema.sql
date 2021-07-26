USE main;

CREATE TABLE IF NOT EXISTS `team`
(
    team_id bigint unsigned not null,
    team_name varchar(255),
    kana varchar(255),
    mnemonic varchar(255),
    anniversary date,
    tw_id varchar(15),
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
    fct_chk tinyint(1) not null,
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
    fct_chk tinyint(1) not null,
    created_at timestamp default current_timestamp,
    updated_at timestamp default current_timestamp on update current_timestamp,
    primary key (`del_item_id`),
    foreign key team_id(team_id) references main.team(team_id),
    unique key uq1 (del_item_id)
) ENGINE=InnoDB CHARSET = utf8mb4 comment='関係ない商品テーブル';

-- メンバーテーブル
CREATE TABLE IF NOT EXISTS `member`
(
    member_id bigint unsigned AUTO_INCREMENT NOT NULL,
    team_id bigint not null,
    member_name varchar(255),
    kana varchar(255),
    mnemonic varchar(255),
    birthday date,
    created_at timestamp default current_timestamp,
    updated_at timestamp default current_timestamp on update current_timestamp,
    primary key (`member_id`),
    foreign key team_id(team_id) references main.team(team_id),
    unique key uq1 (member_id)
);

-- 放送局テーブル
CREATE TABLE IF NOT EXISTS `station`
(
    station_id bigint unsigned AUTO_INCREMENT NOT NULL,
    station_name varchar(255),
    keyword varchar(10) not null,
    created_at timestamp default current_timestamp,
    updated_at timestamp default current_timestamp on update current_timestamp,
    primary key (`station_id`),
    unique key uq1 (station_name)
);

-- TV番組テーブル
CREATE TABLE IF NOT EXISTS `program`
(
    program_id bigint unsigned AUTO_INCREMENT NOT NULL,
    station_id bigint unsigned not null,
    program_code varchar(10) not null,
    title varchar(255),
    description varchar(255),
    on_air_date datetime not null,
    team_id varchar(255) not null,
    member_id varchar(255),
    fct_chk tinyint(1) not null,
    created_at timestamp default current_timestamp,
    updated_at timestamp default current_timestamp on update current_timestamp,
    primary key (`program_id`),
    foreign key station_id(station_id) references main.station(station_id),
    unique key uq1 (program_code)
);

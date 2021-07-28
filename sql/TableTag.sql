-- タグテーブル
CREATE TABLE IF NOT EXISTS `tag`
(
    tag_id bigint unsigned NOT NULL auto_increment,
    tag varchar(60) not null,
    team_id bigint unsigned not null,
    member_id bigint,
    item_id bigint,
    created_at timestamp default current_timestamp,
    updated_at timestamp default current_timestamp on update current_timestamp,
    primary key (`tag_id`),
    foreign key team_id(team_id) references main.team(team_id)
);

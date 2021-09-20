drop procedure if exists MovePublicationDateIntToDate;
delimiter //

/*
wp_postsの新設カラムpublication_dateに値を入れる
使用サーバーに入り、このSQLを配置
$ mysql -u root -p main < MovePublicationDateIntToDate.sql
$ mysql -u root -p main
mysql> call MovePublicationDateIntToDate()
*** DONE ***

*/
create procedure MovePublicationDateIntToDate()
begin
    declare targetId int;
    declare pub_date int;
    declare done int default 0;

    /*
    データ取得
    */
    declare update_target_cur cursor for
        select ID, publication_date_int
        from wp_posts
        where publication_date_int is not null
        ;

    #カーソルオープン
    open update_target_cur;

    #以下グルグル
    repeat
        /*
        fetchで取得した列の値を変数に格納する。
        最初のカーソルで複数列取得した場合はintoの後に格納先変数をカンマ区切りで順番に複数書く。
        */
        fetch update_target_cur into targetId, pub_date_int;

        update wp_posts set publication_date = CAST(pub_date_int AS DATE)
        where ID = targetId;

    until done end repeat;

    -- #カーソルクローズ
    close update_target_cur;
end
//
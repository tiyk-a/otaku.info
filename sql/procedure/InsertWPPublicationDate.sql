drop procedure if exists RandomUpdate;
delimiter //

/*
wp_postsの新設カラムpublication_dateに値を入れる
使用サーバーに入り、このSQLを配置
$ mysql -u root -p main < InsertWPPublicationDate.sql
$ mysql -u root -p main
mysql> call InsertPublicationDate()
*** DONE ***

*/
create procedure InsertPublicationDate()
begin	
    declare targetId int;
    declare pub_date int;
    declare done int default 0;
    
    /*
    データ取得
    */
    declare update_target_cur cursor for
        select ID, CAST(CONCAT(SUBSTRING(post_title, 1, 4),SUBSTRING(post_title, 6, 2),SUBSTRING(post_title, 9, 2)) as SIGNED)
        from wp_posts
        where post_title REGEXP '^[0-9]+年'
        ;

    #カーソルオープン
    open update_target_cur;

    #以下グルグル    
    repeat
        /*
        fetchで取得した列の値を変数に格納する。
        最初のカーソルで複数列取得した場合はintoの後に格納先変数をカンマ区切りで順番に複数書く。
        */
        fetch update_target_cur into targetId, pub_date;

        update wp_posts set publication_date = pub_date
        where ID = targetId;

    until done end repeat;

    -- #カーソルクローズ
    close update_target_cur;
end
//
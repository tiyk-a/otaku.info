package otaku.info.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import otaku.info.entity.RoomItemLike;

import java.util.List;

public interface RoomItemLikeRepository extends JpaRepository<RoomItemLike, Long> {

    @Query(nativeQuery = true, value = "SELECT * FROM room_item_like where item_id = ?1")
    List<RoomItemLike> findByItemId(String itemId);

//    @Query(nativeQuery = true, value = "SELECT added_user FROM room_item_like where created_at >= DATE_SUB(NOW(),INTERVAL 1 DAY)")
    @Query(nativeQuery = true, value = "SELECT added_user FROM room_item_like where created_at >= DATE(NOW())")
    List<String> findByCreatedInADay();

    @Query(nativeQuery = true, value = "SELECT username FROM room_item_like a inner join room_user b on FIND_IN_SET(b.user_id, a.added_user) where a.created_at >= DATE(NOW())")
    List<String> findUserNameCreatedInADay();
}

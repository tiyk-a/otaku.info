package otaku.info.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import otaku.info.entity.RoomMyItem;

import java.util.List;
import java.util.Optional;

public interface RoomMyItemRepository extends JpaRepository<RoomMyItem, Long> {

    @Query(nativeQuery = true, value = "SELECT * FROM room_my_item where item_id = ?1")
    Optional<RoomMyItem> findByItemId(String itemId);

    @Query(nativeQuery = true, value = "SELECT * FROM room_my_item where new_like_count != 0")
    List<RoomMyItem> findUpdTarget();
}

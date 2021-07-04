package otaku.info.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import otaku.info.entity.Item;

import java.util.List;


public interface ItemRepository extends JpaRepository<Item, Long> {

    @Query("SELECT COUNT(*) FROM Item WHERE ITEM_CODE = ?1")
    Long hasItemCode(String itemCode);

    @Query("SELECT item_code FROM Item WHERE ITEM_CAPTION IS NULL")
    List<String> tmpMethod();

    @Query("SELECT item_id FROM Item WHERE ITEM_CODE = ?1")
    Long findItemIdByItemCode(String code);
}

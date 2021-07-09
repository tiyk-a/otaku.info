package otaku.info.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import otaku.info.entity.Item;

import java.util.List;


public interface ItemRepository extends JpaRepository<Item, Long> {

    @Query("SELECT COUNT(*) FROM Item WHERE ITEM_CODE = ?1")
    Long hasItemCode(String itemCode);

    @Query("SELECT item_code FROM Item WHERE title IS NULL or item_caption = ''")
    List<String> tmpMethod();

    @Query("SELECT item_id FROM Item WHERE ITEM_CODE = ?1")
    Long findItemIdByItemCode(String code);

    @Query("SELECT item_code FROM Item WHERE item_code IN ?1")
    List<String> findItemCodeList(List<String> itemCodelist);
}

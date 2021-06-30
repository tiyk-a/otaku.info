package otaku.info.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import otaku.info.entity.Item;


public interface ItemRepository extends JpaRepository<Item, Long> {

    @Query("SELECT COUNT(*) FROM Item WHERE ITEM_CODE = ?1")
    Long hasItemCode(String itemCode);
}

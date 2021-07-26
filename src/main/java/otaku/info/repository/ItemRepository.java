package otaku.info.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import otaku.info.entity.Item;

import java.util.Date;
import java.util.List;


/**
 * 商品テーブルのrepository
 *
 */
public interface ItemRepository extends JpaRepository<Item, Long> {

    @Query("SELECT COUNT(*) FROM Item WHERE ITEM_CODE = ?1")
    Long hasItemCode(String itemCode);

    @Query("SELECT item_code FROM Item WHERE title IS NULL or item_caption = ''")
    List<String> tmpMethod();

    @Query("SELECT item_id FROM Item WHERE ITEM_CODE = ?1")
    Long findItemIdByItemCode(String code);

    @Query("SELECT item_code FROM Item WHERE item_code IN ?1")
    List<String> findItemCodeList(List<String> itemCodelist);

    @Query("SELECT t FROM Item t WHERE publication_date > CURRENT_DATE AND publication_date < ?1")
    List<Item> findFutureItemByDate(Date date);

    @Query("SELECT t FROM Item t WHERE publication_date = CURRENT_DATE")
    List<Item> findReleasedItemList();

    @Query("SELECT count(*) FROM Item WHERE fct_chk = 0")
    Long waitingFctChk();

    @Query("SELECT t FROM Item t WHERE fct_chk = ?1")
    List<Item> findByFctChk(int i);
}

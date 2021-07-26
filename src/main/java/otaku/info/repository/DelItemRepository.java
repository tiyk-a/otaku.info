package otaku.info.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import otaku.info.entity.DelItem;

import java.util.List;

/**
 * 関係ない商品テーブルのrepository
 *
 */
public interface DelItemRepository extends JpaRepository<DelItem, Long> {

    @Query("SELECT item_code FROM del_item WHERE item_code IN ?1")
    List<String> findItemCodeList(List<String> itemCodelist);

    @Query("SELECT count(*) FROM del_item WHERE fct_chk = 0")
    Long waitingFctChk();

    @Query("SELECT t FROM del_item t WHERE fct_chk = ?1")
    List<DelItem> findByFctChk(int i);
}

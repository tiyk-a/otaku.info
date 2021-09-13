package otaku.info.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import otaku.info.entity.Item;

import java.util.Date;
import java.util.List;
import java.util.Optional;


/**
 * 商品テーブルのrepository
 *
 */
public interface ItemRepository extends JpaRepository<Item, Long> {

    @Query("SELECT COUNT(*) FROM Item WHERE ITEM_CODE = ?1")
    Long hasItemCode(String itemCode);

    @Query("SELECT item_code FROM Item WHERE title IS NULL or item_caption = ''")
    List<String> tmpMethod();

    @Query("SELECT item_code FROM Item WHERE item_code IN ?1")
    List<String> findItemCodeList(List<String> itemCodelist);

    @Query("SELECT t FROM Item t WHERE publication_date > CURRENT_DATE AND publication_date < ?1 and del_flg = 0")
    List<Item> findFutureItemByDate(Date date);

    @Query("SELECT t FROM Item t WHERE publication_date = CURRENT_DATE and del_flg = 0")
    List<Item> findReleasedItemList();

    @Query("SELECT t FROM Item t WHERE fct_chk = ?1")
    List<Item> findByFctChk(boolean isFctChked);

    @Modifying
    @Query("UPDATE Item SET del_flg = 1, fct_chk = 1 WHERE item_id in ?1")
    int deleteByItemIdList(List<Long> idList);

    @Query("SELECT item_id FROM Item WHERE item_id in ?1 and del_flg = ?2")
    List<Long> getNotDeletedItemIdList(List<Long> idList, boolean del_flg);

    @Modifying
    @Query("UPDATE Item SET publication_date = ?2, fct_chk = 1 WHERE item_id = ?1")
    int updateAllPublicationDate(Long itemId, Date publicationDate);

    @Query("SELECT COUNT(*) FROM Item WHERE item_id = ?1 and publication_date != ?2")
    Long getItemIdListNotUpdated(Long itemId, Date publicationDate);

    @Query("SELECT t FROM Item t WHERE publication_date >= ?1 and publication_date <= ?2 ORDER BY publication_date")
    List<Item> findItemsBetween(Date from, Date to);

    @Query("SELECT t FROM Item t WHERE publication_date >= ?1 and publication_date <= ?2 and del_flg = ?3 ORDER BY publication_date")
    List<Item> findItemsBetweenDelFlg(Date from, Date to, boolean delFlg);

    @Query("SELECT t FROM Item t WHERE item_code = ?1")
    Optional<Item> findByItemCode(String itemCode);

    @Query("SELECT t FROM Item t WHERE del_flg = ?1 and image1 is null and publication_date > '2021-09-01' and publication_date < '2021-09-10'")
    List<Item> findByDelFlg(boolean delFlg);

    @Query("select t from Item t where wp_id is not null and updated_at >= ?1 and image1 is not null")
    List<Item> findWpIdNotNullUpdatedAt(Date from);

    @Query("select t from Item t where del_flg = 0 and publication_date is not null and item_m_id is null")
    List<Item> findNotDeleted();

    @Query("select t from Item t where team_id like ?1 and publication_date = ?2 and item_m_id is not null")
    List<Item> findSimilarItemList(String teamId, Date publicationDate);

    @Query("select t from Item t where item_m_id = ?1")
    List<Item> findByMasterId(Long itemMasterId);

    @Query("select wp_id from Item where wp_id is not null")
    List<Long> collectWpId();

    @Modifying
    @Query("update Item set wp_id = null where wp_id is not null")
    void clearAllWpId();

    @Query("select t from Item t where item_m_id = ?1")
    List<Item> gatherItems(Long itemMId);

    @Query(nativeQuery = true, value = "select image1 from Item where item_m_id = ?1 and image1 is not null limit 1")
    String getImageUrlByItemMIdImage1NotNull(Long itemMId);
}

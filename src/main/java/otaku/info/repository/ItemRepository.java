package otaku.info.repository;

import org.springframework.data.jpa.repository.JpaRepository;
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

    @Query("SELECT item_id FROM Item WHERE item_id in ?1 and del_flg = ?2")
    List<Long> getNotDeletedItemIdList(List<Long> idList, boolean del_flg);

    @Query("SELECT COUNT(*) FROM Item WHERE item_id = ?1 and publication_date != ?2")
    Long getItemIdListNotUpdated(Long itemId, Date publicationDate);

    @Query("SELECT t FROM Item t WHERE item_code = ?1")
    Optional<Item> findByItemCode(String itemCode);

    @Query("SELECT t FROM Item t WHERE del_flg = ?1 and publication_date > '2021-09-01' and publication_date < '2021-09-10'")
    List<Item> findByDelFlg(boolean delFlg);

    @Query("select t from Item t where wp_id is not null and updated_at >= ?1")
    List<Item> findWpIdNotNullUpdatedAt(Date from);

    @Query(nativeQuery = true, value = "select a.* from item a inner join i_rel b on a.item_id = b.item_id where b.team_id = ?1 and a.publication_date = ?2 and im_id is not null")
    List<Item> findSimilarItemList(Long teamId, Date publicationDate);

    @Query("select t from Item t where im_id = ?1")
    List<Item> findByMasterId(Long itemMasterId);

    @Query("select t from Item t where im_id = ?1")
    List<Item> gatherItems(Long itemMId);

    @Query("select count(*) from Item where item_code = ?1 and site_id = ?2")
    int isRegistered(String code, Integer siteId);

    @Query("select t from Item t where im_id is null and publication_date >= CURRENT_DATE")
    List<Item> findByMIdNullFuture();

    @Query(nativeQuery = true, value = "select a.* from item a inner join i_rel b on a.item_id = b.item_id where b.team_id = ?1 and a.del_flg = 0")
    List<Item> findByTeamIdNotDeleted(Long teamId);

    @Query(nativeQuery = true, value = "select a.* from item a inner join i_rel b on a.item_id = b.item_id where b.team_id = ?1 and a.del_flg = 0 and publication_date >= CURRENT_DATE and a.im_id is null")
    List<Item> findByTeamIdFutureNotDeletedNoIM(Long teamId);

    @Query(nativeQuery = true, value = "select a.* from item a inner join i_rel b on a.item_id = b.item_id where b.team_id = ?1 and a.del_flg = 0 and publication_date >= CURRENT_DATE and a.im_id is not null")
    List<Item> findByTeamIdFutureNotDeletedWIM(Long teamId);
}

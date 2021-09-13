package otaku.info.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import otaku.info.entity.ItemMaster;

import java.util.Date;
import java.util.List;

public interface ItemMasterRepository extends JpaRepository<ItemMaster, Long> {

    @Query("SELECT t FROM item_master t WHERE publication_date = CURRENT_DATE and del_flg = 0")
    List<ItemMaster> findReleasedItemList();

    @Query("SELECT t FROM item_master t WHERE publication_date >= ?1 and publication_date <= ?2 and del_flg = ?3 ORDER BY publication_date")
    List<ItemMaster> findItemsBetweenDelFlg(Date from, Date to, boolean delFlg);

    @Query("select t from item_master t where wp_id is not null")
    List<ItemMaster> findWpIdNotNull();

    @Query("select t from item_master t where image1 is null")
    List<ItemMaster> findImageNull();

    @Query("select t from item_master t where wp_id is not null and image1 is not null")
    List<ItemMaster> findWpIdNotNullImage1Exists();

    @Query("select t from item_master t where wp_id = ?1")
    ItemMaster findByWpId(Integer wpId);

    @Modifying
    @Query("update item_master set wp_id = null where wp_id is not null")
    void clearAllWpId();

    @Query("select t from item_master t where YEAR(publication_date) = ?1")
    List<ItemMaster> findByPublicationYear(Integer year);
}

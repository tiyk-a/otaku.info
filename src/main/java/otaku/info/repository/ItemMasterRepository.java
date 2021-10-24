package otaku.info.repository;


import org.springframework.data.jpa.repository.JpaRepository;
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

    @Query("select t from item_master t where YEAR(publication_date) = ?1")
    List<ItemMaster> findByPublicationYear(Integer year);

    // TODO: これ別teamとかでもヒットして返却するけど大丈夫？
    @Query(nativeQuery = true, value = "select t from item_master t inner join im_rel b on t.item_m_id = b.item_m_id where b.wp_id in ?1 and url is null")
    List<ItemMaster> findByWpIdUrlNullList(List<Integer> wpIdList);

    @Query(nativeQuery = true, value = "select a.* from item_master a inner join im_rel b on a.item_m_id = b.item_m_id where YEAR(publication_date) = ?1 and b.wp_id is null")
    List<ItemMaster> findByPublicationYearWpIdNull(Integer year);

    @Query(nativeQuery = true, value = "select a.* from item_master a inner join im_rel b on a.item_m_id = b.item_m_id where b.wp_id is null")
    List<ItemMaster> findAllNotPosted();

    @Query(nativeQuery = true, value = "select a.* from item_master a inner join im_rel b on a.item_m_id = b.item_m_id where publication_date >= ?1 and b.team_id = ?2 limit ?3")
    List<ItemMaster> findDateAfterTeamIdLimit(Date from, Long teamId, Long limit);

    @Query(nativeQuery = true, value = "select a.* from item_master a inner join im_rel b on a.item_m_id = b.item_m_id where b.team_id = ?1 and publication_date > CURRENT_DATE order by publication_date limit 5")
    List<ItemMaster> findNearFutureIMByTeamId(Long teamId);

    @Query(nativeQuery = true, value = "select a.* from item_master a inner join im_rel b on a.item_m_id = b.item_m_id where b.team_id = ?1 and a.del_flg = 0 and publication_date >= CURRENT_DATE and a.del_flg = 0 order by publication_date desc")
    List<ItemMaster> findByTeamIdNotDeleted(Long teamId);
}

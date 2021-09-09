package otaku.info.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import otaku.info.entity.Item;
import otaku.info.entity.ItemMaster;

import java.util.Date;
import java.util.List;

public interface ItemMasterRepository extends JpaRepository<ItemMaster, Long> {

    @Query("SELECT t FROM item_master t WHERE publication_date = CURRENT_DATE and del_flg = 0")
    List<ItemMaster> findReleasedItemList();

    @Query("SELECT t FROM item_master t WHERE publication_date >= ?1 and publication_date <= ?2 and del_flg = ?3 ORDER BY publication_date")
    List<ItemMaster> findItemsBetweenDelFlg(Date from, Date to, boolean delFlg);
}

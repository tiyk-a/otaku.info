package otaku.info.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import otaku.info.entity.ItemMaster;

import java.util.List;

public interface ItemMasterRepository extends JpaRepository<ItemMaster, Long> {

    @Query("SELECT t FROM item_master t WHERE publication_date = CURRENT_DATE and del_flg = 0")
    List<ItemMaster> findReleasedItemList();
}

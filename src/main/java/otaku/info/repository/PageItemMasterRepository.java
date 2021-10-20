package otaku.info.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import otaku.info.entity.ItemMaster;

public interface PageItemMasterRepository extends PagingAndSortingRepository<ItemMaster, Long> {

    @Query("select t from item_master t")
    Page<ItemMaster> findAll(PageRequest req);
}

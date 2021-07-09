package otaku.info.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import otaku.info.entity.DelItem;

import java.util.List;

public interface DelItemRepository extends JpaRepository<DelItem, Long> {

    @Query("SELECT item_code FROM del_item WHERE item_code IN ?1")
    List<String> findItemCodeList(List<String> itemCodelist);

}

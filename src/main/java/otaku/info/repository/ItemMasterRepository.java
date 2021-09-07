package otaku.info.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import otaku.info.entity.ItemMaster;

public interface ItemMasterRepository extends JpaRepository<ItemMaster, Long> {
}

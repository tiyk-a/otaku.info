package otaku.info.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import otaku.info.entity.ItemMasterRelation;

import java.util.List;

public interface ItemMasterRelationRepository extends JpaRepository<ItemMasterRelation, Long> {

    @Query("select t from item_master_relation t where item_m_id = ?1")
    List<ItemMasterRelation> findByItemMId(Long itemMId)
;}

package otaku.info.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import otaku.info.entity.ItemRelation;

import java.util.List;

public interface ItemRelationRepository extends JpaRepository<ItemRelation, Long> {

    @Query("select t from item_relation t where item_id = ?1")
    List<ItemRelation> findByItemId(Long itemId);

    @Query("select team_id from item_relation t where item_id = ?1")
    List<Long> getTeamIdListByItemId(Long itemId);

    @Query("select count(t) from item_relation t where item_id = ?1 and team_id = ?2 and member_id = ?3")
    int existsByElem(Long itemId, Long teamId, Long memberId);

    @Query("select t from item_relation t where item_id in ?1")
    List<ItemRelation> findByItemIdList(List<Long> itemIdList);
}

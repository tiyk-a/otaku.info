package otaku.info.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import otaku.info.entity.ItemRel;

import java.util.List;
import java.util.Optional;

public interface ItemRelRepository extends JpaRepository<ItemRel, Long> {

    @Query("select t from item_rel t where item_id = ?1")
    List<ItemRel> findByItemId(Long itemId);

    @Query("select team_id from item_rel t where item_id = ?1")
    List<Long> getTeamIdListByItemId(Long itemId);

    @Query("select count(t) from item_rel t where item_id = ?1 and team_id = ?2 and member_id = ?3")
    int existsByElem(Long itemId, Long teamId, Long memberId);

    @Query("select count(t) from item_rel t where item_id = ?1 and team_id = ?2")
    int existsByElem(Long itemId, Long teamId);

    @Query("select t from item_rel t where item_id in ?1")
    List<ItemRel> findByItemIdList(List<Long> itemIdList);

    @Query("select t from item_rel t where item_id = ?1 and team_id = ?2 and member_id is null")
    Optional<ItemRel> findByItemIdTeamIdMemberIdNull(Long itemId, Long teamId);
}

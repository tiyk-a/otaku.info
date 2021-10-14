package otaku.info.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import otaku.info.entity.IRel;

import java.util.List;
import java.util.Optional;

public interface IRelRepository extends JpaRepository<IRel, Long> {

    @Query("select t from i_rel t where item_id = ?1")
    List<IRel> findByItemId(Long itemId);

    @Query("select team_id from i_rel t where item_id = ?1")
    List<Long> getTeamIdListByItemId(Long itemId);

    @Query("select count(t) from i_rel t where item_id = ?1 and team_id = ?2 and member_id = ?3")
    int existsByElem(Long itemId, Long teamId, Long memberId);

    @Query("select count(t) from i_rel t where item_id = ?1 and team_id = ?2")
    int existsByElem(Long itemId, Long teamId);

    @Query("select t from i_rel t where item_id in ?1")
    List<IRel> findByItemIdList(List<Long> itemIdList);

    @Query("select t from i_rel t where item_id = ?1 and team_id = ?2 and member_id is null")
    Optional<IRel> findByItemIdTeamIdMemberIdNull(Long itemId, Long teamId);

    @Query("select t from i_rel t where item_id = ?1 and team_id is not null")
    List<IRel> findByItemIdTeamIdNotNull(Long itemId);
}

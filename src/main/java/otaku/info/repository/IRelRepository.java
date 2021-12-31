package otaku.info.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import otaku.info.entity.IRel;

import java.util.List;

public interface IRelRepository extends JpaRepository<IRel, Long> {

    @Query("select t from i_rel t where item_id = ?1")
    List<IRel> findByItemId(Long itemId);

    @Query("select distinct team_id from i_rel t where item_id = ?1")
    List<Long> findTeamIdByItemId(Long itemId);

    @Query("select team_id from i_rel t where item_id = ?1")
    List<Long> getTeamIdListByItemId(Long itemId);

    @Query("select count(t) from i_rel t where item_id = ?1 and team_id = ?2")
    int existsByElem(Long itemId, Long teamId);

    @Query("select t from i_rel t where item_id in ?1")
    List<IRel> findByItemIdList(List<Long> itemIdList);

    @Query("select t from i_rel t where item_id = ?1 and team_id is not null")
    List<IRel> findByItemIdTeamIdNotNull(Long itemId);

    @Query(nativeQuery = true, value = "select distinct team_id from i_rel a inner join item b on a.item_id = b.item_id where b.im_id = ?1")
    List<Long> findDistinctTeamIdByMasterId(Long masterId);
}

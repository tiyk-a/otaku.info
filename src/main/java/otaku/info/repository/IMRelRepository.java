package otaku.info.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import otaku.info.entity.IMRel;
import otaku.info.entity.IMRelKey;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface IMRelRepository extends JpaRepository<IMRel, IMRelKey> {

    @Query("select t from im_rel t where im_rel_id = ?1")
    IMRel findByImRelId(Long imRelId);
    @Query("select t from im_rel t where im_id = ?1")
    List<IMRel> findByItemMId(Long itemMId);

    @Query("select distinct team_id from im_rel t where im_id = ?1")
    List<Long> findTeamIdByItemMId(Long itemMId);

    @Query("select team_id from im_rel where im_id = ?1")
    List<Long> findTeamIdListByItemMId(Long itemMId);

    @Query("select wp_id from im_rel where im_id = ?1")
    Long getWpIdByItemMId(Long itemMId);

    @Query("select count(t) from im_rel t where im_id = ?1 and team_id = ?2")
    int existsByElem(Long imId, Long teamId);

    @Query("select t from im_rel t where im_id = ?1 and team_id = ?2")
    Optional<IMRel> findByImIdTeamId(Long imId, Long teamId);

    @Query("select t from im_rel t where im_id = ?1 and team_id is not null")
    List<IMRel> findByItemIdTeamIdNotNull(Long imId);

    @Query("select t from im_rel t where wp_id is not null")
    List<IMRel> findAllWpIdNotNull();

    @Query("select t from im_rel t where wp_id = ?1 and team_id = ?2")
    List<IMRel> findbyWpIdTeamId(Long wpId, Long teamId);

    @Query(nativeQuery = true, value = "select a.* from im_rel a inner join im b on a.im_id = b.im_id where a.wp_id is null and b.publication_date >= ?1")
    List<IMRel> findByWpIdNullPublicationDateFuture(Date today);

    @Query("select t from im_rel t where im_id  = ?1 and team_id = ?2")
    IMRel findByItemMIdTeamId(Long imId, Long teamId);
}

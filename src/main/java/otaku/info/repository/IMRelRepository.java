package otaku.info.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import otaku.info.entity.IMRel;
import otaku.info.entity.IMRelKey;

import java.util.List;

public interface IMRelRepository extends JpaRepository<IMRel, IMRelKey> {

    @Query("select t from im_rel t where item_m_id = ?1")
    List<IMRel> findByItemMId(Long itemMId);

    @Query("select team_id from im_rel where item_m_id = ?1")
    List<Long> findTeamIdListByItemMId(Long itemMId);

//    @Query("select member_id from im_rel where item_m_id = ?1")
//    List<Long> findMemberIdListByItemMId(Long itemMId);

    @Query("select wp_id from im_rel where item_m_id = ?1")
    Long getWpIdByItemMId(Long itemMId);

    @Query("select count(t) from im_rel t where item_m_id = ?1 and team_id = ?2")
    int existsByElem(Long imId, Long teamId);

//    @Query("select count(t) from im_rel t where item_m_id = ?1 and team_id = ?2")
//    int existsByElem(Long imId, Long teamId);

//    @Query("select t from im_rel t where item_m_id = ?1 and team_id = ?2")
//    Optional<IMRel> findByItemIdTeamIdMemberIdNull(Long iMId, Long teamId);

    @Query("select t from im_rel t where item_m_id = ?1 and team_id = ?2")
    IMRel findByImIdTeamId(Long imId, Long teamId);

    @Query("select t from im_rel t where member_id is not null")
    List<IMRel> findAllMemberIdNotNull();

    @Query("select t from im_rel t where member_id is null")
    List<IMRel> findAllMemberNull();

    @Query("select t from im_rel t where item_m_id = ?1 and team_id is not null")
    List<IMRel> findByItemIdTeamIdNotNull(Long imId);
}

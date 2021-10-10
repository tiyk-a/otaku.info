package otaku.info.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import otaku.info.entity.IMRel;

import java.util.List;
import java.util.Optional;

public interface IMRelRepository extends JpaRepository<IMRel, Long> {

    @Query("select t from im_rel t where item_m_id = ?1")
    List<IMRel> findByItemMId(Long itemMId);

    @Query("select team_id from im_rel where item_m_id = ?1")
    List<Long> findTeamIdListByItemMId(Long itemMId);

    @Query("select member_id from im_rel where item_m_id = ?1 and member_id is not null")
    List<Long> findMemberIdListByItemMId(Long itemMId);

    @Query("select wp_id from im_rel where item_m_id = ?1")
    Long getWpIdByItemMId(Long itemMId);

    @Query("select count(t) from im_rel t where item_m_id = ?1 and team_id = ?2")
    int existsByElem(Long imId, Long teamId);

    @Query("select count(t) from im_rel t where item_m_id = ?1 and team_id = ?2 and member_id = ?3")
    int existsByElem(Long imId, Long teamId, Long memberId);

    @Query("select t from im_rel t where item_m_id = ?1 and team_id = ?2 and member_id is null")
    Optional<IMRel> findByItemIdTeamIdMemberIdNull(Long iMId, Long teamId);
}

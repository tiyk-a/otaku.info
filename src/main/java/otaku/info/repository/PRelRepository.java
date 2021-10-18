package otaku.info.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import otaku.info.entity.PRel;
import otaku.info.entity.PRelKey;

import java.util.List;
import java.util.Optional;

public interface PRelRepository extends JpaRepository<PRel, PRelKey> {

    @Query("select t from p_rel t where program_id = ?1")
    List<PRel> findAllByProgramId(Long programId);

    @Query("select team_id from p_rel where program_id = ?1")
    List<Long> findTeamIdListByProgramId(Long programId);

    @Query("select member_id from p_rel where program_id = ?1")
    List<Long> findMemberIdListByProgramId(Long programId);

    @Query("select count(t) from p_rel t where program_id = ?1 and team_id = ?2")
    int existsByElem(Long programId, Long teamId);

    @Query("select count(t) from p_rel t where program_id = ?1 and team_id = ?2 and member_id = ?3")
    int existsByElem(Long programId, Long teamId, Long memberId);

    @Query("select t from p_rel t where program_id = ?1 and team_id = ?2 and member_id is null")
    Optional<PRel> findByItemIdTeamIdMemberIdNull(Long programId, Long teamId);
}

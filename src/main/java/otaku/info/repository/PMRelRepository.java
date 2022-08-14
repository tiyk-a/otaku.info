//package otaku.info.repository;
//
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import otaku.info.entity.PMRel;
//import otaku.info.entity.PMRelKey;
//
//import java.util.List;
//import java.util.Optional;
//
//public interface PMRelRepository extends JpaRepository<PMRel, PMRelKey> {
//
//    @Query("select t from pm_rel t where pm_rel_id = ?1")
//    PMRel findByPmRelId(Long id);
//
//    @Query("select t from pm_rel t where pm_id = ?1")
//    List<PMRel> findByPmId(Long pmId);
//
//    @Query("select t from pm_rel t where pm_id = ?1 and del_flg = ?2")
//    List<PMRel> findByPmIdDelFlg(Long pmId, Boolean delFlg);
//
//    @Query("select team_id from pm_rel where pm_id = ?1")
//    List<Long> findTeamIdByProgramId(Long pId);
//
//    @Query("select t from pm_rel t where pm_id = ?1 and team_id = ?2")
//    Optional<PMRel> findByPmIdTeamId(Long pmId, Long teamId);
//
//    @Query("select t.team_id from pm_rel t where pm_id = ?1")
//    List<Long> getTeamIdList(Long pmId);
//}

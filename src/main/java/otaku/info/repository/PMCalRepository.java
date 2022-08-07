package otaku.info.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import otaku.info.entity.PMCal;

import java.util.List;

public interface PMCalRepository extends JpaRepository<PMCal, Long> {

//    @Query("select t from pm_cal t where pm_ver_id in ?1 and pm_rel_id in ?2")
//    List<PMCal> findByVerIdListRelIdList(List<Long> verIdList, List<Long> relIdList);

    @Query("select t from pm_cal t where pm_ver_id in ?1 and team_id in ?2")
    List<PMCal> findByVerIdListTeamIdList(List<Long> verIdList, List<Long> teamIdList);

//    @Query("select t from pm_cal t where pm_ver_id in ?1 and pm_rel_id in ?2 and del_flg = ?3")
//    List<PMCal> findByVerIdListRelIdListDelFlg(List<Long> verIdList, List<Long> relIdList, Boolean delFlg);

    @Query("select t from pm_cal t where pm_ver_id in ?1 and team_id in ?2 and del_flg = ?3")
    List<PMCal> findByVerIdListTeamIdListDelFlg(List<Long> verIdList, List<Long> teamIdList, Boolean delFlg);

//    @Query("select t from pm_cal t where pm_ver_id = ?1 and pm_rel_id = ?2 and del_flg = ?3")
//    Optional<PMCal> findByVerIdRelIdDelFlg(Long verId, Long relId, Boolean delFlg);
}

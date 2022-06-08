package otaku.info.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import otaku.info.entity.PMCal;
import otaku.info.entity.PMRel;
import otaku.info.entity.PMVer;

import java.util.List;
import java.util.Optional;

public interface PMCalRepository extends JpaRepository<PMCal, Long> {

    @Query("select t from pm_cal t where pm_ver_id in ?1 and pm_rel_id in ?2")
    List<PMCal> findByVerIdListRelIdList(List<Long> verIdList, List<Long> relIdList);

    @Query("select t from pm_cal t where pm_ver_id in ?1 and pm_rel_id in ?2 and del_flg = ?3")
    List<PMCal> findByVerIdListRelIdListDelFlg(List<Long> verIdList, List<Long> relIdList, Boolean delFlg);

    @Query("select t from pm_cal t where pm_ver_id = ?1 and pm_rel_id = ?2 and del_flg = ?3")
    Optional<PMCal> findByVerIdRelIdDelFlg(Long verId, Long relId, Boolean delFlg);
}

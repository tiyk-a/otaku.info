package otaku.info.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import otaku.info.entity.PMRelMem;
import otaku.info.entity.PMRelMemKey;

import java.util.List;
import java.util.Optional;

public interface PMRelMemRepository extends JpaRepository<PMRelMem, PMRelMemKey> {

    @Query(nativeQuery = true, value = "select * from pm_rel_mem t where pm_rel_id = ?1")
    List<PMRelMem> tmpMethod(Long pmRelId);

    @Query("select t from pm_rel_mem t where pm_rel_mem_id = ?1")
    Optional<PMRelMem> findByPmRelMemId(Long id);

    @Query("select t from pm_rel_mem t where pm_rel_id = ?1")
    List<PMRelMem> findByPRelId(Long pmRelId);

    @Query("select t from pm_rel_mem t where pm_rel_id = ?1 and del_flg = ?2")
    List<PMRelMem> findByPRelIdDelFlg(Long pmRelId, Boolean delFlg);

    @Query("select t from pm_rel_mem t where pm_rel_id = ?1 and member_id = ?2")
    Optional<PMRelMem> findByPmRelIdMemId(Long pmRelId, Long memId);

    @Query(nativeQuery = true, value = "select a.* from pm_rel_mem a inner join pm_rel b on a.pm_rel_id = b.pm_rel_id where b.pm_id = ?1 and a.member_id = ?2")
    Optional<PMRelMem> findByPmIdMemId(Long pmId, Long memId);

    @Query(nativeQuery = true, value = "select a.* from pm_rel_mem a inner join pm_rel b on a.pm_rel_id = b.pm_rel_id where b.pm_rel_id in ?1 and a.del_flg = ?2")
    List<PMRelMem> findByPmRelIdListDelFlg(List<Long> relIdList, Boolean delFlg);
}

package otaku.info.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import otaku.info.entity.PMRelMem;
import otaku.info.entity.PMRelMemKey;

import java.util.List;

public interface PMRelMemRepository extends JpaRepository<PMRelMem, PMRelMemKey> {

    @Query("select t from pm_rel_mem t where pm_rel_id = ?1")
    List<PMRelMem> findByPRelId(Long pmRelId);
}

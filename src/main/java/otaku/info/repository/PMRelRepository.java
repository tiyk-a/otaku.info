package otaku.info.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import otaku.info.entity.PMRel;
import otaku.info.entity.PMRelKey;

import java.util.List;

public interface PMRelRepository extends JpaRepository<PMRel, PMRelKey> {

    @Query("select t from pm_rel t where pm_id = ?1")
    List<PMRel> findByPmId(Long pmId);
}

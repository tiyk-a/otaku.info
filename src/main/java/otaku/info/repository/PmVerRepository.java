package otaku.info.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import otaku.info.entity.PMVer;

import java.util.List;

public interface PmVerRepository extends JpaRepository<PMVer, Long> {

    @Query("select t from pm_ver t where pm_id = ?1 and del_flg = 0")
    List<PMVer> findByPmId(Long pmId);
}

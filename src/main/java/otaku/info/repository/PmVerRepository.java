package otaku.info.repository;

import com.sun.istack.Nullable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import otaku.info.entity.PMVer;

import java.util.List;

public interface PmVerRepository extends JpaRepository<PMVer, Long> {

    @Query("select t from pm_ver t where pm_id = ?1")
    List<PMVer> findByPmId(Long pmId);

    @Query("select t from pm_ver t where pm_id = ?1 and del_flg = ?2")
    List<PMVer> findByPmIdDelFlg(Long pmId, Boolean delFlg);

    @Query("select t from pm_ver t where pm_id = ?1 and station_id = ?2")
    List<PMVer> findByPmIdStationId(Long pmId, Long stationId);
}

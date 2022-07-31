package otaku.info.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import otaku.info.entity.RegPmStation;

import java.util.List;

public interface RegPmStationRepository extends JpaRepository<RegPmStation, Long> {

    @Query(nativeQuery = true, value = "SELECT count(*) FROM reg_pm_station r WHERE regular_pm_id = ?1 and station_id = ?2")
    int existData(Long regPmId, Long stationId);

    @Query(nativeQuery = true, value = "SELECT r.station_id FROM reg_pm_station r where r.regular_pm_id = ?1")
    List<Long> findStationIdListByReguPmId(Long regPmId);
}

package otaku.info.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import otaku.info.entity.PMVer;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

public interface PmVerRepository extends JpaRepository<PMVer, Long> {

    @Query("select t from pm_ver t where pm_id = ?1")
    List<PMVer> findByPmId(Long pmId);

    @Query("select t from pm_ver t where pm_id = ?1 and del_flg = ?2")
    List<PMVer> findByPmIdDelFlg(Long pmId, Boolean delFlg);

    @Query("select t from pm_ver t where pm_id = ?1 and station_id = ?2")
    List<PMVer> findByPmIdStationId(Long pmId, Long stationId);

    @Query(nativeQuery = true, value = "select t.* from pm_ver t where on_air_date >= ?1 and on_air_date < ?2 and del_flg = 0")
    List<PMVer> findByOnAirDateNotDeleted(LocalDateTime dateTime, LocalDateTime endTime);

    @Query(nativeQuery = true, value = "select t.* from pm_ver t where DATE(on_air_date) >= ?1 and DATE(on_air_date) <= ?2 and del_flg = 0")
    List<PMVer> findByOnAirDateNotDeleted(Date sDate, Date eDate);

    @Query(nativeQuery = true, value = "select t.* from pm_ver t where DATE(on_air_date) = ?1 and del_flg = 0")
    List<PMVer> findByOnAirDateNotDeleted(Date date);

    @Query(nativeQuery = true, value = "select t.* from pm_ver t inner join pm_rel a on t.pm_id = a.pm_id where DATE(on_air_date) = ?1 and t.del_flg = 0 and a.team_id = ?2")
    List<PMVer> findByOnAirDateNotDeletedTeamId(Date date, Long teamId);
}

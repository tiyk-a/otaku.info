package otaku.info.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import otaku.info.entity.PM;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface PMRepository extends JpaRepository<PM, Long> {

    @Query(nativeQuery = true, value = "select t.* from pm t " +
            "inner join pm_ver a on t.pm_id = a.pm_id " +
            "where a.on_air_date >= '2022-01-01' and t.team_arr is null limit 50")
    List<PM> tmpMethod();

    @Query(nativeQuery = true, value = "select t.* from pm t " +
            "inner join pm_ver a on t.pm_id = a.pm_id " +
            "where a.on_air_date < '2022-01-01' and t.team_arr is null limit 50")
    List<PM> tmpMethod2();

    @Query("select t from pm t where pm_id = ?1")
    PM findByPmId(Long pmId);

    @Query("select t from pm t where pm_id in ?1")
    List<PM> findbyPmIdList(List<Long> pmIdList);

    /**
     * 未来のpmで有効なものだけ取ってくる
     *
     * @param teamId
     * @return
     */
    @Query(nativeQuery = true, value = "select a.* from pm a inner join pm_ver b on a.pm_id = b.pm_id where FIND_IN_SET(?1, team_arr) and b.on_air_date >= CURRENT_DATE and a.del_flg = 0 and b.del_flg = 0 order by b.on_air_date asc")
    List<PM> findByTeamIdFuture(Long teamId);

    @Query(nativeQuery = true, value = "select a.* from pm a inner join pm_ver b on a.pm_id = b.pm_id where a.title = ?1 and b.on_air_date = ?2")
    List<PM> findByTitleOnAirDate(String title, LocalDateTime date);

    @Query("select t from pm t where title = 'findByTitle'")
    List<PM> findByTitle(String title);

    @Query(nativeQuery = true, value = "select a.* from pm a inner join pm_ver b on a.pm_id = b.pm_id where b.on_air_date >= current_date and a.del_flg = ?1")
    List<PM> findFutureDelFlg(Boolean delFlg);

    @Query(nativeQuery = true, value = "select a.* from pm a where a.title like %?1% order by pm_id desc limit ?2")
    List<PM> findByKeyLimit(String key, Integer limit);

    @Query(nativeQuery = true, value = "select a.pm_id, a.title, a.description, b.on_air_date, b.station_id from pm a inner join pm_ver b on a.pm_id = b.pm_id where date(b.on_air_date) = ?1 and b.station_id = ?2 order by b.on_air_date desc limit 3")
    List<Object[]> findPmFuByllDtoOnAirDateStationId(LocalDate date, Long stationId);

    @Query(nativeQuery = true, value = "select a.pm_id, a.title, a.description, b.on_air_date, b.station_id from pm a inner join pm_ver b on a.pm_id = b.pm_id where b.on_air_date = ?1 and b.station_id != ?2 order by b.on_air_date desc limit 3")
    List<Object[]> findPmFuByllDtoOnAirDateExStationId(LocalDateTime ldt, Long stationId);

    @Query(nativeQuery = true, value = "select a.pm_id, a.title, a.description, b.on_air_date, b.station_id from pm a inner join pm_ver b on a.pm_id = b.pm_id where b.on_air_date = ?1")
    List<Object[]> findByOnAirDateNotDeleted(LocalDateTime ldt);
}

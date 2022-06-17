package otaku.info.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import otaku.info.entity.PM;

import java.time.LocalDateTime;
import java.util.List;

public interface PMRepository extends JpaRepository<PM, Long> {

    @Query("select t from pm t where pm_id = ?1")
    PM findByPmId(Long pmId);

    /**
     * 未来のpmで有効なものだけ取ってくる
     *
     * @param teamId
     * @return
     */
    @Query(nativeQuery = true, value = "select a.* from pm a inner join pm_ver b on a.pm_id = b.pm_id inner join pm_rel c on a.pm_id = c.pm_id where c.team_id = ?1 and b.on_air_date >= CURRENT_DATE and a.del_flg = 0 and b.del_flg = 0 and c.del_flg = 0 order by b.on_air_date asc")
    List<PM> findByTeamIdFuture(Long teamId);

    @Query(nativeQuery = true, value = "select a.* from pm a inner join pm_ver b on a.pm_id = b.pm_id where a.title = ?1 and b.on_air_date = ?2")
    List<PM> findByTitleOnAirDate(String title, LocalDateTime date);

    @Query("select t from pm t where title = 'findByTitle'")
    List<PM> findByTitle(String title);

    @Query(nativeQuery = true, value = "select a.* from pm a inner join pm_ver b on a.pm_id = b.pm_id where b.on_air_date >= current_date and a.del_flg = ?1")
    List<PM> findFutureDelFlg(Boolean delFlg);

    @Query(nativeQuery = true, value = "select a.* from pm a where a.title like %?1% order by pm_id desc limit ?2")
    List<PM> findByKeyLimit(String key, Integer limit);

    @Query(nativeQuery = true, value = "select a.pm_id, a.title, a.description, b.on_air_date, b.station_id from pm a inner join pm_ver b on a.pm_id = b.pm_id where b.on_air_date = ?1 and b.station_id = ?2 order by b.on_air_date desc limit 3")
    List<Object[]> findPmFuByllDtoOnAirDateStationId(LocalDateTime ldt, Long stationId);

    @Query(nativeQuery = true, value = "select a.pm_id, a.title, a.description, b.on_air_date, b.station_id from pm a inner join pm_ver b on a.pm_id = b.pm_id where b.on_air_date = ?1 and b.station_id != ?2 order by b.on_air_date desc limit 3")
    List<Object[]> findPmFuByllDtoOnAirDateExStationId(LocalDateTime ldt, Long stationId);
}

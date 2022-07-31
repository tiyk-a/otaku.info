package otaku.info.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import otaku.info.entity.Station;

import java.util.List;

public interface StationRepository extends JpaRepository<Station, Long> {

    @Query("SELECT station_id FROM station WHERE station_name = ?1")
    List<Long> findStationId(String stationName);

    @Query("SELECT station_name FROM station WHERE station_id = ?1")
    String getStationName(Long stationId);

    @Query("select t from station t where station_name = ?1")
    List<Station> findByName(String name);

    @Query("select t from station t")
    List<Station> findAll();

    /**
     * 引数idListに入っていないstationのリストを返却します
     *
     * @param stationIdList
     * @return
     */
    @Query("select t from station t where station_name like %?1% and station_id not in ?2")
    List<Station> findByNameExceptIdList(String name, List<Long> stationIdList);
}

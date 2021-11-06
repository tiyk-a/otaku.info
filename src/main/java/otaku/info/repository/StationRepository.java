package otaku.info.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import otaku.info.entity.Station;

import java.util.List;
import java.util.Optional;

public interface StationRepository extends JpaRepository<Station, Long> {

    @Query("SELECT station_id FROM station WHERE station_name = ?1")
    List<Long> findStationId(String stationName);

    @Query("SELECT station_name FROM station WHERE station_id = ?1")
    String getStationName(Long stationId);

    @Query("select t from station t where station_name = ?1")
    List<Station> findByName(String name);

    @Query("select t from station t")
    List<Station> findAll();
}

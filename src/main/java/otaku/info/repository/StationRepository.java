package otaku.info.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import otaku.info.entity.Station;

public interface StationRepository extends JpaRepository<Station, Long> {

    @Query("SELECT station_id FROM station WHERE ?1 LIKE CONCAT('%',keyword, '%')")
    Long findStationId(String stationName);
}

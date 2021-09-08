package otaku.info.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import otaku.info.entity.Program;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

public interface ProgramRepository extends JpaRepository<Program, Long> {

    @Query("SELECT p FROM program p WHERE DATE(on_air_date) = ?1")
    List<Program> findByOnAirDate(Date date);

    @Query("SELECT p FROM program p WHERE on_air_date >= ?1 and on_air_date < ?2 and team_id is not null")
    List<Program> findByOnAirDateTeamId(LocalDateTime ldtFrom, LocalDateTime ldtTo);

    @Query("SELECT t FROM program t WHERE fct_chk = ?1")
    List<Program> findByFctChk(int i);

    @Query("SELECT count(*) FROM program WHERE title = ?1 and station_id = ?2 and on_air_date = ?3")
    Long hasProgram(String title, Long stationId, LocalDateTime onAirDate);

    @Query("SELECT t FROM program t WHERE title = ?1 and station_id = ?2 and on_air_date = ?3")
    Program findByIdentity(String title, Long stationId, LocalDateTime onAirDate);
}

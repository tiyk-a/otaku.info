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

    @Query("SELECT p FROM program p WHERE DATE(on_air_date) >= ?1 and DATE(on_air_date) <= ?2 and del_flg = 0")
    List<Program> findByOnAirDateBeterrn(Date from, Date fo);

    @Query(nativeQuery = true, value = "SELECT p.* FROM program p inner join p_rel b on p.program_id = b.program_id WHERE p.on_air_date >= ?1 and p.on_air_date < ?2 and b.team_id is not null")
    List<Program> findByOnAirDateTeamId(LocalDateTime ldtFrom, LocalDateTime ldtTo);

    @Query("SELECT t FROM program t WHERE fct_chk = ?1")
    List<Program> findByFctChk(boolean fct_chk);

    @Query("SELECT count(*) FROM program WHERE title = ?1 and station_id = ?2 and on_air_date = ?3")
    Long hasProgram(String title, Long stationId, LocalDateTime onAirDate);

    @Query("SELECT t FROM program t WHERE title = ?1 and station_id = ?2 and on_air_date = ?3")
    Program findByIdentity(String title, Long stationId, LocalDateTime onAirDate);

    @Query(nativeQuery = true, value = "select a.* from program a inner join p_rel b on a.program_id = b.program_id where b.team_id = ?1 and a.del_flg = 0 and a.on_air_date >= CURRENT_DATE order by a.on_air_date desc")
    List<Program> findbyTeamId(Long teamId, Long limit);
}

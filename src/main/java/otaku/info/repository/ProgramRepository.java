package otaku.info.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import otaku.info.entity.Program;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface ProgramRepository extends JpaRepository<Program, Long> {

    @Query("select t from program t where program_id = ?1")
    Optional<Program> findByPId(Long programId);

    @Query("select t from program t where program_id in ?1")
    List<Program> findByIdList(List<Long> pIdList);

    @Query(nativeQuery = true, value = "SELECT * FROM program p WHERE DATE(on_air_date) >= ?1 limit 20")
    List<Program> findByOnAirDate(Date date);

    @Query(nativeQuery = true, value = "SELECT * FROM program p WHERE DATE(on_air_date) >= ?1 and pm_id is null and del_flg = ?2 limit ?3")
    List<Program> findByOnAirDatePmIdNullDelFlg(Date date, Boolean delFlg, Integer limit);

    @Query("SELECT p FROM program p WHERE DATE(on_air_date) >= ?1 and DATE(on_air_date) <= ?2 and del_flg = 0")
    List<Program> findByOnAirDateBeterrn(Date from, Date fo);

    @Query(nativeQuery = true, value = "SELECT p.* FROM program p inner join p_rel b on p.program_id = b.program_id WHERE p.on_air_date >= ?1 and p.on_air_date < ?2")
    List<Program> findByOnAirDateBetween(LocalDateTime ldtFrom, LocalDateTime ldtTo);

    @Query("SELECT t FROM program t WHERE fct_chk = ?1")
    List<Program> findByFctChk(boolean fct_chk);

    @Query("SELECT count(*) FROM program WHERE title = ?1 and station_id = ?2 and on_air_date = ?3")
    Long hasProgram(String title, Long stationId, LocalDateTime onAirDate);

    @Query("SELECT t FROM program t WHERE title = ?1 and station_id = ?2 and on_air_date = ?3")
    Program findByIdentity(String title, Long stationId, LocalDateTime onAirDate);

    @Query(nativeQuery = true, value = "select a.* from program a inner join p_rel b on a.program_id = b.program_id where b.team_id = ?1 and a.del_flg = 0 and a.on_air_date >= CURRENT_DATE order by a.on_air_date asc")
    List<Program> findbyTeamId(Long teamId);

    @Query(nativeQuery = true, value = "select a.* from program a inner join p_rel b on a.program_id = b.program_id where b.team_id = ?1 and a.del_flg = 0 and a.on_air_date >= CURRENT_DATE and a.pm_id is null and a.del_flg = ?2 order by a.on_air_date asc limit ?3")
    List<Program> findbyTeamIdPmIdNullDelFlg(Long teamId, Boolean delFlg, Integer limit);

    @Query("select t from program t where station_id = ?1")
    List<Program> findbyStationId(Long sId);

    @Query(nativeQuery = true, value = "SELECT p.* FROM program p inner join p_rel b on p.program_id = b.program_id WHERE DATE(p.on_air_date) = ?1 and b.team_id = ?2")
    List<Program> findByOnAirDateTeamId(Date date, Long teamId);

    @Query(nativeQuery = true, value = "select b.team_id, count(*) from program a inner join p_rel b on a.program_id = b.program_id where a.del_flg = 0 and a.on_air_date >= CURRENT_DATE and a.pm_id is null and a.del_flg = 0 group by b.team_id")
    List<Object[]> getNumbersOfEachTeamIdFutureNotDeletedNoPM();
}

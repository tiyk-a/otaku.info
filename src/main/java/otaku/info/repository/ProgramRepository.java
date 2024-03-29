package otaku.info.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import otaku.info.entity.Program;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface ProgramRepository extends JpaRepository<Program, Long> {

    @Query(nativeQuery = true, value = "select * from program t where DATE(on_air_date) >= '2022-01-01' and team_arr is null")
    List<Program> tmpMethod();

    @Query(nativeQuery = true, value = "select * from program t where DATE(on_air_date) < '2022-01-01' and team_arr is null")
    List<Program> tmpMethod2();

    @Query(nativeQuery = true, value = "SELECT * FROM program i WHERE team_arr like '%[%' or mem_arr like '%[%'")
    List<Program> findbyInvalidArr();

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

    @Query(nativeQuery = true, value = "SELECT p.* FROM program p WHERE p.on_air_date >= ?1 and p.on_air_date < ?2")
    List<Program> findByOnAirDateBetween(LocalDateTime ldtFrom, LocalDateTime ldtTo);

    @Query("SELECT t FROM program t WHERE fct_chk = ?1")
    List<Program> findByFctChk(boolean fct_chk);

    @Query("SELECT count(*) FROM program WHERE title = ?1 and station_id = ?2 and on_air_date = ?3")
    Long hasProgram(String title, Long stationId, LocalDateTime onAirDate);

    @Query("SELECT t FROM program t WHERE title = ?1 and station_id = ?2 and on_air_date = ?3")
    Program findByIdentity(String title, Long stationId, LocalDateTime onAirDate);

    @Query(nativeQuery = true, value = "select a.* from program a where FIND_IN_SET(?1, team_arr) and a.del_flg = 0 and a.on_air_date >= CURRENT_DATE order by a.on_air_date asc")
    List<Program> findbyTeamId(Long teamId);

    @Query(nativeQuery = true, value = "select a.* from program a where FIND_IN_SET(?1, team_arr) and a.del_flg = 0 and a.on_air_date >= CURRENT_DATE and a.pm_id is null and a.del_flg = ?2 order by a.on_air_date asc, a.title asc limit ?3")
    List<Program> findbyTeamIdPmIdNullDelFlg(Long teamId, Boolean delFlg, Integer limit);

    @Query("select t from program t where station_id = ?1")
    List<Program> findbyStationId(Long sId);

    @Query(nativeQuery = true, value = "SELECT p.* FROM program p WHERE DATE(p.on_air_date) = ?1 and FIND_IN_SET(?2, team_arr)")
    List<Program> findByOnAirDateTeamId(Date date, Long teamId);

    @Query(nativeQuery = true, value = "select count(*) from program a where a.del_flg = 0 and a.on_air_date >= CURRENT_DATE and a.pm_id is null and a.del_flg = 0 and FIND_IN_SET(?1, a.team_arr)")
    int getNumberOfTeamIdFutureNotDeletedNoPM(Long teamId);

    @Query(nativeQuery = true, value = "SELECT count(*) FROM program p WHERE pm_id = ?1 and del_flg = 0 and on_air_date >= CURRENT_DATE")
    int findByPmId(Long pmId);

    @Query(nativeQuery = true, value = "SELECT count(*) FROM program p WHERE pm_id is null and del_flg = 0 and on_air_date >= CURRENT_DATE")
    int findByPmIdIsNull();
}

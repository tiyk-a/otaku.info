package otaku.info.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import otaku.info.entity.Program;

import java.util.Date;
import java.util.List;

public interface ProgramRepository extends JpaRepository<Program, Long> {

    @Query("SELECT COUNT(*) FROM program WHERE program_code = ?1")
    Long hasProgramCode(String code);

    @Query("SELECT p FROM program p WHERE DATE(on_air_date) = ?1")
    List<Program> findByOnAirDate(Date date);
}

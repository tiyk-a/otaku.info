package otaku.info.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import otaku.info.entity.Program;

public interface ProgramRepository extends JpaRepository<Program, Long> {

    @Query("SELECT COUNT(*) FROM program WHERE program_code = ?1")
    Long hasProgramCode(String code);
}

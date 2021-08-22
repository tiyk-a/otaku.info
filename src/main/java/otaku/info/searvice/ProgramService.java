package otaku.info.searvice;

import lombok.AllArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import otaku.info.entity.Program;
import otaku.info.repository.ProgramRepository;

import javax.transaction.Transactional;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(value = Transactional.TxType.REQUIRES_NEW, rollbackOn = Throwable.class)
@AllArgsConstructor
public class ProgramService  {

    private final ProgramRepository programRepository;

    public Program save(Program program) {
        return programRepository.save(program);
    }

    public boolean hasProgramCode(String code) {
        Long result = programRepository.hasProgramCode(code);
        return result!=0;
    }

    public List<Program> findByOnAirDate(Date date) {
        return programRepository.findByOnAirDate(date);
    }

    public List<Program> findByOnAirDateTimeTeamId(LocalDateTime ldt, int hour) {
        return programRepository.findByOnAirDateTeamId(ldt, ldt.plusHours(hour));
    }

    public boolean waitingFctChk() {
        Long result = programRepository.waitingFctChk();
        return result!=0;
    }

    public List<Program> findByFctChk(int i) {
        return programRepository.findByFctChk(i);
    }

    public Optional<Program> findbyProgramId(Long programId) {
        return programRepository.findById(programId);
    };

    public boolean hasProgram(String title, Long stationId, LocalDateTime onAirDate) {
        Long result = programRepository.hasProgram(title, stationId, onAirDate);
        return result!=0;
    }

    public Program findByIdentity(String title, Long stationId, LocalDateTime onAirDate) {
        return programRepository.findByIdentity(title, stationId, onAirDate);
    }

    public Program overwrite(Long programId, Program program) {
        Program overridden = new Program();
        BeanUtils.copyProperties(program, overridden);
        overridden.setProgram_id(programId);
        return programRepository.save(overridden);
    }
}

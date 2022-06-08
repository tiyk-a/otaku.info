package otaku.info.service;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import otaku.info.entity.Item;
import otaku.info.entity.Program;
import otaku.info.repository.ProgramRepository;

import javax.transaction.Transactional;
import java.time.*;
import java.util.Date;
import java.util.List;

@Service
@Transactional(value = Transactional.TxType.REQUIRES_NEW, rollbackOn = Throwable.class)
@AllArgsConstructor
public class ProgramService  {

    @Autowired
    PRelService pRelService;

    private final ProgramRepository programRepository;

    public Program save(Program program) {
        return programRepository.save(program);
    }

    public Program findByPId(Long programId) {
        return programRepository.findByPId(programId).orElse(null);
    }

    public List<Program> findByOnAirDate(Date date) {
        return programRepository.findByOnAirDate(date);
    }

    public List<Program> findByOnAirDatePmIdNullDelFlg(Date date, Boolean delFlg) {
        return programRepository.findByOnAirDatePmIdNullDelFlg(date, delFlg);
    }

    public List<Program> findByOnAirDateBeterrn(Date from, Date to) {
        return programRepository.findByOnAirDateBeterrn(from, to);
    }

    public List<Program> findByOnAirDateTimeTeamId(LocalDateTime ldt, int hour) {
        return programRepository.findByOnAirDateTeamId(ldt, ldt.plusHours(hour));
    }

    public boolean hasProgram(String title, Long stationId, LocalDateTime onAirDate) {
        Long result = programRepository.hasProgram(title, stationId, onAirDate);
        return result!=0;
    }

    public Program findByIdentity(String title, Long stationId, LocalDateTime onAirDate) {
        return programRepository.findByIdentity(title, stationId, onAirDate);
    }

    public List<Program> findbyTeamId(Long teamId) {
        return programRepository.findbyTeamId(teamId);
    }

    public List<Program> findbyTeamIdPmIdNullDelFlg(Long teamId, Boolean delFlg) {
        return programRepository.findbyTeamIdPmIdNullDelFlg(teamId, delFlg);
    }

    public List<Program> findbyStationId(Long sId) {
        return programRepository.findbyStationId(sId);
    }

    public List<Program> saveAll(List<Program> pList) {
        return programRepository.saveAll(pList);
    }

    public List<Program> findByOnAirDateTeamId(Date date, Long teamId) {
        return programRepository.findByOnAirDateTeamId(date, teamId);
    }

    public List<Program> findByTeamIdFutureNotDeletedNoPM(Long teamId) {
        return programRepository.findByTeamIdFutureNotDeletedNoPM(teamId);
    }
}

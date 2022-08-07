package otaku.info.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import otaku.info.entity.Program;
import otaku.info.enums.TeamEnum;
import otaku.info.repository.ProgramRepository;

import javax.transaction.Transactional;
import java.time.*;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(value = Transactional.TxType.REQUIRES_NEW, rollbackOn = Throwable.class)
@AllArgsConstructor
public class ProgramService  {

    private final ProgramRepository programRepository;

    public List<Program> tmpMethod() {
        return programRepository.tmpMethod();
    }

    public List<Program> tmpMethod2() {
        return programRepository.tmpMethod2();
    }

    public Program save(Program program) {
        return programRepository.save(program);
    }

    public Program findByPId(Long programId) {
        return programRepository.findByPId(programId).orElse(null);
    }

    public List<Program> findByPidList(List<Long> pidList) {
        return programRepository.findByIdList(pidList);
    }

    public List<Program> findByOnAirDate(Date date) {
        return programRepository.findByOnAirDate(date);
    }

    public List<Program> findByOnAirDatePmIdNullDelFlg(Date date, Boolean delFlg, Integer limit) {
        return programRepository.findByOnAirDatePmIdNullDelFlg(date, delFlg, limit);
    }

    public List<Program> findByOnAirDateBeterrn(Date from, Date to) {
        return programRepository.findByOnAirDateBeterrn(from, to);
    }

    public List<Program> findByOnAirDateBetween(LocalDateTime ldt, int hour) {
        return programRepository.findByOnAirDateBetween(ldt, ldt.plusHours(hour));
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

    public List<Program> findbyTeamIdPmIdNullDelFlg(Long teamId, Boolean delFlg, Integer limit) {
        return programRepository.findbyTeamIdPmIdNullDelFlg(teamId, delFlg, limit);
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

    public Map<Long, Integer> getNumbersOfEachTeamIdFutureNotDeletedNoPM() {
        Map<Long, Integer> mappedResult = new HashMap<>();
        for (TeamEnum teamEnum : TeamEnum.values()) {
            int i = programRepository.getNumberOfTeamIdFutureNotDeletedNoPM(teamEnum.getId());
            mappedResult.put(teamEnum.getId(), i);
        }
        return mappedResult;
    }
}

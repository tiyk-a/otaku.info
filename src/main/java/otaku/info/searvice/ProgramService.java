package otaku.info.searvice;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import otaku.info.entity.Program;
import otaku.info.repository.ProgramRepository;

import javax.transaction.Transactional;
import java.time.*;
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

    public List<Program> findByOnAirDate(Date date) {
        return programRepository.findByOnAirDate(date);
    }

    public List<Program> findByOnAirDateTimeTeamId(LocalDateTime ldt, int hour) {
        return programRepository.findByOnAirDateTeamId(ldt, ldt.plusHours(hour));
    }

    public List<Program> findByFctChk(int i) {
        return programRepository.findByFctChk(i == 1);
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

    /**
     * チームIDとメンバーIDのみをoverrideします。
     *
     * @param programId
     * @param program
     * @return
     */
    public Program overwrite(Long programId, Program program) {
        // 更新するレコード
        Program overridden = findbyProgramId(programId).orElse(new Program());

        // チームIDを更新
        if (StringUtils.isBlank(overridden.getTeam_id())) {
            overridden.setTeam_id(program.getTeam_id());
        } else {
            if (!overridden.getTeam_id().contains(program.getTeam_id())) {
                overridden.setTeam_id(overridden.getTeam_id().concat("," + program.getTeam_id()));
            }
        }

        // メンバーIDを更新
        if (StringUtils.isBlank(overridden.getMember_id())) {
            overridden.setMember_id(program.getMember_id());
        } else {
            if (!overridden.getMember_id().contains(program.getMember_id())) {
                overridden.setMember_id(overridden.getMember_id().concat("," + program.getMember_id()));
            }
        }

        // データを更新しているのでfct_chkカラムは未確認(false)にする。
        overridden.setFct_chk(false);
        return programRepository.save(overridden);
    }

    public List<Program> findAll() {
        return programRepository.findAll();
    }
}

package otaku.info.searvice;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import otaku.info.entity.Program;
import otaku.info.entity.PRel;
import otaku.info.enums.MemberEnum;
import otaku.info.repository.ProgramRepository;

import javax.transaction.Transactional;
import java.time.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    public List<Program> findByOnAirDate(Date date) {
        return programRepository.findByOnAirDate(date);
    }

    public List<Program> findByOnAirDateBeterrn(Date from, Date to) {
        return programRepository.findByOnAirDateBeterrn(from, to);
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
     * Programのチーム情報・メンバー情報を更新します。
     *
     * @param programId
     * @param teamIdList
     * @param memberIdList
     * @return
     */
    public List<PRel> overwrite(Long programId, List<Long> teamIdList, List<Long> memberIdList) {
        // 更新するレコード
        List<PRel> curPRelList = pRelService.getListByProgramId(programId);
        List<PRel> addRelList = new ArrayList<>();

        // TeamIdの追加処理
        if (teamIdList != null && teamIdList.size() > 0) {
            List<Long> curTeamIdList = curPRelList.stream().map(PRel::getTeam_id).collect(Collectors.toList());

            for (Long candTeamId : teamIdList) {
                if (!curTeamIdList.contains(candTeamId)) {
                    PRel rel = new PRel();
                    rel.setProgram_id(programId);
                    rel.setTeam_id(candTeamId);
                    addRelList.add(rel);
                }
            }
        }

        // MemberIdの追加処理
        if (memberIdList != null && memberIdList.size() > 0) {
            List<Long> curMemberIdList = new ArrayList<>();
            for (PRel rel : curPRelList) {
                if (rel.getMember_id() != null) {
                    curMemberIdList.add(rel.getMember_id());
                }
            }

            for (Long candMemberId : memberIdList) {
                if (!curMemberIdList.contains(candMemberId)) {
                    // 既存RelのTeamにこのメンバーのチームがあって、Memberが空だったら入れてあげる
                    Long teamId = MemberEnum.getTeamIdById(candMemberId);
                    if (curPRelList.stream().anyMatch(e -> e.getTeam_id().equals(teamId) && e.getMember_id() == null)) {
                        for (PRel rel : curPRelList) {
                            if (rel.getTeam_id().equals(teamId) && rel.getMember_id() == null) {
                                curPRelList.remove(rel);
                                rel.setMember_id(candMemberId);
                                addRelList.add(rel);
                                break;
                            }
                        }
                    } else if (addRelList.size() > 0 && addRelList.stream().anyMatch(e -> e.getTeam_id().equals(teamId))) {
                        // 今回追加するRelリストのTeamにこのメンバーのチームがあったらMember空だから入れてあげる
                        for (PRel rel : addRelList) {
                            if (rel.getTeam_id().equals(teamId)) {
                                rel.setMember_id(candMemberId);
                                break;
                            }
                        }
                    } else {
                        // 既存RelのTeamにこのメンバーのチームがなかったら、新規でRelを作ってあげる
                        PRel rel = new PRel();
                        rel.setProgram_id(programId);
                        rel.setTeam_id(teamId);
                        rel.setMember_id(candMemberId);
                        addRelList.add(rel);
                    }
                }
            }
        }
        if (addRelList.size() > 0) {
            pRelService.saveAll(addRelList);
        }
        curPRelList.addAll(addRelList);
        return curPRelList;
    }
}

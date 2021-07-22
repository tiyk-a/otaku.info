package otaku.info.controller;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import otaku.info.dto.TvDto;
import otaku.info.entity.Program;
import otaku.info.searvice.MemberService;
import otaku.info.searvice.ProgramService;
import otaku.info.searvice.StationService;
import otaku.info.searvice.TeamService;
import otaku.info.utils.DateUtils;

import java.util.List;

@RestController("/python")
@AllArgsConstructor
public class PythonController {

    @Autowired
    private ProgramService programService;

    @Autowired
    private StationService stationService;

    @Autowired
    private TeamService teamService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private DateUtils dateUtils;

    @PostMapping("/postTvProgram")
    public void postTvProgram(@RequestBody TvDto tvDto) {
        Program program = new Program();

        BeanUtils.copyProperties(tvDto, program);

        // 既に登録された番組でなければ登録処理を実行
        if (!programService.hasProgramCode(program.getProgram_code())) {
            program.setStation_id(findStationId(tvDto.getStation()));
            program.setOn_air_date(dateUtils.stringToLocalDateTime(tvDto.getOn_air_date()));

            List<Long> teamIdList = teamService.findTeamIdListByText(tvDto.getTitle());
            teamIdList.addAll(teamService.findTeamIdListByText(tvDto.getDescription()));
            String teamIdStr = StringUtils.join(teamIdList, ',');
            program.setTeam_id(teamIdStr);

            List<Long> memberIdList = memberService.findMemberIdByText(tvDto.getTitle());
            memberIdList.addAll(memberService.findMemberIdByText(tvDto.getDescription()));
            String memberIdStr = StringUtils.join(memberIdList, ',');
            program.setMember_id(memberIdStr);
            programService.save(program);
            System.out.println(program.getCreated_at() + " Program Saved: " + program.getTitle());
        }
    }

    public Long findStationId(String stationName) {
        return stationService.findStationId(stationName);
    }
}

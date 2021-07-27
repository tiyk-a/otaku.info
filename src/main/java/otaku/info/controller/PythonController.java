package otaku.info.controller;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jettison.json.JSONException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import otaku.info.dto.DbNotifDto;
import otaku.info.dto.TvDto;
import otaku.info.entity.Program;
import otaku.info.searvice.MemberService;
import otaku.info.searvice.ProgramService;
import otaku.info.searvice.StationService;
import otaku.info.searvice.TeamService;
import otaku.info.utils.DateUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

/**
 * pyTwi2からリクエストを受けて何かしらの処理をするコントローラー
 *
 * パスはダイレクトに@xxMappingのが生きています。@RestController("/python")が効いていない。
 */
@RestController("/python")
@AllArgsConstructor
public class PythonController {

    @Autowired
    LineController lineController;

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

    @Autowired
    RestTemplate restTemplate;

    /**
     * Pythonにツイートするようにデータを送る
     *
     * @param teamId
     * @param text
     * @return
     * @throws JSONException
     */
//    public String post(Map<String, String> headers, String json) {
    public String post(Integer teamId, String text) throws JSONException {
        System.out.println("これをTweetします " + text);

        String url = "https://pytwi2.herokuapp.com/twi";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        Map<String, Object> map = new HashMap<>();
        map.put("title", text);
        map.put("teamId", teamId);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(map, headers);
        restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));
        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

        List<DbNotifDto> dbNotifDtoList = new ArrayList<>();
        dbNotifDtoList.add(new DbNotifDto(text + " ■teamId=" + teamId, null, LocalDateTime.now()));
        if (response.getStatusCode() == HttpStatus.CREATED) {
            System.out.println("Request Successful: " + text);
        } else {
            System.out.println("Request Failed: " + text);
        }
        // LINEに投稿完了通知を送る
        lineController.postAll(dbNotifDtoList);
        return "done";
    }

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

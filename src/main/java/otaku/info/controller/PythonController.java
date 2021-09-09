package otaku.info.controller;

import lombok.AllArgsConstructor;
import org.codehaus.jettison.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import otaku.info.searvice.MemberService;
import otaku.info.searvice.ProgramService;
import otaku.info.searvice.StationService;
import otaku.info.searvice.TeamService;
import otaku.info.setting.Setting;
import otaku.info.utils.DateUtils;

import java.nio.charset.StandardCharsets;
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
    private Setting setting;

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


        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        Map<String, Object> map = new HashMap<>();
        map.put("title", text);
        map.put("teamId", teamId);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(map, headers);
        restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));
        ResponseEntity<String> response = restTemplate.postForEntity(setting.getPythonTwitter(), entity, String.class);

        List<String> lineList = new ArrayList<>();
        lineList.add(text + " ■teamId=" + teamId);
        if (response.getStatusCode() == HttpStatus.CREATED) {
            System.out.println("Request Successful: " + text);
        } else {
            System.out.println("Request Failed: " + text);
        }
        // LINEに投稿完了通知を送る
        lineController.postAll(lineList);
        return "done";
    }
}

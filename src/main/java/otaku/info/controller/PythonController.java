package otaku.info.controller;

import lombok.AllArgsConstructor;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import otaku.info.service.MemberService;
import otaku.info.service.ProgramService;
import otaku.info.service.StationService;
import otaku.info.service.TeamService;
import otaku.info.setting.Log4jUtils;
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

    final Logger logger = Log4jUtils.newConsoleCsvAllLogger("PythonController");
    final Logger twLog = Log4jUtils.newFileLogger("PythonControllerTw", "Twitter.log");

    @Autowired
    LineController lineController;

    @Autowired
    private final ProgramService programService;

    @Autowired
    private final StationService stationService;

    @Autowired
    private final TeamService teamService;

    @Autowired
    private final MemberService memberService;

    @Autowired
    private final DateUtils dateUtils;

    @Autowired
    private final Setting setting;

    /**
     * Pythonにツイートするようにデータを送る
     *
     * @param teamId
     * @param text
     * @return
     * @throws JSONException
     */
//    public String post(Map<String, String> headers, String json) {
    public String post(Long teamId, String text) throws JSONException {

        // Twitter投稿が終わった後にLINE通知するテキストを詰めていくリスト
        List<String> lineList = new ArrayList<>();

        // 開発環境の場合Twitterに投稿しない
        logger.debug("Env: " + setting.getTest());
        if (setting.getTest() != null && setting.getTest().equals("dev")) {
            lineList.add(text + " ■teamId=" + teamId);
            twLog.debug("teamId:" + teamId + "■" + text);
        } else {
            if (teamId != null && StringUtils.hasText(text)) {
                logger.debug("🕊 " + text);

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
                Map<String, Object> map = new HashMap<>();
                map.put("title", text);
                map.put("teamId", teamId);
                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(map, headers);

                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));

                ResponseEntity<String> response = restTemplate.postForEntity(setting.getPythonTwitter(), entity, String.class);

                lineList.add(text + " ■teamId=" + teamId);
                logger.debug("Twitter posted ID:" + teamId + ": " + response.getStatusCode() + ":" + text);
                twLog.debug("teamId:" + teamId + "■" + text);
            }
        }
        // LINEに投稿完了通知を送る
        lineController.postAll(lineList);
        return "done";
    }
}

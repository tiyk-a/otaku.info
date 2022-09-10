package otaku.info.controller;

import lombok.AllArgsConstructor;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import otaku.info.setting.Log4jUtils;
import otaku.info.setting.Setting;

import java.net.URLEncoder;
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
    private final Setting setting;

    /**
     * ルートへ接続テスト
     *
     * @return
     */
    public String test() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));

        try {
            // herokuの次のpytwi2
            System.out.println(setting.getPyTwi2());
            ResponseEntity<String> response = restTemplate.getForEntity(setting.getPyTwi2(), String.class);
            if (response.getStatusCode() != HttpStatus.ACCEPTED && response.getStatusCode() != HttpStatus.CREATED) {
                logger.debug(response.getBody());
            }
        } catch (Exception e) {
            logger.error("Pythonエラー");
            e.printStackTrace();
        }
        System.out.println("koko");
        return "FIN.";
    }

    /**
     * Pythonにツイートするようにデータを送る
     *
     * @param teamId
     * @param text
     * @return
     * @throws JSONException
     */
    public String post(Long teamId, String text) throws JSONException {

        // Twitter投稿が終わった後にLINE通知するテキストを詰めていくリスト
//        List<String> lineList = new ArrayList<>();

        // 開発環境の場合Twitterに投稿しない
        logger.debug("Env: " + setting.getTest());
//        if (setting.getTest() != null && setting.getTest().equals("dev")) {
////            lineList.add(text + " ■teamId=" + teamId);
//            twLog.debug("teamId:" + teamId + "■" + text);
//        } else {
            if (teamId != null && StringUtils.hasText(text)) {
                logger.debug("🕊 " + text);

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
                Map<String, Object> map = new HashMap<>();
                map.put("title", text);
                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(map, headers);

                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));

                try {
                    ResponseEntity<String> response = restTemplate.postForEntity(setting.getPyTwi2() + "twi?teamId=" + teamId, entity, String.class);
                    if (response.getStatusCode() != HttpStatus.ACCEPTED && response.getStatusCode() != HttpStatus.CREATED) {
                        logger.debug("Response status CHECKER HIT");
                        logger.debug(response.getBody());
                    }
                    logger.debug("Twitter posted ID:" + teamId + ": " + response.getStatusCode() + ":" + text);
                } catch (HttpServerErrorException e) {
                    logger.debug("500エラーが返ってきました");
                } catch (Exception e) {
                    logger.error("Pythonエラー");
                    e.printStackTrace();
                }

//                lineList.add(text + " ■teamId=" + teamId);
                twLog.debug("teamId:" + teamId + "■" + text);
            }
//        }
        return "done";
    }
}

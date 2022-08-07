package otaku.info.controller;

import lombok.AllArgsConstructor;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Controller;
import org.springframework.web.client.RestTemplate;
import otaku.info.setting.Log4jUtils;
import otaku.info.setting.Setting;

import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * LINEへ投稿を依頼します。
 *
 */
@Controller
@AllArgsConstructor
public class LineController {

    final Logger logger = Log4jUtils.newConsoleCsvAllLogger("LineController");

    @Autowired
    Setting setting;

    /**
     * LINEにポストします。
     *
     * @param messageList
     * @return
     * @throws JSONException
     */
    public String postAll(List<String> messageList) {
        if (messageList == null || messageList.size() == 0) {
            return "done";
        }

        for (String msg: messageList) {
            if (msg == null) {
                continue;
            }

            String outline = "";
            if (msg.length() < 31) {
                outline = msg;
            } else {
                outline = msg.substring(0,30);
            }

            logger.debug("💬 " + outline);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            Map<String, Object> map = new HashMap<>();
            map.put("text", msg);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(map, headers);
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));
            if (setting.getTest() != null && setting.getTest().equals("dev")) {
                logger.debug("🕊: " + msg);
            } else {
                ResponseEntity<String> response = restTemplate.postForEntity(setting.getLineUrl(), entity, String.class);
                logger.debug(outline + ":" + response);
            }
        }
        return "done";
    }

    /**
     * メッセージをLINE通知します。
     *
     * @param message
     * @return
     */
    public String post(String message) {

        String outline = message;
        if (message.length() > 30) {
            outline = message.substring(0,30);
        }
        logger.debug("💬 " + message);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        Map<String, Object> map = new HashMap<>();
        map.put("text", message);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(map, headers);
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));
        if (setting.getTest() != null && setting.getTest().equals("dev")) {
            logger.debug("🕊: " + message);
        } else {
            ResponseEntity<String> response = restTemplate.postForEntity(setting.getLineUrl(), entity, String.class);
            logger.debug("Request Successful: " + outline);
        }
        return "done";
    }
}

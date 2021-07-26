package otaku.info.controller;

import lombok.AllArgsConstructor;
import org.codehaus.jettison.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Controller;
import org.springframework.web.client.RestTemplate;
import otaku.info.dto.DbNotifDto;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * LINEへ投稿を依頼します。
 *
 */
@Controller
@AllArgsConstructor
public class LineController {

    @Autowired
    RestTemplate restTemplate;

    public String postAll(List<DbNotifDto> dbNotifDtoList) throws JSONException {
        String url = "https://line-chiharu-ml.herokuapp.com/dbNotify";

        for (DbNotifDto dto: dbNotifDtoList) {
            String outline = dto.getData().substring(0,30);
            System.out.println("これをpostします： " + outline);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            Map<String, Object> map = new HashMap<>();
            map.put("text", dto.getData());
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(map, headers);
            restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                System.out.println("Request Successful: " + outline);
            } else {
                System.out.println("Request Failed: " + outline);
            }
        }
        return "done";
    }
}

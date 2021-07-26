package otaku.info.controller;

import lombok.AllArgsConstructor;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.RestTemplate;
import otaku.info.dto.DbNotifDto;
import otaku.info.entity.Item;
import otaku.info.entity.Program;
import otaku.info.searvice.ItemService;
import otaku.info.searvice.ProgramService;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * LINEへ投稿を依頼します。
 *
 */
@Controller
@AllArgsConstructor
public class LineController {

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    ItemService itemService;

    @Autowired
    ProgramService programService;

    final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");

    final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

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

    /**
     *
     * @param req カンマ区切りのStringは「i:item or p:program, 1:日時更新 or 2:del_flgオン, Id, yyyyMMdd or yyyyMMddHHmm」
     * @return
     * @throws JSONException
     * @throws ParseException
     */
    @PostMapping("/line/db")
    public String dbManage(@RequestBody String req) throws JSONException, ParseException {
        JSONObject json = new JSONObject(req);
        String text = (String) json.get("text");
        String[] elems = text.split(",");

        if (elems.length < 3) {
            return "Lack elements" + Arrays.toString(elems);
        }

        if (elems[0].equals("i")) {
            // Item
            Item item = itemService.findByItemId((long) Integer.parseInt(elems[2])).orElse(new Item());

            if (item.getItem_id() == null) {
                return "No item found";
            }

            if (elems[1].equals("1")) {
                // Item: Update publication_date(yyyyMMdd)
                item.setPublication_date(new Date(sdf.parse(elems[3]).getTime()));
                // Set fct_chk = true
                item.setFct_chk(true);
            } else if (elems[1].equals("2")) {
                // Item: del_flg on
                item.setDel_flg(true);
                // Set fct_chk = true
                item.setFct_chk(true);
            }
            return itemService.saveItem(item).toString();
        } else if (elems[0].equals("p")) {
            // Program
            Program program = programService.findbyProgramId((long) Integer.parseInt(elems[2])).orElse(new Program());

            if (program.getProgram_id() == null) {
                return "No program found";
            }

            // Program
            if (elems[1].equals("1")) {
                // Program: Update OnAirDate(yyyyMMddHHmm)
                program.setOn_air_date(LocalDateTime.parse(elems[3], formatter));
                // Set fct_chk = true
                program.setFct_chk(true);
            } else if (elems[1].equals("2")) {
                // Program: del_flg on
                program.setDel_flg(true);
                // Set fct_chk = true
                program.setFct_chk(true);
            }
            return programService.save(program).toString();
        }
        return "Not enough parameter";
    }
}

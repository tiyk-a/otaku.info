package otaku.info.controller;

import java.nio.charset.StandardCharsets;
import java.util.*;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import otaku.info.dto.TwiDto;
import otaku.info.entity.Item;
import otaku.info.searvice.db.ItemService;

@RestController("/")
//@AllArgsConstructor
public class SampleController {

    @Autowired
    private RakutenController rakutenController;

    @Autowired
    private TextController textController;

    @Autowired
    private Controller controller;

    @Autowired
    private ItemService itemService;

    @Autowired
    RestTemplate restTemplate;

    @GetMapping("/testdesu/{artistId}")
    public String sample3(@PathVariable String artistId) throws JSONException {
        Item item = new Item();
        item.setSite_id(1);
        item.setPrice(100);
        item.setUrl("hjjkl");
        item.setItem_code("dfghj");
        item.setTeam_id(8);
        Item item1 = itemService.saveItem(item);
        return item1.toString();
    }

    @GetMapping("/twi/{artistId}")
    public String sample1(@PathVariable String artistId) throws JSONException {
        List<String> list = controller.affiliSearchWord(artistId);
        List<Item> itemList = rakutenController.search(list);

        List<Item> savedItemList = rakutenController.saveItems(itemList);
        if (savedItemList.size() > 0) {
            for (Item item: savedItemList) {
                TwiDto twiDto = new TwiDto();
                twiDto.url =item.getUrl();
                System.out.println(item.getUrl());
                twiDto.title = item.getTitle();
                String result = textController.twitter(twiDto);
                post(item.getTeam_id(), result);
            }
        }
        return "Ok";
    }

    public String sample2(Long teamId, String artist) throws JSONException {
        List<String> list = controller.affiliSearchWord(artist);
        List<Item> itemList = rakutenController.search(list);
        System.out.println("➓楽天APIから受信したItemのリスト");
        for (Item item : itemList) {
            item.setTeam_id(Math.toIntExact(teamId));
            System.out.println(item.getTeam_id());
        }
        System.out.println("１２：楽天APIから受信したItemのリストをDB保存します");
        List<Item> savedItemList = rakutenController.saveItems(itemList);
        System.out.println("１２：楽天APIから受信したItemのリストをDB保存しました");
        if (savedItemList.size() > 0) {
            System.out.println("13：保存したItemをTweetします");
            for (Item item: savedItemList) {
                System.out.println("１４；保存するItemはこちら");
                System.out.println(item.getTitle());
                TwiDto twiDto = new TwiDto();
                twiDto.setUrl(item.getUrl());
                twiDto.setTitle(item.getTitle());
                String result = textController.twitter(twiDto);
                post(item.getTeam_id(), result);
            }
        }
        return "Ok";
    }

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

        if (response.getStatusCode() == HttpStatus.CREATED) {
            System.out.println("Request Successful");
            System.out.println(text);
        } else {
            System.out.println("Request Failed");
            System.out.println(text);
        }
        return "done";
    }
}


package otaku.info.controller;

import java.util.*;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
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

//    @GetMapping("/{artistId}")
//    public String sample(@PathVariable String artistId) {
//        List<String> list = controller.affiliSearchWord(artistId);
//        List<Item> itemList = rakutenController.search(list);
//        rakutenController.saveItems(itemList);
//        return "Hello2";
//    }

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
                post(result);
            }
        }
        return "Ok";
    }

    public String sample2(String artistId) throws JSONException {
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
                post(result);
            }
        }
        return "Ok";
    }

//    public String post(Map<String, String> headers, String json) {
    public String post(String text) throws JSONException {

        String url = "http://localhost:5000/twi";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        Map<String, Object> map = new HashMap<>();
        map.put("title", text);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(map, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

        if (response.getStatusCode() == HttpStatus.CREATED) {
            System.out.println("Request Successful");
            System.out.println(response.getBody());
        } else {
            System.out.println("Request Failed");
            System.out.println(response.getStatusCode());
        }
        return "done";
    }
}


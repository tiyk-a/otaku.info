package otaku.info.controller;

import java.nio.charset.StandardCharsets;
import java.util.*;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.codehaus.jettison.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.http.*;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import otaku.info.dto.TwiDto;
import otaku.info.entity.DelItem;
import otaku.info.entity.Item;
import otaku.info.searvice.db.DelItemService;
import otaku.info.searvice.db.ItemService;

@RestController("/")
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
    private DelItemService delItemService;

    @Autowired
    RestTemplate restTemplate;

    @GetMapping("/tmpMethod")
    public String tempMethod() throws ChangeSetPersister.NotFoundException {
        List<String> itemCodeList = itemService.tmpMethod();
        List<Item> itemList = rakutenController.search1(itemCodeList);
        for (Item item : itemList) {
            Item originalItem = itemService.findByItemId(itemService.findItemId(item.getItem_code())).orElseThrow(ChangeSetPersister.NotFoundException::new);
            if (item.getTitle() != null && originalItem.getTitle() == null) {
                originalItem.setTitle(item.getTitle());
            }
            if (item.getItem_caption() != null && originalItem.getItem_caption() == null) {
                originalItem.setItem_caption(item.getItem_caption());
            }
            itemService.updateItem(originalItem);
        }
        return "Done";
    }
    /**
     * ブラウザとかでテスト投稿（1件）がいつでもできるメソッド
     *
     * @param artistId
     * @return
     * @throws JSONException
     */
    @GetMapping("/twi/{artistId}")
    public String sample1(@PathVariable String artistId) throws JSONException {
        Item tmp = new Item();
        tmp.setSite_id(1);
        tmp.setItem_code("adcfvgbhnaa");
        tmp.setTeam_id(1);
        itemService.saveItem(tmp);
        DelItem tmpDel = new DelItem();
        tmpDel.setSite_id(1);
        tmpDel.setItem_code("asxcvbnmaa");
        tmpDel.setTeam_id(1);
        tmpDel.setItem_caption("aaaaaa");
        delItemService.saveItem(tmpDel);

        List<String> list = controller.affiliSearchWord(artistId);
//        List<Item> itemList = rakutenController.search(list);
        List<String> itemCodeList = rakutenController.search(list);

        itemCodeList = itemService.findNewItemList(itemCodeList);
        itemCodeList = delItemService.findNewItemList(itemCodeList);

        List<Item> newItemList = new ArrayList<>();
        if (itemCodeList.size() > 0) {
            newItemList = rakutenController.getDetailsByItemCodeList(itemCodeList);
        }


        System.out.println("１２：楽天APIから受信したItemのリストをDB保存します");
        List<Item> savedItemList = rakutenController.saveItems(newItemList);
        System.out.println(ToStringBuilder.reflectionToString(savedItemList, ToStringStyle.MULTI_LINE_STYLE));
//        System.out.println("13：保存したItemをTweetします");
//        if (savedItemList.size() > 0) {
//            for (Item item: savedItemList) {
//                System.out.println(item.getTitle());
//                TwiDto twiDto = new TwiDto();
//                twiDto.setUrl(item.getUrl());
//                twiDto.setTitle(item.getTitle());
//                String result = textController.twitter(twiDto);
//                post(item.getTeam_id(), result);
//            }
//        }
        List<Item> itemList = itemService.findAll();
        System.out.println(ToStringBuilder.reflectionToString(itemList, ToStringStyle.MULTI_LINE_STYLE));
        return itemList.toString();
    }

    /**
     * バッチで動かしてる定時楽天検索→Pythonにツイート命令を出すまでのメソッド
     *
     * @param teamId
     * @param artist
     * @return
     * @throws JSONException
     */
    public String sample2(Long teamId, String artist) throws JSONException {
        List<String> list = controller.affiliSearchWord(artist);
//        List<Item> itemList = rakutenController.search(list);
        List<String> itemCodeList = rakutenController.search(list);
        System.out.println("➓楽天APIから受信したItemCodeのリスト");

        itemCodeList = itemService.findNewItemList(itemCodeList);
        itemCodeList = delItemService.findNewItemList(itemCodeList);

        List<Item> newItemList = new ArrayList<>();
        if (itemCodeList.size() > 0) {
            newItemList = rakutenController.getDetailsByItemCodeList(itemCodeList);
        }

        // 検索の誤引っ掛かりした商品をストアするリスト
        List<Item> removeList = new ArrayList<>();
        if (newItemList.size() > 0) {
            for (Item item : newItemList) {
                item.setTeam_id(Math.toIntExact(teamId));
                // 検索の誤引っ掛かりを削除するため、アーティスト名がタイトルに含まれていないものを別リストに入れる
                if (!containsTeamName(artist, item.getTitle())) {
                    removeList.add(item);
                }
            }
        }

        // 保存する商品リストから不要な商品リストを削除する
        newItemList.removeAll(removeList);

        // 不要商品リストに入った商品は不要商品テーブルに格納する
        delItemService.saveAll(removeList);

        System.out.println("１２：楽天APIから受信したItemのリストをDB保存します");
        List<Item> savedItemList = rakutenController.saveItems(newItemList);
        itemService.flush();
        System.out.println("13：保存したItemをTweetします");
        if (savedItemList.size() > 0) {
            for (Item item: savedItemList) {
                System.out.println(item.getTitle());
                TwiDto twiDto = new TwiDto();
                twiDto.setUrl(item.getUrl());
                twiDto.setTitle(item.getTitle());
                String result = textController.twitter(twiDto);
                post(item.getTeam_id(), result);
            }
        }
        List<Item> listItem = itemService.findAll();
        return "Ok";
    }

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

        if (response.getStatusCode() == HttpStatus.CREATED) {
            System.out.println("Request Successful: " + text);
        } else {
            System.out.println("Request Failed: " + text);
        }
        return "done";
    }

    /**
     * アーティスト名がテキストの中に含まれているかどうかをチェックする
     *
     * @param teamName
     * @param text
     * @return
     */
    public boolean containsTeamName(String teamName, String text) {
        if (text.contains(teamName)) {
            return true;
        }
        return false;
    }
}


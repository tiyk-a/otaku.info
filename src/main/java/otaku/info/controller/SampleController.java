package otaku.info.controller;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.codehaus.jettison.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import otaku.info.dto.MemberSearchDto;
import otaku.info.dto.TwiDto;
import otaku.info.entity.Item;
import otaku.info.searvice.ItemService;
import otaku.info.searvice.TeamService;

/**
 * 楽天での商品検索指示〜Twitterポスト指示まで。
 *
 */
@RestController("/")
public class SampleController {

    @Autowired
    private RakutenController rakutenController;

    @Autowired
    private TextController textController;

    @Autowired
    private Controller controller;

    @Autowired
    private AnalyzeController analyzeController;

    @Autowired
    private PythonController pythonController;

    @Autowired
    private ItemService itemService;

    @Autowired
    private TeamService teamService;

    @Autowired
    RestTemplate restTemplate;

    /**
     * URLでアクセスできるtmpのメソッドです。
     * 任意に中身を変えます、テスト用。
     *
     * @return
     * @throws ChangeSetPersister.NotFoundException
     */
    @GetMapping("/tmpMethod")
    public String tempMethod() throws ParseException {
        String str = "関西版[本/雑誌] 2021年7月2日号 【表紙";
        Map<String, List<Date>> map = analyzeController.extractPublishDate(str);
        return map.toString();
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

        List<String> list = controller.affiliSearchWord(artistId);
        List<String> itemCodeList = rakutenController.search(list);

        itemCodeList = itemService.findNewItemList(itemCodeList);

        List<Item> newItemList = new ArrayList<>();
        if (itemCodeList.size() > 0) {
            newItemList = rakutenController.getDetailsByItemCodeList(itemCodeList);
        }

        System.out.println("１２：楽天APIから受信したItemのリストをDB保存します");
        List<Item> savedItemList = rakutenController.saveItems(newItemList);
        System.out.println(ToStringBuilder.reflectionToString(savedItemList, ToStringStyle.MULTI_LINE_STYLE));
        List<Item> itemList = itemService.findAll();
        System.out.println(ToStringBuilder.reflectionToString(itemList, ToStringStyle.MULTI_LINE_STYLE));
        return itemList.toString();
    }

    /**
     * Itemに不適切な商品が入ってしまっていたらItemId指定でdel_flgをonにします。
     * パラメータの指定：00-001-22（数字をハイフンで区切ることで複数商品を1リクエストで処理）
     *
     * @return String
     */
    @GetMapping("/moveToDelItem/{itemIdListStr}")
    public String moveToDelItem(@PathVariable String itemIdListStr) {
        List<Long> itemIdList = new ArrayList<>();
        List.of(itemIdListStr.split("-")).forEach(e -> itemIdList.add(Long.valueOf(e)));

        if (itemIdList.size() == 0) return "No Id provided";

        List<Long> notFoundIdList = new ArrayList<>();
        int successCount = 0;
        String result = "";
        for (Long itemId : itemIdList) {
            Item item = itemService.findByItemId(itemId).orElse(new Item());
            if (item.getItem_id() == null) {
                notFoundIdList.add(itemId);
            } else {
                successCount ++;
                item.setDel_flg(true);
                itemService.saveItem(item);
                result = result + "【" + successCount + "】itemId:" + item.getItem_id() + "Title: " + item.getTitle() + "-------------------";
            }

            if (notFoundIdList.size() > 0) {
                result = result + "Not found item";
                for (Long id : notFoundIdList) {
                    result = result + " item_id=" + id;
                }
            }
        }
        return result;
    }

    /**
     * バッチで動かしてる定時楽天検索→Pythonにツイート命令を出すまでのメソッド
     *
     * @param teamId
     * @param artist
     * @return
     * @throws JSONException
     */
    public String sample2(Long teamId, String artist) throws JSONException, ParseException {
        List<String> list = controller.affiliSearchWord(artist);
        List<String> itemCodeList = rakutenController.search(list);

        itemCodeList = itemService.findNewItemList(itemCodeList);

        List<Item> newItemList = new ArrayList<>();
        if (itemCodeList.size() > 0) {
            newItemList = rakutenController.getDetailsByItemCodeList(itemCodeList);
        }

        // 検索の誤引っ掛かりした商品をストアするリスト
        List<Item> removeList = new ArrayList<>();

        if (newItemList.size() > 0) {
            for (Item item : newItemList) {
                // 年月日のデータを集める
                Map<String, List<Date>> resultMap = analyzeController.extractPublishDate(item.getItem_caption());
                if (resultMap.get("publishDateList").size() == 0) {
                    Map<String, List<Date>> resultMap2 = analyzeController.extractPublishDate(item.getTitle());
                    if (resultMap.get("reserveDueList").size() == 0 && resultMap2.get("reserveDueList").size() > 0) {
                        resultMap.put("reserveDueList", resultMap2.get("reserveDueList"));
                    }
                    if (resultMap.get("publishDateList").size() == 0 && resultMap2.get("publishDateList").size() > 0) {
                        resultMap.put("publishDateList", resultMap2.get("publishDateList"));
                    }
                    if (resultMap.get("dateList").size() == 0 && resultMap2.get("dateList").size() > 0) {
                        resultMap.put("dateList", resultMap2.get("dateList"));
                    }
                }

                if (resultMap.get("publishDateList").size() > 0 || resultMap.get("dateList").size() > 0 ) {
                    item.setPublication_date(resultMap.get("publishDateList").get(0));
                    if (item.getPublication_date() == null) {
                        item.setPublication_date(resultMap.get("dateList").get(0));
                    }
                }

                item.setTeam_id(Math.toIntExact(teamId));
                // 検索の誤引っ掛かりを削除するため、アーティスト名がタイトルに含まれていないものを別リストに入れる
                String mnemonic = teamService.getMnemonic(artist);
                if (!containsTeamName(artist, item.getTitle()) && !containsTeamName(artist, item.getItem_caption())
                && (mnemonic != null && !containsTeamName(mnemonic, item.getTitle())) && (mnemonic != null && !containsTeamName(mnemonic, item.getItem_caption()))) {
                    removeList.add(item);
                }
            }
        }

        // 保存する商品リストから不要な商品リストを削除する
        newItemList.removeAll(removeList);

        // 不要商品リストに入った商品を商品テーブルに格納する
        if (removeList.size() > 0) {
            System.out.println("違う商品を保存します");
            removeList.forEach(e -> e.setDel_flg(true));
            itemService.saveAll(removeList);
        }

        List<Item> savedItemList = new ArrayList<>();
        if (newItemList.size() > 0) {
            System.out.println("商品を保存します");
            newItemList.forEach(e -> System.out.println(e.getTitle()));
            savedItemList = rakutenController.saveItems(newItemList);
        }
        if (savedItemList.size() > 0) {
            System.out.println("保存したItemをTweetします");
            for (Item item: savedItemList) {
                if (item.getPublication_date() != null && item.getPublication_date().after(Date.from(LocalDateTime.now().atZone(ZoneId.of("Asia/Tokyo")).toInstant()))) {
                    System.out.println(item.getTitle());
                    TwiDto twiDto = new TwiDto(item.getTitle(), item.getUrl(), item.getPublication_date(), null, (long) item.getTeam_id());
                    String result = textController.twitter(twiDto);
                    pythonController.post(item.getTeam_id(), result);
                } else {
                    System.out.println("未来商品ではないのでTweetしません");
                    System.out.println(item.getTitle());
                }
            }
        }
        return "Ok";
    }

    /**
     * バッチで動かしてる定時楽天検索（個人）→Pythonにツイート命令を出すまでのメソッド
     *
     * @param
     * @return
     * @throws JSONException
     */
    public String searchMember(MemberSearchDto dto) throws JSONException, ParseException {
        List<String> list = controller.affiliSearchWord(dto.getMember_name());
        List<String> itemCodeList = rakutenController.search(list);

        itemCodeList = itemService.findNewItemList(itemCodeList);

        List<Item> newItemList = new ArrayList<>();
        if (itemCodeList.size() > 0) {
            newItemList = rakutenController.getDetailsByItemCodeList(itemCodeList);
        }

        // 検索の誤引っ掛かりした商品をストアするリスト
        List<Item> removeList = new ArrayList<>();

        if (newItemList.size() > 0) {
            for (Item item : newItemList) {
                // 年月日のデータを集める
                Map<String, List<Date>> resultMap = analyzeController.extractPublishDate(item.getItem_caption());
                if (resultMap.get("publishDateList").size() == 0) {
                    Map<String, List<Date>> resultMap2 = analyzeController.extractPublishDate(item.getTitle());
                    if (resultMap.get("reserveDueList").size() == 0 && resultMap2.get("reserveDueList").size() > 0) {
                        resultMap.put("reserveDueList", resultMap2.get("reserveDueList"));
                    }
                    if (resultMap.get("publishDateList").size() == 0 && resultMap2.get("publishDateList").size() > 0) {
                        resultMap.put("publishDateList", resultMap2.get("publishDateList"));
                    }
                    if (resultMap.get("dateList").size() == 0 && resultMap2.get("dateList").size() > 0) {
                        resultMap.put("dateList", resultMap2.get("dateList"));
                    }
                }

                if (resultMap.get("publishDateList").size() > 0 || resultMap.get("dateList").size() > 0 ) {
                    item.setPublication_date(resultMap.get("publishDateList").get(0));
                    if (item.getPublication_date() == null) {
                        item.setPublication_date(resultMap.get("dateList").get(0));
                    }
                }

                item.setTeam_id(Math.toIntExact(dto.getTeam_id()));
                item.setArtist_id(Math.toIntExact(dto.getMember_id()));
                // 検索の誤引っ掛かりを削除するため、アーティスト名がタイトルに含まれていないものを別リストに入れる
                String mnemonic = teamService.getMnemonic(dto.getMember_name());
                if (!containsTeamName(dto.getMember_name(), item.getTitle()) && !containsTeamName(dto.getMember_name(), item.getItem_caption())
                        && (mnemonic != null && !containsTeamName(mnemonic, item.getTitle())) && (mnemonic != null && !containsTeamName(mnemonic, item.getItem_caption()))) {
                    removeList.add(item);
                }
            }
        }

        // 保存する商品リストから不要な商品リストを削除する
        newItemList.removeAll(removeList);

        // 不要商品リストに入った商品は不要商品テーブルに格納する
        if (removeList.size() > 0) {
            System.out.println("違う商品を保存します");
            removeList.forEach(e -> e.setDel_flg(true));
            itemService.saveAll(removeList);
        }

        List<Item> savedItemList = new ArrayList<>();
        if (newItemList.size() > 0) {
            System.out.println("商品を保存します");
            newItemList.forEach(e -> System.out.println(e.getTitle()));
            savedItemList = rakutenController.saveItems(newItemList);
        }
        if (savedItemList.size() > 0) {
            System.out.println("保存したItemをTweetします");
            for (Item item: savedItemList) {
                if (item.getPublication_date() != null && item.getPublication_date().after(Date.from(LocalDateTime.now().atZone(ZoneId.of("Asia/Tokyo")).toInstant()))) {
                    System.out.println(item.getTitle());
                    TwiDto twiDto = new TwiDto(item.getTitle(), item.getUrl(), item.getPublication_date(), null, (long) item.getTeam_id());
                    String result = textController.twitterPerson(twiDto, dto.getMember_name());
                    pythonController.post(item.getTeam_id(), result);
                } else {
                    System.out.println("未来商品ではないのでTweetしません");
                    System.out.println(item.getTitle());
                }
            }
        }
        return "Ok";
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

        // アーティスト名にスペースがあったら切り取って検索もする
        if (text.contains(" ")) {
            if (text.replaceAll(" ", "").contains(teamName)) {
                return true;
            }
        }
        return false;
    }
}


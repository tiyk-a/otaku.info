package otaku.info.controller;

import lombok.AllArgsConstructor;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import otaku.info.entity.AffeliUrl;
import otaku.info.entity.IM;
import otaku.info.entity.IRel;
import otaku.info.entity.Item;
import otaku.info.service.AffeliUrlService;
import otaku.info.service.IMService;
import otaku.info.service.IRelService;
import otaku.info.service.ItemService;
import otaku.info.setting.Log4jUtils;
import otaku.info.setting.Setting;
import otaku.info.utils.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 楽天アフェリエイトへ商品情報を取りに行くコントローラー
 *
 */
@AllArgsConstructor
@Controller
public class RakutenController {

    final Logger logger = Log4jUtils.newConsoleCsvAllLogger("RakutenController");

    @Autowired
    ItemUtils itemUtils;

    @Autowired
    ServerUtils serverUtils;

    @Autowired
    DateUtils dateUtils;

    @Autowired
    Setting setting;

    @Autowired
    IMService imService;

    @Autowired
    AffeliUrlService affeliUrlService;

    @Autowired
    IRelService iRelService;

    @Autowired
    JsonUtils jsonUtils;

    private final ItemService itemService;

    /**
     * 楽天APIにリクエストを投げる
     *
     * @param param Additional params. Will be connected to degault params
     * @return
     */
    public JSONObject request(String param, Long teamId) throws InterruptedException {
        JSONObject jsonObject = new JSONObject();

        try {
            RestTemplate restTemplate = new RestTemplate();
            String finalUrl = setting.getRakutenApiUrl() + setting.getRakutenApiDefParam() + param;
            logger.debug("RAKUTEN SEARCH URL1: " + finalUrl);
            String res = restTemplate.getForObject(finalUrl, String.class);

            if (StringUtils.hasText(res)) {
                jsonObject = jsonUtils.createJsonObject(res, teamId);
            }
            serverUtils.sleep();
        } catch (Exception e) {
            if (e instanceof HttpClientErrorException) {
                logger.debug("Too many requests. sleep");
                Thread.sleep(60000);
            } else if (e instanceof HttpServerErrorException) {
                logger.debug("Server error");
                Thread.sleep(60000);
            } else if (e instanceof IllegalArgumentException) {
                logger.debug("IllegalArgumentExceptionなのでhttps->httpにして再リクエストするよ");
                RestTemplate restTemplate = new RestTemplate();
                String finalUrl = setting.getRakutenApiUrl() + setting.getRakutenApiDefParam() + param;
                finalUrl = finalUrl.replaceAll("https", "http");
                logger.debug("RAKUTEN SEARCH URL2: " + finalUrl);

                try {
                    String res = restTemplate.getForObject(finalUrl, String.class);
                    if (StringUtils.hasText(res)) {
                        jsonObject = jsonUtils.createJsonObject(res, teamId);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else {
                logger.debug("想定外のエラーですね");
                e.printStackTrace();
            }
        }
        return jsonObject;
    }

    /**
     * 楽天商品をキーワード検索します。
     * itemCodeだけを取得してきます。
     */
    public List<String> search(List<String> searchList, Long teamId) throws InterruptedException {
        List<String> resultList = new ArrayList<>();

        if (searchList == null || searchList.isEmpty()) {
            return new ArrayList<>();
        }

        for (String key : searchList) {
            if (key == null) {
                continue;
            }

            String parameter = "&keyword=" + key + "&elements=itemCode&hits=5&sort=-updateTimestamp&" + setting.getRakutenAffiliId();
            JSONObject jsonObject = request(parameter, teamId);
            //JSON形式をクラスオブジェクトに変換。クラスオブジェクトの中から必要なものだけを取りだす
            if (jsonObject.has("Items") && JsonUtils.isJsonArray(jsonObject.get("Items"))) {
                JSONArray itemArray = jsonObject.getJSONArray("Items");
                for (int i = 0; i < itemArray.length(); i++) {
                    resultList.add(itemArray.getJSONObject(i).getString("itemCode").replaceAll("^\"|\"$", ""));
                }
            }
        }
        return resultList;
    }

    /**
     * 楽天商品を商品コードから検索、商品詳細を取得します。
     *
     * @param itemCodeList
     * @return
     */
    public List<Item> getDetailsByItemCodeList(List<String> itemCodeList, Long teamId) throws InterruptedException {
        List<Item> resultList = new ArrayList<>();

        for (String key : itemCodeList) {
            String parameter = "&itemCode=" + key + "&elements=itemCaption%2CitemName%2CitemPrice%2CaffiliateUrl&sort=-updateTimestamp&" + setting.getRakutenAffiliId();
            JSONObject jsonObject = request(parameter, teamId);
            //JSON形式をクラスオブジェクトに変換。クラスオブジェクトの中から必要なものだけを取りだす
            if (jsonObject != null && jsonObject.has("Items") && JsonUtils.isJsonArray(jsonObject.get("Items"))) {
                logger.debug("詳細が取得できたのでデータを詰めます");
                JSONArray jsonArray = jsonObject.getJSONArray("Items");
                for (int i=0; i<jsonArray.length();i++) {
                    Item item = new Item();
                    item.setItem_code(key);
                    item.setSite_id(1);
                    item.setPrice(Integer.parseInt(jsonArray.getJSONObject(i).getString("itemPrice")));
                    item.setTitle(jsonArray.getJSONObject(i).getString("itemName").replaceAll("^\"|\"$", ""));
                    item.setItem_caption(StringUtilsMine.compressString(jsonArray.getJSONObject(i).getString("itemCaption").replaceAll("^\"|\"$", ""), 200));
                    item.setUrl(jsonArray.getJSONObject(i).getString("affiliateUrl").replaceAll("^\"|\"$", ""));
                    resultList.add(item);
                }
            }
            logger.debug("詳細データが見つからなかったのでデータを詰めません");
        }
        return resultList;
    }

    /**
     * 楽天商品を商品コードから検索、Map<ItemCode, アフィリURL>を返します
     *
     * @param itemCodeList
     * @return
     */
    public String getUrlByItemCodeList(List<String> itemCodeList, Long teamId) throws InterruptedException {
        String availableUrl = "";

        for (String key : itemCodeList) {
            String parameter = "&itemCode=" + key + "&elements=availability%2CaffiliateUrl" + setting.getRakutenAffiliId();
            JSONObject jsonObject = request(parameter, teamId);
            //JSON形式をクラスオブジェクトに変換。クラスオブジェクトの中から必要なものだけを取りだす
            if (jsonObject != null && jsonObject.has("Items") && JsonUtils.isJsonArray(jsonObject.get("Items"))) {
                logger.debug("詳細が取得できたのでデータを詰めます");
                JSONArray jsonArray = jsonObject.getJSONArray("Items");
                for (int i=0; i<jsonArray.length();i++) {
                    if (Integer.parseInt(jsonArray.getJSONObject(i).getString("availability")) == 1) {
                        availableUrl = jsonArray.getJSONObject(i).getString("affiliateUrl").replaceAll("^\"|\"$", "");
                        break;
                    }
                }
            }
        }
        return availableUrl;
    }
    /**
     * 楽天アフィリンクの更新を行います
     *
     * @return
     */
    public boolean updateUrl() throws InterruptedException {
        // 更新チェックが必要な商品を集める(未来100日以内の商品)
        List<IM> itemMasterList = imService.findBetweenDelFlg(dateUtils.getToday(), dateUtils.daysAfterToday(20), false);
        Map<IM, List<Item>> itemMasterMap = itemMasterList.stream().collect(Collectors.toMap(e -> e, e -> itemService.findByMasterId(e.getIm_id()).stream().filter(f -> iRelService.findByItemId(f.getItem_id()) != null && !iRelService.findByItemId(f.getItem_id()).isEmpty() && !f.isDel_flg() && f.getSite_id().equals(1)).collect(Collectors.toList())));

        // 更新チェックを行う
        List<Item> targetList = new ArrayList<>();
        for (List<Item> itemList : itemMasterMap.values()) {
            List<Item> tmpList = itemList.stream().filter(e -> updateTarget(e.getUrl())).collect(Collectors.toList());
            if (tmpList.size() > 0) {
                targetList.addAll(tmpList);
            }
        }

        List<Item> updateList = new ArrayList<>();
        List<Item> delItemList = new ArrayList<>();
        List<AffeliUrl> affeliUrlList = new ArrayList<>();

        // ターゲットがあれば更新
        for (Item item : targetList) {
            String parameter = "&itemCode=" + item.getItem_code() + "&elements=affiliateUrl&" + setting.getRakutenAffiliId();
            List<IRel> rel = iRelService.findByItemId(item.getItem_id());
            JSONObject jsonObject = request(parameter, rel.get(0).getTeam_id());
            try {
                if (jsonObject.has("Items") && JsonUtils.isJsonArray(jsonObject.get("Items"))) {
                    String affiliateUrl = jsonObject.getJSONArray("Items").getJSONObject(0).getJSONObject("Item").getString("affiliateUrl").replaceAll("^\"|\"$", "");

                    // 新しいアフィリURLを見つけられた場合はアフィリURLテーブルに古いURLを登録したいのでリストに追加しておく
                    affeliUrlList.add(new AffeliUrl(null, item.getIm_id(), item.getUrl(), null, null));

                    item.setUrl(affiliateUrl);
                    updateList.add(item);
                } else if (!jsonObject.has("Items")) {
                    item.setDel_flg(true);
                    delItemList.add(item);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        if (updateList.size() > 0) {
            // 更新リストが0以上の長さの場合、アフィリURLリストも0以上のはずなため、ここで上書かれる古いアフィリURLを登録する
            affeliUrlService.saveAll(affeliUrlList);

            // 完了したら更新した商品を更新する
            itemService.saveAll(updateList);
        }

        // 更新楽天商品アフィリエイトURLが見つからなかった場合、商品のdel_flgをtrueにしたものを更新する
        if (delItemList.size() > 0) {
            itemService.saveAll(delItemList);
        }
        return true;
    }

    /**
     * 楽天アフィ更新のチェックメソッド
     *
     * @param url
     * @return
     */
    public boolean updateTarget(String url) {
        try {
            // URLにアクセスして要素を取ってくる
            Document d = Jsoup.connect(url).get();
            Elements e = d.getElementsByTag("title");
            return e.text().contains("エラー") || d.text().contains("現在ご購入いただけません") || d.text().contains("ページが表示できません");
        } catch (Exception e) {
            logger.debug("*** updateTarget() エラーです " + url + "***");
            logger.debug(e);
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 引数のItemリストから今もavailableな楽天URLを1つ返します
     *
     * @param itemCodeList
     * @return
     */
    public String findAvailableRakutenUrl(List<String> itemCodeList, Long teamId) throws InterruptedException {

        String url = getUrlByItemCodeList(itemCodeList, teamId);
        if (url.equals("")) {

        }
        return url;
    }

    /**
     * 引数の文字列で楽天検索を行いInterCodeを取得。そこからAvailableな楽天URLを取得し返却します
     *
     * @param title
     * @param teamId
     * @return
     * @throws InterruptedException
     */
    public String findRakutenUrl(String title, Long teamId) throws InterruptedException {
        List<String> searchList = new ArrayList<>();
        searchList.add(title);
        List<String> itemCodeList = search(searchList, teamId);
        String url = "";

        if (itemCodeList.size() > 0) {
            url = findAvailableRakutenUrl(itemCodeList, teamId);
        }
        return url;
    }
}

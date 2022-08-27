package otaku.info.controller;

import lombok.AllArgsConstructor;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import otaku.info.entity.IM;
import otaku.info.entity.Item;
import otaku.info.service.AffeliUrlService;
import otaku.info.service.IMService;
import otaku.info.service.ItemService;
import otaku.info.setting.Log4jUtils;
import otaku.info.setting.Setting;
import otaku.info.utils.*;

import java.util.ArrayList;
import java.util.List;

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
                jsonObject = jsonUtils.createJsonObject(res, teamId, null);
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
                        jsonObject = jsonUtils.createJsonObject(res, teamId, null);
                    }
                } catch (Exception ex) {
                    logger.error("restTemplateのエラーです");
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
            // koko
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
            String parameter = "&itemCode=" + key + "&elements=affiliateUrl&" + setting.getRakutenAffiliId();
            JSONObject jsonObject = request(parameter, teamId);
            //JSON形式をクラスオブジェクトに変換。クラスオブジェクトの中から必要なものだけを取りだす
            if (jsonObject != null && jsonObject.has("Items") && JsonUtils.isJsonArray(jsonObject.get("Items"))) {
                logger.debug("詳細が取得できたのでデータを詰めます");
                JSONArray jsonArray = jsonObject.getJSONArray("Items");
                for (int i=0; i<jsonArray.length();i++) {
                    availableUrl = jsonArray.getJSONObject(i).getString("affiliateUrl").replaceAll("^\"|\"$", "");
                    break;
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
        List<IM> imList = imService.findBetweenDelFlg(dateUtils.getToday(), dateUtils.daysAfterToday(30), false);
        List<IM> updateImList = new ArrayList<>();

        for (IM im : imList) {
            Boolean isExpiredUrl = false;
            if (im.getRakuten_url() != null) {
                // IMに楽天URLが設定あるなら、それがリンク切れかチェックする
                isExpiredUrl = isExpiredUrl(im.getRakuten_url());
            } else {
                // IMに楽天URLが設定ないなら、ひもづくItemに有効な楽天URLがあるかチェックする
                List<Item> itemList = itemService.findByImIdSiteId(im.getIm_id(), 1L);

                // itemからimが更新できたか確認するフラグ
                Boolean imUpdated = false;
                for (Item item : itemList) {
                    isExpiredUrl = isExpiredUrl(item.getUrl());

                    if (!isExpiredUrl) {
                        // リンク切れしてないURLが見つかったらそれをIMにセットしてあげる
                        im.setRakuten_url(item.getUrl());
                        updateImList.add(im);
                        imUpdated = true;
                        break;
                    }
                }

                // itemからIMが更新されてない場合、IMで楽天URL検索が必要なのでフラグをセット
                if (!imUpdated) {
                    isExpiredUrl = true;
                }
            }

            // 楽天URLの更新が必要ならばIMの楽天URLを更新する。itemの方は更新しない
            if (isExpiredUrl) {
                String url = getRakutenUrl(im.getTitle());
                if (!url.equals("")) {
                    im.setRakuten_url(url);
                    updateImList.add(im);
                }
            }
        }

        if (updateImList.size() > 0) {
            imService.saveAll(updateImList);
        }

        return true;
    }

    /**
     * 引数URLの楽天アフィリリンクが陸切れではないかチェックする。
     * リンク更新が必要な場合、trueを返却
     *
     * @param url
     * @return
     */
    public boolean isExpiredUrl(String url) {
        try {
            // URLにアクセスして要素を取ってくる
            Document d = Jsoup.connect(url).get();
            Elements e = d.getElementsByTag("title");
            return e.text().contains("エラー") || d.text().contains("現在ご購入いただけません") || d.text().contains("ページが表示できません");
        } catch (HttpStatusException e) {
            return true;
        } catch (Exception e) {
            logger.debug("*** updateTarget() エラーです " + url + "***");
            e.printStackTrace();
            return true;
        }
    }

    /**
     * 引数imidのimに
     *
     * @param imId
     * @return
     * @throws InterruptedException
     */
    public String getRakutenUrl(Long imId) throws InterruptedException {

        if (imId == null) {
            return "";
        }

        IM im = imService.findById(imId);
        if (im.getRakuten_url() != null && !im.getRakuten_url().equals("")) {
            // imの楽天URL
            Boolean updateTarget = isExpiredUrl(im.getRakuten_url());
            if (!updateTarget) {
                return im.getRakuten_url();
            }
        }

        List<String> urlList = itemService.getRakutenUrl(imId);
        for (String url : urlList) {
            Boolean updateTarget = isExpiredUrl(url);
            if (!updateTarget) {
                im.setRakuten_url(url);
                imService.save(im);
                return url;
            }
        }

        // どのアイテムもinvalidだったら、最新のURLを取得しないといけない
//        String itemCode = urlList.get(0).replaceAll("https://hb.afl.rakuten.co.jp/hgc/g00qtaz9.1sojv97f.g00qtaz9.1sojw7e3/?pc=https%3A%2F%2Fitem.rakuten.co.jp%2F", "");
//        itemCode = itemCode.replaceAll("%2F&m=http.*$", "");
//        List<String> codeList = new ArrayList<>();
//        codeList.add(itemCode);
//        String url = getUrlByItemCodeList(codeList, 0L);
//        im.setRakuten_url(url);

        List<String> kwList = new ArrayList<>();
        kwList.add(im.getTitle());
        String tmp = im.getTitle().replaceAll("\\(.*?\\)", "");
        kwList.add(tmp);

        List<String> codeList = search(kwList, 0L);
        String url = getUrlByItemCodeList(codeList, 0L);

        if (!url.equals("")) {
            im.setRakuten_url(url);
            imService.save(im);
        }
        return url;
    }

    /**
     * 引数textで楽天検索→商品のアフィリURLを返します
     *
     * @param text
     * @return
     * @throws InterruptedException
     */
    public String getRakutenUrl(String text) throws InterruptedException {

        List<String> kwList = new ArrayList<>();
        kwList.add(text);
        String tmp = text.replaceAll("\\(.*?\\)", "");
        kwList.add(tmp);

        List<String> codeList = search(kwList, 0L);
        String url = getUrlByItemCodeList(codeList, 0L);

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
            url = getUrlByItemCodeList(itemCodeList, teamId);
        }
        return url;
    }
}

package otaku.info.controller;

import lombok.AllArgsConstructor;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import otaku.info.entity.Item;
import otaku.info.entity.ItemMaster;
import otaku.info.searvice.ItemMasterService;
import otaku.info.searvice.ItemService;
import otaku.info.setting.Setting;
import otaku.info.utils.ItemUtils;
import otaku.info.utils.JsonUtils;
import otaku.info.utils.ServerUtils;
import otaku.info.utils.StringUtilsMine;

import java.util.ArrayList;
import java.util.List;

/**
 * 楽天アフェリエイトへ商品情報を取りに行くコントローラー
 *
 */
@AllArgsConstructor
@Controller
public class RakutenController {

    @Autowired
    ItemUtils itemUtils;

    @Autowired
    ServerUtils serverUtils;

    @Autowired
    Setting setting;

    @Autowired
    ItemMasterService itemMasterService;

    private final ItemService itemService;

    private static org.springframework.util.StringUtils StringUtilsSpring;

    /**
     * 楽天APIにリクエストを投げる
     *
     * @param param Additional params. Will be connected to degault params
     * @return
     */
    public JSONObject request(String param) {
        JSONObject jsonObject = null;

        try {
            RestTemplate restTemplate = new RestTemplate();
            String finalUrl = setting.getRakutenApiUrl() + setting.getRakutenApiDefParam() + param;
            System.out.println("RAKUTEN SEARCH URL: " + finalUrl);
            String res = restTemplate.getForObject(finalUrl, String.class);

            if (StringUtilsSpring.hasText(res)) {
                jsonObject = new JSONObject(res);
            }
            serverUtils.sleep();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    /**
     * 楽天商品をキーワード検索します。
     * itemCodeだけを取得してきます。
     */
    public List<String> search(List<String> searchList) {
        List<String> resultList = new ArrayList<>();

        for (String key : searchList) {
            String parameter = "&keyword=" + key + "&elements=itemCode&hits=5&" + setting.getRakutenAffiliId();
            JSONObject jsonObject = request(parameter);
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
    public List<Item> getDetailsByItemCodeList(List<String> itemCodeList) {
        List<Item> resultList = new ArrayList<>();

        for (String key : itemCodeList) {
            String parameter = "&itemCode=" + key + "&elements=itemCaption%2CitemName%2CitemPrice%2CaffiliateUrl%2CmediumImageUrls&" + setting.getRakutenAffiliId();
            JSONObject jsonObject = request(parameter);
            //JSON形式をクラスオブジェクトに変換。クラスオブジェクトの中から必要なものだけを取りだす
            if (jsonObject.has("Items") && JsonUtils.isJsonArray(jsonObject.get("Items"))) {
                JSONArray jsonArray = jsonObject.getJSONArray("Items");
                for (int i=0; i<jsonArray.length();i++) {
                    Item item = new Item();
                    item.setItem_code(key);
                    item.setSite_id(1);
                    item.setPrice(Integer.parseInt(jsonArray.getJSONObject(i).getString("itemPrice")));
                    item.setTitle(jsonArray.getJSONObject(i).getString("itemName").replaceAll("^\"|\"$", ""));
                    item.setItem_caption(StringUtilsMine.compressString(jsonArray.getJSONObject(i).getString("itemCaption").replaceAll("^\"|\"$", ""), 200));
                    item.setUrl(jsonArray.getJSONObject(i).getString("affiliateUrl").replaceAll("^\"|\"$", ""));
                    JSONArray imageArray = jsonArray.getJSONObject(i).getJSONArray("mediumImageUrls");
                    item = setImages(item, imageArray);
                    resultList.add(item);
                }
            }
        }
        return resultList;
    }

    /**
     * TODO: これがしっかり動いてるか確認。要。
     * 楽天アフィリンクの更新を行います
     *
     * @return
     */
    public boolean updateUrl() {
        // 更新チェックが必要な商品を集める(未来100日以内の商品)
        List<Item> itemList = itemUtils.roundByPublicationDate(itemService.findFutureItemByDate(100));

        // 更新チェックを行う
        List<Item> targetList = new ArrayList<>();
        for (Item item : itemList) {
            if (updateTarget(item.getUrl())) {
                targetList.add(item);
            }
        }

        // 更新が必要であれば更新する
        List<String> searchList = new ArrayList<>();
        targetList.forEach(e -> searchList.add(e.getItem_code()));

        List<Item> updateList = new ArrayList<>();

        for (String key : searchList) {
            String parameter = "&itemCode=" + key + "&elements=affiliateUrl&" + setting.getRakutenAffiliId();
            JSONObject jsonObject = request(parameter);
            try {
                if (JsonUtils.isJsonArray(jsonObject.get("Items"))) {
                    JSONArray itemArray = jsonObject.getJSONArray("Items");

                    if (itemArray == null) {
                        continue;
                    }

                    for (int i=0; i<itemArray.length();i++) {
                        try {
                            Item item = itemService.findByItemCode(key).orElse(new Item());
                            if (item.getItem_code() == null) {
                                continue;
                            }
                            item.setUrl(itemArray.getJSONObject(i).getString("affiliateUrl").replaceAll("^\"|\"$", ""));
                            updateList.add(item);
                        } catch (Exception e) {
                            System.out.println(e.getMessage());
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        itemService.saveAll(updateList);
        return true;
    }

    /**
     * 楽天アフィ更新のチェックメソッド。要
     *
     * @param url
     * @return
     */
    private boolean updateTarget(String url) {
        try {
            // URLにアクセスして要素を取ってくる
            Document d = Jsoup.connect(url).get();
            Elements e = d.getElementsByTag("title");
            boolean test = e.text().contains("エラー");
            return test;
        } catch (Exception e) {
            // TODO: エラー出たらそのこと自体伝えた方がいい
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 楽天商品のImageを検索して既存商品に追加する
     *
     * @param itemMaster
     * @return
     */
    public ItemMaster addImage(ItemMaster itemMaster) {
        List<Item> itemList = itemService.findByMasterId(itemMaster.getItem_m_id());

        for (Item item : itemList) {
            String parameter = "&itemCode=" + item.getItem_code() + "&elements=mediumImageUrls&" + setting.getRakutenAffiliId();
            JSONObject jsonObject = request(parameter);
            if (jsonObject.has("Items") && JsonUtils.isJsonArray(jsonObject.get("Items"))) {
                JSONArray itemArray = jsonObject.getJSONArray("Items");
                if (itemArray.get(0) != null && JsonUtils.isJsonArray(itemArray.getJSONObject(0).get("mediumImageUrls"))) {
                    JSONArray imageArray = itemArray.getJSONObject(0).getJSONArray("mediumImageUrls");
                    if (imageArray.length() > 0) {
                        item = setImages(item, imageArray);
                        itemService.saveItem(item);
                        itemMaster = setImagesItemMaster(itemMaster, imageArray);
                        itemMasterService.save(itemMaster);
                    }
                }

                // もうマスター商品の画像３枚目まで埋まっていたら処理終了
                if (StringUtils.hasText(itemMaster.getImage3())) {
                    break;
                }
            }
        }
        return itemMaster;
    }

    /**
     * ItemとmediumImageUrlsのリストを引数にと李、画像があればItemにセットし、返却する
     * (Serviceで更新は行わない)
     *
     * @param item セットするItem
     * @param imageArray 画像の入ったarray
     * @return
     */
    private Item setImages(Item item, JSONArray imageArray) {
            if (imageArray.length() > 0) {
                boolean hasNext1 = item.fillBlankImage(imageArray.get(0).toString().replaceAll("^\"|\"$", ""));
                if (imageArray.length() > 1 && hasNext1) {
                    boolean hasNext2 = item.fillBlankImage(imageArray.get(1).toString().replaceAll("^\"|\"$", ""));
                    if (imageArray.length() > 2 && hasNext2) {
                        item.fillBlankImage(imageArray.get(2).toString().replaceAll("^\"|\"$", ""));
                    }
                }
            }
        return item;
    }

    /**
     * ItemとmediumImageUrlsのリストを引数にと李、画像があればItemにセットし、返却する
     * (Serviceで更新は行わない)
     * @param itemMaster セットするマスター商品
     * @param imageArray 画像の入ったarray
     * @return
     */
    private ItemMaster setImagesItemMaster(ItemMaster itemMaster, JSONArray imageArray) {
        if (imageArray.length() > 0) {
            boolean hasNext1 = itemMaster.fillBlankImage(imageArray.get(0).toString().replaceAll("^\"|\"$", ""));
            if (imageArray.length() > 1 && hasNext1) {
                boolean hasNext2 = itemMaster.fillBlankImage(imageArray.get(1).toString().replaceAll("^\"|\"$", ""));
                if (imageArray.length() > 2 && hasNext2) {
                    itemMaster.fillBlankImage(imageArray.get(2).toString().replaceAll("^\"|\"$", ""));
                }
            }
        }
        return itemMaster;
    }
}

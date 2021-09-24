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
            sleep();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

//    public List<Item> search1(List<String> searchList) {
//        List<Item> resultList = new ArrayList<>();
//
//        for (String key : searchList) {
//            String parameter = "&itemCode=" + key + "&elements=itemCode%2CitemCaption%2CitemName&" + setting.getRakutenAffiliId();
//            JSONObject node = request(parameter);
//            if (node != null) {
//
//                if (!JsonUtils.isJsonArray(node.getString("Items"))) {
//                    continue;
//                }
//
//                JSONArray items = node.getJSONArray("Items");
//                for (int i=0; i<items.length();i++) {
//                    try {
//                        Item item = new Item();
//                        item.setItem_code(key);
//                        item.setItem_caption(StringUtilsMine.compressString(items.getJSONObject(i).getString("itemCaption").replaceAll("^\"|\"$", ""), 200));
//                        item.setTitle(items.getJSONObject(i).getString("itemName").replaceAll("^\"|\"$", ""));
//                        if (JsonUtils.isJsonArray(items.getJSONObject(i).getString("mediumImageUrls"))) {
//                            JSONArray imageArray = items.getJSONObject(i).getJSONArray("mediumImageUrls");
//                            if (imageArray.length() > 0) {
//                                item.setImage1(imageArray.getJSONObject(0).getString("imageUrl").replaceAll("^\"|\"$", ""));
//                                if (imageArray.length() > 1) {
//                                    item.setImage2(imageArray.getJSONObject(1).getString("imageUrl").replaceAll("^\"|\"$", ""));
//                                }
//                                if (imageArray.length() > 2) {
//                                    item.setImage3(imageArray.getJSONObject(2).getString("imageUrl").replaceAll("^\"|\"$", ""));
//                                }
//                            }
//                            resultList.add(item);
//                        }
//                    } catch (Exception e) {
//                        System.out.println(e.getMessage());
//                    }
//                }
//            }
//        }
//        return resultList;
//    }

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
                    setImages(item, imageArray);
                    resultList.add(item);
                }
            }
        }
        return resultList;
    }

    /**
     * 楽天商品のリストをDBに保存する指示を出します。
     *
     * @param itemList
     * @return
     */
    public List<Item> saveItems(List<Item> itemList) {
        System.out.println("Itemの保存を始めます。リストは以下");
        itemList.forEach(e -> System.out.println(e.getTitle()));
        List<Item> savedList = itemService.saveAll(itemList);
        return savedList;
    }

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
     * Imageを検索して既存商品に追加するtmpメソッド
     *
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
                        setImages(item, imageArray);
                        itemService.saveItem(item);
                        setImagesItemMaster(itemMaster, imageArray);
                        itemMasterService.save(itemMaster);
                    }
                }
                if (StringUtils.hasText(itemMaster.getImage3())) {
                    continue;
                }
            }
        }
        return itemMaster;
    }

    /**
     * ItemとmediumImageUrlsのリストを引数にとる
     *
     * @param item
     * @param imageArray
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

    private void sleep() {
        try{
            Thread.sleep(10000);
        }catch(InterruptedException e){
            e.printStackTrace();
        }
    }
}

package otaku.info.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import otaku.info.entity.Item;
import otaku.info.searvice.ItemService;
import otaku.info.setting.Setting;
import otaku.info.utils.ItemUtils;
import otaku.info.utils.StringUtilsMine;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
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

    private final ItemService itemService;

    private static org.springframework.util.StringUtils StringUtilsSpring;

    /**
     * 楽天APIにリクエストを投げる
     *
     * @param param
     * @return
     */
    public JsonNode request(String param) {
        JsonNode jsonNode = null;
        //0. 外部APIに接続して
        HttpURLConnection conn = null;
        try {
            URL url = new URL(setting.getRakutenApiUrl());
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.connect();
            PrintWriter out = new PrintWriter(conn.getOutputStream());
            out.write(param);
            out.flush();
            out.close();

            //2. Jsonを取得して
            InputStream stream = conn.getInputStream();
            //文字列のバッファを構築
            StringBuffer sb = new StringBuffer();
            String line = "";
            //文字型入力ストリームを作成
            BufferedReader br = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
            //読めなくなるまでwhile文で回す
            while((line = br.readLine()) != null) {
                System.out.println("④受信Json中身：" + line);
                sb.append(line);
            }

            stream.close();
            String script = "";
            if (StringUtils.hasText(sb)) {
                script = sb.toString();
            }

            //3. 解析して中身をとりだします。
            //ObjectMapperオブジェクトの宣言
            ObjectMapper mapper = new ObjectMapper();

            //JSON形式をクラスオブジェクトに変換。クラスオブジェクトの中から必要なものだけを取りだす
            jsonNode = mapper.readTree(script);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonNode;
    }

    public List<Item> search1(List<String> searchList) {
        List<Item> resultList = new ArrayList<>();

        for (String key : searchList) {
            String parameter = setting.getRakutenApiDefParam() + "&itemCode=" + key + "&elements=itemCode%2CitemCaption%2CitemName&" + setting.getRakutenAffiliId();
            JsonNode node = request(parameter);
            if (node != null) {
                node = node.get("Items");
                for (int i=0; i<node.size();i++) {
                    try {
                        Item item = new Item();
                        item.setItem_code(key);
                        item.setItem_caption(StringUtilsMine.compressString(node.get(i).get("itemCaption").toString().replaceAll("^\"|\"$", ""), 200));
                        item.setTitle(node.get(i).get("itemName").toString().replaceAll("^\"|\"$", ""));
                        JsonNode imageNode = node.get(i).get("mediumImageUrls");
                        if (imageNode.size() > 0) {
                            item.setImage1(imageNode.get(0).get("imageUrl").toString().replaceAll("^\"|\"$", ""));
                            if (imageNode.size() > 1) {
                                item.setImage2(imageNode.get(1).get("imageUrl").toString().replaceAll("^\"|\"$", ""));
                            }
                            if (imageNode.size() > 2) {
                                item.setImage3(imageNode.get(2).get("imageUrl").toString().replaceAll("^\"|\"$", ""));
                            }
                        }
                        resultList.add(item);
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                }
            }
            try{
                Thread.sleep(1000);
            }catch(InterruptedException e){
                e.printStackTrace();
            }
        }
        return resultList;
    }
    /**
     * 楽天商品をキーワード検索します。
     * itemCodeだけを取得してきます。
     */
    public List<String> search(List<String> searchList) {
        List<String> resultList = new ArrayList<>();

        for (String key : searchList) {
            String parameter = setting.getRakutenApiDefParam() + "&keyword=" + key + "&elements=itemCode&hits=5&" + setting.getRakutenAffiliId();
            JsonNode node = request(parameter);
            //JSON形式をクラスオブジェクトに変換。クラスオブジェクトの中から必要なものだけを取りだす
            if (node.get("Items") != null) {
                node = node.get("Items");
                for (int i=0; i<node.size();i++) {
                    resultList.add(node.get(i).get("itemCode").toString().replaceAll("^\"|\"$", ""));
                }
            }
            try{
                Thread.sleep(1000);
            }catch(InterruptedException e){
                e.printStackTrace();
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
            String parameter = setting.getRakutenApiDefParam() + "&itemCode=" + key + "&elements=itemCaption%2CitemName%2CitemPrice%2CaffiliateUrl%2CmediumImageUrls&" + setting.getRakutenAffiliId();
            JsonNode node = request(parameter);
            //JSON形式をクラスオブジェクトに変換。クラスオブジェクトの中から必要なものだけを取りだす
            node = node.get("Items");
            for (int i=0; i<node.size();i++) {
                Item item = new Item();
                item.setItem_code(key);
                item.setSite_id(1);
                item.setPrice(Integer.parseInt(node.get(i).get("itemPrice").toString()));
                item.setTitle(node.get(i).get("itemName").toString().replaceAll("^\"|\"$", ""));
                item.setItem_caption(StringUtilsMine.compressString(node.get(i).get("itemCaption").toString().replaceAll("^\"|\"$", ""), 200));
                item.setUrl(node.get(i).get("affiliateUrl").toString().replaceAll("^\"|\"$", ""));
                JsonNode imageNode = node.get(i).get("mediumImageUrls");
                if (imageNode.size() > 0) {
                    if (imageNode.get(0).get("imageUrl") == null) {
                        item.setImage1(imageNode.get(0).toString().replaceAll("^\"|\"$", ""));
                    } else {
                        item.setImage1(imageNode.get(0).get("imageUrl").toString().replaceAll("^\"|\"$", ""));
                        if (imageNode.size() > 1) {
                            item.setImage2(imageNode.get(1).get("imageUrl").toString().replaceAll("^\"|\"$", ""));
                        }
                        if (imageNode.size() > 2) {
                            item.setImage3(imageNode.get(2).get("imageUrl").toString().replaceAll("^\"|\"$", ""));
                        }
                    }
                }
                resultList.add(item);
            }
            try{
                Thread.sleep(1000);
            }catch(InterruptedException e){
                e.printStackTrace();
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
            String parameter = setting.getRakutenApiDefParam() + "&itemCode=" + key + "&elements=affiliateUrl&" + setting.getRakutenAffiliId();
            JsonNode node = request(parameter);
            if (node != null) {
                node = node.get("Items");

                if (node == null) {
                    continue;
                }

                for (int i=0; i<node.size();i++) {
                    try {
                        Item item = itemService.findByItemCode(key).orElse(new Item());
                        if (item.getItem_code() == null) {
                            continue;
                        }
                        item.setUrl(node.get(i).get("affiliateUrl").toString().replaceAll("^\"|\"$", ""));
                        updateList.add(item);
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                }
                try{
                    Thread.sleep(1000);
                }catch(InterruptedException e) {
                    e.printStackTrace();
                }
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
     * Imageを既存商品に追加するtmpメソッド
     *
     */
    public void addImage() {
        List<Item> itemList = itemService.findByDelFlg(false);

        for (Item item : itemList) {
            String parameter = setting.getRakutenApiDefParam() + "&itemCode=" + item.getItem_code() + "&elements=mediumImageUrls&" + setting.getRakutenAffiliId();
            JsonNode node = request(parameter);
            node = node.get("Items");
            if (!node.isNull()) {
                for (int i=0; i<node.size();i++) {
                    JsonNode imageNode = node.get(i).get("mediumImageUrls");
                    if (imageNode.size() > 0) {
                        item.setImage1(imageNode.get(0).toString().replaceAll("^\"|\"$", ""));

                        if (imageNode.size() > 1) {
                            item.setImage2(imageNode.get(1).toString().replaceAll("^\"|\"$", ""));
                        }

                        if (imageNode.size() > 2) {
                            item.setImage3(imageNode.get(2).toString().replaceAll("^\"|\"$", ""));
                        }
                    }
                }
                itemService.saveItem(item);
            }
            try{
                Thread.sleep(10000);
            }catch(InterruptedException e){
                e.printStackTrace();
            }
        }
    }
}

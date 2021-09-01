package otaku.info.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import otaku.info.entity.Item;
import otaku.info.searvice.ItemService;
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

    static final String RAKUTEN_URL = "https://app.rakuten.co.jp/services/api/IchibaItem/Search/20170706?";

    private final ItemService itemService;

    public static List<Item> search1(List<String> searchList) {
        List<Item> resultList = new ArrayList<>();

        //0. 外部APIに接続して
        HttpURLConnection conn = null;
        try {
            URL url = new URL(RAKUTEN_URL);
            for (String key : searchList) {
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoOutput(true);
                conn.connect();
                PrintWriter out = new PrintWriter(conn.getOutputStream());
                String parameter = "format=json&itemCode=" + key + "&elements=itemCode%2CitemCaption%2CitemName&formatVersion=2&carrier=0&affiliateId=209dd04b.157fa2f2.209dd04c.c65acd6f&applicationId=1074359606109126276";
                out.write(parameter);
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
//                stream.close();
                String script = sb.toString();

                //3. 解析して中身をとりだします。
                //ObjectMapperオブジェクトの宣言
                ObjectMapper mapper = new ObjectMapper();

                //JSON形式をクラスオブジェクトに変換。クラスオブジェクトの中から必要なものだけを取りだす
                JsonNode node = mapper.readTree(script);
                if (node != null) {
                    node = node.get("Items");
                    for (int i=0; i<node.size();i++) {
                        try {
                            Item item = new Item();
                            item.setItem_code(key);
                            item.setItem_caption(StringUtilsMine.compressString(node.get(i).get("itemCaption").toString().replaceAll("^\"|\"$", ""), 200));
                            item.setTitle(node.get(i).get("itemName").toString().replaceAll("^\"|\"$", ""));
                            resultList.add(item);
                        } catch (Exception e) {
                            System.out.println(e.getMessage());
                        }
                    }
                    try{
                        Thread.sleep(1000);
                    }catch(InterruptedException e){
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultList;
    }
    /**
     * 楽天商品をキーワード検索します。
     * itemCodeだけを取得してきます。
     */
    public static List<String> search(List<String> searchList) {
        List<String> resultList = new ArrayList<>();

        //0. 外部APIに接続して
        HttpURLConnection conn = null;
        try {
            URL url = new URL(RAKUTEN_URL);
            for (String key : searchList) {
                System.out.println("検索ワード：" + key);
                //1. パラメーターを送って
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoOutput(true);
                conn.connect();
                PrintWriter out = new PrintWriter(conn.getOutputStream());
                String parameter = "format=json&keyword=" + key + "&availability=1&elements=itemCode&hits=5&formatVersion=2&carrier=0&NGKeyword=%E4%B8%AD%E5%8F%A4%20USED&affiliateId=209dd04b.157fa2f2.209dd04c.c65acd6f&applicationId=1074359606109126276";
                System.out.println("パラメタ：" + parameter);
                out.write(parameter);
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
                    sb.append(line);
                }
//                stream.close();
                String script = sb.toString();

                //3. 解析して中身をとりだします。
                //ObjectMapperオブジェクトの宣言
                ObjectMapper mapper = new ObjectMapper();

                //JSON形式をクラスオブジェクトに変換。クラスオブジェクトの中から必要なものだけを取りだす
                if (mapper.readTree(script).get("Items") != null) {
                    JsonNode node = mapper.readTree(script).get("Items");
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
        } catch (Exception e) {
            e.printStackTrace();
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

        //0. 外部APIに接続して
        HttpURLConnection conn = null;
        try {
            URL url = new URL(RAKUTEN_URL);
            for (String key : itemCodeList) {
                System.out.println("検索ワード：" + key);
                //1. パラメーターを送って
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoOutput(true);
                conn.connect();
                PrintWriter out = new PrintWriter(conn.getOutputStream());
                String parameter = "format=json&itemCode=" + key + "&availability=1&elements=itemCaption%2CitemName%2CitemPrice%2CaffiliateUrl%2CmediumImageUrls&formatVersion=2&carrier=0&NGKeyword=%E4%B8%AD%E5%8F%A4%20USED&affiliateId=209dd04b.157fa2f2.209dd04c.c65acd6f&applicationId=1074359606109126276";
                System.out.println("パラメタ：" + parameter);
                out.write(parameter);
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
                    System.out.println("受信Json：" + line);
                    sb.append(line);
                }
//                stream.close();
                String script = sb.toString();

                //3. 解析して中身をとりだします。
                //ObjectMapperオブジェクトの宣言
                ObjectMapper mapper = new ObjectMapper();

                //JSON形式をクラスオブジェクトに変換。クラスオブジェクトの中から必要なものだけを取りだす
                JsonNode node = mapper.readTree(script).get("Items");
                for (int i=0; i<node.size();i++) {
                    Item item = new Item();
                    item.setItem_code(key);
                    item.setSite_id(1);
                    item.setPrice(Integer.parseInt(node.get(i).get("itemPrice").toString()));
                    item.setTitle(node.get(i).get("itemName").toString().replaceAll("^\"|\"$", ""));
                    item.setItem_caption(StringUtilsMine.compressString(node.get(i).get("itemCaption").toString().replaceAll("^\"|\"$", ""), 200));
                    item.setUrl(node.get(i).get("affiliateUrl").toString().replaceAll("^\"|\"$", ""));
                    resultList.add(item);
                }
                try{
                    Thread.sleep(1000);
                }catch(InterruptedException e){
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
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

        //0. 外部APIに接続して
        HttpURLConnection conn = null;
        try {
            URL url = new URL(RAKUTEN_URL);
            for (String key : searchList) {
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoOutput(true);
                conn.connect();
                PrintWriter out = new PrintWriter(conn.getOutputStream());
                String parameter = "format=json&itemCode=" + key + "&elements=affiliateUrl&formatVersion=2&carrier=0&affiliateId=209dd04b.157fa2f2.209dd04c.c65acd6f&applicationId=1074359606109126276";
                out.write(parameter);
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
    //          stream.close();
                String script = sb.toString();

                //3. 解析して中身をとりだします。
                //ObjectMapperオブジェクトの宣言
                ObjectMapper mapper = new ObjectMapper();

                //JSON形式をクラスオブジェクトに変換。クラスオブジェクトの中から必要なものだけを取りだす
                JsonNode node = mapper.readTree(script);
                if (node != null) {
                    node = node.get("Items");
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        itemService.updateAll(updateList);
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
}

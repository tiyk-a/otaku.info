package otaku.info.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import otaku.info.entity.Item;
import otaku.info.searvice.db.ItemService;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Controller
public class RakutenController {

    static final String RAKUTEN_URL = "https://app.rakuten.co.jp/services/api/IchibaItem/Search/20170706?";

    private final ItemService itemService;

    public static List<Item> search1(List<String> searchList) {
        List<Item> resultList = new ArrayList<>();

        //0. 外部APIに接続して
        HttpURLConnection conn = null;
        System.out.println("①楽天APIに接続");
        try {
            URL url = new URL(RAKUTEN_URL);
            for (String key : searchList) {
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoOutput(true);
                conn.connect();
                PrintWriter out = new PrintWriter(conn.getOutputStream());
                String parameter = "format=json&itemCode=" + key + "&elements=itemCaption&formatVersion=2&carrier=0&affiliateId=209dd04b.157fa2f2.209dd04c.c65acd6f&applicationId=1074359606109126276";
                System.out.println("③パラメタ：" + parameter);
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
                System.out.println("⑤Json中身string：" + script);

                //3. 解析して中身をとりだします。
                //ObjectMapperオブジェクトの宣言
                ObjectMapper mapper = new ObjectMapper();

                //JSON形式をクラスオブジェクトに変換。クラスオブジェクトの中から必要なものだけを取りだす
                JsonNode node = mapper.readTree(script).get("Items");
                for (int i=0; i<node.size();i++) {
                    Item item = new Item();
                    item.setItem_code(key);
                    item.setItem_caption(node.get(i).get("itemCaption").toString().replaceAll("^\"|\"$", ""));
                    resultList.add(item);
                }
                try{
                    Thread.sleep(10000);
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
     * 楽天商品を検索します。
     */
    public static List<Item> search(List<String> searchList) {
        List<Item> resultList = new ArrayList<>();

        //0. 外部APIに接続して
        HttpURLConnection conn = null;
        System.out.println("①楽天APIに接続");
        try {
            URL url = new URL(RAKUTEN_URL);
            for (String key : searchList) {
                System.out.println("②検索ワード：" + key);
                //1. パラメーターを送って
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoOutput(true);
                conn.connect();
                PrintWriter out = new PrintWriter(conn.getOutputStream());
                String parameter = "format=json&keyword=" + key + "&availability=1&elements=itemCaption%2CitemCode%2CitemName%2CitemPrice%2CaffiliateUrl%2CmediumImageUrls&hits=5&formatVersion=2&carrier=0&NGKeyword=%E4%B8%AD%E5%8F%A4%20USED&affiliateId=209dd04b.157fa2f2.209dd04c.c65acd6f&applicationId=1074359606109126276";
                System.out.println("③パラメタ：" + parameter);
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
                System.out.println("⑤Json中身string：" + script);

                //3. 解析して中身をとりだします。
                //ObjectMapperオブジェクトの宣言
                ObjectMapper mapper = new ObjectMapper();

                //JSON形式をクラスオブジェクトに変換。クラスオブジェクトの中から必要なものだけを取りだす
                JsonNode node = mapper.readTree(script).get("Items");
                for (int i=0; i<node.size();i++) {
                    Item item = new Item();
                    item.setItem_code(node.get(i).get("itemCode").toString().replaceAll("^\"|\"$", ""));
                    item.setSite_id(1);
                    item.setPrice(Integer.parseInt(node.get(i).get("itemPrice").toString()));
                    item.setTitle(node.get(i).get("itemName").toString().replaceAll("^\"|\"$", ""));
                    item.setItem_caption(node.get(i).get("itemCaption").toString().replaceAll("^\"|\"$", ""));
                    item.setUrl(node.get(i).get("affiliateUrl").toString().replaceAll("^\"|\"$", ""));
                    resultList.add(item);
                }
                try{
                    Thread.sleep(10000);
                }catch(InterruptedException e){
                    e.printStackTrace();
                }
                }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultList;
    }

    public List<Item> saveItems(List<Item> itemList) {
        System.out.println("Itemの保存を始めます。リストは以下");
        List<Item> savedItemList = new ArrayList<>();
        for (Item item : itemList) {
//            if (!itemService.hasData(item.getItem_code())){
                Item saveItem = itemService.saveItem(item);
                System.out.println("保存しました：" + saveItem.getTitle());
                savedItemList.add(saveItem);
//            }
        }
        return savedItemList;
    }
}

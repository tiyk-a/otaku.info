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

    /**
     * 楽天商品を検索します。
     */
    public static List<Item> search(List<String> searchList) {
        List<Item> resultList = new ArrayList<>();

        //0. 外部APIに接続して
        HttpURLConnection conn = null;
        try {
            URL url = new URL(RAKUTEN_URL);
            for (String key : searchList) {
                System.out.println(key);
                //1. パラメーターを送って
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoOutput(true);
                conn.connect();
                PrintWriter out = new PrintWriter(conn.getOutputStream());
                String parameter = "format=json&keyword=" + key + "&hits=5&affiliateId=209dd04b.157fa2f2.209dd04c.c65acd6f&applicationId=1074359606109126276";
                out.write(parameter);
                out.flush();
                out.close();

                //2. Jsonを取得して
                InputStream stream = conn.getInputStream();
                //文字列のバッファを構築
                StringBuffer sb = new StringBuffer();
                String line = "";
                //文字型入力ストリームを作成
                BufferedReader br = new BufferedReader(new InputStreamReader(stream));
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
                JsonNode node = mapper.readTree(script).get("Items");
                for (int i=0; i<node.size();i++) {
                    System.out.println(node.get(i).get("Item").get("itemName").toString());
                    Item item = new Item();
                    item.setItem_code(node.get(i).get("Item").get("itemCode").toString());
                    item.setSite_id(1);
                    item.setTeam_id(1);
                    item.setPrice(Integer.parseInt(node.get(i).get("Item").get("itemPrice").toString()));
                    item.setTitle(node.get(i).get("Item").get("itemName").toString());
                    item.setUrl(node.get(i).get("Item").get("affiliateUrl").toString());
                    resultList.add(item);
                }
                }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultList;
    }

    public boolean saveItem() {
        Item item = new Item();
        item.setSite_id(1);
        item.setTeam_id(1);
        item.setItem_code("Dummy2");
        itemService.saveItem(item);
        return true;
    }

    public void saveItems(List<Item> itemList) {
        for (Item item : itemList) {
            if (!itemService.hasData(item.getItem_code())){
                itemService.saveItem(item);
            }
        }
    }
}

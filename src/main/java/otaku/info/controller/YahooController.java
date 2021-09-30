package otaku.info.controller;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import otaku.info.entity.Item;
import otaku.info.searvice.ItemService;
import otaku.info.setting.Setting;
import otaku.info.utils.DateUtils;
import otaku.info.utils.JsonUtils;
import otaku.info.utils.ServerUtils;

import javax.servlet.http.HttpServletResponse;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class YahooController {

    @Autowired
    AnalyzeController analyzeController;

    @Autowired
    ItemService itemService;

    @Autowired
    Setting setting;

    @Autowired
    ServerUtils serverUtils;

    HttpServletResponse response;

    /**
     * リクエストを送る
     *
     * @param url
     * @param request
     * @param method
     * @return
     */
    public String request(String url, HttpEntity<String> request, HttpMethod method) {

//        response.setHeader("Cache-Control", "no-cache");

        try {
            RestTemplate restTemplate = new RestTemplate();
            System.out.println("YAHOO SEARCH URL: " + url);
            ResponseEntity<String> responseEntity = restTemplate.exchange(url, method, request, String.class);

            if (responseEntity.getStatusCode().equals(HttpStatus.FORBIDDEN)) {
                return "";
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return responseEntity.getBody();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * Yahoo商品をキーワード検索します。
     */
    public List<Item> search(List<String> searchList) throws ParseException {
        List<Item> itemList = new ArrayList<>();

        for (String key : searchList) {
            String parameter = "&query=" + key + "&results=10";
            JSONObject jo = new JSONObject();
            HttpHeaders headers = new HttpHeaders();
            HttpEntity<String> request = new HttpEntity<>(jo.toString(), headers);
            JSONObject jsonObject = new JSONObject(request(parameter, request, HttpMethod.GET));
            //JSON形式をクラスオブジェクトに変換。クラスオブジェクトの中から必要なものだけを取りだす
            if (jsonObject.has("hits") && JsonUtils.isJsonArray(jsonObject.get("hits"))) {
                JSONArray itemArray = jsonObject.getJSONArray("hits");

                if (itemArray.length() > 0) {
                    for (int i = 0; i < itemArray.length(); i++) {
                        JSONObject jsonObject1 = itemArray.getJSONObject(i);

                        if (jsonObject1.has("code")) {
                            // 商品コードより、既存商品は新規登録対象から弾く
                            String code = jsonObject1.getString("code").replaceAll("^\"|\"$", "");
                            boolean isRegistered = itemService.isRegistered(code, 2);

                            if (!isRegistered) {
                                Item item = new Item();
                                item.setTitle(jsonObject1.getString("name").replaceAll("^\"|\"$", ""));
                                item.setItem_caption(jsonObject1.getString("description").replaceAll("^\"|\"$", ""));
                                item.setImage1(jsonObject1.getJSONObject("image").getString("medium").replaceAll("^\"|\"$", ""));
                                item.setPrice(jsonObject1.getInt("price"));
                                item.setItem_code(jsonObject1.getString("code").replaceAll("^\"|\"$", ""));
                                if (jsonObject1.has("releaseDate") && !jsonObject1.isNull("releaseDate")) {
                                    item.setPublication_date(DateUtils.unixToDate((long) jsonObject1.getInt("releaseDate")));
                                } else {
                                    item.setPublication_date(analyzeController.generatePublicationDate(item));
                                }
                                // TODO:アフィリリンクではないのでアフィリにしなくては！
                                item.setUrl(jsonObject1.getString("url"));
                                item.setSite_id(2);
                                itemList.add(item);
                            }
                        }
                    }
                }
            }
        }
        return itemList;
    }

    /**
     * リクエストを投げて文字列からキーワードを抽出します。
     * https://developer.yahoo.co.jp/webapi/jlp/keyphrase/v2/extract.html
     *
     * @param target
     * @return
     */
    public List<String> extractKeywords(String target) {
        List<String> result = null;

        if (StringUtils.hasText(target)) {
            JSONObject jo = new JSONObject();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            jo.put("id", "1234-1");
            jo.put("jsonrpc", "2.0");
            jo.put("method", "jlp.keyphraseservice.extract");
            Map params = new HashMap();
            params.put("q", target);
            jo.put("params", params);
            HttpEntity<String> request = new HttpEntity<>(jo.toString(), headers);
            String url = setting.getYahooPhraseApi();
            String res = request(url, request, HttpMethod.POST);
            if (!StringUtils.hasText(res)) {
                return null;
            }
            JSONObject jsonObject = new JSONObject(res);
            if (jsonObject.has("result") && jsonObject.getJSONObject("result").has("phrases")) {
                JSONArray jsonArray = jsonObject.getJSONObject("result").getJSONArray("phrases");
                if(jsonArray.length() > 0) {
                    // Map<text, score>
                    Map<String, Integer> textScoreMap = new HashMap<>();
                    for (int i=0; i<jsonArray.length(); i++) {
                        JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                        textScoreMap.put(jsonObject1.getString("text"), jsonObject1.getInt("score"));
                    }

                    if (textScoreMap.entrySet().size() > 0) {
                        // Mapをスコア(value)でdescに並び替え、keyとなっているワードだけをリストに詰める
                        result = textScoreMap.entrySet().stream().sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                                .map(Map.Entry::getKey)
                                .collect(Collectors.toList());
                    }
                }
            }
        }
        return result;
    }
}

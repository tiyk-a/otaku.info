package otaku.info.controller;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;
import otaku.info.dto.WpDto;
import otaku.info.entity.Item;
import otaku.info.searvice.ItemService;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("blog")
@AllArgsConstructor
public class BlogController {

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    TextController textController;

    @Autowired
    ItemService itemService;

    HttpServletResponse response;

    final String URL = "https://otakuinfo.fun/wp-json/wp/v2/";

    /**
     * 近日販売商品のブログページを更新します。
     * ・本日販売
     * ・明日以降1週間の商品
     * 上記商品で画面を書き換える。
     */
    public String updateReleaseItems(){

        // 商品を集めるため今日の日付を取得
        Date today = DateUtils.truncate(new Date(), Calendar.DAY_OF_MONTH);
        Calendar c = Calendar.getInstance();
        c.setTime(today);
        c.add(Calendar.HOUR, 24);
        Date to = c.getTime();

        // 今日発売商品
        List<Item> releaseItemList = itemService.findItemsBetweenDelFlg(today, to, false);

        // チームIDが空の場合は再チェックリストに追加
        List<Item> reCheckItemList = releaseItemList.stream().filter(e -> StringUtils.isEmpty(e.getTeam_id())).collect(Collectors.toList());
        // そして投稿する商品リストから削除
        releaseItemList.removeAll(reCheckItemList);

        // 明日~1週間以内の発売商品
        c.setTime(today);
        c.add(Calendar.DATE, 7);

        Date sevenDaysLater = c.getTime();
        List<Item> futureReleaseItemList = itemService.findItemsBetweenDelFlg(today, sevenDaysLater, false);

        // チームIDが空の場合、tmpリストに追加
        List<Item> tmpList = futureReleaseItemList.stream().filter(e -> StringUtils.isEmpty(e.getTeam_id())).collect(Collectors.toList());
        // そして投稿する商品リストから削除
        futureReleaseItemList.removeAll(tmpList);
        // そして際チェックリストに追加
        reCheckItemList.addAll(tmpList);

        // 商品の収集はここで完了。再チェックが必要な商品は再チェックするようにdel_flgをfalseに戻して更新する
        reCheckItemList.forEach(e -> e.setFct_chk(false));
        itemService.saveAll(reCheckItemList);

        // テキストを生成
        String blogText = textController.blogUpdateReleaseItems(releaseItemList, futureReleaseItemList);

        WpDto wpDto = new WpDto();
        wpDto.setPath("pages/33");
        wpDto.setContent(blogText);

        // リクエスト送信
        request(response, wpDto);
        return "ok";
    }

    private String request(HttpServletResponse response, WpDto wpDto) {
        String finalUrl = URL + wpDto.getPath();

        response.setHeader("Cache-Control", "no-cache");
        HttpHeaders headers = new HttpHeaders();
        headers.add("Accept", MediaType.APPLICATION_JSON_VALUE);
        headers.setContentType(MediaType.APPLICATION_JSON);
        String auth = new String(
            Base64.getEncoder().encode(
                "hayainfo:j2Uz s3Ko YiCx Rbsg SFnQ TFeV".getBytes()
            )
        );
        headers.add("Authorization","Basic " +  auth);
        JSONObject personJsonObject = new JSONObject();

        if (!StringUtils.isEmpty(wpDto.getTitle())) {
            personJsonObject.put("title",wpDto.getTitle());
        }

        personJsonObject.put("author",1);
        personJsonObject.put("categories",wpDto.getCategories());
        personJsonObject.put("tags",wpDto.getTags());

        if (!StringUtils.isEmpty(wpDto.getContent())) {
            personJsonObject.put("content",wpDto.getContent());
        }

        if (!StringUtils.isEmpty(wpDto.getExcerpt())) {
            personJsonObject.put("excerpt",wpDto.getExcerpt());
        }

        personJsonObject.put("status","publish");

        HttpEntity<String> request = new HttpEntity<>(personJsonObject.toString(), headers);
        ResponseEntity<String> responseEntity = restTemplate.exchange(finalUrl, HttpMethod.POST, request, String.class);
        return responseEntity.getBody();
    }

    /**
     * 楽天検索で見つけた新商品についての記事を投稿する。
     *
     * @param item
     */
    public void postNewItem(Item item) {
        WpDto wpDto = item.convertToWpDto();

        if (wpDto != null) {
            // テキストコントローラーで文章をa href付きのものに変更
            WpDto tmpDto = textController.blogItemText(item);
            if (tmpDto != null) {
                wpDto.setTitle(tmpDto.getTitle());
                wpDto.setContent(tmpDto.getContent());
                wpDto.setPath("posts");
                wpDto.setCategories(new Integer[]{5});

                // リクエスト送信
                String res = request(response, wpDto);
                // うまくポストが完了してStringが返却されたらwpIdをitemに登録する
                if (org.springframework.util.StringUtils.hasText(res)) {
                    JSONObject jsonObject = new JSONObject(res);
                    if (jsonObject.get("id") != null) {
                        item.setWp_id(Integer.parseInt(jsonObject.get("id").toString().replaceAll("^\"|\"$", "")));
                        itemService.saveItem(item);
                    }
                }
            }
        }
    }

    /**
     * 画像をWordPressにポストします。
     *
     * @param response
     * @param imageUrl
     * @return 画像ID
     */
    public Integer requestMedia(HttpServletResponse response, Long itemId, String imageUrl) throws IOException {
        String finalUrl = URL + "media";

        response.setHeader("Cache-Control", "no-cache");
        HttpHeaders headers = new HttpHeaders();
        headers.add("content-disposition", "attachment; filename=tmp1.jpg");
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        imageUrl = imageUrl.replaceAll("\\?.*$", "");

        String imagePath = "";
        try(InputStream in = new URL(imageUrl).openStream()){
            imagePath = "/Users/chiara/Desktop/info/src/main/resources/images/item/" + itemId + ".jpg";
            Files.copy(in, Paths.get(imagePath));
        } catch (Exception e) {
            e.printStackTrace();
        }

        body.add("file", new FileSystemResource(imagePath));
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        String auth = new String(
            Base64.getEncoder().encode(
                "hayainfo:j2Uz s3Ko YiCx Rbsg SFnQ TFeV".getBytes()
            )
        );
        headers.add("Authorization","Basic " +  auth);
        JSONObject personJsonObject = new JSONObject();

        ResponseEntity<String> responseEntity = restTemplate.postForEntity(finalUrl, requestEntity, String.class);
        String text = responseEntity.getBody();
        JSONObject jsonObject = new JSONObject(text);
        if (jsonObject.get("id") != null) {
            return Integer.parseInt(jsonObject.get("id").toString().replaceAll("^\"|\"$", ""));
        }
        return 0;
    }

    /**
     * WpIdからポストの内容を取得します。
     *
     * @param wpId
     * @return
     */
    public String requestPostData(String wpId) {
        String finalUrl = URL + "posts/" + wpId;

        HttpHeaders headers = new HttpHeaders();
        String auth = new String(
            Base64.getEncoder().encode(
                "hayainfo:j2Uz s3Ko YiCx Rbsg SFnQ TFeV".getBytes()
            )
        );
        headers.add("Authorization","Basic " +  auth);

        ResponseEntity<String> responseEntity = restTemplate.exchange(finalUrl, HttpMethod.GET, new HttpEntity<>(headers), String.class);
        return responseEntity.getBody();
    }

    /**
     * Tmpブログ新商品投稿メソッド
     */
    public void tmpItemPost() {
        List<Item> itemList = itemService.tmpMethod1();
        for (Item item : itemList) {
            postNewItem(item);
        }
    }

    /**
     * 商品リストからアイキャッチメディアの登録がない商品だけを引き抜いてリストにし返却します。
     *
     * @param itemList
     * @return Item:
     */
    public List<Item> selectBlogData(List<Item> itemList) {
        List<Item> resultList = new ArrayList<>();
        for (Item item : itemList) {
            String result = requestPostData(item.getWp_id().toString());
            Integer featuredMedia = extractMedia(result);
            if (featuredMedia == 0) {
                resultList.add(item);
            }
        }
        return resultList;
    }

    /**
     * アイキャッチメディアがある場合、画像IDを返却します。
     * ない場合、0
     *
     * @param text
     * @return
     */
    private Integer extractMedia(String text) {
        JSONObject jsonObject = new JSONObject(text);
        if (jsonObject.get("featured_media") != null) {
            return Integer.parseInt(jsonObject.get("featured_media").toString().replaceAll("^\"|\"$", ""));
        }
        return 0;
    }

    /**
     * 商品画像1をWordpressに登録します。
     *
     * @param itemList
     */
    public void loadMedia(List<Item> itemList) throws IOException {
        for (Item item : itemList) {
            Integer imageId = requestMedia(response, item.getItem_id(), item.getImage1());
            if (imageId == null || imageId == 0) {
                continue;
            }

            setMedia(item.getWp_id(), imageId);
        }
    }

    /**
     * 投稿にアイキャッチメディアを設定し、更新します。
     *
     * @param wpId
     * @param imageId
     */
    private void setMedia(Integer wpId, Integer imageId) {
        String finalUrl = URL + "item/" + wpId;

        response.setHeader("Cache-Control", "no-cache");
        HttpHeaders headers = new HttpHeaders();
        headers.add("Accept", MediaType.APPLICATION_JSON_VALUE);
        headers.setContentType(MediaType.APPLICATION_JSON);
        String auth = new String(
            Base64.getEncoder().encode(
                "hayainfo:j2Uz s3Ko YiCx Rbsg SFnQ TFeV".getBytes()
            )
        );

        headers.add("Authorization","Basic " +  auth);
        JSONObject personJsonObject = new JSONObject();
        personJsonObject.put("featured_media", imageId);

        HttpEntity<String> request = new HttpEntity<>(personJsonObject.toString(), headers);
        ResponseEntity<String> responseEntity = restTemplate.exchange(finalUrl, HttpMethod.POST, request, String.class);
    }
}

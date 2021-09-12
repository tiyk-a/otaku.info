package otaku.info.controller;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.time.DateUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;
import otaku.info.entity.BlogTag;
import otaku.info.entity.Item;
import otaku.info.entity.ItemMaster;
import otaku.info.entity.Program;
import otaku.info.searvice.BlogTagService;
import otaku.info.searvice.ItemMasterService;
import otaku.info.searvice.ItemService;
import otaku.info.searvice.ProgramService;
import otaku.info.setting.Setting;
import otaku.info.utils.ItemUtils;

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

    @Autowired
    ProgramService programService;

    @Autowired
    ItemMasterService itemMasterService;

    @Autowired
    BlogTagService blogTagService;

    @Autowired
    ItemUtils itemUtils;

    @Autowired
    otaku.info.utils.DateUtils dateUtils;

    @Autowired
    Setting setting;

    private static org.springframework.util.StringUtils StringUtilsSpring;

    HttpServletResponse response;

    /**
     * 近日販売商品のブログページ(固定)を更新します。
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

        // 今日発売マスター商品(teamIdがNullのマスターは削除)
        List<ItemMaster> itemMasterList = itemMasterService.findItemsBetweenDelFlg(today, to, false).stream().filter(e -> e.getTeam_id() != null).collect(Collectors.toList());
        // 今日発売マスター商品からマスターと商品マップを作る(teamIdがNullの商品は削除)
        Map<ItemMaster, List<Item>> itemMasterMap = itemMasterList.stream().collect(Collectors.toMap(e -> e, e -> itemService.findByMasterId(e.getItem_m_id()).stream().filter(f -> f.getTeam_id() != null).collect(Collectors.toList())));

        // 明日~1週間以内の発売商品
        c.setTime(today);
        c.add(Calendar.DATE, 7);

        Date sevenDaysLater = c.getTime();
        // 今日発売マスター商品(teamIdがNullのマスターは削除)
        List<ItemMaster> futureItemMasterList = itemMasterService.findItemsBetweenDelFlg(to, sevenDaysLater, false).stream().filter(e -> e.getTeam_id() != null).collect(Collectors.toList());
        // 今日発売マスター商品からマスターと商品マップを作る(teamIdがNullの商品は削除)
        Map<ItemMaster, List<Item>> futureItemMasterMap = futureItemMasterList.stream().collect(Collectors.toMap(e -> e, e -> itemService.findByMasterId(e.getItem_m_id()).stream().filter(f -> f.getTeam_id() != null).collect(Collectors.toList())));

        // テキストを生成
        String blogText = textController.blogUpdateReleaseItems(itemMasterMap, futureItemMasterMap);

        // リクエスト送信
        HttpHeaders headers = generalHeaderSet(new HttpHeaders());
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("content", blogText);
        HttpEntity<String> request = new HttpEntity<>(jsonObject.toString(), headers);
        String finalUrl = setting.getBlogApiUrl() + "pages/33";
        request(response, finalUrl, request, HttpMethod.POST);
        return "ok";
    }

    /**
     * 認証などどのリクエストでも必要なヘッダーをセットする。
     *
     * @param headers
     * @return
     */
    private HttpHeaders generalHeaderSet(HttpHeaders headers) {
        headers.add("Accept", MediaType.APPLICATION_JSON_VALUE);
        headers.setContentType(MediaType.APPLICATION_JSON);
        String auth = new String(
            Base64.getEncoder().encode(
                    setting.getBlogPw().getBytes()
            )
        );
        headers.add("Authorization","Basic " +  auth);
        return headers;
    }

    /**
     * リクエストを送る
     *
     * @param response
     * @param url
     * @param request
     * @return
     */
    private String request(HttpServletResponse response, String url, HttpEntity<String> request, HttpMethod method) {

        response.setHeader("Cache-Control", "no-cache");

        ResponseEntity<String> responseEntity = restTemplate.exchange(url, method, request, String.class);
        try{
            Thread.sleep(5000);
        }catch(InterruptedException e){
            e.printStackTrace();
        }
        return responseEntity.getBody();
    }

    /**
     * 引数のマスター商品を全て投稿する
     * blogIdを返却する
     *
     * @param itemMaster
     * @param itemList
     */
    public Long postMasterItem(ItemMaster itemMaster, List<Item> itemList) {

        if (itemMaster.getWp_id() != null) {
            updateMasterItem(itemMaster, itemList);
        }

        int blogId = 0;
        // リクエスト送信
        HttpHeaders headers = generalHeaderSet(new HttpHeaders());
        JSONObject jsonObject = new JSONObject();
        String title = textController.createTitle(itemMaster.getPublication_date(), itemMaster.getTitle());
        jsonObject.put("title", title);
        jsonObject.put("author", 1);
        jsonObject.put("categories", new Integer[]{5});

        Integer[] tags = new Integer[itemMaster.getTags().length + 1];
        System.arraycopy(itemMaster.getTags(), 0, tags, 0, itemMaster.getTags().length);
        tags[itemMaster.getTags().length] = dateUtils.getBlogYYYYMMTag(itemMaster.getPublication_date());
        jsonObject.put("tags", tags);
        jsonObject.put("status", "publish");
        jsonObject.put("content", textController.blogReleaseItemsText(Collections.singletonMap(itemMaster, itemList)).get(0));
        HttpEntity<String> request = new HttpEntity<>(jsonObject.toString(), headers);

        String url = setting.getBlogApiUrl() + "posts";
        String res = request(response, url, request, HttpMethod.POST);
        // うまくポストが完了してStringが返却されたらwpIdをitemに登録する
        if (StringUtils.hasText(res)) {
            JSONObject jo = new JSONObject(res);
            if (jo.get("id") != null) {
                blogId = Integer.parseInt(jo.get("id").toString().replaceAll("^\"|\"$", ""));
                itemMaster.setWp_id(blogId);
                itemMasterService.save(itemMaster);
            }
        }
        try{
            Thread.sleep(1000);
        }catch(InterruptedException e){
            e.printStackTrace();
        }
        return (long) blogId;
    }

    /**
     * マスター商品を更新する。
     *
     * @param itemMaster
     * @param itemList
     */
    public Long updateMasterItem(ItemMaster itemMaster, List<Item> itemList) {
        String content = textController.blogReleaseItemsText(Collections.singletonMap(itemMaster, itemList)).get(0);
        HttpHeaders headers = generalHeaderSet(new HttpHeaders());
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("content", content);
        HttpEntity<String> request = new HttpEntity<>(jsonObject.toString(), headers);
        String url = setting.getBlogApiUrl() + "posts/" + itemMaster.getWp_id();
        String res = request(response, url, request, HttpMethod.POST);
        JSONObject jo = new JSONObject(res);
        if (jo.get("id") != null) {
            return Long.parseLong(jo.get("id").toString().replaceAll("^\"|\"$", ""));
        }
        return 0L;
    }

    /**
     * 楽天検索で見つけた新商品のマスター商品IDを繋げ、ブログのマスタ商品投稿を更新する。
     *
     * @param item
     * itemMasterIdを返す
     */
    public Long postOrUpdate(Item item) {
        // 既存の商品マスターがあるか確認する
        Long itemMasterId = itemUtils.judgeNewMaster(item);
        ItemMaster itemMaster = null;

        // 既存の商品マスターがなければ新規登録
        if (itemMasterId.equals(0L)) {
            Map<ItemMaster, Item> savedMap = itemMasterService.addByItem(item);
            for (Map.Entry<ItemMaster, Item> e : savedMap.entrySet()) {
                itemMaster = e.getKey();
                itemMasterId = itemMaster.getItem_m_id();
            }
        } else {
            // 既存の商品マスターがあれば商品を更新
            item.setItem_m_id(itemMasterId);
            itemService.saveItem(item);
            itemMaster = itemMasterService.findById(itemMasterId);
            itemMaster.absolveItem(item);
            itemMasterService.save(itemMaster);
        }

        // ブログを投稿する
        Long blogId = 0L;
        List<Item> itemList = itemService.findByMasterId(itemMasterId);
        if (itemMaster.getWp_id() == null) {
            // 新規投稿する
            blogId = postMasterItem(itemMaster, itemList);
        } else {
            // 既存投稿を更新する(完全洗い替え)
            blogId = updateMasterItem(itemMaster, itemList);
        }
        return blogId;
    }

    /**
     * 画像をWordPressにポストします。
     *
     * @param response
     * @param imageUrl
     * @return 画像ID
     */
    public Integer requestMedia(HttpServletResponse response, Long itemId, String imageUrl) throws IOException {
        String finalUrl = setting.getBlogApiUrl() + "media";

        response.setHeader("Cache-Control", "no-cache");
        HttpHeaders headers = generalHeaderSet(new HttpHeaders());
        headers.add("content-disposition", "attachment; filename=tmp1.jpg");
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        imageUrl = imageUrl.replaceAll("\\?.*$", "");

        String imagePath = "";
        try(InputStream in = new URL(imageUrl).openStream()){
            imagePath = setting.getImageItem() + itemId + ".jpg";
            Files.copy(in, Paths.get(imagePath));
        } catch (Exception e) {
            e.printStackTrace();
        }

        body.add("file", new FileSystemResource(imagePath));
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

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
        String finalUrl = setting.getBlogApiUrl() + "posts/" + wpId;
        HttpHeaders headers = generalHeaderSet(new HttpHeaders());
        return request(response, finalUrl, new HttpEntity<>(headers), HttpMethod.GET);
    }

    /**
     * Tmpブログ新商品投稿メソッド(商品マスターごとに投稿するように修正)
     *
     */
    public void tmpItemPost(List<Item> itemList) {
        Map<ItemMaster, List<Item>> map = itemUtils.groupItem(itemList);
        // 対象はwp_idがnullのマスター商品
        Map<ItemMaster, List<Item>> targetMap = map.entrySet().stream().filter(e -> e.getKey().getWp_id() == null || e.getKey().getWp_id().equals(0)).collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
        // targetMapのマスタ商品をブログに投稿していく
        for (Map.Entry<ItemMaster, List<Item>> e : targetMap.entrySet()) {
            postMasterItem(e.getKey(), e.getValue());
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
        String url = setting.getBlogApiUrl() + "posts/" + wpId;

        HttpHeaders headers = generalHeaderSet(new HttpHeaders());
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("featured_media", imageId);

        HttpEntity<String> request = new HttpEntity<>(jsonObject.toString(), headers);
        request(response, url, request, HttpMethod.POST);
    }

    /**
     * 翌月のyyyyMMタグを追加する。
     *
     */
    public void addNextMonthTag() {
        // どの月でも存在する27・28日の場合、チェックに通す
        if (dateUtils.getDate() == 27 || dateUtils.getDate() == 28) {
            // info DBのblogTagテーブルに翌月のyyyyMMタグが存在するか？
            boolean existsBlogTag = blogTagService.findBlogTagIdByTagName(dateUtils.getNextYYYYMM()) != null;
            if (!existsBlogTag) {
                String url = setting.getBlogApiUrl() + "tags/";

                HttpHeaders headers = generalHeaderSet(new HttpHeaders());
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("name", dateUtils.getNextYYYYMM());

                HttpEntity<String> request = new HttpEntity<>(jsonObject.toString(), headers);
                request(response, url, request, HttpMethod.POST);
            }
        }
    }

    public void getBlogTagNotSavedOnInfoDb() {
        // WPにあるタグを取得する
        String url = setting.getBlogApiUrl() + "tags/";

        HttpHeaders headers = generalHeaderSet(new HttpHeaders());
        JSONObject jsonObject = new JSONObject();
        HttpEntity<String> request = new HttpEntity<>(jsonObject.toString(), headers);
        String res = request(response, url, request, HttpMethod.GET);
        List<BlogTag> blogTagList = new ArrayList<>();

        try {
            JSONArray ja = new JSONArray(res);
            for (int i=0;i<ja.length();i++) {
                Integer wpId = ja.getJSONObject(i).getInt("id");
                String tagName = ja.getJSONObject(i).getString("name").replaceAll("^\"|\"$", "");
                String link = ja.getJSONObject(i).getString("link").replaceAll("^\"|\"$", "");

                if (blogTagService.findBlogTagIdByTagName(tagName) == 0) {
                    BlogTag blogTag = new BlogTag();
                    blogTag.setWp_tag_id((long)wpId);
                    blogTag.setTag_name(tagName);
                    blogTag.setLink(link);
                    blogTagList.add(blogTag);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // infoDBに保存されていないタグは保存する
        blogTagService.saveIfNotSaved(blogTagList);
    }

    /**
     * 商品（マスタじゃない）ページは下書きにする
     *
     */
    public void deleteItemPosts() {

        List<Long> wpIdList = itemService.collectWpId().stream().distinct().collect(Collectors.toList());

        for (Long wpId : wpIdList) {
            // WPにあるタグを取得する
            String url = setting.getBlogApiUrl() + "posts/" + wpId;
            HttpHeaders headers = generalHeaderSet(new HttpHeaders());
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("status","draft");
            HttpEntity<String> request = new HttpEntity<>(jsonObject.toString(), headers);
            request(response, url, request, HttpMethod.POST);
        }
    }

    // Nullが入ってるWPIDをコンソールに出力する
    public void listPostsContainsNull() {
        int n = 1;
        boolean flg = true;
        while (flg) {
            System.out.println(n);
            String url = setting.getBlogApiUrl() + "posts?status=publish&per_page=40&page=" + n;

            HttpHeaders headers = generalHeaderSet(new HttpHeaders());
            JSONObject jsonObject = new JSONObject();
            HttpEntity<String> request = new HttpEntity<>(jsonObject.toString(), headers);
            String res = request(response, url, request, HttpMethod.GET);

            try {
                JSONArray ja = new JSONArray(res);
                for (int i=0;i<ja.length();i++) {
                    Integer wpId = ja.getJSONObject(i).getInt("id");
                    Integer media = ja.getJSONObject(i).getInt("featured_media");
//                    String title = ja.getJSONObject(i).getJSONObject("featured_media").getString("rendered").replaceAll("^\"|\"$", "");
//                    String content = ja.getJSONObject(i).getJSONObject("content").getString("rendered").replaceAll("^\"|\"$", "");
//                    if (title.contains("Null") || content.contains("Null")) {
//                        System.out.println(wpId);
//                        System.out.println(title);
//                        System.out.println(content);
//                    }
                    if (media > 0) {
                        System.out.println(wpId + ":" + media);
                    }
                }
            } catch (Exception e) {
                flg = false;
                e.printStackTrace();
            }
            ++n;
        }
    }

    /**
     * Excerptを空にする
     *
     */
    public void eliminateExcerpt() {
        int n = 1;
        String url = setting.getBlogApiUrl() + "posts?status=publish&per_page=40&page=" + n;

        HttpHeaders headers = generalHeaderSet(new HttpHeaders());
        JSONObject jsonObject = new JSONObject();
        HttpEntity<String> request = new HttpEntity<>(jsonObject.toString(), headers);
        String res = request(response, url, request, HttpMethod.GET);

        try {
            JSONArray ja = new JSONArray(res);
            for (int i=0;i<ja.length();i++) {
                Integer wpId = ja.getJSONObject(i).getInt("id");
//                Integer media = ja.getJSONObject(i).getInt("featured_media");
                String excerpt = ja.getJSONObject(i).getJSONObject("excerpt").getString("rendered").replaceAll("^\"|\"$", "");
//                    String content = ja.getJSONObject(i).getJSONObject("content").getString("rendered").replaceAll("^\"|\"$", "");
//                    if (title.contains("Null") || content.contains("Null")) {
//                        System.out.println(wpId);
//                        System.out.println(title);
//                        System.out.println(content);
//                    }
                if (excerpt != null) {
                    url = setting.getBlogApiUrl() + "posts/" + wpId;

                    HttpHeaders headers1 = generalHeaderSet(new HttpHeaders());
                    JSONObject jsonObject1 = new JSONObject();
                    jsonObject1.put("excerpt", "");
                    HttpEntity<String> request1 = new HttpEntity<>(jsonObject1.toString(), headers1);
                    request(response, url, request1, HttpMethod.POST);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        ++n;
    }

    public void updateTitle() {
        List<ItemMaster> itemMasterList = itemMasterService.findWpIdNotNull();

        for (ItemMaster itemMaster : itemMasterList) {
            HttpHeaders headers = generalHeaderSet(new HttpHeaders());
            JSONObject jsonObject = new JSONObject();
            String title = textController.createTitle(itemMaster.getPublication_date(), itemMaster.getTitle());
            jsonObject.put("title", title);
            HttpEntity<String> request = new HttpEntity<>(jsonObject.toString(), headers);

            String url = setting.getBlogApiUrl() + "posts/" + itemMaster.getWp_id();
            request(response, url, request, HttpMethod.POST);
        }
    }

    public void addTag() {
        getBlogTagNotSavedOnInfoDb();
        List<ItemMaster> itemMasterList = itemMasterService.findWpIdNotNull();

        for (ItemMaster itemMaster : itemMasterList) {
            HttpHeaders headers = generalHeaderSet(new HttpHeaders());
            JSONObject jsonObject = new JSONObject();

            Integer[] tags = new Integer[itemMaster.getTags().length + 1];
            System.arraycopy(itemMaster.getTags(), 0, tags, 0, itemMaster.getTags().length);

            int yyyyMMId = dateUtils.getBlogYYYYMMTag(itemMaster.getPublication_date());

            // もし年月タグがまだ存在しなかったら先に登録する
            if (yyyyMMId == 0) {
                String url = setting.getBlogApiUrl() + "tags/";

                HttpHeaders h = generalHeaderSet(new HttpHeaders());
                JSONObject jo = new JSONObject();
                jo.put("name", dateUtils.getYYYYMM(itemMaster.getPublication_date()));

                HttpEntity<String> request = new HttpEntity<>(jo.toString(), h);
                String res = request(response, url, request, HttpMethod.POST);

                JSONObject jsonObject1 = new JSONObject(res);
                if (jsonObject1.get("id") != null) {
                    yyyyMMId = jsonObject1.getInt("id");
                    String link = jsonObject1.getString("link").replaceAll("^\"|\"$", "");
                    BlogTag blogTag = new BlogTag();
                    blogTag.setTag_name(dateUtils.getYYYYMM(itemMaster.getPublication_date()));
                    blogTag.setWp_tag_id((long) yyyyMMId);
                    blogTag.setLink(link);
                    blogTagService.save(blogTag);
                }
            }
            tags[itemMaster.getTags().length] = yyyyMMId;
            jsonObject.put("tags", tags);
            HttpEntity<String> request = new HttpEntity<>(jsonObject.toString(), headers);

            // 商品ページ投稿更新
            String url = setting.getBlogApiUrl() + "posts/" + itemMaster.getWp_id();
            request(response, url, request, HttpMethod.POST);
        }
    }

    /**
     * TV番組の固定ページを更新
     */
    public void updateTvPage() {
        String text = textController.tvPageText(programService.findByOnAirDate(dateUtils.daysAfterToday(0)))
                + "\n" + textController.tvPageText(programService.findByOnAirDate(dateUtils.daysAfterToday(1)))
                + "\n" + textController.tvPageText(programService.findByOnAirDate(dateUtils.daysAfterToday(2)))
                + "\n" + textController.tvPageText(programService.findByOnAirDate(dateUtils.daysAfterToday(3)))
                + "\n" + textController.tvPageText(programService.findByOnAirDate(dateUtils.daysAfterToday(4)))
                + "\n" + textController.tvPageText(programService.findByOnAirDate(dateUtils.daysAfterToday(5)))
                + "\n" + textController.tvPageText(programService.findByOnAirDate(dateUtils.daysAfterToday(6)));

        System.out.println(text);
        // ページを更新する
        String url = setting.getBlogApiUrl() + "pages/1707";

        HttpHeaders headers = generalHeaderSet(new HttpHeaders());
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("content", text);
        HttpEntity<String> request = new HttpEntity<>(jsonObject.toString(), headers);
        request(response, url, request, HttpMethod.POST);
    }
}

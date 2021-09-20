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
import otaku.info.searvice.*;
import otaku.info.setting.Setting;
import otaku.info.utils.ItemUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("blog")
@AllArgsConstructor
public class BlogController {

    @Autowired
    TextController textController;

    @Autowired
    ImageController imageController;

    @Autowired
    ItemService itemService;

    @Autowired
    ProgramService programService;

    @Autowired
    ItemMasterService itemMasterService;

    @Autowired
    BlogTagService blogTagService;

    @Autowired
    TeamService teamService;

    @Autowired
    ItemUtils itemUtils;

    @Autowired
    otaku.info.utils.DateUtils dateUtils;

    @Autowired
    Setting setting;

    HttpServletResponse response;

    /**
     * 近日販売商品のブログページ(固定)を更新します。
     * ・本日販売
     * ・明日以降1週間の商品
     * 上記商品で画面を書き換える。
     */
    public String updateReleaseItems() {

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
        String auth = new String(Base64.getEncoder().encode(setting.getBlogPw().getBytes()));
        headers.add("Authorization", "Basic " + auth);
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

        try {
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> responseEntity = restTemplate.exchange(url, method, request, String.class);

            if (responseEntity.getStatusCode().equals(HttpStatus.FORBIDDEN)) {
                return "";
            }
            try {
                Thread.sleep(5000);
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
        System.out.println("title: " + title);
        jsonObject.put("title", title);
        jsonObject.put("author", 1);
        jsonObject.put("categories", new Integer[]{5});

        // tag:チーム名と発売日の年月
        BlogTag yyyyMMTag = addTagIfNotExists(itemMaster.getPublication_date());

        if (yyyyMMTag != null && yyyyMMTag.getBlog_tag_id() != null) {
            Integer[] tags = new Integer[itemMaster.getTags().length + 1];
            System.arraycopy(itemMaster.getTags(), 0, tags, 0, itemMaster.getTags().length);
            tags[itemMaster.getTags().length] = Math.toIntExact(yyyyMMTag.getBlog_tag_id());
            jsonObject.put("tags", tags);
            jsonObject.put("status", "publish");
            jsonObject.put("content", textController.blogReleaseItemsText(Collections.singletonMap(itemMaster, itemList)).get(0));
            HttpEntity<String> request = new HttpEntity<>(jsonObject.toString(), headers);

            String url = setting.getBlogApiUrl() + "posts/";
            String res = request(response, url, request, HttpMethod.POST);
            // うまくポストが完了してStringが返却されたらwpIdをitemに登録する
            if (StringUtils.hasText(res)) {
                JSONObject jo = new JSONObject(res);
                if (jo.get("id") != null) {
                    blogId = Integer.parseInt(jo.get("id").toString().replaceAll("^\"|\"$", ""));
                    System.out.println("posted wp blog id: " + blogId);
                    itemMaster.setWp_id(blogId);
                    itemMasterService.save(itemMaster);

                    // 画像を登録する
                    boolean hasImage = itemList.stream().anyMatch(e -> e.getImage1() != null);
                    if (itemMaster.getImage1() != null || hasImage) {

                        // TODO:itemMasterがnullの場合、itemの画像を設定してあげる

                        // itemMasterの画像を設定してあげる
                        if (itemMaster.getImage1() != null) {
                            List<ItemMaster> itemMasterList = new ArrayList<>();
                            itemMasterList.add(itemMaster);
                            loadMedia(itemMasterList, false);
                        }
                    } else {
                        // 画像のないitemMaster & itemの場合、画像生成してアイキャッチを設定してあげる（発売日\n[チーム名1 チーム名2 チーム名3]）
                        // 楽天APIから取得してきた商品画像との見分けは、image1,2,3のパスがhttp://rakutenではなくローカルであることで判別可能

                        // チーム名を文字列に
                        List<String> teamNameList = new ArrayList<>();
                        List.of(itemMaster.getTeam_id().split(",")).stream().forEach(e -> teamNameList.add(teamService.getTeamName(Long.parseLong(e))));
                        String teamName = teamNameList.stream().distinct().collect(Collectors.joining(" "));
                        // 画像生成
                        String path = imageController.createImage(itemMaster.getItem_m_id().toString() + ".png", textController.dateToString(itemMaster.getPublication_date()), teamName);

                        // itemMasterにパスを設定
                        itemMaster.setImage1(path);
                        itemMasterService.save(itemMaster);

                        // 画像投稿&itemMasterに設定
                        List<ItemMaster> itemMasterList = new ArrayList<>();
                        itemMasterList.add(itemMaster);
                        loadMedia(itemMasterList, false);
                    }
                    System.out.println("*** itemMaster saved");
                }
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return (long) blogId;
    }

    public void postAllItemMaster() {
        Integer year = 2001;

        Map<ItemMaster, List<Item>> itemMasterListMap = new HashMap<>();
        while (year < 2022) {
            System.out.println("*** year: " + year);
            // itemMasterを集める
            List<ItemMaster> itemMasterList = itemMasterService.findByPublicationYear(year).stream().filter(e -> e.getWp_id() == null).collect(Collectors.toList());
            System.out.println("itemMasterList.size: " + itemMasterList.size());
            // ひもづくitemを集める
            itemMasterList.forEach(e -> itemMasterListMap.put(e, itemService.gatherItems(e.getItem_m_id())));
            // itemMasterを投稿する
            if (itemMasterListMap.size() > 0) {
                itemMasterListMap.entrySet().stream().sorted(Comparator.comparing(e -> e.getKey().getPublication_date()));
                for (Map.Entry<ItemMaster, List<Item>> e : itemMasterListMap.entrySet()) {
                    System.out.println("item_m_id: " + e.getKey().getItem_m_id() + " itemList size: " + e.getValue().size());
                    postMasterItem(e.getKey(), e.getValue());
                }
            }
            ++year;
        }
    }

    /**
     * マスター商品のcontextを更新する。
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
     * ブログのマスタ商品投稿を更新する。
     *
     * @param itemMasterList itemMasterIdを返す
     */
    public void postOrUpdate(List<ItemMaster> itemMasterList) {

        for (ItemMaster itemMaster : itemMasterList) {
            // ブログを投稿する
            List<Item> itemList = itemService.findByMasterId(itemMaster.getItem_m_id());
            if (itemMaster.getWp_id() == null) {
                // 新規投稿する
                postMasterItem(itemMaster, itemList);
            } else {
                // 既存投稿を更新する(完全洗い替え)
                updateMasterItem(itemMaster, itemList);
            }
        }
    }

    /**
     * 画像をWordPressにポストします。
     *
     * @param response
     * @param imageUrl
     * @return /<WP画像ID, WP画像パス/>
     */
    public Map<Integer, String> requestMedia(HttpServletResponse response, Long itemId, String imageUrl) {
        String finalUrl = setting.getBlogApiUrl() + "media";

        imageUrl = imageUrl.replaceAll("\\?.*$", "");

        String imagePath = "";

        // 楽天の画像の場合は取得しに行く
        if (imageUrl.startsWith("http")) {
            try (InputStream in = new URL(imageUrl).openStream()) {
                imagePath = availablePath(imageUrl);
                Files.copy(in, Paths.get(imagePath));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            imagePath = imageUrl;
        }

        response.setHeader("Cache-Control", "no-cache");
        HttpHeaders headers = generalHeaderSet(new HttpHeaders());
        headers.add("content-disposition", "attachment; filename=" + itemId + ".png");
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

        body.add("file", new FileSystemResource(imagePath));
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        System.out.println("画像投稿します");
        System.out.println(imagePath);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> responseEntity = restTemplate.postForEntity(finalUrl, requestEntity, String.class);
        String text = responseEntity.getBody();
        System.out.println("request result: " + text);
        JSONObject jsonObject = new JSONObject(text);
        if (jsonObject.get("id") != null) {
            return Collections.singletonMap(jsonObject.getInt("id"), jsonObject.get("source_url").toString().replaceAll("^\"|\\|\"$", ""));
        }
        return Collections.singletonMap(0, "");
    }

    /**
     * 使用できるパスを見つけ、返却します
     * 楽天の画像で使用することを想定
     *
     * @param imagePath
     * @return
     */
    private String availablePath(String imagePath) {
        String newPath = setting.getImageItem() + imagePath + ".png";
        Path path = Paths.get(newPath);
        Integer count = 1;

        while (Files.exists(path)) {
            newPath = setting.getImageItem() + imagePath + "_" + count.toString() + ".png";
            path = Paths.get(newPath);
            ++count;
        }
        return newPath;
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
     * @param itemMasterList 登録対象
     * @param wpChk WPへアイキャッチメディアの設定が既にあるかチェックを投げるかフラグ
     */
    public void loadMedia(List<ItemMaster> itemMasterList, boolean wpChk) {
        for (ItemMaster itemMaster : itemMasterList) {

            Integer wpId = 0;

            // wpChkフラグがtrueだったらWPへアイキャッチの設定があるか確認する
            if (wpChk) {
                // すでに画像がブログ投稿にセットされてるか確認しないといけないのでリクエストを送信し既存のデータを取得する
                String url = setting.getBlogApiUrl() + "posts/" + itemMaster.getWp_id() + "?_fields[]=id";

                HttpHeaders headers = generalHeaderSet(new HttpHeaders());
                JSONObject jsonObject = new JSONObject();
                HttpEntity<String> request = new HttpEntity<>(jsonObject.toString(), headers);
                String res = request(response, url, request, HttpMethod.GET);

                try {
                    // アイキャッチメディアのIDを取得する
                    Integer mediaId = extractMedia(res);
                    System.out.println("アイキャッチ：" + mediaId);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // 画像をポストする(WPチェックでメディア設定がなかった場合||WPチェックなしで全て対象の場合)
            if (!wpChk || wpId == 0) {
                String imageUrl = itemMaster.getImage1();
                if (!StringUtils.hasText(imageUrl)) {
                    imageUrl = itemService.getImageUrlByItemMIdImage1NotNull(itemMaster.getItem_m_id());
                }

                // itemにも画像がなかったら生成する
                if (!StringUtils.hasText(imageUrl)) {
                    List<String> teamNameList = new ArrayList<>();
                    List.of(itemMaster.getTeam_id().split(",")).stream().forEach(e -> teamNameList.add(teamService.getTeamName(Long.parseLong(e))));
                    String teamName = teamNameList.stream().distinct().collect(Collectors.joining(" "));
                    imageUrl = imageController.createImage(itemMaster.getItem_m_id().toString() + ".png", textController.dateToString(itemMaster.getPublication_date()), teamName);
                    itemMaster.setImage1(imageUrl);
                    itemMasterService.save(itemMaster);
                }

                // 画像が用意できたら投稿していく
                if (StringUtils.hasText(imageUrl)) {
                    System.out.println("メディアポスト:" + imageUrl);
                    Map<Integer, String> wpMediaIdUrlMap = requestMedia(response, (long) itemMaster.getWp_id(), imageUrl);
                    Integer wpMediaId = null;
                    String mediaUrl = null;

                    if (!wpMediaIdUrlMap.isEmpty()) {
                        Map.Entry<Integer, String> entry = wpMediaIdUrlMap.entrySet().stream().findFirst().get();
                        wpMediaId = entry.getKey();
                        mediaUrl = entry.getValue();
                    }

                    System.out.println("ポスト完了");
                    // なんかアップロードに失敗したら次のマスター商品に飛ばす
                    if (wpMediaId == null || wpMediaId == 0) {
                        continue;
                    }

                    // 無事アップロードできてたらブログ投稿にアイキャッチを設定してあげる
                    setMedia(itemMaster.getWp_id(), wpMediaId);

                    // TODO: itemMasterにはWPにアップした画像のIDを設定するところがないんだよね→画像パスで暫定対応
                    // WPのアイキャッチ画像に登録した画像のパスを設定する
                    itemMaster.setUrl(mediaUrl);
                    itemMasterService.save(itemMaster);
                }
            }
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

    /**
     * WPにあるがDBにないタグを保存する
     *
     */
    public void getBlogTagNotSavedOnInfoDb() {
        // WPにあるタグを取得する
        String url = setting.getBlogApiUrl() + "tags?_fields[]=id&_fields[]=name&_fields[]=link";

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
     * 公開中のブログポストのcontentを上書きする（楽天リンクをカードにした）
     *
     */
    public void updateContent() {
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
                url = setting.getBlogApiUrl() + "posts/" + wpId;

                HttpHeaders headers1 = generalHeaderSet(new HttpHeaders());
                JSONObject jsonObject1 = new JSONObject();
                ItemMaster itemMaster = itemMasterService.findByWpId(wpId);

                if (itemMaster != null && itemMaster.getItem_m_id() != null) {
                    List<Item> itemList = itemService.findByMasterId(itemMaster.getItem_m_id());

                    if (itemList.size() > 0) {
                        Map<ItemMaster, List<Item>> itemMasterListMap = Collections.singletonMap(itemMaster, itemList);
                        String text = textController.blogReleaseItemsText(itemMasterListMap).get(0);
                        jsonObject1.put("content", text);
                        HttpEntity<String> request1 = new HttpEntity<>(jsonObject1.toString(), headers1);
                        String r = request(response, url, request1, HttpMethod.POST);
                        System.out.println(r);
                    }
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

    /**
     * タグが存在しなかったらWPとDB両方に登録する
     *
     */
    public BlogTag addTagIfNotExists(Date date) {

        String yyyyMM = dateUtils.getYYYYMM(date);

        String url = "https://otakuinfo.fun/wp-json/wp/v2/tags?_fields[]=name&slug=" + yyyyMM;

        // request
        HttpHeaders headers = generalHeaderSet(new HttpHeaders());
        JSONObject jsonObject = new JSONObject();
        HttpEntity<String> request = new HttpEntity<>(jsonObject.toString(), headers);
        String res = request(response, url, request, HttpMethod.GET);

        BlogTag blogTag = new BlogTag();

        try {
            JSONArray ja = new JSONArray(res);
            // タグがまだWPになかったら登録する
            if (ja.length() == 0) {
                blogTag = registerTag(date);
            } else {
                // タグはWPにある場合
                blogTag = blogTagService.findByTagName(yyyyMM);

                // WPにタグあるがDBから見つからなかった場合、DBに登録する
                if (blogTag == null || blogTag.getBlog_tag_id() == null) {
                    BlogTag blogTag1 = new BlogTag();

                    // WPからDBに登録したいタグのデータを取ってくる
                    String url1 = "https://otakuinfo.fun/wp-json/wp/v2/tags?slug=" + yyyyMM + "&per_page=1";
                    // request
                    HttpHeaders headers1 = generalHeaderSet(new HttpHeaders());
                    JSONObject jsonObject1 = new JSONObject();
                    HttpEntity<String> request1 = new HttpEntity<>(jsonObject1.toString(), headers);
                    String res1 = request(response, url1, request1, HttpMethod.GET);

                    try {
                        JSONArray ja1 = new JSONArray(res1);

                        blogTag1.setTag_name(ja1.getJSONObject(0).getString("name"));
                        blogTag1.setLink(ja1.getJSONObject(0).getString("link"));
                        blogTag1.setWp_tag_id((long) ja1.getJSONObject(0).getInt("id"));
                        blogTagService.save(blogTag1);

                        // 無事にDB登録までできたので返却するBlogTagに設定してあげる
                        blogTag = blogTag1;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return blogTag;
    }

    public BlogTag registerTag(Date date) {
        String url = setting.getBlogApiUrl() + "tags/";

        HttpHeaders h = generalHeaderSet(new HttpHeaders());
        JSONObject jo = new JSONObject();
        jo.put("name", dateUtils.getYYYYMM(date));

        HttpEntity<String> request = new HttpEntity<>(jo.toString(), h);
        String res = request(response, url, request, HttpMethod.POST);

        JSONObject jsonObject1 = new JSONObject(res);

        Integer yyyyMMId;
        if (jsonObject1.get("id") != null) {
            yyyyMMId = jsonObject1.getInt("id");
            String link = jsonObject1.getString("link").replaceAll("^\"|\"$", "");
            BlogTag blogTag = new BlogTag();
            blogTag.setTag_name(dateUtils.getYYYYMM(date));
            blogTag.setWp_tag_id((long) yyyyMMId);
            blogTag.setLink(link);
            return blogTagService.save(blogTag);
        }
        return new BlogTag();
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
                yyyyMMId = Math.toIntExact(registerTag(itemMaster.getPublication_date()).getBlog_tag_id());
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

    /**
     * アイキャッチメディアの設定がないWPIDを取得します
     *
     * @return
     */
    public List<Integer> findNoEyeCatchPosts() {
        List<Integer> resultList = new ArrayList<>();

        // リクエスト送信
        HttpHeaders headers = generalHeaderSet(new HttpHeaders());
        JSONObject jsonObject = new JSONObject();
        HttpEntity<String> request = new HttpEntity<>(jsonObject.toString(), headers);

        int n = 1;
        boolean nextFlg = true;

        while (nextFlg) {
            String url = setting.getBlogApiUrl() + "posts?status=publish&_fields[]=id&_fields[]=featured_media&per_page=100&page=" + n;
            String res = request(response, url, request, HttpMethod.GET);

            // レスポンスを成形
            try {
                JSONArray ja = new JSONArray(res);

                if (ja.length() > 0) {
                    for (int i=0; i < ja.length(); i++) {
                        if (ja.getJSONObject(i).getInt("featured_media") != 0) {
                            resultList.add(ja.getJSONObject(i).getInt("id"));
                        }
                    }
                    ++n;
                }
            } catch (Exception e) {
                nextFlg = false;
                e.printStackTrace();
            }
        }

        return resultList;
    }

    public Map<Integer, Integer> getPublishedWpIdFeaturedMediaList() {
        Map<Integer, Integer> resultMap = new HashMap<>();

        // リクエスト送信
        HttpHeaders headers = generalHeaderSet(new HttpHeaders());
        JSONObject jsonObject = new JSONObject();
        HttpEntity<String> request = new HttpEntity<>(jsonObject.toString(), headers);

        int n = 1;
        boolean nextFlg = true;

        while (nextFlg) {
            String url = setting.getBlogApiUrl() + "posts?status=publish&_fields[]=featured_media&_fields[]=id&per_page=100&page=" + n;
            String res = request(response, url, request, HttpMethod.GET);

            // レスポンスを成形
            try {
                JSONArray ja = new JSONArray(res);

                if (ja.length() > 0) {
                    for (int i=0; i < ja.length(); i++) {
                        resultMap.put(ja.getJSONObject(i).getInt("id"), ja.getJSONObject(i).getInt("featured_media"));
                    }
                    ++n;
                }
            } catch (Exception e) {
                nextFlg = false;
                e.printStackTrace();
            }
        }
        return resultMap;
    }

    public Map<Integer, String> getMediaUrlByMediaId(List<Integer> mediaIdList) {
        Map<Integer, String> resultMap = new HashMap<>();

        int start = 0;
        int end = mediaIdList.size() -1;
        boolean next100Flg = true;

        if (end > 99) {
            end = 99;
        }

        List<String> mediaIrListStrList = new ArrayList<>();

        while (next100Flg) {
            String tmp = mediaIdList.subList(start, end).stream().map(Object::toString).collect(Collectors.joining(","));
            mediaIrListStrList.add(tmp);
            if (mediaIdList.size() > end) {
                start += 100;
                end += 100;

                if (mediaIdList.size() -1 < end) {
                    end = mediaIdList.size() -1;
                }
            } else {
                next100Flg = false;
            }
        }

        for (String mediaIdStr : mediaIrListStrList) {
            String res = getMediaUrl(mediaIdStr);
            // レスポンスを成形
            try {
                JSONArray ja = new JSONArray(res);

                if (ja.length() > 0) {
                    for (int i=0; i < ja.length(); i++) {
                        resultMap.put(ja.getJSONObject(i).getInt("id"), ja.getJSONObject(i).getString("source_url").replaceAll("^\"|\"$", ""));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return resultMap;
    }

    private String getMediaUrl(String mediaIdList) {
        String url = setting.getBlogApiUrl() + "media?slug=" + mediaIdList + "&_fields[]=id&_fields[]=source_url&per_page=100";

        // リクエスト送信
        HttpHeaders headers = generalHeaderSet(new HttpHeaders());
        JSONObject jsonObject = new JSONObject();
        HttpEntity<String> request = new HttpEntity<>(jsonObject.toString(), headers);
        return request(response, url, request, HttpMethod.GET);
    }
}

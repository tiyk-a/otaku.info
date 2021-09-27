package otaku.info.controller;

import lombok.AllArgsConstructor;
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
import otaku.info.utils.JsonUtils;
import otaku.info.utils.ServerUtils;
import otaku.info.utils.StringUtilsMine;

import javax.servlet.http.HttpServletResponse;
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
    TextController textController;

    @Autowired
    ImageController imageController;

    @Autowired
    RakutenController rakutenController;

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
    ServerUtils serverUtils;

    @Autowired
    StringUtilsMine stringUtilsMine;

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
        Date today = dateUtils.getToday();

        // 24時間後の日付を取得
        Date to = dateUtils.daysAfterToday(1);

        // 今日発売マスター商品(teamIdがNullのマスターは削除)
        List<ItemMaster> itemMasterList = itemMasterService.findItemsBetweenDelFlg(today, to, false).stream().filter(e -> e.getTeam_id() != null).collect(Collectors.toList());
        // 今日発売マスター商品からマスターと商品マップを作る(teamIdがNullの商品は削除)
        Map<ItemMaster, List<Item>> itemMasterMap = itemMasterList.stream().collect(Collectors.toMap(e -> e, e -> itemService.findByMasterId(e.getItem_m_id()).stream().filter(f -> f.getTeam_id() != null).collect(Collectors.toList())));

        // 明日~1週間以内の発売商品
        Date sevenDaysLater = dateUtils.daysAfterToday(7);

        // 今日発売マスター商品(teamIdがNullのマスターは削除)
        List<ItemMaster> futureItemMasterList = itemMasterService.findItemsBetweenDelFlg(to, sevenDaysLater, false).stream().filter(e -> e.getTeam_id() != null).collect(Collectors.toList());
        // 今日発売マスター商品からマスターと商品マップを作る(teamIdがNullの商品は削除)
        Map<ItemMaster, List<Item>> futureItemMasterMap = futureItemMasterList.stream().collect(Collectors.toMap(e -> e, e -> itemService.findByMasterId(e.getItem_m_id()).stream().filter(f -> StringUtils.hasText(f.getTeam_id())).collect(Collectors.toList())));

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
    public HttpHeaders generalHeaderSet(HttpHeaders headers) {
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
    public String request(HttpServletResponse response, String url, HttpEntity<String> request, HttpMethod method) {

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

                        // itemMasterがnullの場合、itemの画像を設定してあげる(1だけ)
                        if (itemMaster.getImage1() == null) {
                            itemMaster.fillBlankImage(itemList.get(0).getImage1());
                        }

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
     * TODO: 楽天画像の場合、すでにWP投稿済みだったとしても毎回楽天から画像をローカルへ保存してしまう。連番がどんどん増えてしまう。
     *
     * @param response
     * @param imageUrl
     * @return /<WP画像ID, WP画像パス/>
     */
    public Map<Integer, String> requestMedia(HttpServletResponse response, Long wpId, String imageUrl) {
        String finalUrl = setting.getBlogApiUrl() + "media";

        imageUrl = imageUrl.replaceAll("\\?.*$", "");

        String imagePath = "";

        // 楽天の画像の場合は取得しに行く
        if (imageUrl.startsWith("https")) {
            try (InputStream in = new URL(imageUrl).openStream()) {
                String identifier = stringUtilsMine.extractSubstring(imageUrl, "\\?.*$");
                imagePath = serverUtils.availablePath(setting.getImageItem() + wpId.toString() + identifier);
                Files.copy(in, Paths.get(imagePath));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            imagePath = imageUrl;
        }

        response.setHeader("Cache-Control", "no-cache");
        HttpHeaders headers = generalHeaderSet(new HttpHeaders());
        headers.add("content-disposition", "attachment; filename=" + wpId.toString() + ".png");
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
     * アイキャッチメディアがある場合、画像IDを返却します。
     * ない場合、0
     *
     * @param text
     * @return
     */
    public Integer extractMedia(String text) {
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

            // wpChkフラグがtrueだったらWPへアイキャッチの設定があるか確認する
            Integer mediaId = 0;
            if (wpChk) {
                // すでに画像がブログ投稿にセットされてるか確認しないといけないのでリクエストを送信し既存のデータを取得する
                String url = setting.getBlogApiUrl() + "posts/" + itemMaster.getWp_id() + "?_fields[]=id&_fields[]=featured_media";

                HttpHeaders headers = generalHeaderSet(new HttpHeaders());
                JSONObject jsonObject = new JSONObject();
                HttpEntity<String> request = new HttpEntity<>(jsonObject.toString(), headers);
                String res = request(response, url, request, HttpMethod.GET);

                try {
                    // アイキャッチメディアのIDを取得する
                    mediaId = extractMedia(res);
                    System.out.println("アイキャッチ：" + mediaId);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // itemMasterに画像が登録されてない場合、image1がローカルgeneratedの場合、楽天検索して画像をitemMasterに追加して更新
            List<Item> itemList = itemService.findByMasterId(itemMaster.getItem_m_id());
            // itemに画像があればitemMasterに設定
            if (itemMaster.getImage1() == null && itemList.stream().anyMatch(e -> StringUtils.hasText(e.getImage1()) || StringUtils.hasText(e.getImage2()) || !StringUtils.hasText(e.getImage3()))) {
                itemMaster.fillBlankImage(itemList.stream().filter(e -> StringUtils.hasText(e.getImage1())).findFirst().get().getImage1());
            }
            // itemMasterの画像がgeneratedの場合、楽天に探しに行く
            if (itemMaster.getImage1() == null || itemMaster.getImage1().startsWith(setting.getImageItem())) {
                itemMaster = rakutenController.addImage(itemMaster);
            }

            // 画像をポストする(WPチェックでメディア設定がなかった場合||WPチェックなしで全て対象の場合)
            if (!wpChk || mediaId == 0) {
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
            if (JsonUtils.isJsonArray(res)) {
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
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // infoDBに保存されていないタグは保存する
        if (blogTagList.size() > 0) {
            blogTagService.saveIfNotSaved(blogTagList);
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
            if (JsonUtils.isJsonArray(res)) {
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
                            if (!JsonUtils.isJsonArray(res1)) {
                                JSONArray ja1 = new JSONArray(res1);

                                blogTag1.setTag_name(ja1.getJSONObject(0).getString("name"));
                                blogTag1.setLink(ja1.getJSONObject(0).getString("link"));
                                blogTag1.setWp_tag_id((long) ja1.getJSONObject(0).getInt("id"));
                                blogTagService.save(blogTag1);

                                // 無事にDB登録までできたので返却するBlogTagに設定してあげる
                                blogTag = blogTag1;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return blogTag;
    }

    /**
     * 日付タグをWPとDBに登録します。
     *
     * @param date
     * @return
     */
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

        // テキストを用意できた時だけページを更新する
        if (StringUtils.hasText(text)) {
            String url = setting.getBlogApiUrl() + "pages/1707";

            HttpHeaders headers = generalHeaderSet(new HttpHeaders());
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("content", text);
            HttpEntity<String> request = new HttpEntity<>(jsonObject.toString(), headers);
            request(response, url, request, HttpMethod.POST);
        }
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
            System.out.println(url);
            String res = request(response, url, request, HttpMethod.GET);

            // レスポンスを成形
            try {
                if (!JsonUtils.isJsonArray(res)) {
                    continue;
                }
                JSONArray ja = new JSONArray(res);

                if (ja.length() > 0) {
                    for (int i=0; i < ja.length(); i++) {
                        if (ja.getJSONObject(i).getInt("featured_media") == 0) {
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
}

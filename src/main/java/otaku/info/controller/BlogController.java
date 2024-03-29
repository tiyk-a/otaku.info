package otaku.info.controller;

import lombok.AllArgsConstructor;
import org.apache.log4j.Logger;
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
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import otaku.info.dto.TwiDto;
import otaku.info.entity.*;
import otaku.info.enums.BlogEnum;
import otaku.info.enums.MemberEnum;
import otaku.info.enums.TeamEnum;
import otaku.info.service.*;
import otaku.info.setting.Log4jUtils;
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
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("blog")
@AllArgsConstructor
public class BlogController {

    final Logger logger = Log4jUtils.newConsoleCsvAllLogger("BlogController");
    final Logger blogLog = Log4jUtils.newFileLogger("BlogControllerPost", "BlogPost.log");

    @Autowired
    TextController textController;

    @Autowired
    RakutenController rakutenController;

    @Autowired
    ImageController imageController;

    @Autowired
    TwTextController twTextController;

    @Autowired
    PythonController pythonController;

    @Autowired
    ItemService itemService;

    @Autowired
    ProgramService programService;

    @Autowired
    PMService pmService;

    @Autowired
    IMService imService;

    @Autowired
    ImVerService imVerService;

    @Autowired
    BlogTagService blogTagService;

    @Autowired
    TeamService teamService;

    @Autowired
    BlogPostService blogPostService;

    @Autowired
    ItemUtils itemUtils;

    @Autowired
    otaku.info.utils.DateUtils dateUtils;

    @Autowired
    ServerUtils serverUtils;

    @Autowired
    JsonUtils jsonUtils;

    @Autowired
    StringUtilsMine stringUtilsMine;

    @Autowired
    Setting setting;

    HttpServletResponse response;

    /**
     * TODO: どこでも使ってないメソッド？いい？
     * 引数TeamEnumのブログにあるタグがDBになかったらDBにデータを入れます
     *
     * @param blogEnum
     */
    public void insertTags(BlogEnum blogEnum) {
        Integer n = 1;
        boolean nextFlg = true;

        while (nextFlg) {
            String url = blogEnum.getSubDomain() + setting.getBlogApiPath() + "tags?_fields[]=id&_fields[]=name&_fields[]=link&per_page=40&page=" + n;

            // request
            HttpHeaders headers = generalHeaderSet(new HttpHeaders(), blogEnum);
            JSONObject jsonObject = new JSONObject();
            HttpEntity<String> request = new HttpEntity<>(jsonObject.toString(), headers);
            String res = request(url, request, HttpMethod.GET, "insertTags()");

            try {
                if (JsonUtils.isJsonArray(res)) {
                    JSONArray ja = new JSONArray(res);

                    // レスポンスがリクエスト通りのarray sizeだったら次があるかもしれない。なかったらもう次はないのでflgをoffにする
                    if (ja.length() == 40) {
                        ++n;
                    } else {
                        nextFlg = false;
                    }

                    List<BlogTag> blogTagList = new ArrayList<>();

                    for (int i = 0; i < ja.length(); i++) {
                        Integer wpId = ja.getJSONObject(i).getInt("id");
                        String tagName = ja.getJSONObject(i).getString("name").replaceAll("^\"|\"$", "");
                        String link = ja.getJSONObject(i).getString("link").replaceAll("^\"|\"$", "");

                        BlogTag blogTag = new BlogTag();
                        blogTag.setWp_tag_id((long) wpId);
                        blogTag.setTag_name(tagName);
                        blogTag.setLink(link);
                        blogTag.setTeam_id(blogEnum.getId());
                        blogTagList.add(blogTag);
                    }
                    blogTagService.saveAll(blogTagList);
                }
            } catch (Exception ex) {
                logger.error("ブログエラー");
                ex.printStackTrace();
            }
        }
    }

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

        List<BlogEnum> blogEnumList = Arrays.asList(BlogEnum.values().clone());

        // これがdata final jum
        Map<BlogEnum, String> dataByEachBlogMap = new HashMap<>();

        // todayのList<IM>を取得(teamid雑多)
        List<IM> todayImList = imService.findBetweenDelFlg(today, to, false).stream().filter(e -> e.getTeamArr() != null && !e.getTeamArr().equals("")).collect(Collectors.toList());

        for (BlogEnum blogEnum : blogEnumList) {
            List<Long> teamIdList = Arrays.stream(TeamEnum.values()).filter(e -> e.getBlogEnumId().equals(blogEnum.getId())).map(TeamEnum::getId).collect(Collectors.toList());

            // 今日発売データをそのブログのものだけ抽出
            List<IM> todayImListOfBlog = todayImList.stream().filter(e -> StringUtilsMine.stringToLongList(e.getTeamArr()).stream().anyMatch(teamIdList::contains)).collect(Collectors.toList());

            // このブログのfutureのList<IM>を取得
            List<IM> futureImListOfBlog = new ArrayList<>();
            for (Long teamId : teamIdList) {
                List<IM> tmpList = imService.findDateAfterTeamIdLimit(to, teamId, 10L);

                // TODO: addallでいいのか？重複するのでは
                futureImListOfBlog.addAll(tmpList);
            }

            // TODO: このメソッドはarrayの中身が空な場合の対応もできてるか？
            String blogText = textController.blogUpdateReleaseItems(todayImListOfBlog, futureImListOfBlog, blogEnum.getSubDomain());

            // put on map
            dataByEachBlogMap.put(blogEnum, blogText);
        }

        // リクエスト送信
        if (dataByEachBlogMap.size() > 0) {
            for (Map.Entry<BlogEnum, String> e : dataByEachBlogMap.entrySet()) {
                HttpHeaders headersMap = generalHeaderSet(new HttpHeaders(), e.getKey());

                if (headersMap != null && !headersMap.isEmpty()) {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("content", e.getValue());
                    HttpEntity<String> request = new HttpEntity<>(jsonObject.toString(), headersMap);
                    String finalUrl = e.getKey().getSubDomain() + setting.getBlogApiPath() + "pages/" + e.getKey().getItemPageId();
                    String res = request(finalUrl, request, HttpMethod.POST, "updateReleaseItems()");
                }
            }
        }
        return "ok";
    }

    /**
     * 認証などどのリクエストでも必要なヘッダーをセットする。
     *
     * @param headers
     * @param subDomainList
     * @return サブドメイン, headersのマップ
     */
    public Map<String, HttpHeaders> generalHeaderSet(HttpHeaders headers, List<String> subDomainList) {

        Map<String, HttpHeaders> resultMap = new TreeMap<>();

        if (subDomainList == null || subDomainList.isEmpty()) {
            headers.add("Accept", MediaType.APPLICATION_JSON_VALUE);
            headers.setContentType(MediaType.APPLICATION_JSON);

            String auth = new String(Base64.getEncoder().encode(setting.getApiPw().getBytes()));
            headers.add("Authorization", "Basic " + auth);
            resultMap.put("", headers);
        } else {
            for (String subDomain : subDomainList) {
                headers.add("Accept", MediaType.APPLICATION_JSON_VALUE);
                headers.setContentType(MediaType.APPLICATION_JSON);

                String auth = "";
                if (subDomain != null) {
                    auth = new String(Base64.getEncoder().encode(BlogEnum.findBySubdomain(subDomain).getApiPw().getBytes()));
                } else {
                    auth = new String(Base64.getEncoder().encode(setting.getApiPw().getBytes()));
                }
                headers.add("Authorization", "Basic " + auth);
                resultMap.put(subDomain, headers);
            }
        }

        return resultMap;
    }

    /**
     * 認証などどのリクエストでも必要なヘッダーをセットする(第2引数がリストではなくチーム1件の場合)。
     *
     * @param headers
     * @param blogEnum
     * @return
     */
    public HttpHeaders generalHeaderSet(HttpHeaders headers, BlogEnum blogEnum) {

        headers.add("Accept", MediaType.APPLICATION_JSON_VALUE);
        headers.setContentType(MediaType.APPLICATION_JSON);

        String auth = "";

        auth = new String(Base64.getEncoder().encode(blogEnum.getApiPw().getBytes()));

        headers.add("Authorization", "Basic " + auth);

        return headers;
    }

    /**
     * リクエストを送る
     *
     * @param url
     * @param request
     * @return
     */
    public String request(String url, HttpEntity<String> request, HttpMethod method, String position) {
        logger.debug("■■■ Request() ■■■ " + position);

        String result = "";

        try {
            RestTemplate restTemplate = new RestTemplate();
            logger.debug(method + ": " + url);
            blogLog.debug(request);
            ResponseEntity<String> responseEntity = restTemplate.exchange(url, method, request, String.class);
            logger.debug("Request posted");

            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                logger.error("ブログエラー");
                e.printStackTrace();
            }
            result = responseEntity.getBody();
        } catch (Exception e) {
            blogLog.debug("***ERROR***");
            blogLog.debug(request);
            blogLog.debug("******");
            logger.debug("Request result: " + result);
            logger.debug("Post: " + url);
            logger.debug("Post: " + method);
            logger.debug("Post: " + request);
            if (e instanceof HttpClientErrorException.Forbidden) {
                throw new HttpClientErrorException(HttpStatus.FORBIDDEN);
            } else if (e instanceof HttpClientErrorException.BadRequest) {
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST);
            } else {
                logger.error("ブログエラー");
                e.printStackTrace();
            }
            result = "";
        } finally {
            blogLog.debug("---Fin---");
            return result;
        }
    }

    /**
     * ブログのマスタ商品投稿を更新する。
     * IMだけ渡されるので、その子の持つteamを確認して、
     * teamのサブドメインを確認して、サブドメインの数だけ投稿が必要
     */
    public void postOrUpdate(IM im) {
        Map<Long, Long> resMap = new TreeMap<>();

        logger.debug("postOrUpdateです。IMid：" + im.getIm_id());
        List<Long> teamIdList = StringUtilsMine.stringToLongList(im.getTeamArr());

        // どのブログにどのTeamの投稿が必要かを全てもつ
        Map<BlogEnum, List<TeamEnum>> blogEnumTeamEnumMap = new HashMap<>();

        for (Long teamId : teamIdList) {
            TeamEnum teamEnum = TeamEnum.get(teamId);
            List<TeamEnum> teamEnumList;

            if (!blogEnumTeamEnumMap.containsKey(BlogEnum.get(teamEnum.getBlogEnumId()))) {
                teamEnumList = new ArrayList<>();
            } else {
                teamEnumList = blogEnumTeamEnumMap.get(BlogEnum.get(teamEnum.getBlogEnumId()));
            }

            teamEnumList.add(teamEnum);
            blogEnumTeamEnumMap.put(BlogEnum.get(teamEnum.getBlogEnumId()), teamEnumList);
        }

        // <TagName, TeamId>
        Map<String, Long> teamNameMap = teamService.findTeamNameByIdList(teamIdList);
        String title = textController.createBlogTitle(im.getPublication_date(), im.getTitle());

        // 画像生成して投稿して画像IDゲットして、で？
        // 画像はここで生成、ポストするのはそれぞれのサイトなのでim_relが出てきてから
        String imageUrl = imageController.createImage(im.getIm_id() + ".png", textController.dateToString(im.getPublication_date()), im.getTitle(), String.join(",", teamNameMap.keySet()));

        // ひとまずcontentを作る。後でSEO対策のinner_imageを詰める（サイトごと）
        List<IM> tmpImList = new ArrayList<>();
        tmpImList.add(im);
        String content = textController.blogReleaseItemsText(tmpImList, imageUrl);

        // generalBlogの有無、対応の有無を管理(なし=1,あり・未対応==2,あり・対応済み=3)
        Integer generalBlogHandle = 1;

        // generalBlogが必要なteamが存在するか
        for (Long teamId : teamIdList) {
            if (generalBlogHandle.equals(2)) {
                break;
            }

            if (BlogEnum.get(TeamEnum.get(teamId).getBlogEnumId()).equals(BlogEnum.MAIN)) {
                generalBlogHandle = 2;
            }
        }

        List<BlogPost> blogPostList = new ArrayList<>();

        // ここからブログごとに処理。必要なところは投稿・更新する
        for (Map.Entry<BlogEnum, List<TeamEnum>> blogData : blogEnumTeamEnumMap.entrySet()) {
            BlogEnum blogEnum = blogData.getKey();
            List<TeamEnum> targetTeamEnumList = blogData.getValue();

            if (targetTeamEnumList == null || targetTeamEnumList.size() == 0) {
                continue;
            }

            // ここ、既存データ見つからない場合は新規BlogPostオブジェクト作って返す
            BlogPost blogPost = blogPostService.findByImIdBlogEnumId(im.getIm_id(), blogEnum.getId()).get(0);
            Boolean generalBlogFlg = blogEnum.equals(BlogEnum.MAIN);

            // inner_imageがまだ投稿されていない場合は投稿していく
            String imagePath = "";
            Long wpId = null;

            // 以下は更新の場合のみ
            if (blogPost.getBlog_post_id() != null) {
                wpId = blogPost.getWp_id();
                imagePath = blogPost.getInner_image();
            }

            // teamが入ってなかったら入れてあげる
            String teamIdList1 = blogPost.getTeam_arr();
            for (TeamEnum teamEnum : targetTeamEnumList) {
                teamIdList1 = StringUtilsMine.addToStringArr(teamIdList1, teamEnum.getId());
            }
            blogPost.setTeam_arr(teamIdList1);

            // memberが入ってなかったら入れてあげる
            String memArr = im.getMemArr();
            if (memArr != null && !memArr.equals("")) {
                String tmpMem = "";
                for (Long memId : StringUtilsMine.stringToLongList(memArr)) {
                    if (targetTeamEnumList.stream().anyMatch(e -> e.equals(TeamEnum.get(MemberEnum.get(memId).getTeamId())))) {
                        tmpMem = StringUtilsMine.addToStringArr(tmpMem, memId);
                    }
                }
                blogPost.setMem_arr(tmpMem);
            }

            if (blogPost.getIm_id() == null) {
                blogPost.setIm_id(im.getIm_id());
            }

            // 登録・更新どちらの場合でも、inner_imageがないなら投稿して用意
            if (imagePath == null || imagePath.isBlank()) {
                System.out.println("メディアポスト:" + imageUrl);
                Map<Integer, String> tmpMap = requestMedia(response, blogEnum, imageUrl);
                for (Map.Entry<Integer, String> elem : tmpMap.entrySet()) {
                    imagePath = elem.getValue();
                }

                System.out.println("メディアポスト完了");

                // blogPostにset inner image
                blogPost.setInner_image(imagePath);
            }

            // BlogEnumが異なるときは設定してあげる
            if (blogPost.getBlog_enum_id() == null || blogPost.getBlog_enum_id().equals("") || !blogPost.getBlog_enum_id().equals(blogEnum.getId())) {
                blogPost.setBlog_enum_id(blogEnum.getId());
            }

            // blogポストに向かう
            HttpHeaders headers = generalHeaderSet(new HttpHeaders(), blogEnum);
            content = content.replace("***INNER_IMAGE***", blogPost.getInner_image());

            if (headers != null) {

                JSONObject jsonObject = new JSONObject();
                if (setting.getTest() != null && setting.getTest().equals("dev")) {
                    jsonObject.put("title", "[dev]" + title);
                } else {
                    jsonObject.put("title", title);
                }
                jsonObject.put("author", 1);

                // カテゴリの設定
                Integer[] cat = new Integer[(1)];
                cat[0] = blogEnum.getCategoryItemId().intValue();
                jsonObject.put("categories", cat);

                // 年月を追加
                String yyyyMM = dateUtils.getYYYYMM(im.getPublication_date());

                // 年月のタグなのでそのsubdomainのgeneralなidをteamidに入れる
                System.out.println("addTagIfNotExistsを呼ぶ");
                System.out.println("1st arg: " + yyyyMM);
                BlogTag yyyyMMTag = addTagIfNotExists(yyyyMM, blogEnum, blogEnum.getId());
                if (yyyyMMTag != null && yyyyMMTag.getTag_name() != null && yyyyMMTag.getTeam_id() != null) {
                    teamNameMap.put(yyyyMMTag.getTag_name(), yyyyMMTag.getTeam_id());
                }

                // member名を追加
                if (im.getMemArr() != null && !im.getMemArr().equals("")) {
                    teamNameMap.putAll(StringUtilsMine.stringToLongList(im.getMemArr()).stream().map(MemberEnum::get).collect(Collectors.toMap(MemberEnum::getName, MemberEnum::getTeamId)));
                }

                List<Long> list = findBlogTagIdListByTagNameListTeamId(teamNameMap);
                int[] tags = new int[0];
                if (!list.isEmpty()) {
                    tags = list.stream().mapToInt(Math::toIntExact).toArray();
                }

                if (tags.length > 0) {
                    jsonObject.put("tags", tags);
                }
                if (setting.getTest() != null && setting.getTest().equals("dev")) {
                    jsonObject.put("status", "draft");
                } else {
                    jsonObject.put("status", "publish");
                }

                jsonObject.put("content", content);

                if (im.getAmazon_image() != null) {
                    JSONObject jsonObjectIn = new JSONObject();
                    String amazon = im.getAmazon_image().replaceAll("\"", "\'");
                    jsonObjectIn.put("amazon_image", amazon);
                    jsonObject.put("meta", jsonObjectIn);
                }

                HttpEntity<String> request = new HttpEntity<>(jsonObject.toString(), headers);

                // wpIdが取得できなかったら、存在しないということなのでそのサブドメインは新規投稿してあげる
                String url = "";

                boolean newPostFlg = true;
                if (wpId == null) {
                    url = blogData.getKey().getSubDomain() + setting.getBlogApiPath() + "posts/";
                } else {
                    newPostFlg = false;
                    url = blogData.getKey().getSubDomain() + setting.getBlogApiPath() + "posts/" + wpId;
                }

                // ここで投稿
                try {
                    logger.debug("ブログ投稿します:" + url + " :imId:" + im.getIm_id());
                    String res = request(url, request, HttpMethod.POST, "postOrUpdate()");
                    JSONObject jo = jsonUtils.createJsonObject(res, teamIdList.get(0), blogEnum.getId());
                    if (jo.get("id") != null) {
                        Long blogId = Long.valueOf(jo.get("id").toString().replaceAll("^\"|\"$", ""));

                        blogPost.setWp_id(blogId);
                        logger.debug("Blog posted: " + url + "\n" + content + "\n" + blogId);
                        resMap.put(im.getIm_id(), blogId);
                    }

                    // アイキャッチ
                    String eyeCatchImage = "";
                    String amazonImagePath = "";
                    if (im.getAmazon_image() != null) {
                        eyeCatchImage = textController.shapeEyeCatchAmazonImage(im.getAmazon_image());
                        if (!eyeCatchImage.equals("")) {
                            amazonImagePath = serverUtils.availablePath("amazon_" + im.getIm_id());

                            // アマゾン画像を取得しにローカル保存
                            try (InputStream in = new URL(eyeCatchImage).openStream()) {
                                Files.copy(in, Paths.get(amazonImagePath));
                            } catch (Exception ex) {
                                System.out.println("Amazon画像取得失敗のためアイキャッチ画像の設定ができません");
                                ex.printStackTrace();
                                break;
                            }
                        } else {
                            System.out.println("Amazon_imageがないのでamazon_image取得ができません");
                            System.out.println(im.getAmazon_image());
                        }
                    }

                    if (amazonImagePath.equals("")) {
                        amazonImagePath = imageUrl;
                    }

                    Integer eyeCatchId = loadMedia(amazonImagePath, blogPost);

                    if (eyeCatchId != null) {
                        blogPost.setWp_eye_catch_id(eyeCatchId);
                    }

                    blogPostList.add(blogPost);
                    // 新規ブログ投稿で未来商品の場合はTwitterポストします
                    if (newPostFlg) {
                        logger.debug("🕊ブログ投稿のお知らせ");
                        if (im.getPublication_date() != null && im.getPublication_date().after(Date.from(LocalDateTime.now().atZone(ZoneId.of("Asia/Tokyo")).toInstant()))) {
                            logger.debug(im.getTitle());
                            url = blogEnum.getSubDomain() + "blog/" + blogPost.getWp_id();

                            // amazon url
                            String a_url = "";
                            if (im.getAmazon_image() != null && !im.getAmazon_image().equals("")) {
                                a_url = StringUtilsMine.getAmazonLinkFromCard(im.getAmazon_image()).orElse("");
                            }

                            // rakuten url
                            String r_url = rakutenController.getRakutenUrl(im.getIm_id());

                            List<String> memNameList = StringUtilsMine.stringToLongList(im.getMemArr()).stream().map(f -> MemberEnum.get(f).getName()).collect(Collectors.toList());
                            List<String> teamNameList = StringUtilsMine.stringToLongList(im.getTeamArr()).stream().map(e -> TeamEnum.get(e).getMnemonic()).collect(Collectors.toList());
                            TwiDto twiDto = new TwiDto(im.getTitle(), a_url, r_url, url, im.getPublication_date(), null, teamNameList, memNameList);

                            if (r_url != null && !r_url.equals("")) {
                                twiDto.setRakuten_url(r_url);
                            }

                            twiDto.setBlog_url(url);

                            if (im.getAmazon_image() != null && !im.getAmazon_image().equals("")) {
                                twiDto.setAmazon_url(StringUtilsMine.getAmazonLinkFromCard(im.getAmazon_image()).orElse(url));
                            }

                            String result;
                            // text作成
                            result = twTextController.twitter(twiDto);
                            // Twitter投稿
                                pythonController.post(teamIdList.get(0), result);
                        } else {
                            logger.debug("❌🕊未来商品ではないので投稿なし");
                            logger.debug(im.getTitle() + "発売日：" + im.getPublication_date());
                        }
                    } else {
                        logger.debug("❌🕊ブログ更新なのでTweetはありません");
                    }
                } catch (Exception ex) {
                    logger.error("ブログエラー");
                    ex.printStackTrace();
                }
            } else {
                logger.debug("headerがエラーみたいです");
            }

            // このrelがgeneralBlogで、このrelにより処理が完了した場合、generalBlogは処理済みに設定する
            if (generalBlogFlg) {
                generalBlogHandle = 3;
            }
        }

        if (blogPostList.size() > 0) {
            blogPostService.saveAll(blogPostList);
        }

        logger.debug("postOrUpdate終わり");
//        Thread.sleep(500);
    }

    /**
     * 翌月のyyyyMMタグを追加する。
     *
     * @param blogEnum
     */
    public void addNextMonthTag(BlogEnum blogEnum) {
        // どの月でも存在する27・28日の場合、チェックに通す
        if (dateUtils.getDate() == 27 || dateUtils.getDate() == 28) {
            logger.debug("月末につき月タグ確認処理");
            // info DBのblogTagテーブルに翌月のyyyyMMタグが存在するか？
            Optional<Long> wpTagId = blogTagService.findBlogTagIdByTagName(dateUtils.getNextYYYYMM(), blogEnum.getId());
            if (wpTagId.isEmpty()) {
                String url = blogEnum.getSubDomain() + setting.getBlogApiPath() + "tags/";

                HttpHeaders headers = generalHeaderSet(new HttpHeaders(), blogEnum);
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("name", dateUtils.getNextYYYYMM());

                HttpEntity<String> request = new HttpEntity<>(jsonObject.toString(), headers);
                request(url, request, HttpMethod.POST, "addNextMonthTag()");
                logger.debug(blogEnum.getSubDomain() + ":次の月タグ追加");
            }
        }
    }

    /**
     * タグが存在しなかったらWPとDB両方に登録する
     */
    public BlogTag addTagIfNotExists(String tagName, BlogEnum blogEnum, Long teamId) {

        System.out.println("***addTagIfNotExists***");
        System.out.println(tagName);
        // slugの文字列を用意
        String slug = textController.getTagSlug(tagName);
        String url = blogEnum.getSubDomain() + setting.getBlogApiPath() + "tags?_fields[]=name&slug=" + slug;

        // request
        HttpHeaders headers = generalHeaderSet(new HttpHeaders(), blogEnum);
        JSONObject jsonObject = new JSONObject();
        HttpEntity<String> request = new HttpEntity<>(jsonObject.toString(), headers);

        // 一応まずそのタグがすでに登録されていないかチェックするリクエスト
        String res = request(url, request, HttpMethod.GET, "addTagIfNotExists()");

        BlogTag blogTag = new BlogTag();

        try {
            if (JsonUtils.isJsonArray(res)) {
                JSONArray ja = new JSONArray(res);
                // タグがまだWPになかったら登録する
                if (ja.length() == 0) {
                    blogTag = registerTag(tagName, blogEnum, teamId);
                } else {
                    // WPにタグあるがDBから見つからなかった場合、DBに登録する
                    blogTag = blogTagService.findByTagName(tagName, blogEnum.getId());

                    if (blogTag == null || blogTag.getBlog_tag_id() == null) {
                        BlogTag blogTag1 = new BlogTag();

                        // WPからDBに登録したいタグのデータを取ってくる(slugで引っ掛ける)
                        String url1 = blogEnum.getSubDomain() + setting.getBlogApiPath() + "tags?slug=" + slug + "&per_page=1";

                        // request
                        HttpHeaders headers1 = generalHeaderSet(new HttpHeaders(), blogEnum);
                        JSONObject jsonObject1 = new JSONObject();
                        HttpEntity<String> request1 = new HttpEntity<>(jsonObject1.toString(), headers1);
                        String res1 = request(url1, request1, HttpMethod.GET, "addTagIfNotExists()_2");

                        try {
                            if (JsonUtils.isJsonArray(res1)) {
                                JSONArray ja1 = new JSONArray(res1);

                                blogTag1.setTag_name(ja1.getJSONObject(0).getString("name"));
                                blogTag1.setLink(ja1.getJSONObject(0).getString("link"));
                                blogTag1.setWp_tag_id((long) ja1.getJSONObject(0).getInt("id"));

                                blogTag1.setTeam_id(blogEnum.getId());
                                blogTagService.save(blogTag1);

                                // 無事にDB登録までできたので返却するBlogTagに設定してあげる
                                blogTag = blogTag1;
                            }
                        } catch (Exception e) {
                            logger.error("ブログエラー");
                            e.printStackTrace();
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("ブログエラー");
            e.printStackTrace();
        }
        return blogTag;
    }

    /**
     * タグをWPとDBに登録します。
     *
     * @param tagName
     * @param blogEnum
     * @param teamId
     * @return
     */
    public BlogTag registerTag(String tagName, BlogEnum blogEnum, Long teamId) {
        String url = blogEnum.getSubDomain() + setting.getBlogApiPath() + "tags/";

        HttpHeaders h = generalHeaderSet(new HttpHeaders(), blogEnum);
        JSONObject jo = new JSONObject();
        jo.put("name", tagName);

        HttpEntity<String> request = new HttpEntity<>(jo.toString(), h);
        String res = request(url, request, HttpMethod.POST, "registerTag()");

        JSONObject jsonObject1 = jsonUtils.createJsonObject(res, teamId, blogEnum.getId());

        int tagId;
        if (jsonObject1.has("id") && jsonObject1.get("id") != null) {
            tagId = jsonObject1.getInt("id");
            String link = jsonObject1.getString("link").replaceAll("^\"|\"$", "");
            BlogTag blogTag = new BlogTag();
            blogTag.setTag_name(tagName);
            blogTag.setWp_tag_id((long) tagId);
            blogTag.setLink(link);

            blogTag.setTeam_id(teamId);
            return blogTagService.save(blogTag);
        }
        return new BlogTag();
    }

    /**
     * TV番組の固定ページを更新(送信先ブログごとにまとめる)
     */
    public void updateTvPage() throws ParseException {
        // subDomainごとにまとめられたので、それぞれのドメインごとにテキストを作ってあげる
        Map<BlogEnum, String> resultMap = new TreeMap<>();

        // 該当期間内の番組を全て取得
//        List<PM> tmpList = pmService.findByOnAirDateNotDeleted(dateUtils.daysAfterToday(0), dateUtils.daysAfterToday(6));
//        List<Long> pmIdList = tmpList.stream().map(PM::getPm_id).distinct().collect(Collectors.toList());
        List<PM> pmList = pmService.findByOnAirDateNotDeleted(dateUtils.daysAfterToday(0), dateUtils.daysAfterToday(6));

        // どのブログにどのTeamの投稿が必要かを全てもつ
        for (TeamEnum teamEnum : TeamEnum.values()) {
            BlogEnum blogEnum = BlogEnum.get(teamEnum.getBlogEnumId());

            List<PM> teamPmList = pmList.stream().filter(e -> StringUtilsMine.stringToLongList(e.getTeamArr()).contains(teamEnum.getId())).collect(Collectors.toList());
            String text = textController.tvPageText(teamPmList, blogEnum.getSubDomain());
            resultMap.put(blogEnum, text);
        }

        // テキストを用意できた時だけページを更新する
        // 各サブドメインがpostされたかチェックつけるMap<Subdomain, T/F>
        Map<BlogEnum, Boolean> postChkMap = new TreeMap<>();
        Arrays.stream(BlogEnum.values()).forEach(f -> postChkMap.put(f, false));

        if (resultMap.size() > 0) {
            for (Map.Entry<BlogEnum, String> e : resultMap.entrySet()) {
                BlogEnum blogEnum = e.getKey();
                String subDomain = e.getKey().getSubDomain();

                String url = subDomain + setting.getBlogApiPath() + "pages/" + blogEnum.getTvPageId();
                HttpHeaders headers = generalHeaderSet(new HttpHeaders(), blogEnum);
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("content", e.getValue());
                HttpEntity<String> request = new HttpEntity<>(jsonObject.toString(), headers);
                request(url, request, HttpMethod.POST, "updateTvPage()_1");
                postChkMap.put(blogEnum, true);
            }
        }

        // postされていないsubdomainが1つ以上あれば
        if (postChkMap.entrySet().stream().anyMatch(e -> e.getValue().equals(false))) {
            for (Map.Entry<BlogEnum, Boolean> e : postChkMap.entrySet()) {
                if (e.getValue().equals(false)) {
                    String url = e.getKey().getSubDomain() + setting.getBlogApiPath() + "pages/" + e.getKey().getTvPageId();

                    HttpHeaders headers = generalHeaderSet(new HttpHeaders(), e.getKey());
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("content", "<h2>１週間以内のTV情報はありません</h2>");
                    HttpEntity<String> request = new HttpEntity<>(jsonObject.toString(), headers);
                    request(url, request, HttpMethod.POST, "updateTvPage()_2");
                    postChkMap.put(e.getKey(), true);
                }
            }
        }
    }

    /**
     * 明日の1日の予定の投稿をポストします
     */
    public void createDailySchedulePost(BlogEnum blogEnum) {

        Date today = dateUtils.getToday();
        Calendar c = Calendar.getInstance();
        c.setTime(today);
        c.add(Calendar.DATE, 1);
        Date tmrw = c.getTime();

        String title = textController.createDailyScheduleTitle(tmrw);

        // チームIDリスト
        List<Long> teamIdList = Arrays.stream(TeamEnum.values()).filter(e -> e.getBlogEnumId().equals(blogEnum.getId())).map(TeamEnum::getId).collect(Collectors.toList());

        // memberList(のちループで詰める。variableだけ宣言)
        List<Long> memIdList = new ArrayList<>();

        // コンテンツ文章の作成
        List<String> tmpList = new ArrayList<>();

        for (Long teamId : teamIdList) {
            // 明日の日付で、テレビ一覧画面を作る
            List<PM> plist = pmService.findByOnAirDateNotDeletedTeamId(tmrw, teamId);

            // 明日の日付で、商品一覧画面を作る
            List<IM> imList = imService.findByTeamIdDate(teamId, tmrw);
            Map<IM, List<ImVer>> imMap = new TreeMap<>();
            for (IM im : imList) {
                List<ImVer> verList = imVerService.findByImId(im.getIm_id());
                imMap.put(im, verList);

                // memIdListにメンバーなかったら詰める
                List<Long> tmpMemIdList = StringUtilsMine.stringToLongList(im.getMemArr());
                if (tmpMemIdList != null && tmpMemIdList.size() > 0) {
                    for (Long memId : tmpMemIdList) {
                        if (!memIdList.contains(memId)) {
                            memIdList.add(memId);
                        }
                    }
                }
            }

            Map<String, Boolean> tmpMap = textController.createDailySchedulePost(teamId, tmrw, imMap, plist);
            for (Map.Entry<String, Boolean> e : tmpMap.entrySet()) {
                tmpList.add(e.getKey());
            }
        }

        String content = String.join("\n", tmpList);

        // post
        String url = blogEnum.getSubDomain() + setting.getBlogApiPath() + "posts/";
        HttpHeaders headers = generalHeaderSet(new HttpHeaders(), blogEnum);

        JSONObject jsonObject = new JSONObject();

        if (setting.getTest() != null && setting.getTest().equals("dev")) {
            jsonObject.put("title", "[dev]" + title);
        } else {
            jsonObject.put("title", title);
        }
        jsonObject.put("author", 1);

        Integer[] cat = new Integer[(1)];
        cat[0] = blogEnum.getDailyScheCategoryId().intValue();

        // dailyScheduleCategoryIdをカテゴリに入れてあげる
        jsonObject.put("categories", cat);

        // タグ名を詰める
        Map<String, Long> tagNameMap = new HashMap<>();
        if (teamIdList.size() > 0) {
            tagNameMap.putAll(teamService.findTeamNameByIdList(teamIdList));
        }

        // 年月を追加/teamIdはそのsubdomainのgeneralなIDを入れる。
        String yyyyMM = dateUtils.getYYYYMM(tmrw);
        System.out.println("addTagIfNotExistsを呼ぶ");
        System.out.println("1st arg: " + yyyyMM);
        BlogTag yyyyMMTag = addTagIfNotExists(yyyyMM, blogEnum, blogEnum.getId());
        if (yyyyMMTag != null && yyyyMMTag.getTag_name() != null && yyyyMMTag.getTeam_id() != null) {
            tagNameMap.put(yyyyMMTag.getTag_name(), yyyyMMTag.getTeam_id());
        }

        // member名を追加
        if (memIdList.size() > 0) {
            tagNameMap.putAll(memIdList.stream().map(MemberEnum::get).collect(Collectors.toMap(MemberEnum::getName, MemberEnum::getTeamId)));
        }

        List<Long> list = findBlogTagIdListByTagNameListTeamId(tagNameMap);
        int[] tags = new int[0];
        if (!list.isEmpty()) {
            tags = list.stream().mapToInt(Math::toIntExact).toArray();
        }

        if (tags.length > 0) {
            jsonObject.put("tags", tags);
        }

        if (setting.getTest() != null && setting.getTest().equals("dev")) {
            jsonObject.put("status", "draft");
        } else {
            jsonObject.put("status", "publish");
        }

        jsonObject.put("content", content);

        HttpEntity<String> request = new HttpEntity<>(jsonObject.toString(), headers);
        String res = request(url, request, HttpMethod.POST, "createDailySchedulePost()");

        JSONObject jo = jsonUtils.createJsonObject(res, teamIdList.get(0), blogEnum.getId());
        if (jo.get("id") != null) {
            Long blogId = Long.valueOf(jo.get("id").toString().replaceAll("^\"|\"$", ""));
            logger.debug("Blog posted: " + url + "\n" + content + "\n" + blogId);
        }
    }

    /**
     * 画像をWordPressにポストします。
     *
     * @param response
     * @param imageUrl
     * @return Map<画像ID, 画像path>
     */
    public Map<Integer, String> requestMedia(HttpServletResponse response, BlogEnum blogEnum, String imageUrl) {
        String finalUrl = blogEnum.getSubDomain() + setting.getBlogApiPath() + "media";

        imageUrl = imageUrl.replaceAll("\\?.*$", "");

        response.setHeader("Cache-Control", "no-cache");
        HttpHeaders headers = generalHeaderSet(new HttpHeaders(), blogEnum);
        headers.add("content-disposition", "attachment; filename=" + imageUrl + ".png");
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

        body.add("file", new FileSystemResource(imageUrl));
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        System.out.println("画像投稿します");
        System.out.println(imageUrl);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> responseEntity = restTemplate.postForEntity(finalUrl, requestEntity, String.class);
        String text = responseEntity.getBody();
        System.out.println("request result: " + text);
        JSONObject jsonObject = new JSONObject(text);
        Map<Integer, String> res;
        if (jsonObject.get("id") != null) {
            Integer imageId = Integer.parseInt(jsonObject.get("id").toString().replaceAll("^\"|\"$", ""));
            String imagePath = jsonObject.get("source_url").toString().replaceAll("^\"|\"$", "");
            res = Collections.singletonMap(imageId, imagePath);
        } else {
            res = Collections.singletonMap(null, null);
        }
        return res;
    }

    /**
     * stringリストからDB検索、データあればそのまま返し、存在しなかったらwpにあるか確認しなければ登録、その後dbにもデータ登録し返却
     *
     * @param tagNameTeamIdMap
     * @return
     */
    public List<Long> findBlogTagIdListByTagNameListTeamId(Map<String, Long> tagNameTeamIdMap) {
        List<Long> tagIdList = new ArrayList<>();

        for (Map.Entry<String, Long> e : tagNameTeamIdMap.entrySet()) {

            Long teamId = e.getValue();
            Long finalTeamId = teamId;
            if (Arrays.stream(BlogEnum.values()).noneMatch(f -> f.getId().equals(finalTeamId))) {
                teamId = BlogEnum.MAIN.getId();
            }

            String name = e.getKey().replaceAll(" ", "");

            Optional<Long> tagIdResult = blogTagService.findBlogTagIdByTagName(name, teamId);

            Long tagId = null;
            if (tagIdResult.isPresent()) {
                tagId = tagIdResult.get();
            } else {
                // タグが見つからなかった場合、WPブログに登録したり引っ張ってきてDBに保存したり
                BlogEnum blogEnum = BlogEnum.get(teamId);
                System.out.println("addTagIfNotExistsを呼ぶ");
                System.out.println("1st arg: " + name);
                BlogTag tag = addTagIfNotExists(name, blogEnum, teamId);
                tagId = tag.getBlog_tag_id();
            }

            if (tagId != null) {
                tagIdList.add(tagId);
            }
        }
        return tagIdList;
    }

    /**
     * wordpressに画像をポストし、投稿アイキャッチにも設定します
     *
     * @param imageUrl 　postする画像パス
     */
    public Integer loadMedia(String imageUrl, BlogPost blogPost) {
        Integer featuredMedia = null;

        if (StringUtils.hasText(imageUrl)) {
            Long wpId = blogPost.getWp_id();
            BlogEnum blogEnum = BlogEnum.get(blogPost.getBlog_enum_id());
            System.out.println("メディアポスト:" + imageUrl);
            Map<Integer, String> map = requestMedia(response, blogEnum, imageUrl);
            System.out.println("ポスト完了");

            // 無事アップロードできてたらブログ投稿にアイキャッチを設定してあげる
            Integer imageId = null;
            for (Map.Entry<Integer, String> elem : map.entrySet()) {
                imageId = elem.getKey();
            }
            String res = setMedia(wpId, imageId, blogEnum);
            featuredMedia = extractMedia(res);
        }
        return featuredMedia;
    }

    /**
     * 投稿にアイキャッチメディアを設定し、更新します。
     *
     * @param wpId
     * @param imageId
     */
    private String setMedia(Long wpId, Integer imageId, BlogEnum blogEnum) {
        String url = blogEnum.getSubDomain() + setting.getBlogApiPath() + "posts/" + wpId;

        HttpHeaders headers = generalHeaderSet(new HttpHeaders(), blogEnum);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("featured_media", imageId);

        HttpEntity<String> request = new HttpEntity<>(jsonObject.toString(), headers);
        return request(url, request, HttpMethod.POST, "setMedia()");
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
     * 既存IMでまだアマゾンアイキャッチない商品について、アイキャッチをセットするtmp method
     * 成功か失敗かを返す
     *
     * @param im
     * @return
     */
    public boolean tmpEyeCatchAmazonSet(IM im) {
        // アイキャッチ
        String eyeCatchImage = "";
        String amazonImagePath = "";
        if (im.getAmazon_image() != null) {
            eyeCatchImage = textController.shapeEyeCatchAmazonImage(im.getAmazon_image());
            if (!eyeCatchImage.equals("")) {
                amazonImagePath = serverUtils.availablePath("amazon_" + im.getIm_id());

                // アマゾン画像を取得しにローカル保存
                try (InputStream in = new URL(eyeCatchImage).openStream()) {
                    Files.copy(in, Paths.get(amazonImagePath));
                } catch (Exception ex) {
                    System.out.println("Amazon画像取得失敗のためアイキャッチ画像の設定ができません");
                    ex.printStackTrace();
                    return false;
                }
            } else {
                System.out.println("Amazon_imageがないのでamazon_image取得ができません");
                System.out.println(im.getAmazon_image());
                return false;
            }
        } else {
            return false;
        }

        if (!amazonImagePath.equals("")) {
            List<BlogPost> blogPostList = blogPostService.findByImId(im.getIm_id());
            List<BlogPost> updateList = new ArrayList<>();
            for (BlogPost blogPost : blogPostList) {
                Integer featuredId = loadMedia(amazonImagePath, blogPost);
                blogPost.setWp_eye_catch_id(featuredId);
                updateList.add(blogPost);
            }

            if (updateList.size() > 0) {
                blogPostService.saveAll(updateList);
            }
        } else {
            return false;
        }
        return true;
    }
}

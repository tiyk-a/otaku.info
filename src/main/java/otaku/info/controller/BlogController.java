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
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.List;
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
    IMService imService;

    @Autowired
    ImVerService imVerService;

    @Autowired
    BlogTagService blogTagService;

    @Autowired
    TeamService teamService;

    @Autowired
    MemberService memberService;

    @Autowired
    IRelService iRelService;

    @Autowired
    IMRelMemService imRelMemService;

    @Autowired
    IMRelService iMRelService;

    @Autowired
    PRelService pRelService;

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
     * 引数TeamEnumのブログにあるタグがDBになかったらDBにデータを入れます
     *
     * @param blogEnum
     */
    public void insertTags(BlogEnum blogEnum, Long teamId) {
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

                    for (int i=0;i<ja.length();i++) {
                        Integer wpId = ja.getJSONObject(i).getInt("id");
                        String tagName = ja.getJSONObject(i).getString("name").replaceAll("^\"|\"$", "");
                        String link = ja.getJSONObject(i).getString("link").replaceAll("^\"|\"$", "");

                        BlogTag blogTag = new BlogTag();
                        blogTag.setWp_tag_id((long)wpId);
                        blogTag.setTag_name(tagName);
                        blogTag.setLink(link);
                        blogTag.setTeam_id(blogEnum.getId());
                        blogTagList.add(blogTag);
                    }
                    blogTagService.saveAll(blogTagList);
                }
            } catch (Exception ex) {
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

        // 今日発売マスター商品(teamIdがNullのマスターは削除)
        List<IM> itemMasterList = imService.findBetweenDelFlg(today, to, false).stream().filter(e -> iMRelService.findTeamIdListByItemMId(e.getIm_id()).size() > 0).collect(Collectors.toList());

        // subDomainごとにまとめる
        Map<String, Map<IM, List<Item>>> teamIdItemMasterItemMap = new TreeMap<>();
        List<String> subDomainList = BlogEnum.getAllSubdomain();
        for (String s : subDomainList) {
            teamIdItemMasterItemMap.put(s, new TreeMap<>());
        }

        for (IM itemMaster : itemMasterList) {
            // itemMasterとitemListは用意できた
            List<Item> itemList = itemService.findByMasterId(itemMaster.getIm_id());
            List<Long> itemIdList = itemList.stream().map(Item::getItem_id).collect(Collectors.toList());
            List<String> subDomainList1 = iRelService.findByItemIdList(itemIdList).stream().map(e -> BlogEnum.get(TeamEnum.get(e.getTeam_id()).getBlogEnumId()).getSubDomain()).distinct().collect(Collectors.toList());

            for (String subDomain : subDomainList1) {
                Map<IM, List<Item>> tmpMap1 = teamIdItemMasterItemMap.get(subDomain);
                if (tmpMap1.containsKey(itemMaster)) {
                    List<Item> tmpList = tmpMap1.get(itemMaster);
                    // itemListからdiffを見つけてそれだけを追加してあげる
                    List<Item> diffList = new ArrayList<>();
                    for (Item i : tmpList) {
                        if (!diffList.contains(i)) {
                            diffList.add(i);
                        }
                    }

                    if (diffList.size() > 0) {
                        tmpList.addAll(diffList);
                    }
                    tmpMap1.put(itemMaster, tmpList);
                } else {
                    tmpMap1.put(itemMaster, itemList);
                }
                teamIdItemMasterItemMap.put(subDomain, tmpMap1);
            }
        }

        // 明日以降発売マスター商品(クエリがうまくできなくてチームごとに取りに行ってる😭
        List<IM> tmpList = new ArrayList<>();
        for (TeamEnum e : TeamEnum.values()) {
            tmpList.addAll(imService.findDateAfterTeamIdLimit(to, e.getId(), 10L));
        }
        List<IM> futureItemMasterList = tmpList.stream().distinct().collect(Collectors.toList());

        // subDomainごとにまとめる
        Map<String, Map<IM, List<Item>>> teamIdItemMasterItemFutureMap = new TreeMap<>();
        for (String s : subDomainList) {
            teamIdItemMasterItemFutureMap.put(s, new TreeMap<>());
        }

        for (IM itemMaster : futureItemMasterList) {
            // itemMasterとitemListは用意できた
            List<Item> itemList = itemService.findByMasterId(itemMaster.getIm_id());
            List<Long> itemIdList = itemList.stream().map(Item::getItem_id).collect(Collectors.toList());
            List<String> subDomainList1 = iRelService.findByItemIdList(itemIdList).stream().map(e -> BlogEnum.get(TeamEnum.get(e.getTeam_id()).getBlogEnumId()).getSubDomain()).distinct().collect(Collectors.toList());

            for (String subDomain : subDomainList1) {
                Map<IM, List<Item>> tmpMap1 = teamIdItemMasterItemFutureMap.get(subDomain);
                if (tmpMap1.containsKey(itemMaster)) {
                    List<Item> tmpList1 = tmpMap1.get(itemMaster);
                    // itemListからdiffを見つけてそれだけを追加してあげる
                    List<Item> diffList = new ArrayList<>();
                    for (Item i : tmpList1) {
                        if (!diffList.contains(i)) {
                            diffList.add(i);
                        }
                    }

                    if (diffList.size() > 0) {
                        tmpList1.addAll(diffList);
                    }
                    tmpMap1.put(itemMaster, tmpList1);
                } else {
                    tmpMap1.put(itemMaster, itemList);
                }
                teamIdItemMasterItemFutureMap.put(subDomain, tmpMap1);
            }
        }

        // ここまでで、明日と先１週間に発売される商品のMapは完成した
        // MapをsubDomainでまとめ、それぞれテキストを生成、それぞれrequest送信する
        Map<String, String> requestMap = new TreeMap<>();

        for (BlogEnum e : BlogEnum.values()) {
            if (!requestMap.containsKey(e.getSubDomain())) {
                requestMap.put(e.getSubDomain(), "先１週間の新発売情報はありません");
            }
        }

        String blogText = "";
        if (teamIdItemMasterItemMap.size() > 0) {
            // <teamId, blogText>
            for (Map.Entry<String, Map<IM, List<Item>>> e : teamIdItemMasterItemMap.entrySet()) {
                // 明日のリストはあるが未来のリストがそもそもない→明日のだけでテキスト作る
                if (teamIdItemMasterItemFutureMap.size() == 0) {
                    blogText = textController.blogUpdateReleaseItems(e.getValue(), null, e.getKey());
                } else {
                    // 明日のリストと未来のリスト両方あるor明日のリストはあるが未来のリスト（同じteamId）がない
                    blogText = textController.blogUpdateReleaseItems(e.getValue(), teamIdItemMasterItemFutureMap.getOrDefault(e.getKey(), null), e.getKey());
                }
                requestMap.put(e.getKey(), blogText);
            }
        } else if (teamIdItemMasterItemFutureMap.size() > 0) {
            // 明日の発売商品がないがその先１週間はある場合
            for (Map.Entry<String, Map<IM, List<Item>>> e : teamIdItemMasterItemFutureMap.entrySet()) {
                blogText = textController.blogUpdateReleaseItems(null, e.getValue(), e.getKey());
                requestMap.put(e.getKey(), blogText);
            }
        }

        // リクエスト送信
        if (requestMap.size() > 0) {
            for (Map.Entry<String, String> e : requestMap.entrySet()) {
                BlogEnum blogEnum = BlogEnum.findBySubdomain(e.getKey());
                HttpHeaders headersMap = generalHeaderSet(new HttpHeaders(), blogEnum);

                if (headersMap != null && !headersMap.isEmpty()) {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("content", blogText);
                    HttpEntity<String> request = new HttpEntity<>(jsonObject.toString(), headersMap);
                    String finalUrl = e.getKey() + setting.getBlogApiPath() + "pages/" + blogEnum.getItemPageId();
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

        Map<String ,HttpHeaders> resultMap = new TreeMap<>();

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
            logger.debug("Post: " + url);
            blogLog.debug(request);
            ResponseEntity<String> responseEntity = restTemplate.exchange(url, method, request, String.class);
            logger.debug("Request posted");
            
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
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
                e.printStackTrace();
            }
            result = "";
        }
        blogLog.debug("---SUCCESS---");
        return result;
    }

    /**
     * ブログのマスタ商品投稿を更新する。
     * imId, wpIdのマップを返します。
     *
     */
    public Map<Long, Long> postOrUpdate(IM itemMaster) throws InterruptedException {
        Map<Long, Long> resMap = new TreeMap<>();

        logger.debug("postOrUpdateです。IMid：" + itemMaster.getIm_id());
        List<Item> itemList = itemService.findByMasterId(itemMaster.getIm_id());
        List<IMRel> relList = iMRelService.findByItemMId(itemMaster.getIm_id());
        List<IMRelMem> memList = imRelMemService.findByImRelIdListNotDeleted(relList.stream().map(e -> e.getIm_rel_id()).collect(Collectors.toList()));
        List<Long> teamIdList = relList.stream().map(IMRel::getTeam_id).collect(Collectors.toList());
        // <TagName, TeamId>
        Map<String, Long> teamNameMap = teamService.findTeamNameByIdList(teamIdList);
        String title = textController.createBlogTitle(itemMaster.getPublication_date(), itemMaster.getTitle());

        // 画像生成して投稿して画像IDゲットして、で？
        // 画像はここで生成、ポストするのはそれぞれのサイトなのでim_relが出てきてから
        String imageUrl = imageController.createImage(itemMaster.getIm_id() + ".png", textController.dateToString(itemMaster.getPublication_date()), String.join(",", teamNameMap.keySet()));

        // ひとまずcontentを作る。後でSEO対策のinner_imageを詰める（サイトごと）
        String content = textController.blogReleaseItemsText(Collections.singletonMap(itemMaster, itemList), null);

        // generalBlogの有無、対応の有無を管理(なし=1,あり・未対応==2,あり・対応済み=3)
        Integer generalBlogHandle = 1;

        // generalBlogがimrelの中にあるか
        for (Long teamId : teamIdList) {
            if (generalBlogHandle.equals(2)) {
                break;
            }

            if (BlogEnum.get(TeamEnum.get(teamId).getBlogEnumId()).equals(BlogEnum.MAIN)) {
                generalBlogHandle = 2;
            }
        }

        // ここからimrelごと(=ブログごと)に処理。必要なところは投稿・更新する
        for (IMRel rel : relList) {

            // このrelがgeneralBlogなのかフラグ
            Boolean generalBlogFlg = BlogEnum.get(TeamEnum.get(rel.getTeam_id()).getBlogEnumId()).equals(BlogEnum.MAIN);

            // このrelがgeneralBlogで、他のgeneralBlogのrelにより処理が完了していたら飛ばす
            if (generalBlogFlg && generalBlogHandle.equals(3)) {
                continue;
            }

            Long wpId = rel.getWp_id();
            Long teamId = rel.getTeam_id();

            // inner_imageがまだ投稿されていない場合は投稿していく
            String imagePath = "";
            if (rel.getInner_image() == null || rel.getInner_image().isBlank()) {
                System.out.println("メディアポスト:" + imageUrl);
                Map<Integer, String> tmpMap = requestMedia(response, teamId, imageUrl);
                for (Map.Entry<Integer, String> elem : tmpMap.entrySet()) {
                    imagePath = elem.getValue();
                }

                System.out.println("メディアポスト完了");

                // imrelを更新する
                rel.setInner_image(imagePath);
                iMRelService.save(rel);
            }

            BlogEnum blogEnum = BlogEnum.get(TeamEnum.get(teamId).getBlogEnumId());

            // blogポストに向かう
            HttpHeaders headers = generalHeaderSet(new HttpHeaders(), blogEnum);
            content = content.replace("***INNER_IMAGE***", rel.getInner_image());

            if (headers != null) {

                JSONObject jsonObject = new JSONObject();
                if (setting.getTest()!= null && setting.getTest().equals("dev")) {
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
                String yyyyMM = dateUtils.getYYYYMM(itemMaster.getPublication_date());

                // 年月のタグなのでそのsubdomainのgeneralなidをteamidに入れる
                BlogTag yyyyMMTag = addTagIfNotExists(yyyyMM, blogEnum.getSubDomain(), blogEnum.getId());
                teamNameMap.put(yyyyMMTag.getTag_name(), yyyyMMTag.getTeam_id());

                // member名を追加
                if (memList != null && memList.size() > 0) {
                    teamNameMap.putAll(memList.stream().map(e -> MemberEnum.get(e.getMember_id())).collect(Collectors.toMap(MemberEnum::getName, MemberEnum::getTeamId)));
                }

                List<Long> list = findBlogTagIdListByTagNameListTeamId(teamNameMap);
                int[] tags = new int[0];
                if (!list.isEmpty()) {
                    tags = list.stream().mapToInt(Math::toIntExact).toArray();
                }

                if (tags.length > 0) {
                    jsonObject.put("tags", tags);
                }
                if (setting.getTest()!= null && setting.getTest().equals("dev")) {
                    jsonObject.put("status", "draft");
                } else {
                    jsonObject.put("status", "publish");
                }

                jsonObject.put("content", content);

                if (itemMaster.getAmazon_image() != null) {
                    JSONObject jsonObjectIn = new JSONObject();
                    jsonObjectIn.put("amazon_image", itemMaster.getAmazon_image());
                    jsonObject.put("meta", jsonObjectIn);
                }

                HttpEntity<String> request = new HttpEntity<>(jsonObject.toString(), headers);

                // wpIdが取得できなかったら、存在しないということなのでそのサブドメインは新規投稿してあげる
                String url = "";
                TeamEnum e = TeamEnum.get(teamId);
                boolean newPostFlg = true;
                if (wpId == null) {
                    url = blogEnum.getSubDomain() + setting.getBlogApiPath() + "posts/";
                } else {
                    newPostFlg = false;
                    url = blogEnum.getSubDomain() + setting.getBlogApiPath() + "posts/" + wpId;
                }

                // ここで投稿
                try {
                    logger.debug("ブログ投稿します:" + url + " :imId:" + itemMaster.getIm_id());
                    String res = request(url, request, HttpMethod.POST, "postOrUpdate()");
                    JSONObject jo = jsonUtils.createJsonObject(res, teamId);
                    if (jo.get("id") != null) {
                        Long blogId = Long.valueOf(jo.get("id").toString().replaceAll("^\"|\"$", ""));

                        if (generalBlogFlg) {
                            for (IMRel iMrel : relList) {
                                if (!blogId.equals(iMrel.getWp_id())) {
                                    iMrel.setWp_id(blogId);
                                    iMRelService.save(iMrel);
                                }
                            }
                        } else {
                            rel.setWp_id(blogId);
                        }

                        iMRelService.save(rel);
                        logger.debug("Blog posted: " + url + "\n" + content + "\n" + blogId);
                        resMap.put(itemMaster.getIm_id(), blogId);
                    }

                    // 新規ブログ投稿で未来商品の場合はTwitterポストします
                    if (newPostFlg) {
                        logger.debug("🕊ブログ投稿のお知らせ");
                        if (itemMaster.getPublication_date() != null && itemMaster.getPublication_date().after(Date.from(LocalDateTime.now().atZone(ZoneId.of("Asia/Tokyo")).toInstant()))) {
                            logger.debug(itemMaster.getTitle());
                            url = blogEnum.getSubDomain() + "blog/" + rel.getWp_id();
                            List<String> memNameList = memList.stream().filter(g -> g.getIm_rel_id().equals(rel.getIm_rel_id())).map(f -> memberService.getMemberName(f.getMember_id())).collect(Collectors.toList());
                            TwiDto twiDto = new TwiDto(itemMaster.getTitle(), url, itemMaster.getPublication_date(), null, teamId, memNameList);
                            // TEST:temporaryアマゾンImageがあればそれを入れてあげるようにする
                            if (itemMaster.getAmazon_image() != null) {
                                twiDto.setUrl(stringUtilsMine.getAmazonLinkFromCard(itemMaster.getAmazon_image()).orElse(url));
                            }

                            String result;
                            // text作成
                            result = twTextController.twitter(twiDto);
                            // Twitter投稿
                            pythonController.post(teamId, result);
                        } else {
                            logger.debug("❌🕊未来商品ではないので投稿なし");
                            logger.debug(itemMaster.getTitle() + "発売日：" + itemMaster.getPublication_date());
                        }
                    } else {
                        logger.debug("❌🕊ブログ更新なのでTweetはありません");
                    }
                } catch (Exception ex) {
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

        logger.debug("postOrUpdate終わり");
        Thread.sleep(500);
        return resMap;
    }

    /**
     * 翌月のyyyyMMタグを追加する。
     *
     */
    public void addNextMonthTag(String subDomain) {
        // どの月でも存在する27・28日の場合、チェックに通す
        if (dateUtils.getDate() == 27 || dateUtils.getDate() == 28) {
            logger.debug("月末につき月タグ確認処理");
            // info DBのblogTagテーブルに翌月のyyyyMMタグが存在するか？
            BlogEnum blogEnum = BlogEnum.findBySubdomain(subDomain);
            Optional<Long> wpTagId = blogTagService.findBlogTagIdByTagName(dateUtils.getNextYYYYMM(), blogEnum.getId());
            if (wpTagId.isEmpty()) {
                String url = subDomain + setting.getBlogApiPath() + "tags/";

                HttpHeaders headers = generalHeaderSet(new HttpHeaders(), blogEnum);
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("name", dateUtils.getNextYYYYMM());

                HttpEntity<String> request = new HttpEntity<>(jsonObject.toString(), headers);
                request(url, request, HttpMethod.POST, "addNextMonthTag()");
                logger.debug(subDomain + ":次の月タグ追加");
            }
        }
    }

    /**
     * タグが存在しなかったらWPとDB両方に登録する
     *
     */
    public BlogTag addTagIfNotExists(String tagName, String subDomain, Long teamId) {

        String url = subDomain + setting.getBlogApiPath() + "tags?_fields[]=name&slug=" + tagName;

        BlogEnum blogEnum = BlogEnum.findBySubdomain(subDomain);
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
                    blogTag = registerTag(tagName, subDomain, teamId);
                } else {
                    // WPにタグあるがDBから見つからなかった場合、DBに登録する
                    blogTag = blogTagService.findByTagName(tagName, blogEnum.getId());

                    if (blogTag == null || blogTag.getBlog_tag_id() == null) {
                        BlogTag blogTag1 = new BlogTag();

                        // WPからDBに登録したいタグのデータを取ってくる
                        String url1 = subDomain + setting.getBlogApiPath() + "tags?slug=" + tagName + "&per_page=1";

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
     * タグをWPとDBに登録します。
     *
     * @param tagName
     * @param subDomain
     * @return
     */
    public BlogTag registerTag(String tagName, String subDomain, Long teamId) {
        String url = subDomain + setting.getBlogApiPath() + "tags/";

        BlogEnum blogEnum = BlogEnum.findBySubdomain(subDomain);
        HttpHeaders h = generalHeaderSet(new HttpHeaders(), blogEnum);
        JSONObject jo = new JSONObject();
        jo.put("name", tagName);

        HttpEntity<String> request = new HttpEntity<>(jo.toString(), h);
        String res = request(url, request, HttpMethod.POST, "registerTag()");

        JSONObject jsonObject1 = jsonUtils.createJsonObject(res, teamId);

        int tagId;
        if (jsonObject1.get("id") != null) {
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
        // 該当期間内の番組を全て取得
        List<Program> tmpList = programService.findByOnAirDateBeterrn(dateUtils.daysAfterToday(0), dateUtils.daysAfterToday(6));

        // 複数Teamがひもづく場合はそれぞれ投稿するため、Mapにする<ProgramId_TeamId, Program>
        Map<String, Program> confirmedMap = new TreeMap<>();
        if (tmpList.size() > 0) {
            for (Program p : tmpList) {
                List<Long> teamIdList = pRelService.getTeamIdList(p.getProgram_id());
                if (teamIdList != null && !teamIdList.isEmpty()) {
                    for (Long teamId : teamIdList) {
                        if (teamId == 0) {
                            continue;
                        }
                        // Mapにする<ProgramId_TeamId, Program>
                        confirmedMap.put(p.getProgram_id() + "_" + teamId, p);
                    }
                }
            }
        }

        // 1件以上データが見つかったら
        if (confirmedMap.size() > 0) {
            // subDomainでまとめるMap<Subdomain, Map<ProgramId_TeamId, Program>>
            Map<String, Map<String, Program>> domainMap = new TreeMap<>();
            for (Map.Entry<String, Program> e : confirmedMap.entrySet()) {
                Long teamId = Long.valueOf(e.getKey().replaceAll("^\\d*_", ""));
                String subDomain = BlogEnum.get(TeamEnum.get(teamId).getBlogEnumId()).getSubDomain();

                Map<String, Program> tmpMap;
                if (domainMap.containsKey(subDomain)) {
                    tmpMap = domainMap.get(subDomain);
                } else {
                    tmpMap = new TreeMap<>();
                }
                tmpMap.put(e.getKey(), e.getValue());
                domainMap.put(subDomain, tmpMap);
            }

            // subDomainごとにまとめられたので、それぞれのドメインごとにテキストを作ってあげる
            Map<String, String> resultMap = new TreeMap<>();
            if (domainMap.size() > 0) {
                for (Map.Entry<String, Map<String, Program>> e : domainMap.entrySet()) {
                    List<Program> pList = e.getValue().entrySet().stream().map(f -> f.getValue()).collect(Collectors.toList());
                    String text = textController.tvPageText(pList, e.getKey());
                    resultMap.put(e.getKey(), text);
                }
            }

            // テキストを用意できた時だけページを更新する
            // 各サブドメインがpostされたかチェックつけるMap<Subdomain, T/F>
            Map<String, Boolean> postChkMap = new TreeMap<>();
            BlogEnum.getAllSubdomain().forEach(e -> postChkMap.put(e, false));

            if (resultMap.size() > 0) {
                for (Map.Entry<String, String> e : resultMap.entrySet()) {
                    String subDomain = e.getKey();
                    BlogEnum blogEnum = BlogEnum.findBySubdomain(subDomain);
                    String url = subDomain + setting.getBlogApiPath() + "pages/" + blogEnum.getTvPageId();
                    HttpHeaders headers = generalHeaderSet(new HttpHeaders(), blogEnum);
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("content", e.getValue());
                    HttpEntity<String> request = new HttpEntity<>(jsonObject.toString(), headers);
                    request(url, request, HttpMethod.POST, "updateTvPage()_1");
                    postChkMap.put(subDomain, true);
                }
            }

            // postされていないsubdomainが1つ以上あれば
            if (postChkMap.entrySet().stream().anyMatch(e -> e.getValue().equals(false))) {
                for (Map.Entry<String, Boolean> e : postChkMap.entrySet()) {
                    if (e.getValue().equals(false)) {
                        String subDomain = e.getKey();
                        BlogEnum blogEnum = BlogEnum.findBySubdomain(subDomain);
                        String url = subDomain + setting.getBlogApiPath() + "pages/" + blogEnum.getTvPageId();
                        HttpHeaders headers = generalHeaderSet(new HttpHeaders(), blogEnum);
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("content", "<h2>１週間以内のTV情報はありません</h2>");
                        HttpEntity<String> request = new HttpEntity<>(jsonObject.toString(), headers);
                        request(url, request, HttpMethod.POST, "updateTvPage()_2");
                        postChkMap.put(subDomain, true);
                    }
                }
            }
        } else {
            Map<String, Boolean> postChkMap = new TreeMap<>();
            BlogEnum.getAllSubdomain().forEach(e -> postChkMap.put(e, false));
            for (Map.Entry<String, Boolean> e : postChkMap.entrySet()) {
                String subDomain = e.getKey();
                BlogEnum blogEnum = BlogEnum.findBySubdomain(subDomain);
                String url = subDomain + setting.getBlogApiPath() + "pages/" + blogEnum.getTvPageId();
                HttpHeaders headers = generalHeaderSet(new HttpHeaders(), blogEnum);
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("content", "<h2>１週間以内のTV情報はありません</h2>");
                HttpEntity<String> request = new HttpEntity<>(jsonObject.toString(), headers);
                request(url, request, HttpMethod.POST, "updateTvPage()_3");
                postChkMap.put(subDomain, true);
            }
        }
    }

    /**
     * wpIdがしっかり繋がっているか確認する。繋がっていないかったらwpId抜いてあげる
     * PENDING: wpの投稿全部落として、wpidがdbに保存されてないやつはどうにかしないといけない
     * -> tmpmethodのためあまり重要ではなく放置
     */
    public void chkWpId() throws InterruptedException {
        List<IMRel> imRelList = iMRelService.findAllWpIdNotNull();
        List<IMRel> updateList = new ArrayList<>();

        for (IMRel rel : imRelList) {
            BlogEnum blogEnum = BlogEnum.get(TeamEnum.get(rel.getTeam_id()).getBlogEnumId());
            String subDomain = blogEnum.getSubDomain();
            if (subDomain != null) {
                String url = subDomain + setting.getBlogApiPath() + "posts/" + rel.getWp_id();
                // request
                HttpHeaders headers = generalHeaderSet(new HttpHeaders(), blogEnum);
                JSONObject jsonObject = new JSONObject();
                HttpEntity<String> request = new HttpEntity<>(jsonObject.toString(), headers);
                String res = request(url, request, HttpMethod.GET, "updateTvPage()_4");

                try {
                    if (StringUtils.hasText(res)) {
                        JSONObject jo = jsonUtils.createJsonObject(res, rel.getTeam_id());
                        if (jo.has("data")) {
                            JSONObject jo1 = jo.getJSONObject("data");
                            if (jo1.has("status")) {
                                int status = jo1.getInt("status");
                                if (status == 404) {
                                    rel.setWp_id(null);
                                    updateList.add(rel);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Thread.sleep(500);
            } else {
                logger.debug("subdomain not found im_rel_id: " + rel.getIm_rel_id() + "getTeam_id: " + rel.getTeam_id() + "getWp_id: " + rel.getWp_id() + "getIm_id: " + rel.getIm_id());
            }
        }
        iMRelService.saveAll(updateList);
        logger.debug("chkWpId() Done");
    }

    /**
     * 明日の1日の予定の投稿をポストします
     *
     */
    public void createDailySchedulePost(String subDomain) {

        Date today = dateUtils.getToday();
        Calendar c = Calendar.getInstance();
        c.setTime(today);
        c.add(Calendar.DATE, 1);
        Date tmrw = c.getTime();

        String title = textController.createDailyScheduleTitle(tmrw);

        // チームIDリスト
        BlogEnum blogEnum = BlogEnum.findBySubdomain(subDomain);
        List<Long> teamIdList = Arrays.stream(TeamEnum.values()).filter(e -> e.getBlogEnumId().equals(blogEnum.getId())).map(TeamEnum::getId).collect(Collectors.toList());

        // memberList(のちループで詰める。variableだけ宣言)
        List<Long> memIdList = new ArrayList<>();

        // コンテンツ文章の作成
        List<String> tmpList = new ArrayList<>();

        for (Long teamId : teamIdList) {
            // 明日の日付で、テレビ一覧画面を作る
            List<Program> plist = programService.findByOnAirDateTeamId(tmrw, teamId);

            // 明日の日付で、商品一覧画面を作る
            List<IM> imList = imService.findByTeamIdDate(teamId, tmrw);
            Map<IM, List<ImVer>> imMap = new TreeMap<>();
            for (IM im : imList) {
                List<ImVer> verList = imVerService.findByImId(im.getIm_id());
                imMap.put(im, verList);

                // memIdListにメンバーなかったら詰める
                List<Long> tmpMemIdList = imRelMemService.findMemIdListByImId(im.getIm_id());
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
        String url = subDomain + setting.getBlogApiPath() + "posts/";
        HttpHeaders headers = generalHeaderSet(new HttpHeaders(), blogEnum);

        JSONObject jsonObject = new JSONObject();

        if (setting.getTest()!= null && setting.getTest().equals("dev")) {
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
        BlogTag yyyyMMTag = addTagIfNotExists(yyyyMM, subDomain, blogEnum.getId());
        tagNameMap.put(yyyyMMTag.getTag_name(), yyyyMMTag.getTeam_id());

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

        if (setting.getTest()!= null && setting.getTest().equals("dev")) {
            jsonObject.put("status", "draft");
        } else {
            jsonObject.put("status", "publish");
        }

        jsonObject.put("content", content);

        HttpEntity<String> request = new HttpEntity<>(jsonObject.toString(), headers);
        String res = request(url, request, HttpMethod.POST, "createDailySchedulePost()");

        JSONObject jo = jsonUtils.createJsonObject(res, teamIdList.get(0));
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
    public Map<Integer, String> requestMedia(HttpServletResponse response, Long teamId, String imageUrl) {
        BlogEnum blogEnum = BlogEnum.get(TeamEnum.get(teamId).getBlogEnumId());
        String finalUrl = blogEnum.getSubDomain() + setting.getBlogApiPath() + "media";

        imageUrl = imageUrl.replaceAll("\\?.*$", "");

//        String imagePath = "";
//
//        // 楽天の画像の場合は取得しに行く
//        if (imageUrl.startsWith("http")) {
//            try (InputStream in = new URL(imageUrl).openStream()) {
//                imagePath = availablePath(imageUrl);
//                Files.copy(in, Paths.get(imagePath));
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        } else {
//            imagePath = imageUrl;
//        }

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
            Optional<Long> tagIdResult = blogTagService.findBlogTagIdByTagName(e.getKey(), teamId);

            Long tagId = null;
            if (tagIdResult.isPresent()) {
                tagId = tagIdResult.get();
            } else {
                // タグが見つからなかった場合、WPブログに登録したり引っ張ってきてDBに保存したり
                BlogEnum blogEnum = BlogEnum.get(teamId);
                BlogTag tag = addTagIfNotExists(e.getKey(), blogEnum.getSubDomain(), teamId);
                tagId = tag.getBlog_tag_id();
            }
            tagIdList.add(tagId);
        }
        return tagIdList;
    }
}

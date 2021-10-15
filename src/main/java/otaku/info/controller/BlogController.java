package otaku.info.controller;

import lombok.AllArgsConstructor;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import otaku.info.entity.*;
import otaku.info.enums.TeamEnum;
import otaku.info.searvice.*;
import otaku.info.setting.Setting;
import otaku.info.utils.ItemUtils;
import otaku.info.utils.JsonUtils;
import otaku.info.utils.ServerUtils;
import otaku.info.utils.StringUtilsMine;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
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
    TagService tagService;

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

    public void insertTags(String subDomain) {
        Integer n = 1;

        String url = subDomain + setting.getBlogApiPath() + "tags?_fields[]=id&_fields[]=name&_fields[]=link&per_page=40&page=" + n;

        // request
        HttpHeaders headers = generalHeaderSet(new HttpHeaders(), subDomain);
        JSONObject jsonObject = new JSONObject();
        HttpEntity<String> request = new HttpEntity<>(jsonObject.toString(), headers);
        String res = request(url, request, HttpMethod.GET);


        try {
            if (JsonUtils.isJsonArray(res)) {
                JSONArray ja = new JSONArray(res);
                List<BlogTag> blogTagList = new ArrayList<>();

                    for (int i=0;i<ja.length();i++) {
                        Integer wpId = ja.getJSONObject(i).getInt("id");
                        String tagName = ja.getJSONObject(i).getString("name").replaceAll("^\"|\"$", "");
                        String link = ja.getJSONObject(i).getString("link").replaceAll("^\"|\"$", "");

                        Long teamId = TeamEnum.findIdBySubDomain(subDomain);

                        if (blogTagService.findBlogTagIdByTagName(tagName, teamId) == 0) {
                            BlogTag blogTag = new BlogTag();
                            blogTag.setWp_tag_id((long)wpId);
                            blogTag.setTag_name(tagName);
                            blogTag.setLink(link);
                            blogTag.setTeam_id(teamId);
                            blogTagList.add(blogTag);
                        }
                    }
                    blogTagService.saveAll(blogTagList);
                }
        } catch (Exception e) {
            e.printStackTrace();
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
        List<ItemMaster> itemMasterList = itemMasterService.findItemsBetweenDelFlg(today, to, false).stream().filter(e -> iMRelService.findTeamIdListByItemMId(e.getItem_m_id()).size() > 0).collect(Collectors.toList());

        // subDomainごとにまとめる
        Map<String, Map<ItemMaster, List<Item>>> teamIdItemMasterItemMap = new TreeMap<>();
        List<String> subDomainList = Arrays.stream(TeamEnum.values()).map(TeamEnum::getSubDomain).distinct().collect(Collectors.toList());
        for (String s : subDomainList) {
            teamIdItemMasterItemMap.put(s, new TreeMap<>());
        }

        for (ItemMaster itemMaster : itemMasterList) {
            // itemMasterとitemListは用意できた
            List<Item> itemList = itemService.findByMasterId(itemMaster.getItem_m_id());
            List<Long> itemIdList = itemList.stream().map(Item::getItem_id).collect(Collectors.toList());
            List<String> subDomainList1 = iRelService.findByItemIdList(itemIdList).stream().map(e -> TeamEnum.findSubDomainById(e.getTeam_id())).distinct().collect(Collectors.toList());

            for (String subDomain : subDomainList1) {
                Map<ItemMaster, List<Item>> tmpMap1 = teamIdItemMasterItemMap.get(subDomain);
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
        List<ItemMaster> tmpList = new ArrayList<>();
        for (TeamEnum e : TeamEnum.values()) {
            tmpList.addAll(itemMasterService.findDateAfterTeamIdLimit(to, e.getId(), 10L));
        }
        List<ItemMaster> futureItemMasterList = tmpList.stream().distinct().collect(Collectors.toList());

        // subDomainごとにまとめる
        Map<String, Map<ItemMaster, List<Item>>> teamIdItemMasterItemFutureMap = new TreeMap<>();
        for (String s : subDomainList) {
            teamIdItemMasterItemFutureMap.put(s, new TreeMap<>());
        }

        for (ItemMaster itemMaster : futureItemMasterList) {
            // itemMasterとitemListは用意できた
            List<Item> itemList = itemService.findByMasterId(itemMaster.getItem_m_id());
            List<Long> itemIdList = itemList.stream().map(Item::getItem_id).collect(Collectors.toList());
            List<String> subDomainList1 = iRelService.findByItemIdList(itemIdList).stream().map(e -> TeamEnum.findSubDomainById(e.getTeam_id())).distinct().collect(Collectors.toList());

            for (String subDomain : subDomainList1) {
                Map<ItemMaster, List<Item>> tmpMap1 = teamIdItemMasterItemFutureMap.get(subDomain);
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

        for (TeamEnum e : TeamEnum.values()) {
            if (!requestMap.containsKey(e.getSubDomain())) {
                requestMap.put(e.getSubDomain(), "先１週間の新発売情報はありません");
            }
        }

        String blogText = "";
        if (teamIdItemMasterItemMap.size() > 0) {
            // <teamId, blogText>
            for (Map.Entry<String, Map<ItemMaster, List<Item>>> e : teamIdItemMasterItemMap.entrySet()) {
                // 明日のリストはあるが未来のリストがそもそもない→明日のだけでテキスト作る
                if (teamIdItemMasterItemFutureMap.size() == 0) {
                    blogText = textController.blogUpdateReleaseItems(e.getValue(), null);
                } else {
                    // 明日のリストと未来のリスト両方あるor明日のリストはあるが未来のリスト（同じteamId）がない
                    blogText = textController.blogUpdateReleaseItems(e.getValue(), teamIdItemMasterItemFutureMap.getOrDefault(e.getKey(), null));
                }
                requestMap.put(e.getKey(), blogText);
            }
        } else if (teamIdItemMasterItemFutureMap.size() > 0) {
            // 明日の発売商品がないがその先１週間はある場合
            for (Map.Entry<String, Map<ItemMaster, List<Item>>> e : teamIdItemMasterItemFutureMap.entrySet()) {
                blogText = textController.blogUpdateReleaseItems(null, e.getValue());
                requestMap.put(e.getKey(), blogText);
            }
        }

        // リクエスト送信
        if (requestMap.size() > 0) {
            for (Map.Entry<String, String> e : requestMap.entrySet()) {
                HttpHeaders headersMap = generalHeaderSet(new HttpHeaders(), e.getKey());

                if (headersMap != null && !headersMap.isEmpty()) {
                    TeamEnum teamEnum = TeamEnum.getBySubDomain(e.getKey());
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("content", blogText);
                    HttpEntity<String> request = new HttpEntity<>(jsonObject.toString(), headersMap);
                    String finalUrl = teamEnum.getSubDomain() + setting.getBlogApiPath() + "pages/" + TeamEnum.getItemPageId(teamEnum.getId());
                    String res = request(finalUrl, request, HttpMethod.POST);
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

            String auth = new String(Base64.getEncoder().encode(setting.getBlogPw().getBytes()));
            headers.add("Authorization", "Basic " + auth);
            resultMap.put("", headers);
        } else {
            for (String subDomain : subDomainList) {
                headers.add("Accept", MediaType.APPLICATION_JSON_VALUE);
                headers.setContentType(MediaType.APPLICATION_JSON);

                String auth = "";
                if (subDomain != null) {
                    auth = new String(Base64.getEncoder().encode(TeamEnum.getBySubDomain(subDomain).getBlogPw().getBytes()));
                } else {
                    auth = new String(Base64.getEncoder().encode(setting.getBlogPw().getBytes()));
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
     * @param subDomain
     * @return
     */
    public HttpHeaders generalHeaderSet(HttpHeaders headers, String subDomain) {

        headers.add("Accept", MediaType.APPLICATION_JSON_VALUE);
        headers.setContentType(MediaType.APPLICATION_JSON);

        String auth = "";

        if (subDomain == null) {
            auth = new String(Base64.getEncoder().encode(setting.getBlogPw().getBytes()));
        }

        TeamEnum e = TeamEnum.getBySubDomain(subDomain);
        if (e == null) {
            auth = new String(Base64.getEncoder().encode(setting.getBlogPw().getBytes()));
        } else {
            auth = new String(Base64.getEncoder().encode(e.getBlogPw().getBytes()));
        }

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
    public String request(String url, HttpEntity<String> request, HttpMethod method) {

        String result = "";

        try {
            RestTemplate restTemplate = new RestTemplate();
            System.out.println("Post: " + url);
            ResponseEntity<String> responseEntity = restTemplate.exchange(url, method, request, String.class);
            
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            result = responseEntity.getBody();
        } catch (Exception e) {
            if (e instanceof HttpClientErrorException.Forbidden) {
                throw new HttpClientErrorException(HttpStatus.FORBIDDEN);
            } else if (e instanceof HttpClientErrorException.BadRequest) {
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST);
            } else {
                e.printStackTrace();
            }
            result = "";
        }

        System.out.println("Request result: " + result);
        return result;
    }

    /**
     * 引数のマスター商品を全て投稿する
     * blogIdを返却する
     *
     * @param itemMaster
     * @param itemList
     */
    public Long postMasterItem(ItemMaster itemMaster, List<Item> itemList) {

        List<IMRel> iMRelList = iMRelService.findByItemMId(itemMaster.getItem_m_id());

        if (iMRelList.stream().anyMatch(e -> e.getWp_id() != null)) {
            updateMasterItem(itemMaster, itemList);
        }

        List<Long> teamIdList = iMRelList.stream().map(IMRel::getTeam_id).distinct().collect(Collectors.toList());
        List<Long> memberIdList = new ArrayList<>();
        for (IMRel rel : iMRelList) {
            List<IMRelMem> relMemList = imRelMemService.findByImRelId(rel.getIm_rel_id());
            if (relMemList.size() > 0) {
                memberIdList = relMemList.stream().map(IMRelMem::getMember_id).collect(Collectors.toList());
            }
        }

        // tag:チーム名と発売日の年月を用意したい(idで指定してあげないといけない（stringでまず集めて、最後にidを見つけに行くor新規登録）)
        // itemMaster -> teamIdList -> teamName -> tag
        List<String> tagList = teamService.findTeamNameByIdList(teamIdList);
        // memberを追加
        List<String> memberNameList = memberService.getMemberNameList(memberIdList);

        if (memberNameList != null && memberNameList.size() > 0) {
            tagList.addAll(memberNameList);
        }

        String title = textController.createBlogTitle(itemMaster.getPublication_date(), itemMaster.getTitle());
        System.out.println("title: " + title);

        List<String> textList = textController.blogReleaseItemsText(Collections.singletonMap(itemMaster, itemList));
        String content = "";
        if (textList.size() > 0) {
            content = textList.get(0);
        }

        Long blogId = 0L;
        if (StringUtils.hasText(content)) {
            List<String> subDomainList = TeamEnum.findSubDomainListByIdList(teamIdList);

            // リクエスト送信
            Map<String, HttpHeaders> headersMap = generalHeaderSet(new HttpHeaders(), subDomainList);

            // subdomainの数だけ帰ってくる
            if (headersMap.size() > 0) {

                // 投稿するドメインごと
                for (Map.Entry<String, HttpHeaders> entry : headersMap.entrySet()) {
                    JSONObject jsonObject = new JSONObject();
                    if (setting.getTest()!= null && setting.getTest().equals("dev")) {
                        jsonObject.put("title", "[dev]" + title);
                    } else {
                        jsonObject.put("title", title);
                    }
                    jsonObject.put("author", 1);
                    jsonObject.put("categories", new Integer[]{5});

                    // 年月
                    BlogTag yyyyMMTag = addTagIfNotExists(itemMaster.getPublication_date(), entry.getKey());
                    tagList.add(yyyyMMTag.getTag_name());

                    // TODO: チームメイトメンバー名が登録されrてるか、新規追加必要か確認執拗
                    // BlogTag yyyyMMTag = addTagIfNotExists(itemMaster.getPublication_date(), entry.getKey()); for all
                    List<Integer> list = blogTagService.findBlogTagIdListByTagNameList(tagList);
                    int[] tags = new int[0];
                    if (!list.isEmpty()) {
                        tags = list.stream().mapToInt(i->i).toArray();
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
                    HttpEntity<String> request = new HttpEntity<>(jsonObject.toString(), entry.getValue());

                    String url = entry.getKey() + setting.getBlogApiPath() + "posts/";

                    String res = request(url, request, HttpMethod.POST);

                    // うまくポストが完了してStringが返却されたらwpIdをRelに登録する
                    if (StringUtils.hasText(res)) {
                        JSONObject jo = jsonUtils.createJsonObject(res);
                        if (jo.get("id") != null) {
                            blogId = Long.valueOf(jo.get("id").toString().replaceAll("^\"|\"$", ""));
                            System.out.println("posted wp blog id: " + blogId.toString() + " Subdomain:" + entry.getKey());
                            IMRel rel = iMRelService.findByImIdTeamId(itemMaster.getItem_m_id(), TeamEnum.findIdBySubDomain(entry.getKey()));
                            rel.setWp_id(blogId);
                            iMRelService.save(rel);
                            System.out.println("*** itemMaster saved");
                        }
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return blogId;
    }

    /**
     * 1つのマスター商品のcontext更新。contextの作成→WP更新ポストまでおこないます。
     *
     * @param itemMaster
     * @param itemList
     */
    public void updateMasterItem(ItemMaster itemMaster, List<Item> itemList) {
        String content = textController.blogReleaseItemsText(Collections.singletonMap(itemMaster, itemList)).get(0);
        List<IMRel> iMRelList = iMRelService.findByItemMId(itemMaster.getItem_m_id());

        // wpIdがrel全部nullだったら新規登録ですね
        if (iMRelList.stream().allMatch(e -> e.getWp_id() == null)) {
            postMasterItem(itemMaster, itemList);
        }

        List<Long> teamIdList = iMRelList.stream().map(IMRel::getTeam_id).distinct().collect(Collectors.toList());
        List<Long> memberIdList = new ArrayList<>();
        for (IMRel rel : iMRelList) {
            List<IMRelMem> relMemList = imRelMemService.findByImRelId(rel.getIm_rel_id());
            if (relMemList.size() > 0) {
                memberIdList = relMemList.stream().map(IMRelMem::getMember_id).collect(Collectors.toList());
            }
        }

        if (teamIdList.size() > 0) {
            Map<String, HttpHeaders> headersMap = generalHeaderSet(new HttpHeaders(), TeamEnum.findSubDomainListByIdList(teamIdList));
            List<IMRel> newIMRelList = new ArrayList<>();

            if (headersMap.size() > 0) {
                // サブドメインごとに処理する
                for (Map.Entry<String, HttpHeaders> entry : headersMap.entrySet()) {
                    Long teamId = TeamEnum.findIdBySubDomain(entry.getKey());
                    String wpId = "";
                    for (IMRel rel : iMRelList) {
                        if (rel.getTeam_id().equals(teamId)) {
                            wpId = rel.getWp_id().toString();
                        }
                    }

                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("content", content);
                    HttpEntity<String> request = new HttpEntity<>(jsonObject.toString(), entry.getValue());

                    // wpIdが取得できなかったら、存在しないということなのでそのサブドメインは新規投稿してあげる
                    String url = "";
                    if (wpId.equals("")) {
                        url = entry.getKey() + setting.getBlogApiPath() + "posts/";
                    } else {
                        url = entry.getKey() + setting.getBlogApiPath() + "posts/" + wpId;
                    }

                    // ここで投稿
                    String res = request(url, request, HttpMethod.POST);
                    JSONObject jo = jsonUtils.createJsonObject(res);
                    if (jo.get("id") != null) {
                        Long blogId = Long.valueOf(jo.get("id").toString().replaceAll("^\"|\"$", ""));
                        IMRel rel = iMRelService.findByImIdTeamId(itemMaster.getItem_m_id(), TeamEnum.findIdBySubDomain(entry.getKey()));
                        rel.setWp_id(blogId);
                        iMRelService.save(rel);
                        System.out.println("Blog posted: " + url + "\n" + content + "\n" + Long.parseLong(jo.get("id").toString().replaceAll("^\"|\"$", "")));
                    }
                }
            }
        }
    }

    /**
     * ブログのマスタ商品投稿を更新する。
     * 1要素のみのMap<新規追加itemMaster,更新itemMaster>を返却します。
     *
     * @param itemMasterList itemMasterIdを返す
     */
    public Map<List<ItemMaster>, List<ItemMaster>> postOrUpdate(List<ItemMaster> itemMasterList) throws InterruptedException {
        List<ItemMaster> newItemMasterList = new ArrayList<>();
        List<ItemMaster> updateItemMasterList = new ArrayList<>();

        for (ItemMaster itemMaster : itemMasterList) {
            // 各teamIdにおいて
            // ブログを投稿する
            List<Item> itemList = itemService.findByMasterId(itemMaster.getItem_m_id());
            List<IMRel> IMRelList = iMRelService.findByItemMId(itemMaster.getItem_m_id());
            boolean isNewPost = IMRelList.stream().noneMatch(e -> e.getWp_id() != null);
            if (isNewPost) {
                // 新規投稿する
                postMasterItem(itemMaster, itemList);
                newItemMasterList.add(itemMaster);
            } else {
                // 既存投稿を更新する(完全洗い替え)
                updateMasterItem(itemMaster, itemList);
                updateItemMasterList.add(itemMaster);
            }
            Thread.sleep(500);
        }
        return Collections.singletonMap(newItemMasterList, updateItemMasterList);
    }

    /**
     * WpIdからポストの内容を取得します。
     *
     * @param wpId
     * @param subDomain
     * @return
     */
    public String requestPostData(String wpId, String subDomain) {
        String finalUrl = subDomain + setting.getBlogApiPath() + "posts/" + wpId;

        HttpHeaders headers = generalHeaderSet(new HttpHeaders(), subDomain);
        return request(finalUrl, new HttpEntity<>(headers), HttpMethod.GET);
    }

    /**
     * アイキャッチメディアがある場合、画像IDを返却します。
     * ない場合、0
     *
     * @param text
     * @return
     */
    public Integer extractMedia(String text) {
        JSONObject jsonObject = jsonUtils.createJsonObject(text);
        if (jsonObject.get("featured_media") != null) {
            return Integer.parseInt(jsonObject.get("featured_media").toString().replaceAll("^\"|\"$", ""));
        }
        return 0;
    }

    /**
     * 翌月のyyyyMMタグを追加する。
     *
     */
    public void addNextMonthTag(String subDomain) {
        // どの月でも存在する27・28日の場合、チェックに通す
        if (dateUtils.getDate() == 27 || dateUtils.getDate() == 28) {
            System.out.println("月末につき月タグ確認処理");
            // info DBのblogTagテーブルに翌月のyyyyMMタグが存在するか？
            Long teamId = TeamEnum.findIdBySubDomain(subDomain);
            Integer wpTagId = blogTagService.findBlogTagIdByTagName(dateUtils.getNextYYYYMM(), teamId);
            boolean existsBlogTag =  (wpTagId!= null) && (wpTagId != 0);
            if (!existsBlogTag) {
                String url = subDomain + setting.getBlogApiPath() + "tags/";

                HttpHeaders headers = generalHeaderSet(new HttpHeaders(), subDomain);
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("name", dateUtils.getNextYYYYMM());

                HttpEntity<String> request = new HttpEntity<>(jsonObject.toString(), headers);
                request(url, request, HttpMethod.POST);
                System.out.println(subDomain + ":次の月タグ追加");
            }
        }
    }

    /**
     * WPにあるがDBにないタグを保存する
     *
     */
    public void getBlogTagNotSavedOnInfoDb(String subDomain) {
        // WPにあるタグを取得する
        String url = subDomain + setting.getBlogApiPath() + "tags?_fields[]=id&_fields[]=name&_fields[]=link";

        HttpHeaders headers = generalHeaderSet(new HttpHeaders(), subDomain);
        JSONObject jsonObject = new JSONObject();
        HttpEntity<String> request = new HttpEntity<>(jsonObject.toString(), headers);
        String res = request(url, request, HttpMethod.GET);
        List<BlogTag> blogTagList = new ArrayList<>();

        try {
            if (JsonUtils.isJsonArray(res)) {
                JSONArray ja = new JSONArray(res);
                for (int i=0;i<ja.length();i++) {
                    Integer wpId = ja.getJSONObject(i).getInt("id");
                    String tagName = ja.getJSONObject(i).getString("name").replaceAll("^\"|\"$", "");
                    String link = ja.getJSONObject(i).getString("link").replaceAll("^\"|\"$", "");
                    Long teamId = TeamEnum.findIdBySubDomain(subDomain);

                    if (blogTagService.findBlogTagIdByTagName(tagName, teamId) == 0) {
                        BlogTag blogTag = new BlogTag();
                        blogTag.setWp_tag_id((long)wpId);
                        blogTag.setTag_name(tagName);
                        blogTag.setLink(link);
                        blogTag.setTeam_id(teamId);
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
    public BlogTag addTagIfNotExists(Date date, String subDomain) {

        String yyyyMM = dateUtils.getYYYYMM(date);

        String url = subDomain + setting.getBlogApiPath() + "tags?_fields[]=name&slug=" + yyyyMM;

        // request
        HttpHeaders headers = generalHeaderSet(new HttpHeaders(), subDomain);
        JSONObject jsonObject = new JSONObject();
        HttpEntity<String> request = new HttpEntity<>(jsonObject.toString(), headers);
        String res = request(url, request, HttpMethod.GET);

        BlogTag blogTag = new BlogTag();

        try {
            if (JsonUtils.isJsonArray(res)) {
                JSONArray ja = new JSONArray(res);
                // タグがまだWPになかったら登録する
                if (ja.length() == 0) {
                    blogTag = registerTag(date, subDomain);
                } else {
                    // タグはWPにある場合
                    blogTag = blogTagService.findByTagName(yyyyMM);

                    // WPにタグあるがDBから見つからなかった場合、DBに登録する
                    if (blogTag == null || blogTag.getBlog_tag_id() == null) {
                        BlogTag blogTag1 = new BlogTag();

                        // WPからDBに登録したいタグのデータを取ってくる
                        String url1 = subDomain + setting.getBlogApiPath() + "tags?slug=" + yyyyMM + "&per_page=1";

                        // request
                        HttpHeaders headers1 = generalHeaderSet(new HttpHeaders(), subDomain);
                        JSONObject jsonObject1 = new JSONObject();
                        HttpEntity<String> request1 = new HttpEntity<>(jsonObject1.toString(), headers1);
                        String res1 = request(url1, request1, HttpMethod.GET);

                        try {
                            if (JsonUtils.isJsonArray(res1)) {
                                JSONArray ja1 = new JSONArray(res1);

                                blogTag1.setTag_name(ja1.getJSONObject(0).getString("name"));
                                blogTag1.setLink(ja1.getJSONObject(0).getString("link"));
                                blogTag1.setWp_tag_id((long) ja1.getJSONObject(0).getInt("id"));

                                Long teamId = TeamEnum.findIdBySubDomain(subDomain);
                                blogTag1.setTeam_id(teamId);
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
    public BlogTag registerTag(Date date, String subDomain) {
        String url = subDomain + setting.getBlogApiPath() + "tags/";

        HttpHeaders h = generalHeaderSet(new HttpHeaders(), subDomain);
        JSONObject jo = new JSONObject();
        jo.put("name", dateUtils.getYYYYMM(date));

        HttpEntity<String> request = new HttpEntity<>(jo.toString(), h);
        String res = request(url, request, HttpMethod.POST);

        JSONObject jsonObject1 = jsonUtils.createJsonObject(res);

        int yyyyMMId;
        if (jsonObject1.get("id") != null) {
            yyyyMMId = jsonObject1.getInt("id");
            String link = jsonObject1.getString("link").replaceAll("^\"|\"$", "");
            BlogTag blogTag = new BlogTag();
            blogTag.setTag_name(dateUtils.getYYYYMM(date));
            blogTag.setWp_tag_id((long) yyyyMMId);
            blogTag.setLink(link);

            Long teamId = TeamEnum.findIdBySubDomain(subDomain);
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
                String subDomain = TeamEnum.findSubDomainById(teamId);

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
            TeamEnum.getAllSubDomain().stream().distinct().forEach(e -> postChkMap.put(e, false));

            if (resultMap.size() > 0) {
                for (Map.Entry<String, String> e : resultMap.entrySet()) {
                    String subDomain = e.getKey();
                    String url = subDomain + setting.getBlogApiPath() + "pages/" + TeamEnum.getTvPageIdBySubDomain(subDomain);
                    HttpHeaders headers = generalHeaderSet(new HttpHeaders(), subDomain);
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("content", e.getValue());
                    HttpEntity<String> request = new HttpEntity<>(jsonObject.toString(), headers);
                    request(url, request, HttpMethod.POST);
                    postChkMap.put(subDomain, true);
                }
            }

            // postされていないsubdomainが1つ以上あれば
            if (postChkMap.entrySet().stream().anyMatch(e -> e.getValue().equals(false))) {
                for (Map.Entry<String, Boolean> e : postChkMap.entrySet()) {
                    if (e.getValue().equals(false)) {
                        String subDomain = e.getKey();
                        String url = subDomain + setting.getBlogApiPath() + "pages/" + TeamEnum.getTvPageIdBySubDomain(subDomain);
                        HttpHeaders headers = generalHeaderSet(new HttpHeaders(), subDomain);
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("content", "<h2>１週間以内のTV情報はありません</h2>");
                        HttpEntity<String> request = new HttpEntity<>(jsonObject.toString(), headers);
                        request(url, request, HttpMethod.POST);
                        postChkMap.put(subDomain, true);
                    }
                }
            }
        } else {
            Map<String, Boolean> postChkMap = new TreeMap<>();
            TeamEnum.getAllSubDomain().stream().distinct().forEach(e -> postChkMap.put(e, false));
            for (Map.Entry<String, Boolean> e : postChkMap.entrySet()) {
                String subDomain = e.getKey();
                String url = subDomain + setting.getBlogApiPath() + "pages/" + TeamEnum.getTvPageIdBySubDomain(subDomain);
                HttpHeaders headers = generalHeaderSet(new HttpHeaders(), subDomain);
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("content", "<h2>１週間以内のTV情報はありません</h2>");
                HttpEntity<String> request = new HttpEntity<>(jsonObject.toString(), headers);
                request(url, request, HttpMethod.POST);
                postChkMap.put(subDomain, true);
            }
        }
    }

    /**
     * wpIdがしっかり繋がっているか確認する。繋がっていないかったらwpId抜いてあげる
     * TODO: wpの投稿全部落として、wpidがdbに保存されてないやつはどうにかしないといけない
     */
    public void chkWpId() throws InterruptedException {
        List<IMRel> imRelList = iMRelService.findAllWpIdNotNull();
        List<IMRel> updateList = new ArrayList<>();

        for (IMRel rel : imRelList) {
            String subDomain = TeamEnum.findSubDomainById(rel.getTeam_id());
            if (subDomain != null) {
                String url = subDomain + setting.getBlogApiPath() + "posts/" + rel.getWp_id();
                // request
                HttpHeaders headers = generalHeaderSet(new HttpHeaders(), subDomain);
                JSONObject jsonObject = new JSONObject();
                HttpEntity<String> request = new HttpEntity<>(jsonObject.toString(), headers);
                String res = request(url, request, HttpMethod.GET);

                try {
                    if (StringUtils.hasText(res)) {
                        JSONObject jo = jsonUtils.createJsonObject(res);
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
                System.out.println("subdomain not found im_rel_id: " + rel.getIm_rel_id() + "getTeam_id: " + rel.getTeam_id() + "getWp_id: " + rel.getWp_id() + "getItem_m_id: " + rel.getItem_m_id());
            }
        }
        iMRelService.saveAll(updateList);
        System.out.println("chkWpId() Done");
    }

    /**
     * ブログの投稿を全部取ってきて、対応するwpidがdbにあるか確認する。なかったら
     * TODO: subdomainなしotakuinfoの場合、teamIdが適切なもの取れていないのではないか？
     */
    public void chkWpIdByBlog() throws InterruptedException {

        List<String> domainList = Arrays.stream(TeamEnum.values()).map(e -> e.getSubDomain()).distinct().collect(Collectors.toList());
        for (String subDomain : domainList) {
            List<String> outPut = new ArrayList<>();
            Long teamId = TeamEnum.findIdBySubDomain(subDomain);
            int n = 1;
            boolean nextFlg = true;
            int errCnt = 0;
            while (nextFlg) {
                String url = subDomain + setting.getBlogApiPath() + "posts?_fields[]=id&_fields[]=title&per_page=50&page=" + n;
                // request
                HttpHeaders headers = generalHeaderSet(new HttpHeaders(), subDomain);
                JSONObject jsonObject = new JSONObject();
                HttpEntity<String> request = new HttpEntity<>(jsonObject.toString(), headers);

                try {
                    String res = request(url, request, HttpMethod.GET);
                    if (StringUtils.hasText(res)) {
                        if (JsonUtils.isJsonArray(res)) {
                            JSONArray ja = new JSONArray(res);
                            for (int i=0;i<ja.length();i++) {
                                Integer wpId = ja.getJSONObject(i).getInt("id");
                                List<IMRel> relList = iMRelService.findbyWpIdTeamId((long) wpId, teamId);
                                if (relList.size() == 0) {
                                    String title = ja.getJSONObject(i).getJSONObject("title").getString("rendered");
                                    outPut.add(subDomain + ":" + wpId + ":" + title);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (e instanceof HttpClientErrorException.BadRequest) {
                        nextFlg = false;
                    }
                    ++errCnt;
                }
                if (errCnt > 5) {
                    nextFlg = false;
                }
                Thread.sleep(50);
                ++n;
            }
            try{
                if (outPut.size() > 0) {
                    File file = new File("/root/outfile_" +subDomain + "txt");
                    FileWriter filewriter = new FileWriter(file);
                    for (String msg : outPut) {
                        filewriter.write(msg + "\n");
                    }
                    filewriter.close();
                }
            }catch(IOException e){
                System.out.println(e);
            }
        }
        System.out.println("chkWpIdByBlog() Done");
    }
}

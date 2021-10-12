package otaku.info.controller;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import otaku.info.entity.*;
import otaku.info.enums.TeamEnum;
import otaku.info.searvice.*;
import otaku.info.setting.Setting;
import otaku.info.utils.DateUtils;
import otaku.info.utils.ItemUtils;
import otaku.info.utils.JsonUtils;
import otaku.info.utils.StringUtilsMine;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Controller
public class TmpController {

    @Autowired
    Setting setting;

    @Autowired
    RakutenController rakutenController;

    @Autowired
    BlogController blogController;

    @Autowired
    TextController textController;

    @Autowired
    ItemService itemService;

    @Autowired
    ProgramService programService;

    @Autowired
    ItemRelService itemRelService;

    @Autowired
    IMRelService imRelService;

    @Autowired
    PRelService pRelService;

    @Autowired
    ItemMasterService itemMasterService;

    @Autowired
    BlogTagService blogTagService;

    @Autowired
    IMRelService IMRelService;

    @Autowired
    DateUtils dateUtils;

    @Autowired
    ItemUtils itemUtils;

    @Autowired
    JsonUtils jsonUtils;

//    /**
//     * Item -> ItemRel
//     * Item.team_id, member_id, wp_idが存在しないとエラーになります
//     */
//    public void moveItemToItemRel2() {
//        List<Item> itemList = itemService.findAll();
//        List<ItemRel> saveList = new ArrayList<>();
//        for (Item i : itemList) {
//            if (i.getTeam_id() == null) {
//                continue;
//            }
//
//            ItemRel rel = new ItemRel();
//            rel.setItem_id(i.getItem_id());
//
//            List<Long> teamIdList = getLongIdList(i.getTeam_id());
//            for (Long teamId : teamIdList) {
//                if (teamId == 0) {
//                    continue;
//                }
//                if (rel.getTeam_id() == null) {
//                    rel.setTeam_id(teamId);
//                    saveList.add(rel);
//                } else {
//                    ItemRel newRel = new ItemRel();
//                    BeanUtils.copyProperties(rel, newRel);
//                    newRel.setTeam_id(teamId);
//                    saveList.add(newRel);
//                }
//            }
//        }
//
//        // ここでitemIdとteamIdだけ入れたrelを全部登録する
//        if (saveList.size() > 0) {
//            itemRelService.saveAll(saveList);
//        }
//
//        // memberのあるitemだけ処理入る
//        List<Item> memberItemList = itemList.stream().filter(e -> e.getMember_id() != null).collect(Collectors.toList());
//        System.out.println("memberItemList size: " + memberItemList.size());
//        List<ItemRel> memberSaveList = new ArrayList<>();
//
//        for (Item i : memberItemList) {
//            if (i.getMember_id() == null) {
//                continue;
//            }
//
//            List<Long> mIdList = getLongIdList(i.getMember_id());
//            for (Long mId : mIdList) {
//                Long teamId = MemberEnum.getTeamIdById(mId);
//                ItemRel rel = itemRelService.findByItemIdTeamIdMemberIdNull(i.getItem_id(), teamId);
//
//                if (rel == null) {
//                    ItemRel newRel = new ItemRel();
//                    newRel.setItem_id(i.getItem_id());
//                    newRel.setTeam_id(teamId);
//                    newRel.setMember_id(mId);
//                    memberSaveList.add(newRel);
//                } else {
//                    rel.setMember_id(mId);
//                    memberSaveList.add(rel);
//                }
//            }
//        }
//        itemRelService.saveAll(memberSaveList);
//
//        // wpIdがあったItemだけ、総合ブログのteamIdだったらrelにwpIdセットします
//        List<Item> wpItemList = itemList.stream().filter(e -> e.getWp_id() != null).collect(Collectors.toList());
//        List<ItemRel> wpSaveList = new ArrayList<>();
//
//        for (Item i : wpItemList) {
//            if (i.getWp_id() == null) {
//                continue;
//            }
//
//            List<ItemRel> wpAddRelList = itemRelService.findByItemId(i.getItem_id());
//            for (ItemRel rel : wpAddRelList) {
//                if (TeamEnum.findSubDomainById(Math.toIntExact(rel.getTeam_id())).equals("NA")) {
//                    rel.setWp_id((long)i.getWp_id());
//                    wpSaveList.add(rel);
//                }
//            }
//        }
//
//        if (wpSaveList.size() > 0) {
//            itemRelService.saveAll(wpSaveList);
//        }
//    }

//    /**
//     * ItemMaster -> Rel
//     * Itemmaster.team_id, member_id, wp_idが存在しないとエラーになります
//     */
//    public void moveItemMasterToIMRel2() {
//        List<ItemMaster> itemMList = itemMasterService.findAll();
//        List<IMRel> saveList = new ArrayList<>();
//        for (ItemMaster im : itemMList) {
//            if (im.getTeam_id() == null) {
//                continue;
//            }
//
//            IMRel rel = new IMRel();
//            rel.setItem_m_id(im.getItem_m_id());
//            List<Long> teamIdList = getLongIdList(im.getTeam_id());
//            for (Long teamId : teamIdList) {
//                if (teamId == 0) {
//                    continue;
//                }
//                if (rel.getTeam_id() == null) {
//                    rel.setTeam_id(teamId);
//                    saveList.add(rel);
//                } else {
//                    IMRel newRel = new IMRel();
//                    BeanUtils.copyProperties(rel, newRel);
//                    newRel.setTeam_id(teamId);
//                    saveList.add(newRel);
//                }
//            }
//        }
//
//        if (saveList.size() > 0) {
//            imRelService.saveAll(saveList);
//        }
//
//        // member
//        List<ItemMaster> memberItemList = itemMList.stream().filter(e -> e.getMember_id() != null).collect(Collectors.toList());
//        List<IMRel> memberSaveList = new ArrayList<>();
//
//        for (ItemMaster im : memberItemList) {
//            if (im.getMember_id() == null) {
//                continue;
//            }
//
//            List<Long> memberIdList = getLongIdList(im.getMember_id());
//            for (Long mId : memberIdList) {
//                Long teamId = MemberEnum.getTeamIdById(mId);
//                IMRel rel = imRelService.findByItemIdTeamIdMemberIdNull(im.getItem_m_id(), teamId);
//
//                if (rel == null) {
//                    IMRel newRel = new IMRel();
//                    newRel.setItem_m_id(im.getItem_m_id());
//                    newRel.setTeam_id(teamId);
//                    newRel.setMember_id(mId);
//                    memberSaveList.add(newRel);
//                } else {
//                    rel.setMember_id(mId);
//                    memberSaveList.add(rel);
//                }
//            }
//        }
//
//        if (memberSaveList.size() > 0) {
//            imRelService.saveAll(memberSaveList);
//        }
//
//        // wpId
//        List<ItemMaster> wpIMList = itemMList.stream().filter(e -> e.getWp_id() != null && e.getTeam_id() != null).collect(Collectors.toList());
//        List<IMRel> wpSaveList = new ArrayList<>();
//        for (ItemMaster im : wpIMList) {
//            if (im.getWp_id() == null) {
//                continue;
//            }
//
//            List<IMRel> wpAddRelList = imRelService.findByItemMId(im.getItem_m_id());
//            for (IMRel rel : wpAddRelList) {
//
//                System.out.println(rel.getTeam_id() + " " + rel.getItem_m_id());
//                if (rel.getTeam_id() == 0) {
//                    continue;
//                }
//                if (TeamEnum.findSubDomainById(Math.toIntExact(rel.getTeam_id())).equals("NA")) {
//                    rel.setWp_id((long)im.getWp_id());
//                    wpSaveList.add(rel);
//                }
//            }
//        }
//
//        if (wpSaveList.size() > 0) {
//            imRelService.saveAll(wpSaveList);
//        }
//    }

//    /**
//     * Program -> Rel
//     * Program.team_id, member_idが存在しないとエラーになります
//     */
//    public void moveProgramToPRel2() {
//        List<Program> programList = programService.findall();
//        List<PRel> saveList = new ArrayList<>();
//        for (Program p : programList) {
//            if (p.getTeam_id() == null) {
//                continue;
//            }
//
//            PRel rel = new PRel();
//            rel.setProgram_id(p.getProgram_id());
//            List<Long> teamIdList = getLongIdList(p.getTeam_id());
//            for (Long teamId : teamIdList) {
//                if (teamId == 0) {
//                    continue;
//                }
//                if (rel.getTeam_id() == null) {
//                    rel.setTeam_id(teamId);
//                    saveList.add(rel);
//                } else {
//                    PRel newRel = new PRel();
//                    BeanUtils.copyProperties(rel, newRel);
//                    newRel.setTeam_id(teamId);
//                    saveList.add(newRel);
//                }
//            }
//        }
//
//        if (saveList.size() > 0) {
//            pRelService.saveAll(saveList);
//        }
//
//        // member
//        List<Program> memberProgramList = programList.stream().filter(e -> e.getMember_id() != null).collect(Collectors.toList());
//        List<PRel> memberSaveList = new ArrayList<>();
//
//        for (Program p : memberProgramList) {
//            if (p.getMember_id() == null) {
//                continue;
//            }
//
//            List<Long> memberIdList = getLongIdList(p.getMember_id());
//            for (Long mId : memberIdList) {
//                Long teamId = MemberEnum.getTeamIdById(mId);
//                PRel rel = pRelService.findByItemIdTeamIdMemberIdNull(p.getProgram_id(), teamId);
//
//                if (rel == null) {
//                    PRel newRel = new PRel();
//                    newRel.setProgram_id(p.getProgram_id());
//                    newRel.setTeam_id(teamId);
//                    newRel.setMember_id(mId);
//                    memberSaveList.add(newRel);
//                } else {
//                    rel.setMember_id(mId);
//                    memberSaveList.add(rel);
//                }
//            }
//        }
//
//        if (memberSaveList.size() > 0) {
//            pRelService.saveAll(memberSaveList);
//        }
//    }

    /**
     * teamIdリストを返します
     *
     * @param teamIdStr
     * @return
     */
    private List<Long> getLongIdList(String teamIdStr) {
        List<Long> resultList = new ArrayList<>();
        if (teamIdStr != null) {
            List<String> strList = Arrays.asList(teamIdStr.split(","));
            for (String s : strList) {
                System.out.println(s);
                try {
                    resultList.add(Long.valueOf(s));
                } catch (Exception e) {
                    System.out.println("Error");
                }
            }
        }
        return resultList;
    }

    /**
     * wpIdは総合ブログのを引き継いでいいか判断し、引き継ぐ場合はidを、引き継がない場合はnullを返します
     *
     * @param wpId
     * @return
     */
    private Long getWpIdJudge(Long wpId, Long teamId) {
        String subDomain = TeamEnum.findSubDomainById(teamId);
        if (subDomain.equals("NA")) {
            return wpId;
        } else {
            return 0L;
        }
    }
    /**
     * [From] BlogController
     * TODO: TmpController内のBlogControllerからお引越してきたメソッドたちはブログのチームごと分岐前のメソッド。走らせたらエラーになってしまうが、とりあえずエラー解消のためheader作成メソッドを持ってきました。もし走らせたいならblogControllerのheader作るメソッド（これと同名）de
     * TODO: エラーが出ないように治してね
     * 認証などどのリクエストでも必要なヘッダーをセットする(第2引数がリストではなくチーム1件の場合)。
     *
     * @param headers
     * @return
     */
    public HttpHeaders generalHeaderSet(HttpHeaders headers) {

        headers.add("Accept", MediaType.APPLICATION_JSON_VALUE);
        headers.setContentType(MediaType.APPLICATION_JSON);

        String auth = "";
        // TODO: 走らせたいならここをチームによってurl変更するように修正
        auth = new String(Base64.getEncoder().encode(setting.getBlogPw().getBytes()));
        headers.add("Authorization", "Basic " + auth);
        return headers;
    }

    /**
     * [From] BlogController
     * WpIdからポストの内容を取得します。
     * TODO: TmpController内のBlogControllerからお引越してきたメソッドたちはブログのチームごと分岐前のメソッド。走らせたらエラーになってしまうが、とりあえずエラー解消のためheader作成メソッドを持ってきました。もし走らせたいならblogControllerのheader作るメソッド（これと同名）de
     * TODO: エラーが出ないように治してね
     *
     * @param wpId
     * @return
     */
    public String requestPostData(String wpId) {
        // TODO: 走らせたいならここをチームによってurl変更するように修正
        String finalUrl = setting.getBlogWebUrl() + setting.getBlogApiPath() + "posts/" + wpId;
        HttpHeaders headers = generalHeaderSet(new HttpHeaders());
        return blogController.request(finalUrl, new HttpEntity<>(headers), HttpMethod.GET);
    }

    /**
     * [From] BlogController
     * 日付タグをWPとDBに登録します。
     * TODO: TmpController内のBlogControllerからお引越してきたメソッドたちはブログのチームごと分岐前のメソッド。走らせたらエラーになってしまうが、とりあえずエラー解消のためheader作成メソッドを持ってきました。もし走らせたいならblogControllerのheader作るメソッド（これと同名）de
     * TODO: エラーが出ないように治してね
     *
     * @param date
     * @return
     */
    public BlogTag registerTag(Date date) {
        // TODO: チームによってurlを変更
        String url = setting.getBlogWebUrl() + setting.getBlogApiPath() + "tags/";

        HttpHeaders h = generalHeaderSet(new HttpHeaders()) ;
        JSONObject jo = new JSONObject();
        jo.put("name", dateUtils.getYYYYMM(date));

        HttpEntity<String> request = new HttpEntity<>(jo.toString(), h);
        String res = blogController.request(url, request, HttpMethod.POST);

        JSONObject jsonObject1 = jsonUtils.createJsonObject(res);

        int yyyyMMId;
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

//    /**
//     * WPにあるがDBにないタグを保存する
//     *
//     */
//    public void getBlogTagNotSavedOnInfoDb() {
//        // TODO: チームによってurlを変更
//        String url = setting.getBlogWebUrl() + setting.getBlogApiPath() + "tags?_fields[]=id&_fields[]=name&_fields[]=link";
//
//        HttpHeaders headers = generalHeaderSet(new HttpHeaders());
//        JSONObject jsonObject = new JSONObject();
//        HttpEntity<String> request = new HttpEntity<>(jsonObject.toString(), headers);
//        String res = blogController.request(url, request, HttpMethod.GET);
//        List<BlogTag> blogTagList = new ArrayList<>();
//
//        try {
//            if (JsonUtils.isJsonArray(res)) {
//                JSONArray ja = new JSONArray(res);
//                for (int i=0;i<ja.length();i++) {
//                    Integer wpId = ja.getJSONObject(i).getInt("id");
//                    String tagName = ja.getJSONObject(i).getString("name").replaceAll("^\"|\"$", "");
//                    String link = ja.getJSONObject(i).getString("link").replaceAll("^\"|\"$", "");
//
//                    if (blogTagService.findBlogTagIdByTagName(tagName) == 0) {
//                        BlogTag blogTag = new BlogTag();
//                        blogTag.setWp_tag_id((long)wpId);
//                        blogTag.setTag_name(tagName);
//                        blogTag.setLink(link);
//                        blogTagList.add(blogTag);
//                    }
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        // infoDBに保存されていないタグは保存する
//        if (blogTagList.size() > 0) {
//            blogTagService.saveIfNotSaved(blogTagList);
//        }
//    }

    /**
     * [From] BlogController
     */
    public void tmpMethod() {
        String result = "[toc depth='5']";
        result = result + "<br /><h2>test from java</h2>\n<h2>h22</h2><h2>h23</h2><h3>h31</h3><h6>h6</h6>";

        System.out.println(result);

        HttpHeaders headers = generalHeaderSet(new HttpHeaders());
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("title", "java test");
        jsonObject.put("author", 1);
        jsonObject.put("status", "publish");
        jsonObject.put("content", result);
        HttpEntity<String> request = new HttpEntity<>(jsonObject.toString(), headers);

        String url = setting.getBlogWebUrl() + setting.getBlogApiPath() + "posts/";
        String res = blogController.request(url, request, HttpMethod.POST);
        System.out.println(res);
    }

    /**
     * [From] RakutenController
     * @param searchList
     * @return
     */
    public List<Item> search1(List<String> searchList) {
        List<Item> resultList = new ArrayList<>();

        for (String key : searchList) {
            String parameter = "&itemCode=" + key + "&elements=itemCode%2CitemCaption%2CitemName&" + setting.getRakutenAffiliId();
            JSONObject node = rakutenController.request(parameter);
            if (node != null) {

                if (node.has("Items") && !JsonUtils.isJsonArray(node.getString("Items"))) {
                    continue;
                }

                JSONArray items = node.getJSONArray("Items");
                for (int i=0; i<items.length();i++) {
                    try {
                        Item item = new Item();
                        item.setItem_code(key);
                        item.setItem_caption(StringUtilsMine.compressString(items.getJSONObject(i).getString("itemCaption").replaceAll("^\"|\"$", ""), 200));
                        item.setTitle(items.getJSONObject(i).getString("itemName").replaceAll("^\"|\"$", ""));
                        if (items.getJSONObject(i).has("mediumImageUrls") && JsonUtils.isJsonArray(items.getJSONObject(i).getString("mediumImageUrls"))) {
                            JSONArray imageArray = items.getJSONObject(i).getJSONArray("mediumImageUrls");
                            if (imageArray.length() > 0) {
                                item.setImage1(imageArray.getJSONObject(0).getString("imageUrl").replaceAll("^\"|\"$", ""));
                                if (imageArray.length() > 1) {
                                    item.setImage2(imageArray.getJSONObject(1).getString("imageUrl").replaceAll("^\"|\"$", ""));
                                }
                                if (imageArray.length() > 2) {
                                    item.setImage3(imageArray.getJSONObject(2).getString("imageUrl").replaceAll("^\"|\"$", ""));
                                }
                            }
                            resultList.add(item);
                        }
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                }
            }
        }
        return resultList;
    }

    /**
     * [From] AnalyzeController
     * 文字列から年月日をみつけ、返します。
     * 発売日、予約締切日などが引っかかる想定。
     *
     * @param text
     * @return
     */
    public List<String> extractYMDList(String text) {
        String regex = "20[0-2]{1}[0-9]{1}(.?)(1[0-2]{1}|0?[1-9])(.?)(3[01]|[12][0-9]|0?[1-9])";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);

        List<String> publishDateList = new ArrayList<>();
        while (matcher.find()) {
            publishDateList.add(matcher.group());
        }
        return publishDateList;
    }

    /**
     * [From] BlogController
     */
    public void postAllItemMaster() {
        Integer year = 2001;

        Map<ItemMaster, List<Item>> itemMasterListMap = new HashMap<>();
        while (year < 2022) {
            System.out.println("*** year: " + year);
            // itemMasterを集める
            List<ItemMaster> itemMasterList = itemMasterService.findByPublicationYearWpIdNull(year);
            System.out.println("itemMasterList.size: " + itemMasterList.size());
            // ひもづくitemを集める
            itemMasterList.forEach(e -> itemMasterListMap.put(e, itemService.gatherItems(e.getItem_m_id())));
            // itemMasterを投稿する
            if (itemMasterListMap.size() > 0) {
                itemMasterListMap.entrySet().stream().sorted(Comparator.comparing(e -> e.getKey().getPublication_date()));
                for (Map.Entry<ItemMaster, List<Item>> e : itemMasterListMap.entrySet()) {
                    System.out.println("item_m_id: " + e.getKey().getItem_m_id() + " itemList size: " + e.getValue().size());
                    blogController.postMasterItem(e.getKey(), e.getValue());
                }
            }
            ++year;
        }
    }

    /**
     * [From] BlogController
     * Tmpブログ新商品投稿メソッド(商品マスターごとに投稿するように修正)
     *
     */
    public void tmpItemPost(List<Item> itemList) {
        Map<ItemMaster, List<Item>> map = itemUtils.groupItem(itemList);
        // 対象はwp_idがnullのマスター商品
        Map<ItemMaster, List<Item>> targetMap = map.entrySet().stream()
                .filter(e -> IMRelService.getWpIdByItemMId(e.getKey().getItem_m_id()) == null || IMRelService.getWpIdByItemMId(e.getKey().getItem_m_id()).equals(0))
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
        // targetMapのマスタ商品をブログに投稿していく
        for (Map.Entry<ItemMaster, List<Item>> e : targetMap.entrySet()) {
            blogController.postMasterItem(e.getKey(), e.getValue());
        }
    }

    /**
     * [From] BlogController
     * 商品リストからアイキャッチメディアの登録がない商品だけを引き抜いてリストにし返却します。
     *
     * @param itemList
     * @return Item:
     */
//    public List<Item> selectBlogData(List<Item> itemList) {
//        List<Item> resultList = new ArrayList<>();
//        for (Item item : itemList) {
//            String result = requestPostData(item.getWp_id().toString());
//            Integer featuredMedia = blogController.extractMedia(result);
//            if (featuredMedia == 0) {
//                resultList.add(item);
//            }
//        }
//        return resultList;
//    }

//    /**
//     * [From] BlogController
//     * 商品（マスタじゃない）ページは下書きにする
//     *
//     */
//    public void deleteItemPosts() {
//
//        List<Long> wpIdList = itemService.collectWpId().stream().distinct().collect(Collectors.toList());
//
//        for (Long wpId : wpIdList) {
//            // WPにあるタグを取得する
//            String url = setting.getBlogWebUrl() + setting.getBlogApiPath() + "posts/" + wpId;
//            HttpHeaders headers = generalHeaderSet(new HttpHeaders());
//            JSONObject jsonObject = new JSONObject();
//            jsonObject.put("status","draft");
//            HttpEntity<String> request = new HttpEntity<>(jsonObject.toString(), headers);
//            blogController.request(url, request, HttpMethod.POST);
//        }
//    }

    /**
     * [From] BlogController
     * Nullが入ってるWPIDをコンソールに出力する
     */
    public void listPostsContainsNull() {
        int n = 1;
        boolean flg = true;
        while (flg) {
            System.out.println(n);
            String url = setting.getBlogWebUrl() + setting.getBlogApiPath() + "posts?status=publish&per_page=40&page=" + n;

            HttpHeaders headers = generalHeaderSet(new HttpHeaders());
            JSONObject jsonObject = new JSONObject();
            HttpEntity<String> request = new HttpEntity<>(jsonObject.toString(), headers);
            String res = blogController.request(url, request, HttpMethod.GET);

            try {
                if (!JsonUtils.isJsonArray(res)) {
                    continue;
                }

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
     * [From] BlogController
     * 公開中のブログポストのcontentを上書きする（楽天リンクをカードにした）
     *
     */
    public void updateContent() {
        int n = 1;
        String url = setting.getBlogWebUrl() + setting.getBlogApiPath() + "posts?status=publish&per_page=40&page=" + n;

        HttpHeaders headers = generalHeaderSet(new HttpHeaders());
        JSONObject jsonObject = new JSONObject();
        HttpEntity<String> request = new HttpEntity<>(jsonObject.toString(), headers);
        String res = blogController.request(url, request, HttpMethod.GET);

        try {
            if (JsonUtils.isJsonArray(res)) {
                JSONArray ja = new JSONArray(res);
                for (int i=0;i<ja.length();i++) {
                    Integer wpId = ja.getJSONObject(i).getInt("id");
                    url = setting.getBlogWebUrl() + setting.getBlogApiPath() + "posts/" + wpId;

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
                            String r = blogController.request(url, request1, HttpMethod.POST);
                            System.out.println(r);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        ++n;
    }

    /**
     * [From] BlogController
     * タイトルを書き換えます
     */
//    public void updateTitle() {
//        List<ItemMaster> itemMasterList = itemMasterService.findWpIdNotNull();
//
//        for (ItemMaster itemMaster : itemMasterList) {
//            HttpHeaders headers = generalHeaderSet(new HttpHeaders());
//            JSONObject jsonObject = new JSONObject();
//            String title = textController.createBlogTitle(itemMaster.getPublication_date(), itemMaster.getTitle());
//            jsonObject.put("title", title);
//            HttpEntity<String> request = new HttpEntity<>(jsonObject.toString(), headers);
//
//            String url = setting.getBlogWebUrl() + setting.getBlogApiPath() + "posts/" + itemMaster.getWp_id();
//            blogController.request(url, request, HttpMethod.POST);
//        }
//    }

    /**
     * [From] BlogController
     * 既存のWP投稿に対して、DBのタグby teamにyyyyMMタグを追加してWPにポストします。
     */
//    public void addTag() {
//        getBlogTagNotSavedOnInfoDb();
//        List<ItemMaster> itemMasterList = itemMasterService.findWpIdNotNull();
//
//        for (ItemMaster itemMaster : itemMasterList) {
//            HttpHeaders headers = generalHeaderSet(new HttpHeaders());
//            JSONObject jsonObject = new JSONObject();
//
//            Integer[] tags = new Integer[itemMaster.getTags().length + 1];
//            System.arraycopy(itemMaster.getTags(), 0, tags, 0, itemMaster.getTags().length);
//
//            int yyyyMMId = dateUtils.getBlogYYYYMMTag(itemMaster.getPublication_date());
//
//            // もし年月タグがまだ存在しなかったら先に登録する
//            if (yyyyMMId == 0) {
//                yyyyMMId = Math.toIntExact(registerTag(itemMaster.getPublication_date()).getBlog_tag_id());
//            }
//            tags[itemMaster.getTags().length] = yyyyMMId;
//            jsonObject.put("tags", tags);
//            HttpEntity<String> request = new HttpEntity<>(jsonObject.toString(), headers);
//
//            // 商品ページ投稿更新
//            String url = setting.getBlogWebUrl() + setting.getBlogApiPath() + "posts/" + itemMaster.getWp_id();
//            blogController.request(url, request, HttpMethod.POST);
//        }
//    }

    /**
     * [From] BlogController
     * 公開済み投稿でfeatured_mediaの設定があるものを返却します
     * TODO: メソッド一部間違えてたから使用箇所でまた実行した方がいいかも
     * @return Map\<WpId, featuredMediaId>
     */
    public Map<Integer, Integer> getPublishedWpIdFeaturedMediaList() {
        Map<Integer, Integer> resultMap = new HashMap<>();

        // リクエスト送信
        HttpHeaders headers = generalHeaderSet(new HttpHeaders());
        JSONObject jsonObject = new JSONObject();
        HttpEntity<String> request = new HttpEntity<>(jsonObject.toString(), headers);

        int n = 1;
        boolean nextFlg = true;

        while (nextFlg) {
            String url = setting.getBlogWebUrl() + setting.getBlogApiPath() + "posts?status=publish&_fields[]=featured_media&_fields[]=id&per_page=100&page=" + n;
            String res = blogController.request(url, request, HttpMethod.GET);

            // レスポンスを成形
            try {
                if (!JsonUtils.isJsonArray(res)) {
                    continue;
                }
                JSONArray ja = new JSONArray(res);

                if (ja.length() > 0) {
                    for (int i=0; i < ja.length(); i++) {
                        if (ja.getJSONObject(i).getInt("featured_media") != 0) {
                            resultMap.put(ja.getJSONObject(i).getInt("id"), ja.getJSONObject(i).getInt("featured_media"));
                        }
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

    /**
     * [From] BlogController
     * WP featuredMediaIDからそのメディアのeternalPathを取得し返却します
     * @param mediaIdList
     * @return Map<featuredMediaId, imagePath>
     */
    public Map<Integer, String> getMediaUrlByMediaId(List<Integer> mediaIdList) {
        Map<Integer, String> resultMap = new HashMap<>();

        int start = 0;
        int end = mediaIdList.size() -1;
        boolean next100Flg = true;

        if (end > 99) {
            end = 99;
        }

        List<String> mediaIrListStrList = new ArrayList<>();

        while (next100Flg && start < end) {
            String tmp = mediaIdList.subList(start, end).stream().map(Object::toString).collect(Collectors.joining(","));
            mediaIrListStrList.add(tmp);
            if (mediaIdList.size() > end + 1) {
                start += 100;
                end += 100;

                if (mediaIdList.size() -1 < end) {
                    end = mediaIdList.size() -1;
                }
            } else {
                next100Flg = false;
            }
        }

        System.out.println("mediaIrListStrList.size(): " + mediaIrListStrList.size());
        for (String mediaIdStr : mediaIrListStrList) {
            String res = getMediaUrl(mediaIdStr);
            // レスポンスを成形
            try {
                if (!JsonUtils.isJsonArray(res)) {
                    continue;
                }
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

    /**
     * [From] BlogController
     * 引数のmediaIdのWP eternalPathを取得し返却します。
     *
     * @param mediaId
     * @return eternalPath
     */
    private String getMediaUrl(String mediaId) {
        String url = setting.getBlogWebUrl() + setting.getBlogApiPath() + "media?slug=" + mediaId + "&_fields[]=id&_fields[]=source_url&per_page=100";

        // リクエスト送信
        HttpHeaders headers = generalHeaderSet(new HttpHeaders());
        JSONObject jsonObject = new JSONObject();
        HttpEntity<String> request = new HttpEntity<>(jsonObject.toString(), headers);
        return blogController.request(url, request, HttpMethod.GET);
    }
}

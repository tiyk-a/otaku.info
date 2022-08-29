package otaku.info.controller;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import otaku.info.entity.*;
import otaku.info.service.*;
import otaku.info.setting.Log4jUtils;
import otaku.info.setting.Setting;
import otaku.info.utils.JsonUtils;
import otaku.info.utils.StringUtilsMine;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Controller
public class TmpController {

    final Logger logger = Log4jUtils.newConsoleCsvAllLogger("TmpController");

    @Autowired
    Setting setting;

    @Autowired
    ItemService itemService;

    @Autowired
    IMService imService;

    @Autowired
    ProgramService programService;

    @Autowired
    PMService pmService;

    @Autowired
    RakutenController rakutenController;

    @Autowired
    BlogController blogController;

    @Autowired
    BlogPostService blogPostService;

    public void adjustArr() {
        // teamArr
        List<Item> itemList = itemService.findbyInvalidArr();
        List<Item> updateItemList = new ArrayList<>();
        for (Item item : itemList) {
            if (item.getTeamArr() != null && item.getTeamArr().contains("[")) {
                item.setTeamArr(StringUtilsMine.removeBrackets(item.getTeamArr()));
            }

            if (item.getMemArr() != null && item.getMemArr().contains("[")) {
                item.setMemArr(StringUtilsMine.removeBrackets(item.getMemArr()));
            }
            updateItemList.add(item);
        }

        List<IM> imList = imService.findbyInvalidArr();
        List<IM> updateImList = new ArrayList<>();
        for (IM im : imList) {
            if (im.getTeamArr() != null && im.getTeamArr().contains("[")) {
                im.setTeamArr(StringUtilsMine.removeBrackets(im.getTeamArr()));
            }

            if (im.getMemArr() != null && im.getMemArr().contains("[")) {
                im.setMemArr(StringUtilsMine.removeBrackets(im.getMemArr()));
            }
            updateImList.add(im);
        }

        List<Program> programList = programService.findbyInvalidArr();
        List<Program> updatePList = new ArrayList<>();
        for (Program im : programList) {
            if (im.getTeamArr() != null && im.getTeamArr().contains("[")) {
                im.setTeamArr(StringUtilsMine.removeBrackets(im.getTeamArr()));
            }

            if (im.getOn_air_date() != null && im.getMemArr().contains("[")) {
                im.setMemArr(StringUtilsMine.removeBrackets(im.getMemArr()));
            }
            updatePList.add(im);
        }

        List<PM> pmList = pmService.findbyInvalidArr();
        List<PM> updatePmList = new ArrayList<>();
        for (PM im : pmList) {
            if (im.getTeamArr() != null && im.getTeamArr().contains("[")) {
                im.setTeamArr(StringUtilsMine.removeBrackets(im.getTeamArr()));
            }

            if (im.getMemArr() != null && im.getMemArr().contains("[")) {
                im.setMemArr(StringUtilsMine.removeBrackets(im.getMemArr()));
            }
            updatePmList.add(im);
        }

        itemService.saveAll(updateItemList);
        imService.saveAll(updateImList);
        programService.saveAll(updatePList);
        pmService.saveAll(updatePmList);
    }

    /**
     * teamIdリストを返します
     *
     * @param teamIdStr
     * @return
     */
    private List<Long> getLongIdList(String teamIdStr) {
        List<Long> resultList = new ArrayList<>();
        if (teamIdStr != null) {
            String[] strList = teamIdStr.split(",");
            for (String s : strList) {
                logger.debug(s);
                try {
                    resultList.add(Long.valueOf(s));
                } catch (Exception e) {
                    logger.debug("Error");
                }
            }
        }
        return resultList;
    }

    /**
     * [From] BlogController
     * TmpController内のBlogControllerからお引越してきたメソッドたちはブログのチームごと分岐前のメソッド。走らせたらエラーになってしまうが、とりあえずエラー解消のためheader作成メソッドを持ってきました。もし走らせたいならblogControllerのheader作るメソッド（これと同名）de
     * エラーが出ないように治してね
     * 認証などどのリクエストでも必要なヘッダーをセットする(第2引数がリストではなくチーム1件の場合)。
     *
     * @param headers
     * @return
     */
    public HttpHeaders generalHeaderSet(HttpHeaders headers) {

        headers.add("Accept", MediaType.APPLICATION_JSON_VALUE);
        headers.setContentType(MediaType.APPLICATION_JSON);

        String auth = "";
        // 走らせたいならここをチームによってurl変更するように修正
        auth = new String(Base64.getEncoder().encode(setting.getApiPw().getBytes()));
        headers.add("Authorization", "Basic " + auth);
        return headers;
    }

    /**
     * [From] BlogController
     * WpIdからポストの内容を取得します。
     * TmpController内のBlogControllerからお引越してきたメソッドたちはブログのチームごと分岐前のメソッド。走らせたらエラーになってしまうが、とりあえずエラー解消のためheader作成メソッドを持ってきました。もし走らせたいならblogControllerのheader作るメソッド（これと同名）de
     * エラーが出ないように治してね
     *
     * @param wpId
     * @return
     */
    public String requestPostData(String wpId) {
        // 走らせたいならここをチームによってurl変更するように修正
        String finalUrl = setting.getBlogWebUrl() + setting.getBlogApiPath() + "posts/" + wpId;
        HttpHeaders headers = generalHeaderSet(new HttpHeaders());
        return blogController.request(finalUrl, new HttpEntity<>(headers), HttpMethod.GET, "requestPostData()");
    }

    /**
     * [From] BlogController
     */
    public void tmpMethod() {
        String result = "[toc depth='5']";
        result = result + "<br /><h2>test from java</h2>\n<h2>h22</h2><h2>h23</h2><h3>h31</h3><h6>h6</h6>";

        logger.debug(result);

        HttpHeaders headers = generalHeaderSet(new HttpHeaders());
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("title", "java test");
        jsonObject.put("author", 1);
        jsonObject.put("status", "publish");
        jsonObject.put("content", result);
        HttpEntity<String> request = new HttpEntity<>(jsonObject.toString(), headers);

        String url = setting.getBlogWebUrl() + setting.getBlogApiPath() + "posts/";
        String res = blogController.request(url, request, HttpMethod.POST, "tmpMethod()");
        logger.debug(res);
    }

    /**
     * [From] RakutenController
     * @param searchList
     * @return
     */
    public List<Item> search1(List<String> searchList, Long teamId) throws InterruptedException {
        List<Item> resultList = new ArrayList<>();

        for (String key : searchList) {
            String parameter = "&itemCode=" + key + "&elements=itemCode%2CitemCaption%2CitemName&" + setting.getRakutenAffiliId();
            JSONObject node = rakutenController.request(parameter, teamId);
            if (node != null && !node.equals("")) {

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
                        resultList.add(item);
                    } catch (Exception e) {
                        logger.debug(e.getMessage());
                    }
                }
            } else {
                logger.info("Rakutenでデータが見つかりませんでした");
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
     * Nullが入ってるWPIDをコンソールに出力する
     */
    public void listPostsContainsNull() {
        int n = 1;
        boolean flg = true;
        while (flg) {
            logger.debug(n);
            String url = setting.getBlogWebUrl() + setting.getBlogApiPath() + "posts?status=publish&per_page=40&page=" + n;

            HttpHeaders headers = generalHeaderSet(new HttpHeaders());
            JSONObject jsonObject = new JSONObject();
            HttpEntity<String> request = new HttpEntity<>(jsonObject.toString(), headers);
            String res = blogController.request(url, request, HttpMethod.GET, "listPostsContainsNull()");

            try {
                if (!JsonUtils.isJsonArray(res)) {
                    continue;
                }

                JSONArray ja = new JSONArray(res);
                for (int i=0;i<ja.length();i++) {
                    Integer wpId = ja.getJSONObject(i).getInt("id");
                    Integer media = ja.getJSONObject(i).getInt("featured_media");
                    if (media > 0) {
                        logger.debug(wpId + ":" + media);
                    }
                }
            } catch (Exception e) {
                flg = false;
                logger.error("Tmp Controllerエラー");
                e.printStackTrace();
            }
            ++n;
        }
    }

    /**
     * [From] BlogController
     * 公開済み投稿でfeatured_mediaの設定があるものを返却します
     * メソッド一部間違えてたから使用箇所でまた実行した方がいいかも
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
            String res = blogController.request(url, request, HttpMethod.GET, "getPublishedWpIdFeaturedMediaList()");

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
                logger.error("Tmp Controllerエラー");
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

        logger.debug("mediaIrListStrList.size(): " + mediaIrListStrList.size());
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
                logger.error("Tmp Controllerエラー");
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
        return blogController.request(url, request, HttpMethod.GET, "getMediaUrl()");
    }
}

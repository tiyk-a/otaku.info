package otaku.info.controller;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.codehaus.jettison.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import otaku.info.batch.scheduler.Scheduler;
import otaku.info.dto.TwiDto;
import otaku.info.entity.Item;
import otaku.info.entity.ItemMaster;
import otaku.info.searvice.*;
import otaku.info.setting.Setting;
import otaku.info.utils.DateUtils;
import otaku.info.utils.ItemUtils;
import otaku.info.utils.StringUtilsMine;


/**
 * 楽天での商品検索指示〜Twitterポスト指示まで。
 *
 */
@RestController
@RequestMapping("/")
public class SampleController {

    @Autowired
    private RakutenController rakutenController;

    @Autowired
    private TextController textController;

    @Autowired
    private Controller controller;

    @Autowired
    private AnalyzeController analyzeController;

    @Autowired
    private PythonController pythonController;

    @Autowired
    private BlogController blogController;

    @Autowired
    private ImageController imageController;

    @Autowired
    private ItemService itemService;

    @Autowired
    private TeamService teamService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private ProgramService programService;

    @Autowired
    private BlogTagService blogTagService;

    @Autowired
    private ItemMasterService itemMasterService;

    @Autowired
    Scheduler scheduler;

    @Autowired
    private Setting setting;

    @Autowired
    private ItemUtils itemUtils;

    @Autowired
    private DateUtils dateUtils;

    private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("h:m");
    /**
     * URLでアクセスできるtmpのメソッドです。
     * 任意に中身を変えます、テスト用。
     * Currently: アイキャッチ画像の画像パスをDBに取り込む→楽天に飛ばさず画像を表示できるようになる
     *
     * @return
     */
    @GetMapping("/tmpMethod")
    public String tempMethod() {

        // publishedのwpId&featured_mediaを取得、featured_mediaが0のものは抜く
        Map<Integer, Integer> wpIdFeaturedMediaMap = blogController.getPublishedWpIdFeaturedMediaList();
        System.out.println("wpIdFeaturedMediaMap.size(): " + wpIdFeaturedMediaMap.size());
        List<Integer> wpIdList = wpIdFeaturedMediaMap.entrySet().stream().filter(e -> e.getValue() != 0).map(Map.Entry::getKey).collect(Collectors.toList());
        System.out.println("wpIdList.size(): " + wpIdList.size());

        // featured_media IDからメディアURLを取得する
        Map<Integer, String> mediaIdMediaUrlMap = blogController.getMediaUrlByMediaId(new ArrayList<>(wpIdFeaturedMediaMap.values()));
        System.out.println("mediaIdMediaUrlMap.size(): " + mediaIdMediaUrlMap.size());

        // 画像パス(itemMaster.url)がnullのitemMasterを集める
        List<ItemMaster> itemMasterList = itemMasterService.findByWpIdUrlNullList(wpIdList);
        System.out.println("itemMasterList.size(): " + itemMasterList.size());

        itemMasterList.forEach(e -> e.setUrl(mediaIdMediaUrlMap.get(e.getWp_id())));
        itemMasterService.saveAll(itemMasterList);
        System.out.println("itemMasterList.size(): " + itemMasterList.size());
        return "done";
    }

    /**
     * ブラウザとかでテスト投稿（1件）がいつでもできるメソッド
     *
     * @param artistId
     * @return
     * @throws JSONException
     */
    @GetMapping("/twi/{artistId}")
    public String sample1(@PathVariable String artistId) throws JSONException {
        Item tmp = new Item();
        tmp.setSite_id(1);
        tmp.setItem_code("adcfvgbhnaa");
        tmp.setTeam_id("1");
        itemService.saveItem(tmp);

        List<String> list = controller.affiliSearchWord(artistId);
        List<String> itemCodeList = rakutenController.search(list);

        itemCodeList = itemService.findNewItemList(itemCodeList);

        List<Item> newItemList = new ArrayList<>();
        if (itemCodeList.size() > 0) {
            newItemList = rakutenController.getDetailsByItemCodeList(itemCodeList);
        }

        System.out.println("１２：楽天APIから受信したItemのリストをDB保存します");
        List<Item> savedItemList = rakutenController.saveItems(newItemList);
        System.out.println(ToStringBuilder.reflectionToString(savedItemList, ToStringStyle.MULTI_LINE_STYLE));
        List<Item> itemList = itemService.findAll();
        System.out.println(ToStringBuilder.reflectionToString(itemList, ToStringStyle.MULTI_LINE_STYLE));
        return itemList.toString();
    }

    @GetMapping("/batch/{id}")
    public String batch(@PathVariable String id) {
        int i = Integer.parseInt(id);
        switch (i) {
            case 1:
                System.out.println("---run1楽天新商品検索 START---");
                scheduler.run1();
                System.out.println("---run1楽天新商品検索 END---");
                break;
            case 2:
                System.out.println("---run2未発売商品リマインダー START---");
                scheduler.run2();
                System.out.println("---run2未発売商品リマインダー END---");
                break;
            case 4:
                System.out.println("---run4商品発売日アナウンス START---");
                scheduler.run4();
                System.out.println("---run4商品発売日アナウンス END---");
                break;
            case 5:
                System.out.println("---run5楽天新商品検索（個人） START---");
                scheduler.run5();
                System.out.println("---run5楽天新商品検索（個人） END---");
                break;
            case 6:
                System.out.println("---run6TV検索 START---");
                scheduler.run6();
                System.out.println("---run6TV検索 END---");
                break;
            case 7:
                System.out.println("---run7TV番組投稿処理 START---");
                scheduler.run7();
                System.out.println("---run7TV番組投稿処理 END---");
                break;
            case 9:
                System.out.println("---run9TVアラート START---");
                scheduler.run9();
                System.out.println("---run9TVアラート END---");
            case 10:
                System.out.println("---run10DB商品アフェリリンク更新 START---");
                scheduler.run10();
                System.out.println("---run10DB商品アフェリリンク更新 END---");
                break;
            case 11:
                // 固定ページ「新商品情報」を更新する
                System.out.println("---run11Blog Update START---");
                scheduler.run11();
                System.out.println("---run11Blog Update END---");
                break;
            case 12:
                // 商品の情報を投稿する
                System.out.println("---run12Blog画像設定 START---");
                scheduler.run12();
                System.out.println("---run12Blog画像設定 END---");
                break;
            case 13: // tmpメソッド
                // ショートコードが反映できるか
                blogController.tmpMethod();
                break;
            case 14:
                // 商品の情報を投稿する
                System.out.println("---Tmpブログ新商品投稿メソッドSTART---");
//                List<Item> itemList = itemService.findNotDeleted();
//                blogController.tmpItemPost(itemList);
                System.out.println("---Tmpブログ新商品投稿メソッドEND---");
                break;
        }
            return "Done";
    }

    /**
     * Itemに不適切な商品が入ってしまっていたらItemId指定でdel_flgをonにします。
     * パラメータの指定：00-001-22（数字をハイフンで区切ることで複数商品を1リクエストで処理）
     *
     * @return String
     */
    @GetMapping("/moveToDelItem/{itemIdListStr}")
    public String moveToDelItem(@PathVariable String itemIdListStr) {
        List<Long> itemIdList = new ArrayList<>();
        List.of(itemIdListStr.split("-")).forEach(e -> itemIdList.add(Long.valueOf(e)));

        if (itemIdList.size() == 0) return "No Id provided";

        List<Long> notFoundIdList = new ArrayList<>();
        int successCount = 0;
        String result = "";
        for (Long itemId : itemIdList) {
            Item item = itemService.findByItemId(itemId).orElse(new Item());
            if (item.getItem_id() == null) {
                notFoundIdList.add(itemId);
            } else {
                successCount ++;
                item.setDel_flg(true);
                itemService.saveItem(item);
                result = result + "【" + successCount + "】itemId:" + item.getItem_id() + "Title: " + item.getTitle() + "-------------------";
            }

            if (notFoundIdList.size() > 0) {
                result = result + "Not found item";
                for (Long id : notFoundIdList) {
                    result = result + " item_id=" + id;
                }
            }
        }
        return result;
    }

    /**
     * バッチで動かしてる定時楽天検索→Pythonにツイート命令を出すまでのメソッド
     *
     * @param teamId
     * @param artist
     * @return
     * @throws JSONException
     */
    public String searchItem(Long teamId, String artist, Long memberId) throws JSONException, ParseException {
        boolean isTeam = memberId == 0;
        List<String> list = controller.affiliSearchWord(artist);
        List<String> itemCodeList = rakutenController.search(list);

        itemCodeList = itemService.findNewItemList(itemCodeList);

        List<Item> newItemList = new ArrayList<>();
        if (itemCodeList.size() > 0) {
            newItemList = rakutenController.getDetailsByItemCodeList(itemCodeList);
        }

        // 検索の誤引っ掛かりした商品をストアするリスト
        List<Item> removeList = new ArrayList<>();

        if (newItemList.size() > 0) {
            for (Item item : newItemList) {
                // 年月日のデータを集める
                Map<String, List<Date>> resultMap = analyzeController.extractPublishDate(item.getItem_caption());
                if (resultMap.get("publishDateList").size() == 0) {
                    Map<String, List<Date>> resultMap2 = analyzeController.extractPublishDate(item.getTitle());
                    if (resultMap.get("reserveDueList").size() == 0 && resultMap2.get("reserveDueList").size() > 0) {
                        resultMap.put("reserveDueList", resultMap2.get("reserveDueList"));
                    }
                    if (resultMap.get("publishDateList").size() == 0 && resultMap2.get("publishDateList").size() > 0) {
                        resultMap.put("publishDateList", resultMap2.get("publishDateList"));
                    }
                    if (resultMap.get("dateList").size() == 0 && resultMap2.get("dateList").size() > 0) {
                        resultMap.put("dateList", resultMap2.get("dateList"));
                    }
                }

                if (resultMap.get("publishDateList").size() > 0 || resultMap.get("dateList").size() > 0 ) {
                    item.setPublication_date(resultMap.get("publishDateList").get(0));
                    if (item.getPublication_date() == null) {
                        item.setPublication_date(resultMap.get("dateList").get(0));
                    }
                }

                Item savedItem = itemService.findByItemCode(item.getItem_code()).orElse(null);

                // 既に商品が登録されていたら（同一商品コード）、チーム名とメンバー名は追加する。
                if (savedItem != null) {
                    String savedTeamId = item.getTeam_id();
                    if (savedTeamId == null || savedTeamId.equals("")) {
                        item.setTeam_id(teamId.toString());
                    } else {
                        if (!savedTeamId.contains(teamId.toString())) {
                            item.setTeam_id(savedTeamId.concat("," + teamId));
                        }
                    }

                    // 個人検索の場合、member_idを設定する。
                    if (!isTeam) {
                        String savedMemberId = item.getMember_id();
                        if (savedMemberId == null || savedMemberId.equals("")) {
                            item.setMember_id(memberId.toString());
                        } else {
                            if (!savedMemberId.contains(memberId.toString())) {
                                item.setMember_id(savedMemberId.concat("," + memberId));
                            }
                        }
                    }
                } else {
                    // 既存商品がなかったら新規登録
                    item.setTeam_id(teamId.toString());

                    // 個人検索の場合、member_idを設定する。
                    if (!isTeam) {
                        item.setMember_id(memberId.toString());
                    }
                }

                // チームで削除チェック（チーム）合致orメンバーで削除チェック（メンバー）合致なら削除リストに追加
                if ((isTeam && addToRemoveList(item, artist, true)) || (!isTeam && addToRemoveList(item, artist, false))) {
                    // 削除対象であれば削除リストに入れる。
                    removeList.add(item);
                }
            }
        }

        // 保存する商品リストから不要な商品リストを削除する
        newItemList.removeAll(removeList);

        // 不要商品リストに入った商品を商品テーブルに格納する
        if (removeList.size() > 0) {
            System.out.println("違う商品を保存します");
            removeList.forEach(e -> e.setDel_flg(true));
            itemService.saveAll(removeList);
        }

        // 正常商品を登録する
        List<Item> savedItemList = new ArrayList<>();
        if (newItemList.size() > 0) {
            System.out.println("商品を保存します");
            newItemList.forEach(e -> System.out.println(e.getTitle()));
            savedItemList = rakutenController.saveItems(newItemList);
        }

        // itemMasterに接続（追加/新規登録）し、itemのitem_m_idも更新する
        Map<ItemMaster, List<Item>> itemMasterListMap = itemUtils.groupItem(savedItemList);

        // ブログ投稿（新規/更新）を行う
        blogController.postOrUpdate(new ArrayList<>(itemMasterListMap.keySet()));

        // マップから、新しいitemMasterだけ引き出す。5分以内に登録されたitemMasterが対象、とすればいいだろうか
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.MINUTE, -5);
        List<ItemMaster> newItemMasterList = new ArrayList<>();
        itemMasterListMap.entrySet().stream().filter(e -> e.getKey().getCreated_at().after(calendar.getTime())).forEach(e -> newItemMasterList.add(e.getKey()));

        // 保存した商品が新しい商品マスタの場合、Tweetする
        if (newItemMasterList.size() > 0) {
            System.out.println("保存したItemMasterをTweetします");
            for (ItemMaster itemMaster: newItemMasterList) {

                // 楽天リンクなどで必要なためリストの一番目のitemを取得
                Item item = itemMasterListMap.get(itemMaster).get(0);

                if (itemMaster.getPublication_date() != null && itemMaster.getPublication_date().after(Date.from(LocalDateTime.now().atZone(ZoneId.of("Asia/Tokyo")).toInstant()))) {
                    System.out.println(itemMaster.getTitle());
                    String[] teamIdArr = itemMaster.getTeam_id().split(",");
                    TwiDto twiDto = new TwiDto(item.getTitle(), item.getUrl(), itemMaster.getPublication_date(), null, Long.parseLong(teamIdArr[teamIdArr.length - 1]));
                    String result;
                    String memberIdStr = itemMaster.getMember_id();
                    if (memberIdStr != null && !memberIdStr.equals("")) {
                        List<Long> memberIdList = new ArrayList<>();
                        List.of(memberIdStr.split(",")).forEach(e -> memberIdList.add((long) Integer.parseInt(e)));
                        if (memberIdList.size() == 1) {
                            String memberName = memberService.getMemberName(memberIdList.get(0));
                            result = textController.twitterPerson(twiDto, memberName);
                        } else {
                            List<String> memberNameList = memberService.getMemberNameList(memberIdList);
                            result = textController.twitterPerson(twiDto, memberNameList.get(memberNameList.size() -1));
                        }
                    } else {
                        result = textController.twitter(twiDto);
                    }
                    if (itemMaster.getTeam_id() != null) {
                        // Twitter投稿
                        pythonController.post(Math.toIntExact(Long.parseLong(teamIdArr[teamIdArr.length - 1])), result);
                    } else {
                        System.out.println("TeamがNullのためTweetしません" + item.getItem_code() + ":" + item.getTitle());
                        break;
                    }
                } else {
                    System.out.println("未来商品ではないのでTweetしません");
                    System.out.println(item.getTitle());
                }
            }
        }
        return "Ok";
    }

    /**
     * 商品が登録不要なものかどうかチェックする
     *
     * @param item
     * @param name
     * @param isTeam チーム:メンバー
     * @return
     */
    private boolean addToRemoveList(Item item, String name, boolean isTeam) {

        // 検索の誤引っ掛かりを削除するため、アーティスト名がタイトルとdescriptionに含まれていないものを別リストに入れる
        String mnemonic;
        if (isTeam) {
            mnemonic = teamService.getMnemonic(name);
        } else {
            mnemonic = memberService.getMnemonic(name);
        }
        if (!StringUtilsMine.arg2ContainsArg1(name, item.getTitle()) && !StringUtilsMine.arg2ContainsArg1(name, item.getItem_caption())) {
            if (mnemonic != null) {
                if (!StringUtilsMine.arg2ContainsArg1(mnemonic, item.getTitle()) && !StringUtilsMine.arg2ContainsArg1(mnemonic, item.getItem_caption())) {
                    return true;
                }
            } else {
                return true;
            }
        }

        // 非公式商品(「ジャニーズ研究会」「J-GENERATION」)を削除リストに入れる（上のtitle/descriptionのアーティスト名チェックで引っかかっていない場合）
        if (StringUtilsMine.arg2ContainsArg1("ジャニーズ研究会", item.getTitle()) || StringUtilsMine.arg2ContainsArg1("J-GENERATION", item.getTitle())) {
            return true;
        }

        // どれも引っ掛からなかったらfalse
        return false;
    }
}


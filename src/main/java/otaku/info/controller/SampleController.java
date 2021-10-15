package otaku.info.controller;

import java.io.*;
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
import otaku.info.dto.ItemRelElems;
import otaku.info.dto.TwiDto;
import otaku.info.entity.*;
import otaku.info.enums.PublisherEnum;
import otaku.info.enums.TeamEnum;
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
    private YahooController yahooController;

    @Autowired
    private TwTextController twTextController;
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
    private TmpController tmpController;

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
    private IRelService iRelService;

    @Autowired
    private IRelMemService iRelMemService;

    @Autowired
    private IMRelService iMRelService;

    @Autowired
    private IMRelMemService imRelMemService;

    @Autowired
    Scheduler scheduler;

    @Autowired
    private Setting setting;

    @Autowired
    private ItemUtils itemUtils;

    @Autowired
    private DateUtils dateUtils;

    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("h:m");
    /**
     * URLでアクセスできるtmpのメソッドです。
     * 任意に中身を変えます、テスト用。
     * Currently: アイキャッチ画像の画像パスをDBに取り込む→楽天に飛ばさず画像を表示できるようになる
     *
     * @return
     */
    @GetMapping("/tmpMethod")
    public String tempMethod() throws FileNotFoundException {

        // Method1
//        // publishedのwpId&featured_mediaを取得、featured_mediaが0のものは抜く
//        Map<Integer, Integer> wpIdFeaturedMediaMap = tmpController.getPublishedWpIdFeaturedMediaList();
//        System.out.println("wpIdFeaturedMediaMap.size(): " + wpIdFeaturedMediaMap.size());
//        List<Integer> wpIdList = wpIdFeaturedMediaMap.entrySet().stream().filter(e -> e.getValue() != 0).map(Map.Entry::getKey).collect(Collectors.toList());
//        System.out.println("wpIdList.size(): " + wpIdList.size());
//
//        // featured_media IDからメディアURLを取得する
//        Map<Integer, String> mediaIdMediaUrlMap = tmpController.getMediaUrlByMediaId(new ArrayList<>(wpIdFeaturedMediaMap.values()));
//        System.out.println("mediaIdMediaUrlMap.size(): " + mediaIdMediaUrlMap.size());
//
//        // 画像パス(itemMaster.url)がnullのitemMasterを集める
//        List<ItemMaster> itemMasterList = itemMasterService.findByWpIdUrlNullList(wpIdList);
//        System.out.println("itemMasterList.size(): " + itemMasterList.size());
//
//        itemMasterList.forEach(e -> e.setUrl(mediaIdMediaUrlMap.get(e.getWp_id())));
//        itemMasterService.saveAll(itemMasterList);
//        System.out.println("itemMasterList.size(): " + itemMasterList.size());

        // Method3全てのitemタイトルを分析して、出版社・雑誌名などを取得したい。そしてitemMasterのtitle作成につなげたいYahoo APIを使用したい
//        String result = "";
//        // ~/Desktop/title.txtにpro環境から落としてきたitem.titleの値を入れておく。それを読んで取り込んでyahoo apiでkeyを引き出してあげる
//        try (BufferedReader br = new BufferedReader(new FileReader("/Users/chiara/Desktop/title.txt"))) {
//            String line;
//            while ((line = br.readLine()) != null) {
//                List<String> tmpList = yahooController.extractKeywords(line);
//                if (tmpList != null && tmpList.size() > 0) {
//                    result = result + String.join(" ", tmpList) + "\n";
//                }
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        System.out.println(setting.getTest());
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
    public String sample1(@PathVariable String artistId) throws InterruptedException {
        Item tmp = new Item();
        tmp.setSite_id(1);
        tmp.setItem_code("adcfvgbhnaa");
        IRel ir = new IRel();
        Item savedItem = itemService.saveItem(tmp);
        ir.setItem_id(savedItem.getItem_id());
        ir.setTeam_id(1L);
        iRelService.save(ir);

        List<String> list = controller.affiliSearchWord(artistId);
        List<String> itemCodeList = rakutenController.search(list);

        itemCodeList = itemService.findNewItemList(itemCodeList);

        List<Item> newItemList = new ArrayList<>();
        if (itemCodeList.size() > 0) {
            newItemList = rakutenController.getDetailsByItemCodeList(itemCodeList);
        }

        System.out.println("１２：楽天APIから受信したItemのリストをDB保存します");
        List<Item> savedItemList = itemService.saveAll(newItemList);
        System.out.println(ToStringBuilder.reflectionToString(savedItemList, ToStringStyle.MULTI_LINE_STYLE));
        List<Item> itemList = itemService.findAll();
        System.out.println(ToStringBuilder.reflectionToString(itemList, ToStringStyle.MULTI_LINE_STYLE));
        return itemList.toString();
    }

    @GetMapping("/batch/{id}")
    public String batch(@PathVariable String id) throws InterruptedException {
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
            case 3:
                blogController.chkWpId();
                blogController.chkWpIdByBlog();
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
            case 8:
                List<String> list = TeamEnum.getAllSubDomain();
                List<String> deleted = new ArrayList<>();
                for (String l : list) {
                    if (!deleted.contains(l)) {
                        deleted.add(l);
                    }
                }
                for (String s : deleted) {
                    blogController.insertTags(s);
                }
                break;
            case 9:
                System.out.println("---run9TVアラート START---");
                scheduler.run9();
                System.out.println("---run9TVアラート END---");
                break;
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
            case 13: // tmpメソッド
                // ショートコードが反映できるか
                tmpController.tmpMethod();
                break;
            case 14:
                // 商品の情報を投稿する
                System.out.println("---Tmpブログ新商品投稿メソッドSTART---");
//                List<Item> itemList = itemService.findNotDeleted();
//                blogController.tmpItemPost(itemList);
                System.out.println("---Tmpブログ新商品投稿メソッドEND---");
                break;
            case 15:
                // irelの重複を解消
                orderiRel();
//                orderiRel2();
//                orderiRel3();
                break;
            case 16:
                orderM();
//                orderM2();
//                orderM3();
                break;
        }
            return "Done";
    }

    /**
     * irelの整理(からteamを入れてあげる)
     */
    private void orderiRel() {
//        ・全部取得ー＞itemでまとめる→teamでまとめる
        List<IRel> iRelList = iRelService.findAll();
        List<IRel> updateList = new ArrayList<>();
        List<IRel> removeList = new ArrayList<>();
        for (IRel rel : iRelList) {
            // もしteamIdがなかったら同じitemIdを持つレコードとってくる
            if (rel.getTeam_id() == 0) {
                List<IRel> groupList = iRelService.findByItemIdTeamIdNotNull(rel.getItem_id());
                if (groupList.size() > 0) {
                    IRel subRel = groupList.get(0);
                    rel.setTeam_id(subRel.getTeam_id());
                    updateList.add(rel);
                } else {
                    removeList.add(rel);
                }
            }
        }
        iRelService.saveAll(updateList);
        iRelService.removeAll(removeList);
    }

    /**
     * irelの整理(重複を削除してあげる)
     */
//    private void orderiRel2() {
////        ・全部取得ー＞itemでまとめる→teamでまとめる
//        List<IRel> iRelList = iRelService.findAll();
//        List<IRel> opeList = iRelService.findAll();
//        List<IRel> removeList = new ArrayList<>();
//        for (IRel rel : iRelList) {
//            for (IRel ope : opeList) {
//                // 一致するレコードがあったら
//                if (rel.getI_rel_id() != ope.getI_rel_id() && rel.getItem_id() == ope.getItem_id() && rel.getTeam_id() == ope.getTeam_id() && (rel.getMember_id() == ope.getMember_id() || (rel.getMember_id() == null && ope.getMember_id() == null))) {
//                    if (rel.getI_rel_id() > ope.getI_rel_id()) {
//                        if (!removeList.contains(rel)) {
//                            removeList.add(rel);
//                        }
//                    } else {
//                        if (!removeList.contains(ope)) {
//                            removeList.add(ope);
//                        }
//                    }
//                }
//            }
//        }
//        iRelService.removeAll(removeList);
//    }

    /**
     * irelの整理(memberを入れてあげる)
     */
//    private void orderiRel3() {
////        ・全部取得ー＞itemでまとめる→teamでまとめる
//        List<IRel> iRelList = iRelService.findAll();
//        List<IRelMem> relMenList = new ArrayList<>();
//        for (IRel rel : iRelList) {
//            if (rel.getMember_id() != null) {
//                IRelMem relMen = new IRelMem(null, rel.getI_rel_id(), rel.getMember_id(), null, null);
//                relMenList.add(relMen);
//            }
//        }
//        iRelMemService.saveAll(relMenList);
//    }

    /**
     * irelの整理(からteamを入れてあげる)
     */
    private void orderM() {
//        ・全部取得ー＞itemでまとめる→teamでまとめる
        List<IMRel> iRelList = iMRelService.findAll();
        List<IMRel> updateList = new ArrayList<>();
        List<IMRel> removeList = new ArrayList<>();
        for (IMRel rel : iRelList) {
            // もしteamIdがなかったら同じitemIdを持つレコードとってくる
            if (rel.getTeam_id() == 0) {
                List<IMRel> groupList = iMRelService.findByItemIdTeamIdNotNull(rel.getItem_m_id());
                if (groupList.size() > 0) {
                    IMRel subRel = groupList.get(0);
                    rel.setTeam_id(subRel.getTeam_id());
                    updateList.add(rel);
                } else {
                    removeList.add(rel);
                }
            }
        }
        iMRelService.saveAll(updateList);
        iMRelService.removeAll(removeList);
    }

    /**
     * irelの整理(重複を削除してあげる)
     */
//    private void orderM2() {
////        ・全部取得ー＞itemでまとめる→teamでまとめる
//        List<IMRel> iRelList = iMRelService.findAll();
//        List<IMRel> opeList = iMRelService.findAll();
//        List<IMRel> removeList = new ArrayList<>();
//        for (IMRel rel : iRelList) {
//            for (IMRel ope : opeList) {
//                // 一致するレコードがあったら
//                if (rel.getIm_rel_id() != ope.getIm_rel_id() && rel.getItem_m_id() == ope.getItem_m_id() && rel.getTeam_id() == ope.getTeam_id() && (rel.getMember_id() == ope.getMember_id() || (rel.getMember_id() == null && ope.getMember_id() == null))) {
//                    if (rel.getIm_rel_id() > ope.getIm_rel_id()) {
//                        if (!removeList.contains(rel)) {
//                            removeList.add(rel);
//                        }
//                    } else {
//                        if (!removeList.contains(ope)) {
//                            removeList.add(ope);
//                        }
//                    }
//                }
//            }
//        }
//        iMRelService.removeAll(removeList);
//    }

    /**
     * irelの整理(memberを入れてあげる)
     */
//    private void orderM3() {
////        ・全部取得ー＞itemでまとめる→teamでまとめる
//        List<IMRel> iRelList = iMRelService.findAll();
//        List<IMRelMem> relMenList = new ArrayList<>();
//        for (IMRel rel : iRelList) {
//            if (rel.getMember_id() != null) {
//                IMRelMem relMen = new IMRelMem(null, rel.getIm_rel_id(), null, null);
//                relMenList.add(relMen);
//            }
//        }
//        imRelMemService.saveAll(relMenList);
//    }

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
     * wpidの入っていないIMをポストする
     *
     * @throws InterruptedException
     */
    private void insertIM() throws InterruptedException {
        // 対象IM（wpIdがnull）を取得
        List<ItemMaster> imList = itemMasterService.findAllNotPosted();
        Map<List<ItemMaster>, List<ItemMaster>> result = blogController.postOrUpdate(imList);
        System.out.println(result.size());
    }

    /**
     * バッチで動かしてる定時楽天検索→Pythonにツイート命令を出すまでのメソッド
     * ①楽天検索
     * ②Yahoo検索
     *
     * @param teamId
     * @param artist
     * @return
     * @throws JSONException
     */
    public String searchItem(Long teamId, String artist, Long memberId) throws JSONException, ParseException, InterruptedException {
        boolean isTeam = memberId == 0L;
        List<String> list = controller.affiliSearchWord(artist);

        // 楽天検索(item_codeを先に取得して、新しいデータだけ詳細を取得してくる)
        List<String> itemCodeList = rakutenController.search(list);

        itemCodeList = itemService.findNewItemList(itemCodeList);

        List<Item> newItemList = new ArrayList<>();
        if (itemCodeList.size() > 0) {
            newItemList = rakutenController.getDetailsByItemCodeList(itemCodeList);
        }

        // Yahoo検索結果を追加(item_codeだけの取得ができないため、がっぽり取得したデータからitem_codeがDBにあるか見て、登録がない場合は詳細をjsonから吸い上げてリストに入れる)
        newItemList.addAll(yahooController.search(list));

        // 検索の誤引っ掛かりした商品をストアするリスト
        List<Item> removeList = new ArrayList<>();

        if (newItemList.size() > 0) {
            for (Item item : newItemList) {
                item.setPublication_date(analyzeController.generatePublicationDate(item));
                Item savedItem = itemService.findByItemCode(item.getItem_code()).orElse(null);

                // チームで削除チェック（チーム）合致orメンバーで削除チェック（メンバー）合致なら削除リストに追加
                if ((isTeam && addToRemoveList(item))) {
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
            savedItemList = itemService.saveAll(newItemList);

            if (savedItemList.size() > 0) {
                List<IRel> iRelList = new ArrayList<>();
                for (Item item : savedItemList) {
                    if (memberId!= null && memberId.equals(0L)) {
                        memberId = null;
                    }
                    iRelList.add(new IRel(null, item.getItem_id(), teamId, null, null));
                }

                // すでに登録されてるrelレコードがあったら重複嫌なので抜く
                iRelList = iRelService.removeExistRecord(iRelList);
                if (iRelList.size() > 0) {
                    List<IRel> savedList = iRelService.saveAll(iRelList);
                    if (memberId != null && memberId != 0L) {
                        List<IRelMem> memList = new ArrayList<>();
                        for (IRel rel : savedList) {
                            IRelMem relMem = new IRelMem(null, rel.getI_rel_id(), memberId, null, null);
                            memList.add(relMem);
                        }
                        iRelMemService.saveAll(memList);
                    }
                }
            }
        }

        // itemMasterに接続（追加/新規登録）し、itemのitem_m_idも更新する
        Map<ItemMaster, List<Item>> itemMasterListMap = itemUtils.groupItem(savedItemList);
        // itemMasterRelも更新する
        for (Map.Entry<ItemMaster, List<Item>> e : itemMasterListMap.entrySet()) {
            List<IMRel> IMRelList = iMRelService.findByItemMId(e.getKey().getItem_m_id());
            List<ItemRelElems> itemMasterRelElemsList = new ArrayList<>();
            for (IMRel rel :IMRelList) {
                List<IMRelMem> imRelMemList = imRelMemService.findByImRelId(rel.getIm_rel_id());
                if (imRelMemList.size() > 0) {
                    for (IMRelMem f : imRelMemList) {
                        ItemRelElems elem = new ItemRelElems(null, rel.getItem_m_id(), rel.getTeam_id(), f.getMember_id(), rel.getWp_id());
                        itemMasterRelElemsList.add(elem);
                    }
                } else {
                    ItemRelElems elem = new ItemRelElems(null, rel.getItem_m_id(), rel.getTeam_id(), null, rel.getWp_id());
                    itemMasterRelElemsList.add(elem);
                }
            }

            List<IRel> iRelList = iRelService.findByItemIdList(e.getValue().stream().map(Item::getItem_id).collect(Collectors.toList()));
            List<ItemRelElems> itemRelElemsList = new ArrayList<>();
            final Long tmp = memberId;
            if (memberId != null && memberId != 0L) {
                iRelList.forEach(f -> itemRelElemsList.add(new ItemRelElems(f.getItem_id(), null, f.getTeam_id(), tmp, null)));
            } else {
                iRelList.forEach(f -> itemRelElemsList.add(new ItemRelElems(f.getItem_id(), null, f.getTeam_id(), null, null)));
            }
            List<ItemRelElems> itemRelElemsDataList = itemRelElemsList.stream().distinct().collect(Collectors.toList());
            if (itemRelElemsDataList.size() > 0 && itemMasterRelElemsList.size() > 0 && itemRelElemsDataList.size() > itemMasterRelElemsList.size()) {
                List<ItemRelElems> sameElemsList = new ArrayList<>();

                for (ItemRelElems item : itemRelElemsDataList) {
                    for (ItemRelElems itemMaster : itemMasterRelElemsList) {
                        if (item.getTeam_id().equals(itemMaster.getTeam_id()) && item.getMember_id().equals(itemMaster.getMember_id())) {
                            sameElemsList.add(item);
                            break;
                        }
                    }
                }

                if (sameElemsList.size() > 0) {
                    itemRelElemsDataList.removeAll(sameElemsList);
                }
                // TODO: 復活すること
                if (itemRelElemsDataList.size() > 0) {
                    List<IMRel> toSaveIMRelList = new ArrayList<>();
                    for (ItemRelElems f : itemRelElemsDataList) {
                        IMRel rel = new IMRel(null, e.getKey().getItem_m_id(), f.getTeam_id(), f.getWp_id(),  null, null);
                        if (f.getMember_id() != null) {
                            IMRel savedRel = iMRelService.save(rel);
                            IMRelMem relMem = new IMRelMem(null, savedRel.getIm_rel_id(), f.getMember_id(), null, null);
                            imRelMemService.save(relMem);
                        } else {
                            toSaveIMRelList.add(rel);
                        }
                    }
                    if (toSaveIMRelList.size() > 0) {
                        iMRelService.saveAll(toSaveIMRelList);
                    }
                }
            }
        }

        // ブログ投稿（新規/更新）を行う
        // Map<新規登録ItemMaster/update ItemMaster>
        Map<List<ItemMaster>, List<ItemMaster>> itemMasterMap = blogController.postOrUpdate(new ArrayList<>(itemMasterListMap.keySet()));

        List<ItemMaster> newItemMasterList = new ArrayList<>();
        List<ItemMaster> updatedItemMasterList = new ArrayList<>();
        for (Map.Entry<List<ItemMaster>, List<ItemMaster>> e : itemMasterMap.entrySet()) {
            newItemMasterList = e.getKey();
            updatedItemMasterList = e.getValue();
        }

        // 新規登録したitemMasterがある場合
        if (newItemMasterList.size() > 0) {
            System.out.println("🕊保存したItemMaster ");
            for (ItemMaster itemMaster: newItemMasterList) {

                // 楽天リンクなどで必要なためリストの一番目のitemを取得
                Item item = itemMasterListMap.get(itemMaster).get(0);

                if (itemMaster.getPublication_date() != null && itemMaster.getPublication_date().after(Date.from(LocalDateTime.now().atZone(ZoneId.of("Asia/Tokyo")).toInstant()))) {
                    System.out.println(itemMaster.getTitle());
                    List<Long> teamIdList = iMRelService.findTeamIdListByItemMId(itemMaster.getItem_m_id());
                    if (teamIdList.size() > 0) {
                        Map<Long, String> twIdMap = teamService.getTeamIdTwIdMapByTeamIdList(teamIdList);
                        for (Map.Entry<Long, String> e : twIdMap.entrySet()) {
                            TwiDto twiDto = new TwiDto(item.getTitle(), item.getUrl(), itemMaster.getPublication_date(), null, e.getKey());
                            String result;

                            // TODO: text作成、memberを抜いてる
//                            List<Long> memberIdList = IMRelService.findMemberIdListByItemMId(itemMaster.getItem_m_id());
//                            if (memberIdList != null && !memberIdList.isEmpty()) {
//                                if (memberIdList.size() == 1) {
//                                    String memberName = memberService.getMemberName(memberIdList.get(0));
//                                    result = textController.twitterPerson(twiDto, memberName);
//                                } else {
//                                    List<String> memberNameList = memberService.getMemberNameList(memberIdList);
//                                    result = textController.twitterPerson(twiDto, memberNameList.get(memberNameList.size() -1));
//                                }
//                            } else {
                                result = twTextController.twitter(twiDto);
//                            }
                            // Twitter投稿
                            pythonController.post(Math.toIntExact(e.getKey()), result);
                        }
                    }
                } else {
                    System.out.println("❌🕊未来商品ではない");
                    System.out.println(item.getTitle());
                }
            }
        }
        return "Ok";
    }

    /**
     * 商品が登録不要なものかどうかチェックする
     * true -> 不要
     * false -> いいデータ
     *
     * @param item
     * @return
     */
    private boolean addToRemoveList(Item item) {
        // ①不適切な出版社・雑誌は削除
        for (PublisherEnum e : PublisherEnum.values()) {
            if (e.getNote() != null && e.getNote().equals(0)) {
                if (StringUtilsMine.arg2ContainsArg1(e.getName(), item.getTitle())) {
                    return true;
                }
            }
        }
        return false;
    }
}


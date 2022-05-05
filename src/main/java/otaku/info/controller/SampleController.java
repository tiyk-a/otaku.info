package otaku.info.controller;

import java.awt.*;
import java.io.*;
import java.security.GeneralSecurityException;
import java.text.ParseException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import com.google.api.services.calendar.model.Event;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import otaku.info.batch.scheduler.Scheduler;
import otaku.info.entity.*;
import otaku.info.enums.PublisherEnum;
import otaku.info.enums.StationEnum;
import otaku.info.enums.TeamEnum;
import otaku.info.service.*;
import otaku.info.setting.Log4jUtils;
import otaku.info.utils.StringUtilsMine;


/**
 * 楽天での商品検索指示〜Twitterポスト指示まで。
 *
 */
@RestController
@RequestMapping("/")
public class SampleController {

    final Logger logger = Log4jUtils.newConsoleCsvAllLogger("SampleController");

    @Autowired
    private RakutenController rakutenController;

    @Autowired
    private YahooController yahooController;

    @Autowired
    private TwTextController twTextController;

    @Autowired
    private AnalyzeController analyzeController;

    @Autowired
    private BlogController blogController;

    @Autowired
    private TmpController tmpController;

    @Autowired
    PythonController pythonController;

    @Autowired
    CalendarApiController calendarApiController;

    @Autowired
    YouTubeApiController youTubeApiController;

    @Autowired
    private ItemService itemService;

    @Autowired
    private TeamService teamService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private StationService stationService;

    @Autowired
    private ProgramService programService;

    @Autowired
    private IMService imService;

    @Autowired
    private IRelService iRelService;

    @Autowired
    private IRelMemService iRelMemService;

    @Autowired
    private IMRelService iMRelService;

    @Autowired
    private IMRelMemService imRelMemService;

    @Autowired
    private PRelService pRelService;

    @Autowired
    private PRelMemService pRelMemService;

    @Autowired
    Scheduler scheduler;

    /**
     * URLでアクセスできるtmpのメソッドです。
     * 任意に中身を変えます、テスト用。
     * Currently: アイキャッチ画像の画像パスをDBに取り込む→楽天に飛ばさず画像を表示できるようになる
     *
     * @return
     */
    @GetMapping("/tmpMethod/{teamId}/{eId}")
    public String tempMethod(@PathVariable Long teamId, @PathVariable String eId) throws IOException, GeneralSecurityException {

        logger.debug("samplecontroller.tmpMethod() START");
        Event e = calendarApiController.updateEvent(TeamEnum.get(teamId).getCalendarId(), eId, new Date(), new Date(), "test" + Math.random(), "test description", true);
        logger.debug("samplecontroller.tmpMethod() END");
        return "Status: " + e.getStatus() + " id: " + e.getId();
    }

    /**
     * ブラウザとかでテスト投稿（1件）がいつでもできるメソッド
     *
     * @return
     */
    @GetMapping("/test")
    public String sample1() {
//        imageController.createImage("test1.png", "日本語のテスト", "楽しみだね！🐶");
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        Font[] allFonts = ge.getAllFonts();

        for (Font font : allFonts) {

            System.out.println(font.getFontName(Locale.JAPAN));
        }
//        youTubeApiController.main();
        return "ok";
    }

    @GetMapping("/batch/{id}")
    public String batch(@PathVariable String id) throws InterruptedException, JSONException {
        int i = Integer.parseInt(id);
        switch (i) {
            case 1:
                scheduler.run1();
                break;
            case 2:
                scheduler.run2();
                break;
            case 3:
                blogController.chkWpId();
                blogController.chkWpIdByBlog();
                break;
            case 4:
                scheduler.run4();
                break;
            case 5:
                scheduler.run5();
                break;
            case 6:
                scheduler.run6();
                break;
            case 7:
                scheduler.run7();
                break;
            case 8:
                scheduler.run8();
                break;
            case 9:
                scheduler.run9();
                break;
            case 10:
                scheduler.run10();
                break;
            case 11:
                scheduler.run11();
                break;
            case 12:
                scheduler.run12();
                break;
            case 13:
                scheduler.run13();
                break;
            case 14:
                // 商品の情報を投稿する
                logger.debug("---Tmpブログ新商品投稿メソッドSTART---");
//                List<Item> itemList = itemService.findNotDeleted();
//                blogController.tmpItemPost(itemList);
                logger.debug("---Tmpブログ新商品投稿メソッドEND---");
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
            case 18:
//                managePRel();
                break;
            case 19:
//                insertPrice();
                break;
            case 20:
//                organizeIm();
                break;
            case 21:
//                insertImRel();
                break;
            case 22:
                TeamEnum[] list = TeamEnum.values().clone();
                Map<String, TeamEnum> map = new HashMap<>();
                for (TeamEnum l : list) {
                    if (!map.containsKey(l.getSubDomain())) {
                        map.put(l.getSubDomain(), l);
                    }
                }
                for (TeamEnum s : map.values()) {
                    blogController.insertTags(s);
                }
                break;
            case 23:
                modifyStation();
                logger.debug("残ったstationをdelします");
                deleteUnusedStation();
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
                List<IMRel> groupList = iMRelService.findByItemIdTeamIdNotNull(rel.getIm_id());
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
     * Itemに不適切な商品が入ってしまっていたらItemId指定でdel_flgをonにします。
     * パラメータの指定：00-001-22（数字をハイフンで区切ることで複数商品を1リクエストで処理）
     *
     * @return String
     */
//    @GetMapping("/moveToDelItem/{itemIdListStr}")
//    public String moveToDelItem(@PathVariable String itemIdListStr) {
//        List<Long> itemIdList = new ArrayList<>();
//        List.of(itemIdListStr.split("-")).forEach(e -> itemIdList.add(Long.valueOf(e)));
//
//        if (itemIdList.size() == 0) return "No Id provided";
//
//        List<Long> notFoundIdList = new ArrayList<>();
//        int successCount = 0;
//        String result = "";
//        for (Long itemId : itemIdList) {
//            Item item = itemService.findByItemId(itemId).orElse(new Item());
//            if (item.getItem_id() == null) {
//                notFoundIdList.add(itemId);
//            } else {
//                successCount ++;
//                item.setDel_flg(true);
//                itemService.saveItem(item);
//                result = result + "【" + successCount + "】itemId:" + item.getItem_id() + "Title: " + item.getTitle() + "-------------------";
//            }
//
//            if (notFoundIdList.size() > 0) {
//                result = result + "Not found item";
//                for (Long id : notFoundIdList) {
//                    result = result + " item_id=" + id;
//                }
//            }
//        }
//        return result;
//    }

    /**
     * バッチで動かしてる定時楽天検索→Pythonにツイート命令を出すまでのメソッド
     * ①楽天検索
     * ②Yahoo検索
     *
     * @param teamId
     * @param name // teamNameかmemberNameが入る
     * @return
     * @throws JSONException
     */
    public String searchItem(Long teamId, String name, Long memberId, Long siteId) throws ParseException, InterruptedException {
        boolean isTeam = memberId == 0L;

        List<String> searchList = new ArrayList<String>(Arrays.asList("雑誌", "CD", "DVD"));
        List<String> resultList = new ArrayList<>();
        // アフィリサイトでの検索ワード一覧
        searchList.forEach(arr -> resultList.add(String.join(" ", name, arr)));

        List<Item> newItemList = new ArrayList<>();
        // 検索の誤引っ掛かりした商品をストアするリスト
        List<Item> removeList = new ArrayList<>();

        // siteIdで処理切り替え
        if (siteId == 1) {
            // ■■■■■　①楽天検索(item_codeを先に取得して、新しいデータだけ詳細を取得してくる)
            List<String> itemCodeList = rakutenController.search(resultList, teamId);

            itemCodeList = itemService.findNewItemList(itemCodeList);

            if (itemCodeList.size() > 0) {
                newItemList = rakutenController.getDetailsByItemCodeList(itemCodeList, teamId);
            }
        } else if (siteId == 2) {
            // ■■■■■　Yahoo検索結果を追加(item_codeだけの取得ができないため、がっぽり取得したデータからitem_codeがDBにあるか見て、登録がない場合は詳細をjsonから吸い上げてリストに入れる)
            newItemList.addAll(yahooController.search(resultList, teamId));
        }

        logger.debug("新商品候補数：" + newItemList.size());
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

        logger.debug("削除新商品候補数：" + removeList.size());
        // 保存する商品リストから不要な商品リストを削除する
        newItemList.removeAll(removeList);
        logger.debug("削除商品除いた後の新商品候補数：" + newItemList.size());

        // 不要商品リストに入った商品を商品テーブルに格納する
        if (removeList.size() > 0) {
            logger.debug("違う商品を保存します: " + removeList.size() + "件");
            removeList.forEach(e -> logger.debug(e.getTitle()));
            removeList.forEach(e -> e.setDel_flg(true));
            // 不要商品はrelの登録などなし
            itemService.saveAll(removeList);
        }

        // 正常商品を登録する
        List<Item> savedItemList = new ArrayList<>();
        if (newItemList.size() > 0) {
            logger.debug("商品を保存します: " + newItemList.size() + "件");
            newItemList.forEach(e -> logger.debug(e.getTitle()));
            savedItemList = itemService.saveAll(newItemList);

            logger.debug("保存に成功した商品数: " + savedItemList.size() + "件");
            if (savedItemList.size() > 0) {
                List<IRel> iRelList = new ArrayList<>();
                for (Item item : savedItemList) {
                    if (memberId!= null && memberId.equals(0L)) {
                        memberId = null;
                    }
                    iRelList.add(new IRel(null, item.getItem_id(), teamId, null, null));
                }
                logger.debug("Relの登録に入ります。新規rel数:" + iRelList.size());

                // すでに登録されてるrelレコードがあったら重複嫌なので抜く
                iRelList = iRelService.removeExistRecord(iRelList);
                logger.debug("登録ずみrel削除後残り新規rel数:" + iRelList.size());
                if (iRelList.size() > 0) {
                    List<IRel> savedList = iRelService.saveAll(iRelList);
                    if (memberId != null && memberId != 0L) {
                        logger.debug("RelMem登録あり");
                        List<IRelMem> memList = new ArrayList<>();
                        for (IRel rel : savedList) {
                            logger.debug(rel.getI_rel_id() + "<-これnullにならないよね？");
                            IRelMem relMem = new IRelMem(null, rel.getI_rel_id(), memberId, null, null);
                            memList.add(relMem);
                        }
                        iRelMemService.saveAll(memList);
                    } else {
                        logger.debug("RelMem登録なし");
                    }
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

    /**
     * PRelに入ってるmemberをPRelMemに移行します
     * このメソッドが安全に完了したらprelのmemberidは削除可能
     *
     */
//    private void managePRel() {
//        // memberの入っているprelを全て取得
//        List<PRel> pRelList = pRelService.findAllMemNotNull();
//        for (PRel rel : pRelList) {
//            logger.debug("prelId:" + rel.getP_rel_id() + " memId:" + rel.getMember_id());
//            PRelMem relMem = new PRelMem(null, rel.getP_rel_id(), rel.getMember_id(), null, null);
//            try {
//                pRelMemService.save(relMem);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//    }
    /**
     * 商品の金額（多分これが正しい）を返す
     *
     * @param itemList
     * @return
     */
    private Integer getPrice(List<Item> itemList) {
        List<Integer> priceList = itemList.stream().map(Item::getPrice).distinct().collect(Collectors.toList());
        if (priceList.size() == 1) {
            return priceList.get(0);
        } else {
            return priceList.stream().max(Integer::compare).orElse(0);
        }
    }

    /**
     * 同じ名前で重複してしまっているstationレコードをどうにかします
     *
     */
    private void modifyStation() {
        List<Station> sList = stationService.findAll().stream().filter(e -> !e.getDel_flg()).collect(Collectors.toList());
        logger.debug("All list size:" + sList.size());
        int count = 1;
        for (Station s : sList) {
            logger.debug("station_id=" + s.getStation_id());
            logger.debug("■■■■■■■count:" + count);
            StationEnum sEnum = StationEnum.get(s.getStation_name());

            // Enumになってないstationの場合、処理に進む
            if (sEnum.equals(StationEnum.NHK)) {
                logger.debug("NHK");
                // 同じ名前のstationができていないか、名前でリストを取得する
                List<Station> dbList = stationService.findByName(s.getStation_name());
                logger.debug("dbList.size=" + dbList.size());
                // 同じ名前のstationが作られていたら、ID一番若いやつにprogramのstation_idを寄せていく
                if (dbList.size() > 1) {
                    Station fstS = dbList.get(0);
                    for (Station ele : dbList) {
                        if (!ele.equals(fstS)) {
                            List<Program> pList = programService.findbyStationId(ele.getStation_id());
                            if (pList.size() > 0) {
                                pList.forEach(e -> e.setStation_id(fstS.getStation_id()));
                                programService.saveAll(pList);
                                logger.debug("program list saved");
                            }
                            ele.setDel_flg(true);
                            stationService.save(ele);
                            logger.debug("station saved");
                        }
                    }
                }
            } else {
                List<Program> pList = programService.findbyStationId(s.getStation_id());
                pList.forEach(e -> e.setStation_id(sEnum.getId()));
                programService.saveAll(pList);
                s.setDel_flg(true);
                stationService.save(s);
            }
            ++ count;
        }
    }

    /**
     * programで一度も使用されていないstationはdelにセットします
     *
     */
    private void deleteUnusedStation() {
        List<Station> sList = stationService.findAll();
        for (Station s : sList) {
            List<Program> programList = programService.findbyStationId(s.getStation_id());
            if (programList.size() == 0) {
                s.setDel_flg(true);
                stationService.save(s);
                logger.debug("使われたないstationをdel登録します:" + s.getStation_id());
            }
        }
    }
}

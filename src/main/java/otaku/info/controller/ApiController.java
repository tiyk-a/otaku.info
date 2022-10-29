package otaku.info.controller;

import java.text.ParseException;
import java.util.*;

import javax.validation.Valid;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.AllArgsConstructor;
import otaku.info.batch.scheduler.Scheduler;
import otaku.info.dto.*;
import otaku.info.entity.*;
import otaku.info.form.*;
import otaku.info.service.*;
import otaku.info.setting.Log4jUtils;
import otaku.info.utils.DateUtils;
import otaku.info.utils.ServerUtils;
import otaku.info.utils.StringUtilsMine;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class ApiController {

    final Logger logger = Log4jUtils.newConsoleCsvAllLogger("ApiController");

    @Autowired
    BlogController blogController;

    @Autowired
    CalendarApiController calendarApiController;

    @Autowired
    RakutenController rakutenController;

    @Autowired
    ItemService itemService;

    @Autowired
    IMService imService;

    @Autowired
    ImVerService imVerService;

    @Autowired
    BlogUpdService blogUpdService;

    @Autowired
    ErrorJsonService errorJsonService;

    @Autowired
    GCalendarService gCalendarService;

    @Autowired
    DelCalService delCalService;

    @Autowired
    ProgramService programService;

    @Autowired
    DateUtils dateUtils;

    @Autowired
    ServerUtils serverUtils;

    @Autowired
    TextController textController;

    @Autowired
    StringUtilsMine stringUtilsMine;

    @Autowired
    Scheduler scheduler;

    /**
     * 各グループ画面のデータ取得
     *
     * @param teamIdStr
     * @return
     */
    @GetMapping("/{teamIdStr}")
    public ResponseEntity<FAllDto> getTop(@PathVariable String teamIdStr) {
        logger.debug("accepted");
        FAllDto dto = new FAllDto();

        Long teamId = 0L;
        if (StringUtilsMine.isNumeric(teamIdStr)) {
            teamId = Long.parseLong(teamIdStr);
        }

        // teamIdが不正値だったらチームごとの件数だけ取得して返す
        boolean skipItemFlg = false;
        if (teamId < 6 || teamId > 21) {
            skipItemFlg = true;
        }

        if (!skipItemFlg) {
            // IMがない未来のItemを取得する
            List<Item> itemList = itemService.findByTeamIdFutureNotDeletedNoIM(teamId);

            // 未来有効でWPIDがnullのIMを取得する
            List<IM> imList = imService.findByTeamIdFutureOrWpIdNull(teamId);
            List<ErrorJson> errorJsonList = errorJsonService.findByTeamIdNotSolved(teamId);

            // IMリスト
            List<FIMDto> fimDtoList = new ArrayList<>();
            for (IM im : imList) {
                FIMDto imDto = new FIMDto();
                imDto.setIm(im);

                // verも追加
                List<ImVer> verList = imVerService.findByImId(im.getIm_id());
                imDto.setVerList(verList);
                fimDtoList.add(imDto);
            }

            dto.setI(itemList);
            dto.setIm(fimDtoList);
            dto.setErrJ(errorJsonList);
        }

        // 各チームのIMなし未来のitem件数を取得しDTOにセットします<TeamId, numberOfItems>
        Map<Long, Integer> numberMap = itemService.getNumbersOfEachTeamIdFutureNotDeletedNoIM();
        dto.setItemNumberMap(numberMap);

        // TVの未チェック件数を設定する
        dto.setTvCount(programService.findByPmId(null));
        logger.debug("fin");
        return ResponseEntity.ok(dto);
    }

    /**
     * まとめて一括IMの登録を行う
     *
     * @param forms
     * @return
     */
    @PostMapping("/im/bundle/new")
    public ResponseEntity<Boolean> newBundleIMyVer(@Valid @RequestBody IMVerForm[] forms) {
        for (IMVerForm imVerForm : forms) {
            ResponseEntity<Boolean> responseEntity = newIMyVer(imVerForm);
            if (!responseEntity.getStatusCode().equals(HttpStatus.OK)) {
                return ResponseEntity.status(500).body(false);
            }
        }
        return ResponseEntity.ok(true);
    }

    /**
     * まとめて一括IMの設定を行う
     *
     * @param forms
     * @return
     */
    @PostMapping("/im/bundle/chk")
    public ResponseEntity<Boolean> newBundleChk(@Valid @RequestBody IMVerForm[] forms) {
        for (IMVerForm imVerForm : forms) {
            ResponseEntity<Boolean> responseEntity = chkItem(imVerForm.getItem_id(), imVerForm.getIm_id());
            if (!responseEntity.getStatusCode().equals(HttpStatus.OK)) {
                return ResponseEntity.status(500).body(false);
            }
        }
        return ResponseEntity.ok(true);
    }

    /**
     * IDから商品を取得し返す
     *
     * @param id 取得する商品のID
     * @return Item
     */
    @GetMapping("/im/{id}")
    public ResponseEntity<FIMDto> getIm(@PathVariable Long teamId, @PathVariable Long id) {
        logger.debug("accepted");
        FIMDto dto = new FIMDto();
        IM im = imService.findById(id);
        List<ImVer> imVerList = imVerService.findByImId(im.getIm_id());

        dto.setIm(im);
        dto.setVerList(imVerList);
        logger.debug("fin");
        return ResponseEntity.ok(dto);
    }

    /**
     * 指定Teamidの商品を未来発売日順に取得し返す、削除されていない商品のみ。
     *
     * @param id 取得するTeamId
     * @return Item
     */
    @GetMapping("/im/team/{id}")
    public ResponseEntity<List<FIMDto>> getTeam(@PathVariable Long id) {
        logger.debug("getTeam teamId=" + id);
        List<IM> imList = imService.findByTeamIdNotDeleted(id);
        List<FIMDto> dtoList = new ArrayList<>();

        for (IM im : imList) {
            FIMDto dto = new FIMDto();
            dto.setIm(im);

            List<ImVer> imVerList = imVerService.findByImId(im.getIm_id());
            dto.setVerList(imVerList);
            dtoList.add(dto);
        }
        logger.debug("fin");
        return ResponseEntity.ok(dtoList);
    }

    /**
     * 商品のデータを更新する
     * TODO: もっと更新すべきデータあるのでは？
     * IMの更新なので、
     *
     * @param id データ更新をする商品のID
     * @param imForm 更新される新しいデータ
     * @return Item
     */
    @PostMapping("/im/{id}")
    public ResponseEntity<FIMDto> upIm(@PathVariable Long id, @Valid @RequestBody IMForm imForm) throws ParseException {
        logger.debug("accepted");
        IM im = imService.findById(id);

        if (imForm.getTitle() != null && !imForm.getTitle().isEmpty()) {
            im.setTitle(imForm.getTitle());
        }

        if (imForm.getPublication_date() != null) {
            im.setPublication_date(dateUtils.stringToDate(imForm.getPublication_date(), "yyyy/MM/dd"));
        }

        im.setBlogNotUpdated(true);
        IM imUpdated = imService.save(im);

        FIMDto dto = new FIMDto();
        dto.setIm(imUpdated);

        List<ImVer> imVerList = imVerService.findByImId(imUpdated.getIm_id());
        dto.setVerList(imVerList);

        logger.debug("fin");
        return ResponseEntity.ok(dto);
    }

    /**
     * IDから商品を削除する
     *
     * @param id 削除される商品のID
     */
    @DeleteMapping("/im/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Boolean> delIm(@PathVariable Long id) {
        logger.debug("accepted");
        try {
            IM im = imService.findById(id);
            im.setDel_flg(true);
            imService.save(im);
        } catch (Exception e) {
            logger.error("APIエラー");
            e.printStackTrace();
            return ResponseEntity.ok(false);
        }
        logger.debug("fin");
        return ResponseEntity.ok(true);
    }

    /**
     * 指定のIMをブログ投稿します
     *
     */
    @GetMapping("/im/blog")
    public ResponseEntity<Boolean> upImBlog(@RequestParam("imId") Long imId) throws InterruptedException {
        logger.debug("accepted");
        IM im = imService.findById(imId);
        if (im != null) {
            BlogUpd blogUpd = new BlogUpd();
            blogUpd.setIm_id(im.getIm_id());
            blogUpdService.save(blogUpd);
        }
        logger.debug("fin");
        return ResponseEntity.ok(true);
    }

    /**
     * IDから商品を削除する
     *
     * @param id 削除される商品のID
     */
    @DeleteMapping("/item/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Boolean> delItem(@PathVariable Long id) {
        logger.debug("accepted");
        try {
            Item item = itemService.findByItemId(id).orElse(null);
            if (item != null) {
                item.setDel_flg(true);
                itemService.save(item);
            }
        } catch (Exception e) {
            logger.error("APIエラー");
            e.printStackTrace();
            return ResponseEntity.ok(false);
        }
        logger.debug("fin");
        return ResponseEntity.ok(true);
    }

    /**
     * IM+verを登録します。すでにIMがある場合は更新
     * ブログポストは行わない
     *
     * @return Boolean true: success / false: failed
     */
    @PostMapping("/im")
    public ResponseEntity<Boolean> newIMyVer(@Valid @RequestBody IMVerForm imVerForm) {
        logger.debug("accepted");

        try {
            IM im = null;
            Item item = itemService.findByItemId(imVerForm.getItem_id()).orElse(null);

            if (item == null) {
                return ResponseEntity.ok(false);
            }

            // im_idが入っていたらverだけ追加処理処理、入っていなかったらim新規登録とあればver追加処理、と判断（ここではimのタイトル変更などはできない）
            // まずはim
            // 新規登録
            if (imVerForm.getIm_id() == null || imVerForm.getIm_id() == 0) {

                // 本当に重複登録がないかチェック
                // 同じタイトルのimがあるなら、新規登録せずに0番目のimをセットする
                List<IM> checkedImList = imService.findByTitle(imVerForm.getTitle());
                if (checkedImList.size() != 0) {
                    im = checkedImList.get(0);
                } else {
                    // 対象のItemが見つからなかったら新規作成。
                    im = new IM();
                }
            } else {
                // 更新
                im = imService.findById(imVerForm.getIm_id());
            }

            if ((im.getAmazon_image() == null || im.getAmazon_image().equals(""))
                    && (imVerForm.getAmazon_image() != null && !imVerForm.getAmazon_image().equals(""))) {
                im.setAmazon_image(imVerForm.getAmazon_image());
            }

            // team
            if (imVerForm.getTeamArr() != null && !imVerForm.getTeamArr().equals("")) {
                String teamArr = im.getTeamArr();
                for (Long teamId : StringUtilsMine.stringToLongList(imVerForm.getTeamArr())) {
                    teamArr = StringUtilsMine.addToStringArr(teamArr, teamId);
                }
                im.setTeamArr(StringUtilsMine.removeBrackets(teamArr));
            }

            // mem
            if (imVerForm.getMemArr() != null && !imVerForm.getMemArr().equals("")) {
                String memArr = im.getMemArr();
                for (Long memId : StringUtilsMine.stringToLongList(imVerForm.getMemArr())) {
                    memArr = StringUtilsMine.addToStringArr(memArr, memId);
                }
                im.setMemArr(StringUtilsMine.removeBrackets(memArr));
            }

            // 日付をstringからDateにして詰める
            if (!imVerForm.getPublication_date().equals("")) {
                im.setPublication_date(dateUtils.stringToDate(imVerForm.getPublication_date(), "yyyy/MM/dd"));
            }

            if (im.getIm_id() != null && !im.getIm_id().equals(0L)) {
                im.setBlogNotUpdated(true);
            }

            // rakuten urlを入れる(yahoo由来で楽天URLがわからない場合は入れない。バッチで夜に入れてもらう)
            if (item.getSite_id().equals(1)) {
                im.setRakuten_url(item.getUrl());
            }

            // wordpressでエラーになる記号を処理し、設定し直す
            im.setTitle(textController.replaceSignals(imVerForm.getTitle()));

            IM savedIm = imService.save(im);

            // verがあれば登録します
            List<String[]> verArr = imVerForm.getVers();

            if (verArr.size() > 0) {
                for (String[] ver : verArr) {
                    String verName = ver[1];
                    if (verName != null) {
                        ImVer newVer = new ImVer();
                        newVer.setVer_name(textController.replaceSignals(verName));
                        newVer.setIm_id(savedIm.getIm_id());
                        newVer.setDel_flg(false);
                        imVerService.save(newVer);
                    }
                }
            }

            // googleカレンダー登録のためのデータを用意する
//            CalendarInsertDto calendarDto = setGCalDate(savedIm);
//            List<GCalendar> calendarList = new ArrayList<>();
//            for (Long teamId : StringUtilsMine.stringToLongList(im.getTeamArr())) {
//                // TODO: calendarDTO作成ない、メンバーちゃんと詰めてる？
//                Event event = calendarApiController.postEvent(TeamEnum.get(teamId).getCalendarId(), calendarDto.getStartDate(), calendarDto.getEndDate(), calendarDto.getTitle(), calendarDto.getDesc(), calendarDto.getAllDayFlg() );
//                GCalendar gCalendar = new GCalendar();
//                gCalendar.setBlog_post_id();
//                gCalendar.setCategory_id(1L);
//                gCalendar.setEvent_id(event.getId());
//                gCalendar.setTeam_id(teamId);
//                gCalendar.setMember_arr(im.getMemArr());
//                calendarList.add(gCalendar);
//            }
//
//            if (calendarList.size() > 0) {
//                gCalendarService.saveAll(calendarList);
//            }

            // itemのim_idを登録します
            item.setIm_id(savedIm.getIm_id());
            item.setFct_chk(true);
            itemService.save(item);

            logger.debug("fin");
            return ResponseEntity.ok(true);
        } catch (Exception e) {
            logger.error("APIエラー");
            e.printStackTrace();
            return ResponseEntity.ok(false);
        }
    }
    
    /**
     * GoogleカレンダーinsertのためのデータをIMから格納する
     * itemなのでall-dayイベントを想定
     *
     * @param im
     * @return
     */
    public CalendarInsertDto setGCalDate(IM im) {
        CalendarInsertDto dto = new CalendarInsertDto();

        dto.setTitle(im.getTitle());

        Date startDate = im.getPublication_date();
        Date endDate = new Date(startDate.getTime() + 86400000);

        dto.setStartDate(startDate);
        dto.setEndDate(endDate);
        String url = stringUtilsMine.getAmazonLinkFromCard(im.getAmazon_image()).orElse(null);

        if (url == null) {
            List<Item> itemList = itemService.findByMasterId(im.getIm_id());
            for (Item i : itemList) {
                if (i.getUrl() != null) {
                    url = i.getUrl();
                    break;
                }
            }
        }

        if (url == null) {
            url = "";
        }

        dto.setDesc(url);

        dto.setAllDayFlg(true);
        return dto;
    }

    /**
     * GoogleカレンダーinsertのためのデータをIMから格納する
     * itemなのでall-dayイベントを想定
     *
     * @param pm
     * @return
     */
//    public CalendarInsertDto setGCalDatePm(PM pm, PMVer ver) {
//        CalendarInsertDto dto = new CalendarInsertDto();
//
//        dto.setTitle(pm.getTitle());
//
//        LocalDateTime startDate = ver.getOn_air_date();
//        LocalDateTime endDate = ver.getOn_air_date();
//
//        dto.setStartDateTime(startDate);
//        dto.setEndDateTime(endDate);
////        String url = stringUtilsMine.getAmazonLinkFromCard(pm.getAmazon_pmage()).orElse(null);
//
////        if (url == null) {
////            List<Item> itemList = itemService.findByMasterId(pm.getIm_id());
////            for (Item i : itemList) {
////                if (i.getUrl() != null) {
////                    url = i.getUrl();
////                    break;
////                }
////            }
////        }
////
////        if (url == null) {
////            url = "";
////        }
//
//        dto.setDesc(pm.getDescription());
//
//        dto.setAllDayFlg(false);
//        return dto;
//    }
    
    /**
     * IM+verを更新します
     * ブログ更新はなし
     *
     * @return Boolean true: success / false: failed
     */
    @PostMapping("/im/upd")
    public ResponseEntity<Boolean> updIMyVer(@Valid @RequestBody IMVerForm imVerForm) {
        logger.debug("accepted");

        try {
            // IMの更新の場合、更新する（verのみの更新もありえるから）
            IM im = imService.findById(imVerForm.getIm_id());
            Boolean updatedFlg = false;
            if (im == null) {
                return ResponseEntity.ok(false);
            }

            // imの更新
            if (!imVerForm.getTitle().equals(im.getTitle())) {
                logger.info("IMのtitle変更");
                im.setTitle(imVerForm.getTitle());
                updatedFlg = true;
            } else {
                logger.info("IMのtitle変更ではありません");
            }

            if (!imVerForm.getAmazon_image().equals(im.getAmazon_image())) {
                logger.info("IMのamazon_image変更");
                im.setAmazon_image(imVerForm.getAmazon_image());
                updatedFlg = true;
            } else {
                logger.info("IMのamazon_image変更ではありません");
            }
            logger.info("amazon_image:" + imVerForm.getAmazon_image());

            if (!StringUtilsMine.sameElementArrays(imVerForm.getTeamArr(), im.getTeamArr())) {
                logger.info("IMのTeamArr変更");
                im.setTeamArr(StringUtilsMine.removeBrackets(imVerForm.getTeamArr()));
                updatedFlg = true;
            } else {
                logger.info("IMのTeamArr変更ではありません");
            }

            if (!StringUtilsMine.sameElementArrays(imVerForm.getMemArr(), im.getMemArr())) {
                logger.info("IMのMemArr変更");
                im.setMemArr(StringUtilsMine.removeBrackets(imVerForm.getMemArr()));
                updatedFlg = true;
            } else {
                logger.info("IMのMemArr変更ではありません");
            }

            // IMの要素が変わってるよフラグがtrueであれば更新してあげます
            if (updatedFlg) {
                im.setBlogNotUpdated(true);
                imService.save(im);
            }

            // verの更新[[id,name][id,name][id,name][id,name][id,name][id,name][id,name]]
            // JsonObjectのverを成形し、DBの値と一致してるか確認する
            // formに入ってきたverオブジェクト
            List<String[]> verArr = imVerForm.getVers();
            // DBに保存されてるverたち
            List<ImVer> verList = imVerService.findByImId(im.getIm_id());

            for (String[] ver : verArr) {
                Boolean existsFlg = true;
                Long verId;
                try {
                    verId = Long.parseLong(ver[0]);
                } catch (Exception e) {
                    // この時点でverIdが取得できなかったら新規のVerってことなので一気にver新規登録に飛びます
                    verId = null;
                    existsFlg = false;
                }

                String verName = ver[1];

                // フォームのImverを1つずつDBのImVerと比較し、更新が必要であれば更新する
                if (existsFlg) {
                    for (ImVer imVer : verList) {

                        // verIdは一致するimVerを見つけた
                        if (verId.equals(imVer.getIm_v_id())) {
                            existsFlg = true;

                            // verNameが一致しない場合、フォームから来たverNameで上書きし保存
                            if (!verName.equals(imVer.getVer_name())) {

                                // verNameが空の場合、論理抹消する。空じゃない場合は名前を更新
                                if (verName.equals("")) {
                                    imVer.setDel_flg(true);
                                } else {
                                    imVer.setVer_name(verName);
                                }
                                imVerService.save(imVer);
                            }
                        } else {
                            existsFlg = false;
                        }

                        // DBのImVerを見つけたらこのforループからは抜けていい
                        if (existsFlg) {
                            break;
                        }
                    }
                }

                // そもそもそのverがDBに存在していなかったら新規登録してあげる
                if (!existsFlg) {
                    ImVer newVer = new ImVer();
                    newVer.setVer_name(verName);
                    newVer.setIm_id(im.getIm_id());
                    newVer.setDel_flg(false);
                    imVerService.save(newVer);
                }
            }
            logger.debug("fin");
            return ResponseEntity.ok(true);
        } catch (Exception e) {
            logger.error("APIエラー");
            e.printStackTrace();
            return ResponseEntity.ok(false);
        }
    }

    /**
     * IM+verを更新します(一括更新)
     *
     * @return Boolean true: success / false: failed
     */
    @PostMapping("/im/bundle/upd")
    public ResponseEntity<Boolean> updBundleIMyVer(@Valid @RequestBody IMVerForm[] imVerForms) {
        logger.debug("accepted");

        for (IMVerForm imVerForm : imVerForms) {
            ResponseEntity<Boolean> responseEntity = updIMyVer(imVerForm);
            if (!responseEntity.getStatusCode().equals(HttpStatus.OK)) {
                return ResponseEntity.status(500).body(false);
            }
        }
        return ResponseEntity.ok(true);
    }

    /**
     * Item一括削除
     *
     * @return Boolean true: success / false: failed
     */
    @PostMapping("/item/bundle/del")
    public ResponseEntity<Boolean> deleteItems(@Valid @RequestBody Integer[] itemIdList) {
        logger.debug("accepted");

        for (Integer itemId : itemIdList) {
            delItem(itemId.longValue());
        }
        return ResponseEntity.ok(true);
    }

    /**
     * Itemにim_idを追加してfct_chkを更新します（既存imある場合ですね）
     *
     * @return Boolean true: success / false: failed
     */
    @GetMapping("/im/chk")
    public ResponseEntity<Boolean> chkItem(@RequestParam("itemId") Long itemId, @RequestParam("imId") Long imId) {
        logger.debug("accepted");

        try {
            Item item = itemService.findByItemId(itemId).orElse(null);
            IM im = imService.findById(imId);

            if (item == null || im == null) {
                return ResponseEntity.ok(false);
            }

            item.setIm_id(imId);
            item.setFct_chk(true);
            itemService.save(item);

            logger.debug("fin");
            return ResponseEntity.ok(true);
        } catch (Exception e) {
            logger.error("APIエラー");
            e.printStackTrace();
            return ResponseEntity.ok(false);
        }
    }

    /**
     * IMを検索する
     *
     * @param key
     * @return
     */
    @GetMapping("/im/search")
    public ResponseEntity<List<IM>> searchOtherTeamIM(@RequestParam("key") String key) {
        if (key.equals("") ) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(imService.findByKeyExcludeTeamId(key));
    }

    /**
     * IMのWPアイキャッチをAmazon画像で更新する
     * パラメタで更新したいIMを選択して。すでにAmazonImageがある場合に使える
     *
     * @param imId
     * @return
     */
    @GetMapping("/im/eye")
    public ResponseEntity<Boolean> setEyeCatch(@RequestParam("id") Long imId) {
        if (imId.equals("") ) {
            return ResponseEntity.notFound().build();
        }

        try {
            IM im = imService.findById(imId);
                blogController.tmpEyeCatchAmazonSet(im);
        } catch (Exception e) {
            return ResponseEntity.ok(false);
        }

        return ResponseEntity.ok(true);
    }

    /**
     * IMのWPアイキャッチをまとめてAmazon画像で更新する
     * パラメタで更新したいIMを選択して。すでにAmazonImageがある場合に使える
     *
     * @param imIdArr
     * @return
     */
//    @PostMapping("/im/eyeBundle")
//    public ResponseEntity<Boolean> setEyeCatchBundle(@Valid @RequestBody Long[] imIdArr) {
//
//        for (Long imId : imIdArr) {
//            setEyeCatch(imId);
//        }
//
//        return ResponseEntity.ok(true);
//    }
}

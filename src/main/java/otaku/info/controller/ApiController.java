package otaku.info.controller;

import java.math.BigInteger;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import javax.validation.Valid;

import com.google.api.services.calendar.model.Event;
import org.apache.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.AllArgsConstructor;
import otaku.info.dto.*;
import otaku.info.entity.*;
import otaku.info.enums.MemberEnum;
import otaku.info.enums.TeamEnum;
import otaku.info.error.MyMessageException;
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
    IRelMemService iRelMemService;

    @Autowired
    IMService imService;

    @Autowired
    ImVerService imVerService;

    @Autowired
    IMRelMemService imRelMemService;

    @Autowired
    ProgramService programService;

    @Autowired
    PMService pmService;

    @Autowired
    PmVerService pmVerService;

    @Autowired
    PRelService pRelService;

    @Autowired
    PMRelService pmRelService;

    @Autowired
    PRelMemService pRelMemService;

    @Autowired
    PMRelMemService pmRelMemService;

    @Autowired
    PMCalService pmCalService;

    @Autowired
    IRelService iRelService;

    @Autowired
    IMRelService imRelService;

    @Autowired
    PageTvService pageTvService;

    @Autowired
    ErrorJsonService errorJsonService;

    @Autowired
    TeamService teamService;

    @Autowired
    StationService stationService;

    @Autowired
    DelCalService delCalService;

    @Autowired
    RegularPmService regularPmService;

    @Autowired
    RegPmStationService regPmStationService;

    @Autowired
    CastService castService;

    @Autowired
    DateUtils dateUtils;

    @Autowired
    ServerUtils serverUtils;

    @Autowired
    TextController textController;

    @Autowired
    StringUtilsMine stringUtilsMine;

    /**
     * 各グループ画面用のデータ取得メソッド
     *
     * @param teamIdStr
     * @return
     */
    @GetMapping("/{teamIdStr}")
    public ResponseEntity<FAllDto> getTop(@PathVariable String teamIdStr) {
        logger.debug("accepted");

        Long teamId = 0L;
        if (teamIdStr == null || stringUtilsMine.isNumeric(teamIdStr)) {
            teamId = Long.parseLong(teamIdStr);
        } else {
            teamId = 17L;
        }

        // IMがない未来のItemを取得する（他チームで登録されてれば取得しない）
        List<Item> itemList = itemService.findByTeamIdFutureNotDeletedNoIM(teamId);

        // 未来/WPIDがnullのIMを取得する
        List<IM> imList = imService.findByTeamIdFutureOrWpIdNull(teamId);
        List<Item> itemList1 = itemService.findByTeamIdFutureNotDeletedWIM(teamId);
        List<ErrorJson> errorJsonList = errorJsonService.findByTeamIdNotSolved(teamId);

        logger.debug("accepted");
        FAllDto dto = new FAllDto();

        List<ItemTeamDto> itemTeamDtoList = new ArrayList<>();

        // IMのないItemリスト
        for (Item item : itemList) {
            ItemTeamDto itemTeamDto = new ItemTeamDto();
            List<IRel> irelList = iRelService.findByItemId(item.getItem_id());

            List<IRelMem> iRelMemList = new ArrayList<>();
            for (IRel irel : irelList) {
                iRelMemList.addAll(iRelMemService.findByIRelId(irel.getI_rel_id()));
            }

            List<Long> memIdList = Arrays.stream(MemberEnum.values()).map(MemberEnum::getId).collect(Collectors.toList());

            itemTeamDto.setItem(item);
            itemTeamDto.setRelList(irelList);
            itemTeamDto.setRelMemList(iRelMemList);
            itemTeamDto.setMemIdList(memIdList);
            itemTeamDtoList.add(itemTeamDto);
        }

        // IMリスト
        List<FIMDto> fimDtoList = new ArrayList<>();
        for (IM im : imList) {
            FIMDto imDto = new FIMDto();
            imDto.setIm(im);

            // verも追加
            List<ImVer> verList = imVerService.findByImId(im.getIm_id());
            imDto.setVerList(verList);

            // relListも入れる
            List<IMRel> imRelList = imRelService.findByImIdNotDeleted(im.getIm_id());
            imDto.setRelList(imRelList);

            List<IMRelMem> imRelMemList = new ArrayList<>();
            for (IMRel rel : imRelList) {
                imRelMemList.addAll(imRelMemService.findByImRelIdNotDeleted(rel.getIm_rel_id()));
            }

            // relMemListも入れる
            imDto.setRelMemList(imRelMemList);
            fimDtoList.add(imDto);
        }

        // IMのあるItemリスト
        List<ItemTeamDto> itemTeamDtoList1 = new ArrayList<>();
        for (Item item1 : itemList1) {
            ItemTeamDto itemTeamDto = new ItemTeamDto();
            List<IRel> irelList = iRelService.findByItemId(item1.getItem_id());

            List<IRelMem> iRelMemList = new ArrayList<>();
            for (IRel irel : irelList) {
                iRelMemList.addAll(iRelMemService.findByIRelId(irel.getI_rel_id()));
            }

            List<Long> memIdList = new ArrayList<>();
            for (IRelMem relMem : iRelMemList) {
                memIdList.add(relMem.getMember_id());
            }

            itemTeamDto.setItem(item1);
            itemTeamDto.setRelList(irelList);
            itemTeamDto.setMemIdList(memIdList);
            itemTeamDtoList1.add(itemTeamDto);
        }

        // 各チームのIMなし未来のitem件数を取得しDTOにセットします<TeamId, numberOfItems>
        Map<BigInteger, BigInteger> numberMap = itemService.getNumbersOfEachTeamIdFutureNotDeletedNoIM();
        dto.setI(itemTeamDtoList);
        dto.setIm(fimDtoList);
        dto.setErrJ(errorJsonList);
        dto.setItemNumberMap(numberMap);
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
     * PM一括登録
     *
     * @return Boolean true: success / false: failed
     */
    @PostMapping("/pm/bundle/new")
    public ResponseEntity<Boolean> updBundlePMyRegi(@Valid @RequestBody PMVerForm[] pmVerForms) {
        logger.debug("accepted");

        try {
            for (PMVerForm pmVerForm : pmVerForms) {
                newPMyVer(pmVerForm);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(false);
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
            ResponseEntity<Boolean> responseEntity = chkItem(imVerForm.getItem_id(), imVerForm.getIm_id(), imVerForm.getTeamId());
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
        IM im = imService.findById(id);
        List<IMRel> tmpList = imRelService.findByImIdTeamId(im.getIm_id(), teamId);
        IMRel rel = null;
        if (!tmpList.isEmpty() && tmpList.size() > 0) {
            rel = tmpList.get(0);
        }
        List<IMRel> relList = new ArrayList<>();
        relList.add(rel);
        List<ImVer> imVerList = imVerService.findByImId(im.getIm_id());

        FIMDto dto = new FIMDto();

        dto.setIm(im);
        dto.setRelList(relList);
        dto.setVerList(imVerList);
        // relMemListも入れる
        List<IMRelMem> imRelMemList = new ArrayList<>();
        for (IMRel imRel : relList) {
            imRelMemList.addAll(imRelMemService.findByImRelId(imRel.getIm_rel_id()));
        }

        dto.setRelMemList(imRelMemList);
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

            List<IMRel> imRelList = imRelService.findByItemMId(im.getIm_id());
            dto.setRelList(imRelList);

            List<ImVer> imVerList = imVerService.findByImId(im.getIm_id());
            dto.setVerList(imVerList);
            List<IMRelMem> imRelMemList = new ArrayList<>();
            for (IMRel imRel : imRelList) {
                imRelMemList.addAll(imRelMemService.findByImRelId(imRel.getIm_rel_id()));
            }

            dto.setRelMemList(imRelMemList);
            dtoList.add(dto);
        }
        logger.debug("fin");
        return ResponseEntity.ok(dtoList);
    }

    /**
     * 商品のデータを更新する
     * IMの更新なので、
     *
     * @param id データ更新をする商品のID
     * @param imForm 更新される新しいデータ
     * @return Item
     */
    @PostMapping("/im/{teamId}/{id}")
    public ResponseEntity<FIMDto> upIm(@PathVariable Long teamId, @PathVariable Long id, @Valid @RequestBody IMForm imForm) throws ParseException {
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

        // im自体の更新であればteamIdは影響ないしこのteamIdのimrelを取得する必要もない
//        IMRel rel = imRelService.findByImIdTeamId(imUpdated.getIm_id(), teamId).orElse(null);
        FIMDto dto = new FIMDto();
        dto.setIm(imUpdated);

        List<IMRel> imRelList = imRelService.findByItemMId(imUpdated.getIm_id());
        dto.setRelList(imRelList);

        List<ImVer> imVerList = imVerService.findByImId(imUpdated.getIm_id());
        dto.setVerList(imVerList);

        List<IMRelMem> imRelMemList = new ArrayList<>();
        for (IMRel imRel : imRelList) {
            imRelMemList.addAll(imRelMemService.findByImRelId(imRel.getIm_rel_id()));
        }

        dto.setRelMemList(imRelMemList);
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
            logger.debug("fin");
            imService.save(im);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(false);
        }
        return ResponseEntity.ok(true);
    }

    /**
     * IDからPMを削除する
     *
     * @param id 削除されるPMのID
     */
    @DeleteMapping("/pm/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Boolean> delPm(@PathVariable Long id) {
        logger.debug("accepted");
        try {
            PM pm = pmService.findByPmId(id);
            pm.setDel_flg(true);
            logger.debug("fin");
            pmService.save(pm);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(false);
        }
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
        logger.debug("fin");
        if (im != null) {
            blogController.postOrUpdate(im);
            return ResponseEntity.ok(true);
        } else {
            return ResponseEntity.ok(false);
        }
    }

    /**
     * TV一覧を返す
     *
     * @return リスト
     */
    @GetMapping("/tv/{teamIdStr}")
    public ResponseEntity<PAllDto> tvAll(@PathVariable String teamIdStr) {
        logger.debug("accepted");

        Long teamId = null;
        if (teamIdStr == null || stringUtilsMine.isNumeric(teamIdStr)) {
            teamId = Long.parseLong(teamIdStr);
        } else {
            teamId = 17L;
        }

        PAllDto pAllDto = new PAllDto();

        // PMのないprogramだけを集める
        List<PDto> pDtoList = new ArrayList<>();
        List<Program> pList = null;

        if (teamId == null || teamId == 5) {
            // teamId不正の場合
            return ResponseEntity.ok(null);
        } else {
            // チーム指定が適切に入っていればそのチームのを返す
            // 10レコードまで取得
            pList = programService.findbyTeamIdPmIdNullDelFlg(teamId, false, 10);
        }

        for (Program p : pList) {
            PDto pDto = new PDto();
            List<PRel> pRelList = pRelService.getListByProgramId(p.getProgram_id());

            List<PRelMem> pRelMemList = new ArrayList<>();
            for (PRel prel : pRelList) {
                pRelMemList.addAll(pRelMemService.findByPRelId(prel.getP_rel_id()));
            }

            // 関連ありそうなPMを集める
            // 同じ日の同じ放送局のもの
            List<String> relPmList = new ArrayList<>();
//            List<PmFullDto> relPmList = pmService.findByOnAirDateNotDeleted(p.getOn_air_date());
//            relPmList.addAll(pmFullDtoList.stream().map(e -> e.getOnAirDate().toString() + e.getTitle() + e.getDescription()).collect(Collectors.toList()));

            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");
            List<PmFullDto> pmFullDtoList = pmService.findByOnAirDateNotDeleted(p.getOn_air_date());
            relPmList.addAll(pmFullDtoList.stream().map(e -> e.getOnAirDate().toString() + e.getTitle() + e.getDescription()).collect(Collectors.toList()));

//            List<PmFullDto> pmFullDtoList = pmService.findPmFuByllDtoOnAirDateStationId(p.getOn_air_date(), p.getStation_id());
//            relPmList.addAll(pmFullDtoList.stream().map(e -> e.getOnAirDate().format(dateTimeFormatter) + " " + e.getTitle() + " " + e.getDescription()).collect(Collectors.toList()));

            // 同じ日・時間の（放送局は違くていい）
//            List<PmFullDto> pmFullDtoList2 = pmService.findPmFuByllDtoOnAirDateExStationId(p.getOn_air_date(), p.getStation_id());
//            relPmList.addAll(pmFullDtoList2.stream().map(e -> e.getOnAirDate().format(dateTimeFormatter) + " " + e.getTitle() + " " + e.getDescription()).collect(Collectors.toList()));

            pDto.setProgram(p);
            pDto.setPRelList(pRelList);
            pDto.setPRelMList(pRelMemList);
            pDto.setStation_name(stationService.findById(p.getStation_id()).getStation_name());
            pDtoList.add(pDto);
            pDto.setRelPmList(relPmList);
        }

        pAllDto.setP(pDtoList);

        // PMの方をつめる
        List<PMDto> pmDtoList = new ArrayList<>();
        // 放送局のマップ
        HashMap<Long, String> stationMap = new HashMap<>();

        // 全チームデータ取得の場合
        List<PM> pmList = null;
        if (teamId == null || teamId == 5) {
            pmList = pmService.findFutureDelFlg(false);
        } else {
            // チーム指定が適切に入っていればそのチームのを返す
            pmList = pmService.findByTeamIdFuture(teamId);
        }

        // PMの付随データを探しにいく
        for (PM pm : pmList) {
            PMDto dto = new PMDto();
            dto.setPm(pm);

            List<PMRel> relList = pmRelService.findByPmIdDelFlg(pm.getPm_id(), false);
            dto.setRelList(relList);

            List<PMRelMem> tmpList = new ArrayList<>();
            if (relList.size() > 0) {
                for (PMRel rel : relList) {
                    List<PMRelMem> relMemList = pmRelMemService.findByPRelIdDelFlg(rel.getPm_rel_id(), false);
                    tmpList.addAll(relMemList);
                }
                dto.setRelMemList(tmpList);
            }

            // verを取ってくる
            List<PMVer> pmVerList = pmVerService.findByPmIdDelFlg(pm.getPm_id(), false);
            List<PMVerDto> verDtoList = new ArrayList<>();
            for (PMVer ver : pmVerList) {
                // 放送局名を取得
                String stationName = stationService.getStationNameByEnumDB(ver.getStation_id());
                PMVerDto pmVerDto = new PMVerDto(ver.getPm_v_id(), ver.getOn_air_date(), ver.getStation_id(), stationName, ver.getDel_flg());
                verDtoList.add(pmVerDto);
            }
            dto.setVerList(verDtoList);

            // verの放送局が放送局マップに存在しない場合、追加する
            for (PMVer v : pmVerList) {
                if (stationMap.entrySet().stream().noneMatch(e -> e.getKey().equals(v.getStation_id()))) {
                    stationMap.put(v.getStation_id(), stationService.getStationNameByEnumDB(v.getStation_id()));
                }
            }

            // リストに入れる
            pmDtoList.add(dto);
        }
        // 返却リストに入れる
        pAllDto.setPm(pmDtoList);

        // regular_pmを入れる
        List<RegularPM> regPmList = regularPmService.findByTeamId(teamId);
        pAllDto.setRegPmList(regPmList);

        // 各チームごとに未確認のprogram数を取得しセット
        Map<BigInteger, BigInteger> numberMap = programService.getNumbersOfEachTeamIdFutureNotDeletedNoPM();
        pAllDto.setPNumberMap(numberMap);

        logger.debug("fin");
        return ResponseEntity.ok(pAllDto);
    }

    /**
     * RegPmを新規登録します
     *
     * @return Boolean
     */
    @PostMapping("/pm/reg/new")
    public ResponseEntity<Boolean> addRegPm(@RequestBody Map<String, Object> input) {

        if (!input.containsKey("title") || !input.containsKey("tm_id_arr")) {
            return ResponseEntity.ok(false);
        }

        RegularPM regPm = null;;

        // reg_pmの登録
        String title = input.get("title").toString();
        if (!regularPmService.existData(title)) {
            RegularPM newRegPm = new RegularPM();
            newRegPm.setTitle(title);
            regPm = regularPmService.save(newRegPm);
        }

        // castの登録
        if (regPm != null) {
            try {
                List<Integer> castArr = (List<Integer>) input.get("tm_id_arr");
                List<Cast> saveList = new ArrayList<>();

                for (Integer sta : castArr) {
                    Long l = new Long(sta);
                    if (!castService.existData(regPm.getRegular_pm_id(), l)) {
                        Cast cast = new Cast();
                        cast.setRegular_pm_id(regPm.getRegular_pm_id());
                        cast.setTm_id(l);
                        saveList.add(cast);
                    }
                }

                if (saveList.size() > 0) {
                    castService.saveAll(saveList);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // 放送局の登録
        if (input.containsKey("station_id_arr") && regPm != null) {
            try {
                List<Integer> castArr = (List<Integer>) input.get("station_id_arr");
                List<RegPmStation> saveList = new ArrayList<>();

                for (Integer sta : castArr) {
                    Long l = new Long(sta);
                    if (!regPmStationService.existData(regPm.getRegular_pm_id(), l)) {
                        RegPmStation regPmStation = new RegPmStation();
                        regPmStation.setRegular_pm_id(regPm.getRegular_pm_id());
                        regPmStation.setStation_id(l);
                        saveList.add(regPmStation);
                    }
                }

                if (saveList.size() > 0) {
                    regPmStationService.saveAll(saveList);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return ResponseEntity.ok(true);
    }

    /**
     * キーワードから放送局を検索します
     *
     * @param key
     * @return
     */
    @GetMapping("/pm/search/sta")
    public ResponseEntity<List<Station>>  searchStation(@RequestParam("key") String key) {
        return ResponseEntity.ok(stationService.findByName(key));
    }

//    /**
//     * 指定Teamidの商品を未来発売日順に取得し返す、削除されていない商品のみ。
//     *
//     * @param id 取得するTeamId
//     * @return Item
//     */
//    @GetMapping("/item/team/{id}")
//    public ResponseEntity<List<Item>> getItemTeam(@PathVariable Long id) {
//        logger.debug("getItemTeam teamId=" + id);
//        List<Item> imList = itemService.findByTeamIdNotDeleted(id);
//        logger.debug("fin");
//        return ResponseEntity.ok(imList);
//    }

    /**
     * 指定商品(Item)を新規登録します。
     * Itemとi_relを作ります
     * 無事に登録できた場合はそのteamIdのerrorJsonとItem(未来)リストを取得し直して返却します
     * errorJsonIdが連携されなかった場合はそのまま登録します
     *
     * @param id 該当のTeamId
     * @return Item
     */
    @PostMapping("/item/team/{id}")
    public ResponseEntity<Item> postItemTeam(@PathVariable Long id, @Valid @RequestBody ItemByJsonForm form) throws MyMessageException {
        logger.debug("postItemTeam teamId=" + id + " errorJsonId=" + form.getJsonId());

        ErrorJson j = null;

        if (form.getJsonId() != null) {
            // 該当のErrorJsonがしっかり存在する場合のみ処理を進める
            j = errorJsonService.findById(form.getJsonId());
        }

        Item savedItem;

        List<Item> regiItemList = new ArrayList<>();

        if (j != null) {
            regiItemList = itemService.isRegistered(form.getItem().getItem_code());
        }

        // item_codeかぶりがない場合、Itemを新規登録
        if (regiItemList.size() == 0) {
            savedItem = itemService.save(form.getItem());

            if (j != null) {
                // errorJsonも解決済みにする
                j.set_solved(true);
                errorJsonService.save(j);
            }

            // Itemは今新規登録したため、該当のirelは絶対ないはず。基本的には。なのでチェックなしでそのままirelの登録は入ってよし
            IRel rel = new IRel();
            rel.setTeam_id(id);
            rel.setItem_id(savedItem.getItem_id());
            IRel savedRel = iRelService.save(rel);
        } else {
            String siteIdList = regiItemList.stream().map(Item::getItem_code).collect(Collectors.joining(","));
            // すでにそのitem_codeの商品登録がある場合（楽天かyahooかどっちかにそのitem_codeの商品がある）、本当に登録するかを確認するようにメッセージを返却する
            throw new MyMessageException("そのitem_codeの商品登録がすでにある", "item_code=" + form.getItem().getItem_code(), "site_id=" + siteIdList);
        }

        logger.debug("fin");
        return ResponseEntity.ok(savedItem);
    }

    /**
     * 商品のデータを更新する
     *
     * @param id データ更新をする商品のID
     * @param form 更新される新しいデータ
     * @return Item
     */
    @PostMapping("/item/{teamId}/{id}")
    public ResponseEntity<Item> upItem(@PathVariable Long teamId, @PathVariable Long id, @Valid @RequestBody Item form) {
        logger.debug("accepted");
        Item item = itemService.findByItemId(id).orElse(new Item());
        item.absorb(form);
        Item savedItem = itemService.save(item);
        logger.debug("fin");
        return ResponseEntity.ok(savedItem);
    }

    /**
     * PMのデータを更新する
     *
     * @param id データ更新をするPMのID
     * @param form 更新される新しいデータ
     * @return Pm
     */
    @PostMapping("/pm/{id}")
    public ResponseEntity<Boolean> upPm(@PathVariable Long id, @Valid @RequestBody PMVerForm form) {
        logger.debug("accepted");

        // pm
        PM pm =pmService.findByPmId(id);
        Boolean updPmFlg = false;

        if (!form.getTitle().equals(pm.getTitle())) {
            pm.setTitle(form.getTitle());
            updPmFlg = true;
        }

        if (!form.getDescription().equals(pm.getDescription())) {
            pm.setDescription(form.getDescription());
            updPmFlg = true;
        }

        if (updPmFlg) {
            pmService.save(pm);
        }


        // pmrel
        List<PMRel> relList = pmRelService.findByPmIdDelFlg(pm.getPm_id(), null);
        List<PMRel> updRelList = new ArrayList<>();
        for (List<Integer> rel : form.getPmrel()) {
            PMRel targetRel = null;
            if (rel.get(0) != null && relList.stream().anyMatch(e -> e.getPm_rel_id().equals(rel.get(0).longValue()))) {
                // 既存のrelがあるならそれを更新
                targetRel = relList.stream().filter(e -> e.getPm_rel_id().equals(rel.get(0).longValue())).findFirst().get();
                if (!targetRel.getTeam_id().equals(rel.get(2).longValue())) {
                    targetRel.setTeam_id(rel.get(2).longValue());
                }
            } else if (relList.stream().noneMatch(e -> e.getTeam_id().equals(rel.get(2).longValue()))) {
                // 既存がなければ新規作成
                targetRel = new PMRel(null, rel.get(1).longValue(), rel.get(2).longValue(), null, null, false);
            }

            if (targetRel != null) {
                updRelList.add(targetRel);
            }
        }

        // pmrel更新対象があれば更新
        if (updRelList.size() > 0) {
            pmRelService.saveAll(updRelList);
        }

        // pmrelm
        List<PMRelMem> updMemList = new ArrayList<>();
        for (List<Integer> relMem : form.getPmrelm()) {
            PMRelMem targetMem = null;
            if (relMem.get(0) != null) {
                // IDがある場合元データを取得
                targetMem = pmRelMemService.findByPmRelMemId(relMem.get(0).longValue());

                if (!targetMem.getPm_rel_id().equals(relMem.get(1).longValue()) ) {
                    targetMem.setPm_rel_id(relMem.get(1).longValue());
                }

                if (!targetMem.getMember_id().equals(relMem.get(2).longValue())) {
                    targetMem.setMember_id(relMem.get(2).longValue());
                }
            } else {
                // IDがない場合要素からデータを確認
                targetMem = pmRelMemService.findByPmIdMemId(pm.getPm_id(), relMem.get(2).longValue());
                if (targetMem == null) {
                    Long relId = null;
                    if (relMem.get(1) == null) {
                        Long teamId = MemberEnum.get(relMem.get(2).longValue()).getTeamId();
                        relId = pmRelService.findByPmIdTeamId(pm.getPm_id(), teamId).getPm_rel_id();
                    } else {
                        relId = relMem.get(1).longValue();
                    }
                    targetMem = new PMRelMem(null, relId, relMem.get(2).longValue(), null, null, false);
                }
            }

            if (targetMem != null) {
                updMemList.add(targetMem);
            }
        }

        if (updMemList.size() > 0) {
            pmRelMemService.saveAll(updMemList);
        }

        // pmver
//        List<PMVer> updVList = new ArrayList<>();
//        for (PMVerDto verDto : form.getVerlist()) {
//            PMVer targetVer = null;
//            if (verDto.getPm_v_id() != null) {
//                // 元データがある場合
//                targetVer = pmVerService.findById(verDto.getPm_v_id());
//                BeanUtils.copyProperties(verDto, targetVer);
//            } else {
//                // 元データがない場合
//                targetVer = new PMVer(null, pm.getPm_id(), verDto.getOn_air_date(), verDto.getStation_id(), false, null, null);
//            }
//
//            if (targetVer != null) {
//                updVList.add(targetVer);
//            }
//        }

//        if (updVList.size() > 0) {
//            pmVerService.saveAll(updVList);
//        }

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
                logger.debug("fin");
                itemService.save(item);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(false);
        }
        return ResponseEntity.ok(true);
    }

    /**
     * IM+verを登録します。すでにIMがある場合は更新
     *
     * @return Boolean true: success / false: failed
     */
    @PostMapping("/im")
    public ResponseEntity<Boolean> newIMyVer(@Valid @RequestBody IMVerForm imVerForm) {
        logger.debug("accepted");
        Boolean updFlg = false;
        Boolean noCalFlg = false;

        try {
            IM im = null;
            Item item = itemService.findByItemId(imVerForm.getItem_id()).orElse(null);

            if (item == null) {
                return ResponseEntity.ok(false);
            }

            // im_idが入っていたらverだけ追加処理処理、入っていなかったらim新規登録とあればver追加処理、と判断（ここではimのタイトル変更などはできない）
            // まずはim
            // imから新規追加?
            if (imVerForm.getIm_id() == null || imVerForm.getIm_id() == 0) {

                // 対象のItemが見つからなかったら処理しません。見つかったら処理する。
                im = new IM();

                // 上書きしてくれるから新規登録も更新もこれだけでいけるはず
                BeanUtils.copyProperties(imVerForm, im);

                // 日付をstringからDateにして詰める
                if (!imVerForm.getPublication_date().equals("")) {
                    im.setPublication_date(dateUtils.stringToDate(imVerForm.getPublication_date(), "yyyy/MM/dd"));
                }

                if (im.getIm_id() != null && !im.getIm_id().equals(0L)) {
                    im.setBlogNotUpdated(true);
                    updFlg = true;
                }

                // rakuten urlを入れる(yahoo由来で楽天URLがわからない場合は入れない。バッチで夜に入れてもらう)
                if (item.getSite_id().equals(1)) {
                    im.setRakuten_url(item.getUrl());
                }

                // wordpressでエラーになる記号を処理し、設定し直す
                im.setTitle(textController.replaceSignals(im.getTitle()));

                // 登録前に本当に重複登録がないかチェック
                // 同じタイトルのimがあるなら、登録せずに0番目のimをセットする
                List<IM> checkedImList = imService.findByTitle(imVerForm.getTitle());
                if (checkedImList.size() == 0) {
                    IM savedIm = imService.save(im);
                    im = savedIm;
                } else {
                    im = checkedImList.get(0);
                    noCalFlg = true;
                }
            } else {
                im = imService.findById(imVerForm.getIm_id());
                noCalFlg = true;
            }

            // googleカレンダー登録のためのデータを用意する
            CalendarInsertDto calendarDto = setGCalDate(im);

            // imrelの登録を行います(irelは更新しない)
            if (imVerForm.getImrel() != null && imVerForm.getImrel().size() > 0) {
                List<List<Integer>> imrelList = imVerForm.getImrel();

                for (List<Integer> rel : imrelList) {
                    // imの新規登録の場合(=imrelはないはず)と更新の場合(=imrelがすでにあるかもしれない)で処理分岐
                    if (!updFlg) {
                        // IM新規登録の場合
                        // teamId=4(未選択)以外だったら登録
                        if (!rel.get(2).equals(4)) {
                            String eventId = "";
                            if (!noCalFlg) {
                                // googleカレンダーの登録を行う
                                Event event = calendarApiController.postEvent(TeamEnum.get(Long.valueOf(rel.get(2))).getCalendarId(), calendarDto.getStartDate(), calendarDto.getEndDate(), im.getTitle(), calendarDto.getDesc(), calendarDto.getAllDayFlg());
                                eventId = event.getId();
                            }

                            // imrelの登録
                            IMRel imRel = imRelService.save(new IMRel(null, im.getIm_id(), Long.valueOf(rel.get(2)), null, null, eventId, null, null, false, null));
                        }
                    } else {
                        // IM更新の場合
                        // rel.get(3)から、irelデータか(-> imrel新規登録)imrelデータか(->imrel更新or変更なし)かを判別して処理分岐
                        Boolean isImrelData = rel.get(3).equals(1);
                        if (isImrelData) {
                            // すでにimrelあるので、teamId確認して更新必要だったら更新する
                            IMRel imRel = imRelService.findByImRelId(Long.valueOf(rel.get(0)));
                            if (!imRel.getTeam_id().equals(Long.valueOf(rel.get(2)))) {
                                // teamId=4(未選択)だったらdel_flg=onにする。それ以外だったら更新

                                // calendarが入っていれば非表示にする
                                if (imRel.getCalendar_id() != null) {
                                    Event event = calendarApiController.hideEvent(imRel.getTeam_id(), imRel.getCalendar_id());

                                    // imrelのcalendarid上書くため、退避する
                                    DelCal delCal = new DelCal(null, TeamEnum.get(imRel.getTeam_id()).getCalendarId(), imRel.getTeam_id(), event.getId(), 1L, null, null);
                                    delCalService.save(delCal);
                                }

                                if (rel.get(2).equals(4)) {
                                    imRel.setDel_flg(true);
                                } else {
                                    imRel.setTeam_id(Long.valueOf(rel.get(2)));

                                    // calendar新しく作る
                                    String eventId = "";
                                    if (!noCalFlg) {
                                        Event event = calendarApiController.postEvent(TeamEnum.get(imRel.getTeam_id()).getCalendarId(), calendarDto.getStartDate(), calendarDto.getEndDate(), calendarDto.getTitle(), calendarDto.getDesc(), calendarDto.getAllDayFlg());
                                        eventId = event.getId();
                                    }
                                    imRel.setCalendar_id(eventId);
                                }
                                imRelService.save(imRel);
                            }
                        } else {
                            // irelデータなので、新規でImrelを登録してあげる

                            // teamId=4(未選択)以外だったら処理進める
                            if (!rel.get(2).equals(4)) {
                                // すでにimrelが登録されてるかもしれないので取得する
                                List<Long> savedImRelTeamIdList = imRelService.findTeamIdByItemMId(im.getIm_id());
                                // 該当teamの登録がすでにないか一応確認
                                Long teamId = savedImRelTeamIdList.stream().filter(e -> e.equals(Long.valueOf(rel.get(2)))).findFirst().orElse(null);
                                if (teamId == null) {
                                    // ないのが確認できたら新規登録
                                    // googleカレンダーの登録を行う
                                    String eventId = "";
                                    if (!noCalFlg) {
                                        Event event = calendarApiController.postEvent(TeamEnum.get(Long.valueOf(rel.get(2))).getCalendarId(), calendarDto.getStartDate(), calendarDto.getEndDate(), im.getTitle(), calendarDto.getDesc(), calendarDto.getAllDayFlg());
                                        eventId = event.getId();
                                    }

                                    // imrelの登録
                                    imRelService.save(new IMRel(null, im.getIm_id(), Long.valueOf(rel.get(2)), null, null, eventId, null, null, false, null));
                                }
                            }
                        }
                    }
                }
            }

            // imrelMemの登録を行います(irelMemは更新しない)
            if (imVerForm.getImrelm() != null && imVerForm.getImrelm().size() > 0) {
                List<List<Integer>> imrelmList = imVerForm.getImrelm();

                // IDがすでにあれば更新、なければ新規登録をする
                for (List<Integer> imrelm : imrelmList) {
                    // imの新規登録の場合(=imrelMはないはず)と更新の場合(=imrelMがすでにあるかもしれない)で処理分岐

                    if (!updFlg) {
                        // IM新規登録の場合、imrelmemもないはずなので新規登録

                        // memberId=30(未選択)以外だったら新規登録
                        if (!imrelm.get(2).equals(30)) {
                            Long tmpTeamId = MemberEnum.getTeamIdById(Long.valueOf(imrelm.get(2)));
                            List<IMRel> tmpList = imRelService.findByImIdTeamId(im.getIm_id(), tmpTeamId);
                            IMRel targetImRel = null;
                            if (!tmpList.isEmpty() && tmpList.size() > 0) {
                                targetImRel = tmpList.get(0);
                            }

                            // teamIdが登録されていなかったらimrelを登録する
                            if (targetImRel == null) {
                                String eventId = "";
                                if (!noCalFlg) {
                                    // googleカレンダーの登録を行う
                                    Event event = calendarApiController.postEvent(TeamEnum.get(tmpTeamId).getCalendarId(), calendarDto.getStartDate(), calendarDto.getEndDate(), im.getTitle(), calendarDto.getDesc(), calendarDto.getAllDayFlg());
                                    eventId = event.getId();
                                }

                                // imrelの登録
                                targetImRel = imRelService.save(new IMRel(null, im.getIm_id(), tmpTeamId, null, null, eventId, null, null, false, null));
                            }

                            imRelMemService.save(new IMRelMem(null, targetImRel.getIm_rel_id(), Long.valueOf(imrelm.get(2)), null, null, false));
                        }
                    } else {
                        // IM更新の場合
                        // imrelm.get(3)から、irelMデータか(-> imrelM新規登録)imrelMデータか(->imrelM更新or変更なし)かを判別して処理分岐
                        Boolean isImrelData = imrelm.get(3).equals(1);

                        if (isImrelData) {
                            // すでにimrelMデータあるのでmemberの更新が必要であれば更新してあげる
                            IMRelMem imRelMem = imRelMemService.findByImRelMemId(Long.valueOf(imrelm.get(0)));

                            // memberId=30(未選択)だったらdel_flg=trueにしてあげる。それ以外だったら必要であれば更新
                            if (imrelm.get(2).equals(30)) {
                                imRelMem.setDel_flg(true);
                                imRelMemService.save(imRelMem);
                            } else {
                                if (!imRelMem.getMember_id().equals(Long.valueOf(imrelm.get(2)))) {
                                    imRelMem.setMember_id(Long.valueOf(imrelm.get(2)));
                                    imRelMemService.save(imRelMem);
                                }
                            }
                        } else {
                            // memberId=30(未選択)以外であれば登録してあげる
                            if (!imrelm.get(2).equals(30)) {
                                // TeamIdがまず登録されてるか確認する
                                Long tmpTeamId = MemberEnum.getTeamIdById(Long.valueOf(imrelm.get(2)));
                                IMRel targetImRel = null;
                                List<IMRel> tmpList = imRelService.findByImIdTeamId(im.getIm_id(), tmpTeamId);
                                if (!tmpList.isEmpty() && tmpList.size() > 0) {
                                    targetImRel = tmpList.get(0);
                                }

                                // teamIdが登録されていなかったらimrelを登録する
                                if (targetImRel == null) {
                                    // googleカレンダーの登録を行う
                                    String eventId = "";
                                    if (!noCalFlg) {
                                        Event event = calendarApiController.postEvent(TeamEnum.get(tmpTeamId).getCalendarId(), calendarDto.getStartDate(), calendarDto.getEndDate(), im.getTitle(), calendarDto.getDesc(), calendarDto.getAllDayFlg());
                                        eventId = event.getId();
                                    }

                                    // imrelの登録
                                    targetImRel = imRelService.save(new IMRel(null, im.getIm_id(), tmpTeamId, null, null, eventId, null, null, false, null));
                                }

                                // 既存でimrelmemの登録がないか確認
                                IMRelMem imRelMem = imRelMemService.findByImRelIdMemId(targetImRel.getIm_rel_id(), tmpTeamId).orElse(null);
                                if (imRelMem == null) {
                                    // imrelの用意ができたのでimrelmemを登録する
                                    imRelMemService.save(new IMRelMem(null, targetImRel.getIm_rel_id(), Long.valueOf(imrelm.get(2)), null, null, false));
                                }
                            }
                        }
                    }
                }
            }

            // itemのim_idを登録します
            item.setIm_id(im.getIm_id());
            item.setFct_chk(true);
            itemService.save(item);

            // verがあれば登録します
            List<String[]> verArr = imVerForm.getVers();

            if (verArr.size() > 0) {

                for (String[] ver : verArr) {
                    String verName = ver[1];

                    ImVer newVer = new ImVer();
                    newVer.setVer_name(textController.replaceSignals(verName));
                    newVer.setIm_id(im.getIm_id());
                    newVer.setDel_flg(false);
                    imVerService.save(newVer);
                }
            }

            logger.debug("fin");
            return ResponseEntity.ok(true);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(false);
        }
    }

    /**
     * regular_pmの登録・更新
     *
     * @return Boolean true: success / false: failed
     */
    @PostMapping("/tv/saveReg")
    public ResponseEntity<Boolean> addNewRegPm(@Valid @RequestBody RegPmForm regPmForm) throws ParseException {
        if (regPmForm == null || regPmForm.getTitle().equals("")) {
            logger.info("RegPmFormの中身が不足でregular_pm登録できません");
            return ResponseEntity.ok(false);
        }

        // 既存データがないかチェック
        Boolean existData = regularPmService.existData(regPmForm.getTitle());
        RegularPM regPm = new RegularPM();
        if (!existData) {
            // 新規データの場合
            regPm = new RegularPM();
        } else {
            // 既存データがある場合
            regPm = regularPmService.findById(regPmForm.getRegular_pm_id());
        }

        if (regPm != null) {
            BeanUtils.copyProperties(regPmForm, regPm);

            // 日付をstringからDateにして詰める
            if (!regPmForm.getStart_date().equals("")) {
                regPm.setStart_date(dateUtils.stringToLocalDateTime(regPmForm.getStart_date(), "yyyy/MM/dd hh:mm"));
            }

            List<Cast> existCastList = castService.findByRegPmId(regPm.getRegular_pm_id());
            List<Cast> castList = new ArrayList<>();
            for (Cast c : regPmForm.getCasts()) {
                Cast cast = new Cast();

                Boolean existCastFlg = existCastList.stream().anyMatch(e -> e.getTm_id().equals(c.getTm_id()));
                if (!existData || !existCastFlg) {
                    // regpm自体がないか、regpmはあるけどcastがない場合は処理する
                    // メンバー
                    if (c.getTm_id() >= 30L) {
                       // 所属チームのデータがないかチェック
                        MemberEnum me = MemberEnum.get(c.getTm_id());
                        Boolean existTeamFlg = existCastList.stream().anyMatch(e -> e.getTm_id().equals(me.getTeamId()));
                        if (!existTeamFlg) {
                            BeanUtils.copyProperties(c, cast);
                            castList.add(cast);
                        }
                    } else {
                        // チームの場合
                        Boolean existTeamFlg = existCastList.stream().anyMatch(e -> e.getTm_id().equals(c.getTm_id()));
                        if (!existTeamFlg) {
                            // 既存チームデータがない場合、登録判定に進む
                            // チームのメンバーIDリストを作る
                            List<Long> memIdList = MemberEnum.findMemIdListByTeamId(c.getTm_id());

                            Boolean existMemFlg = memIdList.stream().anyMatch(e -> existCastList.stream().anyMatch(f -> e.equals(f.getTm_id())));
                            Boolean candMemFlg = memIdList.stream().anyMatch(e -> Arrays.asList(regPmForm.getCasts()).stream().anyMatch(f -> e.equals(f.getTm_id())));

                            if (existMemFlg) {
                                // 既存メンバー登録があったらメンバー登録を削除、チーム登録
                                for (Cast c1 : existCastList) {
                                    // メンバーの場合
                                    if (c1.getTm_id() >= 30L && MemberEnum.get(c1.getTm_id()).getTeamId().equals(c.getTm_id())) {
                                        c1.setDel_flg(true);
                                        castList.add(c1);
                                    }
                                }
                                BeanUtils.copyProperties(c, cast);
                                castList.add(cast);
                            } else if (candMemFlg) {
                                // フォーム内にメンバーがあったらメンバーを削除、チーム登録
                                for (Cast c1 : existCastList) {
                                    // メンバーの場合
                                    if (c1.getTm_id() >= 30L && MemberEnum.get(c1.getTm_id()).getTeamId().equals(c.getTm_id())) {
                                        c1.setDel_flg(true);
                                        castList.add(c1);
                                    }
                                }

                                // メンバー登録ないのでそのままチーム登録する
                                BeanUtils.copyProperties(c, cast);
                                castList.add(cast);
                            }
                        }
                    }
                }
            }
            if (castList.size() > 0) {
                castService.saveAll(castList);
            }
        }
        return ResponseEntity.ok(true);
    }

    /**
     * PM+付随データを登録します。すでにPMがある場合は更新
     *
     * @return Boolean true: success / false: failed
     */
    @PostMapping("/tv")
    public ResponseEntity<Boolean> newPMyVer(@Valid @RequestBody PMVerForm pmVerForm) {
        logger.debug("accepted");
        Boolean updFlg = false;

        try {
            PM pm = null;
            Program program = programService.findByPId(pmVerForm.getProgram_id());

            if (program == null) {
                return ResponseEntity.ok(false);
            }

            // pm_idが入っていたらverだけ追加処理処理、入っていなかったらpm新規登録とあればver追加処理、と判断（ここではpmのタイトル変更などはできない）
            // まずはpm
            if (pmVerForm.getPm_id() == null || pmVerForm.getPm_id() == 0) {

                // 対象のItemが見つからなかったら処理しません。見つかったら処理する。
                pm = new PM();

                // 上書きしてくれるから新規登録も更新もこれだけでいけるはず
                BeanUtils.copyProperties(pmVerForm, pm);

                if (pm.getPm_id() != null && !pm.getPm_id().equals(0L)) {
                    updFlg = true;
                }

                // wordpressでエラーになる記号を処理し、設定し直す
                pm.setTitle(textController.replaceSignals(pm.getTitle()));

                // 登録前に本当に重複登録がないかチェック
                // 同じタイトルのpmがあるなら、登録せずに0番目のpmをセットする
                List<PM> checkedPmList = pmService.findByTitle(pmVerForm.getTitle());
                if (checkedPmList.size() == 0) {
                    PM savedPm = pmService.save(pm);
                    pm = savedPm;
                } else {
                    pm = checkedPmList.get(0);
                }
            } else {
                pm = pmService.findByPmId(pmVerForm.getPm_id());
            }

            // pmrelの登録を行います(irelは更新しない)
            if (pmVerForm.getPmrel() != null && pmVerForm.getPmrel().size() > 0) {
                List<List<Integer>> pmrelList = pmVerForm.getPmrel();

                for (List<Integer> rel : pmrelList) {
                    // pmの新規登録の場合(=pmrelはないはず)と更新の場合(=pmrelがすでにあるかもしれない)で処理分岐
                    if (!updFlg) {
                        // PM新規登録の場合
                        // teamId=4(未選択)以外だったら登録
                        if (!rel.get(2).equals(4)) {
                            // pmrelの登録
                            PMRel pmRel = new PMRel(null, pm.getPm_id(), Long.valueOf(rel.get(2)), null, null, false);
                            pmRelService.save(pmRel);
                        }
                    } else {
                        // PM更新の場合
                        // rel.get(3)から、prelデータか(-> pmrel新規登録)pmrelデータか(->pmrel更新or変更なし)かを判別して処理分岐
                        Boolean isPmrelData = rel.get(3).equals(1);
                        if (isPmrelData) {
                            // すでにpmrelあるので、teamId確認して更新必要だったら更新する
                            PMRel pmRel = pmRelService.findByPmRelId(Long.valueOf(rel.get(0)));
                            if (!pmRel.getTeam_id().equals(Long.valueOf(rel.get(2)))) {
                                // teamId=4(未選択)だったらdel_flg=onにする。それ以外だったら更新
                                if (rel.get(2).equals(4)) {
                                    pmRel.setDel_flg(true);
                                } else {
                                    pmRel.setTeam_id(Long.valueOf(rel.get(2)));
                                }
                                pmRelService.save(pmRel);
                            }
                        } else {
                            // prelデータなので、新規でPmrelを登録してあげる
                            // teamId=4(未選択)以外だったら処理進める
                            if (!rel.get(2).equals(4)) {
                                // すでにpmRelが登録されてるかもしれないので取得する
                                List<Long> savedPmRelTeamIdList = pmRelService.findTeamIdByProgramId(pm.getPm_id());

                                // 該当teamの登録がすでにないか一応確認
                                Long teamId = savedPmRelTeamIdList.stream().filter(e -> e.equals(Long.valueOf(rel.get(2)))).findFirst().orElse(null);
                                if (teamId == null) {
                                    // ないのが確認できたら新規登録
                                    // pmRelの登録
                                    PMRel pmRel = new PMRel(null, pm.getPm_id(), Long.valueOf(rel.get(2)), null, null, false);
                                    pmRelService.save(pmRel);
                                }
                            }
                        }
                    }
                }
            }

            // pmRelMemの登録を行います(irelMemは更新しない)
            if (pmVerForm.getPmrelm() != null && pmVerForm.getPmrelm().size() > 0) {
                List<List<Integer>> pmRelmList = pmVerForm.getPmrelm();

                // IDがすでにあれば更新、なければ新規登録をする
                for (List<Integer> pmRelm : pmRelmList) {
                    // pmの新規登録の場合(=pmRelMはないはず)と更新の場合(=pmRelMがすでにあるかもしれない)で処理分岐

                    if (!updFlg) {
                        // PM新規登録の場合、pmRelmemもないはずなので新規登録

                        // memberId=30(未選択)以外だったら新規登録
                        if (!pmRelm.get(2).equals(30)) {
                            Long tmpTeamId = MemberEnum.getTeamIdById(Long.valueOf(pmRelm.get(2)));
                            PMRel targetPmRel = pmRelService.findByPmIdTeamId(pm.getPm_id(), tmpTeamId);

                            // teamIdが登録されていなかったらpmRelを登録する
                            if (targetPmRel == null) {
                                // pmRelの登録
                                targetPmRel = pmRelService.save(new PMRel(null, pm.getPm_id(), tmpTeamId, null, null, false));
                            }

                            pmRelMemService.save(new PMRelMem(null, targetPmRel.getPm_rel_id(), Long.valueOf(pmRelm.get(2)), null, null, false));
                        }
                    } else {
                        // PM更新の場合
                        // pmRelm.get(3)から、prelMデータか(-> pmRelM新規登録)pmRelMデータか(->pmRelM更新or変更なし)かを判別して処理分岐
                        Boolean isPmrelData = pmRelm.get(3).equals(1);

                        if (isPmrelData) {
                            // すでにpmRelMデータあるのでmemberの更新が必要であれば更新してあげる
                            PMRelMem pmRelMem = pmRelMemService.findByPmRelMemId(Long.valueOf(pmRelm.get(0)));

                            // memberId=30(未選択)だったらdel_flg=trueにしてあげる。それ以外だったら必要であれば更新
                            if (pmRelm.get(2).equals(30)) {
                                pmRelMem.setDel_flg(true);
                                pmRelMemService.save(pmRelMem);
                            } else {
                                if (!pmRelMem.getMember_id().equals(Long.valueOf(pmRelm.get(2)))) {
                                    pmRelMem.setMember_id(Long.valueOf(pmRelm.get(2)));
                                    pmRelMemService.save(pmRelMem);
                                }
                            }
                        } else {
                            // memberId=30(未選択)以外であれば登録してあげる
                            if (!pmRelm.get(2).equals(30)) {
                                // TeamIdがまず登録されてるか確認する
                                Long tmpTeamId = MemberEnum.getTeamIdById(Long.valueOf(pmRelm.get(2)));
                                PMRel targetPmRel = pmRelService.findByPmIdTeamId(pm.getPm_id(), tmpTeamId);

                                // teamIdが登録されていなかったらpmRelを登録する
                                if (targetPmRel == null) {
                                    // pmRelの登録
                                    targetPmRel = pmRelService.save(new PMRel(null, pm.getPm_id(), tmpTeamId, null, null, false));
                                }

                                // 既存でpmRelmemの登録がないか確認
                                PMRelMem pmRelMem = pmRelMemService.findByPmRelIdMemId(targetPmRel.getPm_rel_id(), tmpTeamId);
                                if (pmRelMem == null) {
                                    // pmRelの用意ができたのでpmRelmemを登録する
                                    pmRelMemService.save(new PMRelMem(null, targetPmRel.getPm_rel_id(), Long.valueOf(pmRelm.get(2)), null, null, false));
                                }
                            }
                        }
                    }
                }
            }

            // programのpm_idを登録します
            program.setPm_id(pm.getPm_id());
            program.setFct_chk(true);
            programService.save(program);

            // verがあれば登録します
            List<PMVer> pmVerList = pmVerService.findByPmIdDelFlg(pm.getPm_id(), null);

            PMVer targetVer = null;
            if (pmVerList == null || pmVerList.size() == 0) {
                targetVer = new PMVer(null, pm.getPm_id(), program.getOn_air_date(), program.getStation_id(), false, null, null);
            } else {
                if (pmVerList.stream().noneMatch(e -> e.getStation_id().equals(program.getStation_id()) && e.getOn_air_date().equals(program.getOn_air_date()))) {
                    targetVer = new PMVer(null, pm.getPm_id(), program.getOn_air_date(), program.getStation_id(), false, null, null);
                } else if (pmVerList.stream().anyMatch(e -> e.getStation_id().equals(program.getStation_id()) && e.getOn_air_date().equals(program.getOn_air_date()))) {
                    targetVer = pmVerList.stream().filter(e -> e.getStation_id().equals(program.getStation_id()) && e.getOn_air_date().equals(program.getOn_air_date())).findFirst().get();
                    if (targetVer.getDel_flg().equals(true)) {
                        targetVer.setDel_flg(false);
                    } else {
                        targetVer = null;
                    }
                }
            }

            if (targetVer != null) {
                pmVerService.save(targetVer);
            }

            // カレンダーを登録・更新する
            // 既存カレンダーデータ取得する
            List<PMVer> verList = pmVerService.findByPmIdDelFlg(pm.getPm_id(), true);
            List<PMRel> relList = pmRelService.findByPmIdDelFlg(pm.getPm_id(), true);
            List<Long> verIdList = verList.stream().map(e -> e.getPm_v_id()).collect(Collectors.toList());
            List<Long> relIdList = relList.stream().map(e -> e.getPm_rel_id()).collect(Collectors.toList());
            List<PMCal> pmCalList = pmCalService.findByVerIdListRelIdListDelFlg(verIdList, relIdList, false);
            List<PMCal> updCalList = new ArrayList<>();

            // 既存データのないものは作成する
            for (PMVer ver : verList) {
                for (PMRel rel : relList) {
                    if (pmCalList.stream().noneMatch(e -> e.getPm_ver_id().equals(ver.getPm_v_id()) && e.getPm_rel_id().equals(rel.getPm_rel_id()))) {
                        // 作成条件合致したらまず既存削除データがないか確認する
                        PMCal delCal = pmCalService.findByVerIdRelIdDelFlg(ver.getPm_v_id(), rel.getPm_rel_id(), true);
                        if (delCal != null) {
                            // 既存があればフラグの変更のみ
                            delCal.setDel_flg(false);
                            updCalList.add(delCal);
                        } else {
                            // 既存がないなら新規作成
                            PMCal cal = new PMCal(null, ver.getPm_v_id(), rel.getPm_rel_id(), false, null, false);
                            updCalList.add(cal);
                        }
                    }
                }
            }

            // 既存データの不要なものは削除する
            for (PMCal cal : pmCalList) {
                if (verList.stream().noneMatch(e -> e.getPm_v_id().equals(cal.getPm_ver_id()))
                        || (verList.stream().anyMatch(e -> e.getPm_v_id().equals(cal.getPm_ver_id())) && relList.stream().noneMatch(e -> e.getPm_rel_id().equals(cal.getPm_rel_id())) )) {
                    cal.setDel_flg(true);
                    // TODO: カレンダーを抜きたい
                    updCalList.add(cal);
                }
            }

            // 更新が必要なデータ全部更新。
            List<PMCal> targetCalList = pmCalService.saveAll(updCalList);

            // googleカレンダー登録のためのデータを用意する
            for (PMCal cal : targetCalList) {
                PMVer targetVer2 = verList.stream().filter(e -> e.getPm_v_id().equals(cal.getPm_ver_id())).findFirst().get();
                PMRel targetRel = relList.stream().filter(e -> e.getPm_rel_id().equals(cal.getPm_rel_id())).findFirst().get();
                // TODO: ここの処理減らせる。rel使わないから同じverのは1度だけ取得するようにしてあげるといい
                CalendarInsertDto calendarDto = setGCalDatePm(pm, targetVer2);
                Event event = calendarApiController.postEvent(TeamEnum.get(targetRel.getTeam_id()).getCalendarId(), calendarDto.getStartDateTime(), calendarDto.getEndDateTime(), pm.getTitle(), calendarDto.getDesc(), calendarDto.getAllDayFlg());
                cal.setCalendar_id(event.getId());
                cal.setCal_active_flg(true);
                pmCalService.save(cal);
            }

            logger.debug("fin");
            return ResponseEntity.ok(true);
        } catch (Exception e) {
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
    public CalendarInsertDto setGCalDatePm(PM pm, PMVer ver) {
        CalendarInsertDto dto = new CalendarInsertDto();

        dto.setTitle(pm.getTitle());

        LocalDateTime startDate = ver.getOn_air_date();
        LocalDateTime endDate = ver.getOn_air_date();

        dto.setStartDateTime(startDate);
        dto.setEndDateTime(endDate);
//        String url = stringUtilsMine.getAmazonLinkFromCard(pm.getAmazon_pmage()).orElse(null);

//        if (url == null) {
//            List<Item> itemList = itemService.findByMasterId(pm.getIm_id());
//            for (Item i : itemList) {
//                if (i.getUrl() != null) {
//                    url = i.getUrl();
//                    break;
//                }
//            }
//        }
//
//        if (url == null) {
//            url = "";
//        }

        dto.setDesc(pm.getDescription());

        dto.setAllDayFlg(false);
        return dto;
    }
    
    /**
     * IM+verを更新します
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

            // imrelを更新
            // formのimrel1つずつ処理
            List<IMRel> imRelList = imRelService.findByImIdNotDeleted(im.getIm_id());
            List<Long> copyRelIdListt = new ArrayList<>();

            for (List<Integer> imrel : imVerForm.getImrel()) {

                IMRel targetRel = null;
                boolean newRelFlg = false;

                // listから元データが見つかるか？
                if (imrel.get(0) != null && imRelList.stream().anyMatch(e -> e.getIm_rel_id().equals(imrel.get(0).longValue()))) {
                    targetRel = imRelList.stream().filter(e -> e.getIm_rel_id().equals(imrel.get(0).longValue())).findFirst().get();

                    // 値に変更がある時だけ更新する
                    if (!targetRel.getIm_id().equals(imrel.get(1).longValue()) || !targetRel.getTeam_id().equals(imrel.get(2).longValue())) {
                        targetRel.setDel_flg(true);
                        imRelService.save(targetRel);

                        if (!imrel.get(2).equals(4)) {
                            newRelFlg = true;
                        }
                    }
                    copyRelIdListt.add(targetRel.getIm_rel_id());
                } else {
                    // del_flgがtrueのレコードで該当のがないか検索
                    List<IMRel> targetRel2 = imRelService.findByImIdTeamId(imrel.get(1).longValue(), imrel.get(2).longValue());
                    if (!targetRel2.isEmpty() && targetRel2.size() > 0) {
                        // targetがあったらdel_flgをfalseに戻してあげてレコード復活
                        targetRel2.get(0).setDel_flg(false);
                        imRelService.save(targetRel2.get(0));
                    } else {
                        newRelFlg = true;
                    }
                }

                if (newRelFlg) {
                    IMRel newRel = new IMRel(null, imrel.get(1).longValue(), imrel.get(2).longValue(), null, null, TeamEnum.get(imrel.get(2).longValue()).getCalendarId(), null, null, false, null);
                    imRelService.save(newRel);
                }
            }

            // 削除処理がないか、更新後のサイズから確認
            if (copyRelIdListt.size() != imRelList.size()) {
                for (IMRel rel : imRelList) {
                    if (!copyRelIdListt.contains(rel.getIm_rel_id())) {
                        rel.setDel_flg(true);
                        imRelService.save(rel);
                    }
                }
            }

            // memlistも更新
            if (imVerForm.getImrelm() != null) {
                // 今有効なrellistを取得
                List<IMRel> relList = imRelService.findByImIdNotDeleted(im.getIm_id());

                // relごとに該当のrelmemを見つけて更新
                for (IMRel rel : relList) {
                    // 今すでに登録されてるデータを取ってくる
                    List<IMRelMem> existRelMemList = imRelMemService.findByImRelIdNotDeleted(rel.getIm_rel_id());
                    List<Long> updatedRelMemIdList = new ArrayList<>();
                    boolean newMenFlg = false;

                    if (imVerForm.getImrelm().stream().anyMatch(e -> e.get(1).longValue() == rel.getIm_rel_id())) {
                        List<List<Integer>> relMList = imVerForm.getImrelm().stream().filter(e -> e.get(1).longValue() == rel.getIm_rel_id()).collect(Collectors.toList());
                        for (List<Integer> data : relMList) {
                            IMRelMem targetMem = null;
                            if (data.get(0) != null) {
                                targetMem = existRelMemList.stream().filter(e -> e.getIm_rel_mem_id() == data.get(0).longValue()).findFirst().orElse(null);

                                if (targetMem != null && targetMem.getIm_rel_mem_id().equals(data.get(0).longValue())
                                        && (!targetMem.getIm_rel_id().equals(data.get(1).longValue()) || !targetMem.getMember_id().equals(data.get(2).longValue()))) {
                                    targetMem.setDel_flg(true);
                                    imRelMemService.save(targetMem);

                                    if (!data.get(2).equals(30)) {
                                        newMenFlg = true;
                                    }
                                }

                                if (targetMem != null) {
                                    updatedRelMemIdList.add(targetMem.getIm_rel_mem_id());
                                }

                            } else {
                                Optional<IMRelMem> targetMem2 = imRelMemService.findByImRelIdMemId(rel.getIm_rel_id(), data.get(2).longValue());

                                if (targetMem2.isPresent()) {
                                    targetMem2.get().setDel_flg(false);
                                    imRelMemService.save(targetMem2.get());
                                } else {
                                    newMenFlg = true;
                                }
                            }

                            if (newMenFlg) {
                                IMRelMem newMem = new IMRelMem(null, data.get(1).longValue(), data.get(2).longValue(), null, null, false);
                                imRelMemService.save(newMem);
                            }
                        }
                    }

                    // 更新漏れがないか確認、あるならdel_flg=trueにして更新
                    if (existRelMemList.size() != updatedRelMemIdList.size()) {
                        for (IMRelMem relMem : existRelMemList) {
                            if (updatedRelMemIdList.stream().noneMatch(e -> e.equals(relMem.getIm_rel_mem_id()))) {
                                relMem.setDel_flg(true);
                                imRelMemService.save(relMem);
                            }
                        }
                    }
                }
            }

            logger.debug("fin");
            return ResponseEntity.ok(true);
        } catch (Exception e) {
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
     * Program一括削除
     *
     * @return Boolean true: success / false: failed
     */
    @PostMapping("/tv/bundle/del_p")
    public ResponseEntity<Boolean> bundleDelP(@Valid @RequestBody Long[] pIdArr) {
        logger.debug("accepted");
        List<Program> pList = programService.findByPidList(Arrays.asList(pIdArr));
        if (pList.size() > 0) {
            pList.forEach(e -> e.setDel_flg(true));
            programService.saveAll(pList);
        }
        return ResponseEntity.ok(true);
    }

    /**
     * Itemにim_idを追加してfct_chkを更新します（既存imある場合ですね）
     *
     * @return Boolean true: success / false: failed
     */
    @GetMapping("/im/chk")
    public ResponseEntity<Boolean> chkItem(@RequestParam("itemId") Long itemId, @RequestParam("imId") Long imId, @RequestParam("teamId") Long teamId) {
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

            // imrelがない場合は作成します
            IMRel rel = null;
            List<IMRel> tmpList = imRelService.findByImIdTeamId(imId, teamId);
            if (!tmpList.isEmpty() && tmpList.size() > 0) {
                rel = tmpList.get(0);
            }

            if (rel == null) {
                IMRel newRel = new IMRel();
                newRel.setTeam_id(teamId);
                newRel.setIm_id(imId);
                imRelService.save(newRel);
            }

            logger.debug("fin");
            return ResponseEntity.ok(true);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(false);
        }
    }

    /**
     * Programにpm_idを追加してfct_chkを更新します（既存pmある場合ですね）
     *
     * @return Boolean true: success / false: failed
     */
    @GetMapping("/pm/chk")
    public ResponseEntity<Boolean> chkProgram(@RequestParam("pId") Long pid, @RequestParam("pmId") Long pmId) {
        logger.debug("accepted");

        try {
            Program program = programService.findByPId(pid);

            if (program != null) {
                program.setPm_id(pmId);
                program.setFct_chk(true);
                programService.save(program);
            }

            logger.debug("fin");
            return ResponseEntity.ok(true);
        } catch (Exception e) {
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
     * PMを検索する
     *
     * @param key
     * @return
     */
    @GetMapping("/pm/search")
    public ResponseEntity<List<PM>> searchOtherPm(@RequestParam("key") String key) {
        if (key.equals("") ) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(pmService.findByKeyLimit(key, 5));
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
            List<IMRel> relList = imRelService.findByItemMId(imId);
            for (IMRel rel : relList) {
                blogController.tmpEyeCatchAmazonSet(im, rel);
            }
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

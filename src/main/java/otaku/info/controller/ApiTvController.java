package otaku.info.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.apache.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.AllArgsConstructor;
import otaku.info.dto.*;
import otaku.info.entity.*;
import otaku.info.form.*;
import otaku.info.service.*;
import otaku.info.setting.Log4jUtils;
import otaku.info.utils.DateUtils;
import otaku.info.utils.ServerUtils;
import otaku.info.utils.StringUtilsMine;

@RestController
@RequestMapping("/api/tv")
@AllArgsConstructor
public class ApiTvController {

    final Logger logger = Log4jUtils.newConsoleCsvAllLogger("ApiTvController");

    @Autowired
    CalendarApiController calendarApiController;

    @Autowired
    ProgramService programService;

    @Autowired
    PMService pmService;

    @Autowired
    PmVerService pmVerService;

    @Autowired
    PMCalService pmCalService;

    @Autowired
    StationService stationService;

    @Autowired
    DelCalService delCalService;

    @Autowired
    RegularPmService regularPmService;

    @Autowired
    DateUtils dateUtils;

    @Autowired
    ServerUtils serverUtils;

    @Autowired
    TextController textController;

    @Autowired
    StringUtilsMine stringUtilsMine;

    /**
     * TV一覧を返す
     *
     * @return リスト
     */
    @GetMapping("/{teamIdStr}")
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
        List<RegPMDto> regPMDtoList = new ArrayList<>();
        for (RegularPM regPm : regPmList) {
            RegPMDto regPMDto = new RegPMDto();
            regPMDto.setRegularPM(regPm);
            regPMDto.setStationMap(stationService.findStationIdNameMap(StringUtilsMine.stringToLongList(regPm.getStationArr())));
            regPMDtoList.add(regPMDto);
        }
        pAllDto.setRegPmList(regPMDtoList);

        // 各チームごとに未確認のprogram数を取得しセット
        Map<Long, Integer> numberMap = programService.getNumbersOfEachTeamIdFutureNotDeletedNoPM();
        pAllDto.setPNumberMap(numberMap);

        logger.debug("fin");
        return ResponseEntity.ok(pAllDto);
    }

    /**
     * regular_pmの登録・更新
     *
     * @return Boolean true: success / false: failed
     */
    @PostMapping("/saveReg")
    public ResponseEntity<Boolean> addNewRegPm(@Valid @RequestBody RegPmForm regPmForm) {
        if (regPmForm == null || regPmForm.getTitle().equals("")) {
            logger.info("RegPmFormの中身が不足でregular_pm登録できません");
            return ResponseEntity.ok(false);
        }

        // 既存データがないかチェック
        Boolean existData = regularPmService.existData(regPmForm.getTitle());
        RegularPM regPm;
        if (!existData) {
            // 新規データの場合
            regPm = new RegularPM();
        } else {
            // 既存データがある場合
            regPm = regularPmService.findById(regPmForm.getRegular_pm_id());
        }

        BeanUtils.copyProperties(regPmForm, regPm);

        // 日付をstringからDateにして詰める
        if (!regPmForm.getStart_date().equals("")) {
            regPm.setStart_date(dateUtils.stringToLocalDateTime(regPmForm.getStart_date(), "yyyy/MM/dd hh:mm"));
        }

        return ResponseEntity.ok(true);
    }

    /**
     * PM+付随データを登録します。すでにPMがある場合は更新
     *
     * @return Boolean true: success / false: failed
     */
    @PostMapping("/")
    public ResponseEntity<Boolean> newPMyVer(@Valid @RequestBody PMVerForm pmVerForm) {
        logger.debug("accepted");
        Boolean updFlg = false;

        try {
            PM pm;
            Program program = programService.findByPId(pmVerForm.getProgram_id());
            RegularPM regularPM = null;

            if (program == null) {
                return ResponseEntity.ok(false);
            }

            if (pmVerForm.getRegular_pm_id() != null) {
                regularPM = regularPmService.findById(pmVerForm.getRegular_pm_id());
            }

            // pm_idが入っていたらverだけ追加処理処理、入っていなかったらpm新規登録とあればver追加処理、と判断（ここではpmのタイトル変更などはできない）
            // まずはpm
            if (pmVerForm.getPm_id() == null || pmVerForm.getPm_id() == 0) {

                // 対象のItemが見つからなかったら処理しません。見つかったら処理する。
                pm = new PM();

                // 上書きしてくれるから新規登録も更新もこれだけでいけるはず
                BeanUtils.copyProperties(pmVerForm, pm);

                if (regularPM != null) {
                    pm.setRegular_pm_id(regularPM.getRegular_pm_id());
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

            // チームの登録を行います
            if (pmVerForm.getTeamArr() != null && !pmVerForm.getTeamArr().equals("")) {
                String teamArr = "";
                if (regularPM == null) {
                    teamArr = pmVerForm.getTeamArr();
                } else {
                    teamArr = StringUtilsMine.elemsToSave(regularPM.getTeamArr(), pmVerForm.getTeamArr());
                }
                pm.setTeamArr(teamArr);
            }

            // メンバーの登録を行います
            if (pmVerForm.getMemArr() != null && !pmVerForm.getMemArr().equals("")) {
                String memArr = "";
                if (regularPM == null) {
                    memArr = pmVerForm.getMemArr();
                } else {
                    memArr = StringUtilsMine.elemsToSave(regularPM.getMemArr(), pmVerForm.getMemArr());
                }
                pm.setTeamArr(memArr);
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
            List<Long> verIdList = verList.stream().map(e -> e.getPm_v_id()).collect(Collectors.toList());

            // TODO: calendar更新
//            List<PMCal> pmCalList = pmCalService.findByVerIdListTeamIdListDelFlg(verIdList, , false);
//            List<PMCal> updCalList = new ArrayList<>();
//
//            // 既存データのないものは作成する
//            for (PMVer ver : verList) {
//                for (PMRel rel : relList) {
//                    if (pmCalList.stream().noneMatch(e -> e.getPm_ver_id().equals(ver.getPm_v_id()) && e.getPm_rel_id().equals(rel.getPm_rel_id()))) {
//                        // 作成条件合致したらまず既存削除データがないか確認する
//                        PMCal delCal = pmCalService.findByVerIdRelIdDelFlg(ver.getPm_v_id(), rel.getPm_rel_id(), true);
//                        if (delCal != null) {
//                            // 既存があればフラグの変更のみ
//                            delCal.setDel_flg(false);
//                            updCalList.add(delCal);
//                        } else {
//                            // 既存がないなら新規作成
//                            PMCal cal = new PMCal(null, ver.getPm_v_id(), rel.getPm_rel_id(), false, null, false);
//                            updCalList.add(cal);
//                        }
//                    }
//                }
//            }

            // 既存データの不要なものは削除する
//            for (PMCal cal : pmCalList) {
//                if (verList.stream().noneMatch(e -> e.getPm_v_id().equals(cal.getPm_ver_id()))
//                        || (verList.stream().anyMatch(e -> e.getPm_v_id().equals(cal.getPm_ver_id())) && relList.stream().noneMatch(e -> e.getPm_rel_id().equals(cal.getPm_rel_id())) )) {
//                    cal.setDel_flg(true);
//                    // TODO: カレンダーを抜きたい
//                    updCalList.add(cal);
//                }
//            }

            // 更新が必要なデータ全部更新。
//            List<PMCal> targetCalList = pmCalService.saveAll(updCalList);

            // googleカレンダー登録のためのデータを用意する
//            for (PMCal cal : targetCalList) {
//                PMVer targetVer2 = verList.stream().filter(e -> e.getPm_v_id().equals(cal.getPm_ver_id())).findFirst().get();
//                PMRel targetRel = relList.stream().filter(e -> e.getPm_rel_id().equals(cal.getPm_rel_id())).findFirst().get();
//                // TODO: ここの処理減らせる。rel使わないから同じverのは1度だけ取得するようにしてあげるといい
//                CalendarInsertDto calendarDto = setGCalDatePm(pm, targetVer2);
//                Event event = calendarApiController.postEvent(TeamEnum.get(targetRel.getTeam_id()).getCalendarId(), calendarDto.getStartDateTime(), calendarDto.getEndDateTime(), pm.getTitle(), calendarDto.getDesc(), calendarDto.getAllDayFlg());
//                cal.setCalendar_id(event.getId());
//                cal.setCal_active_flg(true);
//                pmCalService.save(cal);
//            }

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

        dto.setDesc(pm.getDescription());

        dto.setAllDayFlg(false);
        return dto;
    }

    /**
     * Program一括削除
     *
     * @return Boolean true: success / false: failed
     */
    @PostMapping("/bundle/del_p")
    public ResponseEntity<Boolean> bundleDelP(@Valid @RequestBody Long[] pIdArr) {
        logger.debug("accepted");
        List<Program> pList = programService.findByPidList(Arrays.asList(pIdArr));
        if (pList.size() > 0) {
            pList.forEach(e -> e.setDel_flg(true));
            programService.saveAll(pList);
        }
        return ResponseEntity.ok(true);
    }
}

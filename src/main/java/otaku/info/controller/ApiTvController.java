package otaku.info.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

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
    PMCalService pmCalService;

    @Autowired
    StationService stationService;

    @Autowired
    DelCalService delCalService;

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

        Long teamId = 0L;
        if (StringUtilsMine.isNumeric(teamIdStr)) {
            teamId = Long.parseLong(teamIdStr);
        }

        // teamIdが不正値だったらチームごとの件数だけ取得して返す
        boolean skipItemFlg = false;
        if (teamId < 6L || teamId > 21L) {
            skipItemFlg = true;
        }

        PAllDto pAllDto = new PAllDto();
        if (!skipItemFlg) {
            // PMのないprogramだけを集める
            List<PDto> pDtoList = new ArrayList<>();
            List<Program> pList = null;

            if (teamId == 5L) {
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
                List<RelPmDto> relPmList = new ArrayList<>();

                // 同じ日の同じ放送局のもの
                List<PmFullDto> pmFullDtoList = pmService.findByOnAirDateNotDeleted(p.getOn_air_date());
                for (PmFullDto dto : pmFullDtoList) {
                    RelPmDto relPmDto = new RelPmDto();
                    relPmDto.setPm_id(dto.getPmId().longValue());
                    relPmDto.setTitle(dto.getTitle());
                    relPmDto.setDescription(dto.getDescription());
                    relPmDto.setOn_air_date(dto.getOnAirDate());
                    relPmList.add(relPmDto);
                }

//                relPmList.addAll(pmFullDtoList.stream().map(e -> e.getOnAirDate().toString() + e.getTitle() + e.getDescription()).collect(Collectors.toList()));

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

                // リストに入れる
                pmDtoList.add(dto);
            }
            // 返却リストに入れる
            pAllDto.setPm(pmDtoList);
        }

        // 各チームごとに未確認のprogram数を取得しセット
        Map<Long, Integer> numberMap = programService.getNumbersOfEachTeamIdFutureNotDeletedNoPM();
        pAllDto.setPNumberMap(numberMap);

        logger.debug("fin");
        return ResponseEntity.ok(pAllDto);
    }

    /**
     * PM+付随データを登録します。すでにPMがある場合は更新
     * 放送局追加は判定できないのでフロントでがんばって！
     *
     * @return Boolean true: success / false: failed
     */
    @PostMapping("/")
    public ResponseEntity<Boolean> newPMyVer(@Valid @RequestBody PMVerForm pmVerForm) {
        logger.debug("accepted");

        try {
            System.out.println(pmVerForm.getOn_air_date());
            PM pm;
            Program program = programService.findByPId(pmVerForm.getProgram_id());

            if (program == null) {
                return ResponseEntity.ok(false);
            }

            // pm_idが入っていたらverだけ追加処理処理、入っていなかったらpm新規登録とあればver追加処理、と判断（ここではpmのタイトル変更などはできない）
            // まずはpm
            if (pmVerForm.getPm_id() == null || pmVerForm.getPm_id() == 0) {
                pm = new PM();
            } else {
                pm = pmService.findByPmId(pmVerForm.getPm_id());
            }

            // 上書きしてくれるから新規登録も更新もこれだけでいけるはず
            BeanUtils.copyProperties(pmVerForm, pm);
            //DateTimeFormatterクラスのオブジェクトを生成
            DateTimeFormatter dtFt = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");
            //String型の日付からLocalDateTimeクラスのオブジェクトを生成
            LocalDateTime datePar = LocalDateTime.parse(pmVerForm.getOn_air_date(), dtFt);
            pm.setOn_air_date(datePar);
            System.out.println(pm.getOn_air_date());

            // wordpressでエラーになる記号を処理し、不要な文字を削除して設定し直す
            pm.setTitle(textController.replaceSignals(pm.getTitle()));

            // 登録前に本当に重複登録がないかチェック
            // 同じタイトルのpmがあるなら、登録せずに0番目のpmをセットする
            List<PM> checkedPmList = pmService.findByTitle(pmVerForm.getTitle());
            if (checkedPmList.size() != 0) {
                pm = checkedPmList.get(0);
            }

            // チームの登録を行います
            if (pmVerForm.getTeamArr() != null && !pmVerForm.getTeamArr().equals("")) {
                String teamArr  = pmVerForm.getTeamArr();
                pm.setTeamArr(StringUtilsMine.removeBrackets(teamArr));
            }

            // メンバーの登録を行います
            if (pmVerForm.getMemArr() != null && !pmVerForm.getMemArr().equals("")) {
                String memArr = pmVerForm.getMemArr();
                pm.setMemArr(StringUtilsMine.removeBrackets(memArr));
            }

            // 放送局の登録を行います
            if (pmVerForm.getStation_id() != null && !pmVerForm.getStation_id().equals("")) {
                if (pm.getStationArr() == null || pm.getStationArr().equals("")) {
                    pm.setStationArr("");
                }
                pm.setStationArr(StringUtilsMine.addToStringArr(pm.getStationArr(), pmVerForm.getStation_id()));
            }

            PM savedPm = pmService.save(pm);
            // programのpm_idを登録します
            program.setPm_id(savedPm.getPm_id());
            program.setFct_chk(true);
            programService.save(program);

            // カレンダーを登録・更新する
            // 既存カレンダーデータ取得する
//            List<PMVer> verList = pmVerService.findByPmIdDelFlg(pm.getPm_id(), true);
//            List<Long> verIdList = verList.stream().map(e -> e.getPm_v_id()).collect(Collectors.toList());

            // TODO: calendar更新
//            List<PMCal> pmCalList = pmCalService.findByVerIdListTeamIdListDelFlg(verIdList, , false);
//            List<PMCal> updCalList = new ArrayList<>();

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
            logger.error("TV APIエラー");
            e.printStackTrace();
            return ResponseEntity.ok(false);
        }
    }

    /**
     * PMに放送局を追加して
     * ProgramはPMIDを登録します
     *
     * @param input
     * @return
     */
    @PostMapping("/addStation")
    public ResponseEntity<Boolean> addPMStation(@RequestBody Map<String, Object> input) {
        if (!input.containsKey("pm_id") || !input.containsKey("program_id") || !input.containsKey("station_id")) {
            return ResponseEntity.ok(false);
        }

        PM pm = pmService.findByPmId(Long.parseLong(input.get("pm_id").toString()));
        if (pm != null) {
            if (pm.getStationArr() == null) {
                pm.setStationArr("");
            }
            Long staId = Long.parseLong(input.get("station_id").toString());
            pm.setStationArr(StringUtilsMine.addToStringArr(pm.getStationArr(), staId));
        }
        pmService.save(pm);

        Long programId = Long.parseLong(input.get("program_id").toString());
        Program program = programService.findByPId(programId);
        if (program != null) {
            program.setPm_id(pm.getPm_id());
            programService.save(program);
        }
        return ResponseEntity.ok(true);
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
//
//        dto.setDesc(pm.getDescription());
//
//        dto.setAllDayFlg(false);
//        return dto;
//    }

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

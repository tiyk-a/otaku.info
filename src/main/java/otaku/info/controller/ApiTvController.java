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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.AllArgsConstructor;
import otaku.info.dto.*;
import otaku.info.entity.*;
import otaku.info.enums.MemberEnum;
import otaku.info.enums.TeamEnum;
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
    PageTvService pageTvService;

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
        List<RegPMDto> regPMDtoList = new ArrayList<>();
        for (RegularPM regPm : regPmList) {
            RegPMDto regPMDto = new RegPMDto();
            regPMDto.setRegularPM(regPm);
            regPMDto.setCastList(castService.findIdListByRegPmId(regPm.getRegular_pm_id()));
            regPMDto.setStationMap(regPmStationService.findStationIdListByReguPmId(regPm.getRegular_pm_id()));
            regPMDtoList.add(regPMDto);
        }
        pAllDto.setRegPmList(regPMDtoList);

        // 各チームごとに未確認のprogram数を取得しセット
        Map<BigInteger, BigInteger> numberMap = programService.getNumbersOfEachTeamIdFutureNotDeletedNoPM();
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
    @PostMapping("/")
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

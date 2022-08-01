package otaku.info.controller;

import java.util.*;

import javax.validation.Valid;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.AllArgsConstructor;
import otaku.info.entity.*;
import otaku.info.enums.MemberEnum;
import otaku.info.form.*;
import otaku.info.service.*;
import otaku.info.setting.Log4jUtils;
import otaku.info.utils.DateUtils;
import otaku.info.utils.ServerUtils;
import otaku.info.utils.StringUtilsMine;

@RestController
@RequestMapping("/api/pm")
@AllArgsConstructor
public class ApiPmController {

    final Logger logger = Log4jUtils.newConsoleCsvAllLogger("ApiPmController");

    @Autowired
    ApiTvController apiTvController;

    @Autowired
    BlogController blogController;

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
     * PM一括登録
     *
     * @return Boolean true: success / false: failed
     */
    @PostMapping("/bundle/new")
    public ResponseEntity<Boolean> updBundlePMyRegi(@Valid @RequestBody PMVerForm[] pmVerForms) {
        logger.debug("accepted");

        try {
            for (PMVerForm pmVerForm : pmVerForms) {
                apiTvController.newPMyVer(pmVerForm);
            }
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
    @DeleteMapping("/{id}")
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
     * RegPmを新規登録します
     *
     * @return Boolean
     */
    @PostMapping("/reg/new")
    public ResponseEntity<Boolean> addRegPm(@RequestBody Map<String, Object> input) {

        if (!input.containsKey("title") || !input.containsKey("tm_id_arr")) {
            return ResponseEntity.ok(false);
        }

        RegularPM regPm = null;

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
    @GetMapping("/search/sta")
    public ResponseEntity<List<Station>>  searchStation(@RequestParam("key") String key) {
        return ResponseEntity.ok(stationService.findByName(key));
    }

    /**
     * PMのデータを更新する
     *
     * @param id データ更新をするPMのID
     * @param form 更新される新しいデータ
     * @return Pm
     */
    @PostMapping("/{id}")
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
     * Programにpm_idを追加してfct_chkを更新します（既存pmある場合ですね）
     *
     * @return Boolean true: success / false: failed
     */
    @GetMapping("/chk")
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
     * PMを検索する
     *
     * @param key
     * @return
     */
    @GetMapping("/search")
    public ResponseEntity<List<PM>> searchOtherPm(@RequestParam("key") String key) {
        if (key.equals("") ) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(pmService.findByKeyLimit(key, 5));
    }
}

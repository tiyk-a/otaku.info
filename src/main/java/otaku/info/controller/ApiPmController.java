package otaku.info.controller;

import java.time.LocalDateTime;
import java.util.*;

import javax.validation.Valid;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.AllArgsConstructor;
import otaku.info.entity.*;
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

        // reg_pmの登録
        String title = input.get("title").toString();
        if (!regularPmService.existData(title)) {
            RegularPM newRegPm = new RegularPM();
            newRegPm.setTitle(title);

            List<Integer> castArr = (List<Integer>) input.get("tm_id_arr");
            String teamStr = "";
            String memStr = "";
            for (Integer sta : castArr) {
                Long l = new Long(sta);
                if (l < 30L) {
                    teamStr = StringUtilsMine.addToStringArr(teamStr, l);
                } else {
                    memStr = StringUtilsMine.addToStringArr(memStr, l);
                }
            }

            newRegPm.setTeamArr(StringUtilsMine.removeBrackets(teamStr));
            newRegPm.setMemArr(StringUtilsMine.removeBrackets(memStr));
            // 放送局の登録
            if (input.containsKey("station_id_arr")) {
                try {
                    List<Long> stationIdList = (List<Long>) input.get("station_id_arr");
                    newRegPm.setStationArr(StringUtilsMine.longListToString(stationIdList));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            regularPmService.save(newRegPm);
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
        PM pm = pmService.findByPmId(id);
        Boolean updPmFlg = false;

        if (!form.getTitle().equals(pm.getTitle())) {
            pm.setTitle(form.getTitle());
            updPmFlg = true;
        }

        if (!form.getDescription().equals(pm.getDescription())) {
            pm.setDescription(form.getDescription());
            updPmFlg = true;
        }

        if (!form.getRegular_pm_id().equals(pm.getRegular_pm_id())) {
            pm.setRegular_pm_id(form.getRegular_pm_id());
            updPmFlg = true;
        }

        if (!StringUtilsMine.sameElementArrays(form.getTeamArr(), pm.getTeamArr())) {
            pm.setTeamArr(StringUtilsMine.removeBrackets(form.getTeamArr()));
            updPmFlg = true;
        }

        if (!StringUtilsMine.sameElementArrays(form.getMemArr(), pm.getMemArr())) {
            pm.setMemArr(StringUtilsMine.removeBrackets(form.getMemArr()));
            updPmFlg = true;
        }

        if (updPmFlg) {
            pmService.save(pm);
        }

        // pmver
        List<PMVer> updVList = new ArrayList<>();
        for (Object verObj : form.getVerList()) {
            PMVer ver = new PMVer();

            Map<String, Object> map = new ObjectMapper().convertValue(verObj, Map.class);
            if (map.containsKey("v_id")) {
                ver = pmVerService.findById((Long) map.get("v_id"));
            }

            ver.setPm_id(pm.getPm_id());

            if (map.containsKey("on_air_date")) {
                String dateStr = (String) map.get("on_air_date");
                LocalDateTime localDateTime = dateUtils.stringToLocalDateTime(dateStr, "YYYY-MM-DD HH:mm");
                ver.setOn_air_date(localDateTime);
            }

            if (map.containsKey("station_name")) {
                String stationName = (String) map.get("station_name");
                List<Station> stationList = stationService.findByName(stationName);

                if (stationList.size() > 0) {
                    ver.setStation_id(stationList.get(0).getStation_id());
                }
            }

            if (map.containsKey("del_flg")) {
                ver.setDel_flg((boolean) map.get("del_flg"));
            }

            if (ver.getOn_air_date() != null) {
                updVList.add(ver);
            }
        }

        if (updVList.size() > 0) {
            pmVerService.saveAll(updVList);
        }

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

    /**
     * IMを検索する
     *
     * @param key
     * @return
     */
    @GetMapping("/searchReg")
    public ResponseEntity<List<RegularPM>> searchRegPm(@RequestParam("key") String key) {
        if (key.equals("") ) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(regularPmService.findByKeyLimit(key, 10L));
    }
}

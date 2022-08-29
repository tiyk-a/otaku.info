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
            logger.error("PM APIエラー");
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
            logger.error("PM APIエラー");
            e.printStackTrace();
            return ResponseEntity.ok(false);
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

        if (!StringUtilsMine.sameElementArrays(form.getTeamArr(), pm.getTeamArr())) {
            pm.setTeamArr(StringUtilsMine.removeBrackets(form.getTeamArr()));
            updPmFlg = true;
        }

        if (!StringUtilsMine.sameElementArrays(form.getMemArr(), pm.getMemArr())) {
            pm.setMemArr(StringUtilsMine.removeBrackets(form.getMemArr()));
            updPmFlg = true;
        }

        if (!StringUtilsMine.sameElementArrays(form.getStationArr(), pm.getStationArr())) {
            pm.setStationArr(StringUtilsMine.removeBrackets(form.getStationArr()));
            updPmFlg = true;
        }

        if (updPmFlg) {
            pmService.save(pm);
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
            logger.error("PM APIエラー");
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

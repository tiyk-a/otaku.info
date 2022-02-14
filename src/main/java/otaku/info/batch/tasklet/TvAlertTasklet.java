package otaku.info.batch.tasklet;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import otaku.info.controller.LoggerController;
import otaku.info.controller.PythonController;
import otaku.info.controller.TwTextController;
import otaku.info.entity.PRel;
import otaku.info.entity.Program;
import otaku.info.enums.TeamEnum;
import otaku.info.repository.PRelRepository;
import otaku.info.service.ProgramService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@StepScope
public class TvAlertTasklet implements Tasklet {

    @Autowired
    PythonController pythonController;

    @Autowired
    TwTextController twTextController;

    @Autowired
    LoggerController loggerController;

    @Autowired
    ProgramService programService;

    @Autowired
    PRelRepository pRelRepository;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        // これから1時間以内に放送開始し、チームIDも埋まっているレコードを取得する。
        List<Program> programList = programService.findByOnAirDateTimeTeamId(LocalDateTime.now(), 1);
        loggerController.printTvAlertTasklet("*** program list " + programList.size());
        if (programList.size() > 0) {
            // 嵐のエラーが多いので嵐はちょっと抜くtmp対応
            List<Long> removeList = new ArrayList<>();
            for (Program p : programList) {
                List<PRel> pRelList = pRelRepository.findAllByProgramId(p.getProgram_id());
                boolean postFlg = pRelList.stream().noneMatch(e -> e.getTeam_id().equals(TeamEnum.get("ARASHI").getId()));

                if (!postFlg) {
                    removeList.add(p.getProgram_id());
                }
            }

            programList.removeIf(e -> removeList.contains(e.getProgram_id()));

            loggerController.printTvAlertTasklet("*** post counts " + programList.size());
            // Postする番組の投稿文を作る Map<ProgramId-TeamId, text>
            Map<Long, String> postMap = new HashMap<>();
            programList.forEach(e -> postMap.putAll(twTextController.tvAlert(e)));
            // Post
            if (postMap.size() > 0) {
                for (Map.Entry<Long, String> post : postMap.entrySet()) {
                    pythonController.post(post.getKey(), post.getValue());
                }
            }
        }
        return RepeatStatus.FINISHED;
    }

}
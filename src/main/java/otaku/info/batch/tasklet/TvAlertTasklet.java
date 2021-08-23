package otaku.info.batch.tasklet;

import org.slf4j.Logger;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import otaku.info.controller.PythonController;
import otaku.info.controller.TextController;
import otaku.info.entity.Program;
import otaku.info.searvice.ProgramService;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@StepScope
public class TvAlertTasklet implements Tasklet {

    @Autowired
    PythonController pythonController;

    @Autowired
    TextController textController;

    @Autowired
    ProgramService programService;

    Logger logger = org.slf4j.LoggerFactory.getLogger(TvAlertTasklet.class);

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        logger.info("--- TVアラート START ---");
        // これから1時間以内に放送開始し、チームIDも埋まっているレコードを取得する。
        List<Program> programList = programService.findByOnAirDateTimeTeamId(LocalDateTime.now(), 1);
        if (programList.size() > 0) {
            // Postする番組の投稿文を作る Map<ProgramId-TeamId, text>
            Map<String, String> postMap = new HashMap<>();
            programList.forEach(e -> postMap.putAll(textController.tvAlert(e)));
            // Post
            if (postMap.size() > 0) {
                for (Map.Entry<String, String> post : postMap.entrySet()) {
                    String num = post.getKey();
                    num = num.replaceAll("^.*-", "");
                    pythonController.post(Integer.parseInt(num), post.getValue());
                }
            }
        }
        logger.info("--- TVアラート END ---");
        return RepeatStatus.FINISHED;
    }

}
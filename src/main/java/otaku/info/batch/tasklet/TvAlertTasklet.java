package otaku.info.batch.tasklet;

import org.apache.log4j.Logger;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import otaku.info.controller.PythonController;
import otaku.info.controller.TwTextController;
import otaku.info.entity.Program;
import otaku.info.searvice.ProgramService;
import otaku.info.setting.Log4jUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@StepScope
public class TvAlertTasklet implements Tasklet {

    final Logger logger = Log4jUtils.newConsoleCsvAllLogger();

    @Autowired
    PythonController pythonController;

    @Autowired
    TwTextController twTextController;

    @Autowired
    ProgramService programService;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        // これから1時間以内に放送開始し、チームIDも埋まっているレコードを取得する。
        List<Program> programList = programService.findByOnAirDateTimeTeamId(LocalDateTime.now(), 1);
        if (programList.size() > 0) {
            // Postする番組の投稿文を作る Map<ProgramId-TeamId, text>
            Map<String, String> postMap = new HashMap<>();
            programList.forEach(e -> postMap.putAll(twTextController.tvAlert(e)));
            // Post
            if (postMap.size() > 0) {
                for (Map.Entry<String, String> post : postMap.entrySet()) {
                    String num = post.getKey();
                    num = num.replaceAll("^.*-", "");
                    pythonController.post(Long.parseLong(num), post.getValue());
                }
            }
        }
        return RepeatStatus.FINISHED;
    }

}
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
import otaku.info.entity.*;
import otaku.info.service.PmVerService;

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
    TwTextController twTextController;

    @Autowired
    LoggerController loggerController;

    @Autowired
    PmVerService pmVerService;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        // これから1時間以内に放送開始し、チームIDも埋まっているレコードを取得する。
        List<PMVer> pmVerList = pmVerService.findByOnAirDateNotDeleted(LocalDateTime.now(),1);
        loggerController.printTvAlertTasklet("*** pmVer list " + pmVerList.size());
        if (pmVerList.size() > 0) {
            // Postする番組の投稿文を作る Map<ProgramId-TeamId, text>
            Map<Long, String> postMap = new HashMap<>();
            pmVerList.forEach(e -> postMap.putAll(twTextController.tvAlert(e)));

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
package otaku.info.batch.tasklet;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import otaku.info.controller.LoggerController;
import otaku.info.controller.RakutenController;

@Component
@StepScope
public class UpdateUrlTasklet implements Tasklet {

    @Autowired
    RakutenController rakutenController;

    @Autowired
    LoggerController loggerController;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        boolean result = rakutenController.updateUrl();
        if (result) {
            loggerController.printUpdateUrlTasklet("SUCCESS");
        } else {
            loggerController.printUpdateUrlTasklet("FAILED");
        }
        return RepeatStatus.FINISHED;
    }
}

package otaku.info.batch.tasklet;

import org.apache.log4j.Logger;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import otaku.info.controller.RakutenController;
import otaku.info.setting.Log4jUtils;

@Component
@StepScope
public class UpdateUrlTasklet implements Tasklet {

    final Logger logger = Log4jUtils.newConsoleCsvAllLogger();

    @Autowired
    RakutenController rakutenController;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        logger.debug("--- DB商品アフェリリンク更新 START ---");
        boolean result = rakutenController.updateUrl();
        if (result) {
            logger.debug("SUCCESS");
        } else {
            logger.debug("FAILED");
        }
        logger.debug("--- DB商品アフェリリンク更新 END ---");
        return RepeatStatus.FINISHED;
    }
}

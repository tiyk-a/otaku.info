package otaku.info.batch.tasklet;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import otaku.info.controller.BlogController;
import otaku.info.controller.LoggerController;
import otaku.info.entity.IMRel;
import otaku.info.entity.IM;
import otaku.info.service.IMRelService;
import otaku.info.service.IMService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * データが揃ってるのにブログポストされてないIMがあったら投稿してあげる
 *
 */
@Component
@StepScope
public class BlogCatchupTasklet implements Tasklet {

    @Autowired
    BlogController blogController;

    @Autowired
    LoggerController loggerController;

    @Autowired
    IMService imService;

    @Autowired
    IMRelService imRelService;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {

        List<IM> imList = imService.findFuture();

        for (IM im : imList) {
            List<IMRel> relList = imRelService.findByItemMId(im.getIm_id()).stream().filter(e -> e.getWp_id() == null).collect(Collectors.toList());
            if (relList.size() > 0) {
                blogController.postOrUpdate(im);
            }
        }
        return RepeatStatus.FINISHED;
    }
}

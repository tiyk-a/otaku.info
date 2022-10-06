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
import otaku.info.entity.BlogUpd;
import otaku.info.entity.IM;
import otaku.info.service.BlogUpdService;
import otaku.info.service.IMService;
import otaku.info.utils.ServerUtils;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * ブログを投稿するジョブ
 *
 */
@Component
@StepScope
public class BlogPostTasklet implements Tasklet {

    @Autowired
    BlogUpdService blogUpdService;

    @Autowired
    IMService imService;

    @Autowired
    BlogController blogController;

    @Autowired
    LoggerController loggerController;

    @Autowired
    ServerUtils serverUtils;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {

        // 未更新のもの（created_at = updated_at)を取得
        List<BlogUpd> blogUpdList = blogUpdService.findNotUpdated();
        while (blogUpdList.size() > 0) {
            loggerController.printBlogPostTasklet("*** 投稿ブログあり！ ***");
            mainTask(blogUpdList);
            // 10秒待つ
            serverUtils.sleep();
            blogUpdList = blogUpdService.findNotUpdated();
        }
        return RepeatStatus.FINISHED;
    }

    /**
     * 繰り返す中身の処理
     *
     * @param blogUpdList
     */
    private void mainTask(List<BlogUpd> blogUpdList) {
        List<BlogUpd> updateTargetList = new ArrayList<>();
        for (BlogUpd blogUpd : blogUpdList) {
            IM im = imService.findById(blogUpd.getIm_id());
            if (im != null && im.getIm_id() != null) {
                blogController.postOrUpdate(im);
                Long datetime = System.currentTimeMillis();
                Timestamp timestamp = new Timestamp(datetime);
                blogUpd.setUpdated_at(timestamp);
                updateTargetList.add(blogUpd);
            }
        }

        if (updateTargetList.size() > 0) {
            blogUpdService.saveAll(updateTargetList);
        }
    }
}

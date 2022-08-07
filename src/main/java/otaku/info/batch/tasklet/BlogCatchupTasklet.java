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
import otaku.info.entity.BlogPost;
import otaku.info.entity.IM;
import otaku.info.enums.TeamEnum;
import otaku.info.service.BlogPostService;
import otaku.info.service.IMService;
import otaku.info.utils.StringUtilsMine;

import java.util.List;

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
    BlogPostService blogPostService;

    @Autowired
    IMService imService;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {

        List<IM> imList = imService.findFuture();

        for (IM im : imList) {
            if (im.getTeamArr() != null) {
                for (Long teamId : StringUtilsMine.stringToLongList(im.getTeamArr())) {
                    BlogPost blogPost = blogPostService.findByImIdBlogEnumId(im.getIm_id(), TeamEnum.get(teamId).getBlogEnumId());
                    if (blogPost.getWp_id() == null) {
                        blogController.postOrUpdate(im);
                    }
                }
            }
        }
        return RepeatStatus.FINISHED;
    }
}

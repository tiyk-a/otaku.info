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
import otaku.info.utils.DateUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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

    @Autowired
    DateUtils dateUtils;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        // wpIdがnullで未来発売の商品のimrelを集める
        List<IMRel> imRelList = imRelService.findByWpIdNullPublicationDateFuture(dateUtils.getToday());
        loggerController.printBlogCatchupTaskletLogger("対象imrel:" + imRelList.size());
        // teamId, list<Itemmaster>
        Map<Long, List<IM>> imTeamIdMap = new TreeMap<>();
        for (IMRel imRel : imRelList) {
            List<IM> tmpList = new ArrayList<>();
            if (imTeamIdMap.containsKey(imRel.getTeam_id())) {
                tmpList = imTeamIdMap.get(imRel.getTeam_id());
            }
            IM im = imService.findById(imRel.getIm_id());
            if (im != null) {
                tmpList.add(im);
            }
            imTeamIdMap.put(imRel.getTeam_id(), tmpList);
        }

        loggerController.printBlogCatchupTaskletLogger("ポストありteam数:" + imTeamIdMap.size());
        // List<Itemmaster>, teamId
        for (Map.Entry<Long, List<IM>> e : imTeamIdMap.entrySet()) {
            // TODO: teamid=0Lはあってはいけないはずだがまだいるので処理分割してる
            if (e.getKey() != 0L) {
                Map<Long, Long> imWpMap = blogController.postOrUpdate(e.getValue(), e.getKey());
                loggerController.printBlogCatchupTaskletLogger("teamId:" + e.getKey() + " itemMaster数:" + e.getValue().size());
            } else {
                loggerController.printBlogCatchupTaskletLogger("teamId=0L:" + e.getValue().size());
            }
        }

        //
        return RepeatStatus.FINISHED;
    }
}

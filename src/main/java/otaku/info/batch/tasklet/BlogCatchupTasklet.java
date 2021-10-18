package otaku.info.batch.tasklet;

import org.apache.log4j.Logger;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import otaku.info.controller.BlogController;
import otaku.info.entity.IMRel;
import otaku.info.entity.ItemMaster;
import otaku.info.searvice.IMRelService;
import otaku.info.searvice.ItemMasterService;
import otaku.info.setting.Log4jUtils;
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

    final Logger logger = Log4jUtils.newConsoleCsvAllLogger("BlogCatchupTasklet");

    @Autowired
    BlogController blogController;

    @Autowired
    ItemMasterService itemMasterService;

    @Autowired
    IMRelService imRelService;

    @Autowired
    DateUtils dateUtils;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        // wpIdがnullで未来発売の商品のimrelを集める
        List<IMRel> imRelList = imRelService.findByWpIdNullPublicationDateFuture(dateUtils.getToday());
        logger.debug("対象imrel:" + imRelList.size());
        // teamId, list<Itemmaster>
        Map<Long, List<ItemMaster>> imTeamIdMap = new TreeMap<>();
        for (IMRel imRel : imRelList) {
            List<ItemMaster> tmpList = new ArrayList<>();
            if (imTeamIdMap.containsKey(imRel.getTeam_id())) {
                tmpList = imTeamIdMap.get(imRel.getTeam_id());
            }
            ItemMaster im = itemMasterService.findById(imRel.getItem_m_id());
            if (im != null) {
                tmpList.add(im);
            }
            imTeamIdMap.put(imRel.getTeam_id(), tmpList);
        }

        logger.debug("ポストありteam数:" + imTeamIdMap.size());
        // List<Itemmaster>, teamId
        for (Map.Entry<Long, List<ItemMaster>> e : imTeamIdMap.entrySet()) {
            // TODO: teamid=0Lはあってはいけないはずだがまだいるので処理分割してる
            if (e.getKey() != 0L) {
                blogController.postOrUpdate(e.getValue(), e.getKey());
                logger.debug("teamId:" + e.getKey() + " itemMaster数:" + e.getValue().size());
            } else {
                logger.debug("teamId=0L:" + e.getValue().size());
            }
        }
        return RepeatStatus.FINISHED;
    }
}

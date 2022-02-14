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
import otaku.info.entity.IMRel;
import otaku.info.entity.Item;
import otaku.info.entity.IM;
import otaku.info.enums.TeamEnum;
import otaku.info.service.IMRelService;
import otaku.info.service.IMService;
import otaku.info.service.ItemService;

import java.util.*;

@Component
@StepScope
public class PublishAnnounceTasklet implements Tasklet {

    @Autowired
    PythonController pythonController;

    @Autowired
    TwTextController twTextController;

    @Autowired
    LoggerController loggerController;

    @Autowired
    ItemService itemService;

    @Autowired
    IMService imService;

    @Autowired
    IMRelService IMRelService;

    /**
     * TODO: 今日発売のあるチームだけポストする
     *
     * @param contribution
     * @param chunkContext
     * @return
     * @throws Exception
     */
    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        List<IM> itemMasterList = imService.findReleasedItemList();
        loggerController.printPublishAnnounceTasklet("itemMasterList size: " + itemMasterList.size());
        Integer postCount = 0;

        for (IM itemMaster : itemMasterList) {
            List<IMRel> relList = IMRelService.findByItemMId(itemMaster.getIm_id());
            if (relList.size() > 0) {
                // member違いのrelもあるのでチームごとにrelListをまとめる
                Map<Long, List<IMRel>> teamMemberMap = new TreeMap<>();
                for (IMRel rel : relList) {
                    List<IMRel> tmpList = new ArrayList<>();
                    if (teamMemberMap.containsKey(rel.getTeam_id())) {
                        tmpList = teamMemberMap.get(rel.getTeam_id());
                    }
                    tmpList.add(rel);
                    teamMemberMap.put(rel.getTeam_id(), tmpList);
                }

                Map<Long, List<IMRel>> noTwMap = new HashMap<>();
                for (Map.Entry<Long, List<IMRel>> e : teamMemberMap.entrySet()) {
                    // twIdがない場合
                    if (TeamEnum.get(e.getKey()).getTw_id().equals("")) {
                        noTwMap.put(e.getKey(), e.getValue());
                    } else {
                        Item item = itemService.findByMasterId(itemMaster.getIm_id()).get(0);
                        String text = "";
                        if (item != null) {
                            text = twTextController.releasedItemAnnounce(itemMaster, e.getKey(), item);
                        }
                        pythonController.post(e.getKey(), text);
                        ++postCount;
                    }
                }

                // 個別TWないチームのデータがあったら
                if (noTwMap.size() > 0) {
                    Item item = itemService.findByMasterId(itemMaster.getIm_id()).get(0);
                    String text = "";
                    if (item != null) {
                        text = twTextController.releasedItemAnnounce(itemMaster,7L, item);
                    }
                    pythonController.post(TeamEnum.ABCZ.getId(), text);
                    ++postCount;
                }
            }

            try{
                Thread.sleep(1000);
            }catch(InterruptedException e){
                e.printStackTrace();
            }
        }
        loggerController.printPublishAnnounceTasklet("postCount: " + postCount);
        return RepeatStatus.FINISHED;
    }
}

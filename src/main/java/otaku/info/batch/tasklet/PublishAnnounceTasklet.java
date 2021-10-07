package otaku.info.batch.tasklet;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import otaku.info.controller.PythonController;
import otaku.info.controller.TextController;
import otaku.info.entity.Item;
import otaku.info.entity.ItemMaster;
import otaku.info.searvice.IMRelService;
import otaku.info.searvice.ItemMasterService;
import otaku.info.searvice.ItemService;
import otaku.info.searvice.TeamService;

import java.util.ArrayList;
import java.util.List;

@Component
@StepScope
public class PublishAnnounceTasklet implements Tasklet {

    @Autowired
    PythonController pythonController;

    @Autowired
    TextController textController;

    @Autowired
    ItemService itemService;

    @Autowired
    TeamService teamService;

    @Autowired
    ItemMasterService itemMasterService;

    @Autowired
    IMRelService IMRelService;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        System.out.println("--- 商品発売日アナウンス START ---");
        List<ItemMaster> itemMasterList = itemMasterService.findReleasedItemList();

        for (ItemMaster itemMaster : itemMasterList) {
            List<Item> itemList = itemService.findByMasterId(itemMaster.getItem_m_id());
            List<Long> teamIdList = IMRelService.findTeamIdListByItemMId(itemMaster.getItem_m_id());
            if (teamIdList.size() > 0) {
                // 固有Twitterのないチームの投稿用オブジェクト
                List<Long> noTwitterTeamIdList = new ArrayList<>();

                for (Long teamId : teamIdList) {
                    String twId = teamService.getTwitterId(teamId);
                    if (twId == null) {
                        noTwitterTeamIdList.add(teamId);
                    } else {
                        String text = textController.releasedItemAnnounce(itemMaster, itemList.get(0));
                        pythonController.post(Math.toIntExact(teamId), text);
                    }
                }

                if (noTwitterTeamIdList.size() > 0) {
                    String text = textController.releasedItemAnnounce(itemMaster, itemList.get(0));
                    pythonController.post(Math.toIntExact(noTwitterTeamIdList.get(0)), text);
                }
            }

            try{
                Thread.sleep(1000);
            }catch(InterruptedException e){
                e.printStackTrace();
            }
        }
        System.out.println("--- 商品発売日アナウンス END ---");
        return RepeatStatus.FINISHED;
    }
}

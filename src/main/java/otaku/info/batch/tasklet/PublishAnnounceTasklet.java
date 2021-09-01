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
import otaku.info.dto.TwiDto;
import otaku.info.entity.Item;
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

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        System.out.println("--- 商品発売日アナウンス START ---");
        List<Item> itemList = itemService.findReleasedItemList();
        for (Item item : itemList) {
            // 一つの商品に複数チームが登録されている場合、固有のTwitterがあるチームはそれぞれ投稿、固有Twitterがないチームはタグとチーム名全部つけて１つ投稿
            String[] teamIdArr = item.getTeam_id().split(",");
            if (teamIdArr.length > 1) {
                // 固有Twitterのないチームの投稿用オブジェクト
                List<Long> noTwitterTeamIdList = new ArrayList<>();

                for (String idStr : teamIdArr) {
                    long teamId = Long.parseLong(idStr);
                    String twId = teamService.getTwitterId(teamId);
                    if (twId == null) {
                        noTwitterTeamIdList.add(teamId);
                    } else {
                        String text = textController.releasedItemAnnounce(item);
                        pythonController.post(Math.toIntExact(teamId), text);
                    }
                }

                if (noTwitterTeamIdList.size() > 0) {
                    String text = textController.releasedItemAnnounce(item, noTwitterTeamIdList);
                    pythonController.post(Math.toIntExact(noTwitterTeamIdList.get(0)), text);
                }
            } else {
                // チームが１つしかなかったらそのまま投稿
                long teamId = Long.parseLong(teamIdArr[teamIdArr.length - 1]);
                TwiDto twiDto = new TwiDto(item.getTitle(), item.getUrl(), item.getPublication_date(), null, teamId);
                pythonController.post(Math.toIntExact(teamId), textController.futureItemReminder(twiDto));
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

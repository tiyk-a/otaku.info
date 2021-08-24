package otaku.info.batch.tasklet;

import org.slf4j.Logger;
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
import otaku.info.utils.DateUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
@StepScope
public class FutureItemReminderTasklet implements Tasklet {

    @Autowired
    PythonController pythonController;

    @Autowired
    TextController textController;

    @Autowired
    ItemService itemService;

    @Autowired
    TeamService teamService;

    Logger logger = org.slf4j.LoggerFactory.getLogger(FutureItemReminderTasklet.class);

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        logger.info("--- 未発売商品リマインダー START ---");
        // 1年以内に発売される商品リストを取得
        List<Item> itemList = itemService.findFutureItemByDate(365);
        for (Item item : itemList) {
            // 10日以上先の商品はキリのいい日のみポストする
            if (item.getPublication_date().compareTo(DateUtils.daysAfterToday(10)) > 0) {
                // 100で割り切れる日数の時
                if (item.getPublication_date().compareTo(new Date()) % 100 == 0) {
                    post(item);
                } else if (item.getPublication_date().compareTo(DateUtils.daysAfterToday(100)) < 0) {
                    // 残り100日以下で10日刻み
                    if (item.getPublication_date().compareTo(new Date()) % 10 == 0) {
                        post(item);
                    }
                }
            } else {
                // 10日以下だったら毎日ポストする
                post(item);
            }
        }
        logger.info("--- 未発売商品リマインダー END ---");
        return RepeatStatus.FINISHED;
    }

    private void post(Item item) throws Exception {
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
                    TwiDto twiDto = new TwiDto(item.getTitle(), item.getUrl(), item.getPublication_date(), null, teamId);
                    pythonController.post(Math.toIntExact(teamId), textController.futureItemReminder(twiDto));
                }
            }

            if (noTwitterTeamIdList.size() > 0) {
                TwiDto twiDto = new TwiDto(item.getTitle(), item.getUrl(), item.getPublication_date(), null, null);
                pythonController.post(Math.toIntExact(noTwitterTeamIdList.get(0)), textController.futureItemReminder(twiDto, noTwitterTeamIdList));
            }
        } else {
            // チームが１つしかなかったらそのまま投稿
            long teamId = Long.parseLong(teamIdArr[teamIdArr.length - 1]);
            TwiDto twiDto = new TwiDto(item.getTitle(), item.getUrl(), item.getPublication_date(), null, teamId);
            pythonController.post(Math.toIntExact(teamId), textController.futureItemReminder(twiDto));
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

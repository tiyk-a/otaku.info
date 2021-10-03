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
import otaku.info.searvice.ItemRelationService;
import otaku.info.searvice.ItemService;
import otaku.info.searvice.TeamService;
import otaku.info.utils.ItemUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @Autowired
    ItemRelationService itemRelationService;

    @Autowired
    ItemUtils itemUtils;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        System.out.println("--- 未発売商品リマインダー START ---");
        // 10日以内に発売される商品リストを取得(round処理は削除なしそのまま使用)
        List<Item> itemList = itemUtils.roundByPublicationDate(itemService.findFutureItemByDate(10));

        // 商品マスタ：その商品リストのマップ
        Map<ItemMaster, List<Item>> itemMap = itemUtils.groupItem(itemList);

        for (Map.Entry<ItemMaster, List<Item>> e : itemMap.entrySet()) {
            post(e.getKey(), e.getValue());
        }
        System.out.println("--- 未発売商品リマインダー END ---");
//        System.out.println("--- TMP追加：マスタ商品がない商品はマスタを探して登録する START ---");
//        List<Item> tmpList = itemService.findNotDeleted();
//        blogController.tmpItemPost(tmpList);
//        System.out.println("--- TMP追加：マスタ商品がない商品はマスタを探して登録する END ---");
        return RepeatStatus.FINISHED;
    }

    private void post(ItemMaster itemMaster, List<Item> itemList) throws Exception {
        // 一つの商品に複数チームが登録されている場合、固有のTwitterがあるチームはそれぞれ投稿、固有Twitterがないチームはタグとチーム名全部つけて１つ投稿
        List<Long> teamIdArr = new ArrayList<>();
        itemList.forEach(e -> teamIdArr.addAll(itemRelationService.getTeamIdListByItemId(e.getItem_id())));
        itemList = itemList.stream().distinct().collect(Collectors.toList());

        if (teamIdArr.size() > 1) {
            // 固有Twitterのないチームの投稿用オブジェクト
            List<Long> noTwitterTeamIdList = new ArrayList<>();

            // 固有Twitterのあるなしで分ける
            for (Long teamId : teamIdArr) {
                String twId = teamService.getTwitterId(teamId);
                if (twId == null) {
                    // 固有アカウントがない場合
                    noTwitterTeamIdList.add(teamId);
                } else {
                    // 固有アカウントがある場合
                    String text = textController.futureItemReminder(itemMaster, itemList.get(0), teamId);
                    pythonController.post(Math.toIntExact(teamId), text);
                }
            }

            // 固有Twitterなしチームがある場合はここでまとめて投稿する
            if (noTwitterTeamIdList.size() > 0) {
                String text = textController.futureItemReminder(itemMaster, itemList.get(0), noTwitterTeamIdList);
                pythonController.post(Math.toIntExact(noTwitterTeamIdList.get(0)), text);
            }
        } else if (teamIdArr.size() == 1) {
            // チームが１つしかなかったらそのまま投稿
            long teamId = teamIdArr.get(0);
            String text = textController.futureItemReminder(itemMaster, itemList.get(0), teamId);
            pythonController.post(Math.toIntExact(teamId), text);
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

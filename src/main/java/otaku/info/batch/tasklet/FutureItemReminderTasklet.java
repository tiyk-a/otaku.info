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
import otaku.info.controller.RakutenController;
import otaku.info.controller.TwTextController;
import otaku.info.entity.Item;
import otaku.info.entity.IM;
import otaku.info.enums.TeamEnum;
import otaku.info.service.IMService;
import otaku.info.service.ItemService;

import java.util.List;
import java.util.stream.Collectors;

@Component
@StepScope
public class FutureItemReminderTasklet implements Tasklet {

    @Autowired
    RakutenController rakutenController;

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

    /**
     * TODO: 日数ではなくteamごとに件数指定で取得、全チームの情報を流すように変更します。
     *
     * @param contribution
     * @param chunkContext
     * @return
     * @throws Exception
     */
    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        for (TeamEnum e : TeamEnum.values()) {
            // 7日以内に発売の商品は絶対投稿したい。それ以降のやつは毎日ポストしないでいい。5で割れる日だけでいい
            List<IM> imList = imService.findNearFutureIMByTeamId(e.getId());
            loggerController.printFutureItemReminderTasklet(e.getName() + "imList size: " + imList.size());

            post(imList, e);
//        logger.debug("--- TMP追加：マスタ商品がない商品はマスタを探して登録する START ---");
//        List<Item> tmpList = itemService.findNotDeleted();
//        blogController.tmpItemPost(tmpList);
//        logger.debug("--- TMP追加：マスタ商品がない商品はマスタを探して登録する END ---");
        }
        return RepeatStatus.FINISHED;
    }

    private void post(List<IM> imList, TeamEnum teamEnum) throws Exception {

        // TODO: 未来商品が全くないチームについての処理
        if (imList.isEmpty()) {
            loggerController.printFutureItemReminderTasklet(teamEnum.getName() + "imList empty");
        }

        Integer postCount = 0;
        for (IM im : imList) {
            // TODO: メンバー名を取得していない
            Item item = null;
            String rakutenUrl = "";
            List<Item> itemList = itemService.findByMasterId(im.getIm_id());
            Boolean findRakutenFlg = false;
            if (itemList != null && itemList.size() > 0) {
                // tweetするときは楽天のアフィリURLに飛ばしたいため①IMにひもづくitemがyahooしかないなら、楽天で検索しURLを取得する②楽天のURLは有効であるか確認してから投稿する
                List<Item> rakutenList = itemList.stream().filter(e -> e.getSite_id().equals(1)).collect(Collectors.toList());
                findRakutenFlg = rakutenList.size() == 0;
                if (!findRakutenFlg) {
                    rakutenUrl = rakutenController.findAvailableRakutenUrl(rakutenList.stream().map(Item::getItem_code).collect(Collectors.toList()), teamEnum.getId());
                } else {
                    rakutenUrl = rakutenController.findRakutenUrl(im.getTitle(), teamEnum.getId());
                    item.setUrl(rakutenUrl);
                }
            }
            String text = "";
            if (item != null) {
                text = twTextController.futureItemReminder(im, teamEnum.getId(), rakutenUrl);
            }
            pythonController.post(teamEnum.getId(), text);
            ++postCount;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        loggerController.printFutureItemReminderTasklet("postCount: " + postCount);
    }
}

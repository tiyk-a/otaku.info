package otaku.info.batch.tasklet;

import org.apache.log4j.Logger;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import otaku.info.controller.PythonController;
import otaku.info.controller.TwTextController;
import otaku.info.entity.Item;
import otaku.info.entity.ItemMaster;
import otaku.info.enums.TeamEnum;
import otaku.info.searvice.ItemMasterService;
import otaku.info.searvice.ItemService;
import otaku.info.setting.Log4jUtils;

import java.util.List;

@Component
@StepScope
public class FutureItemReminderTasklet implements Tasklet {

    final Logger logger = Log4jUtils.newConsoleCsvAllLogger("FutureItemReminderTasklet");

    @Autowired
    PythonController pythonController;

    @Autowired
    TwTextController twTextController;

    @Autowired
    ItemService itemService;

    @Autowired
    ItemMasterService itemMasterService;

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
            // 10日以内に発売される商品リストを取得(round処理は削除なしそのまま使用)
            List<ItemMaster> imList = itemMasterService.findNearFutureIMByTeamId(e.getId());
            logger.debug(e.getName() + "imList size: " + imList.size());

            post(imList, e);
//        logger.debug("--- TMP追加：マスタ商品がない商品はマスタを探して登録する START ---");
//        List<Item> tmpList = itemService.findNotDeleted();
//        blogController.tmpItemPost(tmpList);
//        logger.debug("--- TMP追加：マスタ商品がない商品はマスタを探して登録する END ---");
        }
        return RepeatStatus.FINISHED;
    }

    private void post(List<ItemMaster> imList, TeamEnum teamEnum) throws Exception {

        // TODO: 未来商品が全くないチームについての処理
        if (imList.isEmpty()) {
//            pythonController.post(teamEnum.getId(), teamEnum.getName() + "の新商品情報は");
            logger.debug(teamEnum.getName() + "imList empty");
        }

        Integer postCount = 0;
        for (ItemMaster im : imList) {
            // TODO: メンバー名を取得していない
            Item item = null;
            List<Item> itemList = itemService.findByMasterId(im.getItem_m_id());
            if (itemList != null && itemList.size() > 0) {
                item = itemList.get(0);
            }
            String text = "";
            if (item != null) {
                text = twTextController.futureItemReminder(im, teamEnum.getId(), item);
            }
            pythonController.post(teamEnum.getId(), text);
            ++postCount;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        logger.debug("postCount: " + postCount);
    }
}

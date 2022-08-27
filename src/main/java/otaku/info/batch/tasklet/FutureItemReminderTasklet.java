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
import otaku.info.entity.IM;
import otaku.info.enums.TeamEnum;
import otaku.info.service.IMService;
import otaku.info.utils.StringUtilsMine;

import java.util.List;

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
        }
        return RepeatStatus.FINISHED;
    }

    private void post(List<IM> imList, TeamEnum teamEnum) throws Exception {

        // 未来商品が全くないチームは現状ポストなし
        if (imList.isEmpty()) {
            loggerController.printFutureItemReminderTasklet(teamEnum.getName() + "imList emptyのためno post");
        }

        Integer postCount = 0;
        for (IM im : imList) {

            // メンバー名を取得
            List<Long> memIdList = StringUtilsMine.stringToLongList(im.getMemArr());

            String rakutenUrl = "";
            if (im.getRakuten_url() == null || im.getRakuten_url().equals("")) {
                rakutenUrl = rakutenController.findRakutenUrl(im.getTitle(), teamEnum.getId());
            } else {
                rakutenUrl = im.getRakuten_url();
            }

            String text = "";
            if (rakutenUrl != null) {
                text = twTextController.futureItemReminder(im, teamEnum.getId(), im.getRakuten_url());
                pythonController.post(teamEnum.getId(), text);
                ++postCount;
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                loggerController.printFutureItemReminderTasklet("リマインダーエラー");
                e.printStackTrace();
            }
        }
        loggerController.printFutureItemReminderTasklet("postCount: " + postCount);
    }
}

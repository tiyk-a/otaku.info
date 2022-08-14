package otaku.info.batch.tasklet;

import org.codehaus.jettison.json.JSONException;
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

import java.util.*;

/**
 * 出版通知処理
 *
 */
@Component
@StepScope
public class PublishAnnounceTasklet implements Tasklet {

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
     *
     * @param contribution
     * @param chunkContext
     * @return
     * @throws Exception
     */
    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        List<IM> imList = imService.findReleasedItemList();
        loggerController.printPublishAnnounceTasklet("imlist size: " + imList.size());
        Integer postCount = 0;

        for (IM im : imList) {

            // general twitter用のチーム・メンバーリストを用意→まとめてポスト
            Long teamIdHead = null;
            List<Long> teamIdList = new ArrayList<>();
            List<Long> memIdList = StringUtilsMine.stringToLongList(im.getMemArr());

            if (im.getTeamArr() != null && !im.getTeamArr().equals("")) {

                for (Long teamId : StringUtilsMine.stringToLongList(im.getTeamArr())) {

                    // general twitter使うチームならリストにGU追加して終わり。自分のtwあるならポストに向かう
                    if (TeamEnum.get(teamId).getTw_id().equals("")) {
                        if (teamIdHead == null) {
                            teamIdHead = teamId;
                        }
                        teamIdList.add(teamId);
                    } else {
                        List<Long> teamIdList2 = new ArrayList<>();
                        teamIdList2.add(teamId);
                        post(im, teamId, teamIdList2);
                        ++postCount;
                    }
                }

                // general twitterへのポストが必要な時、ポストする
                if (teamIdList.size() > 0) {
                    post(im, teamIdHead, teamIdList);
                }
            }

            try{
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        loggerController.printPublishAnnounceTasklet("postCount: " + postCount);
        return RepeatStatus.FINISHED;
    }

    /**
     *
     * @param im データもと
     * @param teamId Twitteｒポストの際にキーとするteam
     * @param teamIdList テキストに入れるteamのリスト
     * @throws JSONException
     * @throws InterruptedException
     */
    private void post(IM im, Long teamId, List<Long> teamIdList) throws JSONException, InterruptedException {

        String url = StringUtilsMine.getAmazonLinkFromCard(im.getAmazon_image()).orElse(null);
        if (url == null) {
            if (im.getRakuten_url().equals("")) {
                url = rakutenController.findRakutenUrl(im.getTitle(), teamId);
            } else {
                url = im.getRakuten_url();
            }
        }

        // text作ってポストする
        String text = twTextController.releasedItemAnnounce(im, url);
        pythonController.post(teamId, text);
    }
}

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
import otaku.info.entity.IMRel;
import otaku.info.entity.IMRelMem;
import otaku.info.entity.Item;
import otaku.info.entity.IM;
import otaku.info.enums.TeamEnum;
import otaku.info.service.IMRelMemService;
import otaku.info.service.IMRelService;
import otaku.info.service.IMService;
import otaku.info.service.ItemService;

import java.util.*;
import java.util.stream.Collectors;

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
    ItemService itemService;

    @Autowired
    IMService imService;

    @Autowired
    IMRelService IMRelService;

    @Autowired
    IMRelMemService imRelMemService;

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
        loggerController.printPublishAnnounceTasklet("itemMasterList size: " + imList.size());
        Integer postCount = 0;

        for (IM itemMaster : imList) {
            List<IMRel> relList = IMRelService.findByItemMId(itemMaster.getIm_id());

            // general twitter用のチーム・メンバーリストを用意→まとめてポスト
            Long teamIdHead = null;
            List<Long> teamIdList = new ArrayList<>();
            List<Long> memIdList = new ArrayList<>();

            if (relList.size() > 0) {

                for (IMRel rel : relList) {
                    List<IMRelMem> memList = imRelMemService.findByImRelIdNotDeleted(rel.getIm_rel_id());

                    // general twitter使うチームならリストにGU追加して終わり。自分のtwあるならポストに向かう
                    if (TeamEnum.get(rel.getTeam_id()).getTw_id().equals("")) {
                        if (teamIdHead == null) {
                            teamIdHead = rel.getTeam_id();
                        }
                        teamIdList.add(rel.getTeam_id());
                        if (memList != null && memList.size() > 0) {
                            memIdList.addAll(memList.stream().map(IMRelMem::getMember_id).collect(Collectors.toList()));
                        }
                    } else {
                        List<Long> teamIdList2 = new ArrayList<>();
                        teamIdList2.add(rel.getTeam_id());
                        post(itemMaster, rel.getTeam_id(), teamIdList2);
                        ++postCount;
                    }
                }

                // general twitterへのポストが必要な時、ポストする
                if (teamIdList.size() > 0) {
                    post(itemMaster, teamIdHead, teamIdList);
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
        List<Item> itemList = itemService.findByMasterId(im.getIm_id());
        String rakutenUrl = rakutenController.findAvailableRakutenUrl(itemList.stream().map(Item::getItem_code).collect(Collectors.toList()), teamId);
        // text作ってポストする
        String text = twTextController.releasedItemAnnounce(im, teamIdList, rakutenUrl);
        pythonController.post(teamId, text);
    }
}
